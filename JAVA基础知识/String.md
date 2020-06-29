- [对Java中String.intern()方法的理解](https://blog.csdn.net/qq_33858191/article/details/80219757)
- [Java中的String为什么是不可变的？—String源码分析](https://blog.csdn.net/qq_37267015/article/details/78738512)
- 【好】[Java的String类为什么是不可变的以及字符串常量池](<https://blog.csdn.net/cryssdut/article/details/50782403>)

- [图文解读：5 个刁钻的 String 面试题！](<https://mp.weixin.qq.com/s?__biz=MzU0OTk3ODQ3Ng==&mid=2247487282&idx=1&sn=0c3f229bdd2e83926f861632146b25d8&chksm=fba6e731ccd16e2715c360b29462cb24bfbff6c9fe47e5c757ed2f0a2bd3f29fc52264fb7cb3&mpshare=1&scene=23&srcid=&sharer_sharetime=1587261029871&sharer_shareid=e6d90aec84add5cf004cb1ab6979727c#rd>)

- [为什么阿里巴巴不建议在for循环中使用"+"进行字符串拼接](<https://mp.weixin.qq.com/s?__biz=MzAwNDA2OTM1Ng==&mid=2453141826&idx=2&sn=759e6e18c4694b510ac8e0af31608b6c&chksm=8cf2dbc1bb8552d7575b31770cf5af9b25c470f57aa6a9727446ab99ae2d76b2c5767ff280f6&mpshare=1&scene=23&srcid=&sharer_sharetime=1586143986593&sharer_shareid=e6d90aec84add5cf004cb1ab6979727c#rd>)

- <https://blog.csdn.net/qq_34490018/article/details/82110578>

#### 效率方面：

1、**因为对象不可变，所以可以使用常量池的方式优化效率**。在拷贝或者创建内容相同对象时，就不必复制内容本身，而是只要复制它的地址即可，复制地址只需要很小的空间和时间。只有不可变对象才能使用常量池，因为可以保证引用同一常量值的多个变量不产生相互影响。



2、**因为对象不可变，所以String对象可以自身缓存HashCode。**Java中String对象的哈希码被频繁地使用, 比如在hashMap 等容器中。字符串不变性保证了hash码的唯一性,因此可以放心地进行缓存.这也是一种性能优化手段,意味着不必每次都去计算新的哈希码

> private int hash;*//用来缓存HashCode*



#### 安全性方面：

1、**String被许多的Java类(库)用来当做参数,例如 网络连接地址URL,文件路径path,还有反射机制所需要的String参数等, 假若String不是固定不变的,将会引起各种安全隐患**。