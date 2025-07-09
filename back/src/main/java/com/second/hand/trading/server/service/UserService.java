package com.second.hand.trading.server.service;

import com.second.hand.trading.server.model.UserModel;
import com.second.hand.trading.server.vo.PageVo;

import java.util.List;

public interface UserService {

    /**
     * 获取某个用户的公开信息
     * @param id
     * @return
     */
    UserModel getUser(Long id);

    /**
     * 登录接口
     * @param accountNumber
     * @return
     */
    UserModel getUserByAccountNumber(String accountNumber);

    /**
     * 获取密码接口
     * @param accountNumber
     * @return
     */
    String getPassword(String accountNumber);

    /**
     * 注册接口
     * @param userModel
     * @return
     */
    boolean userSignIn(UserModel userModel);

    /**
     * 更新用户信息
     * @param userModel
     * @return
     */
    boolean updateUserInfo(UserModel userModel);

    /**
     * 修改密码
     * @param newPassword
     * @param oldPassword
     * @param id
     * @return
     */
    boolean updatePassword(String newPassword, String oldPassword,Long id);

    PageVo<UserModel> getUserByStatus(int status, int page , int nums);

    int setCollectTag(Long id,String collectTag);

    int setSkimTag(Long id,String skimTag);

    int setIssueTag(Long id,String issueTag);

    int setShieldTag(Long id,String shieldTag);

    int setDecreaseTag(Long id,String decreaseTag);

    Long getTradeCount(Long id);

    int getApplauseRate(Long id);

    int setTradeCount(Long id,Long tradeCount);

    int setApplauseRate(Long id,int applauseRate);

}
