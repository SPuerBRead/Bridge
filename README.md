# Bridge

无回显漏洞测试辅助平台 (Spring Boot + Spring Security + Netty)

平台使用Java编写，提供DNSLOG，HTTPLOG等功能，辅助渗透测试过程中无回显漏洞及SSRF等漏洞的验证和利用。


主要功能
-----------

* DNSLOG
* HTTPLOG
* 自定义DNS解析
* DNS Rebinding
* 自定义HTTP Response(Response内容、状态码、Header)
* 数据查询API


部署方法（支持手动部署和Docker部署）
-----------

### 1. 手动部署

#### 域名解析

假设根域名是dnslog.com，服务器IP是10.10.10.10进行以下配置
    
    配置A记录，子域名ns，解析到10.10.10.10
    
    配置NS记录，子域名dns，解析到ns.dnslog.com
    
    配置A记录，子域名dnslog，解析到10.10.10.10
    
dnslog.dnslog.com 用于访问平台web
    
dns.dnslog.com 作为测试时payload中设置的域名，每个用户对应dns.dnslog.com下的子域名，如1.dns.dnslog.com，登录平台后可以在API信息中看到对应的地址
    
子域名随意设置，对应上即可
    
#### 数据库配置

登录mysql执行以下命令，bridge.sql在程序的根目录下
    
    source bridge.sql

#### 服务器配置

环境：Java 1.8、Maven
    
修改resources目录下application.properties文件中的web服务端口（默认80端口）和数据库连接信息
    
    mvn clean package -DskipTests
    
maven生成的jar包位置在target目录下，如dns_log-0.0.1-SNAPSHOT.jar
    
    java -jar dns_log-0.0.1-SNAPSHOT.jar dns.dnslog.com dnslog.dnslog.com 10.10.10.10 a1b2c3d4
    
第一个参数指定payload设置对应的子域名
    
第二个参数指定访问平台对应的子域名
    
第三个参数服务器的IP地址
    
第四个参数设置注册时的注册暗号，注册需要填写该字段
    
 
### 2. Docker部署

域名解析部分与手动部署相同，无需配置数据库和服务器

    git clone https://github.com/SPuerBRead/Bridge.git
    cd ./Bridge

默认的mysql密码是password，若要修改，请保持以下两项中的密码相同（可不修改）

    1. docker-compose.yml文件中的MYSQL_ROOT_PASSWORD项
    
    2. 程序配置文件application.properties中的spring.datasource.password

修改docker-compose.yml倒数第三行command的值，此处为启动命令，将对应参数替换成域名配置中的信息，如：

    java -jar dns_log-0.0.1-SNAPSHOT.jar dns.dnslog.com dnslog.dnslog.com 10.10.10.10 a1b2c3d4

参数含义见手动部署部分。

配置完成后执行以下命令：

    docker-compose build
    docker-compose up -d
    
访问  dnslog.dnslog.com（实际域名根据根域名和配置而定）即可看到登录界面。
    

部分截图
-----------

DNSLOG

![15655801079930](https://user-images.githubusercontent.com/18071202/62844371-6e976080-bcf3-11e9-9356-8c7d10af37b0.jpg)


HTTPLOG

![15655803891520](https://user-images.githubusercontent.com/18071202/62844457-14e36600-bcf4-11e9-8501-744fb1406417.jpg)

API接口
-----------

apiKey在登录后的API信息页面中

#### dnslog查询接口

http://xxx.xx/api/dnslog/search?token={apiKey}&keyword={test}

keyword参数值必须是完整除去logAdress后的部分，此处没有模糊查询，如aaaaaa.1.dnslog.com对应keyword=aaaaaa，返回数据格式样例如下：

```
]
  {
    "ip": "localhost",
    "host": "test1.1.dns.xxxx.com",
    "time": "2019-07-30 15:25:14.0",
    "type": "A(1)"
  }
]
```

#### httplog查询接口
http://xxx.xx/api/weblog/search?token={apiKey}&keyword={test}
keyword要求同上，返回数据格式样例如下：
 
```
[
  {
    "path": "/",
    "method": "POST",
    "data": "",
    "ip": "10.10.37.75",
    "host": "test.1.dns.xxxx.com",
    "header": "{\"content-length\":\"22896\",\"postman-token\":\"9575b873-ccd9-4d5b-ba8a-c1f746e40086\",\"host\":\"test.1.dns.xxxx.com\",\"content-type\":\"text/plain\",\"connection\":\"keep-alive\",\"cache-control\":\"no-cache\",\"accept-encoding\":\"gzip, deflate\",\"user-agent\":\"PostmanRuntime/7.13.0\",\"accept\":\"*/*\"}",
    "time": "2019-07-23 17:50:10.0",
    "params": null,
    "version": "HTTP/1.1"
  }
]
```

