package com.second.hand.trading.server.model;

import lombok.Getter;
import lombok.Setter;

/**
 * 该类用于对商品进行排序
 */
@Getter
@Setter
public class RecommendIdleItemModel {

    private IdleItemModel idleItemModel;

    private int score;

    public RecommendIdleItemModel(IdleItemModel idleItemModel, int score) {
        this.idleItemModel = idleItemModel;
        this.score = score;
    }

}
