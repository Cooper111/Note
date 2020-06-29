package Mode_struct.Agent.Learn_reflect;

/**
 * @author Kevin
 * @date 2020/3/10 19:37
 */
public class ObjectTest {
    public static void main(String[] args) {
        String className = "Mode_struct.Agent.Learn_reflect.Hero";
        try {
            //获取类对象第一种方式
            Class pClass1 = Class.forName(className);
            //获取类对象第二种方式
            Class pClass2 = Hero.class;
            //获取类对象的第三种方式
            Class pClass3 = new Hero().getClass();
            System.out.println(pClass1==pClass2);//输出true
            System.out.println(pClass1==pClass3);//输出true
        } catch (ClassNotFoundException e) {
            e.printStackTrace();
        }
    }
}
