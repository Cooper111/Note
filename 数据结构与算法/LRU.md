# 搞懂LRU

3月11号结束了杭州某公司的三面，第二天告知被拒了，不太意外。在面试中面试官有问我

- LRU概念，即算法原本的名字和意思（我只记得LRU缓存淘汰）
- LRU的实现（我说了LRU的运行机制，和`LinkedList`实现方式）
- LRU的应用（我说不知道···）

而我一问三不知，才觉学的囫囵吞枣。现在我就按照面试的顺序，好好梳理下LRU相关的知识点。



# LRU概念

LRU即 `Least Rencetly Used`（最近最少使用）缓存替换策略。在任何LRU算法中，它必定有以下两个策略组成：

- **退化策略**：根据访问情况，对节点按热度进行排序（hot->cold），以便决定哪些节点是热节点（hot）的，哪些节点是冷节点（cold）的。这个退化的策略，一般按以下两种方式去处理：

  - 非集中式

    即每命中一次就进行退化操作

    ```
    非集中式的退化操作，往往由双向链表的方式去实现。每次命中之后就移动命中节点在链表中的位置。（位置靠前的就是hot的数据）。当然，复杂的策略中，有用queue数组进行hot分级等。
    ```

  - 集中式

    定期去进行退化操作

    ```
    在集中式的退化操作，常用的策略是：每次命中之后，记录一个时间戳、定时器时间点等等参数。由一个线程去扫描，定期清除老数据。
    ```

- **清除策略**：即去掉那些cold的数据

  - 替换：这个在操作系统缓存中应该是一个常用的做法
  - 删除：删除掉数据，以腾出空间放新的数据。（因为内存是有限的）



# LRU的实现

​        LRU缓存的运行机制，接触过的都估计熟悉的，主要就是命中后移到头结点、删除尾节点两部分。按照这个思路，是难免问到如何去实现的，于是接下来总结下如何实现，详细可见[此参考链接](<https://www.cnblogs.com/lzrabbit/p/3734850.html#f3>)

​        Java里面实现LRU缓存通常**有两种选择**，一种是**使用`LinkedHashMap`，**一种是**使用`链表+HashMap`。**

#### ①使用`LinkedHashMap`

​        用`LinkedHashMap`的每个节点做一个双向链表。每次访问这个节点，就把该节点移动到双向链表的头部。满了以后，就从链表的尾部删除（`LinkedHashMap`有一个判断是否删除最老数据的方法，默认是返回false，即不删除数据）。所以`LinkedHashMap`自身已经实现了顺序存储，默认情况下是按照元素的添加顺序存储，也可以启用按照访问顺序存储。



使用`LinkedHashMap`实现LRU缓存的方法就是对`LinkedHashMap`实现简单的扩展，扩展方式有两种

- `inheritance`（继承）

  ```java
  public class LRUCache2<K, V> extends LinkedHashMap<K, V>
  //1.构造函数里super构造父类
  //2.重写方法removeEldestEntry，ToString
  ```

- `delegation`（代理）

  `delegation方式`实现更加优雅一些，但是由于没有实现Map接口，所以线程同步就需要自己搞定了

  ```java
  public class LRUCache3<K, V> {
      LinkedHashMap<K, V> map;
  //1.构造函数里map构造参数
  //2.map构造时重写方法removeEldestEntry
      
  public synchronized void put(K key, V value) {//自己搞定线程同步
          map.put(key, value);
      }
  ```



如何使用`LinkedHashMap`已实现的LRU机制呢？

- `LinkedHashMap`构造函数1：参数`accessOrder`为**true**时，即会**按照访问顺序排序，最近访问的放在最前，最早访问的放在后面**

- `LinkedHashMap`构造函数2：容量 = `CatchSize` /  装载因子，转为代码：

  ```java
  LinkedHashMap<String, String>((int) Math.ceil(cacheSize / 0.75f) + 1, 0.75f, true)
  ```

- `LinkedHashMap`重写方法：

  ```java
  	@Override
      protected boolean removeEldestEntry(Map.Entry eldest) {
          return size() > MAX_CACHE_SIZE;//这里的MAX_CACHE_SIZE，是cacheSize ，不是cacheSize / 0.75f
      }
  ```

  

#### ②双向链表+`HashMap`实现

​        面试的时候LRU相关的我已经忘的差不多了，说实现方法临时想了一个用`LinkedList`实现，然后说了直接拿链表实现的思路。那么问题来了，在插入时一定会先查找特定节点是否存在，只能进行遍历找节点的操作，时间复杂度到了O(n)级别，面试官听完后直言学的不深，工业中LRU性能不能这么低。

​	那么如何使得查找的时间复杂度为`O(1)`呢？所以得使用Map系列，直接get取出对应节点，下面来介绍`HashMap+双向链表`的实现。

- 实现**将特定节点移到队头**

  ```java
  private void moveToFirst(Entry entry) {
          if (entry == first) return;
          if (entry.pre != null) entry.pre.next = entry.next;
          if (entry.next != null) entry.next.pre = entry.pre;
          if (entry == last) last = last.pre;
  
          if (first == null || last == null) {
              first = last = entry;
              return;
          }
  
          entry.next = first;
          first.pre = entry;
          first = entry;
          entry.pre = null;
      }
  ```

- 实现**将尾部节点删除**

  ```java
  if (hashMap.size() >= MAX_CACHE_SIZE) {
      hashMap.remove(last.key);//删除HashMap中的引用对象
      removeLast();//删除链表中真正的节点
  }
  ```

- 实现插入函数：put（这里对应的运行机制写，应该很熟）

  ```java
  	public void put(K key, V value) {
          Entry entry = getEntry(key);
          if (entry == null) {
              if (hashMap.size() >= MAX_CACHE_SIZE) {//如果超出容量
                  hashMap.remove(last.key);
                  removeLast();
              }
              entry = new Entry();
              entry.key = key;
          }
          entry.value = value;
          moveToFirst(entry);
          hashMap.put(key, entry);
      }
  ```

- 实现获取函数：get（这里对应的运行机制写，应该很熟）

  ```java
  public V get(K key) {
          Entry<K, V> entry = getEntry(key);
          if (entry == null) return null;
          moveToFirst(entry);
          return entry.value;
      }
  ```



#### 值得注意的点：

- 如果`HashMap`的`Value`不是基本类型，那么**只存放引用对象**而不会存放本身，所以

  ```java
  Node node = hashmap.get('node');
  //System.out.println(node.getVal());
  node.setVal('xxx');
  //System.out.println(hashmap.get('node').getVal())
  ```

  此时打印出来的node是修改后的值

- 双向链表+`HashMap`实现方式，构造的`HashMap`和链表最好**使用泛型**





# LRU的应用

- `LInkedHashMap`

  > 学习方式：学习了[博文](<https://www.cnblogs.com/ganchuanpu/p/8908093.html>)+查看源码

- 操作系统页面置换算法

- `Redis`中LRU的实现

- `mybatis`源码`LruCache`的实现（`LinkedHashMap`，`accessOrder`设置为true）



后面是各个应用的源码分析，较长



## 0x01`LinkedHashMap`

 **一、概述**

 `LinkedHashMap`是Map接口的哈希表和链接列表实现，具有可预知的迭代顺序。

 `LinkedHashMap`实现与`HashMap`的不同之处在于，后者维护着一个**运行于所有条目的双重链接列表**。此链接列表定义了迭代顺序，该迭代顺序可以是插入顺序或者是访问顺序，默认是插入顺序，可将构造参数accessOrder设置为true改为访问顺序。

 `LinkedHashMap`线程不安全



**概述**：**`LinkedHashMap`是`HashMap`的子类。只是重写了其中维持顺序的列表的相关的操作**

**对于每个节点**都增加了用于迭代顺序的before和after节点，形成维持顺序的双向链表

**在新建节点和插入节点后**加入`AfterNodeAcess`方法，将节点插入迭代顺序链表尾

**在插入函数结尾时**加入`afterNodeInsertion`方法判断是否要删除最早节点，将表头节点删除

**二、实现**

见此：<https://blog.csdn.net/weixin_39723544/article/details/83269282>

下面是我的提要：

- LinkedHashMap的Entry结构

  > `LinkedHashMap的entry在hashmap的基础上多了before和after两个地址，依次来维护顺序。这点和LinkedList一致.`

- LinkedHashMap字段

  ```
  链表的头结点、尾结点、还有迭代顺序标志accessOrder
  ```

- `LinkedHashMap`构造方法

  ```java
  //四种构造方法对应父类四种，调用super传参构造，多了个accessOrder
  	public LinkedHashMap() {
          super();
          accessOrder = false; // 是否开启LRU缓冲
      }
  
      public LinkedHashMap(int initialCapacity) {
          super(initialCapacity);
          accessOrder = false; // 是否开启LRU缓冲
      }
  
      public LinkedHashMap(int initialCapacity, float loadFactor) {
          super(initialCapacity, loadFactor);
          accessOrder = false; // 是否开启LRU缓冲
      }
  
      public LinkedHashMap(Map<? extends K, ? extends V> m) {
          super();
          accessOrder = false; // 是否开启LRU缓冲
          putMapEntries(m, false);
      }
  ```

- **核心函数：`AfterNodeAccess`**

- ##### 存储：putVal方法

  与HashMap的PutVal方法基本一样,区别在于：

  - 新建节点时，新节点的迭代顺序链表尾部添加
  - 插入完毕后，插入节点的迭代顺序链表尾部添加
  - 方法执行完毕后，是否要删除最早元素

  ```java
  与HashMap的PutVal方法基本一样，
  不同：
  ①newNode
  Node<K,V> newNode(int hash, K key, V value, Node<K,V> e) {
          LinkedHashMap.Entry<K,V> p =
              new LinkedHashMap.Entry<K,V>(hash, key, value, e);
          // 作用：将新建的节点添加到维护的双向链表上去
          // 方式：往链表的尾部进行添加
          linkNodeLast(p);
          return p;
      }
      
  	// link at the end of list
  	//newNode中要把新建的节点插入到维护迭代顺序的双向链表尾部
      private void linkNodeLast(LinkedHashMap.Entry<K,V> p) {
          LinkedHashMap.Entry<K,V> last = tail;
         // p为新的需要追加的结点
          tail = p;
          // 如果last为null.则表示现在链表为空。新new出来的p元素就是链表的头结点
          if (last == null)
              head = p;
          else {
          // 否则就是链表中已存在结点的情况：往尾部添加即可
          	// 把新追加p的结点的前驱结点设置之前的尾部结点
          	// 把之前的尾部结点的后驱结点设为新追加的p结点
              p.before = last;
              last.after = p;
          }
  
  
  ②插入后对于插入的Node e，执行
  afterNodeAccess(e);
  //把Entry e插入到维护迭代顺序的双向链表尾部
  	// 作用：将结点元素移到链表的最后位置
      void afterNodeAccess(Node<K,V> e) { // move node to last
          LinkedHashMap.Entry<K,V> last;
          // 根据LRU原则，只有当元素不在尾部的时候，才需要进行以为操作
          // 需要move的e结点有以下几种情况：
          // ①e结点没有前驱结点，有后驱结点情况下：将头结点设为e的后驱结点，然后把e的后驱结点的前驱结点“连接”到e的前驱结点的前驱结点上（这时候为null）。最后把p设为tail结点，置于尾部。
          // ②e结点没有后驱结点，有前驱结点情况下：将e结点的前驱结点和后驱结点（null）直接相连；并把last结点设为e的前驱结点。最后将p(即e)结点设为tail结点，置于尾部。
          // ③链表为空情况下：直接将当前结点p，置为链表的head结点。
          // ④e结点既有前驱结点，也有后驱结点情况下：将p结点的前驱结点和后驱结点直接相连。然后把p结点方法最后一个结点。同时将tail结点赋值为p.
          if (accessOrder && (last = tail) != e) {
              LinkedHashMap.Entry<K,V> p =
                  (LinkedHashMap.Entry<K,V>)e, b = p.before, a = p.after;
              p.after = null;
              if (b == null)
                  head = a;
              else
                  b.after = a;
              if (a != null)
                  a.before = b;
              else
                  last = b;
              if (last == null)
                  head = p;
              else {
                  p.before = last;
                  last.after = p;
              }
              tail = p;
              ++modCount;
          }
      }        
          
          
  ③在putVal末尾
  // 由LinkedHashMap的实现，并调用
  // 作用：在执行一次插入操作都会执行的操作
  // 主要就是对LRU算法的支持。
  // 是否移动最早的元素，删除策略
  afterNodeInsertion(evict);
          
      void afterNodeInsertion(boolean evict) { // possibly remove eldest
          LinkedHashMap.Entry<K,V> first;
          if (evict && (first = head) != null && removeEldestEntry(first)) {
              //removeEldestEntry这里一直返回false，如果LRU得改写返回true
              K key = first.key;
              removeNode(hash(key), key, null, false, true);
          }
      }
  ```

- **获取：get函数**

  ```java
      // 说明：调用HashMap的get逻辑；如果获取值为null，则直接返回null。否则判断是否开启了LRU,如果开启的话，就把最近访问的元素放到链表的尾部。最后返回需要获取元素的值。
      public V get(Object key) {
          Node<K,V> e;
          if ((e = getNode(hash(key), key)) == null)
              return null;
          if (accessOrder)
              afterNodeAccess(e);
          return e.value;
      }
  ```

- **删除：`removeNode`**

  删除基本与`HashMap`相同，但是删除完后对于删除节点，也要在迭代顺序的链表里去除

  ```java
  afterNodeRemoval(node);
  // 在这里就是：删除节点，删除其关联的维护顺序的双向列表的操作
  void afterNodeRemoval(Node<K,V> e) { // unlink
          LinkedHashMap.Entry<K,V> p =
              (LinkedHashMap.Entry<K,V>)e, b = p.before, a = p.after;
          // 此时p节点已删除，将p的前驱和后驱结点均置为null
          p.before = p.after = null;
          // 目的：将a,b结点进行相连操作
          if (b == null)
              head = a;
          else
              b.after = a;
          if (a == null)
              tail = b;
          else
              a.before = b;
      }
  ```

-   **iterator**从head节点指向tail节点



  

## `Redis`的LRU实现

**概述：Redis采用了一个近似的做法，使用了一个全局的LRU时钟，然后每个key都会有一个lru时间，记录上次访问的时间。redis的`estimateObjectIdleTime`函数计算出对应key没有访问的时长，然后执行相关淘汰策略：**

**选取`server.maxmemory_samples`个数目的key，进行比较，然后淘汰最近最久没有访问的key**



如果按照HashMap和双向链表实现，需要额外的存储存放 next 和 prev 指针，牺牲比较大的存储空间，显然是不划算的。所以Redis采用了一个近似的做法，就是随机取出若干个key，然后按照访问时间排序后，淘汰掉最不经常使用的，具体分析如下：

为了支持LRU，Redis 2.8.19中使用了一个全局的LRU时钟，`server.lruclock`，定义如下，

```cpp
#define REDIS_LRU_BITS 24
unsigned lruclock:REDIS_LRU_BITS; /* Clock for LRU eviction */
```

默认的LRU时钟的分辨率是1秒，可以通过改变`REDIS_LRU_CLOCK_RESOLUTION`宏的值来改变，Redis会在`serverCron()`中调用`updateLRUClock`定期的更新LRU时钟，更新的频率和hz参数有关，默认为`100ms`一次，如下，

```cpp
#define REDIS_LRU_CLOCK_MAX ((1<<REDIS_LRU_BITS)-1) /* Max value of obj->lru */
#define REDIS_LRU_CLOCK_RESOLUTION 1 /* LRU clock resolution in seconds */

void updateLRUClock(void) {
    server.lruclock = (server.unixtime / REDIS_LRU_CLOCK_RESOLUTION) &
                                                REDIS_LRU_CLOCK_MAX;
}
```

`server.unixtime`是系统当前的unix时间戳，当 lruclock 的值超出REDIS_LRU_CLOCK_MAX时，会从头开始计算，所以在计算一个key的最长没有访问时间时，可能key本身保存的lru访问时间会比当前的lrulock还要大，这个时候需要计算额外时间，如下，

**`estimateObjectIdleTime`这个方法就是返回没有被访问的时长**

```cpp
/* Given an object returns the min number of seconds the object was never
 * requested, using an approximated LRU algorithm. */
unsigned long estimateObjectIdleTime(robj *o) {
    if (server.lruclock >= o->lru) {
        return (server.lruclock - o->lru) * REDIS_LRU_CLOCK_RESOLUTION;
    } else {
        return ((REDIS_LRU_CLOCK_MAX - o->lru) + server.lruclock) *
                    REDIS_LRU_CLOCK_RESOLUTION;
    }
}
```

Redis支持和LRU相关淘汰策略包括，

- `volatile-lru` 设置了过期时间的key参与近似的lru淘汰策略
- `allkeys-lru` 所有的key均参与近似的lru淘汰策略

当进行LRU淘汰时，Redis按如下方式进行的，

```cpp
......
            /* volatile-lru and allkeys-lru policy */
            else if (server.maxmemory_policy == REDIS_MAXMEMORY_ALLKEYS_LRU ||
                server.maxmemory_policy == REDIS_MAXMEMORY_VOLATILE_LRU)
            {
                for (k = 0; k < server.maxmemory_samples; k++) {
                    sds thiskey;
                    long thisval;
                    robj *o;

                    de = dictGetRandomKey(dict);
                    thiskey = dictGetKey(de);
                    /* When policy is volatile-lru we need an additional lookup
                     * to locate the real key, as dict is set to db->expires. */
                    if (server.maxmemory_policy == REDIS_MAXMEMORY_VOLATILE_LRU)
                        de = dictFind(db->dict, thiskey);
                    o = dictGetVal(de);
                    thisval = estimateObjectIdleTime(o);

                    /* Higher idle time is better candidate for deletion */
                    if (bestkey == NULL || thisval > bestval) {
                        bestkey = thiskey;
                        bestval = thisval;
                    }
                }
            }
            ......
```

Redis会基于`server.maxmemory_samples`配置选取固定数目的key，然后比较它们的lru访问时间，然后淘汰最近最久没有访问的key，maxmemory_samples的值越大，Redis的近似LRU算法就越接近于严格LRU算法，但是相应消耗也变高，对性能有一定影响，样本值默认为5。





# 参考文章

- **Jdk1.8源码**

- [LRU缓存实现(Java)](https://www.cnblogs.com/lzrabbit/p/3734850.html)
- [LRU原理和Redis实现——一个今日头条的面试题](<https://zhuanlan.zhihu.com/p/34133067>)
- [（转载）ConcurrentHaspLRUHashMap实现初探](<https://blog.csdn.net/njchenyi/article/details/8046914>)
- [【这个源码不太对】LinkedHashMap的实现原理](https://www.cnblogs.com/ganchuanpu/p/8908093.html)
- [Map之LinkedHashMap源码实现](<https://blog.csdn.net/weixin_39723544/article/details/83269282>)

