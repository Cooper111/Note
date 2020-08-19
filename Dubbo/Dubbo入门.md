## Dubbo 基本概念

`RMI：远程方法调用`

**三大核心能力**：面向接口的远程方法调用，智能容错和负载均衡，以及服务自动注册和发现。



现代的分布式服务框架的基本概念与 RMI 是类似的，同样是使用 Java 的 Interface 作为服务契约，通过注册中心来完成服务的注册和发现，远程通讯的细节也是通过代理类来屏蔽。具体来说，Dubbo 在工作时有以下四个角色参与：

1. 服务提供者 - 启动时在指定端口上暴露服务，并将服务地址和端口注册到注册中心上
2. 服务消费者 - 启动时向注册中心订阅自己感兴趣的服务，以便获得服务提供方的地址列表
3. 注册中心 - 负责服务的注册和发现，负责保存服务提供方上报的地址信息，并向服务消费方推送
4. 监控中心 - 负责收集服务提供方和消费方的运行状态，比如服务调用次数、延迟等，用于监控
5. 运行容器 - 负责服务提供方的初始化、加载以及运行的生命周期管理

![dubbo-architecture](http://dubbo.apache.org/img/blog/dubbo-architecture.png)

**部署阶段**

- 服务提供者在指定端口暴露服务，并向注册中心注册服务信息。
- 服务消费者向注册中心发起服务地址列表的订阅。

**运行阶段**

- 注册中心向服务消费者推送地址列表信息。
- 服务消费者收到地址列表后，从其中选取一个向目标服务发起调用。
- 调用过程服务消费者和服务提供者的运行状态上报给监控中心。



# 多版本

version

指定接口的不同实现bean，用版本号来区分



我觉得可以用来做灰度发布

# 本地存根

远程服务后，客户端通常只剩下接口，而实现全在服务器端，但提供方有些时候想在客户端也执行部分逻辑，比如：做 `ThreadLocal` 缓存，提前验证参数，调用失败后伪造容错数据等等，此时就需要在 API 中带上 Stub，客户端生成 Proxy 实例，会把 Proxy 通过构造函数传给 Stub [[1\]](http://dubbo.apache.org/zh-cn/docs/user/demos/local-stub.html#fn1)，然后把 Stub 暴露给用户，Stub 可以决定要不要去调 Proxy。

![/user-guide/images/stub.jpg](http://dubbo.apache.org/docs/zh-cn/user/sources/images/stub.jpg)

`参考链接：<http://dubbo.apache.org/zh-cn/docs/user/demos/local-stub.html>`

在 spring 配置文件中按以下方式配置：

```xml
<dubbo:service interface="com.foo.BarService" stub="true" />
```

或

```xml
<dubbo:service interface="com.foo.BarService" stub="com.foo.BarServiceStub" />
```

提供 Stub 的实现 [[2\]](http://dubbo.apache.org/zh-cn/docs/user/demos/local-stub.html#fn2)：

```java
package com.foo;
public class BarServiceStub implements BarService {
    private final BarService barService;
    
    // 构造函数传入真正的远程代理对象
    public BarServiceStub(BarService barService){
        this.barService = barService;
    }
 
    public String sayHello(String name) {
        // 此代码在客户端执行, 你可以在客户端做ThreadLocal本地缓存，或预先验证参数是否合法，等等
        try {
            return barService.sayHello(name);
        } catch (Exception e) {
            // 你可以容错，可以做任何AOP拦截事项
            return "容错数据";
        }
    }
}
```

------

1. Stub 必须有可传入 Proxy 的构造函数。 [↩︎](http://dubbo.apache.org/zh-cn/docs/user/demos/local-stub.html#fnref1)
2. 在 interface 旁边放一个 Stub 实现，它实现 `BarService` 接口，并有一个传入远程 `BarService` 实例的构造函数 [↩︎](http://dubbo.apache.org/zh-cn/docs/user/demos/local-stub.html#fnref2)

#### 注意点！

可以把本地存根代码放在接口`api`项目里！



# 配置：优先级

`JVM级别-Ddubbo.xxx.xxx`     >    `dubbo.xml`    >    `application.properties`



`参考链接：<http://dubbo.apache.org/zh-cn/docs/user/configuration/properties.html>`

# 配置：超时

`timeout`：超时属性(`ms`)

配置覆盖优先级：

1）精确优先（方法级优先，接口级次之，全局配置再次之）

2）消费者优先（如果**级别一样**，则消费方优先，提供方次之）



也就是如果精确度提供方更高，则提供方的配置优先！

`参考链接：<http://dubbo.apache.org/zh-cn/docs/user/configuration/xml.html>`

# 配置：重试次数

`retries`：重试次数

如果有多个provider提供方，那么consumer配置的超时重试会在不同的provider上重试

#### 注意点：

- 幂等接口：设置重试次数

  > e.g.   查询、删除、修改

- 非幂等接口：不能设置重试次数

  > e.g.   新增



# 配置：启动时检查

> 参考：`<http://dubbo.apache.org/zh-cn/docs/user/demos/preflight-check.html>`

Dubbo 缺省会在启动时检查依赖的服务是否可用，不可用时会抛出异常，阻止 Spring 初始化完成，以便上线时，能及早发现问题，默认 `check="true"`。

可以通过 `check="false"` 关闭检查，比如，测试时，有些服务不关心，或者出现了循环依赖，必须有一方先启动。

另外，**如果你的 Spring 容器是懒加载的，或者通过 API 编程延迟引用服务，请关闭 check**，否则服务临时不可用时，会抛出异常，拿到 null 引用，如果 `check="false"`，总是会返回引用，当服务恢复时，能自动连上。



# 参考

- <http://dubbo.apache.org/zh-cn/blog/dubbo-101.html>