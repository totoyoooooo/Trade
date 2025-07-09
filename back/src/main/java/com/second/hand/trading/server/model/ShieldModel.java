package com.second.hand.trading.server.model;

import lombok.Getter;
import lombok.Setter;

import java.util.Date;

@Getter
@Setter
public class ShieldModel {

    /**
     * 自增主键id
     */
    private Long id;

    /**
     * 加入收藏的时间
     */
    private Date createTime;

    /**
     * 用户主键id
     */
    private Long userId;

    /**
     * 闲置物主键id
     */
    private Long idleId;

    private IdleItemModel idleItem;

}
