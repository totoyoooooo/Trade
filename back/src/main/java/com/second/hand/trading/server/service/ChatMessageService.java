package com.second.hand.trading.server.service;

import com.second.hand.trading.server.model.ChatMessageModel;

import java.util.List;

public interface ChatMessageService {

    int createChatMessage(ChatMessageModel chatMessageModel);

    List<ChatMessageModel> getChatMessageByChatId(String chatId);

    void readChatMessage(String chatId,Long senderId);

    void revokeChatMessage(Long id);
}
