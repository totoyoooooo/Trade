package com.second.hand.trading.server.service.impl;

import com.second.hand.trading.server.dao.FavoriteDao;
import com.second.hand.trading.server.dao.IdleItemDao;
import com.second.hand.trading.server.model.FavoriteModel;
import com.second.hand.trading.server.model.IdleItemModel;
import com.second.hand.trading.server.model.OrderModel;
import com.second.hand.trading.server.service.FavoriteService;
import org.springframework.stereotype.Service;

import javax.annotation.Resource;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

@Service
public class FavoriteServiceImpl implements FavoriteService {

    @Resource
    private FavoriteDao favoriteDao;

    @Resource
    private IdleItemDao idleItemDao;

    /**
     * 新增收藏
     * @param favoriteModel
     * @return
     */
    public boolean addFavorite(FavoriteModel favoriteModel){
        return favoriteDao.insert(favoriteModel)==1;
    }

    /**
     * 删除收藏
     * @param id
     * @return
     */
    public boolean deleteFavorite(Long id){
        return favoriteDao.deleteByPrimaryKey(id)==1;
    }

    /**
     * 判断用户是否收藏某个闲置
     * user_id建索引
     * @param userId
     * @param idleId
     * @return
     */
    public Integer isFavorite(Long userId,Long idleId){
        return favoriteDao.checkFavorite(userId,idleId);
    }

    /**
     * 查询一个用户的所有收藏
     * 关联查询，没有用join，通过where in查询关联的闲置信息
     * @param userId
     * @return
     */
    public List<FavoriteModel> getAllFavorite(Long userId){
        List<FavoriteModel> list=favoriteDao.getMyFavorite(userId);
        for(FavoriteModel favoriteModel:list){
            favoriteModel.setIdleItem(idleItemDao.selectByPrimaryKey(favoriteModel.getIdleId()));
        }
        return list;
    }

    /**
     * 根据物品ID获取该物品被收藏量
     * @param idleId
     * @return
     */
    public List<FavoriteModel> getFavoriteByIdleItem(Long idleId){
        return favoriteDao.getFavoriteByIdleItem(idleId);
    }

    @Override
    public FavoriteModel getFavoriteById(Long id) {
        return favoriteDao.selectByPrimaryKey(id);
    }

}
