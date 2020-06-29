package Mode_creation.singleton;

import java.util.concurrent.atomic.AtomicLong;

/**
 * @author Kevin
 * @date 2020/3/8 16:20
 * 双重检测式
 */
public class IdGenerater {
    // AtomicLong是一个Java并发库中提供的一个原子变量类型,
    // 它将一些线程不安全需要加锁的复合操作封装为了线程安全的原子操作，
    // 比如下面会用到的incrementAndGet().
    private AtomicLong id = new AtomicLong(0);
    private static IdGenerater instance;
    private IdGenerater(){}

    public static IdGenerater getInstance() {
        if(instance == null) {
            synchronized (IdGenerater.class) {
                if (instance == null) {
                    instance = new IdGenerater();
                }
            }
        }
        return instance;
        //return SingleTonHolder.instance;
    }

    //来个静态内部类试试
    private static class SingleTonHolder {
        private static final IdGenerater instance1 = new IdGenerater();
    }

    public long getId() {
        return id.incrementAndGet();
    }
}
// IdGenerator使用举例
// long id = IdGenerator.getInstance().getId();
