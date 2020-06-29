- [11张图：帮你搞定 Spring Bean 生命周期](<https://mp.weixin.qq.com/s?__biz=MzU0OTk3ODQ3Ng==&mid=2247487102&idx=1&sn=d8cb0c78833f059e0a1f25436750d0fa&chksm=fba6e67dccd16f6b310a250c2dbbe36740c5204f2a8c5cfc597e2c5ded4493b62618899a380c&mpshare=1&scene=23&srcid=&sharer_sharetime=1583474741114&sharer_shareid=e6d90aec84add5cf004cb1ab6979727c#rd>)

- [spring源码刨析-spring-beans(内部核心组件,beanDefinition加载过程)](<https://mp.weixin.qq.com/s?__biz=MzI0NjM4MTc0Ng==&mid=2247486045&idx=1&sn=d7204df13581788e4dd8754238a46b2f&chksm=e9416780de36ee964308a609212a2f8cccc61026a2758b671fcee7767ccb9035ce14867637bd&mpshare=1&scene=23&srcid=&sharer_sharetime=1583175754578&sharer_shareid=e6d90aec84add5cf004cb1ab6979727c#rd>)

- [Spring 的 Bean 生命周期，11 张高清流程图及代码，深度解析](<https://mp.weixin.qq.com/s?__biz=MzUzMTA2NTU2Ng==&mid=2247489368&idx=1&sn=8b0ef227bc0972ffc93e531dafe444c7&chksm=fa4968e9cd3ee1ff94ef02a98970e917c1cd72ce40199dc19300c5f432cb121cd1b930e45351&mpshare=1&scene=23&srcid=&sharer_sharetime=1586704748673&sharer_shareid=e6d90aec84add5cf004cb1ab6979727c#rd>)

- [不懂SpringApplication生命周期事件？那就等于不会Spring Boot嘛](<https://fangshixiang.blog.csdn.net/article/details/105762050?from=groupmessage>)

- [Spring 为啥默认把bean设计成单例的？这篇讲的明明白白的](<https://mp.weixin.qq.com/s?__biz=MzI4Njk5OTg1MA==&mid=2247484046&idx=1&sn=3e7fbbba4dceb0eccdf4779a16956c08&chksm=ebd516cbdca29fdd4e1341bcb904e92eb53128740ea8f1a7ad5186fd8f684f4d9e19eb8ee552&mpshare=1&scene=23&srcid=&sharer_sharetime=1587726226667&sharer_shareid=e6d90aec84add5cf004cb1ab6979727c#rd>)



## [5. Spring bean](https://snailclimb.gitee.io/javaguide/#/docs/system-design/framework/spring/SpringInterviewQuestions?id=5-spring-bean)

### [5.1 Spring 中的 bean 的作用域有哪些?](https://snailclimb.gitee.io/javaguide/#/docs/system-design/framework/spring/SpringInterviewQuestions?id=51-spring-%e4%b8%ad%e7%9a%84-bean-%e7%9a%84%e4%bd%9c%e7%94%a8%e5%9f%9f%e6%9c%89%e5%93%aa%e4%ba%9b)

- singleton : 唯一 bean 实例，Spring 中的 bean 默认都是单例的。
- prototype : 每次请求都会创建一个新的 bean 实例。
- request : 每一次HTTP请求都会产生一个新的bean，该bean仅在当前HTTP request内有效。
- session : 每一次HTTP请求都会产生一个新的 bean，该bean仅在当前 HTTP session 内有效。
- global-session： 全局session作用域，仅仅在基于portlet的web应用中才有意义，Spring5已经没有了。Portlet是能够生成语义代码(例如：HTML)片段的小型Java Web插件。它们基于portlet容器，可以像servlet一样处理HTTP请求。但是，与 servlet 不同，每个 portlet 都有不同的会话

### [5.2 Spring 中的单例 bean 的线程安全问题了解吗？](https://snailclimb.gitee.io/javaguide/#/docs/system-design/framework/spring/SpringInterviewQuestions?id=52-spring-%e4%b8%ad%e7%9a%84%e5%8d%95%e4%be%8b-bean-%e7%9a%84%e7%ba%bf%e7%a8%8b%e5%ae%89%e5%85%a8%e9%97%ae%e9%a2%98%e4%ba%86%e8%a7%a3%e5%90%97%ef%bc%9f)

大部分时候我们并没有在系统中使用多线程，所以很少有人会关注这个问题。单例 bean 存在线程问题，主要是因为当多个线程操作同一个对象的时候，对这个对象的非静态成员变量的写操作会存在线程安全问题。

常见的有两种解决办法：

1. 在Bean对象中尽量避免定义可变的成员变量（不太现实）。
2. 在类中定义一个ThreadLocal成员变量，将需要的可变成员变量保存在 ThreadLocal 中（推荐的一种方式）。

### [5.3 @Component 和 @Bean 的区别是什么？](https://snailclimb.gitee.io/javaguide/#/docs/system-design/framework/spring/SpringInterviewQuestions?id=53-component-%e5%92%8c-bean-%e7%9a%84%e5%8c%ba%e5%88%ab%e6%98%af%e4%bb%80%e4%b9%88%ef%bc%9f)

1. 作用对象不同: `@Component` 注解作用于类，而`@Bean`注解作用于方法。
2. `@Component`通常是通过类路径扫描来自动侦测以及自动装配到Spring容器中（我们可以使用 `@ComponentScan` 注解定义要扫描的路径从中找出标识了需要装配的类自动装配到 Spring 的 bean 容器中）。`@Bean` 注解通常是我们在标有该注解的方法中定义产生这个 bean,`@Bean`告诉了Spring这是某个类的示例，当我需要用它的时候还给我。
3. `@Bean` 注解比 `Component` 注解的自定义性更强，而且很多地方我们只能通过 `@Bean` 注解来注册bean。比如当我们引用第三方库中的类需要装配到 `Spring`容器时，则只能通过 `@Bean`来实现。

`@Bean`注解使用示例：

```java
@Configuration
public class AppConfig {
    @Bean
    public TransferService transferService() {
        return new TransferServiceImpl();
    }

}
```

上面的代码相当于下面的 xml 配置

```xml
<beans>
    <bean id="transferService" class="com.acme.TransferServiceImpl"/>
</beans>
```

下面这个例子是通过 `@Component` 无法实现的。

```java
@Bean
public OneService getService(status) {
    case (status)  {
        when 1:
                return new serviceImpl1();
        when 2:
                return new serviceImpl2();
        when 3:
                return new serviceImpl3();
    }
}
```

### [5.4 将一个类声明为Spring的 bean 的注解有哪些?](https://snailclimb.gitee.io/javaguide/#/docs/system-design/framework/spring/SpringInterviewQuestions?id=54-%e5%b0%86%e4%b8%80%e4%b8%aa%e7%b1%bb%e5%a3%b0%e6%98%8e%e4%b8%baspring%e7%9a%84-bean-%e7%9a%84%e6%b3%a8%e8%a7%a3%e6%9c%89%e5%93%aa%e4%ba%9b)

我们一般使用 `@Autowired` 注解自动装配 bean，要想把类标识成可用于 `@Autowired` 注解自动装配的 bean 的类,采用以下注解可实现：

- `@Component` ：通用的注解，可标注任意类为 `Spring` 组件。如果一个Bean不知道属于哪个层，可以使用`@Component` 注解标注。
- `@Repository` : 对应持久层即 Dao 层，主要用于数据库相关操作。
- `@Service` : 对应服务层，主要涉及一些复杂的逻辑，需要用到 Dao层。
- `@Controller` : 对应 Spring MVC 控制层，主要用户接受用户请求并调用 Service 层返回数据给前端页面。

### [5.5 Spring 中的 bean 生命周期?](https://snailclimb.gitee.io/javaguide/#/docs/system-design/framework/spring/SpringInterviewQuestions?id=55-spring-%e4%b8%ad%e7%9a%84-bean-%e7%94%9f%e5%91%bd%e5%91%a8%e6%9c%9f)

这部分网上有很多文章都讲到了，下面的内容整理自：<https://yemengying.com/2016/07/14/spring-bean-life-cycle/> ，除了这篇文章，再推荐一篇很不错的文章 ：<https://www.cnblogs.com/zrtqsk/p/3735273.html> 。

- Bean 容器找到配置文件中 Spring Bean 的定义。
- Bean 容器利用 Java Reflection API 创建一个Bean的实例。
- 如果涉及到一些属性值 利用 `set()`方法设置一些属性值。
- 如果 Bean 实现了 `BeanNameAware` 接口，调用 `setBeanName()`方法，传入Bean的名字。
- 如果 Bean 实现了 `BeanClassLoaderAware` 接口，调用 `setBeanClassLoader()`方法，传入 `ClassLoader`对象的实例。
- 与上面的类似，如果实现了其他 `*.Aware`接口，就调用相应的方法。
- 如果有和加载这个 Bean 的 Spring 容器相关的 `BeanPostProcessor` 对象，执行`postProcessBeforeInitialization()` 方法
- 如果Bean实现了`InitializingBean`接口，执行`afterPropertiesSet()`方法。
- 如果 Bean 在配置文件中的定义包含 init-method 属性，执行指定的方法。
- 如果有和加载这个 Bean的 Spring 容器相关的 `BeanPostProcessor` 对象，执行`postProcessAfterInitialization()` 方法
- 当要销毁 Bean 的时候，如果 Bean 实现了 `DisposableBean` 接口，执行 `destroy()` 方法。
- 当要销毁 Bean 的时候，如果 Bean 在配置文件中的定义包含 destroy-method 属性，执行指定的方法。

图示：

![Spring Bean 生命周期](http://my-blog-to-use.oss-cn-beijing.aliyuncs.com/18-9-17/48376272.jpg)

与之比较类似的中文版本:

![Spring Bean 生命周期](http://my-blog-to-use.oss-cn-beijing.aliyuncs.com/18-9-17/5496407.jpg)



**当你使用了组件扫描之后，它会自动注册一个`AutoWiredAnnotationBeanPostProcessor`实例**

