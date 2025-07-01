package com.second.hand.trading.server.service.impl;

import com.second.hand.trading.server.dao.ChatDao;
import com.second.hand.trading.server.model.ChatModel;
import com.second.hand.trading.server.service.ChatService;
import org.springframework.stereotype.Service;

import javax.annotation.Resource;
import java.util.List;

@Service
public class ChatServiceImpl implements ChatService {

    @Resource
    private ChatDao chatDao;

    @Override
    public int createChat(ChatModel chatModel) {
        return chatDao.createChat(chatModel.getId(), chatModel.getUser1_id(), chatModel.getUser2_id());
    }

    @Override
    public ChatModel getChatById(String id) {
        return chatDao.getChatById(id);
    }

    @Override
    public List<ChatModel> getChatList(Long id) {
        return chatDao.getChatList(id);
    }

    @Override
    public void addUnread(Long id) {
        chatDao.addUnread(id);
    }

    @Override
    public void clearUnread(Long id) {
        chatDao.clearUnread(id);
    }
}
