package com.second.hand.trading.server.model;

import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
public class ChatRequestModel {
    private String sessionId;
    private String message;
    private Long userId;
}
