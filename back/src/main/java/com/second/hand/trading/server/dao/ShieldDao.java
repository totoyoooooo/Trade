package com.second.hand.trading.server.dao;

import com.second.hand.trading.server.model.ShieldModel;
import org.apache.ibatis.annotations.Mapper;

import java.util.List;

@Mapper
public interface ShieldDao {

    int deleteByPrimaryKey(Long id);

    int insert(ShieldModel record);

    ShieldModel selectByPrimaryKey(Long id);

    List<ShieldModel> getMyShield(Long userId);

    Integer checkShield(Long userId,Long idleId);

    int updateByPrimaryKey(ShieldModel record);

    List<ShieldModel> getShieldByIdleItem(Long idleId);

}
