package com.second.hand.trading.server.dao;

import com.second.hand.trading.server.model.UserModel;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;

import java.util.List;

@Mapper
public interface UserDao {
    int deleteByPrimaryKey(Long id);

    int insert(UserModel record);

    int insertSelective(UserModel record);

    UserModel getUserByAccountNumber(@Param("accountNumber") String accountNumber);

    String getPassword(@Param("accountNumber") String accountNumber);

    UserModel selectByPrimaryKey(Long id);

    List<UserModel> getUserList();

    List<UserModel> findUserByList(List<Long> idList);

    List<UserModel> getNormalUser(int begin, int nums);

    List<UserModel> getBanUser(int begin, int nums);

    int countNormalUser();

    int countBanUser();

    int updateByPrimaryKeySelective(UserModel record);

    int updateByPrimaryKey(UserModel record);

    int updatePassword(@Param("newPassword") String newPassword,
                       @Param("oldPassword") String oldPassword,@Param("id") Long id);
}