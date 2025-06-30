package com.second.hand.trading.server.service;

import com.second.hand.trading.server.model.ChatModel;

import java.util.List;

public interface ChatService {

    int createChat(ChatModel chatModel);

    ChatModel getChatById(String id);

    List<ChatModel> getChatList(Long id);

    void addUnread(Long id);

    void clearUnread(Long id);

}
