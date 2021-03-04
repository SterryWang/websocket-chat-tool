package com.xkcoding.websocket.socketio.payload;

import lombok.Data;

/**
 * <p>
 * 广播消息载荷
 * </p>
 *
 * @author yangkai.shen
 * @date Created in 2018-12-18 20:01
 */
@Data
public class BroadcastMessageRequest {
    /**
     * 消息内容
     */
    private String message;
    /**
     * 消息发送方用户id
     */
    private String fromUid;
    /**
     * 消息发送方用户名称
     */
    private String fromUserName;

}
