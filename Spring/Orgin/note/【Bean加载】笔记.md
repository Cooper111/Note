# 概述

- 经过容器初始化阶段后，应用程序中定义的 bean 信息已经全部加载到系统中了，当我们显示或者隐式地调用 `BeanFactory#getBean(...)` 方法时，则会触发加载 Bean 阶段。
- 在这阶段，容器会首先检查所请求的对象是否已经初始化完成了，如果没有，则会根据注册的 Bean 信息实例化请求的对象，并为其注册依赖，然后将其返回给请求方。



# 如何记录Bean正在创建

- **逻辑**

位置：`getSingleton(beanName, objectFactory)`

前置处理：`beforeSingletonCreation(beanName)`在前置处理中加入表示正创建的`HashMap`

后置处理：`afterSingletonCreation(beanName);`在后置处理从表示正创建的`HashMap`中删除

- **调用**

当`getSingleton`时，若一级缓存没有，则**查询是否正在创建**，是则查看二三级缓存是否存在该缓存



# `CreateBean`

该方法在

- 创建

  `getBean(...)  -> doGetBean(...) ->  getSingleton(...)`时**创建**

- 调用

  `getSingleton() -> singleton.getObject()`时**调用**

  


`singletonFactory.getObject()`  就是 `createBean()`，通过lambda表达式赋的。



**先看看`#getSingleton(...)`方法总体作用**

![](http://static2.iocoder.cn/20170912091609918.jpeg)

这些功能全在`createBean(...)`里实现了。



`createBean`**功能：**

- 解析指定 `BeanDefinition` 的 class 属性。

- 处理 `override` 属性。

- 实例化的前置处理（AOP就是基于这个地方判断的）

  ```java
  // AbstractBeanDefinition.java
  
  Object bean = resolveBeforeInstantiation(beanName, mbdToUse);
  // ↓↓↓ 
  if (bean != null) {
  	return bean;
  }
  ```

  该函数`resolveBeforeInstantiation`的核心在于

  - `applyBeanPostProcessorsBeforeInstantiation()`  

  - `applyBeanPostProcessorsAfterInitialization()`

    两个方法，before 为实例化前的后处理器应用，after 为实例化后的后处理器应用

- 创建 Bean 对象`doCreateBean()`

  - 1.如果是单例模式，则清除缓存。

  - ##### 2.调用`#createBeanInstance(String beanName, RootBeanDefinition mbd, Object[] args)`方法，**实例化 bean** ，主要是将 `BeanDefinition` 转换为`org.springframework.beans.BeanWrapper`对象。

  - 3.MergedBeanDefinitionPostProcessor 的应用

  - 4.单例模式的**循环依赖**处理

  - ##### 5.调用 `#populateBean(String beanName, RootBeanDefinition mbd, BeanWrapper bw)` 方法，进行**属性填充**。将所有属性填充至 bean 的实例中

  - ##### 6.调用 `#initializeBean(final String beanName, final Object bean, RootBeanDefinition mbd)` 方法，初始化 bean

  - 7.依赖检查

  - 8.注册 `DisposableBean`





# Bean 的实例化策略：`InstantiationStrategy`

1. Bean 的创建，主要是 `AbstractAutowireCapableBeanFactory#doCreateBean(...)` 方法。在这个方法中有 Bean 的实例化、属性注入和初始化过程，对于 Bean 的实例化过程这是根据 Bean 的类型来判断的，如果是单例模式，则直接从 `factoryBeanInstanceCache` 缓存中获取，否则调用 `#createBeanInstance(...)` 方法来创建。
2. 在 `#createBeanInstance(...)` 方法中，如果 Supplier 不为空，则调用 `#obtainFromSupplier(...)` 实例化 bean。如果 `factory` 不为空，则调用 `#instantiateUsingFactoryMethod(...)` 方法来实例化 Bean 。如果都不是，则调用 `#instantiateBean(...)` 方法来实例化 Bean 。但是无论是 `#instantiateUsingFactoryMethod(...)` 方法，还是 `#instantiateBean()` 方法，最后都一定会调用到 `InstantiationStrategy` 接口的 `#instantiate(...)` 方法。

## 1. InstantiationStrategy

`InstantiationStrategy` 接口定义了 Spring Bean 实例化的策略，根据创建对象情况的不同，提供了三种策略：无参构造方法、有参构造方法、工厂方法

## 2. SimpleInstantiationStrategy

**`InstantiationStrategy`** 接口有两个实现类：**``SimpleInstantiationStrategy`**` 和 **``CglibSubclassingInstantiationStrategy``**。



`SimpleInstantiationStrategy` 对以上三个方法都做了简单的实现。

① 如果**是工厂方法实例化**，则直接使用反射创建对象

② 如果是**构造方法实例化**，则是先判断是否有 `MethodOverrides`，如果没有则是直接使用反射，如果有则就需要 CGLIB 实例化对象。



# 解决循环依赖问题

### 我的理解：

利用三级缓存（`singletonObjects`、`earlySingletonObjects`、`singletonFactories`）

来提前曝光正在创建的Bean，从而破除循环依赖

- #### 取缓存

  在`GetBean()`->`doGetBean()`里，一开始便会试着取singleton缓存：

  ```java
  getSingleton(String beanName, boolean allowEarlyReference)
  ```

  该函数会依次遍历三级缓存去取，函数逻辑：

  > - 首先，从一级缓存 `singletonObjects` 获取。
  > - 如果，没有且当前指定的 `beanName` 正在创建，就再从二级缓存 `earlySingletonObjects` 中获取。
  > - 如果，还是没有获取到且允许 `singletonFactories` 通过 `#getObject()` 获取，则从三级缓存 `singletonFactories` 获取。如果获取到，则通过其 `#getObject()` 方法，获取对象，并将其加入到二级缓存 `earlySingletonObjects` 中，并从三级缓存 `singletonFactories` 删除

  

  注：上面的步骤，有三级缓存升二级缓存的步骤。

  ​	二级缓存存在的**意义**，就是缓存三级缓存中的 `ObjectFactory` 的 `#getObject()` 方法的执行结果，提早曝光的**单例** Bean 对象

- #### 存缓存

  这里实现了提前曝光。

  - 存三级缓存

    - 调用位置：

      ①`getBean()` -> `doGetBean()` -> `getSIngleton(beanName, objectFactory)` -> `singletonFactory.getObject()`【这里其实就是`createBean`方法】 ->`doCreateBean(beanName, mbdToUse, args)` `

      ②`doCreateBean(...)`底下的-> `createBeanInstance(String beanName, RootBeanDefinition mbd, Object[] args)`

    - 存的逻辑

      其实就是存到三级缓存···

      ```java
      // DefaultSingletonBeanRegistry.java
      
      protected void addSingletonFactory(String beanName, ObjectFactory<?> singletonFactory) {
      	Assert.notNull(singletonFactory, "Singleton factory must not be null");
      	synchronized (this.singletonObjects) {
      		if (!this.singletonObjects.containsKey(beanName)) {
      			this.singletonFactories.put(beanName, singletonFactory);
      			this.earlySingletonObjects.remove(beanName);
      			this.registeredSingletons.add(beanName);
      		}
      	}
      }
      ```

  - 存一级缓存

    - 调用位置

      `getBean()` -> `doGetBean()` -> `getSIngleton(beanName, objectFactory)`

      ```java
      // AbstractBeanFactory.java
      
      public Object getSingleton(String beanName, ObjectFactory<?> singletonFactory) {
          Assert.notNull(beanName, "Bean name must not be null");
          synchronized (this.singletonObjects) {
              Object singletonObject = this.singletonObjects.get(beanName);
              if (singletonObject == null) {
                  //....
                  try {
                      singletonObject = singletonFactory.getObject();
                      newSingleton = true;
                  }
                  //.....
                  if (newSingleton) {
                      addSingleton(beanName, singletonObject);
                  }
              }
              return singletonObject;
          }
      }
      ```

    - 存的逻辑

      添加至一级缓存，同时从二级、三级缓存中删除

      ```java
      // DefaultSingletonBeanRegistry.java
      
      protected void addSingleton(String beanName, Object singletonObject) {
      	synchronized (this.singletonObjects) {
      		this.singletonObjects.put(beanName, singletonObject);
      		this.singletonFactories.remove(beanName);
      		this.earlySingletonObjects.remove(beanName);
      		this.registeredSingletons.add(beanName);
      	}
      }
      ```

      

- 核心：

  在`getBean() -> doGetBean()`里，如果再singleton缓存和父缓存中获取不到，在各scope创建Bean之前，有一段依赖Bean处理

  ```java
  // <7> 处理所依赖的 bean
              String[] dependsOn = mbd.getDependsOn();
              if (dependsOn != null) {
                  for (String dep : dependsOn) {
                      // 若给定的依赖 bean 已经注册为依赖给定的 bean
                      // 循环依赖的情况
                      if (isDependent(beanName, dep)) {
                          throw new BeanCreationException(mbd.getResourceDescription(), beanName,
                                  "Circular depends-on relationship between '" + beanName + "' and '" + dep + "'");
                      }
                      // 缓存依赖调用 TODO 芋艿
                      registerDependentBean(dep, beanName);
                      try {
                          getBean(dep);
                      } catch (NoSuchBeanDefinitionException ex) {
                          throw new BeanCreationException(mbd.getResourceDescription(), beanName,
                                  "'" + beanName + "' depends on missing bean '" + dep + "'", ex);
                      }
                  }
              }
  ```



​	但是，按照![](http://static2.iocoder.cn/images/Spring/2019-06-13/01.png)

也就是在`getBean()` -> `doGetBean()` -> `getSIngleton(beanName, objectFactory)` -> `singletonFactory.getObject()`【这里其实就是`createBean`方法】 ->`doCreateBean(beanName, mbdToUse, args) -> addSingletonFactory(...)` 这里，已经过了上述依赖Bean处理代码段，



在 `getSingleton`三大步骤的第一步实例化之后，进入属性注入时

```java
//可能存在依赖于其他 bean 的属性，则会递归初始依赖 bean
populateBean(beanName, mbd, instanceWrapper);
```

注：具体的递归依赖在`autowireByName、autowireByType`方法

这里的逻辑在`createBean`里 （`1.实例化 2.循环依赖 3.属性填充 4.初始化`），但是在



但在`populateBean`之前一步，

```java
// 提前将创建的 bean 实例加入到 singletonFactories 中
// 这里是为了后期避免循环依赖
addSingletonFactory(beanName, () -> getEarlyBeanReference(beanName, mbd, bean));
```

这里解决了循环依赖




### 芋道源码的理解：

至此，Spring 关于 singleton bean 循环依赖已经分析完毕了。所以我们基本上可以确定 Spring 解决循环依赖的方案了：

- Spring 在创建 bean 的时候并不是等它完全完成，而是在创建过程中将创建中的 bean 的 ObjectFactory 提前曝光（即加入到 `singletonFactories` 缓存中）。
- 这样，一旦下一个 bean 创建的时候需要依赖 bean ，则直接使用 ObjectFactory 的 `#getObject()` 方法来获取了，也就是 [「2.1 getSingleton」](http://svip.iocoder.cn/Spring/IoC-get-Bean-createBean-5/#) 小结中的方法中的代码片段了。

到这里，关于 Spring 解决 bean 循环依赖就已经分析完毕了。最后来描述下就上面那个循环依赖 Spring 解决的过程：

- 首先 A 完成初始化第一步（`即createBeanInstance实例化`）并将自己提前曝光出来（通过 `ObjectFactory` 将自己提前曝光），在初始化的时候，发现自己依赖对象 B，此时就会去尝试 get(B)，这个时候发现 B 还没有被创建出来
- 然后 B 就走创建流程，在 B 初始化的时候，同样发现自己依赖 C，C 也没有被创建出来
- 这个时候 C 又开始初始化进程，但是在初始化的过程中发现自己依赖 A，于是尝试 get(A)，这个时候由于 A 已经添加至缓存中（一般都是添加至三级缓存 `singletonFactories` ），通过 ObjectFactory 提前曝光，所以可以通过 `ObjectFactory#getObject()` 方法来拿到 A 对象，C 拿到 A 对象后顺利完成初始化，然后将自己添加到一级缓存中
- 回到 B ，B 也可以拿到 C 对象，完成初始化，A 可以顺利拿到 B 完成初始化。到这里整个链路就已经完成了初始化过程了

![](http://static2.iocoder.cn/images/Spring/2019-06-13/01.png)