package com.xkcoding.websocket.socketio.controller;

import cn.hutool.core.lang.Dict;
import cn.hutool.core.util.ReflectUtil;
import cn.hutool.core.util.StrUtil;
import com.xkcoding.websocket.socketio.config.SocketChannelDao;
import com.xkcoding.websocket.socketio.handler.MessageEventHandler;
import com.xkcoding.websocket.socketio.payload.BroadcastMessageRequest;
import com.xkcoding.websocket.socketio.payload.SocketChannel;
import com.xkcoding.websocket.socketio.payload.SysHelperRequest;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.lang.reflect.Field;
import java.util.ArrayList;
import java.util.List;

/**
 * <p>
 * 消息发送Controller
 * </p>
 *
 *
 * @date Created in 2018-12-18 19:50
 */
@RestController
@RequestMapping("/send")
@Slf4j
public class MessageController {
    @Autowired
    private MessageEventHandler messageHandler;
    @Autowired
    private SocketChannelDao socketChannelDao;

    @PostMapping("/broadcast")
    public Dict broadcast(@RequestBody BroadcastMessageRequest message) {
        if (isBlank(message)) {
            return Dict.create().set("flag", false).set("code", 400).set("message", "参数为空");
        }
        log.info("拦截到用户{}发来的群发信息{}", message.getFromUserName(), message.getMessage());
        messageHandler.sendToBroadcast(message, false);
        return Dict.create().set("flag", true).set("code", 200).set("message",
            "发送成功");
    }

    /**
     * 系统运行助手
     *
     * @param message
     * @return
     */
    @PostMapping("/sysHelper")
    public Dict helperInform(@RequestBody SysHelperRequest message) {
        if (isBlank(message)) {
            return Dict.create().set("flag", false).set("code", 400).set("message", "参数为空");
        }
        log.info("拦截到{}运行通知:{}", message.getFromUserName(), message.getMessage());
        //临时屏蔽HTML通知
        /*if (SysHelperRequest.MsgContentTypeEnum.HTML.getValue().equals(message.getContentType())) {
            log.info("html通知格式，暂时不发送到通知平台");
            return Dict.create().set("flag", true).set("code", 200).set("message",
                "html通知格式，暂时不响应");
        }*/


        messageHandler.sendToBroadcast(message, true);
        return Dict.create().set("flag", true).set("code", 200).set("message",
            "发送成功");
    }

    @PostMapping("/getAllUsers")
    public Dict getAllUsers() {
        List<SocketChannel> userInfoList = socketChannelDao.findAll();
        // message.add("张三");
        // message.add("李四");


        // log.info("全部在线用户已经获取！");
        return Dict.create().set("flag", true).set("code", 200).set("message", userInfoList);
    }

    /**
     * 判断Bean是否为空对象或者空白字符串，空对象表示本身为<code>null</code>或者所有属性都为<code>null</code>
     *
     * @param bean Bean对象
     * @return 是否为空，<code>true</code> - 空 / <code>false</code> - 非空
     */
    private boolean isBlank(Object bean) {
        if (null != bean) {
            for (Field field : ReflectUtil.getFields(bean.getClass())) {
                Object fieldValue = ReflectUtil.getFieldValue(bean, field);
                if (null != fieldValue) {
                    if (fieldValue instanceof String && StrUtil.isNotBlank((String) fieldValue)) {
                        return false;
                    } else if (!(fieldValue instanceof String)) {
                        return false;
                    }
                }
            }
        }
        return true;
    }

}
