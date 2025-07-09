package com.second.hand.trading.server.service.impl;

import com.second.hand.trading.server.dao.TagDao;
import com.second.hand.trading.server.model.TagModel;
import com.second.hand.trading.server.service.TagService;
import org.springframework.stereotype.Service;

import javax.annotation.Resource;
import java.util.List;

@Service
public class TagServiceImpl implements TagService {

    @Resource
    private TagDao tagDao;

    @Override
    public int createTag(String text) {
        return tagDao.createTag(text);
    }

    @Override
    public int deleteTag(String text) {
        return tagDao.deleteTag(text);
    }

    @Override
    public int setTagCount(String text, Long use_count) {
        return tagDao.setTagCount(text, use_count);
    }

    @Override
    public TagModel getTag(String text) {
        return tagDao.getTag(text);
    }

    @Override
    public List<TagModel> getAkinTag(String text) {
        return tagDao.getAkinTag(text);
    }

    @Override
    public List<TagModel> getAllTag() {
        return tagDao.getAllTag();
    }
}
