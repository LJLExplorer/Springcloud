# Springboot3+Springcloud2023.0.5+Springcloud alibaba2023.0.3.2学习及踩坑记录

Spingboot3.2.12版本，对应springCloud2023.0.5，

springboot与springcloud版本对应：https://spring.io/projects/spring-cloud#overview

springcloud与springcloud alibaba版本对应：https://sca.aliyun.com/docs/2023/overview/version-explain/?spm=5238cd80.596123a.0.0.38965fc6Ffa0gd

nacos版本2.4.3 配置文件改了，否则读取不到（Issues链接）：https://github.com/alibaba/nacos/issues/12908

Spingboot3.4.1版本与idea中的gradle插件冲突无法启动，Spingboot3.3.7版本与eureka server冲突，无法启动![image-20250101203911539](src/main/resources/image/image-20250101203911539.png)

## Eureka注册与发现(现在常用的是Nacos,Eureka 2版本已经不在维护，后续会将注册中心换为Nacos)

假设有三个服务，Eureka注册中心，生产者（用户服务）、消费者

### 父工程

###### pom

```
<?xml version="1.0" encoding="UTF-8"?>
<project xmlns="http://maven.apache.org/POM/4.0.0" xmlns:xsi="http://www.w3.org/2001/XMLSchema-instance"
	xsi:schemaLocation="http://maven.apache.org/POM/4.0.0 https://maven.apache.org/xsd/maven-4.0.0.xsd">
	<modelVersion>4.0.0</modelVersion>
	<parent>
		<groupId>org.springframework.boot</groupId>
		<artifactId>spring-boot-starter-parent</artifactId>
		<version>3.2.12</version>
		<relativePath/> <!-- lookup parent from repository -->
	</parent>
	<groupId>com.ljl</groupId>
	<artifactId>springcloud</artifactId>
	<version>0.0.1-SNAPSHOT</version>
	<packaging>pom</packaging>
	<name>springcloud</name>
	<description>Demo project for Spring Boot</description>
	<url/>
	<licenses>
		<license/>
	</licenses>
	<developers>
		<developer/>
	</developers>
	<scm>
		<connection/>
		<developerConnection/>
		<tag/>
		<url/>
	</scm>
	<properties>
		<java.version>17</java.version>
		<spring-cloud.version>2023.0.5</spring-cloud.version>
	</properties>
	<dependencies>

		<dependency>
			<groupId>org.projectlombok</groupId>
			<artifactId>lombok</artifactId>
			<version>1.18.30</version>
			<scope>provided</scope>
<!--			<optional>true</optional>-->
		</dependency>
		<dependency>
			<groupId>org.springframework.boot</groupId>
			<artifactId>spring-boot-starter-test</artifactId>
			<scope>test</scope>
		</dependency>
		<dependency>
            <groupId>org.springframework.boot</groupId>
            <artifactId>spring-boot-starter</artifactId>
    </dependency>
	</dependencies>

	<dependencyManagement>
		<dependencies>
			<dependency>
				<groupId>org.springframework.cloud</groupId>
				<artifactId>spring-cloud-dependencies</artifactId>
				<version>${spring-cloud.version}</version>
				<type>pom</type>
				<scope>import</scope>
			</dependency>
		</dependencies>
	</dependencyManagement>

	<build>
		<plugins>
			<plugin>
				<groupId>org.springframework.boot</groupId>
				<artifactId>spring-boot-maven-plugin</artifactId>
				<configuration>
					<excludes>
						<exclude>
							<groupId>org.projectlombok</groupId>
							<artifactId>lombok</artifactId>
						</exclude>
					</excludes>
				</configuration>
			</plugin>
		</plugins>
	</build>
</project>
```



### 生产者

###### pom

```
<?xml version="1.0" encoding="UTF-8"?>
<project xmlns="http://maven.apache.org/POM/4.0.0"
         xmlns:xsi="http://www.w3.org/2001/XMLSchema-instance"
         xsi:schemaLocation="http://maven.apache.org/POM/4.0.0 http://maven.apache.org/xsd/maven-4.0.0.xsd">
    <modelVersion>4.0.0</modelVersion>
    <parent>
        <groupId>com.ljl</groupId>
        <artifactId>springcloud</artifactId>
        <version>0.0.1-SNAPSHOT</version>
    </parent>

    <artifactId>user-service</artifactId>

    <properties>
        <maven.compiler.source>17</maven.compiler.source>
        <maven.compiler.target>17</maven.compiler.target>
        <project.build.sourceEncoding>UTF-8</project.build.sourceEncoding>
    </properties>

    <dependencies>
        <dependency>
            <groupId>org.mybatis.spring.boot</groupId>
            <artifactId>mybatis-spring-boot-starter</artifactId>
            <version>3.0.4</version>
        </dependency>
        <dependency>
            <groupId>com.mysql</groupId>
            <artifactId>mysql-connector-j</artifactId>
            <scope>runtime</scope>
        </dependency>
        <dependency>
          <groupId>org.springframework.boot</groupId>
          <artifactId>spring-boot-starter-web</artifactId>
				</dependency>
    </dependencies>
</project>
```

###### application.yml

```
server:
  port: ${port:9001}
spring:
  application:
    # 注册的服务id
    name: user-service
  datasource:
    driver-class-name: com.mysql.cj.jdbc.Driver
    username: root
    password: test
    url: jdbc:mysql://127.0.0.1:3306/springcloud?characterEncoding=utf-8&useSSL=false
  jackson:
    serialization:
      WRITE_DATES_AS_TIMESTAMPS: false
      FAIL_ON_EMPTY_BEANS: true
    time-zone: UTC
mybatis:
  type-aliases-package: com.ljl.user.domain
  mapper-locations: classpath:mapper/*.xml
logging:
  level:
    com: info
```

### 消费者

###### pom

```
<?xml version="1.0" encoding="UTF-8"?>
<project xmlns="http://maven.apache.org/POM/4.0.0"
         xmlns:xsi="http://www.w3.org/2001/XMLSchema-instance"
         xsi:schemaLocation="http://maven.apache.org/POM/4.0.0 http://maven.apache.org/xsd/maven-4.0.0.xsd">
    <modelVersion>4.0.0</modelVersion>
    <parent>
        <groupId>com.ljl</groupId>
        <artifactId>springcloud</artifactId>
        <version>0.0.1-SNAPSHOT</version>
    </parent>

    <artifactId>consumer</artifactId>

    <properties>
        <maven.compiler.source>17</maven.compiler.source>
        <maven.compiler.target>17</maven.compiler.target>
        <project.build.sourceEncoding>UTF-8</project.build.sourceEncoding>
    </properties>
    <dependencies>
        <dependency>
            <groupId>junit</groupId>
            <artifactId>junit</artifactId>
            <scope>test</scope>
        </dependency>
        <dependency>
          <groupId>org.springframework.boot</groupId>
          <artifactId>spring-boot-starter-web</artifactId>
				</dependency>
    </dependencies>

</project>
```

###### ConsumerApplication

```
import org.springframework.boot.SpringApplication;
import org.springframework.context.annotation.Bean;
import org.springframework.web.client.RestTemplate;

@SpringBootApplication
public class ConsumerApplication {

    @Bean
    public RestTemplate restTemplate() {
        return new RestTemplate();
    }

    public static void main(String[] args) {
        SpringApplication.run(ConsumerApplication.class, args);
    }
}
```

###### ConsumerController

```
@RestController
@RequestMapping("/consumer")
public class ConsumerController {

  @Autowired
  private RestTemplate restTemplate;
  
  @GetMapping("/{id}")
  public User queryById(@PathVariable Long id){
    String url =
    "HTTP://localhost:9001/user/" + id;
    return restTemplate.getForObject(url, User.class);
  }
}
```

访问 http://localhost:8080/consume/7

### 服务注册中心

###### pom

```
<?xml version="1.0" encoding="UTF-8"?>
<project xmlns="http://maven.apache.org/POM/4.0.0"
         xmlns:xsi="http://www.w3.org/2001/XMLSchema-instance"
         xsi:schemaLocation="http://maven.apache.org/POM/4.0.0 http://maven.apache.org/xsd/maven-4.0.0.xsd">
    <modelVersion>4.0.0</modelVersion>
    <parent>
        <groupId>com.ljl</groupId>
        <artifactId>springcloud</artifactId>
        <version>0.0.1-SNAPSHOT</version>
    </parent>

    <artifactId>eureka-server</artifactId>

    <properties>
        <maven.compiler.source>17</maven.compiler.source>
        <maven.compiler.target>17</maven.compiler.target>
        <project.build.sourceEncoding>UTF-8</project.build.sourceEncoding>
    </properties>

    <dependencies>
        <dependency>
            <groupId>org.springframework.cloud</groupId>
            <artifactId>spring-cloud-starter-netflix-eureka-server</artifactId>
        </dependency>
    </dependencies>


    <build>
        <plugins>
            <plugin>
                <groupId>org.springframework.boot</groupId>
                <artifactId>spring-boot-maven-plugin</artifactId>
            </plugin>
        </plugins>
    </build>
</project>
```

###### application.yml

```
server:
  port: ${port:10086}
spring:
  application:
    name: eureka-server
eureka:
  client:
    service-url:
      # eureka 服务地址，如果是集群的话；需要指定其它集群eureka地址
      defaultZone: ${defaultZone:http://127.0.0.1:10086/eureka}
    # 不注册自己
    register-with-eureka: false
    # 不拉取服务
    fetch-registry: false
```

###### EurekaServerApplication

```
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.cloud.netflix.eureka.server.EnableEurekaServer;

// 声明是Eureka服务中心
@EnableEurekaServer
@SpringBootApplication
public class EurekaServerApplication {
    public static void main(String[] args) {
        SpringApplication.run(EurekaServerApplication.class, args);
    }
}
```

访问 http://127.0.0.1:10086/

#### 服务注册

user-service上添加eureka的客户端依赖 并且自动注册到eureka服务端上

###### pom

```
	<dependency>
    <groupId>org.springframework.cloud</groupId>
    <artifactId>spring-cloud-starter-netflix-eureka-client</artifactId>
  </dependency>
```

###### UserApplication

```
// 开启Eureka服务端
@MapperScan("com.ljl.user.mapper")
@SpringBootApplication
@EnableDiscoveryClient
public class UserApplication {
    public static void main(String[] args) {
        SpringApplication.run(UserApplication.class, args);
    }
}
```

###### application.yml添加配置

```
server:
  port: ${port:9001}
spring:
  application:
    # 注册的服务id
    name: user-service
  datasource:
    driver-class-name: com.mysql.cj.jdbc.Driver
    username: root
    password: test
    url: jdbc:mysql://127.0.0.1:3306/springcloud?characterEncoding=utf-8&useSSL=false
  jackson:
    serialization:
      WRITE_DATES_AS_TIMESTAMPS: false
      FAIL_ON_EMPTY_BEANS: true
    time-zone: UTC
mybatis:
  type-aliases-package: com.ljl.user.domain
  mapper-locations: classpath:mapper/*.xml
eureka:
  client:
    service-url:
      defaultZone: HTTP://127.0.0.1:10086/eureka
    register-with-eureka: true
  instance:
    ip-address: 127.0.0.1
    prefer-ip-address: true
    # 续约时间 心跳
    lease-renewal-interval-in-seconds: 30
    # 失效时间
    lease-expiration-duration-in-seconds: 90
logging:
  level:
    com: info
```

#### 服务发现

consumer上添加依赖注册到eureka上

###### pom

```
 <dependency>
   <groupId>org.springframework.cloud</groupId>
   <artifactId>spring-cloud-starter-netflix-eureka-client</artifactId>
 </dependency>
```

###### application.yml

```
spring:
  application:
    name: consumer
eureka:
  client:
    service-url:
      defaultZone: http://127.0.0.1:10086/eureka
logging:
  level:
    io.github.resilience4j: DEBUG
    org.springframework.web.client: DEBUG
    com.ljl.consumer: DEBUG
    org.springframework.cloud.circuitbreaker: DEBUG


```

###### ConsumerApplication

```
import com.ljl.consumer.config.CustomLoadBalancerConfiguration;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.cloud.client.discovery.EnableDiscoveryClient;
import org.springframework.cloud.client.loadbalancer.LoadBalanced;
import org.springframework.cloud.loadbalancer.annotation.LoadBalancerClient;
import org.springframework.cloud.loadbalancer.annotation.LoadBalancerClients;
import org.springframework.cloud.openfeign.EnableFeignClients;
import org.springframework.context.annotation.Bean;
import org.springframework.web.client.RestTemplate;

@SpringBootApplication
@EnableDiscoveryClient
public class ConsumerApplication {

    @Bean
    public RestTemplate restTemplate() {
        return new RestTemplate();
    }

    public static void main(String[] args) {
        SpringApplication.run(ConsumerApplication.class, args);
    }
}
```

###### ConsumerController

```
import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.ljl.consumer.domain.dto.User;
import jakarta.annotation.Resource;
import lombok.extern.slf4j.Slf4j;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.client.RestTemplate;

import static com.ljl.consumer.utils.constant.LimterConstant.*;


@Slf4j
@RestController
@RequestMapping("/consumer")
public class ConsumerController {

    private Logger logger = LoggerFactory.getLogger(ConsumerController.class);

    @Resource
    private RestTemplate restTemplate;

    @GetMapping("/user/{id}")
    public User queryById(@PathVariable Long id) throws JsonProcessingException {
       List<ServiceInstance> serviceInstances =
         discoveryClient.getInstances("user-service");
         ServiceInstance serviceInstance = serviceInstances.get(0);
       String url =
         "http://" + serviceInstance.getHost() + ":" +
         serviceInstance.getPort() + "/user/" + id;
         return restTemplate.getForObject(url, User.class);
    }

    // 回退方法，当熔断器触发时调用
    public User fallback(Long id, Throwable t) {
        logger.error("服务不可用，进入回退方法: {}", t.getMessage());
        User user = new User();
        user.setId(id);
        if( t instanceof RequestNotPermitted)
            user.setName("请求过于频繁，请稍后再试。");
        else
            user.setName("服务暂时不可用，请稍后再试。");
        return user;
    }
}
```

###### 生产者需要提供心跳

###### application.yml

```java
eureka:
  client:
    service-url:
      defaultZone: HTTP://127.0.0.1:10086/eureka
    register-with-eureka: true
  instance:
    ip-address: 127.0.0.1
    prefer-ip-address: true
    # 续约时间 心跳
    lease-renewal-interval-in-seconds: 30
    # 失效时间
    lease-expiration-duration-in-seconds: 90
```

###### 消费者拉取最新服务列表

###### consumer端 application.yml

```
eureka:
  client:
    service-url:
      defaultZone: http://127.0.0.1:10086/eureka
    # 每隔30秒 会重新拉取并更新数据,为了得到服务最新状态
    registry-fetch-interval-seconds: 30
```



#### 续约与失败剔除和关闭自我保护（关闭自我保护确保可以成功剔除失效的，开发阶段关闭 投入使用时开启）

##### 服务下线：

当服务进行正常关闭操作时，它会触发一个服务下线的REST请求给Eureka Server，告诉服务注册中

心：“我要下线了”。服务中心接受到请求之后，将该服务置为下线状态

##### 失效剔除

有时我们的服务可能由于内存溢出或网络故障等原因使得服务不能正常的工作，而服务注册中心并未收

到“服务下线”的请求。相对于服务提供者的“服务续约”操作，服务注册中心在启动时会创建一个定时任

务，默认每隔一段时间（默认为60秒）将当前清单中超时（默认为90秒）没有续约的服务剔除，这个操

作被称为失效剔除。 可以通过 eureka.server.eviction-interval-timer-in-ms 参数对其进行修改，单位是毫秒。

##### 自我保护

我们关停一个服务，就会在Eureka面板看到一条警告：

![Snipaste_2025-01-05_19-10-47](src/main/resources/image/Snipaste_2025-01-05_19-10-47.png)

这是触发了Eureka的自我保护机制。当一个服务未按时进行心跳续约时，Eureka会统计最近15分钟心跳

失败的服务实例的比例是否超过了85%，当EurekaServer节点在短时间内丢失过多客户端（可能发生了

网络分区故障）。在生产环境下，因为网络延迟等原因，心跳失败实例的比例很有可能超标，但是此时

就把服务剔除列表并不妥当，因为服务可能没有宕机。Eureka就会把当前实例的注册信息保护起来，不

予剔除。生产环境下这很有效，保证了大多数服务依然可用。

但是这给我们的开发带来了麻烦， 因此开发阶段我们都会关闭自我保护模式：

###### eureka-server端application.yml

```
eureka:
  client:
    service-url:
      # eureka 服务地址，如果是集群的话；需要指定其它集群eureka地址
      defaultZone: ${defaultZone:http://127.0.0.1:10086/eureka}
    # 不注册自己
    register-with-eureka: false
    # 不拉取服务
    fetch-registry: false
  server:
    #    失效剔除时间 ms
    eviction-interval-timer-in-ms: 1000
    #    关闭自我保护模式
    #    当一个服务未按时进行心跳续约时，Eureka会统计最近15分钟心跳
    #    失败的服务实例的比例是否超过了85%，当EurekaServer节点在短时间内丢失过多客户端（可能发生了
    #    网络分区故障）。在生产环境下，因为网络延迟等原因，心跳失败实例的比例很有可能超标，但是此时
    #    就把服务剔除列表并不妥当，因为服务可能没有宕机。Eureka就会把当前实例的注册信息保护起来，不
    #    予剔除。生产环境下这很有效，保证了大多数服务依然可用。
    enable-self-preservation: false
```



## 负载均衡

springboot3中ribbon（负责负载均衡的）被弃用，使用spring-cloud-starter-loadbalancer进行负载均衡，修改负载策略只能通过配置类修改

###### pom

```
    <dependency>
      <groupId>org.springframework.cloud</groupId>
      <artifactId>spring-cloud-starter-loadbalancer</artifactId>
    </dependency>
```

###### CustomLoadBalancerConfiguration

```
import org.springframework.cloud.client.ServiceInstance;
import org.springframework.cloud.loadbalancer.core.RandomLoadBalancer;
import org.springframework.cloud.loadbalancer.core.ReactorLoadBalancer;
import org.springframework.cloud.loadbalancer.core.ServiceInstanceListSupplier;
import org.springframework.cloud.loadbalancer.support.LoadBalancerClientFactory;
import org.springframework.context.annotation.Bean;
import org.springframework.core.env.Environment;

public class CustomLoadBalancerConfiguration {

    @Bean
    ReactorLoadBalancer<ServiceInstance> randomLoadBalancer(Environment environment,
                                                            LoadBalancerClientFactory loadBalancerClientFactory) {
        String name = environment.getProperty(LoadBalancerClientFactory.PROPERTY_NAME);
        return new RandomLoadBalancer(loadBalancerClientFactory
                .getLazyProvider(name, ServiceInstanceListSupplier.class),
                name);
    }
}
```

###### ConsumerApplication

```
package com.ljl.consumer;

import com.ljl.consumer.config.CustomLoadBalancerConfiguration;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.cloud.client.discovery.EnableDiscoveryClient;
import org.springframework.cloud.client.loadbalancer.LoadBalanced;
import org.springframework.cloud.loadbalancer.annotation.LoadBalancerClient;
import org.springframework.context.annotation.Bean;
import org.springframework.web.client.RestTemplate;


@SpringBootApplication
@EnableDiscoveryClient
@LoadBalancerClient(value = "user-service", configuration = CustomLoadBalancerConfiguration.class)
public class ConsumerApplication {

    @Bean
    @LoadBalanced
    public RestTemplate restTemplate() {
        return new RestTemplate();
    }

    public static void main(String[] args) {
        SpringApplication.run(ConsumerApplication.class, args);
    }

}

```

value是被负载均衡的名，configuration就是对应写的类

修改策略教程：https://www.cnblogs.com/itxiaoshen/p/16247702.html

 [微服务生态组件之Spring Cloud LoadBalancer详解和源码分析 - itxiaoshen - 博客园.html](Notes/Spring Cloud LoadBalancer详解/微服务生态组件之Spring Cloud LoadBalancer详解和源码分析 - itxiaoshen - 博客园.html) 

测试时开了两个user-service，复制后改vm运行参数 `-Dport=9002` (意思就是更换端口运行第二个user-service)

## Resilience4j 可做熔断器和限流器：

Hystrix也被弃用，推荐使用**Resilience4j**

###### pom

```
				<!-- Resilience4j Spring Boot Starter -->
        <dependency>
            <groupId>org.springframework.cloud</groupId>
            <artifactId>spring-cloud-starter-circuitbreaker-resilience4j</artifactId>
        </dependency>
        <dependency>
            <groupId>io.github.resilience4j</groupId>
            <artifactId>resilience4j-spring-boot3</artifactId>
        </dependency>
        <!-- 由于断路保护等需要AOP实现，所以必须导入AOP包 -->
        <dependency>
            <groupId>org.springframework.boot</groupId>
            <artifactId>spring-boot-starter-aop</artifactId>
        </dependency>
```

#####  consumer总的pom

```
<?xml version="1.0" encoding="UTF-8"?>
<project xmlns="http://maven.apache.org/POM/4.0.0"
         xmlns:xsi="http://www.w3.org/2001/XMLSchema-instance"
         xsi:schemaLocation="http://maven.apache.org/POM/4.0.0 http://maven.apache.org/xsd/maven-4.0.0.xsd">
    <modelVersion>4.0.0</modelVersion>
    <parent>
        <groupId>com.ljl</groupId>
        <artifactId>springcloud</artifactId>
        <version>0.0.1-SNAPSHOT</version>
    </parent>

    <artifactId>consumer</artifactId>

    <properties>
        <maven.compiler.source>17</maven.compiler.source>
        <maven.compiler.target>17</maven.compiler.target>
        <project.build.sourceEncoding>UTF-8</project.build.sourceEncoding>
    </properties>
    <dependencies>
        <dependency>
            <groupId>org.springframework.cloud</groupId>
            <artifactId>spring-cloud-starter-netflix-eureka-client</artifactId>
        </dependency>
        <dependency>
            <groupId>org.springframework.cloud</groupId>
            <artifactId>spring-cloud-starter-loadbalancer</artifactId>
        </dependency>
        <dependency>
            <groupId>junit</groupId>
            <artifactId>junit</artifactId>
            <scope>test</scope>
        </dependency>
        <dependency>
          <groupId>org.springframework.boot</groupId>
          <artifactId>spring-boot-starter-web</artifactId>
				</dependency>
        <!-- Resilience4j Spring Boot Starter -->
        <dependency>
            <groupId>org.springframework.cloud</groupId>
            <artifactId>spring-cloud-starter-circuitbreaker-resilience4j</artifactId>
        </dependency>
        <dependency>
            <groupId>io.github.resilience4j</groupId>
            <artifactId>resilience4j-spring-boot3</artifactId>
        </dependency>
        <!-- 由于断路保护等需要AOP实现，所以必须导入AOP包 -->
        <dependency>
            <groupId>org.springframework.boot</groupId>
            <artifactId>spring-boot-starter-aop</artifactId>
        </dependency>
        <!-- feign调用 -->
        <dependency>
            <groupId>org.springframework.cloud</groupId>
            <artifactId>spring-cloud-starter-openfeign</artifactId>
        </dependency>
    </dependencies>
</project>
```



### 熔断器

###### Application.yml

```
resilience4j:
	# 熔断器
	circuitbreaker:
    # 定义不同的熔断器实例
    instances:
      # 熔断器名称
      myCircuitBreaker:
        base-config: default
    configs:
      default:
        # 是否注册健康指示器 用于监控
        register-health-indicator: true
        # 滑动窗口大小 用于统计失败率
        sliding-window-size: 10
        # 触发熔断器的前的最小调用次数
        minimum-number-of-calls: 3
        # 失败率阈值 超过该值断路器将打开
        failure-rate-threshold: 50
        # 断路器打开后等待的秒
        wait-duration-in-open-state: 10s
        # 半开状态下允许的调用次数
        permitted-number-of-calls-in-half-open-state: 3
        # 是否自动从打开状态过渡到半开状态
        automatic-transition-from-open-to-half-open-enabled: true
        # 捕获所有异常
        record-exceptions:
          - java.lang.Exception
          - java.lang.RuntimeException
          - java.lang.Throwable
```

###### ConsumerApplication

<!--需要加LoadBalanced才能开启熔断器-->

```
import com.ljl.consumer.config.CustomLoadBalancerConfiguration;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.cloud.client.discovery.EnableDiscoveryClient;
import org.springframework.cloud.client.loadbalancer.LoadBalanced;
import org.springframework.cloud.loadbalancer.annotation.LoadBalancerClient;
import org.springframework.cloud.loadbalancer.annotation.LoadBalancerClients;
import org.springframework.context.annotation.Bean;
import org.springframework.web.client.RestTemplate;

@SpringBootApplication
@EnableDiscoveryClient
@LoadBalancerClients({
        @LoadBalancerClient(value = "user-service", configuration = CustomLoadBalancerConfiguration.class),
        @LoadBalancerClient(value = "root-service", configuration = CustomLoadBalancerConfiguration.class)
})
public class ConsumerApplication {

    @Bean
    @LoadBalanced
    public RestTemplate restTemplate() {
        return new RestTemplate();
    }

    public static void main(String[] args) {
        SpringApplication.run(ConsumerApplication.class, args);
    }
}
```

###### ConsumerController

```
import com.ljl.consumer.domain.dto.User;
import io.github.resilience4j.circuitbreaker.annotation.CircuitBreaker;
import jakarta.annotation.Resource;
import lombok.extern.slf4j.Slf4j;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.client.RestTemplate;

@Slf4j
@RestController
@RequestMapping("/consumer")
public class ConsumerController {

    @Resource
    private RestTemplate restTemplate;

    private static final String CIRCUIT_BREAKER_NAME = "myCircuitBreaker";
    private static final String USER_SERVICE_NAME = "user-service";
    private static final String ROOT_SERVICE_NAME = "root-service";

    // @CircuitBreaker：应用断路器，指定断路器名称和回退方法。
    // fallback(Throwable t)：当断路器打开或服务调用失败时调用的回退方法。
    @GetMapping("/user/{id}")
    @CircuitBreaker(name = CIRCUIT_BREAKER_NAME, fallbackMethod = "myCircuitFallback")
    public User queryById(@PathVariable Long id) {
        if (id < 0) {
            log.error("id不能为负");
            throw new RuntimeException("Invalid id");
        }

        String url = "http://user-service/user/" + id;
        return restTemplate.getForObject(url, User.class);
    }

    @GetMapping("/root/{id}")
    public User queryRootById(@PathVariable Long id) {
        String url = "http://root-service/root/" + id;
        return restTemplate.getForObject(url, User.class);
    }

    // 回退方法，当熔断器触发时调用，参数和返回值需要与被熔断的方法相同（参数在原来基础上加Throwable）
    public User myCircuitFallback(Long id, Throwable t) {
        log.error("服务不可用，进入回退方法: {}", t.getMessage());
        User fallbackUser = new User();
        fallbackUser.setId(id);
        fallbackUser.setName("服务暂时不可用，请稍后再试。");
        return fallbackUser;
    }
}
```

断路器状态监测

```
import io.github.resilience4j.circuitbreaker.CircuitBreaker;
import io.github.resilience4j.circuitbreaker.CircuitBreakerRegistry;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Component;

import static com.ljl.consumer.utils.constant.LimterConstant.CIRCUIT_BREAKER_NAME;

//监听熔断器状态
@Component
public class CircuitBreakerEventListener {

    private final Logger logger = LoggerFactory.getLogger(CircuitBreakerEventListener.class);

    public CircuitBreakerEventListener(CircuitBreakerRegistry circuitBreakerRegistry) {
        // 获取注册的熔断器实例
        CircuitBreaker circuitBreaker = circuitBreakerRegistry.circuitBreaker(CIRCUIT_BREAKER_NAME);
        // 注册监听器
        circuitBreaker.getEventPublisher()
                .onStateTransition(event -> {
                    logger.debug("服务器状态: " + event);
                })
                .onFailureRateExceeded(event -> {
                    logger.debug("故障率超标: " + event);
                })
                .onCallNotPermitted(event -> {
                    logger.debug("超过调用失败次数，服务被禁止调用: " + event);
                })
                .onError(event -> {
                    logger.error("服务调用失败: " + event);
                })
                .onSuccess(event -> {
                    logger.debug("服务调用成功: " + event);
                });
    }
}

```

### 限流器

###### application.yml

```
resilience4j:
	# 熔断器
  circuitbreaker:
    # 定义不同的熔断器实例
    instances:
      # 熔断器名称
      myCircuitBreaker:
        base-config: default
    configs:
      default:
        # 是否注册健康指示器 用于监控
        register-health-indicator: true
        # 滑动窗口大小 用于统计失败率
        sliding-window-size: 10
        # 触发熔断器的前的最小调用次数
        minimum-number-of-calls: 3
        # 失败率阈值 超过该值断路器将打开(需要将限流器的错误排除故障率统计)
        failure-rate-threshold: 50
        # 断路器打开后等待的秒
        wait-duration-in-open-state: 10s
        # 半开状态下允许的调用次数
        permitted-number-of-calls-in-half-open-state: 3
        # 是否自动从打开状态过渡到半开状态
        automatic-transition-from-open-to-half-open-enabled: true
        # 捕获所有异常
        record-exceptions:
          - java.lang.Exception
          - java.lang.RuntimeException
          - java.lang.Throwable
        ignore-exceptions:
          - io.github.resilience4j.ratelimiter.RequestNotPermitted
  # 限流器
  ratelimiter:
    instances:
      myRateLimiter:
        limitForPeriod: 5                 # 每个周期允许的最大请求数
        limitRefreshPeriod: 10s            # 限流刷新周期
        timeoutDuration: 500ms            # 超过限流等待的最大时间
```

###### ConsumerController

```
import com.ljl.consumer.domain.dto.User;
import io.github.resilience4j.circuitbreaker.annotation.CircuitBreaker;
import io.github.resilience4j.ratelimiter.RequestNotPermitted;
import io.github.resilience4j.ratelimiter.annotation.RateLimiter;
import jakarta.annotation.Resource;
import lombok.extern.slf4j.Slf4j;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.client.RestTemplate;

import static com.ljl.consumer.utils.constant.LimterConstant.CIRCUIT_BREAKER_NAME;
import static com.ljl.consumer.utils.constant.LimterConstant.RATE_LIMITER_NAME;


@Slf4j
@RestController
@RequestMapping("/consumer")
public class ConsumerController {

    @Resource
    private RestTemplate restTemplate;


    // @CircuitBreaker：应用断路器，指定断路器名称和回退方法。
    // fallback(Throwable t)：当断路器打开或服务调用失败时调用的回退方法。
    // 熔断器和限流器时需要将fallbackMethod加在熔断器上,否则不走熔断器了（原因未知 试出来的）
    @GetMapping("/user/{id}")
    @CircuitBreaker(name = CIRCUIT_BREAKER_NAME, fallbackMethod = "Fallback")
    @RateLimiter(name = RATE_LIMITER_NAME)
    public User queryById(@PathVariable Long id) {
        if (id < 0) {
            log.error("id不能为负");
            throw new RuntimeException("Invalid id");
        }

        String url = "http://user-service/user/" + id;
        return restTemplate.getForObject(url, User.class);
    }

    @GetMapping("/root/{id}")
    public User queryRootById(@PathVariable Long id) {
        String url = "http://root-service/root/" + id;
        return restTemplate.getForObject(url, User.class);
    }

    // 回退方法，当熔断器触发时调用
    public User Fallback(Long id, Throwable t) {
        log.error("服务不可用，进入回退方法: {}", t.getMessage());
        User fallbackUser = new User();
        fallbackUser.setId(id);
        if( t instanceof RequestNotPermitted)
            fallbackUser.setName("请求过于频繁，请稍后再试。");
        else
            fallbackUser.setName("服务暂时不可用，请稍后再试。");
        return fallbackUser;
    }
}
```

###### RateLimiterEventListener

```
import io.github.resilience4j.ratelimiter.RateLimiterRegistry;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Component;

import static com.ljl.consumer.utils.constant.LimterConstant.RATE_LIMITER_NAME;

@Component
public class RateLimiterEventListener {

    Logger logger = LoggerFactory.getLogger(RateLimiterEventListener.class);

    public RateLimiterEventListener(RateLimiterRegistry rateLimiterRegistry) {
        rateLimiterRegistry.rateLimiter(RATE_LIMITER_NAME).getEventPublisher()
                .onSuccess(event -> {
                    logger.debug("成功通过限流器，限流器状态关闭: " + event);
                }).onFailure(event -> {
                    logger.error("请求过多，限流器开启" + event);
                });
    }
}

```

### 重试

###### application.yml

```
resilience4j:
  # 熔断器
  circuitbreaker:
    # 定义不同的熔断器实例
    instances:
      # 熔断器名称
      myCircuitBreaker:
        base-config: default
    configs:
      default:
        # 是否注册健康指示器 用于监控
        register-health-indicator: true
        # 滑动窗口大小 用于统计失败率
        sliding-window-size: 10
        # 触发熔断器的前的最小调用次数
        minimum-number-of-calls: 3
        # 失败率阈值 超过该值断路器将打开(需要将限流器的错误排除故障率统计)
        failure-rate-threshold: 50
        # 断路器打开后等待的秒
        wait-duration-in-open-state: 10s
        # 半开状态下允许的调用次数
        permitted-number-of-calls-in-half-open-state: 3
        # 是否自动从打开状态过渡到半开状态
        automatic-transition-from-open-to-half-open-enabled: true
        # 捕获所有异常
        record-exceptions:
          - java.lang.Exception
          - java.lang.RuntimeException
          - java.lang.Throwable
        ignore-exceptions:
          - io.github.resilience4j.ratelimiter.RequestNotPermitted
  # 限流器
  ratelimiter:
    instances:
      myRateLimiter:
        limitForPeriod: 5                 # 每个周期允许的最大请求数
        limitRefreshPeriod: 1s            # 限流刷新周期
        timeoutDuration: 500ms            # 超过限流等待的最大时间
  # 重试
  retry:
      instances:
        myRetry:
          # 最大重试次数(3 就是1次调用+2次重试)
          maxAttempts: 3
          # 重试之间的等待时间
          wait-duration: 2s
          retryExceptions:
            - java.lang.IllegalStateException
            - java.io.IOException
            - java.util.concurrent.TimeoutException
            - org.springframework.web.client.ResourceAccessException
```

###### ConsumerController

```
import com.ljl.consumer.domain.dto.User;
import io.github.resilience4j.circuitbreaker.annotation.CircuitBreaker;
import io.github.resilience4j.ratelimiter.RequestNotPermitted;
import io.github.resilience4j.ratelimiter.annotation.RateLimiter;
import io.github.resilience4j.retry.annotation.Retry;
import jakarta.annotation.Resource;
import lombok.extern.slf4j.Slf4j;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.client.RestTemplate;

import static com.ljl.consumer.utils.constant.LimterConstant.*;


@Slf4j
@RestController
@RequestMapping("/consumer")
public class ConsumerController {

    private Logger logger = LoggerFactory.getLogger(ConsumerController.class);

    @Resource
    private RestTemplate restTemplate;

    @GetMapping("/user/{id}")
    // @CircuitBreaker：应用断路器，指定断路器名称和回退方法。
    // fallback(Throwable t)：当断路器打开或服务调用失败时调用的回退方法。
    // 熔断器和限流器时需要将fallbackMethod加在熔断器上,否则不走熔断器了（原因未知 试出来的）
    // 熔断器、限流器和重试都在时需要将fallbackMethod加在重试器上 否则不走重试器（原因未知 试出来的）
    @CircuitBreaker(name = CIRCUIT_BREAKER_NAME)
    @RateLimiter(name = RATE_LIMITER_NAME)
    @Retry(name = RETRY_NAME, fallbackMethod = "fallback")
    public User queryById(@PathVariable Long id) {
        if (id < 0) {
            logger.error("id不能为负");
            throw new RuntimeException("Invalid id");
        }

        String url = "http://user-service/user/" + id;
        return restTemplate.getForObject(url, User.class);
    }

    @GetMapping("/root/{id}")
    public User queryRootById(@PathVariable Long id) {
        String url = "http://root-service/root/" + id;
        return restTemplate.getForObject(url, User.class);
    }

    // 回退方法，当熔断器触发时调用
    public User fallback(Long id, Throwable t) {
        logger.error("服务不可用，进入回退方法: {}", t.getMessage());
        User user = new User();
        user.setId(id);
        if( t instanceof RequestNotPermitted)
            user.setName("请求过于频繁，请稍后再试。");
        else
            user.setName("服务暂时不可用，请稍后再试。");
        return user;
    }
}
```

###### RetryEventListener

```java
import io.github.resilience4j.retry.RetryRegistry;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Component;

import static com.ljl.consumer.utils.constant.LimterConstant.RETRY_NAME;

@Component
public class RetryEventListener {

    private final Logger logger = LoggerFactory.getLogger(RetryEventListener.class);

    public RetryEventListener(RetryRegistry retryRegistry) {
        retryRegistry.retry(RETRY_NAME).getEventPublisher()
                .onRetry(event -> {
                    logger.debug("尝试重新调用: " + event);
                }).onSuccess(event -> {
                    logger.debug("重试后调用失败:" + event);
                }).onError(event -> {
                    logger.error("重试后仍然失败: " + event);
                }).onIgnoredError(event -> {
                    logger.debug("重试器忽略的异常:" + event);
                });

    }

}
```

## Feign调用

###### pom

```
				<!-- feign调用 -->
        <dependency>
            <groupId>org.springframework.cloud</groupId>
            <artifactId>spring-cloud-starter-openfeign</artifactId>
        </dependency>
```

###### ConsumerApplication

```
import com.ljl.consumer.config.CustomLoadBalancerConfiguration;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.cloud.client.discovery.EnableDiscoveryClient;
import org.springframework.cloud.client.loadbalancer.LoadBalanced;
import org.springframework.cloud.loadbalancer.annotation.LoadBalancerClient;
import org.springframework.cloud.loadbalancer.annotation.LoadBalancerClients;
import org.springframework.cloud.openfeign.EnableFeignClients;
import org.springframework.context.annotation.Bean;
import org.springframework.web.client.RestTemplate;

//开启feign调用
@EnableFeignClients
@SpringBootApplication
@EnableDiscoveryClient
@LoadBalancerClients({
        @LoadBalancerClient(value = "user-service", configuration = CustomLoadBalancerConfiguration.class),
        @LoadBalancerClient(value = "root-service", configuration = CustomLoadBalancerConfiguration.class)
})
public class ConsumerApplication {

    @Bean
    @LoadBalanced
    public RestTemplate restTemplate() {
        return new RestTemplate();
    }

    public static void main(String[] args) {
        SpringApplication.run(ConsumerApplication.class, args);
    }
}
```

###### client.ConsumerFeignController

```
import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import io.github.resilience4j.circuitbreaker.annotation.CircuitBreaker;
import io.github.resilience4j.ratelimiter.RequestNotPermitted;
import io.github.resilience4j.ratelimiter.annotation.RateLimiter;
import io.github.resilience4j.retry.annotation.Retry;
import jakarta.annotation.Resource;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.HashMap;
import java.util.Map;

import static com.ljl.consumer.utils.constant.HttpConstant.CODE;
import static com.ljl.consumer.utils.constant.HttpConstant.MESSAGE;
import static com.ljl.consumer.utils.constant.LimterConstant.*;

@RestController
@RequestMapping("/cf")
public class ConsumerFeignController {

    private final Logger logger = LoggerFactory.getLogger(ConsumerFeignController.class);

    @Resource
    private UserClient userClient;

    @GetMapping("/{id}")
    @CircuitBreaker(name = CIRCUIT_BREAKER_NAME)
    @RateLimiter(name = RATE_LIMITER_NAME)
    @Retry(name = RETRY_NAME, fallbackMethod = "fallback")
    public String queryById(@PathVariable("id") Long id) {
        if(id < 0){
            logger.error("id不能为负");
            throw new RuntimeException();
        }
        return userClient.queryById(id);
    }

    // 回退方法，当熔断器触发时调用
    public String fallback(Long id, Throwable t) {
        logger.error("服务不可用，进入回退方法: {}", t.getMessage());
        Map<String, Object> map = new HashMap<>();
        map.put(CODE, 500);

        if (t instanceof RequestNotPermitted){
            logger.error( "请求过于频繁，请稍后再试。");
            map.put(MESSAGE, "请求过于频繁，请稍后再试。");
        }
        else{
            map.put(MESSAGE, "服务暂时不可用，请稍后再试。");
            logger.error("服务暂时不可用，请稍后再试。");
        }
        try {
            return new ObjectMapper().writeValueAsString(map);
        } catch (JsonProcessingException e) {
            throw new RuntimeException(e);
        }
    }
}
```

###### client.UserClient

```
import com.ljl.consumer.domain.dto.User;
import org.springframework.cloud.openfeign.FeignClient;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;

@FeignClient(name = "user-service")
public interface UserClient {

    //http://user-service/user/123
    @GetMapping("/user/{id}")
    String queryById(@PathVariable("id") Long id);
}
```

## Spring Cloud Gateway网关

### Gateway与Fegin的区别

Gateway 作为整个应用的流量入口，接收所有的请求，如PC、移动端等，并且将不同的请求转- 发至不同的处理微服务模块，其作用可视为nginx；大部分情况下用作权限鉴定、服务端流量控制

Feign 则是将当前微服务的部分服务接口暴露出来，并且主要用于各个微服务之间的服务调用

### Gateway配置

###### pom

```
<?xml version="1.0" encoding="UTF-8"?>
<project xmlns="http://maven.apache.org/POM/4.0.0"
         xmlns:xsi="http://www.w3.org/2001/XMLSchema-instance"
         xsi:schemaLocation="http://maven.apache.org/POM/4.0.0 http://maven.apache.org/xsd/maven-4.0.0.xsd">
    <modelVersion>4.0.0</modelVersion>
    <parent>
        <groupId>com.ljl</groupId>
        <artifactId>springcloud</artifactId>
        <version>0.0.1-SNAPSHOT</version>
    </parent>

    <artifactId>gateway</artifactId>

    <properties>
        <maven.compiler.source>17</maven.compiler.source>
        <maven.compiler.target>17</maven.compiler.target>
        <project.build.sourceEncoding>UTF-8</project.build.sourceEncoding>
    </properties>

    <dependencies>
        <dependency>
            <groupId>org.springframework.cloud</groupId>
            <artifactId>spring-cloud-starter-gateway</artifactId>
        </dependency>
        <dependency>
            <groupId>org.springframework.cloud</groupId>
            <artifactId>spring-cloud-starter-netflix-eureka-client</artifactId>
        </dependency>
        <dependency>
            <groupId>org.springframework.cloud</groupId>
            <artifactId>spring-cloud-starter-loadbalancer</artifactId>
        </dependency>
    </dependencies>
</project>
```

###### GatewayApplication

```
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.cloud.client.discovery.EnableDiscoveryClient;

@EnableDiscoveryClient
@SpringBootApplication
public class GatewayApplication {
    public static void main(String[] args) {
        SpringApplication.run(GatewayApplication.class, args);
    }
}
```

###### application.yml

```
server:
  port: 10010
spring:
  application:
    name: api-gateway
  cloud:
    gateway:
      routes:
        # 路由id，可以随意写
        - id: consumer-service-route
          # 代理的服务地址
          uri:
            lb://consumer
          # http://127.0.0.1:8080
          # 路由断言，可以配置映射路径
          predicates:
            # - Path=/cf/**
            # - Path=/**
            - Path=/api/cf/**
          filters:
            # 添加请求路径的前缀
            # - PrefixPath=/cf
            # 表示过滤1个路径，2表示两个路径，以此类推
            - StripPrefix=1
        # 第二个路由规则：访问/cf/**不做任何处理
        - id: cf-direct-route
          uri:
            lb://consumer
          predicates:
            - Path=/cf/**
eureka:
  client:
    service-url:
      defaultZone: http://127.0.0.1:10086/eureka
    register-with-eureka: true
  instance:
    prefer-ip-address: true

```

### 过滤器

Gateway作为网关的其中一个重要功能，就是实现请求的鉴权。而这个动作往往是通过网关提供的过滤器来实现的。前面的 路由前缀 章节中的功能也是使用过滤器实现的。

![image-20250111170912069](src/main/resources/image/Snipaste_2025-01-11_17-13-06.png)

###### application.yml

```
server:
  port: 10010
spring:
  application:
    name: api-gateway
  cloud:
    gateway:
      routes:
        # 路由id，可以随意写
        - id: consumer-service-route
          # 代理的服务地址
          uri:
            lb://consumer
          # http://127.0.0.1:8080
          # 路由断言，可以配置映射路径
          predicates:
            # - Path=/cf/**
            # - Path=/**
            - Path=/api/cf/**
          filters:
            # 添加请求路径的前缀
            # - PrefixPath=/cf
            # 表示过滤1个路径，2表示两个路径，以此类推
            - StripPrefix=1
          
        # 第二个路由规则：访问/cf/**不做任何处理
        - id: cf-direct-route
          uri:
            lb://consumer
          predicates:
            - Path=/cf/**
eureka:
  client:
    service-url:
      defaultZone: http://127.0.0.1:10086/eureka
    register-with-eureka: true
  instance:
    prefer-ip-address: true
```

#### 配置全局过滤器码 (仅能实现添加头 移除头等简单逻辑 复杂逻辑如动态路由、鉴权需要实现 GlobalFilter 接口）

###### application.yml

```
server:
  port: 10010
spring:
  application:
    name: api-gateway
  cloud:
    gateway:
      routes:
        # 路由id，可以随意写
        - id: consumer-service-route
          # 代理的服务地址
          uri:
            lb://consumer
          # http://127.0.0.1:8080
          # 路由断言，可以配置映射路径
          predicates:
            # - Path=/cf/**
            # - Path=/**
            - Path=/api/cf/**
          filters:
            # 添加请求路径的前缀
            # - PrefixPath=/cf
            # 表示过滤1个路径，2表示两个路径，以此类推
            - StripPrefix=1
        # 第二个路由规则：访问/cf/**不做任何处理
        - id: cf-direct-route
          uri:
            lb://consumer
          predicates:
            - Path=/cf/**
      # 默认过滤器 对所有路由都生效
      default-filters:
        - AddResponseHeader=myName, ljl
eureka:
  client:
    service-url:
      defaultZone: http://127.0.0.1:10086/eureka
    register-with-eureka: true
  instance:
    prefer-ip-address: true
```

##### default-filters和GlobalFilter区别和使用场景

|     特性     |             default-filters              |               GlobalFilter               |
| :----------: | :--------------------------------------: | :--------------------------------------: |
|   配置方式   |       声明式（YAML/Properties 配置       |           编程式（Java 实现）            |
|   能力范围   |   限于 Spring Cloud Gateway 内置过滤器   |            支持自定义复杂逻辑            |
|   可扩展性   |               不支持自定义               |        支持依赖注入和复杂逻辑处理        |
|    灵活性    |                   较低                   |                    高                    |
| 作用范围控制 |            默认作用于所有路由            |           可以动态控制作用范围           |
|   使用场景   | 简单的全局过滤需求（如添加头、移除头等） | 复杂的全局过滤需求（如动态路由、鉴权等） |

#### 自定义过滤器

在过滤器（MyParamGatewayFilterFactory）中将http://localhost:10010/api/user/8?name=yemage中的参数name的值获取到并输出到控制台；并且参数名是可变的，也就是不一定每次都是name；需要可以通过配置过滤器的时候做到配置参数名。

###### Application.yml

``` application.yml
port: 10010
spring:
  application:
    name: api-gateway
  cloud:
    gateway:
      routes:
        # 路由id，可以随意写
        - id: consumer-service-route
          # 代理的服务地址
          uri:
            lb://consumer
          # http://127.0.0.1:8080
          # 路由断言，可以配置映射路径
          predicates:
            # - Path=/cf/**
            # - Path=/**
            - Path=/api/cf/**
          filters:
            # 添加请求路径的前缀
            # - PrefixPath=/cf
            # 表示过滤1个路径，2表示两个路径，以此类推
            - StripPrefix=1
            # 自定义的过滤器如 HAHAGatewayFilterFactory 去掉 GatewayFilterFactory
            - HAHA=name
            - HAHA=age
        # 第二个路由规则：访问/cf/**不做任何处理
        - id: cf-direct-route
          uri:
            lb://consumer
          predicates:
            - Path=/cf/**
      # 默认过滤器 对所有路由都生效
      default-filters:
        - AddResponseHeader=myName, ljl

eureka:
  client:
    service-url:
      defaultZone: http://127.0.0.1:10086/eureka
    register-with-eureka: true
  instance:
    prefer-ip-address: true
```

###### HAHAGatewayFilterFactory

``` package com.ljl.filter;
import lombok.*;
import org.springframework.cloud.gateway.filter.GatewayFilter;
import org.springframework.cloud.gateway.filter.GatewayFilterChain;
import org.springframework.cloud.gateway.filter.factory.AbstractGatewayFilterFactory;
import org.springframework.http.server.reactive.ServerHttpRequest;
import org.springframework.stereotype.Component;
import org.springframework.web.server.ServerWebExchange;
import reactor.core.publisher.Mono;

import java.util.Arrays;
import java.util.List;

@Component
public class HAHAGatewayFilterFactory extends AbstractGatewayFilterFactory<HAHAGatewayFilterFactory.Config> {

    static final String PARAM_NAME = "param";

    public HAHAGatewayFilterFactory() {
        super(Config.class);
    }

    public List<String> shortcutFieldOrder() {

        return List.of(PARAM_NAME);
    }

    //    过滤器（MyParamGatewayFilterFactory）中将http://localhost:10010/api/user/8?name=yemage
    //    中的参数name的值获取到并输出到控制台；并且参数名是可变的，也就是不一定每次都是name；需要可以通过配置过滤器的时候做到配置参数名。
    @Override
    public GatewayFilter apply(Config config) {
        // 非lambda写法
        return new GatewayFilter() {
            @Override
            public Mono<Void> filter(ServerWebExchange exchange, GatewayFilterChain chain) {
                // 拉取请求
                ServerHttpRequest request = exchange.getRequest();
                // 检查参数中是否包含 config.name
                if (request.getQueryParams().containsKey(config.param)) {
                    // 遍历参数的值并打印
                    request.getQueryParams().get(config.param).forEach(value -> {
                        System.out.printf("局部过滤器: 参数 %s = %s%n", config.param, value);
                    });
                }
                // 将请求传递给下一个过滤器
                return chain.filter(exchange);
            }
        };

        // lambda写法
//        return ((exchange, chain) -> {
//            // http://localhost:10010/api/user/8?name=yemage config.param ==> name
//            //获取请求参数中param对应的参数名 的参数值
//            ServerHttpRequest request = exchange.getRequest();
//            if (request.getQueryParams().containsKey(config.param)) {
//                // 遍历参数的值并打印
//                request.getQueryParams().get(config.param).forEach(value -> {
//                    System.out.printf("局部过滤器: 参数 %s = %s%n", config.param, value);
//                });
//            }
//            return chain.filter(exchange);
//        });

    }

    @Data
    @NoArgsConstructor
    @AllArgsConstructor
    public static class Config {
        //对应在配置过滤器的时候指定的参数名
        private String param;
    }
}
```

#### 自定义全局过滤器

编写全局过滤器，在过滤器中检查请求中是否携带token请求头。如果token请求头存在则放行；如果token为空或者不存在则设置返回的状态码为：未授权也不再执行下去。

###### MyGlobalFilter

``` package com.ljl.filter;
import io.micrometer.common.util.StringUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.cloud.gateway.filter.GatewayFilterChain;
import org.springframework.cloud.gateway.filter.GlobalFilter;
import org.springframework.core.Ordered;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Component;
import org.springframework.web.server.ServerWebExchange;
import reactor.core.publisher.Mono;

@Component
public class MyGlobalFilter implements GlobalFilter, Ordered {

    private final static Logger logger = LoggerFactory.getLogger(MyGlobalFilter.class);

    @Override
    public Mono<Void> filter(ServerWebExchange exchange, GatewayFilterChain chain) {
        logger.info("--------------全局过滤器MyGlobalFilter------------------");
        String token = exchange.getRequest().getHeaders().getFirst("token");
        if (StringUtils.isBlank(token)) {
            //设置响应状态码为未授权
            exchange.getResponse().setStatusCode(HttpStatus.UNAUTHORIZED);
            return exchange.getResponse().setComplete();
        }
        return chain.filter(exchange);
    }

    @Override
    public int getOrder() {
        // 值越小执行优先级越高
        return 1;
    }
}
```

#### 跨域配置

###### application.yml

``` 
server:
  port: 10010
spring:
  application:
    name: api-gateway
  cloud:
    gateway:
      routes:
        # 路由id，可以随意写
        - id: consumer-service-route
          # 代理的服务地址
          uri:
            lb://consumer
          # http://127.0.0.1:8080
          # 路由断言，可以配置映射路径
          predicates:
            # - Path=/cf/**
            # - Path=/**
            - Path=/api/cf/**
          filters:
            # 添加请求路径的前缀
            # - PrefixPath=/cf
            # 表示过滤1个路径，2表示两个路径，以此类推
            - StripPrefix=1
            # 自定义的过滤器如 HAHAGatewayFilterFactory 去掉 GatewayFilterFactory
            - HAHA=name
            - HAHA=age
        # 第二个路由规则：访问/cf/**不做任何处理
        - id: cf-direct-route
          uri:
            lb://consumer
          predicates:
            - Path=/cf/**
      # 默认过滤器 对所有路由都生效
      default-filters:
        - AddResponseHeader=myName, ljl
      # globalcors 用于配置全局的 CORS（跨域资源共享）设置。
      globalcors:
        # corsConfigurations: 定义 CORS 配置的路径模式
        cors-configurations:
          # 匹配所有路径。
          '[/**]':
            allowed-origins:
              # 允许的源，* 表示允许所有源。
              *
            allowed-methods:
              # 允许来自上面网址的所有GET方法跨域
              - get

eureka:
  client:
    service-url:
      defaultZone: http://127.0.0.1:10086/eureka
    register-with-eureka: true
  instance:
    prefer-ip-address: true

```

#### gateway针对特定ip进行限流

需要配合redis进行限流

###### pom

```
  <!-- Redis 支持 -->
  <dependency>
    <groupId>org.springframework.boot</groupId>
    <artifactId>spring-boot-starter-data-redis</artifactId>
  </dependency>
```

###### Application.yml

``` 
server:
  port: 10010
spring:
  application:
    name: api-gateway
  cloud:
    gateway:
      routes:
        # 路由id，可以随意写
        - id: consumer-service-route
          # 代理的服务地址
          uri:
            lb://consumer
          # http://127.0.0.1:8080
          # 路由断言，可以配置映射路径
          predicates:
            # - Path=/cf/**
            # - Path=/**
            - Path=/api/cf/**
          filters:
            # 添加请求路径的前缀
            # - PrefixPath=/cf
            # 表示过滤1个路径，2表示两个路径，以此类推
            - StripPrefix=1
            # 自定义的过滤器如 HAHAGatewayFilterFactory 去掉 GatewayFilterFactory
            - HAHA=name
            - HAHA=age

            # name必须写 RequestRateLimiter
            - name: RequestRateLimiter
              args:
                # key-resolver：决定限流的依据。
                key-resolver: "#{@ipKeyResolver}" # 引用自定义限流键生成器
                # replenishRate 令牌生成速率（每秒） 每秒钟生成replenishRate个令牌，桶满了不放了
                # burstCapacity：令牌桶的最大容量(在刚启动的1s时候令牌桶是空，所以请求还是会被拒绝),
                # 同时突发情况下每秒钟最大可以允许每个ip请求burstCapacity次。
                # 如果桶满了就不生成令牌了
                redis-rate-limiter:
                  replenishRate: 1
                  burstCapacity: 2
        # 第二个路由规则：访问/cf/**不做任何处理
        - id: cf-direct-route
          uri:
            lb://consumer
          predicates:
            - Path=/cf/**
      # 默认过滤器 对所有路由都生效
      default-filters:
        - AddResponseHeader=myName, ljl
      # globalcors 用于配置全局的 CORS（跨域资源共享）设置。
      globalcors:
        # corsConfigurations: 定义 CORS 配置的路径模式
        cors-configurations:
          # 匹配所有路径。
          '[/**]':
            # 允许的源，* 表示允许所有源。
            allowed-origins: "*"
            allowed-methods:
              # 允许来自上面网址的所有GET方法跨域
              - get
  data:
    redis:
      host: localhost
      port: 6379
      password: test
      lettuce:
        pool:
          max-active: 8
          max-idle: 8
          min-idle: 0
      timeout: 3s
      connect-timeout: 3s
eureka:
  client:
    service-url:
      defaultZone: http://127.0.0.1:10086/eureka
    register-with-eureka: true
  instance:
    prefer-ip-address: true
logging:
  level:
    org.springframework.cloud.gateway: debug
    io.github.resilience4j: debug
```

###### RateLimiterConfig

```
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.cloud.gateway.filter.ratelimit.KeyResolver;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.web.server.ServerWebExchange;
import reactor.core.publisher.Mono;

import java.util.Objects;

/**
 * 自定义根据ip进行限流
 */
@Configuration
public class RateLimiterConfig {

    private final static Logger logger = LoggerFactory.getLogger(RateLimiterConfig.class);

    @Bean(name = "ipKeyResolver")
    public KeyResolver ipKeyResolver() {
        return exchange -> Mono.just(getClientIp(exchange));
    }

    private String getClientIp(ServerWebExchange exchange) {
        String forwardedHeader = exchange.getRequest().getHeaders().getFirst("X-Forwarded-For");
        logger.debug("forwardedHeader: " + forwardedHeader);
        if (forwardedHeader != null) {
            return forwardedHeader.split(",")[0];
        }
        return Objects.requireNonNull(exchange.getRequest().getRemoteAddress()).getAddress().getHostAddress();
    }
}
```

## Nacos

常用的是nacos，nacos=注册中心+配置中心的组合 -> Nacos = Eureka + Config + Bus

nacos是springcloud alibaba的组件，所以需要在总的pom文件中加入springcloud alibaba组件

###### 父pom文件

```
<?xml version="1.0" encoding="UTF-8"?>
<project xmlns="http://maven.apache.org/POM/4.0.0" xmlns:xsi="http://www.w3.org/2001/XMLSchema-instance"
         xsi:schemaLocation="http://maven.apache.org/POM/4.0.0 https://maven.apache.org/xsd/maven-4.0.0.xsd">
    <modelVersion>4.0.0</modelVersion>
    <parent>
        <groupId>org.springframework.boot</groupId>
        <artifactId>spring-boot-starter-parent</artifactId>
        <version>3.2.12</version>
        <relativePath/> <!-- lookup parent from repository -->
    </parent>
    <groupId>com.ljl</groupId>
    <artifactId>springcloud</artifactId>
    <version>0.0.1-SNAPSHOT</version>
    <packaging>pom</packaging>
    <name>springcloud</name>
    <description>Demo project for Spring Boot</description>
    <url/>
    <licenses>
        <license/>
    </licenses>
    <developers>
        <developer/>
    </developers>
    <modules>
        <module>user-service</module>
        <module>consumer</module>
        <module>eureka-server</module>
        <module>root-service</module>
        <module>gateway</module>
    </modules>
    <scm>
        <connection/>
        <developerConnection/>
        <tag/>
        <url/>
    </scm>
    <properties>
        <java.version>17</java.version>
        <spring-cloud.version>2023.0.5</spring-cloud.version>
        <spring-cloud-alibaba.version>2023.0.3.2</spring-cloud-alibaba.version>
    </properties>

    <repositories>
        <repository>
            <id>aliyunmaven</id>
            <url>https://maven.aliyun.com/repository/public</url>
        </repository>
        <repository>
            <id>spring-cloud-alibaba</id>
            <url>https://maven.aliyun.com/repository/spring-cloud-alibaba</url>
        </repository>
    </repositories>

    <dependencies>
        <dependency>
            <groupId>org.projectlombok</groupId>
            <artifactId>lombok</artifactId>
            <version>1.18.30</version>
            <scope>provided</scope>
            <!--			<optional>true</optional>-->
        </dependency>
        <dependency>
            <groupId>org.springframework.boot</groupId>
            <artifactId>spring-boot-starter-test</artifactId>
            <scope>test</scope>
        </dependency>
        <dependency>
            <groupId>org.springframework.boot</groupId>
            <artifactId>spring-boot-starter</artifactId>
        </dependency>

    </dependencies>

    <dependencyManagement>
        <dependencies>
            <dependency>
                <groupId>org.springframework.cloud</groupId>
                <artifactId>spring-cloud-dependencies</artifactId>
                <version>${spring-cloud.version}</version>
                <type>pom</type>
                <scope>import</scope>
            </dependency>
            <dependency>
                <groupId>com.alibaba.cloud</groupId>
                <artifactId>spring-cloud-alibaba-dependencies</artifactId>
                <version>${spring-cloud-alibaba.version}</version>
                <type>pom</type>
                <scope>import</scope>
            </dependency>
        </dependencies>
    </dependencyManagement>

    <build>
        <plugins>
            <!--			<plugin>-->
            <!--				<groupId>org.apache.maven.plugins</groupId>-->
            <!--				<artifactId>maven-compiler-plugin</artifactId>-->
            <!--				<configuration>-->
            <!--					<annotationProcessorPaths>-->
            <!--						<path>-->
            <!--							<groupId>org.projectlombok</groupId>-->
            <!--							<artifactId>lombok</artifactId>-->
            <!--							<version>1.18.30</version>-->
            <!--						</path>-->
            <!--					</annotationProcessorPaths>-->
            <!--					<source>17</source>-->
            <!--					<target>17</target>-->
            <!--				</configuration>-->
            <!--			</plugin>-->
            <plugin>
                <groupId>org.springframework.boot</groupId>
                <artifactId>spring-boot-maven-plugin</artifactId>
                <configuration>
                    <excludes>
                        <exclude>
                            <groupId>org.projectlombok</groupId>
                            <artifactId>lombok</artifactId>
                        </exclude>
                    </excludes>
                </configuration>
            </plugin>
        </plugins>
    </build>
</project>
```

###### 子pom文件，注册和发现的服务

```
  <!-- nacos服务的注册发现 -->
  <dependency>
    <groupId>com.alibaba.cloud</groupId>
    <artifactId>spring-cloud-starter-alibaba-nacos-discovery</artifactId>
  </dependency>
```

将有关eureka的东西注释掉，配置文件加上就行，nacos没有loadbalancer，需要加上依赖否则发现不了服务

group需要自己去nacos建 namespace在建完之后生成一个自己复制过来配置，不同namespace之间服务发现不了

```
spring:  
  cloud:
    nacos:
      discovery:
        username: nacos
        password: nacos
        server-addr: localhost:8848
        # 区分开发 测试 发行组方便测试（dev、test、prod）
        group: dev_group
        namespace: 
```

###### 配置中心

```
        <!-- nacos配置中心做依赖管理 -->
        <dependency>
            <groupId>com.alibaba.cloud</groupId>
            <artifactId>spring-cloud-starter-alibaba-nacos-config</artifactId>
        </dependency>
        <!--   加载bootstrap 新版本默认禁用了bootstrap，需要自己开启     -->
        <dependency>
            <groupId>org.springframework.cloud</groupId>
            <artifactId>spring-cloud-starter-bootstrap</artifactId>
        </dependency>
```

##### application.yml修改（删除有关nacos的改到bootstrap.yml里)

```
spring:
  cloud:
    loadbalancer:
      enabled: true
    openfeign:
      client:
        config:
          default:
            # 连接超时时间 3s
            connect-timeout: 3000
            # 读取超时时间 1s
            read-timeout: 1000
          # 单独某个服务配置 会覆盖全局配置
          user-service:
            connect-timeout: 2000
            read-timeout: 2000
      httpclient:
        hc5:
          enabled: true
      compression:
        request:
          enabled: true  #请求开启压缩功能
          min-request-size: 2048 #最小触发压缩的大小
          mime-types: text/xml,application/xml,application/json #触发压缩数据类型
        response: #响应也开启压缩功能
          enabled: true
#eureka:
#  client:
#    service-url:
#      defaultZone: http://127.0.0.1:10086/eureka
#    # 每隔30秒 会重新拉取并更新数据,为了得到服务最新状态
#    registry-fetch-interval-seconds: 30
resilience4j:
  # 熔断器
  circuitbreaker:
    # 定义不同的熔断器实例
    instances:
      # 熔断器名称
      myCircuitBreaker:
        base-config: default
    configs:
      default:
        # 是否注册健康指示器 用于监控
        register-health-indicator: true
        # 滑动窗口大小 用于统计失败率
        sliding-window-size: 10
        # 触发熔断器的前的最小调用次数
        minimum-number-of-calls: 3
        # 失败率阈值 超过该值断路器将打开(需要将限流器的错误排除故障率统计)
        failure-rate-threshold: 50
        # 断路器打开后等待的秒
        wait-duration-in-open-state: 10s
        # 半开状态下允许的调用次数
        permitted-number-of-calls-in-half-open-state: 3
        # 是否自动从打开状态过渡到半开状态
        automatic-transition-from-open-to-half-open-enabled: true
        # 捕获所有异常
        record-exceptions:
          - java.lang.Exception
          - java.lang.RuntimeException
          - java.lang.Throwable
        ignore-exceptions:
          - io.github.resilience4j.ratelimiter.RequestNotPermitted
  # 限流器
  ratelimiter:
    instances:
      myRateLimiter:
        base-config: default
    configs:
      default:
        limitForPeriod: 5                # 每个周期允许的最大请求数
        limitRefreshPeriod: 1s            # 限流刷新周期
        timeoutDuration: 500ms            # 超过限流等待的最大时间
  # 重试
  retry:
    instances:
      myRetry:
        # 最大重试次数(3 就是1次调用+2次重试)
        maxAttempts: 3
        # 重试之间的等待时间
        wait-duration: 2s
        retryExceptions:
          - java.lang.IllegalStateException
          - java.io.IOException
          - java.util.concurrent.TimeoutException
          - org.springframework.web.client.ResourceAccessException
          - feign.FeignException
logging:
  level:
    io.github.resilience4j: DEBUG
    org.springframework.web.client: DEBUG
    com.ljl.consumer: DEBUG
    org.springframework.cloud.circuitbreaker: DEBUG
```

###### bootstrap.yml

```
spring:
  application:
    name: consumer-service
  # 指定开发环境
  profiles:
    active: dev
  cloud:
    nacos:
      server-addr: localhost:8848
      username: 'nacos'
      password: 'nacos'
      discovery:
        namespace: 743cb407-170d-4c67-8d26-044ace52c462
        # 区分开发 测试 发行组方便测试（dev、test、prod）
        group: dev_group
      config:
        refresh-enabled: true
        namespace: 743cb407-170d-4c67-8d26-044ace52c462
        # 随便取，对应每行的group就行 非命名空间
        group: my_group
        file-extension: yaml
  config:
    import:
      - nacos:${spring.application.name}-${spring.profiles.active}.${spring.cloud.nacos.config.file-extension}?refreshEnabled=true
logging:
  level:
    com.alibaba.nacos: DEBUG
```

