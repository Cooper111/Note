# [【Spring】Spring AOP实现原理](https://www.cnblogs.com/puyangsky/p/6218925.html)

AOP面向切面编程，通过`（预编译（AspectJ）和）`**动态代理**的方式去实现一些程序功能的统一维护的一种技术

在不影响业务代码的同时，无侵入的织入些功能

## 动态代理：

### JDK自带方法：

`jdk`中的动态代理通过反射类`Proxy`和`InvocationHandler`回调接口实现，要求委托类必须实现一个接口，只能对该类接口中定义的方法实现代理，这在实际编程中有一定的局限性。

- 核心接口：`java.lang.reflect.InvocationHandler`接口

  对于被代理的类的操作都会由该接口中的invoke方法实现

- 核心方法：`java.lang.reflect.Proxy.newProxyInstance`方法

  该方法会返回一个被修改过的类的实例，从而可以自由的调用该实例的方法




通过 `Proxy.newProxyInstance` 创建的代理对象是**在jvm运行时**动态生成的一个对象，它并不是我们的`InvocationHandler`类型，也不是我们定义的那组接口的类型，而是在运行是动态生成的一个对象，并且命名方式都是这样的形式，以**$**开头，proxy为中，最后一个数字表示对象的标号。




### cglib实现

<https://www.cnblogs.com/chinajava/p/5880887.html>

使用`cglib`（[Code Generation Library](https://github.com/cglib/cglib)）实现动态代理，并不要求委托类必须实现接口，底层采用**asm字节码生成框架**生成代理类的字节码

**CGLIB是针对类实现代理，主要是对指定的类生成一个子类，覆盖其中的方法（继承）**

- 实现`MethodInterceptor`接口，定义方法的拦截器
- 利用`Enhancer`类生成代理类

代理对象的生成过程由[Enhancer](http://www.boyunjian.com/javasrc/org.sonatype.sisu.inject/cglib/3.0/_/net/sf/cglib/proxy/Enhancer.java)类实现，大概步骤如下：
1、生成代理类Class的二进制字节码；
2、通过`Class.forName`加载二进制字节码，生成Class对象；
3、通过反射机制获取实例构造，并初始化代理类对象。

### cglib字节码生成

Enhancer是CGLib的字节码增强器，可以方便的对类进行扩展，内部调用`GeneratorStrategy.generate`方法生成代理类的字节码



### CGlib比JDK快？

　 (1)使用CGLib实现动态代理，CGLib底层采用ASM字节码生成框架，使用字节码技术生成代理类，**比使用Java反射效率要高**。唯一需要注意的是，CGLib不能对声明为final的方法进行代理，因为CGLib原理是动态生成被代理类的子类。

　 (2)在对JDK动态代理与CGlib动态代理的代码实验中看，1W次执行下，JDK7及8的动态代理性能比CGlib要好**20%**左右。



#### 表述2

性能问题：由于Cglib代理是利用ASM字节码生成框架在内存中生成一个需要被代理类的子类完成代理，而JDK动态代理是利用反射原理完成动态代理，所以**Cglib创建的动态代理对象性能**比JDk动态代理动态创建出来的代理对象新能要好的多，但是**对象创建的速度比JDk动态代理要慢**，所以，当Spring使用的是单例情况下可以选用Cglib代理，反之使用JDK动态代理更加合适。同时还有一个问题，**被final修饰的类只能使用JDK动态代理**，因为被final修饰的类不能被继承，而**Cglib则是利用的继承原理实现代理的**。





### 在Spring AOP中支持4中类型的通知：

1：before advice 在方法执行前执行。

2：after  returning  advice 在方法执行后返回一个结果后执行。

3：after  throwing advice 在方法执行过程中抛出异常的时候执行。

4：Around  advice 在方法执行前后和抛出异常时执行，相当于综合了以上三种通知。



# AOP实现功能

监控、统计、鉴权、限流、事务、幂等、日志



#### [简述SpringAop以及拦截器和过滤器](https://www.cnblogs.com/dugqing/p/8906013.html)

`aop`的实现主要**有两种方式**，一种是通过回调函数，另一种是代理。

- **回调函数**

  通过回调的方式实现的`aop`有`Filter`（过滤器）和`Interceptor`（拦截器）

- **代理**

  在`debug调试`中经常会看到一些bean的名字结尾是$proxy或者`$cglibProxy`等等，说明bean实际上都是经过代理后的代理对象，而不是实际的对象





## AOP实现功能示例

- [在SpringBoot中用SpringAOP实现日志记录功能](https://www.cnblogs.com/wangshen31/p/9379197.html)
- [Spring AOP在鉴权和日志中的应用](<https://blog.csdn.net/limengliang4007/article/details/78660834>)
- [Spring Boot + Redis 实现接口幂等性 | 分布式开发必知](<https://blog.csdn.net/j3T9Z7H/article/details/99254177>)





### 接口幂等性

<https://blog.csdn.net/qq_41490913/article/details/105027651>

#### 一、概念

幂等性, 通俗的说就是一个接口, 多次发起同一个请求, 必须保证操作只能执行一次
比如:

- 订单接口, 不能多次创建订单
- 支付接口, 重复支付同一笔订单只能扣一次钱
- 支付宝回调接口, 可能会多次回调, 必须处理重复回调
- 普通表单提交接口, 因为网络超时等原因多次点击提交, 只能成功一次
  等等

#### 二、常见解决方案

1. 唯一索引 -- 防止新增脏数据
2. token机制 -- 防止页面重复提交（redis+token）
3. 悲观锁 -- 获取数据的时候加锁(锁表或锁行)
4. 乐观锁 -- 基于版本号version实现, 在更新数据那一刻校验数据
5. 分布式锁 -- redis(jedis、redisson)或zookeeper实现
6. 状态机 -- 状态变更, 更新数据时判断状态

#### 三、我的实现方案

为需要保证幂等性的每一次请求创建一个唯一标识`token`, 先获取`token`, 并将此`token`存入redis, 请求接口时, 将此`token`放到header或者作为请求参数请求接口, 后端接口判断redis中是否存在此`token`:

- 如果存在, 正常处理业务逻辑, 并从redis中删除此`token`, 那么, 如果是重复请求, 由于`token`已被删除, 则不能通过校验, 返回`请勿重复操作`提示
- 如果不存在, 说明参数不合法或者是重复请求, 返回提示即可