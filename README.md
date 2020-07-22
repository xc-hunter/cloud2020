[toc]

# PARTⅠ start

微服务架构是一种架构模式，提倡将单一应用程序划分成一组小服务，服务之间相互配合，互相协调。每个服务运行在其独立的进程中，服务与服务间采用轻量级的通信机制互相协作(通常是基于HTTP协议的Restful API)。

Spring Cloud选用Hoxton.SR1
Spring Boot选用2.2.2.RELEASE
Spring Cloud与Spring Boot 选型之间的约束[依赖查看](https://start.spring.io/actuator/info)
Spring Cloud Alibaba选用2.1.0.RELEASE
Java选用8
Maven选用3.5及以上
Mysql选用5.7及以上
[cloud参考资料](https://www.springcloud.cc/)

## cloud组件的替换

* 服务发现/注册     Eureka,可用替换
  * Zookeeper,保守路线使用
  * Consul，不推荐使用
  * **Nacos**，alibaba的，推荐使用
* 服务调用   Ribbon，停止更新，替换
  * LoadBalance，
  * Feign，推荐使用OpenFeign
* 熔断/降级    Hystrix，官网不推荐使用，可用替换
  * resilience4j,国外使用
  * Sentinel，alibaba的，国内使用
* 服务网关    Zuul，可用替换
  * GateWay，Spring推荐使用
* 配置    Config
  * Apollo，上海携程
  * **Nacos**，alibaba的
* 服务总线   Bus
  * **Nacos**

## 微服务架构编码构建

### 工程

约定>配置>编码
IDEA新建project工作空间---微服务cloud整体聚合父工程project构建，使用Maven，org.apache.maven.archetypes:maven-archetype-site。工程设置中：字符编码File Encodings，注解生效激活Annotation Processors，java编译版本选择Java Compiler，File Type过滤.idea、.iml

#### 父工程

dependencyManagement:这个元素提供一种管理依赖版本号的方式。通常会在一个组织或者项目的最顶层的父POM中使用。
使用pom.xml中的depenencyManagement元素让所有子项目中引用一个依赖而不用显示的列出版本号，Maven会沿着父子层次往上走，直到找到一个用于dependencyManagement元素的项目，然后使用这个元素中指定的版本号。

多个子项目可复用同一版本，当子项目也可自己选择声明版本，dependencyManagement只是声明依赖，并不实现引入，故子项目需要显示声明需要的依赖，类似于接口及具体实现的关系。

父工程创建完成执行mvn:install将父工程发布到仓库方便子工程继承

#### Rest微服务工程构建

order:80端口，消费者
payment：8001端口,微服务提供者

##### 微服务提供者module模块payment

* 建module，在父工程上new  module
* 改POM，
* 写YAML
* 主启动
* 业务类
  * 建表sql
  * entities
  * dao
  * service
  * controller

##### 热部署：

* 加入devtools依赖
* 加入插件到pom.xml中
* 在设置中enabling automatic build，compiler中打勾adbc
* ctrl+alt+shift+/，选择regustry，勾选compiler.automake.allow.when.app.running和actionSystem.assertFocusAccessFromEdt

先学习使用RestTemplate

开启Run Dashboad：

##### 工程重构：

系统中重复部分提取出来，一处部署，处处复用。如entities

* 新建cloud-api-common,放重复部分，工具类，共用实体等
* 父工程新建module，可以选择maven也可选择spring initializr
* 导入通用依赖，创建公用部分，mvn:install
* 其他的module可通过gav引入这个公用module

## 服务注册中心：Eureka

前面使用RestTemplate进行互相调用，但缺少一个总的管理者。

基础知识：

* 服务治理：传统的RPC远程调用框架中，管理每个服务与服务之间的依赖关系比较复杂,故需要服务治理，管理服务之间的依赖关系，可以实现服务调用、负载均衡、容错等，实现服务发现与注册。Spring Cloud封装了Netflix公司开发的Eureka模块来实现服务治理
* 服务注册：Eureka采用CS的设计架构，Eureka Server作为服务注册功能的服务器，是服务注册中心。系统中其他微服务，使用Eureka的Client连接到Eureka Server并维持`心跳连接`。EurekaClient启动后，会向Eureka Server发送心跳(默认周期为30S)，如果Eureka Server在多个心跳周期内没有接收到某个节点的心跳，EurekaServer会从服务注册表中把这个服务节点移除(默认90s)
  * 在服务注册与发现中，存在一个服务注册中心。当服务器启动时，对将当前服务器的信息，如服务地址通信地址等以别名方式注册到注册中心中，另一方(消费者|服务提供者)，以别名方式去注册中心去获取到实际的服务通讯地址，然后实现本地RPC调用。
  * RPC远程调用框架核心设计思想：在于注册中心。
  * 通常，服务采用集群。

### 单机版Eureka构建步骤

* 生成Server端注册中心：建module，改pom，写yml，主启动，测试
  * yml中属性配置：服务端

### 集群版Eureka

| 属性                                                 | 作用                                                         |
| ---------------------------------------------------- | ------------------------------------------------------------ |
| spring.application.name                              | 在注册中心中的服务名                                         |
| eureka.instance.hostname                             | eureka服务端的实例名称                                       |
| eureka.instance.instanceId                           | 微服务在注册中心中的实例名                                   |
| eureka.instance.prefer-ip-address                    | 访问路径可以显示Ip地址，默认为false                          |
| eureka.client.register-with-eureka                   | 是否将自己注册到EurekaServer，默认为true                     |
| eureka.client.fetchRegistry                          | 是否从EurakaServer抓取已有的注册信息，默认为true             |
| eureka.client.service-url.defaultZone                | EurekaServer地址，可多个，http://服务器实例名:端口号/eureka  |
| eureka.server.enable-self-preservation               | 在服务器节点配置，默认为true，开启自我保护模式               |
| euraka.server.eviction-interval-timer-in-ms          | 在服务器节点配置，确认心跳的间隔时间                         |
| eureka.instance.lease-renewal-interval-in-seconds    | 在微服务节点配置，客户端向服务端发送心跳的间隔时间           |
| eureka.instance.lease-expiration-duration-in-seconds | 在微服务节点配置，Eureka服务端在收到最后一次心跳后等待时间上限 |

* 注册中心集群：互相注册，相互守望eureka.client.serviceurl.defaultZone
* 其他微服务：spring.application.name配置服务名称对应于serviceId,defaultZone可选择多个。eureka.client.instance.instanceId，在服务注册中心中一个serviceId可对应多个InstanceId，即一个服务多节点
* 微服务提供者集群实现
* 在微服务提供集群的基础上，仍使用RestTemplate调用时可不使用地址和端口号，可使用服务别名去访问，但需要在获取RestTemplate的bean上配置负载均衡算法@LoadBalance
* 服务发现Discovery：对于注册进eureka里面的微服务，可以在微服务端通过服务发现discoveryclient获取服务注册中心注册的所有微服务名，也可以根据服务的名称来获得该服务的信息，如服务的各个实例。。。。
* 自我保护机制：保护模式，进入保护模式，eureka在某个时刻某个微服务不可用(即没有收到心跳)时，eureka不会立刻清理，依旧会对该微服务的信息进行保存，属于`CAP里面的AP分支`。
  默认情况下，如果EurekaServer在一定时间内没有接收到某个微服务实例的心跳，Euraka将会注销该实例(默认90秒)。但是当网络分区故障发生时(延时、卡顿、拥挤)时，微服务与EurekaServer之间无法正常通信，以上行为可能变得非常危险。因为微服务本身是健康的，不应该被注销。
  自我保护模式：当EurekaServer节点在短时间内丢失过多客户端时,(可能发生了网络故障)，那么这个节点会进入自我保护模式
* 禁止自我保护：掉线就注销，
  * 在服务端节点，eureka.server.enable-self-preservation:false,关闭自我保护机制，保证不可用微服务被及时删除，euraka.server.eviction-interval-timer-in-ms:2000,更改确认心跳的时间间隔。
  * 在微服务节点，eureka.instance.lease-renewal-interval-in-seconds:1,Eureka客户端向服务端发送心跳的间隔时间，默认30s，eureka.instance.lease-expiration-duration-in-seconds:2,Eureka服务端在收到最后一次心跳后等待时间上限，单位为秒，默认90秒，超时剔除服务。

### Eureka停更，2.0之后停更

## 服务注册中心：Zookeeper

### Spring Cloud整合Zookeeper替换Eureka

注册中心zookeeper:

* zookeeper为一个分布式协调工具，可以实现注册中心功能
* 安装zookeeper，关闭linux服务器防火墙systemctl stop firewalld后启动zookeeper服务器
* zookeeper服务器期待Eureka服务器，作为服务注册中心
* 可能出现版本问题，spring-cloud-starter-zookeeper-discovery引入的zookeeper依赖版本与服务器安装的zookeeper的`版本冲突`
* zookeeper中zkCli.sh启动

#### 服务提供者

* pom中引入spring-cloud-starter-zookeeper-discovery依赖

* 在yaml中配置属性：spring.application.name以及spring.cloud.zookeeper.connect-string

* 主启动类上@EnableDiscoveryClient

* 服务节点为临时节点，

#### 服务消费者

与服务提供者一样配置进入zookeeper，一样使用RestTemplate,需要配置Bean。

集群也是在spring.cloud.zookeeper.connect-string下可配置多个

## 服务注册中心：Consul

Consul使用Go语言开发，提供微服务系统中的服务治理、配置中心、控制总线等功能。每个功能可单独使用，也可以一起使用构建全方位的服务网格。

* 服务发现：提供HTTP和DNS两种发现方式
* 健康监测、支持多种方式，HTTP、TCP、Docker、Shell脚本定制化
* KV存储、Key-Value的存储方式
* 多数据中心、
* 可视化web界面

Consul安装：windows版：只有一个consul.exe文件，直接开发模式启动consul agent -dev，可以使用localhost:8500访问首页

### 服务提供者

POM中引用依赖spring-cloud-starter-consul-discovery

yaml中配置属性:

* spring.application.name:服务名
* spring.cloud.consul.
  * .host:localhost
  * .port:8500
  * .discovery
    * service-name:${spring.application.name}

主启动类：@EnableDiscoveryClient

### 服务消费者

配置同上，使用RestTemplate，进行服务消费

## 三个注册中心的异同点

|                   | Eureka     | Zookeeper  | Consul     |
| ----------------- | ---------- | ---------- | ---------- |
| 语言              | Java       | Java       | Go         |
| CAP               | AP，高可用 | CP，一致性 | CP，一致性 |
| 服务健康检查      | 可配支持   | 支持       | 支持       |
| 对外暴露接口      | HTTP       | 客户端     | HTTP/DNS   |
| Spring Cloud 集成 | 已集成     | 已集成     | 已集成     |

CAP Theoram:Consistency`强一致`,Availability`可用性`,Partition Tolerance`分区容错性`
CAP理论关注粒度是数据，而不是整体的系统设计

CAP理论的核心是：一个分布式系统不可能同时很好的满足一致性，可用性和分区容错性，
根据CAP原理将NoSQL数据库分成了满足CA原则、CP原则、AP原则三大类

* CA -单点集群，满足一致性和可用性，但可扩展性不太强大。RDBMS
* CP-满足一致性，分区容忍性的系统，通常性能不是特别高。MongoDB、Hbase、Redis
* AP-满足可用性，分区容忍性的系统，通常可能对一致性要求低一些。CouchDB、Cassandra、DynamoDB

* AP架构、

* CP架构、

## 服务调用：Spring Cloud Ribbon

概述：基于Netflix Ribbon实现的一套客户端负载均衡的工具，主要功能是提供客户端的软件负载均衡和服务调用。在配置文件中列出Load Balancer的所有机器，Ribbon会自动基于某种负载均衡算法去选择连接这些机器。同时，可使用Ribbon实现自定义的负载均衡算法。

Ribbon目前进入维护模式，但目前仍在使用，未来使用Spring Cloud LoadBanlancer替代
LB负载均衡：将用户请求平摊分配到多个服务上，从而达到系统的高可用。

Ribbon`本地负载均衡`，在调用微服务接口时，会在注册中心上获取注册信息服务列表之后缓存到JVN本地，从而在本地实现RPC远程服务调用技术。
Nginx为`服务器负载均衡`,负载均衡由服务器实现

* 集中式LB：即在服务的消费方和提供方之间使用`独立的LB设施`（可以为硬件，如F5，可以为软件。如Nginx），由该设施负责把访问请求通过莫一种策略转发至服务的提供方。
* 进程内LB：将LB逻辑集成到消费方，消费方从注册中心获知有哪些地址可用，然后根据负载均衡算法选择出一个合适的服务。Ribbon属于这一类，为一个类库，集成于消费方进程，消费方通过它来获取到服务提供方的地址。

依赖spring-cloud-starter-netflix-eureka-client,传入了ribbon依赖。
@LoadBalanced

### RestTemplate的使用

* getForObject方法，可使用三个参数，依次为url，返回类型，URI路径参数，返回对象为响应体ResponseBody中数据转化成的对象，
* getForEntity方法，返回对象为ResponseEntity对象，包含响应体和响应头。
  * ResponseEntity对象可以使用很多方法
* postForObject方法，三个参数，依次为url，请求实体对象HttpEntity<T>,返回类型。通过POST请求传递JSON请求体。
  * 例子  HttpHeaders header=new HttpHeader();
    header.setContentType(MediaType.APPLICATION_JSON_UTF8)，还可设置其他请求头信息
    HttpEntity<User> request=new HttpEntity<>(user,header);
* postForEntity方法

### 负载均衡算法

核心接口：IRuler，choose方法具体实现：

* RonndRobinRule:轮询算法
* WeightResponseTimeRule：加权轮询，响应速度越快的实列权重越大，越容易被选中。
* RandomRule：随机算法
* RetryRule：先轮询获取服务，如果获取失败则在指定时间内会进行重试
* BestAvailableRule：会先过滤掉故障实例，然后选择一个并发量小的服务
* AvailabilityFilteringRule：先过滤故障实例，在选择并发较小的实例
* ZoneAvoidanceRule：符合判断server所在区域的性能和server的可用性选择服务器

#### 替换

自定义配置类不能位于@ComponnentScan的包及子包下，否则自定义的配置类会被所有的Ribbon客户端共享，达不到特殊化定制的目的。此处的特殊化定制，即`针对不同的服务调用，可以定制LB的rule`。
主启动类上去指定使用的规则配置类所在

* 在外面的包中配置一个IRule的Bean
* @RibbonClient(name="调用的服务名"，configuration=MySelfRule.class)

#### Ribbon默认负载均衡算法原理

轮询负载均衡算法原理：rest接口第几次请求%服务的总实例数=实际调用服务的实例的下标，每次服务重启后，rest接口计数初始化为1

List[0] instance=127.0.0.1:8001
List[1] instance=127.0.0.1:8002，如第三次请求时，3%2=1，则返回127.0.0.1：8001

源码：

手写：自己

* 新建接口LoadBalancer，

* 新建类实现LoadBalancer，使用discoveryClient获取服务实例。类上@Component

* ``` 
  @Component
  public class MyLb implements LoadBalance {
      private AtomicInteger atomicInteger=new AtomicInteger(0);
      public final int getAndIncrement(){
          int current;
          int next;
          do{
              current=this.atomicInteger.get();
              next=current>=2147483647?0:current+1;
          }while(!this.atomicInteger.compareAndSet(current, next));
          return next;
      }
      //此处传入的List<ServiceInstance>可以由DiscoveryClient获取
      @Override
      public ServiceInstance returnInstance(List<ServiceInstance> serviceInstanceList) {
  
          int index=this.getAndIncrement()%serviceInstanceList.size();
          return serviceInstanceList.get(index);
      }
  }
  ```

## 服务调用：OpenFeign

Feign停止更新，不用再关心。

OpenFeign：

* 声明式调用组件，一个声明式WebService客户端，使用方法是`定义一个服务接口，然后在上面注解`。
* 前面使用Ribbon+RestTemplate时，利用RestTemplate对于http请求的封装处理，形成一套模块化的调用方法。但实际开发中，针对一个服务的调用是调用这个服务的多处功能，所以针对每一个服务自行封装客户端来包装这些服务的调用。Feign帮助我们定义和实现依赖服务接口的定义。类似于Dao接口上的@Mapper。
* 例子：一个payment服务提供get/create/modify的功能服务，使用Ribbon和RestTemplate时，针对每个功能，每次都得使用RestTemplate调用，属于模板式调用。使用Feign可以解决这种问题。

 ```
@FeignClient("服务名")
//service可以定义一个服务得多个功能,这个接口中的方法可对应于服务提供方的controller中的方法
public interface Service{
@GetMapping("请求服务的路径，不是从这个客户端开始的全路径")
public T getT(@PathVariable() )
}
接着使用时将service注入到controler中即可
在controller中
@AutoWired
private Service service;
 ```

### 使用步骤

* 引入依赖spring-cloud-starter-openfeign依赖
* 主启动类上加@EnableFeignClients
* 新建服务接口，@FeignClient注解，服务接口中方法书写。
* service接口注入到controller中，直接使用这个接口调用方法完成远程调用。

### 超时控制

服务消费者与服务提供者对于服务调用时的时间互相约定

默认Feign客户端只等待1秒钟，但是服务端处理需要超过1s时，导致feign客户端不想等待，直接返回报错。
为了避免这种情况，需要设置feign客户端的超时控制。

yml文件中开启配置

~~~
#设置feign客户端超时时间)
feign:
  client:
    config:
      #default代表所有服务
      default:
        #feign客户端建立连接超时时间
        connect-timeout: 10000
        #feign客户端建立连接后读取资源超时时间
        read-timeout: 20000
        #日志级别
        logger-level: 
      #而service-test表示当调用service-test这个服务时，用下面的配置，即特殊化定制
      service-test:
        connect-timeout: 10000
        read-timeout: 20000
## ribbon设置也可        
ribbon:
  #HTTP建立socket超时时间
  ConnectTimeout: 50000
  #http读取响应socket超时时间
  ReadTimeout: 50000
~~~



### 日志打印功能

Feign提供了日志打印功能，可通过配置调整日志级别，从而了解feign中Http请求的细节，即对于Feign接口的调用情况进行监控和输出。

日志级别：

* NONE:默认的，不显示任何日志
* BASIC：仅记录请求方法、url、响应状态码以及执行时间
* HEADERS：除了BASIC中定义的信息之外，还有请求和响应的头信息
* FULL:除了HEADERS中的信息，还有请求和响应的正文及数据

配置日志Bean：

* 在配置类中@Bean一个Logger.Level

```
@Configuration
public class FeignConfig{
@Bean
Logger.Level feignLoggerLevel(){
return Logger.Level.FULL;
}
}
```

* yml中开启日志配置

``` 
logging:
  level:
    #feign日志以什么级别监控哪个接口
    com.xc.springcloud.service.PaymentService: debug
```



* 后台日志查看

## Hystrix断路器[资料](https://github.com/Netflix/Hystrix/wiki/How-To-Use)

假设支付微服务请求中出现压力过大，服务响应变缓，进入瘫痪状态，而此时订单微服务还是正常响应，但当订单微服务调用支付微服务时，就会出现大量等大，若还是持续的调用，则订单微服务也会大量积压请求，导致订单微服务也不可用。这种传染病式的病变需要解决。级联故障需要解决。

重要概念：

* 服务降级：一般为fallback，服务器忙，稍后再试，不让客户端等待返回友好提示，下面为服务降级触发情景：
  * 程序运行异常、超时、服务熔断触发服务降级、线程池/信号量打满也会触发服务降级
* 服务熔断：一般为break，达到最大服务访问后，直接拒绝访问，然后调用服务降级的方法并返回友好提示
* 服务限流：一般为flowlimit，秒杀高并发等操作，严禁同时过来拥挤，将请求排队，有序进行。

断路器：即将大量积压请求熔断，来保障其自身微服务的可用性，避免蔓延。
处理限制请求的方式的策略很多，如限流、缓存等。最为常见的是降级服务，当请求其他微服务出现`超时或者发生故障`时，就会使用自身服务的其他方法进行响应。即有一个符合预期的备选响应，保证服务调用方的不会被长时间、不必要的占用，从而避免了蔓延。
Hystrix：停止更新，进行维护。

### 案例

#### 构建平台

构建cloud-provider-hystrix-payment8001模块，引入spring-cloud-starter-netflix-hystrix依赖。

* JMeter高并发压测

请求出问题时：解决：

* 8001服务超时了或者down机了，调用者80不能一直卡死等待，必须有服务降级
* 8001服务ok，调用者80自己出故障，或者有自我要求（自己的等待时间小于服务提供者的消耗时间），自己处理降级。

#### 服务降级

@HystrixCommand，方法上

@EnableCircuitBreakr，主启动类上

##### 服务提供方：

设置自身被调用超时时间的峰值，峰值内可以正常运行，超过时需要由兜底的方法处理，做服务降级fallback。

```
@HystrixCommand(fallbackMethod = "paymentInfo_TimeoutHandler",
                commandProperties ={
            	@HystrixProperty(name="execution.isolation.thread.timeoutInMilliseconds",value="3000")
})
public String paymentInfo_Timeout(Integer id){
        //int age=10/0;  模拟down机
        try {
            TimeUnit.SECONDS.sleep(5);//模拟超时
        } catch (InterruptedException e) {
            e.printStackTrace();
        }
        return "线程池："+Thread.currentThread().getName()+"*****paymentInfo_Timeout,id:"+id+"****耗时3秒钟";
    }
    当前服务不可用时，都是使用fallback指定的方法。
    
```

##### 服务消费方：

`一般只在消费方做服务降级，提供方的服务降级不常见`。

对于调用服务有自己的时间要求,自己做服务降级

##### 全局服务降级

每个业务方法对应于一个fallback方法，很sb，也太过冗余，可解决：

* `@DefaultProperties(defaultFallback="降级方法名")`为所有业务方法配置一个默认降级方法，这个注解放在业务类上，其中的降级方法写在这个注解类中，这样为这个类中所有方法定义了fallback方法。其他方法只需按需定义@HystrixConmand中的其他属性。

* 个别特殊业务可以写定制化的降级服务，即在@HystrixCommand中配置属性fallbackmethod。

  但降级服务与业务代码仍在一起

##### 通配服务降级

将降级服务与业务代码脱离。解决：

* 新建类PaymentFallbackService实现feignclient的服务接口，实现接口里面的方法，方法的实现即为降级服务

* 在yml中配置

  ``` 
  feign:
     hystrix:
       enabled:true
       在feign中开启Hystrix
  ```

* 在原先的@FeignClient上加属性fallback

```
@FeignClient(value="cloud-payment-hystrix-service",fallback = PaymentFallbackService.class)
public interface PaymentHystrixService
```

#### 服务熔断

达到最大服务访问后，直接拒绝访问，然后调用服务降级的方法并返回友好提示

熔断机制：应对雪崩效应的一种微服务链路保护机制。当扇出链路的某个微服务出错不可用或者响应时间太长时，会进行服务的降级，进而熔断该节点微服务的调用，快速返回错误的响应信息。
当检测到该节点微服务调用响应正常后，`恢复调用链路`。
在Spring Cloud框架里，熔断机制通过Hystrix实现。Hystrix会监控微服务间调用的状况。
当失败的调用到一定阈值，缺省是5s内20次调用失败，就会启动熔断机制，熔断机制的注解是@HystrixCommand

```
@HystrixCommand(fallbackMethod = "paymentCircuitBreaker_fallback",commandProperties = {
            @HystrixProperty(name="circuitBreaker.enabled",value="true"),
            @HystrixProperty(name="circuitBreaker.requestVolumeThreshold",value="10"),
            @HystrixProperty(name="circuitBreaker.sleepWindowInMilliseconds",value="10000"),
            @HystrixProperty(name="circuitBreaker.errorThresholdPercentage",value="60")
    })
    public String paymentCircuitBreaker(@PathVariable("id")Integer id){
        if(id<0){
            throw new RuntimeException("****id不能为负数***");
        }
        String serialNumber= IdUtil.simpleUUID();
        return Thread.currentThread().getName()+"\t"+"调用成功，流水号："+serialNumber;
    }
    public String paymentCircuitBreaker_fallback(@PathVariable("id")Integer id){
        return "id不能负数，请稍后再试，******id:"+id;
    }
@HystrixProperty(name="circuitBreaker.enabled",value="true"),//是否开启断路器
@HystrixProperty(name="circuitBreaker.requestVolumeThreshold",value="10")//请求次数
@HystrixProperty(name="circuitBreaker.sleepWindowInMilliseconds",value="10000")//休眠时间窗
@HystrixProperty(name="circuitBreaker.errorThresholdPercentage",value="60")//失败率达到多少后跳闸
10秒中10次访问失败率达到60%时跳闸，断路器open，一段时间后，默认为5秒，此时断路器为half-open状态试着让一个请求去调用，若通过，断路器则恢复到closed状态，正常访问ok，不用去fallback的方法。即恢复链路调用。
其他属性

```

熔断类型：

* open：处于open时，来请求都会去降级服务fallback处理，降级逻辑为主逻辑
* closed
* half-open：open一段休眠时间窗后，会转至此状态。

涉及到断路器的三个重要参数

* 快照时间窗：断路器确定是否打开需要统计一些请求和错误数据，而统计的时间范围就是快照时间窗，默认为最近的10秒
* 请求总数阈值：在快照时间窗内，必须满足请求总数阈值才有资格熔断。默认为20，意味着，在10秒内秒如果该hystrix命令的调用次数不足20次，即使所有的请求都超时或其他原因失败，断路器都不会断开。
* 错误百分比阈值：默认为50%，当请求总数在快照时间窗内超过了阈值，如发生了30次调用，如果在这30次调用中，有50%及以上的失败比，断路器断开open

即先要有请求的次数满足，然后还要满足错误百分比，才能去open。

* 自动恢复功能：当处于open时，所有请求都去降级处理方法，经过一个休眠时间窗后，默认5秒，断路器为half-open,会让一个请求尝试访问，若成功访问，则断路器转到closed，否则，继续处于open，再等待一个休眠时间窗，再次下一轮的尝试。

##### 重点:HystrixCommandProperties

其中的property

| 属性                                                | 意义                                                         |
| --------------------------------------------------- | ------------------------------------------------------------ |
| execution.isolation.strategy                        | 设置隔离策略，THREAD 表示线程池SEMAPHORE表示信号池隔离       |
|                                                     |                                                              |
|                                                     |                                                              |
| execution.isolation.semaphore.maxConcurrentRequests | 当隔离策略选择信号池隔离时，用来设置信号池的大小(最小并发数)默认为10 |
| execution.isolation.thread.timeoutMilliseconds      | 配置命令执行的超时时间，默认为10                             |
| execution.timeout.enabled                           | 是否启用超时时间，默认为true                                 |
| execution.isolation.thread.interruptOnTimeout       | 执行超时的时候是否中断，默认为true                           |
| execution.isolation.thread.interruptOnCancel        | 执行被取消的时候是否中断，默认为true                         |
| fallback.isolation.semaphore.maxConcurrentRequests  | 允许回调方法执行的最大并发数                                 |
| fallback.enabled                                    | 服务降级是否启用，是否执行回调函数                           |
| circuitBreaker.enabled                              | 是否启用断路器，默认为true                                   |
| circuitBreaker.requestVolumeThreshold               | 设置在滚动时间窗中，断路器熔断的最小请求数。默认为20.若在滚动时间窗中，请求数未达到这个数，即使请求全失败，断路器也不会open |
| circuitBreaker.errorThresholdPercentage             | 在滚动时间窗中，总请求数的失败比达到这个失败比，则断路器open，默认为50 |
| circuitBreaker.sleepWindowInMilliseconds            | 休眠时间窗，处于open时，过这段时间后，会转为half-open，在尝试熔断的请求命令，如果依然失败，则open，若成功，则closed，默认5000，即5s |
| circuitBreaker.forceOpen                            | 断路器强制打开，默认false                                    |
| circuitBreaker.forceClosed                          | 断路器强制关闭，默认false                                    |
| metrics.rollingStats.timeInMilliseconds             | 滚动时间窗，该时间用于断路器判断健康度时需要收集信息的持续时间，默认为10000，即10秒 |
| metrics.rollingStats.numBuckets                     | 该属性用来设置滚动时间窗统计指标信息时划分桶的数量，断路器在收集指标信息时会根据设置的时间窗长度分成多个桶来统计各度量值，每个桶记录了一段时间内的采集指标。默认为10，即10秒内划分成10个桶收集。 |
| metrics.rollingPercentile.enabled                   | 设置对命令执行的延迟是否使用百分位数来跟踪和计算，默认为false，所有概要统计返回-1 |
| metrics.rollingPercentile.timeInMilliseconds        | 设置百分位统计的滚动窗口的持续时间，默认60000，即60s         |
| metrics.rollingPercentile.numBuckets                | 设置百分位统计滚动窗口中使用桶的数量，默认60000              |
| metrics.rollingPercentile.bucketSize                | 设置在执行过程中每个桶中保留的最大执行次数。如果在滚动时间窗内发生超过该设定值得执行次数，就从最初的位置开始重写。例如，将该值设为100，滚动窗口为10秒，10秒内一个桶中发生了500次执行，该桶只保留最后的100次执行的统计。该数的大小将会增加内存量的消耗，并增加排序百分位数所需的计算时间，默认100 |
| metrics.healthSnapshot.intervalInMilliseconds       | 设置采集影响断路器状态的健康快照（请求的成功、错误百分比）的间隔等待时间，默认500 |
| requestCache.enabled                                | 是否开启请求缓存，默认为true                                 |
| requestLog.enabled                                  | HystrixCommand的执行和事件是否打印日志到HystrixRequestLog中  |

#### 服务限流

在Alibaba里面用Sentinel说明。

#### Hystrix工作流程

[参考连接](https://github.com/Netflix/Hystrix/wiki/How-it-Works)

#### Hystrix Dashboard

准实时的调用监控，Hystrix会持续地记录所有通过Hystrix发起的请求的执行信息，并以统计报表和图形的形式展示给用户，包括每秒执行多少请求，多少成功，多少失败。

* 新建cloud-hystrix-dashboard9001，

* pom中引入spring-cloud-starter-netflix-hystrix-dashboard依赖，yml中定义server.port以及spring.application.name

* 主启动类上添加@EnableHystrixDashboard

* 所有其他微服务提供类需要监控依赖配置，下面2选1

  * 1、yml中暴露端点，这个访问的路径为/actuator/hystrix.stream

    ```
    management:
      endpoints:
        web:
          exposure:
            include: health,info,hystrix.stream
    ```

    

  * 2、在需要监控的微服务自动类中加入servlet配置，添加/hystrix.stream路径。访问的路径为/hystrix.stream

```
management:
  endpoints:
    web:
      exposure:
        include: health,info,hystrix.stream
```

* 访问cloud-hystrix-dashboard9001的路径，locahost:9001/hystrix，输入要监控微服务的地址，定义Delay轮询时间，即隔多少时间轮询一次和Title，仪表盘页面的标题
* 仪表盘页面的解读

![image-20200620140000807](C:\Users\Administrator\AppData\Roaming\Typora\typora-user-images\image-20200620140000807.png)

## 服务网关

### zuul：[官网](https://github.com/Netflix/zuul/wiki)

* 新建一个module，

* pom中的依赖

  ```pom
  <!引入服务发现>
  <dependency>
              <groupId>org.springframework.cloud</groupId>
              <artifactId>spring-cloud-starter-netflix-eureka-client</artifactId>
          </dependency>
   <!引入zuul的依赖>
  <dependency>
              <groupId>org.springframework.cloud</groupId>
              <artifactId>spring-cloud-starter-netflix-zuul</artifactId>
          </dependency>
  ```

* yml中配置

  ```
  server:
    port: 80
  spring:
    application:
      name: zuul
  eureka:
    client:
      service-url:
        defaultZone: http://eureka7001.com:7001/eureka/
  
  zuul.routes.<key>.path=/u/**
  auul.routes.<key>-url=http://localhost:8001/##path对应于url
                       =服务在注册中心的serviceId,这样zuul会自动使用服务端负载均衡，分摊请求到各个节点
  ```

* 在主启动类中加@EnableZuulProxy，这个注解包含了@EnableCircuitBreaker，即zuul引入了断路机制，以防在请求不到的时候进行断路，避免网关发生请求无法释放的场景。

* zuul的module中还可以配置其他网关功能，如过滤请求s，避免恶意请求转发到后端的服务上。

  * **入站过滤器**Inbound Filters在路由到源之前执行，并且可以用于身份验证，路由和修饰请求之类的操作
  * **端点过滤器**Endpoint Filter可用于返回静态响应，否则内置`ProxyEndpoint`过滤器会将请求路由到源。
  * **出站过滤器**Outbound Filters在从源获取响应后执行，可用于度量标准，修饰用户的响应或添加自定义标头。

zuul.1基于Servlet2.5使用阻塞架构，每次IO操作都是从工作线程中选择一个执行，请求线程被阻塞到工作线程完成。

zuul.2基于Netty非阻塞和支持长连接，但spring cloud目前未整合。

### Spring Cloud GateWay

zuul已经不行了，尽量使用GateWay[官网](http://cloud.spring.io/spring-cloud-static/spring-cloud-gateway/2.2.1.RELEASE/reference/html)

WebFlux[官网](https://docs.spring.io/spring/docs/current/spring-framework-reference/web-reactive.html#spring-webflux)

概述：SpringCloud GateWay`基于WebFlux框架实现`，而WebFlux框架底层则使用了高性能的`Reactor模式通信框架Netty`。提供统一的路由方式且基于Filter链的形式提供了网关的基本功能。

#### 特性：

* 动态路由：可以匹配任何请求属性
* 可对路由指定Predicete断言和Filter过滤器
* 集成Hystrix的断路器功能
* 集成Spring Cloud 服务发现功能
* 请求限流
* 支持路径重写

#### 三大核心概念：

* Route：路由是构建网关的基本模块，由ID，目标URI，一系列断言和过滤器组成，若断言为true，则匹配该路由。
* Predicate：可以匹配HTTP请求中的所有内容，如果请求与断言匹配则进行路由。断言即匹配条件。
* Filter：对于请求在路由前和路由后对请求进行修改

#### GateWay工作流程

![image-20200620160109114](C:\Users\Administrator\AppData\Roaming\Typora\typora-user-images\image-20200620160109114.png)

客户端向Spring  Cloud GateWay发出请求，然后在Gateway Handler Mapping中找到与请求匹配的路由，将其发送到GateWay Web Handler。Handler再通过指定的过滤器链，请求再发送到实际的服务器。过滤器链中有pre，post

* pre：可以做参数检验、权限校验、流量监控、日志输出、协议转换等
* post：作响应内容、响应头的修改，日志的输出、流量监控。

#### 实践

* 新建module

* pom.xml中引入依赖spring-cloud-starter-gateway以及spring-cloud-starter-netflix-eureka-client

* application.yml文件

  ```
  server:
    port: 9527
  spring:
    application:
      name: cloud-gateway  #在注册中心中的名字，可标识一个服务集群，项目名称相同，为同一个微服务的节点
    cloud:
      gateway:
        routes:
          - id: payment
            uri: http://localhost:8001
            predicates:
              - Path=/payment/get/**
          - id: payment_2
            uri: http://localhost:8001
            predicates:
              - Path=/payment/lb/**
  eureka:
    client:
      service-url:
        defaultZone: http://eureka7001.com:7001/eureka/
  ```

  

* 主启动类，只需@SpringBootApplication注解

路由的配置也可使用编码配置。

```
@Configuration
public class GateWayConfig
{
 @Bean
    public RouteLocator getRouteLocator(RouteLocatorBuilder routeLocatorBuilder){
        RouteLocatorBuilder.Builder routes=routeLocatorBuilder.routes();
        routes.route("path_route_payment", r -> r.path("/guonei").uri("http://news.baidu.com/guonei"));
        return routes.build();
    }

}
```

##### 动态路由

提供服务名进行动态路由，修改yml文件

```
server:
  port: 9527
spring:
  application:
    name: cloud-gateway  #在注册中心中的名字，可标识一个服务集群，项目名称相同，为同一个微服务的节点
  cloud:
    gateway:
      discovery:
      	locator:
      		enabled: true#开启从注册中心动态创建路由的功能，利用微服务名进行路由
      routes:
        - id: payment
          uri: lb://cloud-payment-service#使用lb://serviceID
          predicates:
            - Path=/payment/get/**
        - id: payment_2
          uri: lb://cloud-payment-service
          predicates:
            - Path=/payment/lb/**
eureka:
  client:
    service-url:
      defaultZone: http://eureka7001.com:7001/eureka/
```

##### predicates的使用，GatewayFilter Factories

可看官网介绍

| route predicate         | 使用示例                                                     |
| ----------------------- | ------------------------------------------------------------ |
| After Route Predicate   | - After=2020-06-20T17:13:06.721889600+08:00[Asia/Shanghai]，在这个时间之后路由才能匹配 |
| Before Route Predicate  | - Before=2030-06-20T17:13:06.721889600+08:00[Asia/Shanghai]，在这个时间之前路由才能匹配 |
| Between Route Predicate | - Between=2020-06-20T17:13:06.721889600+08:00[Asia/Shanghai]，2030-06-20T17:13:06.721889600+08:00[Asia/Shanghai]，在这个时间之间路由才能匹配 |
| Cookie Route Predicate  | - Cookie=cookiename,正则表达式，获取对应的cookiename和用来匹配value正则表达式，匹配上才会路由，-Cookie=username，zzyy |
| Header Route Predicate  | - Header=X-Request-Id,\d+   两个参数，一个为请求头属性name，一个为用来匹配value正则表达式，匹配上才会路由 |
| Host Route Predicate    | - Host=\- Host=**.somehost.org,**.anotherhost.org    a list of host name patterns. The pattern is an Ant-style pattern with `.` as the separator |
| Method Route Predicate  | \- Method=GET,POST                                           |
| Path Route Predicate    | \- Path=/red/{segment},/blue/{segment}                       |
| Query Route Predicate   | \- Query=red, gree.   匹配查询参数                           |

```
过滤器加载顺序
Loaded RoutePredicateFactory [After]
Loaded RoutePredicateFactory [Before]
Loaded RoutePredicateFactory [Between]
Loaded RoutePredicateFactory [Cookie]
Loaded RoutePredicateFactory [Header]
Loaded RoutePredicateFactory [Host]
Loaded RoutePredicateFactory [Method]
Loaded RoutePredicateFactory [Path]
Loaded RoutePredicateFactory [Query]
Loaded RoutePredicateFactory [ReadBodyPredicateFactory]
Loaded RoutePredicateFactory [RemoteAddr]
Loaded RoutePredicateFactory [Weight]
Loaded RoutePredicateFactory [CloudFoundryRouteService]
```

##### Filter,GatewayFilter Factories

filter的生命周期：pre和post，分别可对请求和响应做处理。
filter的种类：GatewayFilter和Global Filter

###### 自定义过滤器

* implements GlobalFilter,Ordered(用于过滤器排序)

  ```
  @Bean
  public GlobalFilter customFilter() {
      return new CustomGlobalFilter();
  }
  
  public class CustomGlobalFilter implements GlobalFilter, Ordered {
  
      @Override
      //过滤的主要方法
      public Mono<Void> filter(ServerWebExchange exchange, GatewayFilterChain chain) {
          log.info("custom global filter");
          String uname=exchange.get
          return chain.filter(exchange);
      }
  
      @Override
      //设置过滤器优先级
      public int getOrder() {
          return -1;
      }
  }
  ```

## 服务配置： Config

每个微服务需要必要的配置信息才能运行，故一套集中式的、动态的配置管理设施是必不可少的。

SpringCloud Config为微服务架构中的微服务提供集中式的外部配置支持，配置服务器为各个不同微服务应用的所有环境提供了一个中心化的外部配置。如若多个微服务连接同一个外部mysql，这个mysql的配置则应集中管理。

Spring Cloud Config分为服务端和客户端，

* 服务端：也称为分布式配置中心，它是一个独立的微服务应用，用来连接配置服务器并为客户端提供配置信息，加密/解密信息等访问接口。
* 客户端：各个微服务，启动时，从配置中心获取和加载配置信息。
* 配置服务器默认采用git来存储配置信息，这样有助于对环境配置进行版本管理，并可通过git客户端工具来方便管理与访问配置内容。

作用：

* 集中管理配置文件
* 不同环境不同配置，动态化的配置更新，分环境部署如dev/test/prod/beta/release
* 允许期间动态调整配置，不再需要每个服务都编写配置文件，服务会向配置中心同意拉取用来配置的信息
* 当配置发生变动时，服务不许重启即可感知配置的变化并应用新的配置
* 配置信息以post接口暴露

与github整合配置

[官网](https://cloud.spring.io/spring-cloud-static/spring-cloud-config/2.2.3.RELEASE/reference/html/)

### ConfigServer

* 新建module，cloud-config-center

* pom引入依赖spring-cloud-starter-netflix-eureka-client和spring-cloud-config-server

* application.yml文件

  ```
  server:
    port: 3344
  spring:
    application:
      name: cloud-config-center
    cloud:
      config:
        server:
          git:
            uri: git@github.com:xc-hunter/springcloud-config.git
            ####搜索目录
            search-paths: 
              - springcloud-config
        #####读取分支
        label: master  
  eureka:
    client:
      service-url:
        defaultZone: http://localhost:7001/eureka
  ```

* 主启动类上@SpringBootApplication和@EnableConfigServer
  使用localhost:3344`/master/config-dev.yml`访问可以获取到github仓库上的文件。

* 配置读取github仓库path规则：

  * /{label}/{application}-{profile}.yml,规则解读为哪个分支上的哪个yml，推荐使用
  * /{application}-{profile}.yml，规则为默认读取master分支
  * /{application}/{profile}/{label}，

### 客户端

* 新建module
* pom中引入依赖spring-cloud-starter-config和spring-cloud-starter-netflix-eureka-client
* 不使用applicaiton.yml,使用bootstrap.yml
  * application.yml为用户级的资源配置项
  * bootstrap.yml是系统级的，优先级更加高，Spring Cloud会创建一个"BootstrapContext",作为ApplicationContext的父上下文，初始化时，BootstrapContext负责从外部源加载配置属性并解析配置，这两个上下文共享一个从外部获取的Environment。
    Bootstrap中的属性具有高优先级，默认情况下，他们不会被本地配置覆盖。且两个上下文有着不同的约定，所以新增一个bootstrap.yml文件，保证两上下文配置分离。
  * 加载时，由于bootstrap.yml为高优先级，会先被加载。

```
server:
  port: 3355
spring:
  application:
    name: config-client
  cloud:
    #客户端配置
    config:
      label: master #分支名称
      name: config #配置文件名称
      profile: dev #读取后缀名称，
      uri: http://localhost:3344 #配置中心地址
      #上面4个配置的结果是会去访问http://localhost:3344/master/config-dev.yml
eureka:
  client:
    service-url:
      defaultZone: http://localhost:7001/eureka
```

* 主启动类@SpringBootApplication

* 启动时出现问题，Unregistering application CONFIG-CLIENT with eureka with status DOWN，是缺少容器，引入spring-boot-starter-web依赖，而在服务端时不出现问题是因为服务端的config-server依赖引入了web依赖。

* 使用一个controler方法验证是否引入了配置，使用${}获取到config-dev.yml中的值。使用localhost:3355/configInfo验证

  ```
  @RestController
  public class ConfigController {
      @Value("${config.info}")
      private String configInfo;
      @GetMapping("/configInfo")
      public String getConfigInfo(){
          return configInfo;
      }
  }
  ```

### 动态刷新(手动版)

linux运维修改github上的配置文件，刷新配置服务端路径访问时，配置文件也修改了，因为服务端的路径访问配置文件其实是去Fetched远程仓库，不需重启就可更新。而配置客户端不更新，因为他是在应用启动时先去服务端拿到了配置，再开启服务，必须得重启才会更新。

* 解决配置客户端的动态更新：修改配置客户端模块

  * 映入spring-boot-starter-actuator依赖监控

  * 修改yml，暴露监控端点management.endpoints.web.exposure.include="*",

  * 在业务类controller上加@RefreshScope

  * 客户端配置完成，但仍需要外部帮助

    * 运维在修改远程github上的配置文件后，需要对客户端进行一次刷的操作，

      ```
      curl -X POST "http://客户端地址/actuator/refresh"
      ```

但这种动态刷新仍很麻烦，但服务很多或者定点某些服务更改配置时，不是很好的解决方案，于是引入消息总线。Spring Cloud Bus结合Spring Cloud Config可实现配置的动态刷新。

## 消息总线:Spring Cloud Bus

Spring Cloud Bus是用来将分布式系统的节点与轻量级消息系统连接起来的框架，整合了Java的事件处理机制和消息中间件的功能。可用于广播状态更改、事件推送等，也可当作微服务间的通讯通道。

总线：在微服务架构的系统中，通常会使用轻量级的消息代理来构建一个共用的消息主题，并让系统中的所有微服务实例都连接上来。由于该主题中产生的消息会被所有实例监听和配置，故称为消息总线。在总线上的实例，都可广播需要让其他实例知道的消息。

基本原理：ConfigClient实例都监听MQ中同一个Topic(默认为springcloudbus)。当一个服务刷新数据的时候，会将信息放入到Topic中，这样其他监听同一Topic的服务就能得到通知，然后去更新自己的配置。

Bus支持两种消息代理：

* RabbitMQ：
* Kafaka：

### 设计思想：

#### 动态刷新广播通知

利用消息总线触发一个服务端的/bus/refresh端点，而刷新所有客户端的配置

* 在配置ConfigServer端和客户端添加消息总线支持

* ConfigServer端

  ```
  <!--添加消息总线RabbitMQ支持-->
          <dependency>
              <groupId>org.springframework.cloud</groupId>
              <artifactId>spring-cloud-starter-bus-amqp</artifactId>
          </dependency>
  yml中添加
  #rabbitmq相关配置
  spring：
    rabbitmq:
      host: 192.168.3.7
      port: 5672   ## 不是15672
      username: 。。。
      password: 。。。
  #暴露端点
  management:
    endpoints:
      web:
        exposure:
          include: 'bus-refresh'
  ```

* ​	ConfigClient端

```
<!--添加消息总线支持-->
        <dependency>
            <groupId>org.springframework.cloud</groupId>
            <artifactId>spring-cloud-starter-bus-amqp</artifactId>
        </dependency>
yml中
#rabbitmq相关配置
spring：
  rabbitmq:
    host: 192.168.3.7
    port: 5672  ## 不是15672
    username: 。。。
    password: 。。。
```

配置完成后，现在的操作为：

* 运维：修改github上的配置文件，针对配置中心及配置服务端发送post请求curl -X POST "http://服务端地址/actuator/bus-refresh",
* curl之后，个人观察，配置服务端会按属性文件的配置去fetch远程github，被通知的客户端会去fetch服务端，转而得到对应配置文件远程github地址。

#### 动态刷新定点通知

不需要广播通知时，需要精确通知。

指定具体某个实例生效，即被通知，

* 公式  curl -X POST "http://服务端地址/actuator/bus-fresh`/{destination}`"
* 通过destination参数指定需要更新配置的服务或实例，这个参数例子：  /serviceId:port    

## 消息驱动：Spring Cloud Stream

解决问题：屏蔽底层消息中间件的差异，降低切换成本，`统一消息的编程模型`。解耦合。

概述：一个构建消息驱动微服务的框架，

* 应用程序通过inputs或者outputs来与Spring Cloud Stream 中binder对象交互。
* 通过配置来binding，而Spring Cloud Stream的binder对象负责与消息中间件交互
* 通过使用Spring Integration来连接消息代理中间件以实现消息事件驱动
* Spring Cloud Stream为一些消息中间件提供了个性化的自动化配置实现，引用了发布-订阅、消费组、分区的三个核心概念。
* 目前仅支持Kafka、RabbitMQ
* 实际使用时，弄清楚如何与Sping Cloud Stream交互就可方便的使用消息驱动的方式。

参考见[官网](https://spring.io/projects/spring-cloud-stream)

设计思想：通过定义绑定器Binder作为中间层，实现了应用程序与消息中间件细节之间的隔离。

![image-20200622095956825](C:\Users\Administrator\AppData\Roaming\Typora\typora-user-images\image-20200622095956825.png)

### 标准流程

![image-20200622101447828](C:\Users\Administrator\AppData\Roaming\Typora\typora-user-images\image-20200622101447828.png)

* Binder：连接中间件，屏蔽差异
* Channel：通道，是队列的一种抽象，在消息通讯系统中是实现存储和转发的媒介
* Source和Sink：输出和输入
* 注解：
  * @Input：标识输入通道，通过该输入通道接收到的消息进入应用程序
  * @Output：标识输出通道，发布的消息通过该通道离开应用程序
  * `@StreamListener`：监听队列，用于消费者端，消息接收
  * `@EnableBinding`：指信道channel和exchange绑定在一起

### 案例说明

* 消息发送端：

  * 新建module，cloud-stream-rabbirmq-provider

  * pom新引入spring-cloud-starter-stream-rabbitmq依赖，其他通用依赖同前，

  * yml文件

    ```
    server:
      port: 8801
    spring:
      application:
        name: cloud-stream-provider  #在注册中心中的名字，可标识一个服务集群，项目名称相同，为同一个微服务的节点
    
      cloud:
        stream:
          binders: #在此处配置要绑定的rabbitmq的服务消息
            defaultRabbit: #表示binder的名称，用于binding整合
              type: rabbit  #消息中间件类型
              environment:  #设置rabbitmq的相关环境配置
                spring:
                  rabbitmq:
                    host: 192.168.3.7
                    port: 5672
                    username: ...
                    password: ...
          bindings:  #服务的整合处理
            output:
              destination: studyExchange  #表示要使用的exchange名称定义
              content-type: application/json #设置消息类型，为文本时设置/text/json
              binder: defaultRabbit  #绑定到上面的binder
    eureka:
      client:
        register-with-eureka: true  #表示注册到注册中心
        fetch-registry: true
        service-url:
          defaultZone: http://eureka7001.com:7001/eureka/#注册中心地址
      instance:
        instance-id: send-8801.com
        prefer-ip-address: true
        lease-expiration-duration-in-seconds: 5  #设置最近的一次心跳之后，下一次心跳的最晚发送间隔,默认90秒
        lease-renewal-interval-in-seconds: 2 #设置心跳的时间间隔，默认30s
    ```

  * 主启动类，只需@SpringBootApplication

  * 定义service接口IMessageProvider，一个send方法。

  * 接着新建类实现接口，类上添加@EnableBinding(Source.class)注解进行绑定，表示其为发送方，也进行了channel和exchange的绑定,这个相当于一个消息生产服务。其中MseesgeChannel为关键Bean。

    ```
    @EnableBinding(Source.class) //表示当前这个类是source，负责生产消息，并且发送给channel
    public class IMessageProviderImpl implements IMessageProvider {
        @Autowired
        private MessageChannel output;  //channel,将信息发送这个channel
        @Override
        public String send() {
            String serial= UUID.randomUUID().toString();
            output.send(MessageBuilder.withPayload(serial).build()); //build方法会创建一个build类
            System.out.println("******serial:"+serial);
            return serial;
        }
    }
    ```

  * controller中新建类，导入消息生产服务，进行消息发送

* 消息接受端

  * 新建module，cloud-stream-rabbirmq-provider

  * pom新引入spring-cloud-starter-stream-rabbitmq依赖，其他通用依赖同前，

  * yml文件

    ```
    server:
      port: 8802
    spring:
      application:
        name: cloud-stream-consumer  #在注册中心中的名字，可标识一个服务集群，项目名称相同，为同一个微服务的节点
    
      cloud:
        stream:
          binders: #在此处配置要绑定的rabbitmq的服务消息
            defaultRabbit: #表示binder的名称，用于binding整合
              type: rabbit  #消息中间件类型
              environment:  #设置rabbitmq的相关环境配置
                spring:
                  rabbitmq:
                    host: 192.168.3.7
                    port: 5672
                    username: xiechao
                    password: xiechao
          bindings:  #服务的整合处理
            input:  #input表示其为接受端
              destination: studyExchange  #表示要使用的exchange名称定义
              content-type: application/json #设置消息类型，为文本时设置/text/json
              binder: defaultRabbit  #绑定到上面的binder
    eureka:
      client:
        register-with-eureka: true  #表示注册到注册中心
        fetch-registry: true
        service-url:
          defaultZone: http://eureka7001.com:7001/eureka/#注册中心地址
      instance:
        instance-id: send-8801.com
        prefer-ip-address: true
        lease-expiration-duration-in-seconds: 5  #设置最近的一次心跳之后，下一次心跳的最晚发送间隔,默认90秒
        lease-renewal-interval-in-seconds: 2 #设置心跳的时间间隔，默认30s
    ```

  * 主启动类没做改变，controller类上

    * 重要的注解为`@EnableBinding(Sink.class)，@StreamListener(Sink.INPUT) `

    ```
    @RestController
    @EnableBinding(Sink.class)//启用绑定，表示当前类为sink，负责接收channel发送过来的消息进行消费
    public class ReceiveMessageListenerController {
        @Value("${server.port}")
        private String serverport;
        @StreamListener(Sink.INPUT)  //指定监听sink.input，input在配置文件中已配置，绑定在一个指定Exchange上
        public void input(Message<String> message){
            System.out.println("消费组1号，------>接收到的消息："+message.getPayload()+"\t port:"+serverport);
        }
    }
    
    ```

#### 消息重复消费解决

有的消息只允许被消费一次，故需要限制消费端。

##### Stream中的消息分组

Stream中处于同一个group中的多个消费组是竞争关系，可保证消息只会被其中一个应用消费一次。不同组是可以全面消费的。不指定组时，`默认分给新组`,即消费者消费时默认属于不同组。

```
bindings:  #服务的整合处理
        input:  #input表示其为接受端
          destination: studyExchange  #表示要使用的exchange名称定义
          content-type: application/json #设置消息类型，为文本时设置/text/json
          binder: defaultRabbit  #绑定到上面的binde
          group:  #######此处为新增内容，可指定其绑定的组。
```

自己实验时，感觉消费信息是同一个组轮询消费。

#### 消息持久化

客户端当绑定到一个Exchange后，如果下机且未指定在RabbitMQ中exchange中已有的group，服务端在此时发送消息，消息会存到Exchange中的不同group的消息目的地，客户端重新上线，但由于没有绑定到已有的group，不会接受到服务端在其下线期间发送的消息。若客户端绑定到已有的group，会接受到服务端在其下线期间发送的消息。

#### 补充：queue

destination对应于RabbitMQ中的exchange,另外可设置group，如果设置了group，那么 group 名称就会成为 queue 的名称，如果没有设置 group ，那么 queue 就会根据 destination + 随机字符串的方式命名。发送和接受时最终还是消息到queque上。

## 请求链路跟踪：Spring Cloud Sleuth

在微服务框架中，一个由客户端发起的请求在后端系统中会经过多个不同的服务节点调用来协同产生最后的请求结果，每一个前端请求都会形成一条复杂的分布式服务调用链路，链路中的任何一环出现高延时或错误都会引起整个请求的失败。

[reference](https://github.com/spring-cloud/spring-cloud-sleuth)

Spring Cloud Sleuth提供了一套完整的服务跟踪的解决方案，兼容支持了zipkin(图形化展示链路调用过程，负责展现)。

zipkin的使用，：

* zipkin的使用在SpringCloud从F版起已不需要自己构建zipkin Server，只需调用jar包即可。[下载zipkin](https://dl.bintray.com/openzipkin/maven/io/zipkin/java/zipkin-server)

* 运行jar,开启zipkin服务端
* 运行控制台

![image-20200622155713298](C:\Users\Administrator\AppData\Roaming\Typora\typora-user-images\image-20200622155713298.png)

![image-20200622155759761](C:\Users\Administrator\AppData\Roaming\Typora\typora-user-images\image-20200622155759761.png)

### 使用

* 已有项目引入spring-cloud-starter-zipkin的依赖，包含了sleuth和zipkin

* yml文件修改

  ```
  spring:
    zipkin:
      base-url:http://localhost:9411 ##指定zipkin地址
      sleuth：
        sampler：
          probability：1 #采样率值，介于0、1之间，为1表示全部采集。
  ```

  

* 在zipkin的bashboard中，输入服务名，span名称等信息展示

# PARTⅠ end




