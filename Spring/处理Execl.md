# 处理Execl数据

这里简单的罗列下我用的工具类

- 导入Execl文件

  ```java
  /**
   *
   * 	读取文件
   *
   */
  public static List<Student> readExcel(String filePath) {
      //创建集合存放数据
      List<Student> list = new ArrayList<Student>();
      POIFSFileSystem fs = null;
      try {
          fs=new POIFSFileSystem(new FileInputStream(filePath));
          HSSFWorkbook wb = new HSSFWorkbook(fs);
          //读取第第张表格
          HSSFSheet sheet = wb.getSheetAt(0);
          //这里sheet.getLastRowNum() = 2
          //获取行
          for(int i= 1;i<=sheet.getLastRowNum();i++) {
              HSSFRow row = sheet.getRow(i);
              String[] str = new String[row.getLastCellNum()];
              //这里row.getLastCellNum()为4
              for (int j=0;j<row.getLastCellNum();j++) {
                  str[j]=row.getCell(j).toString();
              }
              Student user = new Student();
              //user.setId(Integer.valueOf(str[0]));
              user.setName(str[0]);
              user.setSex(str[1]);
              //user.setBirthDay(PoiUtils.stringToDate(str[3]));
              user.setAge((int)Math.ceil(Double.valueOf(str[2])));
              user.setClazz_id((int)Math.ceil(Double.valueOf(str[3])));
              list.add(user);
              // System.out.println("添加了第： "+i+" 个对象");
          }
      } catch (Exception e) {
          // TODO Auto-generated catch block
          e.printStackTrace();
      } finally {
          try {
              fs.close();
          } catch (IOException e) {
              // TODO Auto-generated catch block
              e.printStackTrace();
          }
      }
      System.out.println("返回了一个存放了 "+list.size()+" 个Student对象的集合");
  
      return list;
  }
  ```

- 导出Execl文件

  ```java
      /**
       * 将对象中封装的数据存放到HSSFWorkbook对象中
       *
       * @param list
       * @return workbook
       */
      public static HSSFWorkbook getWorkBook(List<Student> list) {
  //        for (Student user : list) {
  //            System.out.println(user);
  //        }
          // 创建工作表对象
          HSSFWorkbook workbook = new HSSFWorkbook();
          // 在对象中创建一个表
          HSSFSheet sheet = workbook.createSheet("sheet0");
          // 获取表头
          String[] header = PoiUtils.getTabHeader();
          for (int i = 0; i < list.size() + 1; i++) {
              // 创建第i行
              HSSFRow row = sheet.createRow(i);
              // 设置表头
              if (i == 0) {
                  for (int j = 0; j < header.length; j++) {
                      row.createCell(j).setCellValue(header[j]);
                  }
              }
              // 将内容写入excel表格
              else {
                  // 获取对象属性值
                  String[] userMsg = getStudentMsg(list.get(i - 1));
                  for (int j = 0; j < userMsg.length; j++) {
                      row.createCell(j).setCellValue(userMsg[j]);
                  }
              }
  
          }
          return workbook;
      }
  	//将对象属性值存入到string数组中
      public static String[] getStudentMsg(Student student) {
          String[] studentMsg = {
                  student.getName() != null ? student.getName().toString() : null,
                  student.getSex() != null ? student.getSex().toString() : null,
                  student.getAge() != null ? student.getAge().toString() : null,
                  student.getClazz_id() != null ? student.getClazz_id().toString() : null
          };
          return studentMsg;
      }
      /**
       * 提供表头
       *
       * @return
       */
      public static String[] getTabHeader() {
          //String[] tabHeader = { "用户ＩＤ", "用户名", "密码", "个性签名", "生日", "年龄", "工资" };
          String[] tabHeader = {"Name", "Sex", "Age", "Clazz_id"};
          return tabHeader;
  
      }
  ```

- 导出Execl时还有一些常见的格式转换

  ```java
  /**
       * 将日期类型的数据转成字符串
       *
       * @param date
       * @return date
       */
      public static String dateToString(Date date) {
          SimpleDateFormat sdf = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss");
  
          return sdf.format(date);
  
      }
  
      /**
       * 将字符串类型的数据转成日期
       *
       * @param str
       * @return date
       */
      public static Date stringToDate(String str) {
          SimpleDateFormat sdf = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss");
  
          try {
              return sdf.parse(str);
          } catch (Exception e) {
              // TODO Auto-generated catch block
              throw new RuntimeException("转换失败");
          }
  
      }
  ```

  





# 异常解决

- [Your stream was neither an OLE2 stream, nor an OOXML stream.问题的解决](https://www.cnblogs.com/baobaodong/p/5829735.html)



# 参考

- https://github.com/20Fen/SpringBootExecl

- 上传Execl：<https://blog.csdn.net/weixin_40031468/article/details/98212309>

- 【主要参考】数据导入导出Execl：<https://blog.csdn.net/qq_38701478/article/details/83692394>

- SSM 框架整合POI插件技术导出EXCEL文件：<https://blog.csdn.net/mukvintt/article/details/80657769>

