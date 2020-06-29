# 并发工具类

- ReentrantLock和Condition（可重入锁和条件）
- Semphore（信号量）
- ReadWriteLock（读写锁）
- StampedLock（新读写锁）
- CountDownLatch和CyclicBarrier（线程同步器：倒计时器、循环栅栏）
- LockSupprot（线程阻塞工具类）
- Guava的RateLimiter（好用的限流工具）
- Executor和线程池
- Future





### CountDownLatch 和 CyclicBarrier 的区别

参考：<https://time.geekbang.org/column/article/89461>

​        CountDownLatch 和 CyclicBarrier 是 Java 并发包提供的两个非常易用的线程同步工具类，这两个工具类用法的区别在这里还是有必要再强调一下：CountDownLatch 主要用来解决一个线程等待多个线程的场景，可以类比旅游团团长要等待所有的游客到齐才能去下一个景点；而 CyclicBarrier 是一组线程之间互相等待，更像是几个驴友之间不离不弃。

​        除此之外 CountDownLatch 的计数器是不能循环利用的，也就是说一旦计数器减到 0，再有线程调用 await()，该线程会直接通过。但 CyclicBarrier 的计数器是可以循环利用的，而且具备自动重置的功能，一旦计数器减到 0 会自动重置到你设置的初始值。除此之外，CyclicBarrier 还可以设置回调函数，可以说是功能丰富。



e.g.  对账系统实现

1.使用`CountDownLatch`

```java
// 创建2个线程的线程池
Executor executor = 
  Executors.newFixedThreadPool(2);
while(存在未对账订单){
  // 计数器初始化为2
  CountDownLatch latch = 
    new CountDownLatch(2);
  // 查询未对账订单
  executor.execute(()-> {
    pos = getPOrders();
    latch.countDown();
  });
  // 查询派送单
  executor.execute(()-> {
    dos = getDOrders();
    latch.countDown();
  });
  
  // 等待两个查询操作结束
  latch.await();
  
  // 执行对账操作
  diff = check(pos, dos);
  // 差异写入差异库
  save(diff);
}
```

![](https://static001.geekbang.org/resource/image/a5/3b/a563c39ece918578ad2ff33ab5f3743b.png)

#### 优化：

1.这两个查询操作和对账操作也是**可以并行**的，也就是说，在执行对账操作的时候，可以同时去执行下一轮的查询操作；

2.两次查询操作能够和对账操作并行，对账操作还依赖查询操作的结果，这明显有点**生产者 - 消费者**的意思，两次查询操作是生产者，对账操作是消费者。既然是生产者 - 消费者模型，那就**需要有个队列，来保存生产者生产的数据，而消费者则从这个队列消费数据**。

3.**两个队列的元素之间还有对应关系**。具体如下图所示，订单查询操作将订单查询结果插入订单队列，派送单查询操作将派送单插入派送单队列，这两个队列的元素之间是有一一对应的关系的。两个队列的好处是，**对账操作可以每次从订单队列出一个元素，从派送单队列出一个元素，然后对这两个元素执行对账操作，这样数据一定不会乱掉。**

![](https://static001.geekbang.org/resource/image/22/da/22e8ba1c04a3bc2605b98376ed6832da.png)

![](https://static001.geekbang.org/resource/image/65/ad/6593a10a393d9310a8f864730f7426ad.png)

2.使用`CyclicBarrier`

```java
// 订单队列
Vector<P> pos;
// 派送单队列
Vector<D> dos;
// 执行回调的线程池 
Executor executor = 
  Executors.newFixedThreadPool(1);
final CyclicBarrier barrier =
  new CyclicBarrier(2, ()->{
    executor.execute(()->check());
  });
  
void check(){
  P p = pos.remove(0);
  D d = dos.remove(0);
  // 执行对账操作
  diff = check(p, d);
  // 差异写入差异库
  save(diff);
}
  
void checkAll(){
  // 循环查询订单库
  Thread T1 = new Thread(()->{
    while(存在未对账订单){
      // 查询订单库
      pos.add(getPOrders());
      // 等待
      barrier.await();
    }
  });
  T1.start();  
  // 循环查询运单库
  Thread T2 = new Thread(()->{
    while(存在未对账订单){
      // 查询运单库
      dos.add(getDOrders());
      // 等待
      barrier.await();
    }
  });
  T2.start();
}
```



## CyclicBarrier的回调函数在哪个线程执行？

CyclicBarrier的回调函数在哪个线程执行啊？主线程吗？比如这里的最后一段代码中，循环会在回调的时候阻塞吗？
如果是这样的话，那check函数岂不是可以直接作为回调函数了呀，并不需要线程池了啊

> CyclicBarrier的回调函数执行在一个回合里最后执行await()的线程上，而且同步调用回调函数check()，调用完check之后，才会开始第二回合。所以check如果不另开一线程异步执行，就起不到性能优化的作用了







### LockSupprot.park/unpark 和 Thread.suspend/ resume 有什么区别

两者都不会释放资源。

Thread.resume不可以执行在suspend之前，而unpark可以执行在park之前。

由于编译器优化，不能保证resume和suspend的执行顺序，所以基本不适用suspend和resume方法了。