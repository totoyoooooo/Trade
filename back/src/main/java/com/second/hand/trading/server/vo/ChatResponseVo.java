package com.second.hand.trading.server.vo;

import com.second.hand.trading.server.model.IdleItemModel;
import lombok.Builder;
import lombok.Data;

import java.util.List;

@Data
@Builder
public class ChatResponseVo {
    private String message;
    private Boolean success;
    private List<IdleItemModel> products;
    private Integer productsCount;
}
