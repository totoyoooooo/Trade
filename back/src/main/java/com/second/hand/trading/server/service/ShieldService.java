package com.second.hand.trading.server.service;

import com.second.hand.trading.server.model.ShieldModel;

import java.util.List;

public interface ShieldService {

    /**
     * 添加屏蔽
     */
    boolean addShield(ShieldModel shieldModel);

    /**
     * 取消屏蔽
     */
    boolean deleteShield(Long id);

    /**
     * 判断是否屏蔽
     */
    Integer isShield(Long userId,Long idleId);

    /**
     * 获取屏蔽列表
     */
    List<ShieldModel> getAllShield(Long userId);

    /**
     *  根据物品ID获取所有屏蔽物品
     */
    List<ShieldModel> getShieldByIdleItem(Long idleId);

    ShieldModel getShieldById(Long id);

}
