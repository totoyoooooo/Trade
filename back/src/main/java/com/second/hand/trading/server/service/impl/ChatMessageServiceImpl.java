package com.second.hand.trading.server.service.impl;

import com.second.hand.trading.server.dao.ChatMessageDao;
import com.second.hand.trading.server.model.ChatMessageModel;
import com.second.hand.trading.server.service.ChatMessageService;
import org.springframework.stereotype.Service;

import javax.annotation.Resource;
import java.util.List;

@Service
public class ChatMessageServiceImpl implements ChatMessageService {

    @Resource
    private ChatMessageDao chatMessageDao;

    @Override
    public int createChatMessage(ChatMessageModel chatMessageModel) {
        return chatMessageDao.createChatMessage(chatMessageModel.getChat_id(), chatMessageModel.getSender_id(), chatMessageModel.getContent(), chatMessageModel.getSend_time());
    }

    @Override
    public List<ChatMessageModel> getChatMessageByChatId(String chatId) {
        return chatMessageDao.getChatMessageByChatId(chatId);
    }

    @Override
    public void readChatMessage(String chatId,Long senderId) {
        chatMessageDao.readChatMessage(chatId,senderId);
    }

    @Override
    public void revokeChatMessage(Long id) {
        chatMessageDao.revokeChatMessage(id);
    }


}
