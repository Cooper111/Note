# 面试复习资料

- <https://blog.csdn.net/qingtian211/article/details/81316067>



# 概述

`AQS 是基于 volitale 和 CAS 实现的`

队列同步器`AbstractQueuedSynchronizer`（以下简称同步器），**是用来构建锁或者其他同步组件的基础框架**

- 它使用了一个`int`成员变量表示**同步状态**

  ```
  AQS 是基于 volitale 和 CAS 实现的，其中 AQS 中维护一个 valitale 类型的变量 state 来做一个可重入锁的重入次数，加锁和释放锁也是围绕这个变量来进行的。state 为0表示没有任何线程持有这个锁，线程持有该锁后将 state 加1，释放时减1。多次持有释放则多次加减。
  ```

- 通过内置的**FIFO双向队列**来完成**获取锁线程**的排队工作

  - 链表除了头结点外，每一个节点都记录了线程的信息，代表一个等待线程。
  - 同步器包含两个节点类型的应用，一个指向头节点，一个指向尾节点，未获取到锁的线程会创建节点线程安全（`compareAndSetTail`）的加入队列尾部。同步队列遵循FIFO，首节点是获取同步状态成功的节点。
  - 未获取到锁的线程将创建一个节点，设置到尾节点
  - 首节点的线程在释放锁时，将会唤醒后继节点。而后继节点将会在获取锁成功时将自己设置为首节点

![](F:/JAVA/%E5%B9%B6%E5%8F%91/images/AQS.png)



# 三大考点

- [AQS独占锁模式](<https://www.cnblogs.com/lfls/p/7598380.html>)
- [AQS共享锁模式](<https://www.cnblogs.com/lfls/p/7599863.html>)
- [AQS条件队列](<https://www.cnblogs.com/lfls/p/7615982.html?utm_source=debugrun&utm_medium=referral>)



## 结点状态`waitStatus`

变量waitStatus则表示当前Node结点的等待状态，共有5种取值CANCELLED、SIGNAL、CONDITION、PROPAGATE、0。

- **CANCELLED**(1)：表示当前结点已取消调度。当timeout或被中断（响应中断的情况下），会触发变更为此状态，进入该状态后的结点将不会再变化。
- **SIGNAL**(-1)：表示后继结点在等待当前结点唤醒。后继结点入队时，会将前继结点的状态更新为SIGNAL。
- **CONDITION**(-2)：表示结点等待在Condition上，当其他线程调用了Condition的signal()方法后，CONDITION状态的结点将**从等待队列转移到同步队列中**，等待获取同步锁。
- **PROPAGATE**(-3)：**共享模式下**，前继结点不仅会唤醒其后继结点，同时也可能会唤醒后继的后继结点。
- **0**：新结点入队时的默认状态。

注意，**负值表示结点处于有效等待状态，而正值表示结点已被取消。所以源码中很多地方用>0、<0来判断结点的状态是否正常**。





# [0x01  AQS之独占锁模式](https://www.cnblogs.com/lfls/p/7598380.html)

### 【重点】函数流程如下：

      1. `tryAcquire()`**尝试直接去获取资源**，如果成功则直接返回（这里体现了非公平锁，每个线程获取锁时会尝试直接抢占加塞一次，而CLH队列中可能还有别的线程在等待）；
      2. `addWaiter()`将**该线程加入等待队列的尾部**，并标记为**独占模式**；
      3. `acquireQueued()`**使线程阻塞在等待队列中获取资源，一直获取到资源后才返回**。如果在整个等待过程中被中断过，则返回true，否则返回false。
      4. 如果线程在等待过程中被中断过，它是不响应的。只是获取资源后才再进行自我中断`selfInterrupt()`，将中断补上。





下面以 `ReentrantLock` 非公平锁的代码看看 AQS 的原理

## 请求锁

请求锁时有三种可能：

1. 如果没有线程持有锁，则请求成功，当前线程直接获取到锁。
2. 如果当前线程已经持有锁，则使用 CAS 将 state 值加1，表示自己再次申请了锁，释放锁时减1。这就是可重入性的实现。
3. 如果由其他线程持有锁，那么将自己添加进等待队列。

```java
final void lock() {
    if (compareAndSetState(0, 1))   
        setExclusiveOwnerThread(Thread.currentThread()); //没有线程持有锁时，直接获取锁，对应情况1
    else
        acquire(1);
}
 
public final void acquire(int arg) {
    if (!tryAcquire(arg) && //在此方法中会判断当前持有线程是否等于自己，对应情况2
        acquireQueued(addWaiter(Node.EXCLUSIVE), arg)) //将自己加入队列中，对应情况3
        selfInterrupt();
}
```

当`acquireQueued`返回True，只能是获取资源成功并且是被中断唤醒的

这里`acquireQueued`函数的返回值代表是否是被中断唤醒的，如果是，执行`selfInterrupt();`

如果线程在等待过程中被中断过，它是不响应的。只是获取资源后才再进行自我中断`selfInterrupt()`，将中断补上。

## 创建 Node 节点并加入链表

如果没竞争到锁，这时候就要进入等待队列。队列是默认有一个 head 节点的，并且不包含线程信息。上面情况3中，`addWaiter` 会创建一个 Node，并添加到链表的末尾，Node 中持有当前线程的引用。同时还有一个成员变量 `waitStatus`，表示线程的等待状态，初始值为0。我们还需要关注两个值：

- CANCELLED，值为1，表示取消状态，就是说我不要这个锁了，请你把我移出去。
- SINGAL，值为-1，表示下一个节点正在挂起等待，注意是下一个节点，不是当前节点。

同时，加到链表末尾的操作使用了 CAS+死循环的模式，很有代表性，拿出来看一看：

```java
//addWaiter
Node node = new Node(mode);
for (;;) {
    Node oldTail = tail;
    if (oldTail != null) {
        U.putObject(node, Node.PREV, oldTail);
        if (compareAndSetTail(oldTail, node)) {
            oldTail.next = node;
            return node;
        }
    } else {
        initializeSyncQueue();
    }
}
```

可以看到，在死循环里调用了 CAS 的方法。如果多个线程同时调用该方法，那么每次循环都只有一个线程执行成功，其他线程进入下一次循环，重新调用。N个线程就会循环N次。这样就在无锁的模式下实现了并发模型。



## 挂起等待

- `acquireQueued`如果此节点的上一个节点是头部节点，则再次尝试获取锁，获取到了就移除并返回。获取不到就进入下一步`shouldParkAfterFailedAcquire`函数；

- 判断前一个节点的 `waitStatus`，如果是 SINGAL，则返回 true，并调用 `LockSupport.park()` 将线程挂起；

- 如果是 CANCELLED，则将前一个节点移除；

- 如果是其他值，则将前一个节点的 `waitStatus` 标记为 SINGAL，进入下一次循环。

  **这里的判断函数**是`shouldParkAfterFailedAcquire(Node pred, Node node)`

  > 它是用来**判断当前节点是否可以被挂起，也就是唤醒条件是否已经具备**，即如果挂起了，那一定是可以由其他线程来唤醒的。该方法如果返回false，即挂起条件没有完备，那就会重新执行`acquireQueued`方法的循环体，进行重新判断，如果返回true，那就表示万事俱备，可以挂起了

可以看到，一个线程最多有两次机会，还竞争不到就去挂起等待。

```java
//acquireQueued
final boolean acquireQueued(final Node node, int arg) {
    try {
        boolean interrupted = false;
        for (;;) {
            final Node p = node.predecessor();
            if (p == head && tryAcquire(arg)) {
                setHead(node);
                p.next = null; // help GC
                return interrupted;
            }
            if (shouldParkAfterFailedAcquire(p, node) &&
                parkAndCheckInterrupt())
                interrupted = true;
        }
    } catch (Throwable t) {
        cancelAcquire(node);
        throw t;
    }
}
```

当acquireQueued返回True，只能是获取资源成功并且是被中断唤醒的

## 释放锁

- 调用 tryRelease，此方法由子类实现。实现非常简单，如果当前线程是持有锁的线程，就将 state 减1。减完后如果 state 大于0，表示当前线程仍然持有锁，返回 false。如果等于0，表示已经没有线程持有锁，返回 true，进入下一步；
- 如果头部节点的 `waitStatus` 不等于0（为0代表已经被取消），则调用`LockSupport.unpark()`唤醒其下一个节点。头部节点的下一个节点就是等待队列中的第一个线程，这反映了 AQS 先进先出的特点。另外，即使是非公平锁，进入队列之后，还是得按顺序来。

```java
public final boolean release(int arg) {
    if (tryRelease(arg)) { //将 state 减1
        Node h = head;
        if (h != null && h.waitStatus != 0)
            unparkSuccessor(h);
        return true;
    }
    return false;
}
 
private void unparkSuccessor(Node node) {
    int ws = node.waitStatus;
    if (ws < 0)
        node.compareAndSetWaitStatus(ws, 0);
        
    Node s = node.next;
    if (s == null || s.waitStatus > 0) { 
        s = null;
        for (Node p = tail; p != node && p != null; p = p.prev)
            if (p.waitStatus <= 0)
                s = p;
    }
    if (s != null) //唤醒第一个等待的线程
        LockSupport.unpark(s.thread);
}
```

执行`unpark`后，被唤醒的线程在函数`parkAndCheckInterrupt`中

```java
private final boolean parkAndCheckInterrupt() {
        LockSupport.park(this);
        //被唤醒之后，返回中断标记，即如果是正常唤醒则返回false，如果是由于中断醒来，就返回true
        return Thread.interrupted();
    }
```

**Thread.interrupted()方法在返回中断标记的同时会清除中断标记，也就是说当由于中断醒来然后获取锁成功，那么整个`acquireQueued`方法就会返回true表示是因为中断醒来，但如果中断醒来以后没有获取到锁，继续挂起，由于这次的中断已经被清除了，下次如果是被正常唤醒，那么`acquireQueued`方法就会返回false，表示没有中断。**



## 公平锁如何实现

上面分析的是非公平锁，那公平锁呢？很简单，在竞争锁之前判断一下等待队列中有没有线程在等待就行了。

```java
protected final boolean tryAcquire(int acquires) {
    final Thread current = Thread.currentThread();
    int c = getState();
    if (c == 0) {
        if (!hasQueuedPredecessors() && //判断等待队列是否有节点
            compareAndSetState(0, acquires)) {
            setExclusiveOwnerThread(current);
            return true;
        }
    }
    ......
    return false;
}
```

我的理解就是如最上面的图，每次入队列不会去和state比较，而是直接入队



# [0x02  AQS之共享锁模式](https://www.cnblogs.com/lfls/p/7599863.html)

#### 一、执行过程概述

获取锁的过程：

1. 当线程调用acquireShared()申请获取锁资源时，如果成功，则进入临界区。
2. 当获取锁失败时，则创建一个共享类型的节点并进入一个FIFO等待队列，然后被挂起等待唤醒。
3. 当队列中的等待线程被唤醒以后就重新尝试获取锁资源，如果成功则**唤醒后面还在等待的共享节点并把该唤醒事件传递下去，即会依次唤醒在该节点后面的所有共享节点**，然后进入临界区，否则继续挂起等待。

释放锁过程：

1. 当线程调用`releaseShared()`进行锁资源释放时，如果释放成功，则唤醒队列中等待的节点，如果有的话。





**获取锁的方法**`acquireShared()`，如下

```
   public final void acquireShared(int arg) {
        //尝试获取共享锁，返回值小于0表示获取失败
        if (tryAcquireShared(arg) < 0)
            //执行获取锁失败以后的方法
            doAcquireShared(arg);
    }
```

这里`tryAcquireShared()`方法是留给用户去实现具体的获取锁逻辑的。关于该方法的实现有两点需要特别说明：

### 以下这块是理解的核心

```
tryAcquireShared()依然需要自定义同步器去实现。但是AQS已经把其返回值的语义定义好了：负值代表获取失败；0代表获取成功，但没有剩余资源；正数表示获取成功，还有剩余资源，其他线程还可以去获取。所以这里acquireShared()的流程就是：

   1. tryAcquireShared()尝试获取资源，成功则直接返回；
   2. 失败则通过doAcquireShared()进入等待队列，直到获取到资源为止才返回。
```

再看**执行获取锁失败以后的方法**：`doAcquireShared`

```java
//其内addWaiter(Node.SHARED)创建一个共享类型的节点并进入一个FIFO等待队列，然后被挂起等待唤醒
//doAcquireShared函数使线程阻塞在等待队列中获取资源，一直获取到资源后才返回。等于0表示不用唤醒后继节点，大于0需要

private void doAcquireShared(int arg) {
    final Node node = addWaiter(Node.SHARED);//加入队列尾部
    boolean failed = true;//是否成功标志
    try {
        boolean interrupted = false;//等待过程中是否被中断过的标志
        for (;;) {
            final Node p = node.predecessor();//前驱
            if (p == head) {//如果到head的下一个，因为head是拿到资源的线程，此时node被唤醒，很可能是head用完资源来唤醒自己的
                int r = tryAcquireShared(arg);//尝试获取资源
                if (r >= 0) {//成功
                    setHeadAndPropagate(node, r);//将head指向自己，还有剩余资源可以再唤醒之后的线程
                    p.next = null; // help GC
                    if (interrupted)//如果等待过程中被打断过，此时将中断补上。
                        selfInterrupt();
                    failed = false;
                    return;
                }
            }
            
            //判断状态，寻找安全点，进入waiting状态，等着被unpark()或interrupt()
            if (shouldParkAfterFailedAcquire(p, node) &&
                parkAndCheckInterrupt())
                interrupted = true;
        }
    } finally {
        if (failed)
            cancelAcquire(node);
    }
}
```

`setHeadAndPropagate`方法，除了设置新的头结点以外还有一个传递动作

**上面的`setHeadAndPropagate()`方法表示等待队列中的线程成功获取到共享锁，这时候它需要唤醒它后面的共享节点（如果有），**

```java
private void setHeadAndPropagate(Node node, int propagate) {
    Node h = head; 
    setHead(node);//head指向自己
     //如果还有剩余量，继续唤醒下一个邻居线程
    if (propagate > 0 || h == null || h.waitStatus < 0) {
        Node s = node.next;
        if (s == null || s.isShared())
            doReleaseShared();//释放掉资源后，唤醒后继
    }
}
```

**但是当通过`releaseShared（）`方法去释放一个共享锁的时候，接下来等待独占锁跟共享锁的线程都可以被唤醒进行尝试获取。**



# [0x03  AQS之条件队列](https://www.cnblogs.com/lfls/p/7615982.html)

条件队列是建立在锁基础上的，而且必须是**独占锁**。

**整个AQS分为两个队列，一个同步队列，一个条件队列。只有同步队列中的节点才能获取锁。前面两篇独占锁共享锁文章中提到的加入队列就是同步队列。条件队列中所谓的唤醒是把节点从条件队列移到同步队列，让节点有机会去获取锁。**

#### 执行过程概述

**等待条件的过程**：

1. 在操作条件队列之前首先需要成功获取独占锁，不然直接在获取独占锁的时候已经被挂起了。
2. （加入条件队列，释放锁资源，挂起）成功获取独占锁以后，如果当前条件还不满足，则在当前锁的**条件队列**上**挂起**，与此同时**释放掉当前获取的锁资源**。这里可以考虑一下如果不释放锁资源会发生什么？
3. 如果被唤醒，则检查是否可以获取独占锁，否则继续挂起。

**条件满足后的唤醒过程**（以唤醒一个节点为例，也可以唤醒多个）：

1. 把当前等待队列中的第一个有效节点（如果被取消就无效了）加入同步队列等待被前置节点唤醒，如果此时前置节点被取消，则直接唤醒该节点让它重新在同步队列里适当的尝试获取锁或者挂起。

> 如果被唤醒且已经被转移到了同步队列，则会执行与独占锁一样的方法`acquireQueued()`进行同步队列独占获取。



**为什么是独占锁？**

我的理解：

1.因为把节点加入到条件队列中以后，接下来要做的就是释放锁资源，这里要获取当前的state并释放，这个state不能并发

2.条件队列唤醒也是需要独占锁的，`isHeldExclusively()`



## LockSupport.park()会释放锁资源吗？

不会，它只负责阻塞当前线程，释放锁资源实际上是在Condition的await()方法中实现的。



### AQS的简单应用：Mutex（互斥锁）

```java
//Mutex内部类Sync继承AQS，改写了其中的tryAcquire和tryRelease

// 尝试获取资源，立即返回。成功则返回true，否则false。
        public boolean tryAcquire(int acquires) {
            assert acquires == 1; // 这里限定只能为1个量
            if (compareAndSetState(0, 1)) {//state为0才设置为1，不可重入！
                setExclusiveOwnerThread(Thread.currentThread());//设置为当前线程独占资源
                return true;
            }
            return false;
        }

        // 尝试释放资源，立即返回。成功则为true，否则false。
        protected boolean tryRelease(int releases) {
            assert releases == 1; // 限定为1个量
            if (getState() == 0)//既然来释放，那肯定就是已占有状态了。只是为了保险，多层判断！
                throw new IllegalMonitorStateException();
            setExclusiveOwnerThread(null);
            setState(0);//释放资源，放弃占有状态
            return true;
        }
```

同步类在实现时一般都将自定义同步器（sync）定义为内部类，供自己使用；而同步类自己（Mutex）则实现某个接口，对外服务。当然，接口的实现要直接依赖sync，它们在语义上也存在某种对应关系！！而sync只用实现资源state的获取-释放方式tryAcquire-tryRelelase，至于线程的排队、等待、唤醒等，上层的AQS都已经实现好了，我们不用关心。

　　除了`Mutex，ReentrantLock/CountDownLatch/Semphore`这些同步类的实现方式都差不多，不同的地方就在获取-释放资源的方式`tryAcquire-tryRelelase`。掌握了这点，AQS的核心便被攻破了！



# 参考链接

- 【框架清楚】<https://www.cnblogs.com/waterystone/p/4920797.html>
- 【具体】<https://blog.csdn.net/u010325193/article/details/86590169>
- 【具体】<https://www.cnblogs.com/lfls/p/7599863.html>
- 【具体】<https://www.cnblogs.com/lfls/p/7615982.html?utm_source=debugrun&utm_medium=referral>
- [Java中的锁[原理、锁优化、CAS、AQS]](<https://mp.weixin.qq.com/s?__biz=MzI2OTQ4OTQ1NQ==&mid=2247487982&idx=2&sn=e02303bab7a3ffe462fbd6ff779e6ac4&chksm=eaded5aedda95cb87ee6140d9e9f0d94c1bd2d977d7e028991ecc79869653f0aa0d817569457&mpshare=1&scene=23&srcid=&sharer_sharetime=1583059275113&sharer_shareid=e6d90aec84add5cf004cb1ab6979727c#rd>)
- **超级好文**：<https://www.javadoop.com/>

- [我画了35张图就是为了让你深入 AQS](<https://mp.weixin.qq.com/s?__biz=MzU2NDg0OTgyMA==&mid=2247486513&idx=1&sn=f35b96ff326359ae42fbbfc05904d6b6&chksm=fc45f1c2cb3278d44980c705f7a299fc1516ef580d563c096fa992cd7a99fdcdf66ab93b47fc&mpshare=1&scene=23&srcid=&sharer_sharetime=1588393089592&sharer_shareid=e6d90aec84add5cf004cb1ab6979727c#rd>)

