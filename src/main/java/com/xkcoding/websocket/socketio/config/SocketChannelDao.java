package com.xkcoding.websocket.socketio.config;

import cn.hutool.core.collection.CollUtil;
import com.xkcoding.websocket.socketio.payload.SocketChannel;
import org.springframework.stereotype.Component;

import java.util.ArrayList;
import java.util.Optional;
import java.util.concurrent.ConcurrentHashMap;

/**
 * @author ：Sterry
 * @description：TODO
 * @date ：2021/2/25 21:17
 */
@Component
public class SocketChannelDao {


    public  int  getOnlineNum(){
        return DB.size();
    }

    public  boolean  ifPresent(String userID){

        return  DB.containsKey(userID);
    }
    /**
     * 模拟数据库存储 user_id <-> session_id 的关系
     */
    public static final ConcurrentHashMap<String, SocketChannel> DB = new ConcurrentHashMap<>();
    /**
     * 保存/更新 user_id <-> session_id 的关系
     *
     * @param userId    用户id
     * @param socketChannel socketChannel
     */
    public void saveSocetChannel(String userId, SocketChannel socketChannel) {
        DB.put(userId, socketChannel);
    }

    /**
     * 删除 user_id <-> session_id 的关系
     *
     * @param userId 用户id
     */
    public void deleteByUserId(String userId) {
        DB.remove(userId);
    }

    /**
     * 获取所有SessionId
     *
     * @return SessionId列表
     */
    public ArrayList<SocketChannel> findAll() {
        return CollUtil.newArrayList(DB.values());
    }

    /**
     * 根据UserId查询SessionId
     *
     * @param userId 用户id
     * @return SessionId
     */
    public SocketChannel findByUserId(String userId) {
        return DB.get(userId);
    }


}
