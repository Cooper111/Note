# Blocking阻塞队列

有空必看！<https://www.javadoop.com/post/java-concurrent-queue>



关于LinkedBlockingQueue理解难点的概要：

- cascading notifies（瀑布流式通知）
- 为什么两把锁（数组链表、并发性、出现n个take线程阻塞情况，不同的生产者消费者协调方式）



其中的疑难点-LinkedBlockingQueue两把锁同时一读一写下的控制<http://www.imooc.com/article/34490>

```
当一个put操作加入了一个元素，至少可以提供一次take操作的时候，就会signal一个taker，这个taker在做take操作的时候会检查从put操作通知他到到他去take这个过程是否有更多元素进来，有的话会去signal其他taker，然后其他taker有继续signal其他的taker，如此往下，形成级联通知(直译，貌似还挺能表达这个意思)。take操作对puter的通知也是类似的操作。

作者：潇潇雨雨
链接：http://www.imooc.com/article/34490
来源：慕课网
```

===================================================================================

**关于LinkedBlobkingQueue中的put和take的    if (c + 1 < capacity) notFull.signal();  if (c > 1) notEmpty.signal()**

是这样的:

像我们一般会像ArrayBlockingQueue那样思考，同一个锁，满了，等消费者唤醒，空了，等生产者唤醒。 但是Linked有个情况,例如现在是空的，有两个take线程阻塞了，现在有两个put线程生产了两个，如果像上面那样思考，如果原来是0才唤醒消费者那么，消费者只唤醒其中一个，但是明明还有1个没有消费，这样第二个消费者一直阻塞。所以Linked才会消费者唤醒消费者 有点像AQS队列的Node唤醒后面的Node。

还有一点就是Linked比Array机制不太一样，像上面那种情况，Array不满是不会唤醒消费者的,capicity是5，put 2个 不唤醒消费者，而Linked会唤醒。

如果需求是定量批处理，如每次处理5个，Arrays            如果是像来多少个处理多少个 Linked :)

======================================================================================

博主你好，关于LinkedBlockingQueue有个问题请教一下。

```java
public void put(E e) throws InterruptedException {     ...     if (c + 1 < capacity)             notFull.signal();         //这一行代码不解     } finally {     ...

public E take() throws InterruptedException {     ...
    if (c > 1)             notEmpty.signal();         //这一行代码不解     } finally {
```

疑惑在于，put中的notFull.await，应该是由take方来signal， take中的notEmpty,应该是由put方来signal。

那么finally上一行的signal是在什么情况下使用的呢？

> <https://codereview.stackexchange.com/questions/87141/arrayblockingqueue-concurrent-put-and-take> 说这个signal的追加，是处于性能的考虑。这种用法叫做cascading notifies，我翻译成瀑布流式通知。
>
> Also, to minimize need for puts to get takeLock and vice-versa, cascading notifies are used. When a put notices that it has enabled at least one take, it signals taker. That taker in turn signals others if more items have been entered since the signal. And symmetrically for takes signalling puts. （上面的话，在源码的类的注释中可以找到）
>
> 对于puts来说，最小化对takeLock的需求，反之亦然

=============================================================================

> 在ArrayBlockingQueue中，速率的调控是通过生产者唤醒消费者，消费者唤醒生产者互相作用来实现的调控。
>
> 在LinkedBlockingQueue中，则是生产者在队列未满的情况下唤醒生产者，也就是finally之前的       if (c + 1 < capacity)    notFull.signal();，消费者在队列不为空的时候唤醒消费者，对应的是if (c > 1)   notEmpty.signal();  但是存在两种特殊情况：
>
>  1 .假设队列满了，生产者可能全部处于await状态，那么此时就需要消费者出队后唤醒生产者。也就是take操作return之前的signalNotFull()
>
>  2 .假设队列为空，消费者可能全部处于await状态，那么此时就需要生产者生产之后唤醒消费者，也就是put操作return之前的signalNotEmpty()
>
> 感觉这就是两种不同的生产者消费者协调策略。





### ConcurrentLinkedQueue

- ConcurrentLinkedQueue对Node操作时采用了CAS操作：

  ```
  casItem方法更新Node的item值
  lazySetNext方法使得原先的head节点变为哨兵节点，方便GC
  casNext方法设置下一个Node点
  ```

- ConcurrentLinkedQueue类内部，head表示链表头部（不为null），tail表示链表尾部（tail是每两次更新一下）

- offer()方法，队尾插入

  - 调用offer()第一次casNext插入，tail不更新，和head重合；
  - 调用offer()第二次先循环找到最后一个点（循环被修改？目前tail:下一节点），再casNext插入，再casTail更新tail
  - 如果遇到哨兵节点，从头开始遍历

- poll()方法，队头弹出（演示就调用了一次poll方法）

  - for循环第一次，head.item为null，所以找到下一节点
  - for循环第二次，cas操作删除弹出元素（`p.casItem(item, null)`）
  - for循环第三次，将原先头结点设置为哨兵（`lazySetNext`方法）

  

  

