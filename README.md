## Dubbo&ZooKeeper〖二〗让你知道什么是Dubbo远程调用

**github地址**： [仓库地址](https://github.com/ktoking/Dubbo_ZK)
### 一. 什么是Dubbo
官网简介： [建议看看官网介绍](https://dubbo.apache.org/zh-cn/docs/user/preface/background.html)
**Dubbo 采用全 Spring 配置方式，透明化接入应用，对应用没有任何 API 侵入，只需用 Spring 加载 Dubbo 的配置即可，Dubbo 基于 Spring 的 Schema 扩展 进行加载。**

### 二. 废话一波
- **首先，先来废话一波，Dubbo的远程调用怎么调用？首先我们是学习了Springcloud的，其实感觉思想是一样的，那我消费者调用提供者那我肯定得知道，提供者在哪对吧，那我只能去知道提供者在哪的地方去问，这就是我们的注册中心，就是指路人，注册提供者到注册中心后，我们消费者问问路，就知道你在哪，然后调用。**
- 服务容器负责启动，加载，运行服务提供者。
- 服务提供者在启动时，向注册中心注册自己提供的服务。
- 服务消费者在启动时，向注册中心订阅自己所需的服务。
- 注册中心返回服务提供者地址列表给消费者，如果有变更，注册中心将基于长连接推送变更数据给消费者。
- 服务消费者，从提供者地址列表中，基于软负载均衡算法，选一台提供者进行调用，如果调用失败，再选另一台调用。
- 服务消费者和提供者，在内存中累计调用次数和调用时间，定时每分钟发送一次统计数据到监控中心。
![在这里插入图片描述](https://img-blog.csdnimg.cn/20200728160439376.png?x-oss-process=image/watermark,type_ZmFuZ3poZW5naGVpdGk,shadow_10,text_aHR0cHM6Ly9ibG9nLmNzZG4ubmV0L2tpbmd0b2s=,size_16,color_FFFFFF,t_70)
### 三. 准备工作
#### 3.1 安装Zookeeper作为注册中心
没安装的小伙伴们，一定先照着我之前的博客先安装个ZK

[安装ZK](https://blog.csdn.net/kingtok/article/details/104311911)
#### 3.2 项目目录结构
![在这里插入图片描述](https://img-blog.csdnimg.cn/20200728161609663.png?x-oss-process=image/watermark,type_ZmFuZ3poZW5naGVpdGk,shadow_10,text_aHR0cHM6Ly9ibG9nLmNzZG4ubmV0L2tpbmd0b2s=,size_16,color_FFFFFF,t_70)
#### 3.3 建议大火们，把手找回来
毕竟这简单的Demo**有手就行**
![在这里插入图片描述](https://img-blog.csdnimg.cn/20200728161818547.png?x-oss-process=image/watermark,type_ZmFuZ3poZW5naGVpdGk,shadow_10,text_aHR0cHM6Ly9ibG9nLmNzZG4ubmV0L2tpbmd0b2s=,size_16,color_FFFFFF,t_70)

### 四. 开始建Demo
#### 4.1 新建父项目dubbo_demo
![在这里插入图片描述](https://img-blog.csdnimg.cn/20200728162838436.png?x-oss-process=image/watermark,type_ZmFuZ3poZW5naGVpdGk,shadow_10,text_aHR0cHM6Ly9ibG9nLmNzZG4ubmV0L2tpbmd0b2s=,size_16,color_FFFFFF,t_70)

**首先这一步是为了Maven复用，指定了父版本，子模块就可以轻松复用父工程的依赖，你也不想每次一个工程就一大堆依赖吧，看着就头疼，选用Maven创建项目，什么都不要选，直接next就行，下同**


**POM文件添加**
```xml
<?xml version="1.0" encoding="UTF-8"?>
<project xmlns="http://maven.apache.org/POM/4.0.0"
         xmlns:xsi="http://www.w3.org/2001/XMLSchema-instance"
         xsi:schemaLocation="http://maven.apache.org/POM/4.0.0 http://maven.apache.org/xsd/maven-4.0.0.xsd">
    <modelVersion>4.0.0</modelVersion>

    <groupId>com.sf</groupId>
    <artifactId>dubbo_demo</artifactId>
    <packaging>pom</packaging>
    <version>1.0-SNAPSHOT</version>
    <modules>
        <module>dubbo_api</module>
        <module>dubbo_provide</module>
        <module>dubbo_consumer</module>
    </modules>

    <properties>
        <motan.version>0.3.0</motan.version>
        <dubbo.version>2.5.3</dubbo.version>
        <dubbox.version>2.8.4</dubbox.version>
        <spring.version>4.3.6.RELEASE</spring.version>
        <java.version>1.8</java.version>
        <project.build.sourceEncoding>UTF-8</project.build.sourceEncoding>
    </properties>

    <dependencies>

        <dependency>
            <groupId>com.alibaba</groupId>
            <artifactId>dubbo</artifactId>
            <version>2.5.3</version>
            <exclusions>
                <exclusion>
                    <groupId>org.springframework</groupId>
                    <artifactId>spring</artifactId>
                </exclusion>
            </exclusions>
        </dependency>

        <dependency>
            <groupId>com.github.sgroschupf</groupId>
            <artifactId>zkclient</artifactId>
            <version>0.1</version>
        </dependency>

        <!-- spring相关 -->
        <dependency>
            <groupId>org.springframework</groupId>
            <artifactId>spring-core</artifactId>
            <version>${spring.version}</version>
        </dependency>
        <dependency>
            <groupId>org.springframework</groupId>
            <artifactId>spring-beans</artifactId>
            <version>${spring.version}</version>
        </dependency>
        <dependency>
            <groupId>org.springframework</groupId>
            <artifactId>spring-context</artifactId>
            <version>${spring.version}</version>
        </dependency>
        <dependency>
            <groupId>org.springframework</groupId>
            <artifactId>spring-jdbc</artifactId>
            <version>${spring.version}</version>
        </dependency>
        <dependency>
            <groupId>org.springframework</groupId>
            <artifactId>spring-web</artifactId>
            <version>${spring.version}</version>
        </dependency>
        <dependency>
            <groupId>org.springframework</groupId>
            <artifactId>spring-webmvc</artifactId>
            <version>${spring.version}</version>
        </dependency>
        <dependency>
            <groupId>org.springframework</groupId>
            <artifactId>spring-aop</artifactId>
            <version>${spring.version}</version>
        </dependency>
        <dependency>
            <groupId>org.springframework</groupId>
            <artifactId>spring-tx</artifactId>
            <version>${spring.version}</version>
        </dependency>
        <dependency>
            <groupId>org.springframework</groupId>
            <artifactId>spring-orm</artifactId>
            <version>${spring.version}</version>
        </dependency>
        <dependency>
            <groupId>org.springframework</groupId>
            <artifactId>spring-context-support</artifactId>
            <version>${spring.version}</version>
        </dependency>
        <dependency>
            <groupId>org.springframework</groupId>
            <artifactId>spring-test</artifactId>
            <version>${spring.version}</version>
        </dependency>
        <dependency>
            <groupId>org.springframework</groupId>
            <artifactId>spring-jms</artifactId>
            <version>${spring.version}</version>
        </dependency>
        <dependency>
            <groupId>org.aspectj</groupId>
            <artifactId>aspectjrt</artifactId>
            <version>1.6.11</version>
        </dependency>
        <dependency>
            <groupId>org.aspectj</groupId>
            <artifactId>aspectjweaver</artifactId>
            <version>1.6.11</version>
        </dependency>
    </dependencies>


</project>
```
#### 4.2 新建dubbo_api模块
**作为一个公用模块，api里面是一个接口,定义两个接口**
![在这里插入图片描述](https://img-blog.csdnimg.cn/20200728162705336.png?x-oss-process=image/watermark,type_ZmFuZ3poZW5naGVpdGk,shadow_10,text_aHR0cHM6Ly9ibG9nLmNzZG4ubmV0L2tpbmd0b2s=,size_16,color_FFFFFF,t_70)

```java
package com.api.service;


/**

 * 定义服务接口

 */
public interface DemoService {
    String sayHello(String name);

    String myFun(String no);
}

```
#### 4.3 新建dubbo_provide
##### 4.3.1 POM文件引入api的依赖

```xml
 <dependencies>
        <dependency>
            <groupId>com.sf</groupId>
            <artifactId>dubbo_api</artifactId>
            <version>1.0-SNAPSHOT</version>
            <scope>compile</scope>
        </dependency>
    </dependencies>
```
##### 4.3.2 项目目录
![在这里插入图片描述](https://img-blog.csdnimg.cn/20200728163348337.png?x-oss-process=image/watermark,type_ZmFuZ3poZW5naGVpdGk,shadow_10,text_aHR0cHM6Ly9ibG9nLmNzZG4ubmV0L2tpbmd0b2s=,size_16,color_FFFFFF,t_70)
##### 4.3.3 实现api里的接口

```java
package com.provider.service;

import com.api.service.DemoService;

public class DemoServiceImpl implements DemoService {
    public String sayHello(String name) {
        return "Hello "+name;
    }

    public String myFun(String no) {
        return no+"提供者本机服务器提供";
    }
}
```
##### 4.3.4 新建测试类

```java	
package com.provider.test;

import org.springframework.context.support.ClassPathXmlApplicationContext;

import java.io.IOException;

public class ProviderTest {
    public static void main(String[] args) {
        ClassPathXmlApplicationContext context = new ClassPathXmlApplicationContext("classpath:springmvc.xml");
        context.start();

        System.out.println("Dubbo provider start...");

        try {
            System.in.read();   // 按任意键退出
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

}
```
##### 4.3.5 resource下新建两个配置xml
**dubbo-provider.xml**

```xml
<?xml version="1.0" encoding="UTF-8"?>
<beans xmlns="http://www.springframework.org/schema/beans"
       xmlns:xsi="http://www.w3.org/2001/XMLSchema-instance"
       xmlns:dubbo="http://code.alibabatech.com/schema/dubbo"
       xsi:schemaLocation="http://www.springframework.org/schema/beans
    http://www.springframework.org/schema/beans/spring-beans.xsd
    http://code.alibabatech.com/schema/dubbo
    http://code.alibabatech.com/schema/dubbo/dubbo.xsd">

    <!-- 提供方应用信息，用于计算依赖关系 -->
    <dubbo:application name="dubbo_provider"  />

    <!-- 使用zookeeper注册中心暴露服务地址 -->
    <dubbo:registry address="zookeeper://10.203.26.84:2181" />

    <!-- 用dubbo协议在20880端口暴露服务 -->
    <dubbo:protocol name="dubbo" port="20880" />

    <!-- 声明需要暴露的服务接口 -->
    <dubbo:service interface="com.api.service.DemoService" ref="demoService" />
    <!-- 接口实现类-->
    <bean id="demoService" class="com.provider.service.DemoServiceImpl"/>

</beans>
```

**springmvc.xml**

```xml
<?xml version="1.0" encoding="UTF-8"?>
<beans xmlns="http://www.springframework.org/schema/beans" xmlns:aop="http://www.springframework.org/schema/aop"
       xmlns:context="http://www.springframework.org/schema/context"
       xmlns:util="http://www.springframework.org/schema/util" xmlns:xsi="http://www.w3.org/2001/XMLSchema-instance"
       xsi:schemaLocation="http://www.springframework.org/schema/aop
        http://www.springframework.org/schema/aop/spring-aop-4.0.xsd
        http://www.springframework.org/schema/beans
        http://www.springframework.org/schema/beans/spring-beans-4.0.xsd
        http://www.springframework.org/schema/context
        http://www.springframework.org/schema/context/spring-context-4.0.xsd
        http://www.springframework.org/schema/util
        http://www.springframework.org/schema/util/spring-util-4.0.xsd"
       default-autowire="byName">

    <aop:aspectj-autoproxy />
    <context:component-scan base-package="com" />
    <import resource="classpath:dubbo-provider.xml" />
</beans>
```

#### 4.4 新建模块dubbo_consumer
##### 4.4.1 POM添加api依赖
```xml
<dependencies>

        <dependency>
            <groupId>com.sf</groupId>
            <artifactId>dubbo_api</artifactId>
            <version>1.0-SNAPSHOT</version>
            <scope>compile</scope>
        </dependency>

    </dependencies>
```
##### 4.4.2 项目目录
![在这里插入图片描述](https://img-blog.csdnimg.cn/20200728164905854.png?x-oss-process=image/watermark,type_ZmFuZ3poZW5naGVpdGk,shadow_10,text_aHR0cHM6Ly9ibG9nLmNzZG4ubmV0L2tpbmd0b2s=,size_16,color_FFFFFF,t_70)
##### 4.4.3 调用远程方法
**这边首先去获取bean（接口），然后调用接口里面的方法**
实现远程调用
```java
package com.comsumer;

import com.api.service.DemoService;
import org.springframework.context.support.ClassPathXmlApplicationContext;

import java.io.IOException;

public class ConsumerTest {
    public static void main(String[] args) {
        ClassPathXmlApplicationContext context = new ClassPathXmlApplicationContext(new String[] { "classpath:springmvc.xml" });

        context.start();
        DemoService demoService = (DemoService) context.getBean("demoService");

        System.out.println(demoService.sayHello("哈哈哈"));

        System.out.println(demoService.myFun("消费者触发： "));
        try {
            System.in.read();
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

}

```
##### 4.4.4 resource添加两个配置xml
**dubbo-consumer.xml**

```xml
<?xml version="1.0" encoding="UTF-8"?>
<beans xmlns="http://www.springframework.org/schema/beans"
       xmlns:xsi="http://www.w3.org/2001/XMLSchema-instance" xmlns:dubbo="http://code.alibabatech.com/schema/dubbo"
       xsi:schemaLocation="http://www.springframework.org/schema/beans
        http://www.springframework.org/schema/beans/spring-beans.xsd
        http://code.alibabatech.com/schema/dubbo
        http://code.alibabatech.com/schema/dubbo/dubbo.xsd ">
    <!-- 消费方应用名，用于计算依赖关系，不是匹配条件，不要与提供方一样 -->
    <dubbo:application name="dubbo_consumer" />
    <!-- 使用multicast广播注册中心暴露发现服务地址 -->
    <dubbo:registry  protocol="zookeeper" address="zookeeper://10.203.26.84:2181" />
    <!-- 生成远程服务代理，可以和本地bean一样使用demoService -->
    <dubbo:reference id="demoService" interface="com.api.service.DemoService" />
</beans>

```
**springmvc.xml**

```xml
<?xml version="1.0" encoding="UTF-8"?>
<beans xmlns="http://www.springframework.org/schema/beans" xmlns:aop="http://www.springframework.org/schema/aop"
       xmlns:context="http://www.springframework.org/schema/context"
       xmlns:util="http://www.springframework.org/schema/util" xmlns:xsi="http://www.w3.org/2001/XMLSchema-instance"
       xsi:schemaLocation="http://www.springframework.org/schema/aop
        http://www.springframework.org/schema/aop/spring-aop-4.0.xsd
        http://www.springframework.org/schema/beans
        http://www.springframework.org/schema/beans/spring-beans-4.0.xsd
        http://www.springframework.org/schema/context
        http://www.springframework.org/schema/context/spring-context-4.0.xsd
        http://www.springframework.org/schema/util
        http://www.springframework.org/schema/util/spring-util-4.0.xsd"
       default-autowire="byName">

    <aop:aspectj-autoproxy />
    <context:component-scan base-package="com" />
    <import resource="classpath:/dubbo-consumer.xml" />
</beans>

```
#### 4.5 测试
**先开启提供者，然后再开启消费者**

首先，服务提供者开启
![在这里插入图片描述](https://img-blog.csdnimg.cn/20200728165655425.png)
然后，消费者调用提供者，实现远程调用
![在这里插入图片描述](https://img-blog.csdnimg.cn/20200728165713953.png?x-oss-process=image/watermark,type_ZmFuZ3poZW5naGVpdGk,shadow_10,text_aHR0cHM6Ly9ibG9nLmNzZG4ubmV0L2tpbmd0b2s=,size_16,color_FFFFFF,t_70)

![在这里插入图片描述](https://img-blog.csdnimg.cn/20200728165954503.png?x-oss-process=image/watermark,type_ZmFuZ3poZW5naGVpdGk,shadow_10,text_aHR0cHM6Ly9ibG9nLmNzZG4ubmV0L2tpbmd0b2s=,size_16,color_FFFFFF,t_70)

### 真的有手就行，转载请标注~
