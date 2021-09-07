# 

### 

## 



## 1. 背景

​	作为一名开发人员，经常发现单元测试无法覆盖所有的场景问题，在集成的测试环境无法经常无法即时查看到系统的运行时异常，无法充分监测SIT、UAT、REL测试过程中的异常，往往有人反映后再被动去排查问题。故开发此事件平台使得开发测试人员可以实时监测到测试过程中的异常信息，方便问题定位和缺陷修复，当然，接入此事件平台的系统，需要添加异常拦截器，统一将捕获的异常发送到此事件平台。

   此工具的另一大功能是即时通讯，主要目的是为了就相关故障事件的解决，进行沟通和协作。当然，你也可以把它单独当做一个聊天工具来使用，具备广播和私聊功能。

   此工具的项目骨架是基于开源项目[spring-boot-demo/demo-websocket-socketio at master · xkcoding/spring-boot-demo (github.com)](https://github.com/xkcoding/spring-boot-demo/tree/master/demo-websocket-socketio)，感谢大神发布的开源代码。在原来的基础上重点增加了事件播报功能，同时前端部分进行了重新绘制，功能界面做了调整，支持浏览器通知，并且支持通知开关等功能。

   

## 2.事件接入方法

主要参考`MessageController.java`类中的`helperInform()`方法：

```java
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

```

具体的接口可以如下：

接口地址：http://localhost:9876/demo/send/sysHelper

请求样例报文体：

```json
{
    "message": "java.lang.NullPointerException: null\nat com.xkcoding.websocket.socketio.handler.MessageEventHandler.sendToBroadcast(MessageEventHandler.java:213) ~[classes/:na]\n  at com.xkcoding.websocket.socketio.controller.MessageController.broadcast(MessageController.java:45) ~[classes/:na]\n  at sun.reflect.NativeMethodAccessorImpl.invoke0(Native Method) ~[na:1.8.0_181]\n  at sun.reflect.NativeMethodAccessorImpl.invoke(NativeMethodAccessorImpl.java:62) ~[na:1.8.0_181]\n  at sun.reflect.DelegatingMethodAccessorImpl.invoke(DelegatingMethodAccessorImpl.java:43) ~[na:1.8.0_181]\n  at java.lang.reflect.Method.invoke(Method.java:498) ~[na:1.8.0_181]\n  at org.springframework.web.method.support.InvocableHandlerMethod.doInvoke", //事件详情
    "fromUid": "microservice1", //事件生产者代码
    "fromUserName": "microservice2", //事件生产者简称
    "sendTime": "2021-03-04 17:04",
    "contentType":"plainText", //事件内容格式
    "envProfile":"REL"  //事件生产者环境标识符
    
}
```









## 2. 运行方式

1. 启动 `SpringBootDemoWebsocketSocketioApplication.java`
2. 使用不同的浏览器，访问 http://localhost:9876/demo/

## 3. 运行效果

**浏览器1：**

![Snipaste_2021-09-07_11-02-27](https://i.loli.net/2021/09/07/W3hT8BSNYcekFIP.png)

**浏览器2：**

![Snipaste_2021-09-07_11-03-04](https://i.loli.net/2021/09/07/LmwOgN3FDxKWJHM.png)

**事件通报功能：**

![Snipaste_2021-09-07_14-18-49](https://i.loli.net/2021/09/07/NACUzQ84MIld3Fe.png)
