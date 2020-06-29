### [redis 的线程模型](https://snailclimb.gitee.io/javaguide/#/docs/database/Redis/Redis?id=redis-%e7%9a%84%e7%ba%bf%e7%a8%8b%e6%a8%a1%e5%9e%8b)

> 参考地址:<https://www.javazhiyin.com/22943.html>

redis 内部使用文件事件处理器 `file event handler`，这个文件事件处理器是单线程的，所以 redis 才叫做单线程的模型。它采用 IO 多路复用机制同时监听多个 socket，根据 socket 上的事件来选择对应的事件处理器进行处理。

文件事件处理器的结构包含 4 个部分：

- 多个 socket
- IO 多路复用程序
- 文件事件分派器
- 事件处理器（连接应答处理器、命令请求处理器、命令回复处理器）

**多个 socket 可能会并发产生不同的操作，每个操作对应不同的文件事件，但是 IO 多路复用程序会监听多个 socket，会将 socket 产生的事件放入队列中排队，事件分派器每次从队列中取出一个事件，把该事件交给对应的事件处理器进行处理。**



为什么Redis这么快？

- 纯内存
- 单线程，IO多路复用

然后说上面那段加粗的话