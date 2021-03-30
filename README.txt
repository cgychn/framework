简介：

1.IOC（已实现）：
（1）先调用 MyClassLoader.loadClass() 传入要扫描的包路径，classloader会扫描指定包路径下的所有class，放入：[class, ...]
（2）调用所有 Injector 接口的实现，将被标记的类实例化并注入容器，容器结构为：[IocEntity{tpye(类类型), name(注入时的名字，默认为类名首字母小写), object(类对象)}, ...]
（3）注入容器时需要创建代理对象，比如 @Mapper 注解的是一个接口，需要自动实现该接口（创建代理）
（4）自动装配：根据 @AutoWired 注解的值或者类型，从容器中拿相应的对象，如果被注解的字段是个接口，需要从容器中拿他的实现（如果该接口有多个实现，需要手动指定实现名称，即IocEntity的name属性）

2.事务：
（1）在注入阶段扫描方法上有没有 @Transaction 注解，有的话为该方法创建一个切面（cglib动态代理实现）
（2）代理方法会在invoke该方法前创建一个connection放到ThreadLocal中，方法内部的sql都会复用ThreadLocal中的connection，出错回滚，不出错提交，实现事务
