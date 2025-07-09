package com.second.hand.trading.server.dao;

import com.second.hand.trading.server.model.TagModel;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;

import java.util.List;

@Mapper
public interface TagDao {

    int createTag(@Param("text") String text);

    int deleteTag(@Param("text") String text);

    int setTagCount(@Param("text") String text,@Param("use_count") Long use_count);

    TagModel getTag(@Param("text") String text);

    List<TagModel> getAkinTag(@Param("text") String text);

    List<TagModel> getAllTag();

}
