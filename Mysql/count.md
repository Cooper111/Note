- [MySQL：count(*),count(1),count(col)效率比较](<https://blog.csdn.net/zyz_1024/article/details/87940315>)

- [关于mysql中的count()函数](https://www.cnblogs.com/-flq/p/10302965.html)

- 【好】[MySQL学习笔记：count(1)、count(*)、count（字段）的区别](https://www.cnblogs.com/hider/p/11726690.html)

  ```
  COUNT函数的用法，主要用于统计表行数。主要用法有COUNT(*)、COUNT(字段)和COUNT(1)。
  
  因为COUNT(*)是SQL92定义的标准统计行数的语法，所以MySQL对他进行了很多优化，MyISAM中会直接把表的总行数单独记录下来供COUNT(*)查询，而InnoDB则会在扫表的时候选择最小的索引来降低成本。当然，这些优化的前提都是没有进行where和group的条件查询。
  
  在InnoDB中COUNT(*)和COUNT(1)实现上没有区别，而且效率一样，但是COUNT(字段)需要进行字段的非NULL判断，所以效率会低一些。
  
  因为COUNT(*)是SQL92定义的标准统计行数的语法，并且效率高，所以请直接使用COUNT(*)查询表的行数！
  ```

- <https://blog.csdn.net/u011165335/article/details/80298404>

