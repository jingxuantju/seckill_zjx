# seckill_zjx
这是一个秒杀项目。



#### 两次MD5加密

- 第一次MD5加密是当**用户输入明文密码之后传到后端的时候要进行加密**，因为明文在网络中传输是很容易被人截获的。第一次的salt是前端定义的。所以第一次加密的**salt是一个final变量**，要和前端保证一致。
- 第一次MD5加密是当**后端接收了进行一次MD5加密的数据，存储到数据库之前再进行一次MD5加密**，防止数据库被盗用且知道salt，可以反推出明文密码，加密两次后反推后的数据任然是MD5加密过的。第二次的**salt是存储在数据库中的**。



#### 设置RespBean局统一返回对象

- 项目开发中，一般情况下都会对数据返回的格式做一个统一的要求，一般会包括状态码、信息及数据三部分。
  - private long code;
  - private String message;
  - private Object object;（上一个项目的R对象是一个Map：private Map<String, Object> data;）
- 以及枚举类，一般包含状态码和信息提示



#### 用户登录参数校验

- 编写ValidatorUtil类使用**正则表达式**校验手机号是否合法
- 每个类都写大量的健壮性判断过于麻烦，我们可以使用java的验证组件进行验证
  - 引入相依依赖
  - **自定义注解@IsMobile和IsMobileValidator自定义校验规则类**简化我们的代码，在LoginVo的属性上加入相应的自定义注解或者内置注解，并在LoginController中的对应参数LoginVo加入**@Valid**注解，表示LoginVo这个参数传入的时候要先进行校验



#### 全局异常处理

全局异常处理可以将异常处理解耦出来，实现异常信息的统一处理和维护。本质就是在项目中的异常全部抛出，由统一异常处理进行捕获处理。

[RestControllerAdvice注解与全局异常处理](https://juejin.cn/post/7025484367539470344)

- RestControllerAdvice的作用范围是：单个项目中所有使用了RequestMapping（像PostMapping底层是使用了RequestMapping注解的也支持）的类都归他管，归RestControllerAdvice管，这是一个应用于Controller层的**切面注解**，该注解还需要与其他注解配合使用才有意义。
- `@RestControllerAdvice+@ExceptionHandler`这两个注解的组合，被用作项目的全局异常处理，一旦项目中发生了异常，就会进入使用了RestControllerAdvice注解类中使用了ExceptionHandler注解的方法，**我们可以在这里处理全局异常，返回自定义的错误枚举类，将异常信息输出到指定的位置。并对所有的错误信息进行归置。**
- 在处理全局异常之前，一般只能返回一个错误的对象，处理全局异常之后可以抛出异常



#### cookie保持登录转态

因为在秒杀时首先要校验是否登录，所以对于登陆要保持状态，所以使用cookie

- 登录后使用UUIDUtil随机生成cookie，方法一：存cookie到seesion中需要用到request和response，`request.getSession().setAttribute(userTicket, user);`userTicket就是cookie
- 方法二：可以存入redis：`redisTemplate.opsForValue().set("user:" + userTicket, user)`;注意要自定义redisConfig，使用Json格式序列化



#### Redis实现分布式Session

##### Redis存储session的需要考虑问题：

- session数据如何在Redis中存储？

  考虑到session中数据类似map的结构，采用redis中hash存储session数据比较合适，如果使用单个value存储session数据，不加锁的情况下，就会存在session覆盖的问题，因此**使用hash存储session，每次只保存本次变更session属性的数据，避免了锁处理，性能更好**

- session属性变更何时触发存储？

  如果每改一个session的属性就触发存储，在**变更较多session属性时会触发多次redis写操作，对性能也会有影响，我们是在每次请求处理完后，做一次session的写入，并且只写入变更过的属性**

  如果本次没有做session的更改， 是不会做redis写入的，仅当没有变更的session超过一个时间阀值（不变更session刷新过期时间的阀值），就会触发session保存，以便session能够延长有效期

##### 1. spring-session-data-redis解决

- Spring 官方针对 Session 管理这个问题，提供了专门的组件 Spring Session，使用 Spring Session 在项目中集成分布式 Session 非常方便。Spring 为 Spring Session 和 Redis 的集成提供了组件：spring-session-data-redis，**只需要引入依赖和配置reids，不需要改变任何代码，session就会自动存在redis中**

##### 2. redis直接解决

**把用户信息存到reids中去，通过session去redis中获取用户信息**

- 第一次登录时，生成cookie存入redis，以Hash的格式，key是用户的cookie，值就是用户对象。

- 第二次登录时，根据用户登录传来的cookie，通过key去redis中找对应的对象。判断对象是否存在。



#### WebMvcConfigurer类

[WebMvcConfigurer中addArgumentResolvers方法的使用](https://www.1024sou.com/article/499053.html)：将的比较清楚

- addArgumentResolvers()：该方法可以用在对于**Controller中方法参数传入之前对该参数进行处理。**然后将处理好的参数在传给Controller中的方法。
- 用户第一次登录之后会得到一个cookie，在以后每次的访问过程中都会携带Cookie进行访问。在后台的Controller中对于需要登录权限的访问接口都要先获取Cookie中的sessionId，再使用sessionId从session中获取用户登录信息来判断用户登录情况决定是否放行。如果在每个需要登录权限访问的方法中都要写这个逻辑就会使代码重复，出现了冗余。
- 自定义addArgumentResolvers类，提前进行校验cookie，获取用户信息，此时**controller接收的参数不是由前端传入的**，而是由addArgumentResolvers方法处理之后传进来的，进一步解耦。



#### 秒杀商品独立建表

- 原思路：秒杀商品可以直接在商品表上建一个字段，比如1是秒杀商品，2不是。但是如果活动很多，就没法处理，而且秒杀价格和正常价格也不好显示，也不好维护。所以建议单独建表。
- 建立四个表，商品表，订单表，秒杀商品表，秒杀订单表



#### 秒杀业务主要流程

- 判断库存是否足够，可以通过获得goodsVo的库存是否小于1
- 同一用户是否重复下单，
  - 可以用MP的条件查询，是否能查到一个记录userid和goodsid已经存在，如果有，就说明改用户已经抢过该商品了
  - **存入redis中**，`redisTemplate.opsForValue().get("order:" + user.getId() + ":" + goodsId);`如果已经有订单了，说明抢过了，直接抛出异常
- 去秒杀商品表中减库存
- 生成订单，用订单号生成秒杀订单，用订单号实现外键级联



[性能测试：TPS和QPS的区别](https://www.cnblogs.com/uncleyong/p/11059556.html)

#### Mysql Linux下安装

[Mysql linux rpm版下载地址](https://dev.mysql.com/downloads/repo/yum/)

[网易开源镜像站](http://uni.mirrors.163.com/)

- `yum -y install mysql80-community-release-el7-5.noarch.rpm`
- `yum -y install mysql-community-server`
- 安装完成之后要首先启动mysql： `systemctl start mysqld`
- 检查mysql是否正常启动：`systemctl status mysqld`
- 获取mysql的内置密码
  - `cat /var/log/mysqld.log` ，找密码
  - 直接使用管道命令查找密码信息所在的对应行：`grep "password" /var/log/mysqld.log`（小技巧：xshell中 crtl+insert是复制，shift+insert是粘贴）
- 登录mysql：`mysql -u root - p`
- 修改密码：`alter user 'root'@'localhost' identified by 'rootRoot123.';`
- 查看密码要求：`show variables like 'validate_password%';`
- 修改密码要求：`set global validate_password.policy = 0;`，`set global validate_password.length = 0;`
- 修改为简单密码：`alter user 'root'@'localhost' identified by '123456';`，
- 创建新用户zjx：`create user 'zjx'@'%' identified by '123456';`
- 给新用户所有权限保证其可以远程连接：`grant all on *.* to 'zjx'@'%';`，第一个`*`表示所有库，第二个`*`表示所有表，`%`表示在所有主机上都可以访问。



#### Linux下使用Jmeter

- 在Linux下安装Mysql和Jmeter
- `java -jar seckill-0.01-SNAPSHOT.jar`运行java项目
- `cd /user/local/apache-jmeter-5.3/bin` 目录下运行`./jmeter.sh -n -t first.jmx -l result.jtl`
- 同时新开一个窗口使用`top`观察1,5,15分钟的load average



#### 超卖现象

- 在windows端和Linux端使用Jmeter 请求30000次，出现了严重的超卖现象
- 解决方法：
  - 悲观锁：在秒杀订单的表的用户id和商品id加联合索引，同时在ServiceImpl方法上加@Transactional注解，这样查询的时候就会产生行锁锁住记录行
  - 乐观锁：CAS+版本号 `update t_goods set stock=stock-1, version = version+1 where id=1, version=version;`
  - redis的单线程方法，当秒杀时，执行DECR 即可，当DECR返回值小于0时，即代表库存卖完了。



#### 缓存

- **页面缓存**（局限性较大），
  - 这样用户在短时间访问页面的时候就可以直接从redis中获得，不用每次访问都实时获取页面数据再渲染，降低数据库的压力。QPS从1200+提升到2300+
  - 但是这样每次后端给前端传输的还是整个页面，数据量还是很大，所以可以把每次前端不会修改的数据静态化，实现前后端分离。后端每次只传输需要改变的值即可。
  - 前后端分离后，前端就是一个静态页面，前端要获取数据的话，不再是通过模板引擎，不在通过model去传输数据，进行页面跳转，而是直接在前端进行页面跳转，通过ajax去请求接口，接收数据。
- **对象缓存**。这里需要注意，**如果修改了数据库一定要删除redis中对应的值**。比如我要修改用户的密码。修改完后，如果修改成功，要及时删除redis对应的值(redisTemplate.delete();)。



#### 预减库存

- 直接在redis中预减库存，**减少对数据库的并发访问**，后续还是要处理数据库，让数据库的数据正常扣减
- 实现
  - SeckillController实现InitializingBean接口，实现afterPropertiesSet()方法，在**初始化时把库存加载到redis中**
  - 直接在redis中扣减库存，不需要走数据库了。如果库存还有，就要封装对象发送给MQ
  - 如果redis中库存减为0了，但是还一直有请求过来，就会一直返回使用错误，可以用一个**Map进行内存标记**，private Map<Long, Boolean> **EmptyStockMap**，key是goodsId，value是是否为空。先判断Map，如果为空，这则不需要访问redis了，降低对redis的访问。



#### 使用RabbitMq

- 用user和goodsId封装了一个seckillMessage消息对象，通过RabbitMq发送这个消息对象，最后在我们的**监听者MQReceiver中做判断**，方法要加注解@RabbitListener(queues = "seckillQueue")。判断是否有库存，是否重复下单，如果都没有，则进行下单操作。使其变成**异步操作**，使其可以在Controller中直接先返回一个对象0，表示排队中。
- 后续再做一个**客户端的轮训**，即消费者去消费消息的时候，去获取秒杀订单对象。如果秒杀订单对象不为null，则返回订单id，下单成功，如果redis中存在空库存这个字符串，则返回-1，就是下单失败。否则返回0，就是排队中，
  - @RabbitListener(queues = "seckillQueue")：注解指定目标方法作为消费消息的方法，通过注解参数指定所监听的队列或者Binding。

#### QPS变化

- 优化前785 -> 优化后2454





#### Redis分布式锁

- 原本在redis预减库存，采取的方式是valueOperations.decrement()，保证操作的原子性
- 使用redisTemplate.execute(RedisScript<T> script, List<String> keys, Object args[])，使用了lua脚本保证原子性。
- 可以把lua脚本直接存在服务器端，减少网络中的数据传输量，可以提高吞吐量。



#### 秒杀接口地址的隐藏

- 在秒杀开始前将接口地址隐藏，等秒杀开始时通过获取请求真正的接口地址，再请求秒杀
- 编写一个创建秒杀地址的方法createPath()，**使用UUID+MD5加密随机生成一个str同时存入redis**，因为这个地址可能1分钟就会生成一个新的，所以不存入数据库
- 在秒杀方法前加入秒杀实际路径的判断checkPath(user, goodsId)，秒杀地址根据用户和商品是唯一的
- 但是如果黄牛提前得知了这个字符串并且获得了地址的拼接规则，还是有问题，所以要引入验证码机制



#### 生成验证码

- 防止脚本
- 拉长时间跨度，均分请求，降低前几秒集中的QPS
- 导入依赖，编写获取验证码方法verifyCode()，生成验证码的同时**存入redis**并设置过期时间，
- 在获取获取秒杀地址前先进行验证码的验证checkCaptcha()，将用户输入和redis中的值进行比较判断是否正确。



#### 接口限流

- 比如在1秒，接口最多能被请求1000次，如果秒杀刚开始被请求2000次就会挂掉，所以需要做接口限流，比如一个人5s只能请求5次。
- 使用reids实现计数器算法，如果第一次访问，存入一个值，并设置过期时间30s，如果第二次来，自增，如果超过5次，抛出异常
- 如果想在不同的接口进行限流就会产生代码的冗余，也会造成代码的耦合，所以自定义一个注解，拦截器实现
  - 使用**拦截器**实现，实现HandlerInterceptor接口，重写preHandle()方法，表示在业务逻辑之前处理，所有的判断全部写在这个方法里即可。
- 还有漏桶和令牌桶等方法可以实现接口的限流
- 除了要求能够**限制数据的平均传输速率**外，还要求允许**某种程度的突发传输**。这时候漏桶算法可能就不合适了，令牌桶算法更为适合。这些方法的本质还是要集成redis实现。

[接口限流算法：漏桶算法&令牌桶算法](https://segmentfault.com/a/1190000015967922)



#### 秒杀抢购系统的主要考虑点

- 高并发和黑客刷请求对服务器的负载冲击
- 高并发带来的超卖问题
- 高负载情况下下单速度和成功率的保障

[项目中遇到哪些难点，如何解决的](