package com.second.hand.trading.server.dao;

import com.second.hand.trading.server.model.ChatModel;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;

import java.util.List;

@Mapper
public interface ChatDao {

    int createChat(@Param("id") String id,@Param("user1_id") Long user1Id, @Param("user2_id") Long user2Id);

    ChatModel getChatById(@Param("id") String id);

    List<ChatModel> getChatList(@Param("user_id") Long userId);

    void addUnread(@Param("id") Long id);

    void clearUnread(@Param("id") Long id);

}
