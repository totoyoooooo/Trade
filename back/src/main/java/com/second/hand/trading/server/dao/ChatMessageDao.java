package com.second.hand.trading.server.dao;

import com.second.hand.trading.server.model.ChatMessageModel;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;

import java.util.List;

@Mapper
public interface ChatMessageDao {

    int createChatMessage(@Param("chat_id") String chatId, @Param("sender_id") Long senderId,@Param("content") String content,@Param("send_time") String sendTime);

    List<ChatMessageModel> getChatMessageByChatId(@Param("chat_id") String id);

    ChatMessageModel getChatMessageById(@Param("id") String id);

    void readChatMessage(@Param("chat_id") String chatId, @Param("sender_id") Long senderId);

    void revokeChatMessage(@Param("id") Long id);
}
