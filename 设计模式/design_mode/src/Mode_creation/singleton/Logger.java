package Mode_creation.singleton;

import java.io.File;
import java.io.FileWriter;
import java.io.IOException;
import java.net.URISyntaxException;

/**
 * @author Kevin
 * @date 2020/3/8 16:02
 * 单例示例：处理资源访问冲突
 */
public class Logger {
    private FileWriter writer;
    private static final Logger instance = new Logger();


    private Logger() {
        File file = new File("/Users/wangzheng/log.txt");
        try {
            writer = new FileWriter(file, true);//true表示追加写入
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    public static Logger getInstance() {
        return instance;
    }

    public void log(String message) throws IOException {
        writer.write(message);
    }
}
