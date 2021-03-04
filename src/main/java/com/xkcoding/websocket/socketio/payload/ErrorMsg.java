package com.xkcoding.websocket.socketio.payload;

import lombok.Data;

/**
 * @author ：Sterry
 * @description：TODO
 * @date ：2021/2/26 18:41
 */
@Data
public class ErrorMsg {

    private   String  errorCode;
    private  String  errorMsg;

}
