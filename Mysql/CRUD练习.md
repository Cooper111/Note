# DELETE 和  TRUNCATE

链接：

https://www.nowcoder.com/questionTerminal/994ed81b7ae345ac9c04e84608ba3e6a

来源：牛客网

删除表的语句为：`DROP TABLE table_name`; 

  而`DELETE`和`TRUNCATE` TABLE都是删除表中的数据的语句，它们的不同之处在于： 

  1、TRUNCATE TABLE比DELETE的速度快； 

  2、TRUNCATE TABLE 是删除表的所有行，而DELETE是删除表的一行或者多行（除非DELETE不带WHERE语句）； 

  3、在删除时如果遇到任何一行违反约束（主要是外键约束），TRUNCATE   TABLE仍然删除，只是表的结构及其列、约束、索引等保持不变，但DELETE是直接返回错误； 

  4、对于被外键约束的表，不能使用TRUNCATE   TABLE，而应该使用不带WHERE语句的DELETE语句。

5、如果想保留标识计数值，要用DELETE，因为TRUNCATE   TABLE会对新行标志符列搜用的计数值重置为该列的种子。

```
	\1. TRUNCATE TABLE 在功能上与不带 Where     子句的 Delete 语句相同：二者均删除表中的全部行。但 TRUNCATE TABLE 比 Delete 速度快，且使用的系统和事务日志资源少。 

   \2. Delete     语句每次删除一行，并在事务日志中为所删除的每行记录一项。TRUNCATE TABLE 通过释放存储表数据所用的数据页来删除数据，并且只在事务日志中记录页的释放。  

   \3. TRUNCATE TABLE     删除表中的所有行，但表结构及其列、约束、索引等保持不变。新行标识所用的计数值重置为该列的种子。如果想保留标识计数值，请改用 Delete。 

   \4. 速度，一般来说: drop> truncate > delete 

   \5.    想保留表而将所有数据删除. 如果和事务无关,用truncate即可. 如果和事务有关,或者想触发trigger,还是用delete.    
```





- [题目：统计男女生数目](<https://blog.csdn.net/wangjinsu7/article/details/52257150>)
  分组+聚合

- [牛客网数据库SQL实战详细剖析(41-50)](<https://mp.weixin.qq.com/s?__biz=MzUyOTAwMzI4NA==&mid=2247488356&idx=2&sn=d4059c1166924d76c25311a219b2b2fa&chksm=fa66f51fcd117c09ce744ad9b98a5f04467a485eb6d0f4e974a0ed11277be48ae60d1d7ab803&mpshare=1&scene=23&srcid=&sharer_sharetime=1587177166044&sharer_shareid=e6d90aec84add5cf004cb1ab6979727c#rd>)

  SQL实战其他题目也可参见文章！



# 连接查询、子查询

- [连接查询？子查询？看这篇文章就行了](<https://mp.weixin.qq.com/s?__biz=MzIzNzE1ODExNw==&mid=2247483896&idx=1&sn=463407aa2edd835b1f45e27a401b7fe2&chksm=e8cdaa72dfba23647c3a2764f4f3ce1c9c600bab6650375a206ec755723cda4a51fb7bfdb691&mpshare=1&scene=23&srcid=&sharer_sharetime=1587231942706&sharer_shareid=e6d90aec84add5cf004cb1ab6979727c#rd>)

- [为什么代码规范要求SQL语句不要过多的join？](<https://mp.weixin.qq.com/s?__biz=MzAwMjk5Mjk3Mw==&mid=2247488645&idx=4&sn=a061ee70875b92e83cf0503cc39eda96&chksm=9ac0aaa7adb723b192bbe3a5586010e2a266ad47f16be13352dc94c57a5eaba6d09d965862cb&mpshare=1&scene=23&srcid=&sharer_sharetime=1586937624774&sharer_shareid=e6d90aec84add5cf004cb1ab6979727c#rd>)





# Where和Having区别

- WHERE 子句作用于**表和视图**，HAVING 子句**作用于组**

  通俗的讲，where语句作用在group by之前，having语句作用在group by之后

- WHERE 子句**不能包含聚集函数**； 因为试图用聚集函数判断那些行输入给聚集运算是没有意义的。 相反，HAVING 子句**总是包含聚集函数**。



<https://www.cnblogs.com/lmaster/p/6373045.html>



## Having和Group by

```sql
FROM 
WHERE （先过滤单表／视图／结果集，再JOIN）
GROUP BY
HAVING （WHERE过滤的是行，HAVING过滤的是组，所以在GROUP之后）
ORDER BY
```

- <https://www.cnblogs.com/948046hs/p/9122231.html>
- <https://www.cnblogs.com/lmaster/p/6373045.html>





# 场景

- update设置的参数来自从其他表select出的结果

  参考：<https://www.cnblogs.com/sttchengfei/p/12660820.html>

  ```java
  update A inner join(select id,name from B) c on A.id = c.id set A.name = c.name;
  ```

- [mysql 一张表的数据插入另一张表的sql语句](https://www.cnblogs.com/guchunchao/p/10700467.html)

  - 表的结构完全一样

    ```mysql
    insert into 表1
    　　select * from 表2
    ```

  - 表的结构不一样

    ```mysql
    insert into 表(列名1,列名2,列名3)
    　　select 列1,列2,列3 from 表2
    ```

    



# 练习题

- [牛客网数据库SQL实战详细剖析(41-50)](<https://mp.weixin.qq.com/s?__biz=MzUyOTAwMzI4NA==&mid=2247488356&idx=2&sn=d4059c1166924d76c25311a219b2b2fa&chksm=fa66f51fcd117c09ce744ad9b98a5f04467a485eb6d0f4e974a0ed11277be48ae60d1d7ab803&mpshare=1&scene=23&srcid=&sharer_sharetime=1587177166044&sharer_shareid=e6d90aec84add5cf004cb1ab6979727c#rd>)

- 真实题目：

   ```mysql
  kaiwen980826 13:57:53
  员工部门表（注：可以增加表,解决如下问题）
  CREATE TABLE emp_dept
  (
      dept_no            STRING   COMMENT '部门编号',
      dept_name          STRING   COMMENT '部门名称',
      super_work_no      STRING   COMMENT '部门主管',
      super_dept_no      STRING   COMMENT '上级部门编号',
      super_dept_name    STRING   COMMENT '上级部门名称',
      dept_type          STRING   COMMENT '部门类型:root/dept ,root 表示根节点'
     )
  
  
  1.计算负责部门总数最多的10个主管及负责部门数。
  
  
  
  2.根据一个部门编号，查询出所有下级部门。
  # 用with实现迭代计算
  
  
  
  3.计算主管负责顶点部门数（比如：张三负责部门A,B,C；其中A的上级部门B，那么张三管理的顶点是：B、C）。
  
  
  ```

  