package com.second.hand.trading.server.controller;

import com.second.hand.trading.server.model.ChatMessageModel;
import com.second.hand.trading.server.model.ChatModel;
import com.second.hand.trading.server.model.UserModel;
import com.second.hand.trading.server.service.ChatMessageService;
import com.second.hand.trading.server.service.ChatService;
import com.second.hand.trading.server.service.UserService;
import com.second.hand.trading.server.vo.ResultVo;
import com.second.hand.trading.server.enums.ErrorMsg;
import com.second.hand.trading.server.websocket.WebSocketServer;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;

import java.util.*;


@CrossOrigin
@RestController
@RequestMapping("chat")
public class ChatController {

    @Autowired
    private ChatService chatService;

    @Autowired
    private UserService userService;

    @Autowired
    private ChatMessageService chatMessageService;

    @PostMapping("addAndGetChat")
    public ResultVo addAndGetChat(@RequestBody ChatModel chatModel) {
        if(chatService.getChatById(chatModel.getId()) == null){
            chatService.createChat(chatModel);
            chatModel = chatService.getChatById(chatModel.getId());
        }
        return ResultVo.success(chatModel);
    }

    @PostMapping("getChatList")
    public ResultVo getChatList(@RequestParam Long userId,@RequestParam Long type) {
        List<ChatModel> chatList = chatService.getChatList(userId);
        List<ChatModel> resultList = new ArrayList<>(); // 使用一个新的列表来构建结果

        if(!chatList.isEmpty()){
            List<ChatModel> toRemove = new ArrayList<>();
            for(ChatModel chatModel : chatList){
                chatModel.setGetterId(userId);
                UserModel userModel = userService.getUser(chatModel.getOtherUserId());
                chatModel.setStatus(WebSocketServer.isOnline(userModel.getId()) ? "在线" : "离线");
                chatModel.setName(userModel.getNickname());
                chatModel.setAvatar(userModel.getAvatar());
                List<ChatMessageModel> message = getMessage(chatModel.getId());
                //删除撤回的消息
                if(!message.isEmpty()) {
                    List<ChatMessageModel> remove = new ArrayList<>();
                    for (ChatMessageModel chatMessageModel : message) {
                        if (chatMessageModel.getHas_revoke() == 1) remove.add(chatMessageModel);
                    }
                    message.removeAll(remove);
                }
                if(!message.isEmpty()){
                    Long unreadCount = 0L;
                    for(ChatMessageModel chatMessageModel : message){
                        if(!chatMessageModel.getSender_id().equals(userId) && chatMessageModel.getHas_read() == 0) unreadCount++;
                    }
                    chatModel.setUnread(unreadCount);
                    chatModel.setLastMessage(message.get(message.size() - 1).getContent());
                    chatModel.setTimestamp(message.get(message.size() - 1).getSend_time());
                    resultList.add(chatModel); // 有消息的直接加入结果列表
                }else{
                    // 如果消息列表为空
                    if(type != -1){ // 如果type不是-1 (即type是某个用户ID，表示创建新聊天)
                        // 如果当前聊天是与type用户的新聊天（但没有消息），则添加到结果列表
                        if ((chatModel.getUser1_id().equals(userId) && chatModel.getUser2_id().equals(type)) || 
                            (chatModel.getUser2_id().equals(userId) && chatModel.getUser1_id().equals(type))) {
                            resultList.add(chatModel);
                        }
                    } 
                    // type == -1，且消息列表为空的聊天，不显示，不加入resultList
                }
            }
        }

        // 如果经过上述处理后resultList仍然为空，且type不是-1，则尝试创建一个新的聊天模型
        if (resultList.isEmpty() && type != -1) {
            ChatModel newChatModel = new ChatModel();
            Long user1 = userId < type ? userId : type;
            Long user2 = userId < type ? type : userId;
            newChatModel.setId(user1 + "" + user2);
            newChatModel.setUser1_id(user1);
            newChatModel.setUser2_id(user2);
            newChatModel.setGetterId(userId);
            // 填充otherUser信息
            UserModel otherUserModel = userService.getUser(type);
            if (otherUserModel != null) {
                newChatModel.setOtherId(type);
                newChatModel.setName(otherUserModel.getNickname());
                newChatModel.setAvatar(otherUserModel.getAvatar());
                newChatModel.setStatus(WebSocketServer.isOnline(otherUserModel.getId()) ? "在线" : "离线");
            } else {
                // 如果 otherUserModel 为空，返回错误，因为无法创建有效聊天
                return ResultVo.fail(ErrorMsg.ACCOUNT_NOT_EXIT); 
            }
            newChatModel.setUnread(0L);
            newChatModel.setLastMessage("");
            newChatModel.setTimestamp("");
            resultList.add(newChatModel);
        }

        if (!resultList.isEmpty()) {
            resultList.sort((p1, p2) -> {
                String timestamp1 = p1.getTimestamp();
                String timestamp2 = p2.getTimestamp();

                if (timestamp1 == null && timestamp2 == null) {
                    return 0;
                } else if (timestamp1 == null) {
                    return -1;
                } else if (timestamp2 == null) {
                    return 1;
                } else {
                    return timestamp2.compareTo(timestamp1);
                }
            });
            return ResultVo.success(resultList);
        }
        // 只有当resultList为空且type==-1时才返回失败（即没有历史聊天，也不是创建新聊天）
        return ResultVo.fail(ErrorMsg.SYSTEM_ERROR);
    }

    @PostMapping("openChat")
    public ResultVo openChat(@RequestBody ChatModel chatModel) {
        List<ChatMessageModel> chatMessageModelList = getMessage(chatModel.getId());
        List<ChatMessageModel> remove = new ArrayList<>();
        for(ChatMessageModel chatMessageModel : chatMessageModelList){
            if(chatModel.getGetterId().equals(chatMessageModel.getSender_id())){
                chatMessageModel.setIsMe(1);
                chatMessageModel.setAvatar(userService.getUser(chatMessageModel.getSender_id()).getAvatar());
            }else{
                chatMessageModel.setIsMe(0);
                chatMessageModel.setAvatar(userService.getUser(chatModel.getOtherUserId()).getAvatar());
            }
            if(chatMessageModel.getHas_revoke() == 1) remove.add(chatMessageModel);
        }
        chatMessageModelList.removeAll(remove);
        Map<String,Object> ans = new LinkedHashMap<>();
        ans.put("chatId",chatModel.getId());
        ans.put("otherUser",userService.getUser(chatModel.getOtherUserId()));
        ans.put("myUser",userService.getUser(chatModel.getGetterId()));
        ans.put("messages",chatMessageModelList);
        return ResultVo.success(ans);
    }

    @PostMapping("sendMessage")
    public ResultVo sendMessage(@RequestBody ChatMessageModel chatMessageModel) {
        if(chatMessageService.createChatMessage(chatMessageModel) == 1){
            chatMessageModel.setIsMe(1);
            chatMessageModel.setAvatar(userService.getUser(chatMessageModel.getSender_id()).getAvatar());
            return ResultVo.success(chatMessageModel);
        }
        return ResultVo.fail(ErrorMsg.COMMIT_FAIL_ERROR);
    }

    private List<ChatMessageModel> getMessage(String chatId){
        return chatMessageService.getChatMessageByChatId(chatId);
    }

    @PostMapping("clearUnread")
    public ResultVo clearUnread(@RequestParam String chat_id,@RequestParam Long sender_id) {
        chatMessageService.readChatMessage(chat_id,sender_id);
        return ResultVo.success();
    }

    @PostMapping("revokeMessage")
    public ResultVo revokeMessage(@RequestParam Long id) {
        chatMessageService.revokeChatMessage(id);
        return ResultVo.success();
    }

}
