# Statement

参考链接：<https://blog.csdn.net/qq_30982323/article/details/80625317>



概述：

preparedStatement就是预编译，对应Mybatis的#{}

Statement就是即时编译，对应Mybatis的${}



PreparedStatement是**预编译**得,PreparedStatement**支持批处理**

PreparedStatement对象允许数据库预编译SQL语句，这样在随后的运行中可以节省时间并增加代码的可读性。

然而，除了缓冲的问题之外，至少还有一个更好的原因使我们在企业应用程序中更喜欢使用PreparedStatement对象,那就是**安全性**。传递给PreparedStatement对象的参数可以被**强制进行类型转换**，使开发人员可以确保在插入或查询数据时与底层的数据库格式匹配。



# JDBC

- [JDBC步骤（以查询为例）](<https://blog.csdn.net/hbscxf/article/details/93754093>)



