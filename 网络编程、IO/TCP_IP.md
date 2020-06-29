<https://www.nowcoder.com/test/question/done?tid=30500411&qid=44768#summary>

```
A经历SYN_RECV状态
B经历SYN_SEND状态
C经历ESTABLISHED状态
D经历TIME_WAIT状态
E服务器在收到syn包时将加入半连接队列
F服务器接受到客户端的ack包后将从半连接队列删除
```

D错

TCP建立连接时，首先客户端和服务器处于close状态。然后客户端发送SYN同步位，此时客户端处于SYN-SEND状态，服务器处于lISTEN状态，当服务器收到SYN以后，向客户端发送同步位SYN和确认码ACK，然后服务器变为SYN-RCVD，客户端收到服务器发来的SYN和ACK后，客户端的状态变成ESTABLISHED(已建立连接)，客户端再向服务器发送ACK确认码，服务器接收到以后也变成ESTABLISHED。然后服务器客户端开始数据传输。

A、B、C是TCP三次握手的状态，D是四次挥手后发送端的最后一个状态。详细状态转移图见 <http://www.cnblogs.com/tonyluis/p/5729531.html>
处在SYNC_RECV的TCP连接称为半连接，并存储在内核的半连接队列中，在内核收到对端发送的ack包时会查找半连接队列，并将符合的requst_sock信息存储到完成三次握手的连接的队列中，然后删除此半连接。

TIME_WAIT是拆除阶段的

