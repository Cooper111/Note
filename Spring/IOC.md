- [浅析Spring IOC 容器源码](<https://mp.weixin.qq.com/s?__biz=MzU2NDY5NzgwNw==&mid=2247483795&idx=1&sn=822787cfc51f9b4c8a61a9c7e22b1db4&chksm=fc464e33cb31c725c11c9578c5e28b8b642605aebf275a3c142328a54cba64e902b57658613c&mpshare=1&scene=23&srcid=&sharer_sharetime=1583426853120&sharer_shareid=e6d90aec84add5cf004cb1ab6979727c#rd>)

- <https://yikun.github.io/2015/05/28/Spring-IOC%E6%A0%B8%E5%BF%83%E6%BA%90%E7%A0%81%E5%AD%A6%E4%B9%A0/>

- 极客时间（设计模式之美——工厂模式）

- [一分钟带你玩转 Spring IoC](<https://mp.weixin.qq.com/s?__biz=MzAwNDA2OTM1Ng==&mid=2453142042&idx=2&sn=fa3a69d3dab043228426e33b1e00b540&chksm=8cf2d899bb85518f4330ed76790497c2651ea06cec18969e432e7981db9b15e28b968e60198f&mpshare=1&scene=23&srcid=&sharer_sharetime=1588671177303&sharer_shareid=e6d90aec84add5cf004cb1ab6979727c#rd>)



实际上，DI 容器底层最基本的设计思路就是基于工厂模式的。DI 容器相当于一个大的工厂类，负责在程序启动的时候，根据配置（要创建哪些类对象，每个类对象的创建需要依赖哪些其他类对象）事先创建好对象。当应用程序需要使用某个类对象的时候，直接从容器中获取即可。正是因为它持有一堆对象，所以这个框架才被称为“容器”。





1.初始化

XML -> Resource -> BeanDefinition -> BeanFactory



2.注入依赖

初始化IOC -> 初始化Bean(非Lazyinit)  -> 创建Bean实例 -> 注入Property->初始化方法



**IOC是这样的** ：  argClasses、argObjects由BeanDefinition得到

> bean = beanClass.getConstructor(argClasses).newInstance(argObjects);

看得出来是用构造器newInstance，               参数Class类，参数



ioc这里不对，阅读源码后知道了分三种情况：工厂方法（反射），默认构造方法（反射），指定构造方法（创建 Enhancer ，使用cglib实例化）

而且实例化后，还得解决依赖循环、属性注入、初始化



**AOP是这样的**：

```
Proxy.newProxyInstance(interfaceClazz.getClassLoader(), new Class[]{interfaceClazz}, new MyHandler(proxy));
```

看得出来是用Proxy.newProxyInstance，      ClassLoader，Class类，invocaitonHandler组成