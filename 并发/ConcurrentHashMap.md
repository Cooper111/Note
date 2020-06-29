- <https://blog.csdn.net/java_yes/article/details/79842173?depth_1-utm_source=distribute.pc_relevant.none-task&utm_source=distribute.pc_relevant.none-task>
- [ConcurrentHashMap万字长文，太全面了！](<https://mp.weixin.qq.com/s?__biz=MzI2OTQxMTM4OQ==&mid=2247492309&idx=2&sn=859eed98138023fbc897299e3e7cdd6c&chksm=eae21787dd959e914abcf69e8536af6c4bbc3069372fe9e5922ad987d04f2eb23daa8c152679&mpshare=1&scene=23&srcid=&sharer_sharetime=1586966072057&sharer_shareid=e6d90aec84add5cf004cb1ab6979727c#rd>)
- [面试再被问到 ConcurrentHashMap，把这篇文章甩给他](<https://mp.weixin.qq.com/s?__biz=MzUzMTA2NTU2Ng==&mid=2247489331&idx=1&sn=cfe74834a83b8684ac76163871a95235&chksm=fa496882cd3ee194a09327a3f3498f4feb2caf8f05d723fc1c86d55458b02240a6d4fbaad95b&mpshare=1&scene=23&srcid=&sharer_sharetime=1586704965722&sharer_shareid=e6d90aec84add5cf004cb1ab6979727c#rd>)

- [我就知道面试官接下来要问我 ConcurrentHashMap 底层原理了](<https://mp.weixin.qq.com/s?__biz=MzAwNDA2OTM1Ng==&mid=2453141985&idx=2&sn=875412f607c18fa6ba6aa0b0862b7fe6&chksm=8cf2db62bb855274b424ca374a6d2ac9a9d2760032194f0e450f42add00f090126ae3bd4149b&mpshare=1&scene=23&srcid=&sharer_sharetime=1587950317980&sharer_shareid=e6d90aec84add5cf004cb1ab6979727c#rd>)

- [为什么ConcurrentHashmap的get方法不加锁](<https://mp.weixin.qq.com/s/exeWjwdXJ_9ota4lFO_W3Q>)



**在1.7中ConcurrentHashMap使用了分段锁，1.8中则使用了CAS算法。**

### 1.7 分段锁

ConcurrentHashMap是由Segment数组结构和HashEntry数组结构组成。

Segment是一种可重入锁ReentrantLock，在ConcurrentHashMap里扮演锁的角色，

HashEntry则用于存储键值对数据。

一个ConcurrentHashMap里包含一个Segment数组，Segment的结构和HashMap类似，是一种数组和链表结构， 一个Segment里包含一个HashEntry数组，每个HashEntry是一个链表结构的元素， 每个Segment守护者一个HashEntry数组里的元素,当对HashEntry数组的数据进行修改时，必须首先获得它对应的Segment锁。



### 1.8 CAS

与HashMap不相同的是，它并不是直接转换为红黑树，而是把这些结点包装成TreeNode放在TreeBin对象中，由TreeBin完成对红黑树的包装。而且TreeNode在ConcurrentHashMap集成自Node类，而并非HashMap中的集成自LinkedHashMap.Entry
在实际的ConcurrentHashMap“数组”中，存放的是TreeBin对象，而不是TreeNode对象，这是与HashMap的区别。另外TreeBean这个类还带有了读写锁。



### 插入方法：

1.*// 如果这个位置没有值，利用**CAS操作**直接存储在该位置，不需要加锁*

2.*//如果这个位置有值，**结点（Hash值相同的头结点）上锁（Synchronized）**，插入链表或者树*

```java
    /** Implementation for put and putIfAbsent */
    final V putVal(K key, V value, boolean onlyIfAbsent) {
            //不允许 key或value为null
        if (key == null || value == null) throw new NullPointerException();
        //计算hash值
        int hash = spread(key.hashCode());
        int binCount = 0;
        //死循环,直到插入成功
        for (Node<K,V>[] tab = table;;) {
            Node<K,V> f; int n, i, fh;
            //如果table为空的话，初始化table
            if (tab == null || (n = tab.length) == 0)
                tab = initTable();
            // 根据hash值计算出在table里面的位置 
            else if ((f = tabAt(tab, i = (n - 1) & hash)) == null) {
                // 如果这个位置没有值，利用CAS操作直接存储在该位置，不需要加锁
                if (casTabAt(tab, i, null,
                             new Node<K,V>(hash, key, value, null)))
                    break;             // no lock when adding to empty bin
            }
            // 检查table[i]的节点的hash是否等于MOVED
            // 如果等于，则检测到正在扩容，则帮助其扩容
            else if ((fh = f.hash) == MOVED)
                tab = helpTransfer(tab, f);//帮助扩容
            else {
                V oldVal = null;
                //结点上锁  这里的结点可以理解为hash值相同组成的链表的头结点
                synchronized (f) {//上锁，（Hash值相同的头结点）
                    if (tabAt(tab, i) == f) {
                        if (fh >= 0) {//说明这个节点是一个链表的节点 不是树的节点
                            binCount = 1;
                            //在这里遍历链表所有的结点
                            for (Node<K,V> e = f;; ++binCount) {
                                K ek;
                                //如果hash值和key值相同  则修改对应结点的value值
                                if (e.hash == hash &&
                                    ((ek = e.key) == key ||
                                     (ek != null && key.equals(ek)))) {
                                    oldVal = e.val;
                                    if (!onlyIfAbsent)
                                        e.val = value;
                                    break;
                                }
                                Node<K,V> pred = e;
                                //插入在链表尾部
                                if ((e = e.next) == null) {
                                    pred.next = new Node<K,V>(hash, key,
                                                              value, null);
                                    break;
                                }
                            }
                        }
                        //如果这个节点是树节点，就按照树的方式插入值
                        else if (f instanceof TreeBin) {
                            Node<K,V> p;
                            binCount = 2;
                            if ((p = ((TreeBin<K,V>)f).putTreeVal(hash, key,
                                                           value)) != null) {
                                oldVal = p.val;
                                if (!onlyIfAbsent)
                                    p.val = value;
                            }
                        }
                    }
                }
                //插入成功后，如果插入的是链表节点，则要判断下该桶位是否要转化为树
                if (binCount != 0) {
                    //如果链表长度已经达到临界值8 就需要把链表转换为树结构
                    if (binCount >= TREEIFY_THRESHOLD)
                        treeifyBin(tab, i);
                    if (oldVal != null)
                        return oldVal;
                    break;
                }
            }
        }
        //将当前ConcurrentHashMap的元素数量+1
        addCount(1L, binCount);
        return null;
    }
```



### 扩容

- NewTable
- ***利用CAS方法更新这个扩容阈值**，在这里面sizectl值减一，说明新加入一个线程参与到扩容操作*
- *//节点（*Hash值相同的头结点*）上锁，深拷贝操作*（*//在table的i位置上插入forwardNode节点  表示已经处理过该节点*）



### get（不加锁）

- 首先计算hash值，定位到该table索引位置，如果是首节点符合就返回
- 如果遇到扩容的时候，会调用标志正在扩容节点ForwardingNode的find方法，查找该节点，匹配就返回
- 以上都不符合的话，就往下遍历节点，匹配就返回，否则最后就返回null

get没有加锁的话，ConcurrentHashMap**是如何保证读到的数据不是脏数据的呢？**

##### volatile！

Java提供了volatile关键字来保证可见性、有序性。但不保证原子性。