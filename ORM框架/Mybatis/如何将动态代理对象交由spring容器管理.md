# 如何将动态代理对象交由spring容器管理

- ### spring bean的实例化过程

- ### FactoryBean

- ### BeanFactoryPostProcessor

- ### ImportBeanDefinitionRegistrar

  ImportBeanDefinitionRegistrar可以动态将自己的对象注册到BeanDefinition，然后会spring的bean实例化流程，生成实例对象到ioc容器

  - 编写测试Dao接口，为什么要是接口呢？因为我们要利用代理生成Dao的实例对象啊

    ```java
    public interface MyDao {
        void query();
    }
    ```

  - 编写自定义Registrar

    ```java
    public class MyImportBeanDefinitionRegistrar implements ImportBeanDefinitionRegistrar {
        @Override
        public void registerBeanDefinitions(AnnotationMetadata annotationMetadata, BeanDefinitionRegistry beanDefinitionRegistry) {
            这里用到前面定义的MyFactoryBean
            BeanDefinitionBuilder builder = BeanDefinitionBuilder.genericBeanDefinition(MyFactoryBean.class);
            //生成beanDefinition
            GenericBeanDefinition beanDefinition = (GenericBeanDefinition)builder.getBeanDefinition();
            //将beanDefinition注册
            beanDefinitionRegistry.registerBeanDefinition(MyDao.class.getName(),beanDefinition);
        }
    }
    ```

  - 更改MyFactoryBean,动态代理生成接口MyDao对象

    ```java
    public class MyFactoryBean implements FactoryBean {
     
        @Override
        public Object getObject() throws Exception {
            //利用动态代理生成MyDao的实例对象
            Object instance = Proxy.newProxyInstance(MyFactoryBean.class.getClassLoader(), new Class[]{MyDao.class}, new InvocationHandler(){
                @Override
                public Object invoke(Object proxy, Method method, Object[] args) throws Throwable {
                    System.out.println(”执行业务逻辑“);
                    return null;
                }
            });
            return instance;
        }
     
        @Override
        public Class<?> getObjectType() {
            return MyDao.class;
        }
    }
    ```

  - 自定义注解@MyScan，并通过@Import导入MyImportBeanDefinitionRegistrar。这样就会被spring扫描到

    ```java
    @Retention(RetentionPolicy.RUNTIME)
    @Target({ElementType.TYPE})
    @Documented
    @Import({MyImportBeanDefinitionRegistrar.class})
    public @interface MyScan {
    }
    ```

  - 最后一步，在项目启动类加上@MyScan。并编写测试。调用Mydao

    ```java
    MyDao myDao = SpringContextUtils.getBean(MyDao.class);
    myDao.execute();  //打印出”执行业务逻辑“
    ```

    

- ### 简单模拟Mybaitis中的动态代理Mapper接口执行sql

  我们引申一下。我们不是有一个MyDao吗?并且在MyFactoryBean中代理实现的时候也是讲其硬编码写死的。MyImportBeanDefinitionRegistrar中也是写死的，这样可不行，那么我们要怎么将其写活呢。

  - 在MyFactoryBean定义变量来接受class，并通过构造函数设置值。最后修改后的MyFactoryBean如下

    ```java
    public class MyFactoryBean implements FactoryBean {
     
        private Class classzz;
        public MyFactoryBean(Class classzz){
            this.classzz = classzz;
        }
       @Override
        public Object getObject() throws Exception {
            //利用动态代理生成实例对象
            Object instance = Proxy.newProxyInstance(MyFactoryBean.class.getClassLoader(), new Class[]{classzz.class}, new InvocationHandler(){
                @Override
                public Object invoke(Object proxy, Method method, Object[] args) throws Throwable {
                    System.out.println(”执行业务逻辑“);
                    return null;
                }
            });
            return instance;
        }
        @Override
        public Class<?> getObjectType() {
            return this.classzz;
        }
    }
    ```

  - 更改MyImportBeanDefinitionRegistrar逻辑,我们定义一个Class数据来模拟多个class。通过beanDefinition.getConstructorArgumentValues().addGenericArgumentValue(aClass.getTypeName());调用MyFactoryBean的有参构造函数生成MyFactoryBean。

    ```java
    public class MyImportBeanDefinitionRegistrar implements ImportBeanDefinitionRegistrar {
        @Override
        public void registerBeanDefinitions(AnnotationMetadata annotationMetadata, BeanDefinitionRegistry beanDefinitionRegistry) {
            //这里将数组写死了。我们可以定义一个包，扫描包下的所有接口class，这里就不做实现了，这里为了演示效果，多定义了一个接口MyDao1，跟MyDao定义相同的，代码就不贴出来了。
            Class[] classes = {MyDao.class,MyDao1.class};
            for (Class aClass : classes) {
                BeanDefinitionBuilder builder = BeanDefinitionBuilder.genericBeanDefinition(MyFactoryBean.class);
                GenericBeanDefinition beanDefinition = (GenericBeanDefinition)builder.getBeanDefinition();
                //调用刚刚定义的MyFactoryBean有参构造函数
                beanDefinition.getConstructorArgumentValues().addGenericArgumentValue(aClass.getTypeName());
                beanDefinitionRegistry.registerBeanDefinition(aClass.getName(),beanDefinition);
            }
        }
    }
    ```

  - 测试实例

    ```java
    MyDao myDao = SpringContextUtils.getBean(MyDao.class);
    myDao.query(); //执行业务逻辑
    MyDao1 myDao1 = SpringContextUtils.getBean(MyDao1.class);
    myDao1.query(); //执行业务逻辑
    ```

    有没有感觉到有点类似mybatis了，接口Mapper，没有任何实现，但是可以直接@Autowired进行调用，没错，就是在模拟Mybatis。不过，我们自己定义的@MyScan注解，它的是@MapperScan注解，后面参数为Mapper的包路径，我们这里就没有实现类，因为我们在MyImportBeanDefinitionRegistrar中定义数组来模拟包路径扫描class了。



### 调用了Dao接口都是同一个，应该执行不同的sql查询，如何实现？

- 自定义@Select注解，这个注解就是用在Dao接口方法定义上的,value为sql语句

  ```java
  @Target(ElementType.METHOD)
  @Retention(RetentionPolicy.RUNTIME)
  @Documented
  public @interface Select {
      String value() default "";
  }
  ```

- 在Dao接口中使用@Select注解

  ```java
  public interface MyDao {
      @Select("SELECT * FROM T1")
      void query();
  }
   
  public interface MyDao1 {
      @Select("SELECT * FROM T2")
      void query();
  }
  ```

- 在动态代理生成代理对象的InvocationHandler编写具体获取sql逻辑

  ```java
  public Object invoke(Object proxy, Method method, Object[] args) throws Throwable {
          String value = method.getDeclaredAnnotation(Select.class).value();
          System.out.println(value);
          return null;
      }
  ```

- 调用刚刚刚刚的测试方法，打印sql语句

  ```java
  MyDao myDao = SpringContextUtils.getBean(MyDao.class);
  myDao.query(); //SELECT * FROM T1
  MyDao1 myDao1 = SpringContextUtils.getBean(MyDao1.class);
  myDao1.query(); //SELECT * FROM T2
  ```

  



# 自己的概述

- Mybatis将Dap注册到Spring的ioc，是通过ImportBeanDefinitionRegister动态的将自己的class的BeanDefinition注册到DefinitionMapper里的。

- 而在BeanDefinition里，Key是这个特定Dao的clazz对象，而Value为Mybatis的BeanFactory所生成的BeanDefition。所以对Dao的getBean方法中创建实例的逻辑是调用这个FactoryBean完成的。

- FactoryBean接收传入的Dao的clazz，有getObject和getClass接口。其中getObject接口是通过动态代理实现！
- getObject通过动态代理，那么其中的InvoketionHandler是如何的呢？crud业务逻辑如何实现？这里就看之前拉钩里的！！！每个mapper对应一个MapperPoxyFactory，在MapperPoxyFactory其中实现了动态代理：
  - 如果是Object
  - 如果不是。则判断command是crud哪一个，调用Connection中对应crud方法处理！！！







# 参考

- <https://blog.csdn.net/chuta9217/article/details/100617763>

