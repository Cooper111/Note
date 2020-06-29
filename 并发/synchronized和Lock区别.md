# [synchronized和Lock的异同](https://www.cnblogs.com/gejuncheng/p/10777691.html)

- ####    JAVA语言使用两种机制来实现堆某种共享资源的同步，synchronized和Lock。其中，synchronized使用Object对象本身的notify、wait、notifyAll调度机制，而lock可以使用Condition进行线程之间的调度，完成synchronized实现所有功能。

- **用法不一样**

  synchronized可以加方法上，代码中，括号表示要锁的对象；

  Lock需要制定起始位置和终点位置；

  synchronized是托管给JVM执行的；Lock是代码实现的，它有比synchronized更精确的线程定义

- **性能不一样**

  在JDK 5中增加的ReentrantLock。它不仅拥有和synchronized相同的并发性和内存语义，还增加了锁投票，定时锁，等候和中断锁等。它们的性能在不同情况下会不同：**在资源竞争不是很激励的情况下**，synchronized的性能要优于ReentrantLock，但**在资源紧张很激烈的情况下**，synchronized的性能会下降的很快，而ReentrantLock的性能基本保持不变。

- **锁机制不一样**

  synchronized获得锁和释放锁的机制都在代码块结构中，当获得锁时，必须以相反的机制去释放，并且自动解锁，不会因为异常导致没有被释放而导致死锁。

  而Lock需要开发人员手动去释放，并且写在finally代码块中，否则会可能引起死锁问题的发生。此外，Lock还提供的更强大的功能，可以通过tryLock的方式采用非阻塞的方式取获得锁。



##  虽然synchronized和Lock都可以用来实现线程同步，但最好不要同时使用两种放式，因为synchronized和ReentrantLock所使用的机制不同，但是他们是独立运行的，相当于两种类型的锁，在使用时不会影响。





## Lock的本质是AQS，再问下去就是AQS了！

于是学习：<https://blog.csdn.net/u010325193/article/details/86590169>