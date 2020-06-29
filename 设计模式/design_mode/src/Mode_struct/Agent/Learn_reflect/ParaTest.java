package Mode_struct.Agent.Learn_reflect;

import java.lang.reflect.Field;

/**
 * @author Kevin
 * @date 2020/3/10 20:45
 */

public class ParaTest {
    public static void main(String[] args) {
        HeroPlus h =new HeroPlus();
        //使用传统方式修改name的值为garen
        h.name = "garen";

        try {
            //获取类HeroPlus的名字叫做name的字段
            Field f1= h.getClass().getDeclaredField("name");
            //修改这个字段的值
            f1.set(h, "teemo");
            //打印被修改后的值
            System.out.println(h.name);

        } catch (Exception e) {
            // TODO Auto-generated catch block
            e.printStackTrace();
        }
    }
}

