package com.second.hand.trading.server.service;

import com.second.hand.trading.server.model.TagModel;
import org.apache.ibatis.annotations.Param;

import java.util.List;

public interface TagService {

    int createTag(String text);

    int deleteTag(String text);

    int setTagCount(String text,Long use_count);

    TagModel getTag(String text);

    List<TagModel> getAkinTag(@Param("text") String text);

    List<TagModel> getAllTag();

}
