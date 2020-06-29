# Echarts整合

在前端ajax请求，success后接受值填充至`Echarts`，可以分情况讨论解决方法。

- 只需要数组
- 需要包含多值的字典，值可以是字段，可以是数组



### 只需要数组

比如是柱状图，饼图。

只需要代表category的name数组，和代表数组的value数组。

这种情况不用封装Vo类（也可以构造，就麻烦点），直接**使用`js`，解析对象集合构造数组即可**（当然也可以构造两个数组传进去哈哈）

e.g.   一个柱状图例子

```html
<!DOCTYPE html>
<html lang="en">
<head>
    <meta charset="UTF-8">
    <title>Title</title>
    <script src="https://cdn.bootcss.com/jquery/3.1.1/jquery.min.js"></script>
    <script src="js/echarts.min.js"></script>
</head>
<body>
<!-- 为ECharts准备一个具备大小（宽高）的Dom -->
<div id="main" style="width: 600px;height:400px;"></div>
<script type="text/javascript">
    // 基于准备好的dom，初始化echarts实例
    var myChart = echarts.init(document.getElementById('main'));

    // 指定图表的配置项和数据
    myChart.setOption({
        title: {
            text: 'ECharts 入门示例'
        },
        tooltip: {},
        legend: {
            data:['销量']
        },
        xAxis: {
            data: []
        },
        yAxis: {},
        series: [{
            name: '销量',
            type: 'bar',
            data: []
        }]
    });

    myChart.showLoading();

    var names=[];    //类别数组（实际用来盛放X轴坐标值）
    var nums=[];    //销量数组（实际用来盛放Y坐标值）

    $.ajax({
        type : "get",
        async : false,            //异步请求（同步请求将会锁住浏览器，用户其他操作必须等待请求完成才可以执行）
        url : "list",    //请求发送到TestServlet处
        data : {},
        dataType : "json",        //返回数据形式为json
        success : function(result) {
            //请求成功时执行该函数内容，result即为服务器返回的json对象
            var data = result.data;
            if (data) {
                for(var i=0;i<data.length;i++){
                    names.push(data[i].name);    //挨个取出类别并填入类别数组
                }
                for(var i=0;i<data.length;i++){
                    nums.push(data[i].num);    //挨个取出销量并填入销量数组
                }
                myChart.hideLoading();    //隐藏加载动画
                myChart.setOption({        //加载数据图表
                    xAxis: {
                        data: names
                    },
                    series: [{
                        // 根据名字对应到相应的系列
                        name: '销量',
                        data: nums
                    }]
                });

            }

        },
        error : function() {
            //请求失败时执行该函数
            alert("图表请求数据失败!");
            myChart.hideLoading();
        }
    })
</script>
</body>
</html>
```



### 需要包含多值的字典

比如折线图，需要name，value，type等等字段。这时候使用数组不太方便，尤其是value字段也是一个数组时。

```javascript
//这里只是举个例子哈···这属性也太多了
series: [
          {
            name: '预期',
            data: [820, 932, 301, 1434, 1290, 1330, 1320],
            type: 'line',
            // 设置小圆点消失
            // 注意：设置symbol: 'none'以后，拐点不存在了，设置拐点上显示数值无效
            symbol: 'none',
            // 设置折线弧度，取值：0-1之间
            smooth: 0.5,
          },
 
          {
            name: '实际',
            data: [620, 732, 941, 834, 1690, 1030, 920],
            type: 'line',
            // 设置折线上圆点大小
            symbolSize:8,
            itemStyle:{
              normal:{
                // 拐点上显示数值
                label : {
                show: true
                },
                borderColor:'red',  // 拐点边框颜色
                lineStyle:{                 
                  width:5,  // 设置线宽
                  type:'dotted'  //'dotted'虚线 'solid'实线
                }
              }
            }
          },
 
          {
            name: '假设',
            data: [120, 232, 541, 134, 290, 130, 120],
            type: 'line',
            // 设置折线上圆点大小
            symbolSize:10,
            // 设置拐点为实心圆
            symbol:'circle',            
            itemStyle: {
              normal: {
                // 拐点上显示数值
                label : {
                  show: true
                },
                lineStyle:{
                  // 使用rgba设置折线透明度为0，可以视觉上隐藏折线
                  color: 'rgba(0,0,0,0)'
                }
              }
            }
          }
        ],
```

比如下面这种折线图需要的数据，此时可以使用封装Vo类。

e.g.我的一次简单的封装Vo类

这里是将订单对象的`name,num`字段值进行解析，转化为可用于`Echarts`的销量的Vo类

```java
@Service
public class OrderServiceImpl implements OrderService {
    @Resource
    private OrderMapper orderMapper;

    @Override
    public List<OrderVo> weeklyCount() {
        // 这里是统计各个商品在一周的各天内的销量
        List<Order> orders = orderMapper.weeklyCount();
        // 这里可以用CurrentHashMap优化
        HashMap<String, OrderVo> orderKind = new HashMap<>();

        for (Order order: orders) {
            // 如果该商品类不存在
            if (!orderKind.containsKey(order.getName())) {
                // 创建该商品类
                OrderVo temp = new OrderVo(order.getName());
                // 使得商品类存在
                orderKind.put(order.getName(), temp);
            }
            // 处理订单，计算商品类在某日的销量
            orderKind.get(order.getName()).data[order.getDay()-1] += order.getNum();
        }
        //返回Echarts用的Series数组
        //return (List<OrderVo>) orderKind.values();        //Collections硬转List，这样看网上好像是不行的
        return new ArrayList<OrderVo>(orderKind.values());  //将collection转为object数组返回，里面调用了toArray
    }

    // 折线图的返回，代替Series
    public class OrderVo implements Serializable {
        public String name;
        // 这样初始化为0了，这里可以用Atom原子类优化
        public int[] data;
        public String type = "line";
        public String stack = "总量";

        public OrderVo(String name) {
            this.name = name;
            data = new int[7];
        }
    }
}
```

当有ajax请求过来时，返回的数据是这样的：

```shell
C:\Users\Kavin>curl http://localhost:8080/weekly

[{"name":"衬衫"data":[5,8,12,9,25,13,8],"type":"line","stack":"销量"},{"name":"羊毛衫","data":[9,4,2,3,8,15,20],"type":"line","stack":"销量"}]
```

对应的html页面这么写：

```html
<!DOCTYPE html>
<html lang="en">
<head>
    <meta charset="UTF-8">
    <title>周内各商品类销量趋势</title>
    <script src="https://cdn.bootcss.com/jquery/3.1.1/jquery.min.js"></script>
    <script src="js/echarts.min.js"></script>
</head>
<body>
<!-- 为ECharts准备一个具备大小（宽高）的Dom -->
<div id="main" style="width: 600px;height:400px;"></div>
<script type="text/javascript">
    // 基于准备好的dom，初始化echarts实例
    var myChart = echarts.init(document.getElementById('main'));

    // 指定图表的配置项和数据
    myChart.setOption({
        title: {
            text: '周内各商品类销量趋势'
        },
        tooltip: {
            trigger: 'axis'
        },
        legend: {
            data: ['衬衫', '羊毛衫', '雪纺衫', '裤子', '高跟鞋','袜子']
        },
        grid: {
            left: '3%',
            right: '4%',
            bottom: '3%',
            containLabel: true
        },
        toolbox: {
            feature: {
                saveAsImage: {}
            }
        },
        xAxis: {
            type: 'category',
            boundaryGap: false,
            data: ['周一', '周二', '周三', '周四', '周五', '周六', '周日']
        },
        yAxis: {
            type: 'value'
        },
        series: []
    });

    myChart.showLoading();


    $.ajax({
        type : "get",
        async : false,            //异步请求（同步请求将会锁住浏览器，用户其他操作必须等待请求完成才可以执行）
        url : "weekly",    //请求发送到TestServlet处
        data : {},
        dataType : "json",        //返回数据形式为json
        success : function(result) {
            //请求成功时执行该函数内容，result即为服务器返回的json对象
            var data = result;
            if (data) {
                myChart.hideLoading();    //隐藏加载动画
                myChart.setOption({        //加载数据图表
                    series: result
                });
            }

        },
        error : function() {
            //请求失败时执行该函数
            alert("图表请求数据失败!");
            myChart.hideLoading();
        }
    })
</script>
</body>
</html>
```







# 案例

- 比较不错的入门疫情签到展示：<https://gitee.com/lyp_Believer/yqfx?_from=gitee_search>
- 比较不错的demo：<https://gitee.com/dpzhoufeng/echartsdemo>



# 参考

- `SpringBoot+Thymeleaf+ECharts`实现大数据可视化（基础篇）：<https://blog.csdn.net/shaock2018/article/details/86706101>