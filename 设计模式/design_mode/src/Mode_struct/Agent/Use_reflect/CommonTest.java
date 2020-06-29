package Mode_struct.Agent.Use_reflect;

/**
 * @author Kevin
 * @date 2020/3/10 22:00
 * 按照传统方法切换业务
 */
public class CommonTest {
    public static void main(String[] args) {
        //new Service1().doService1();
        //必须重新修改代码
        new Service2().doService();
    }
}
