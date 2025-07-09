package com.second.hand.trading.server.model;

import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
public class ChatMessageModel {

    private Long id;

    private String chat_id;

    private Long sender_id;

    private String content;

    private String send_time;

    private int isMe;

    private String avatar;

    private int has_read;

    private int has_revoke;
}
