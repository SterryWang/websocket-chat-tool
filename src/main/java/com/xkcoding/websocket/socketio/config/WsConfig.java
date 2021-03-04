package com.xkcoding.websocket.socketio.config;

import lombok.Data;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.context.annotation.PropertySource;
import org.springframework.stereotype.Component;

/**
 * <p>
 * WebSocket配置类
 * </p>
 *
 * @author yangkai.shen
 * @date Created in 2018-12-18 16:41
 */
@ConfigurationProperties(prefix = "ws.server")
@Data
public class WsConfig {

    private Integer maxOnline;

    /**
     * 端口号
     */
   // @Value("${ws.server.port}")
    private Integer port;

    /**
     * host
     */
    //@Value("${ws.server.host}")
    private String host;

}
