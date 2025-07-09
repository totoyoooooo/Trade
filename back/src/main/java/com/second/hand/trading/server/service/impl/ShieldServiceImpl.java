package com.second.hand.trading.server.service.impl;

import com.second.hand.trading.server.dao.IdleItemDao;
import com.second.hand.trading.server.dao.ShieldDao;
import com.second.hand.trading.server.model.ShieldModel;
import com.second.hand.trading.server.service.ShieldService;
import org.springframework.stereotype.Service;

import javax.annotation.Resource;
import java.util.List;

@Service
public class ShieldServiceImpl implements ShieldService {

    @Resource
    private ShieldDao shieldDao;

    @Resource
    private IdleItemDao idleItemDao;

    @Override
    public boolean addShield(ShieldModel shieldModel) {
        return shieldDao.insert(shieldModel) == 1;
    }

    @Override
    public boolean deleteShield(Long id) {
        return shieldDao.deleteByPrimaryKey(id) == 1;
    }

    @Override
    public Integer isShield(Long userId, Long idleId) {
        return shieldDao.checkShield(userId, idleId);
    }

    @Override
    public List<ShieldModel> getAllShield(Long userId) {
        List<ShieldModel> list=shieldDao.getMyShield(userId);
        for(ShieldModel shieldModel:list){
            shieldModel.setIdleItem(idleItemDao.selectByPrimaryKey(shieldModel.getIdleId()));
        }
        return list;
    }

    @Override
    public List<ShieldModel> getShieldByIdleItem(Long idleId) {
        return shieldDao.getShieldByIdleItem(idleId);
    }

    @Override
    public ShieldModel getShieldById(Long id) {
        return shieldDao.selectByPrimaryKey(id);
    }
}
