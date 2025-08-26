package com.second.hand.trading.server.dao;

import com.second.hand.trading.server.model.WantedItemModel;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;

import java.util.List;

@Mapper
public interface WantedItemDao {

    int insertWantedItem(WantedItemModel wantedItemModel);

    int updateWantedItem(WantedItemModel wantedItemModel);

    int deleteWantedItem(@Param("id") Long id);

    WantedItemModel getWantedItemById(@Param("id") Long id);

    List<WantedItemModel> getWantedItemList(@Param("offset") int offset, @Param("limit") int limit, @Param("wantedName") String wantedName);

    int countAllWantedItems(@Param("wantedName") String wantedName);
}
