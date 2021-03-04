package com.xkcoding.websocket.socketio.handler;

import cn.hutool.core.lang.Dict;
import cn.hutool.core.util.ObjectUtil;
import com.corundumstudio.socketio.AckRequest;
import com.corundumstudio.socketio.SocketIOClient;
import com.corundumstudio.socketio.SocketIOServer;
import com.corundumstudio.socketio.annotation.OnConnect;
import com.corundumstudio.socketio.annotation.OnDisconnect;
import com.corundumstudio.socketio.annotation.OnEvent;
import com.xkcoding.websocket.socketio.config.DbTemplate;
import com.xkcoding.websocket.socketio.config.Event;
import com.xkcoding.websocket.socketio.config.SocketChannelDao;
import com.xkcoding.websocket.socketio.payload.*;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;

import java.util.Collection;
import java.util.Optional;
import java.util.UUID;
import java.util.concurrent.ConcurrentHashMap;

/**
 * <p>
 * 消息事件处理
 * </p>
 *
 * @author yangkai.shen
 * @date Created in 2018-12-18 18:57
 */
@Component
@Slf4j
public class MessageEventHandler {
    @Autowired
    private SocketIOServer server;

    @Autowired
    private DbTemplate dbTemplate;
    @Autowired
    private SocketChannelDao socketChannelDao;
    @Value("${ws.server.maxOnline}")
    private int maxOnline;
    // 存储不符合重名校验的伪连接，这些伪连接需要最终断开
    private ConcurrentHashMap<UUID, String> repeatNameSessions = new ConcurrentHashMap();

    /**
     * 添加connect事件，当客户端发起连接时调用
     *
     * @param client 客户端对象
     */
    @OnConnect
    public void onConnect(SocketIOClient client) {
        log.info("当前登录人数为：{}", socketChannelDao.getOnlineNum());
        if (socketChannelDao.getOnlineNum() >= maxOnline) {
            log.error("登录人数已满，请稍后登录！");

            ErrorMsg errorMsg = new ErrorMsg();
            errorMsg.setErrorMsg("登录人数已满，请稍后登录！");
            //当然由客户端的 error事件去主动中断连接是不安全的，应该由服务端来中断连接，这个我们就忽略了
            client.sendEvent(Event.ERROR, errorMsg);
            return;

        }

        if (client != null) {
            String token = client.getHandshakeData().getSingleUrlParam("token");
            // 模拟用户id 和token一致
            String userId = client.getHandshakeData().getSingleUrlParam("token");
            //判断用户是否已存在，存在则拒绝连接
            if (socketChannelDao.ifPresent(userId)) {
                log.error("用户名{}已存在，请使用其他用户名登录！");
                repeatNameSessions.put(client.getSessionId(), userId);
                ErrorMsg errorMsg = new ErrorMsg();
                errorMsg.setErrorMsg("【连接异常】用户名" + userId + "已存在，请刷新网页后使用其他用户名登录！");
                //当然由客户端的 error事件去主动中断连接是不安全的，应该由服务端来中断连接，这个我们就忽略了
                client.sendEvent(Event.ERROR, errorMsg);
                return;


            }


            String userName = client.getHandshakeData().getSingleUrlParam("name");
            UUID sessionId = client.getSessionId();
            SocketChannel channel = new SocketChannel();
            channel.setUserId(userId);
            channel.setUserName(userName);
            channel.setSessionId(sessionId);
            socketChannelDao.saveSocetChannel(userId, channel);

            //dbTemplate.save(userId, sessionId);
            log.info("{} 连接成功,【token】= {},【sessionId】= {}", userName, token, sessionId);
        } else {
            log.error("客户端为空");
        }
    }

    /**
     * 添加disconnect事件，客户端断开连接时调用，刷新客户端信息
     *
     * @param client 客户端对象
     */
    @OnDisconnect
    public void onDisconnect(SocketIOClient client) {
        if (client != null) {
            String token = client.getHandshakeData().getSingleUrlParam("token");
            // 模拟用户id 和token一致
            String userId = client.getHandshakeData().getSingleUrlParam("token");
            String userName = client.getHandshakeData().getSingleUrlParam("name");
            UUID sessionId = client.getSessionId();

            // dbTemplate.deleteByUserId(userId);
            /*
             * 如果是伪连接，则从repeatNameSessions中清除，
             * 如果是正常的连接断开，则从正常连接数据库中删除
             */
            if (!repeatNameSessions.containsKey(sessionId)) {
                socketChannelDao.deleteByUserId(userId);
            } else {
                repeatNameSessions.remove(sessionId);
            }

            log.info("{}断开连接,【token】= {},【sessionId】= {}", userName, token, sessionId);
            client.disconnect();
        } else {
            log.error("客户端为空");
        }
    }

    /**
     * 加入群聊
     *
     * @param client  客户端
     * @param request 请求
     * @param data    群聊
     */
    @OnEvent(value = Event.JOIN)
    public void onJoinEvent(SocketIOClient client, AckRequest request, JoinRequest data) {
        log.info("用户：{} 已加入群聊：{}", data.getUserId(), data.getGroupId());
        client.joinRoom(data.getGroupId());

        server.getRoomOperations(data.getGroupId()).sendEvent(Event.JOIN, data);
    }


    @OnEvent(value = Event.CHAT)
    public void onChatEvent(SocketIOClient client, AckRequest request, SingleMessageRequest data) {
        //Optional<UUID> toUser = dbTemplate.findByUserId(data.getToUid());
        //有空指针风险，后续优化
        SocketChannel fromUserInfo = socketChannelDao.findByUserId(data.getFromUid());
        SocketChannel toUserInfo = socketChannelDao.findByUserId(data.getToUid());
        Optional<SocketChannel> toUser = Optional.ofNullable(toUserInfo);


        if (toUser.isPresent()) {
            //log.info("用户 {} 刚刚私信了用户 {}：{}", data.getFromUid(), data.getToUid(), data.getMessage());
            log.info("用户 {} 刚刚私信了用户 {}：{}", fromUserInfo.getUserName(), toUserInfo.getUserName(),
                data.getMessage());
            sendToSingle(toUser.get().getSessionId(), data);
            request.sendAckData(Dict.create().set("flag", true).set("message", "发送成功"));
        } else {
            request.sendAckData(Dict.create().set("flag", false).set("message", "发送失败，对方不想理你(" + data.getToUid() +
                "不在线)"));
        }
    }

    @OnEvent(value = Event.GROUP)
    public void onGroupEvent(SocketIOClient client, AckRequest request, GroupMessageRequest data) {
        Collection<SocketIOClient> clients = server.getRoomOperations(data.getGroupId()).getClients();

        boolean inGroup = false;
        for (SocketIOClient socketIOClient : clients) {
            if (ObjectUtil.equal(socketIOClient.getSessionId(), client.getSessionId())) {
                inGroup = true;
                break;
            }
        }
        if (inGroup) {
            log.info("群号 {} 收到来自 {} 的群聊消息：{}", data.getGroupId(), data.getFromUid(), data.getMessage());
            sendToGroup(data);
        } else {
            request.sendAckData("请先加群！");
        }
    }

    /**
     * 单聊
     */
    public void sendToSingle(UUID sessionId, SingleMessageRequest message) {
        server.getClient(sessionId).sendEvent(Event.CHAT, message);
    }

    /**
     * 广播
     */
    /*public void sendToBroadcast(BroadcastMessageRequest message) {
        log.info("系统紧急广播一条通知：{}", message.getMessage());
        for (UUID clientId : dbTemplate.findAll()) {
            if (server.getClient(clientId) == null) {
                continue;
            }
            server.getClient(clientId).sendEvent(Event.BROADCAST, message);
        }
    }*/

    /**
     * 广播
     */
    public void sendToBroadcast(BroadcastMessageRequest message, boolean isSysHelper) {
        if (!isSysHelper) {
            String fromUserName = socketChannelDao.findByUserId(message.getFromUid()).getUserName();
            message.setFromUserName(fromUserName);
            log.info("{}紧急广播一条通知：{}", message.getFromUserName(), message.getMessage());

        }

        for (SocketChannel channel : socketChannelDao.findAll()) {
            if (server.getClient(channel.getSessionId()) == null) {
                continue;
            }

            server.getClient(channel.getSessionId()).sendEvent(isSysHelper ? Event.SYSHELPER : Event.BROADCAST,
                message);
        }
    }

    /**
     * 群聊
     */
    public void sendToGroup(GroupMessageRequest message) {
        server.getRoomOperations(message.getGroupId()).sendEvent(Event.GROUP, message);
    }
}
