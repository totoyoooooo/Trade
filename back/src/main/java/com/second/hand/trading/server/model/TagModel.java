package com.second.hand.trading.server.model;

import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
public class TagModel {

    private String text;

    private Long use_count;

    @Override
    public String toString() {
        return "TagModel [text=" + text + ", use_count=" + use_count + "]";
    }
}
