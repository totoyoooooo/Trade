package com.second.hand.trading.server.controller;

import com.second.hand.trading.server.enums.ErrorMsg;
import com.second.hand.trading.server.model.TagModel;
import com.second.hand.trading.server.service.TagService;
import com.second.hand.trading.server.tag.TagUtils;
import com.second.hand.trading.server.vo.ResultVo;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@CrossOrigin
@RestController
@RequestMapping("tag")
public class TagController {

    @Autowired
    private TagService tagService;

    @PostMapping("createTag")
    public ResultVo createTag(@RequestParam String text) {
        List<String> list = TagUtils.splitTagText(text);
        for(String tag : list) {
            TagModel tagModel;
            if((tagModel = tagService.getTag(tag)) == null) {
                tagService.createTag(tag);
            }else{
                tagService.setTagCount(tagModel.getText(),tagModel.getUse_count() + 1);
            }
        }
        return ResultVo.success();
    }

    @PostMapping("getAkinTag")
    public ResultVo getAkinTag(@RequestParam String text) {
        text = text.replace("#","\\#");
        text += "%";
        List<TagModel> list = tagService.getAkinTag(text);
        if(!list.isEmpty()){
            System.out.println(list);
            return ResultVo.success(list);
        }
        return ResultVo.fail();
    }

    @PostMapping("getAllTag")
    public ResultVo getAllTag() {
        List<TagModel> list = tagService.getAllTag();
        if (list == null) {
            return ResultVo.fail(ErrorMsg.SYSTEM_ERROR);
        }
        return ResultVo.success(list);
    }

}
