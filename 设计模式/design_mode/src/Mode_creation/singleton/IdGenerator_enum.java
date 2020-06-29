package Mode_creation.singleton;

import java.util.concurrent.atomic.AtomicLong;

/**
 * @author Kevin
 * @date 2020/3/8 17:41
 */
public enum IdGenerator_enum {
    INSTANCE;
    private AtomicLong id = new AtomicLong(0);

    public long getId() {
        return id.incrementAndGet();
    }
}
