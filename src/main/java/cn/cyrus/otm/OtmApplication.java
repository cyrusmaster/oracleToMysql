package cn.cyrus.otm;

import lombok.extern.slf4j.Slf4j;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.context.ConfigurableApplicationContext;
import org.springframework.core.env.ConfigurableEnvironment;

import java.net.InetAddress;
import java.net.UnknownHostException;

@Slf4j
@SpringBootApplication
public class OtmApplication {

	public static void main(String[] args) throws UnknownHostException {
		  ConfigurableApplicationContext run = SpringApplication.run(OtmApplication.class, args);
		  ConfigurableEnvironment env = run.getEnvironment();
			   log.info("\n----------------------------------------------------------\n\t" +
							"应用 '{}' 启动成功!\n\t" +
							"在线接口文档：http://{}:{}/doc.html\n" +
							"----------------------------------------------------------",
					env.getProperty("spring.application.name"),
					InetAddress.getLocalHost().getHostAddress(),
					env.getProperty("server.port") + env.getProperty("server.servlet.context-path")
			);
	}

}
