package com.xkcoding.websocket.socketio.payload;

import lombok.Data;

/**
 * @author ：Sterry
 * @description： 外在通知的请求实体
 * @date ：2021/3/4 15:18
 */
@Data
public class SysHelperRequest extends  BroadcastMessageRequest{

    /**
     * 消息内容
     */
   /* private String message;*/
    /**
     * 消息格式
     */
    private String contentType;
    /**
     * 消息发送方用户id
     */
   /* private String fromUid;*/
    /**
     * 消息发送方用户名称
     */
  /*  private String fromUserName;*/
    /**
     * 客户端IP
     */
    private String clientIp;
    /**
     * 消息发送时间
     */
    private  String  sendTime;
    /**
     * 通知级别
     */
    private String informLevel;
    /**
     * 环境别名，比如UAT，REL，SIT等，用于前端显示时进行过滤
     */
    private  String envProfile;

    /**
     * 定义通知消息内容格式枚举
     */

    public enum MsgContentTypeEnum {
        HTML("html"), PLAINTEXT("plainText");

        private String value;

        private MsgContentTypeEnum(String value) {
            this.value = value;

        }

        public String getValue() {
            return this.value;
        }


    }

    /**
     * 通知级别枚举
     */
    public enum InformLevelEnum {
        ERROR("error"),
        WARN("warn"),
        INFO("info");

        private String value;

        private InformLevelEnum(String value) {
            this.value = value;
        }

        public  String  getValue(){
            return  this.value;
        }

    }


}
