- [Springboot + redis操作多种实现（以及Jedis,Redisson,Lettuce的区别比较)](https://blog.csdn.net/qq_42105629/article/details/102589319)
- [springboot(七).springboot整合jedis实现redis缓存](https://www.cnblogs.com/GodHeng/p/9301330.html)

- [【好】Spring Boot2.x 整合lettuce redis 和 redisson](<https://blog.csdn.net/zl_momomo/article/details/82788294>)

springboot2之前redis的连接池为jedis，2.0以后redis的连接池改为了lettuce，lettuce能够支持redis4，需要java8及以上。
lettuce是基于netty实现的与redis进行同步和异步的通信。

`Lettuce`和`Jedis`的都是连接`Redis Server`的客户端程序。`Jedis`在**实现上是直连redis server，多线程环境下非线程安全，除非使用连接池，为每个Jedis实例增加物理连接**。`Lettuce`基于Netty的连接实例（StatefulRedisConnection），**可以在多个线程间并发访问，且线程安全，满足多线程环境下的并发访问，同时它是可伸缩的设计，一个连接实例不够的情况也可以按需增加连接实例**。



jedis:连接池(JedisPool),就是享学课堂james老师用的，可以参考他的





- 基于docker实现redis高可用集群：<https://www.cnblogs.com/yloved/p/11559902.html>