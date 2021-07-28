简介：

1.IOC（已实现）：
（1）先调用 MyClassLoader.loadClass() 传入要扫描的包路径，classloader会扫描指定包路径下的所有class，放入：[class, ...]
（2）调用所有 Injector 接口的实现，将被标记的类实例化并注入容器，容器结构为：[IocEntity{tpye(类类型), name(注入时的名字，默认为类名首字母小写), object(类对象)}, ...]
（3）注入容器时需要创建代理对象，比如 @Mapper 注解的是一个接口，需要自动实现该接口（创建代理）
（4）自动装配：根据 @AutoWired 注解的值或者类型，从容器中拿相应的对象，如果被注解的字段是个接口，需要从容器中拿他的实现（如果该接口有多个实现，需要手动指定实现名称，即IocEntity的name属性）

2.事务（已实现）：
（1）在注入阶段扫描方法上有没有 @Transaction 注解，有的话为该方法创建一个切面（cglib动态代理实现）
（2）代理方法会在invoke该方法前创建一个connection放到ThreadLocal中，方法内部的sql都会复用ThreadLocal中的connection，出错回滚，不出错提交，实现事务

3.rpc（已实现）：
（1）使用 @RPCService / application.prop 配置rpc相关的信息
（2）rpc默认使用zookeeper作为注册中心
（3）rpc内部使用socket的对象流通信
（4）socket服务端有最大socket连接数（可配），默认为50个
（5）socket增加心跳机制，确保不在线的客户端不会占用服务端资源
（6）socket客户端配置有socket连接池（可配），实现socket连接复用，节省服务器资源
（7）socket服务端配置了线程池，确保资源利用最大化

新改进：

（1）jdbc statement 换成 preparestatement
（2）增加sql参数解析器
（3）将connection换为连接池
（4）prop配置文件加载器
（5）基于socket的rpc框架
（6）rpc默认的zookeeper注册中心
（7）socket增加连接池，和最大连接数限制
（8）线程池改用lazy模式，并设有线程空闲超时机制，确保线程池中没有太多空闲线程，节省资源
（9）数据库增加二级缓存机制，二级缓存依赖命名空间，通过 @EnableCache 配置，commit操作刷新命名空间中的缓存（默认使用java的hashmap结构实现），开发者可以自行实现com.framework.db.cache.Cache接口编写缓存类并替换（框架已经通过类名反射（在 @EnableCache 注解中把 cacheImplClass 属性配置成自己的实现类即可）支持自定义缓存类）
