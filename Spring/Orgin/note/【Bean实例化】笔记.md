# Bean实例化

![](https://img-blog.csdnimg.cn/2018110615394396.png?x-oss-process=image/watermark,type_ZmFuZ3poZW5naGVpdGk,shadow_10,text_aHR0cHM6Ly9ibG9nLmNzZG4ubmV0L3p6dWNoZW55Yg==,size_16,color_FFFFFF,t_70)



```java
// AbstractAutowireCapableBeanFactory.java

protected Object initializeBean(final String beanName, final Object bean, @Nullable RootBeanDefinition mbd) {
    if (System.getSecurityManager() != null) { // 安全模式
        AccessController.doPrivileged((PrivilegedAction<Object>) () -> {
            // <1> 激活 Aware 方法，对特殊的 bean 处理：Aware、BeanClassLoaderAware、BeanFactoryAware
            invokeAwareMethods(beanName, bean);
            return null;
        }, getAccessControlContext());
    } else {
        // <1> 激活 Aware 方法，对特殊的 bean 处理：Aware、BeanClassLoaderAware、BeanFactoryAware
        invokeAwareMethods(beanName, bean);
    }

    // <2> 后处理器，before
    Object wrappedBean = bean;
    if (mbd == null || !mbd.isSynthetic()) {
        wrappedBean = applyBeanPostProcessorsBeforeInitialization(wrappedBean, beanName);
    }

    // <3> 激活用户自定义的 init 方法
    try {
        invokeInitMethods(beanName, wrappedBean, mbd);
    } catch (Throwable ex) {
        throw new BeanCreationException(
                (mbd != null ? mbd.getResourceDescription() : null),
                beanName, "Invocation of init method failed", ex);
    }

    // <2> 后处理器，after
    if (mbd == null || !mbd.isSynthetic()) {
        wrappedBean = applyBeanPostProcessorsAfterInitialization(wrappedBean, beanName);
    }

    return wrappedBean;
}
```

Bean已经被实例化，依赖处理，属性注入，现在在这里初始化。

- `<1>` 激活 **Aware** 方法。`invokeAwareMethods(beanName, bean)`
- `<2>` **后置处理器的应用——before**  a`pplyBeanPostProcessorsBeforeInitialization(wrappedBean, beanName);`
- `<3>` 激活**自定义的 `init` 方法**   `invokeInitMethods(beanName, wrappedBean, mbd);`
- `<2>` **后置处理器的应用——after**    `applyBeanPostProcessorsAfterInitialization(wrappedBean, beanName);`



# 注意点

- 在 `invokeInitMethods(beanName, wrappedBean, mbd);`里核心为`afterPropertiesSet`和`invokeCustomInitMethod(beanName, bean, mbd);`
  - 首先，检查是否为 `InitializingBean` 。如果**是**的话，需要执行 `#afterPropertiesSet()` 方法，因为我们除了可以使用 `init-method` 来自定初始化方法外，还可以实现 `InitializingBean` 接口。接口仅有一个 `#afterPropertiesSet()` 方法。
  - 两者的执行先后顺序是先 `<1>` 的 `#afterPropertiesSet()` 方法，后 `<2>` 的 `init-method` 对应的方法。





# Aware

- **功能**

  将spring容器的资源在初始化时set进实现Aware的Bean

  之后通过**回调**的方式实现容器通知

- **spring 提供的aware的接口：**

  > BeanNameAware ：可以获取容器中bean的名称
  >
  > BeanFactoryAware:获取当前bean factory这也可以调用容器的服务
  >
  > ApplicationContextAware： 当前的applicationContext， 这也可以调用容器的服务
  >
  > MessageSourceAware：获得message source，这也可以获得文本信息
  >
  > applicationEventPulisherAware：应用事件发布器，可以发布事件，
  >
  > ResourceLoaderAware： 获得资源加载器，可以获得外部资源文件的内容；

- **使用**

  > 如果bean实现了BeanNameAware接口，spring将bean的id传给setBeanName()方法；
  >
  > 如果bean实现了BeanFactoryAware接口，spring将调用setBeanFactory方法，将BeanFactory实例传进来；
  >
  > 如果bean实现了`ApplicationContextAware`接口，它的`setApplicationContext`()方法将被调用，将应用上下文的引用传入到bean中；
  >
  > 如果bean实现了BeanPostProcessor接口，它的postProcessBeforeInitialization方法将被调用；
  >
  > 如果bean实现了InitializingBean接口，spring将调用它的afterPropertiesSet接口方法，类似的如果bean使用了init-method属性声明了初始化方法，该方法也会被调用；
  >
  > 如果bean实现了BeanPostProcessor接口，它的postProcessAfterInitialization接口方法将被调用；
  >
  > 此时bean已经准备就绪，可以被应用程序使用了，他们将一直驻留在应用上下文中，直到该应用上下文被销毁；
  >
  > 若bean实现了`DisposableBean`接口，spring将调用它的`distroy`()接口方法。同样的，如果bean使用了destroy-method属性声明了销毁方法，则该方法被调用；

  注意：除了通过实现Aware结尾接口获取spring内置对象，也可以通过`@Autowired`注解直接注入相关对象，如下：
  （如果需要用到静态方法中，如工具方法，还是采用实现接口的方式）

  ```java
  @Autowired
  private MessageSource messageSource; 
  
  @Autowired
  private ResourceLoader resourceLoader; 
  
  @Autowired
  private ApplicationContext applicationContext;
  ```

- 项目中实际Aware应用：

  创建`SpringContextHolder`类，被spring容器管理，同时，该类持有spring容器实例，提供静态公共方法，获取spring容器中管理的bean，如获取`TestBean`的实例bean。

  注意特殊情况，管理另外spring容器的Bean，解决方法：很简单，就是让biz的spring管理support提供公共方法的那个bean类，即让biz项目对`SpringContextHolder`进行自动扫描。

  ```xml
      <!-- 启动自动扫描，添加SpringContextHolder类所在包com.test.spring.support的扫描,base-package 如果多个，用“,”分隔 -->
      <context:component-scan base-package="com.test.spring.support,com.test.spring.biz"></context:component-scan>
  ```

  参考地址：<https://blog.csdn.net/javaloveiphone/article/details/52143126>



# `PostProcessor`

- **BeanPostProcessor 的作用：**在 Bean 完成实例化后，如果我们需要对其进行一些配置、增加一些自己的处理逻辑，那么请使用 BeanPostProcessor

- **BeanProcessor的注册，除了application其他都不是自动的**

  > `#getBeanPostProcessors()` 方法，返回的是 `beanPostProcessors` 集合，该集合里面存放就是我们自定义的 `BeanPostProcessor` ，如果该集合中存在元素则调用相应的方法，否则就直接返回 bean 了。这也是为什么使用 `BeanFactory` 容器是无法输出自定义 `BeanPostProcessor` 里面的内容，因为在 `BeanFactory#getBean(...)` 方法的过程中根本就没有将我们自定义的 `BeanPostProcessor` 注入进来，所以要想 `BeanFactory` 容器 的 `BeanPostProcessor` 生效我们必须手动调用 `#addBeanPostProcessor(BeanPostProcessor beanPostProcessor)` 方法，将定义的 `BeanPostProcessor` 注册到相应的 `BeanFactory` 中。**但是 `ApplicationContext` 不需要手动，因为 `ApplicationContext` 会自动检测并完成注册**。

  自动注册的步骤：

  > 方法首先 `beanFactory` 获取注册到该 BeanFactory 中所有 BeanPostProcessor 类型的 `beanName` 数组，其实就是找所有实现了 BeanPostProcessor 接口的 bean ，然后迭代这些 bean ，将其按照 PriorityOrdered、Ordered、无序的顺序，添加至相应的 List 集合中，最后依次调用 `#sortPostProcessors(List<?> postProcessors, ConfigurableListableBeanFactory beanFactory)` 方法来进行排序处理、 `#registerBeanPostProcessors(ConfigurableListableBeanFactory beanFactory, List<BeanPostProcessor> postProcessors)` 方法来完成注册

- **PostProcessor原理**

  ![](http://static.iocoder.cn/8d2ae41f84bfb1928845428dae1b26c1)

- 注意点

  > 1. BeanPostProcessor 的作用域是容器级别的，它只和所在的容器相关 ，当 BeanPostProcessor 完成注册后，它会应用于所有跟它在同一个容器内的 bean 。
  > 2. BeanFactory 和 ApplicationContext 对 BeanPostProcessor 的处理不同，ApplicationContext 会自动检测所有实现了 BeanPostProcessor 接口的 bean，并完成注册，但是使用 BeanFactory 容器时则需要手动调用 `AbstractBeanFactory#addBeanPostProcessor(BeanPostProcessor beanPostProcessor)` 方法来完成注册
  > 3. ApplicationContext 的 BeanPostProcessor 支持 Ordered，而 BeanFactory 的 BeanPostProcessor 是不支持的，原因在于ApplicationContext 会对 BeanPostProcessor 进行 Ordered 检测并完成排序，而 BeanFactory 中的 BeanPostProcessor 只跟注册的顺序有关。

- 使用例子

  自定义注解，然后再`PostProcessor`里取出Bean，对Bean内属性字段field取值并覆盖成注解内值

  <https://www.jianshu.com/p/1c919c21f3b8>



# InitializingBean 和 init-method

在`invokeInitMethods`方法里，先会调用`InitializingBean` ，再调用`init-method`

## ①InitializingBean

Spring 的 `org.springframework.beans.factory.InitializingBean` 接口，为 bean 提供了定义初始化方法的方式，它仅包含了一个方法：`#afterPropertiesSet()` 。

Spring 在完成实例化后，设置完所有属性，进行 “Aware 接口” 和 “`BeanPostProcessor` 前置处理”之后，会接着检测当前 bean 对象是否实现了 `InitializingBean` 接口。如果是，则会调用其 `#afterPropertiesSet()` 方法，进一步调整 bean 实例对象的状态。

- **作用**

  在 `#afterPropertiesSet()` 方法中，我们是可以改变 bean 的属性的，这相当于 Spring 容器又给我们提供了一种可以改变 bean 实例对象的方法。**

- **调用位置：**

  bean 初始化阶段（ `#initializeBean(final String beanName, final Object bean, RootBeanDefinition mbd)` 方法）， Spring 容器会主动检查当前 bean 是否已经实现了 InitializingBean 接口，如果实现了，则会掉用其 `#afterPropertiesSet()` 方法。这个主动检查、**调用的动作是由 `#invokeInitMethods(String beanName, final Object bean, @Nullable RootBeanDefinition mbd)` 方法来完成的。**

## ②`init-method`

该属性用于在 bean 初始化时指定执行方法，可以用来替代实现 `InitializingBean` 接口

```xml
<bean id="initializingBeanTest" class="org.springframework.core.test.InitializingBeanTest"
        init-method="setOtherName">
    <property name="name" value="chenssy 1 号"/>
</bean>
```



## ③小结

从 `#invokeInitMethods(...)` 方法中，我们知道 `init-method` 指定的方法会在 `#afterPropertiesSet()` 方法之后执行，如果 `#afterPropertiesSet()` 方法的执行的过程中出现了异常，则 `init-method` 是不会执行的，而且由于 `init-method` 采用的是反射执行的方式，所以 `#afterPropertiesSet()` 方法的执行效率一般会高些，但是并不能排除我们要优先使用 `init-method`，主要是因为它消除了 bean 对 Spring 的依赖，Spring 没有侵入到我们业务代码，这样会更加符合 Spring 的理念。诚然，`init-method` 是基于 xml 配置文件的，就目前而言，我们的工程几乎都摒弃了配置，而采用注释的方式，那么 `@PreDestory` 可能适合你，当然这个注解我们后面分析。



[SpringBoot【初步】：SpringBoot使用引入xml配置文件以及目录结构推荐](https://blog.csdn.net/u012190514/article/details/79951874)