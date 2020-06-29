<https://www.cnblogs.com/web424/p/6807582.html>



[Java](http://lib.csdn.net/base/javase)中实现多线程有两种方法：继承Thread类、实现Runnable接口，在程序开发中只要是多线程，肯定永远以实现Runnable接口为主，因为实现Runnable接口相比继承Thread类有如下优势：

​    1、可以避免由于Java的单继承特性而带来的局限；

​    2、增强程序的健壮性，代码能够被多个线程共享，**代码与数据是独立的；**

​    3、适合多个相同程序代码的线程区**处理同一资源**的情况。





概要：就是说，Thread的话内部变量不共享，Runnable的话变量共享



下面以典型的买票程序（基本都是以这个为例子）为例，来说明二者的区别。

​    首先通过继承Thread类实现，代码如下：

1. **class MyThread extends Thread{**  
2. ​    **private int ticket = 5;**  
3. ​    **public void run(){**  
4. ​        **for (int i=0;i<10;i++)**  
5. ​        {  
6. ​            **if(ticket > 0){**  
7. ​                System.out.println("ticket = " + ticket--);  
8. ​            }  
9. ​        }  
10. ​    }  
11. }  
12.   
13. **public class ThreadDemo{**  
14. ​    **public static void main(String[] args){**  
15. ​        **new MyThread().start();**  
16. ​        **new MyThread().start();**  
17. ​        **new MyThread().start();**  
18. ​    }  
19. }  

​    某次的执行结果如下：

![img](http://img.blog.csdn.net/20131206094239437)



​    从结果中可以看出，每个线程单独卖了5张票，即独立地完成了买票的任务，但实际应用中，比如火车站售票，需要多个线程去共同完成任务，在本例中，即多个线程共同买5张票。

​    下面是通过实现Runnable接口实现的多线程程序，代码如下：

1. **class MyThread implements Runnable{**  
2. ​    **private int ticket = 5;**  
3. ​    **public void run(){**  
4. ​        **for (int i=0;i<10;i++)**  
5. ​        {  
6. ​            **if(ticket > 0){**  
7. ​                System.out.println("ticket = " + ticket--);  
8. ​            }  
9. ​        }  
10. ​    }  
11. }  
12.   
13. **public class RunnableDemo{**  
14. ​    **public static void main(String[] args){**  
15. ​        MyThread my = **new MyThread();**  
16. ​        **new Thread(my).start();**  
17. ​        **new Thread(my).start();**  
18. ​        **new Thread(my).start();**  
19. ​    }  
20. }  

​    某次的执行结果如下:

![img](http://img.blog.csdn.net/20131206094642328)

​    从结果中可以看出，三个线程一共卖了5张票，即它们共同完成了买票的任务，实现了资源的共享。



  **针对以上代码补充三点：**

​    1、在第二种方法（Runnable）中，ticket输出的顺序并不是54321，这是因为线程执行的时机难以预测，ticket--并不是原子操作。

​    2、在第一种方法中，我们new了3个Thread对象，即三个线程分别执行三个对象中的代码，因此便是三个线程去独立地完成卖票的任务；而在第二种方法中，我们同样也new了3个Thread对象，但只有一个Runnable对象，3个Thread对象共享这个Runnable对象中的代码，因此，便会出现3个线程共同完成卖票任务的结果。如果我们new出3个Runnable对象，作为参数分别传入3个Thread对象中，那么3个线程便会独立执行各自Runnable对象中的代码，即3个线程各自卖5张票。

​    3、在第二种方法中，由于3个Thread对象共同执行一个Runnable对象中的代码，因此可能会造成线程的不安全，比如可能ticket会输出-1（如果我们System.out....语句前加上线程休眠操作，该情况将很有可能出现），这种情况的出现是由于，一个线程在判断ticket为1>0后，还没有来得及减1，另一个线程已经将ticket减1，变为了0，那么接下来之前的线程再将ticket减1，**便得到了-1**。**这就需要加入同步操作（即互斥锁）**，确保同一时刻只有一个线程在执行每次for循环中的操作。而在第一种方法中，并不需要加入同步操作，因为每个线程执行自己Thread对象中的代码，不存在多个线程共同执行同一个方法的情况。



这是前面的例子，**Runnable加锁的情况**

<https://blog.51cto.com/13501268/2071911>



```java
三．使用synchronized关键字实现同步（卖票问题）

1.synchronized的使用方法：

（1）同步代码块：synchronized放在对象前面限制一段代码的执行

Synchronized(同步代码块){

需要同步的代码

}

（2）同步方法：synchronized放在方法声明中,表明同步方法

public synchronized void method(){

.........

}

2.加上同步机制后，效率低的原因：

（1）会丧失Java多线程的并发优势，在执行到同步代码块（或同步方法）时，只能有一个线程执行，其他线程必须等待执行同步代码块（同步方法）的线程释放同步对象的锁

（2）其他等待锁释放的线程会不断检查锁的状态

例1（同步代码块）：

包含同步代码块的线程：

package synchronizeddemo;

 

public class SynchronizedRun implements Runnable{

private int tickets=5;

 

@Override

public void run() {

for (int i = 1; i <=100; i++) {//故意是循环次数大于总票数

synchronized (this) {//同步代码块

if(tickets>0){//如果还有票则出售

try {

Thread.sleep(1000);

} catch (InterruptedException e) {

e.printStackTrace();

}

System.out.println(Thread.currentThread().getName()+"正在出售第"+(tickets--)+"张票");

}

}

}

}

 

}

测试同步代码块：

package synchronizeddemo;

 

public class TestSyn {

 

public static void main(String[] args) {

new Thread(new SynchronizedRun(),"窗口一——>").start();

new Thread(new SynchronizedRun(),"窗口二——>").start();

new Thread(new SynchronizedRun(),"窗口三——>").start();

 

}

 

}

运行结果：

窗口一——>正在出售第5张票

窗口三——>正在出售第5张票

窗口二——>正在出售第5张票

窗口三——>正在出售第4张票

窗口二——>正在出售第4张票

窗口一——>正在出售第4张票

窗口三——>正在出售第3张票

窗口二——>正在出售第3张票

窗口一——>正在出售第3张票

窗口三——>正在出售第2张票

窗口二——>正在出售第2张票

窗口一——>正在出售第2张票

窗口三——>正在出售第1张票

窗口一——>正在出售第1张票

窗口二——>正在出售第1张票

**由结果可见，并未实现资源共享，因为在循环的过程中，每次都创建性新线程拿到的都是新锁，与前面的锁不同

**测试类稍作改进后：

package synchronizeddemo;

 

public class TestSyn {

 

public static void main(String[] args) {

SynchronizedRun sr=new SynchronizedRun();

new Thread(sr,"窗口一——>").start();

new Thread(sr,"窗口二——>").start();

new Thread(sr,"窗口三——>").start();

 

}

 

}

运行结果：

窗口一——>正在出售第5张票

窗口一——>正在出售第4张票

窗口三——>正在出售第3张票

窗口三——>正在出售第2张票

窗口三——>正在出售第1张票

例2（同步方法）：

创建同步方法线程：

package synchronizeddemo;

 

public class SynchronizedRun implements Runnable{

private int tickets=5;

 

@Override

public void run() {

for (int i = 0; i <100; i++) {

sell();

}

 

}

 

public synchronized void sell() {//同步方法，同步方法中this充当同步 对象

if(tickets>0){//如果还有票，就卖票

try {

Thread.sleep(1000);

} catch (InterruptedException e) {

e.printStackTrace();

}

System.out.println(Thread.currentThread().getName()+"正在出售第"+(tickets--)+"张票");

}

 

}

 

}

测试同步方法线程：

package synchronizeddemo;

 

public class TestSyn {

 

public static void main(String[] args) {

SynchronizedRun sr=new SynchronizedRun();

new Thread(sr,"窗口一——>").start();

new Thread(sr,"窗口二——>").start();

new Thread(sr,"窗口三——>").start();

 

}

 

}

运行结果：

窗口一——>正在出售第5张票

窗口一——>正在出售第4张票

窗口一——>正在出售第3张票

窗口三——>正在出售第2张票

窗口三——>正在出售第1张票
```

