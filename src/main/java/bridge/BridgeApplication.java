package bridge;

import bridge.config.DnslogConfig;
import bridge.dnsserver.UDPServer;
import org.mybatis.spring.annotation.MapperScan;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.context.ConfigurableApplicationContext;
import org.springframework.context.annotation.Configuration;
import org.springframework.scheduling.annotation.EnableScheduling;

@Configuration
@SpringBootApplication
@MapperScan("bridge.mapper")
@EnableScheduling
public class BridgeApplication {

    public static void main(String[] args) {

        DnslogConfig.dnslogDomain = args[0];
        DnslogConfig.managerDomain = args[1];
        DnslogConfig.ip = args[2];
        DnslogConfig.signal = args[3];

        ConfigurableApplicationContext context = SpringApplication.run(BridgeApplication.class, args);

        UDPServer udpServer = context.getBean(UDPServer.class);

        udpServer.start();
    }

}
