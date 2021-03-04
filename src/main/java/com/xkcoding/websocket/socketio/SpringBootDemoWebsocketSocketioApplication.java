package com.xkcoding.websocket.socketio;

import com.xkcoding.websocket.socketio.config.WsConfig;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.boot.context.properties.EnableConfigurationProperties;
import org.springframework.context.annotation.PropertySource;

/**
 * <p>
 * 启动器
 * </p>
 *
 * @author yangkai.shen
 * @date Created in 2018-12-12 13:59
 */
@SpringBootApplication
public class SpringBootDemoWebsocketSocketioApplication {

    public static void main(String[] args) {
        SpringApplication.run(SpringBootDemoWebsocketSocketioApplication.class, args);
    }
}
