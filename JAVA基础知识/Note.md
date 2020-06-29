- [java string ==和equals的使用](https://www.cnblogs.com/gc65/p/java.html)

- java有多少种数据类型？

  ```java
  //https://www.php.cn/java-class-code-415635.html
  8种基本数据类型，xxxx，还有。。。
  引用数据类型：类，数组，接口
  基本类型与引用类型区别：基本数据类型和引用类型的区别主要在于基本数据类型是分配在栈上的，而引用类型是分配在堆上的
  ```

- 下面关于一个类的静态成员描述中,不正确的是()

  ```
  A静态成员变量可被该类的所有方法访问
  B该类的静态方法只能访问该类的静态成员函数
  C该类的静态数据成员变量的值不可修改
  D子类可以访问父类的静态成员
  E静态成员无多态特性
  ```

  答案C

  类的静态成员属于整个类 而不是某个对象，可以被类的所有方法访问，子类当然可以父类静态成员；
  静态方法属于整个类，在对象创建之前就已经分配空间，类的非静态成员要在对象创建后才有内存，所有静态方法只能访问静态成员，不能访问非静态成员；
  静态成员可以被任一对象修改，修改后的值可以被所有对象共享。



- String类难点:<https://blog.csdn.net/u010775025/article/details/86507090>

- Float，Double类难点：[类型升级 ————精度丢失问题](<https://blog.csdn.net/weixin_44736274/article/details/90769042>)

- [JAVA中try、catch、finally带return的执行顺序总结](https://www.cnblogs.com/pcheng/p/10968841.html)

- [java中 count=count++等于0的原因](https://blog.csdn.net/qq_40301026/article/details/95540215)
- [count++和count=count+1效率问题](https://blog.csdn.net/lv836735240/article/details/38496623?depth_1-utm_source=distribute.pc_relevant.none-task&utm_source=distribute.pc_relevant.none-task)

- [LinkedHashMap解析，实现LRU](<https://www.cnblogs.com/mengheng/p/3683137.html>)

  ```java
  //注意点
  //这里的addBefore(lm.header)是做什么呢?再看
  private void addBefore(Entry<K,V> existingEntry) {
              after  = existingEntry;//这里existingEntry是已有的链表
              before = existingEntry.before;
              before.after = this;//注意这里的this，是指要插入的元素e
              after.before = this;
          }
  ```

  