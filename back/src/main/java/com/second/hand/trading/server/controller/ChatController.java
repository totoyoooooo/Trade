package com.second.hand.trading.server.controller;

import com.second.hand.trading.server.model.ChatMessageModel;
import com.second.hand.trading.server.model.ChatModel;
import com.second.hand.trading.server.model.UserModel;
import com.second.hand.trading.server.service.ChatMessageService;
import com.second.hand.trading.server.service.ChatService;
import com.second.hand.trading.server.service.UserService;
import com.second.hand.trading.server.vo.ResultVo;
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
                }else{
                    if(type == -1){
                        toRemove.add(chatModel);
                    }else{
                        if(!chatModel.getUser1_id().equals(type) && !chatModel.getUser2_id().equals(type)){
                            toRemove.add(chatModel);
                        }
                    }
                }
            }
            if(!toRemove.isEmpty()) chatList.removeAll(toRemove);
            chatList.sort((p1, p2) -> {
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
            return ResultVo.success(chatList);
        }
        return ResultVo.fail();
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
        return ResultVo.fail();
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
