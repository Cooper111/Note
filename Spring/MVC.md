MVC 是一种设计模式.

**MVC 的原理图如下：**

![img](http://my-blog-to-use.oss-cn-beijing.aliyuncs.com/18-10-11/60679444.jpg)

### [SpringMVC 简单介绍](https://snailclimb.gitee.io/javaguide/#/docs/system-design/framework/spring/SpringMVC-Principle?id=springmvc-%e7%ae%80%e5%8d%95%e4%bb%8b%e7%bb%8d)

SpringMVC 框架是以请求为驱动，围绕 Servlet 设计，将请求发给控制器，然后通过模型对象，分派器来展示请求结果视图。其中核心类是 DispatcherServlet，它是一个 Servlet，顶层是实现的Servlet接口。



### [SpringMVC 工作原理（重要）](https://snailclimb.gitee.io/javaguide/#/docs/system-design/framework/spring/SpringMVC-Principle?id=springmvc-%e5%b7%a5%e4%bd%9c%e5%8e%9f%e7%90%86%ef%bc%88%e9%87%8d%e8%a6%81%ef%bc%89)

**简单来说：**

客户端发送请求-> 前端控制器 DispatcherServlet 接受客户端请求 -> 找到处理器映射 HandlerMapping 解析请求对应的 Handler-> HandlerAdapter 会根据 Handler 来调用真正的处理器开处理请求，并处理相应的业务逻辑 -> 处理器返回一个模型视图 ModelAndView -> 视图解析器进行解析 -> 返回一个视图对象->前端控制器 DispatcherServlet 渲染数据（Moder）->将得到视图对象返回给用户

**如下图所示：** ![SpringMVC运行原理](http://my-blog-to-use.oss-cn-beijing.aliyuncs.com/18-10-11/49790288.jpg)





### [Spring与SpringMVC的区别](https://www.cnblogs.com/rainbow70626/p/9784938.html)

Spring是IOC和AOP的容器框架，

SpringMVC是基于Spring功能之上添加的Web框架，想用SpringMVC必须先依赖Spring

Spring可以说是一个管理bean的容器，也可以说是包括很多开源项目的总称，spring mvc是其中一个开源项目，所以简单走个流程的话，http请求一到，由容器（如：tomact）解析http搞成一个request，通过映射关系（路径，方法，参数啊）被spring mvc一个分发器去找到可以处理这个请求的bean，那tomcat里面就由spring管理bean的一个池子（bean容器）里面找到，处理完了就把响应返回回去。

SpringMVC是一个MVC模式的WEB开发框架;