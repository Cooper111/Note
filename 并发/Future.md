# Future

- Future模式
- JDK中Future实现
- Guava中Future实现
- CompletableFuture



# 源码

**FutureTask类有一个内部类Sync**，一些实质性的工作会委托Sync类实现。补充了解



# CompletableFuture

- 启动

  `runAsync(Runnable runnable)`和`supplyAsync(Supplier supplier)`，

  它们之间的区别是：Runnable 接口的 run() 方法没有返回值，而 Supplier 接口的 get() 方法是有返回值的

- 线程池

  默认情况下 CompletableFuture 会使用公共的 ForkJoinPool 线程池，这个线程池默认创建的线程数是 CPU 的核数。

  也可以自己传入线程池

  ```java
  //使用默认线程池
  static CompletableFuture<Void> 
    runAsync(Runnable runnable)
  static <U> CompletableFuture<U> 
    supplyAsync(Supplier<U> supplier)
  //可以指定线程池  
  static CompletableFuture<Void> 
    runAsync(Runnable runnable, Executor executor)
  static <U> CompletableFuture<U> 
    supplyAsync(Supplier<U> supplier, Executor executor)  
  ```

  

改变线程之间关系

- 线程池（内部，外部）
- 描述串行关系
- 描述AND汇聚关系
- 描述OR汇聚关系
- 异常处理::catch机制
- 异常处理::finally机制
- 创建时（`supplyAsync`）传入的有值函数类`Supplier<U>`，和thenApply时传入的有值函数类`Function<? super T,? extends U>`



# CompletionService

改变结果优先级顺序

- CompletionService 将线程池 Executor 和阻塞队列 BlockingQueue 的功能融合在了一起，能够让批量异步任务的管理更简单。
- 除此之外，CompletionService 能够让异步任务的执行结果有序化，先执行完的先进入阻塞队列

我的应用概括：

- 阻塞队列保证耗时少的任务优先完成，批量异步任务结果有序化+性能提高
- 阻塞队列实现ForkingForking 的集群模式，这种集群模式下，支持并行地调用多个查询服务，只要有一个成功返回结果，整个服务就可以返回了