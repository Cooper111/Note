# 数据类型

- <https://blog.csdn.net/qq_44750696/article/details/100057282>









### String和varchar区别

- https://blog.csdn.net/qq_44750696/article/details/100057282

- <https://zhidao.baidu.com/question/524781714532338445.html>

```
String在java里无长度限制，varchar在oracle里是有长度限制的；
String修饰的字符串不能被修改，varchar修饰的字符串能被修改；
String是以大写字母开始的，varchar不是以大写字母开始的；
String是一种固定长度的类型，varchar则是一种可变长度的类型；

java这门语言将String类型的内部数据结构是以一个对象的操作来考虑的，java这门语言将varchar类型的内部数据结构没有以一个对象的操作来考虑的；

String在mysql中为不可变长度的字符串，varchar在mysql中为可变长度的字符串；

相同存储量的话String比varchar更占空间。
```


VARCHAR(M)是一种比CHAR更加灵活的数据类型，同样用于表示字符数据，但是VARCHAR可以保存可变长度的字符串。其中M代表该数据类型所允许保存的字符串的最大长度，只要长度小于该最大值的字符串都可以被保存在该数据类型中。

因此，对于那些难以估计确切长度的数据对象来说，使用VARCHAR数据类型更加明智。MySQL4.1以前,VARCHAR数据类型所支持的最大长度255,5.0以上版本支持65535字节长度,utf8编码下最多支持21843个字符(不为空)。



### datetime和timestamp区别

- 【好】<https://www.cnblogs.com/mxwz/p/7520309.html>

- https://blog.csdn.net/wangjun5159/article/details/48010563
- https://blog.csdn.net/qq_44750696/article/details/100057282

**datetime**： 

1、保存格式为YYYYMMDDHHMMSS（年月日时分秒）的整数，所以，它与时区无关，存入的是什么值就是什么值，不会根据当前时区进行转换。

2、从mysql 5.6.4中，可以存储小数片段，最多到小数点后6位，显示时格式为 yyyy-MM-dd HH:mm:ss[.222222]

      mysql5.5中，没有小数片段，精确到秒。所以，我再从5.6版本迁移到5.5版本时，因为生成的sql中datetime(6),所以无法导入数据库。

3、存储范围：从1000-01-01 00:00:00 到'9999-12-31 23:59:59'

4、长度，8个字节，datetime(n),n不是存储长度，而是显示的小数位数，即使小数位数是0，存储是也是存储的6位小数，仅仅显示0位而已

5、显示时，显示日期和时间



**timestamp**：

1、存入的是自1970-01-01午夜(格林尼治标准时间)以来的秒数，它和unix时间戳相同。所以它与时区有关，查询时转为相应的时区时间。比如，存储的是1970-01-01 00:00:00，客户端是北京，那么就加8个时区的小时1970-01-01 08:00:00。

2、有小数片段，至少从5.5就开始有

3、存储范围：'1970-01-01 00:00:01' UTC to '2038-01-19 03:14:07' 

4、可以当做时间戳使用，在更新时，自动更新，这一列只能由系统自动更新，不能由sql更新，这个在乐观锁时有广泛的应用

6、长度，4字节，因为存储长度的原因，决定了它支持的范围的比datetime的要小

7、显示时，显示日期和时间



**date**

date，时分秒都存储了，但只显示日期。对应Java中的java.sql.Date



#### 从MySQL 5.6.5开始，Automatic Initialization and Updating同时适用于TIMESTAMP和DATETIME，且不限制数量

create table字段为timestamp，

```sql
`hiredate` timestamp NOT NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP
```

**自动初始化和更新**