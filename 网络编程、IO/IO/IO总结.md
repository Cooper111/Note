### [BIO,NIO,AIO 总结](https://snailclimb.gitee.io/javaguide/#/docs/java/BIO-NIO-AIO?id=bionioaio-%e6%80%bb%e7%bb%93)

NIO是一种同步阻塞的I/O模型

NIO是一种同步非阻塞的I/O模型

AIO是一种异步非阻塞的I/O模型





Java IO 类的“奇怪”用法Java IO 类库非常庞大和复杂，有几十个类，负责 IO 数据的读取和写入。如果对 Java IO 类做一下分类，我们可以从下面两个维度将它划分为四类。具体如下所示：

![](https://static001.geekbang.org/resource/image/50/13/5082df8e7d5a4d44a34811b9f562d613.jpg)

针对不同的读取和写入场景，Java IO 又在这四个父类基础之上，扩展出了很多子类。具体如下所示：在我初学 Java 的时候，曾经对 Java IO 的一些用法产生过很大疑惑，比如下面这样一段代码。我们打开文件 test.txt，从中读取数据。其中，InputStream 是一个抽象类，FileInputStream 是专门用来读取文件流的子类。BufferedInputStream 是一个支持带缓存功能的数据读取类，可以提高数据读取的效率。