package Mode_creation.singleton;

/**
 * @author Kevin
 * @date 2020/3/8 17:27
 */
public class StaticClass {

    public static long OUTER_DATE = System.currentTimeMillis();

    static {
        System.out.println("外部类静态块加载时间：" + System.currentTimeMillis());
    }

    public StaticClass() {
        System.out.println("外部类构造函数时间：" + System.currentTimeMillis());
    }

    static class InnerStaticClass {
        public static long INNER_STATIC_DATE = System.currentTimeMillis();
        static{
            System.out.println("静态内部类静态块加载时间：" + System.currentTimeMillis());
        }
    }

    class InnerClass {
        public long INNER_DATE = 0;
        public InnerClass() {
            INNER_DATE = System.currentTimeMillis();
        }
    }

    public static void main(String[] args) {
        //StaticClass outer = new StaticClass();
        //System.out.println("外部类静态变量加载时间：" + outer.OUTER_DATE);
        //System.out.println("非静态内部类加载时间"+outer.new InnerClass().INNER_DATE);
        System.out.println("静态内部类加载时间："+InnerStaticClass.INNER_STATIC_DATE);
    }
}