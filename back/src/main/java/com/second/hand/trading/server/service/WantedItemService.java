package com.second.hand.trading.server.service;

import com.second.hand.trading.server.model.WantedItemModel;

import java.util.List;
import java.util.Map;

public interface WantedItemService {

    WantedItemModel addWantedItem(WantedItemModel wantedItemModel);

    Map<String, Object> getWantedItemList(int page, int limit, String wantedName);

    WantedItemModel getWantedItemById(Long id);

    boolean updateWantedItem(WantedItemModel wantedItemModel);

    boolean deleteWantedItem(Long id);

}
