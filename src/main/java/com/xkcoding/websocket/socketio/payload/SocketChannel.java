package com.xkcoding.websocket.socketio.payload;

import lombok.Data;

import java.util.UUID;

/**
 * @author ：Sterry
 * @description： socket会话信息
 * @date ：2021/2/25 21:13
 */
@Data
public class SocketChannel {
    String userName;
    String userId;
    UUID sessionId;
}
