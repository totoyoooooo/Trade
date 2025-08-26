package com.second.hand.trading.server.service.impl;

import com.second.hand.trading.server.dao.WantedItemDao;
import com.second.hand.trading.server.model.WantedItemModel;
import com.second.hand.trading.server.service.WantedItemService;
import org.springframework.stereotype.Service;

import javax.annotation.Resource;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

@Service
public class WantedItemServiceImpl implements WantedItemService {

    @Resource
    private WantedItemDao wantedItemDao;

    @Override
    public WantedItemModel addWantedItem(WantedItemModel wantedItemModel) {
        if (wantedItemDao.insertWantedItem(wantedItemModel) > 0) {
            return wantedItemModel;
        }
        return null;
    }

    @Override
    public Map<String, Object> getWantedItemList(int page, int limit, String wantedName) {
        int offset = (page - 1) * limit;
        List<WantedItemModel> list = wantedItemDao.getWantedItemList(offset, limit, wantedName);
        int count = wantedItemDao.countAllWantedItems(wantedName);
        Map<String, Object> map = new HashMap<>();
        map.put("count", count);
        map.put("data", list);
        return map;
    }

    @Override
    public WantedItemModel getWantedItemById(Long id) {
        return wantedItemDao.getWantedItemById(id);
    }

    @Override
    public boolean updateWantedItem(WantedItemModel wantedItemModel) {
        return wantedItemDao.updateWantedItem(wantedItemModel) > 0;
    }

    @Override
    public boolean deleteWantedItem(Long id) {
        return wantedItemDao.deleteWantedItem(id) > 0;
    }
}
