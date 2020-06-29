package Mode_struct.Agent.Learn_reflect;

import java.lang.reflect.Method;

/**
 * @author Kevin
 * @date 2020/3/10 21:24
 */

public class MethodTest {
    public static void main(String[] args) {

        HeroPlus h = new HeroPlus();

        try {
            // 获取这个名字叫做setName，参数类型是String的方法
            Method m = h.getClass().getMethod("setName", String.class);
            // 对h对象，调用这个方法
            m.invoke(h, "盖伦");
            // 使用传统的方式，调用getName方法
            System.out.println(h.getName());

        } catch (Exception e) {
            // TODO Auto-generated catch block
            e.printStackTrace();
        }

    }
}


