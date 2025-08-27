package com.second.hand.trading.server.service.impl;

import com.second.hand.trading.server.dao.UserDao;
import com.second.hand.trading.server.model.UserModel;
import com.second.hand.trading.server.service.UserService;
import com.second.hand.trading.server.vo.PageVo;
import org.springframework.stereotype.Service;

import javax.annotation.Resource;
import java.util.List;



@Service
public class UserServiceImpl implements UserService {

    @Resource
    private UserDao userDao;

    /**
     * 查询一个用户的公开信息
     * @param id
     * @return
     */
    public UserModel getUser(Long id){
        return userDao.selectByPrimaryKey(id);
    }

    /**
     * 登录，安全问题未解决
     * @param accountNumber
     * @return
     */
    public UserModel getUserByAccountNumber(String accountNumber){
        return userDao.getUserByAccountNumber(accountNumber);
    }

    /**
     * 获取密码
     * @param accountNumber
     * @return
     */
    public String getPassword(String accountNumber){
        return userDao.getPassword(accountNumber);
    }

    /**
     *注册
     * @param userModel
     * @return
     */
    public boolean userSignIn(UserModel userModel){
        return userDao.insert(userModel) == 1;
    }

    /**
     *修改用户公开信息，未验证用户身份
     * @param userModel
     * @return
     */
    public boolean updateUserInfo(UserModel userModel){
        return userDao.updateByPrimaryKeySelective(userModel)==1;
    }

    /**
     * 修改密码
     * @param newPassword
     * @param oldPassword
     * @param id
     * @return
     */
    public boolean updatePassword(String newPassword, String oldPassword,Long id){
        return userDao.updatePassword(newPassword,oldPassword,id)==1;
    }

    public PageVo<UserModel> getUserByStatus(int status,int page ,int nums){
        List<UserModel> list;
        int count=0;
        if(status==0){
            count=userDao.countNormalUser();
            list=userDao.getNormalUser((page-1)*nums, nums);
        }else {
            count=userDao.countBanUser();
            list=userDao.getBanUser((page-1)*nums, nums);
        }
        return new PageVo<>(list,count);
    }

    @Override
    public int setCollectTag(Long id, String collectTag) {
        UserModel userModel = new UserModel();
        userModel.setId(id);
        userModel.setCollectTag(collectTag);
        return userDao.updateByPrimaryKeySelective(userModel);
    }

    @Override
    public int setSkimTag(Long id, String skimTag) {
        UserModel userModel = new UserModel();
        userModel.setId(id);
        userModel.setSkimTag(skimTag);
        return userDao.updateByPrimaryKeySelective(userModel);
    }

    @Override
    public int setIssueTag(Long id, String issueTag) {
        UserModel userModel = new UserModel();
        userModel.setId(id);
        userModel.setIssueTag(issueTag);
        return userDao.updateByPrimaryKeySelective(userModel);
    }

    @Override
    public int setShieldTag(Long id, String shieldTag) {
        UserModel userModel = new UserModel();
        userModel.setId(id);
        userModel.setShieldTag(shieldTag);
        return userDao.updateByPrimaryKeySelective(userModel);
    }

    @Override
    public int setDecreaseTag(Long id, String decreaseTag) {
        UserModel userModel = new UserModel();
        userModel.setId(id);
        userModel.setDecreaseTag(decreaseTag);
        return userDao.updateByPrimaryKeySelective(userModel);
    }

    @Override
    public Long getTradeCount(Long id) {
        return userDao.getTradeCount(id);
    }

    @Override
    public int getApplauseRate(Long id) {
        return userDao.getApplauseRate(id);
    }

    @Override
    public int setTradeCount(Long id, Long tradeCount) {
        UserModel userModel = new UserModel();
        userModel.setId(id);
        userModel.setTradeCount(tradeCount);
        return userDao.updateByPrimaryKeySelective(userModel);
    }

    @Override
    public int setApplauseRate(Long id, int applauseRate) {
        UserModel userModel = new UserModel();
        userModel.setId(id);
        userModel.setApplauseRate(applauseRate);
        return userDao.updateByPrimaryKeySelective(userModel);
    }
}
