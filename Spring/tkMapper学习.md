# tkMapper插件学习





### 开发流程

- 导入包

  ```xml
  <dependency>
      <groupId>tk.mybatis</groupId>
      <artifactId>mapper</artifactId>
      <version>3.4.6</version>
  </dependency> 
  ```

- 增加如下Mapper的配置

  mybatis对于通用mapper的配置相比于与平常的配置，也只是改变了bean的calss，将`org.mybatis.spring.mapper.MapperScannerConfigurer`改为了`tk.mybatis.spring.mapper.MapperScannerConfigurer`

  ```java
  /**
   * Mybatis & Mapper & PageHelper 配置
   */
  @Configuration
  public class MybatisConfigurer {
  
      @Bean
      public SqlSessionFactory sqlSessionFactoryBean(DataSource dataSource) throws Exception {
          SqlSessionFactoryBean factory = new SqlSessionFactoryBean();
          factory.setDataSource(dataSource);
          factory.setTypeAliasesPackage(MODEL_PACKAGE);
  
          //配置分页插件，详情请查阅官方文档
          PageHelper pageHelper = new PageHelper();
          Properties properties = new Properties();
          properties.setProperty("pageSizeZero", "true");//分页尺寸为0时查询所有纪录不再执行分页
          properties.setProperty("reasonable", "true");//页码<=0 查询第一页，页码>=总页数查询最后一页
          properties.setProperty("supportMethodsArguments", "true");//支持通过 Mapper 接口参数来传递分页参数
          pageHelper.setProperties(properties);
  
          //添加插件
          factory.setPlugins(new Interceptor[]{pageHelper});
  
          //添加XML目录
          ResourcePatternResolver resolver = new PathMatchingResourcePatternResolver();
          factory.setMapperLocations(resolver.getResources("classpath:mapper/*.xml"));
          return factory.getObject();
      }
  
      @Bean
      public MapperScannerConfigurer mapperScannerConfigurer() {
          MapperScannerConfigurer mapperScannerConfigurer = new MapperScannerConfigurer();
          mapperScannerConfigurer.setSqlSessionFactoryBeanName("sqlSessionFactoryBean");
          mapperScannerConfigurer.setBasePackage(MAPPER_PACKAGE);
  
          //配置通用Mapper，详情请查阅官方文档
          Properties properties = new Properties();
          properties.setProperty("mappers", MAPPER_INTERFACE_REFERENCE);//Mapper插件基础接口的完全限定名
          properties.setProperty("notEmpty", "false");//insert、update是否判断字符串类型!='' 即 test="str != null"表达式内是否追加 and str != ''
          properties.setProperty("IDENTITY", "MYSQL");
          mapperScannerConfigurer.setProperties(properties);
  
          return mapperScannerConfigurer;
      }
  
  }
  ```

- 增加表的映射类，类似JPA

  ```java
  /**
   * @author Kevin
   * @date 2020/4/19 15:05
   */
  @Data
  @NoArgsConstructor
  @AllArgsConstructor
  @Table(name = "tb_student")
  @JsonIgnoreProperties(value = {"handler","hibernateLazyInitializer","fieldHandler"})
  public class Student {
      @Id
      @GeneratedValue(strategy = GenerationType.IDENTITY)     // 提供主键的生成策略,自增
      private Integer id;
      @Column(name = "name")
      private String name;
      @Column(name = "sex")
      private String sex;
      @Column(name = "age")
      private Integer age;
      @Column(name = "clazz_id")
      private Integer clazz_id;
  
  }
  ```

- Mapper插件基础泛型接口Mapper

  构建通用的mapper，我们只需要继承接口就好，不需要其余多余的操作。封装通用的CommentMapper接口，方便我们继承

  ```java
  /**
   * 定制版MyBatis Mapper插件接口，如需其他接口参考官方文档自行添加。
   */
  public interface Mapper<T>
          extends
          BaseMapper<T>,
          ConditionMapper<T>,
          IdsMapper<T>,
          InsertListMapper<T> {
  }
  ```

- 基于通用MyBatis Mapper插件的Service接口的实现

  ```java
  public abstract class AbstractService<T> implements Service<T> {
  
      @Autowired
      protected Mapper<T> mapper;
  
      private Class<T> modelClass;    // 当前泛型真实类型的Class
  
      public AbstractService() {
          ParameterizedType pt = (ParameterizedType) this.getClass().getGenericSuperclass();
          modelClass = (Class<T>) pt.getActualTypeArguments()[0];
      }
  
      public void save(T model) {
          mapper.insertSelective(model);
      }
  
      public void save(List<T> models) {
          mapper.insertList(models);
      }
  
      public void deleteById(Integer id) {
          mapper.deleteByPrimaryKey(id);
      }
  
      public void deleteByIds(String ids) {
          mapper.deleteByIds(ids);
      }
  
      public void update(T model) {
          mapper.updateByPrimaryKeySelective(model);
      }
  
      public T findById(Integer id) {
          return mapper.selectByPrimaryKey(id);
      }
  
      @Override
      public T findBy(String fieldName, Object value) throws TooManyResultsException {
          try {
              T model = modelClass.newInstance();
              Field field = modelClass.getDeclaredField(fieldName);
              field.setAccessible(true);
              field.set(model, value);
              return mapper.selectOne(model);
          } catch (ReflectiveOperationException e) {
              throw new ServiceException(e.getMessage(), e);
          }
      }
  
      public List<T> findByIds(String ids) {
          return mapper.selectByIds(ids);
      }
  
      public List<T> findByCondition(Condition condition) {
          return mapper.selectByCondition(condition);
      }
  
      public List<T> findAll() {
          return mapper.selectAll();
      }
  }
  ```

- 然后就可以快速开发了

  - 构造Mapper：

    ```java
    public interface StudentMapper extends Mapper<Student> {
    }
    ```

  - 构造Service:

    ```java
    public interface StudentService extends Service<Student> {
    }
    ```

  - 构造ServiceImpl

    ```java
    @org.springframework.stereotype.Service
    @Transactional
    public class StudentServiceImpl extends AbstractService<Student> implements StudentService {
        @Resource
        private StudentMapper studentMapper;
    }
    ```

    

  





# 封装vo扩展类

​	用于一对一，多对一，多对多的查询。比如教室对象（id，name）被Select出来，应该内含集合List<Student>。

​	所以扩展教室类为(id, name, students)作为Mapper的返回值，如下：

```java
/**
 * @author Kevin
 * @date 2020/4/19 15:36
 */
public interface ClazzMapper extends Mapper<Clazz> {
    /**
     *  查询班级信息（附带班级所有学生）
     */
    @Select("SELECT * FROM tb_clazz WHERE id = #{id} limit 1")
    @Results({
            @Result(column = "id", property = "id"),
            @Result(column = "code", property = "code"),
            @Result(column = "name", property = "name"),
            @Result(column = "id", property = "students",
                many = @Many(
                        select = "com.example.demo.dao.StudentMapper.queryStudentInfoByClazz",
                        fetchType = FetchType.LAZY
                ))
    })
    public ClazzExtra queryClazzInfoWithStudent(Integer id);
}
```







# 参考

- Github  Springboot&Mybatis种子项目：<https://github.com/lihengming/spring-boot-api-project-seed>

- springboot集成tkMapper：<https://blog.csdn.net/qq_42937522/article/details/103258526>

- tkMapper多对多：<https://blog.csdn.net/qq_42937522/article/details/103258526>

- Mybatis多对多常见写法：<https://www.cnblogs.com/chenliyang/p/6548400.html>

  > 总结：必须有桥表，且使用两个inner join关联3表完成查询

- Mybatis 插件 tkMapper: https://www.jianshu.com/p/6decc1d893a3