# 0x01 Jar启动实现

总体来说，Spring Boot `jar` 启动的原理是非常清晰的，整体如下图所示：

[![Spring Boot  启动原理](http://www.iocoder.cn/images/Spring-Boot/2019-01-07/30.png)](http://www.iocoder.cn/images/Spring-Boot/2019-01-07/30.png)Spring Boot 启动原理

**红色**部分，解决 `jar` 包中的**类加载**问题：

- 通过 [Archive](https://github.com/spring-projects/spring-boot/blob/master/spring-boot-project/spring-boot-tools/spring-boot-loader/src/main/java/org/springframework/boot/loader/archive/Archive.java)，实现 `jar` 包的**遍历**，将 `META-INF/classes` 目录和 `META-INF/lib` 的每一个内嵌的 `jar` 解析成一个 Archive 对象。
- 通过 [Handler](https://github.com/spring-projects/spring-boot/blob/master/spring-boot-project/spring-boot-tools/spring-boot-loader/src/main/java/org/springframework/boot/loader/jar/Handler.java)，处理 `jar:` 协议的 URL 的资源**读取**，也就是读取了每个 Archive 里的内容。
- 通过 [LaunchedURLClassLoader](https://github.com/spring-projects/spring-boot/blob/master/spring-boot-project/spring-boot-tools/spring-boot-loader/src/main/java/org/springframework/boot/loader/LaunchedURLClassLoader.java)，实现 `META-INF/classes` 目录下的类和 `META-INF/classes` 目录下内嵌的 `jar` 包中的类的加载。具体的 URL 来源，是通过 Archive 提供；具体 URL 的读取，是通过 Handler 提供。

**橘色**部分，解决 Spring Boot 应用的**启动**问题：

- 通过 [MainMethodRunner](https://github.com/spring-projects/spring-boot/blob/master/spring-boot-project/spring-boot-tools/spring-boot-loader/src/main/java/org/springframework/boot/loader/MainMethodRunner.java) ，实现 Spring Boot 应用的启动类的执行。

  ```
  public class MainMethodRunner {
  
  	private final String mainClassName;
  
  	private final String[] args;
  
  	/**
  	 * Create a new {@link MainMethodRunner} instance.
  	 * @param mainClass the main class
  	 * @param args incoming arguments
  	 */
  	public MainMethodRunner(String mainClass, String[] args) {
  		this.mainClassName = mainClass;
  		this.args = (args != null) ? args.clone() : null;
  	}
  
  	public void run() throws Exception {
  	    // <1> 加载 Spring Boot
  		Class<?> mainClass = Thread.currentThread().getContextClassLoader().loadClass(this.mainClassName);
  		// <2> 反射调用 main 方法
  		Method mainMethod = mainClass.getDeclaredMethod("main", String[].class);
  		mainMethod.invoke(null, new Object[] { this.args });
  	}
  
  }
  ```

  - `<1>` 处：通过 LaunchedURLClassLoader 类加载器，加载到我们设置的 Spring Boot 的主启动类。
  - `<2>` 处：通过**反射**调用主启动类的 `#main(String[] args)` 方法，启动 Spring Boot 应用。这里也告诉了我们答案，为什么我们通过编写一个带有 `#main(String[] args)` 方法的类，就能够启动 Spring Boot 应用。

当然，上述的一切都是通过 [Launcher](https://github.com/spring-projects/spring-boot/blob/master/spring-boot-project/spring-boot-tools/spring-boot-loader/src/main/java/org/springframework/boot/loader/Launcher.java) 来完成引导和启动，通过 `MANIFEST.MF` 进行具体配置。





# 0x02 SpringbootApplication

先来分析 Spring Boot 应用的**启动过程**



# 0x03 自动配置

- [微信推文](<https://mp.weixin.qq.com/s?__biz=MzIyMjQwMTgyNA==&mid=2247484313&idx=1&sn=9f91b0e5a40c5a26d7f96719e18bb07a&chksm=e82f47d6df58cec008b31ebcb3f482f752216d50e777ffcacf01017039d65997dd03544c2234&mpshare=1&scene=23&srcid=0724IWHFpciwr5wRVanVmQpB&sharer_sharetime=1595560999065&sharer_shareid=e6d90aec84add5cf004cb1ab6979727c#rd>)

- [芋道源码](<http://svip.iocoder.cn/Spring-Boot/AutoConfiguration/>)

