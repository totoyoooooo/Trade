package com.second.hand.trading.server.controller;

import com.second.hand.trading.server.enums.ErrorMsg;
import com.second.hand.trading.server.model.IdleItemModel;
import com.second.hand.trading.server.service.IdleItemService;
import com.second.hand.trading.server.service.UserService;
import com.second.hand.trading.server.tag.TagUtils;
import com.second.hand.trading.server.vo.ResultVo;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;

import javax.validation.constraints.NotEmpty;
import javax.validation.constraints.NotNull;
import java.util.Date;
@CrossOrigin
@RestController
@RequestMapping("idle")
public class IdleItemController {

    @Autowired
    private IdleItemService idleItemService;

    @Autowired
    private UserService userService;

    @PostMapping("add")
    public ResultVo addIdleItem(@CookieValue("shUserId")
                                @NotNull(message = "登录异常 请重新登录")
                                @NotEmpty(message = "登录异常 请重新登录") String shUserId,
                                @RequestBody IdleItemModel idleItemModel){
        System.out.println(idleItemModel);
        String tag = idleItemModel.getIdleTag();
        String tagObject = userService.getUser(Long.valueOf(shUserId)).getIssueTag() == null ? "" : userService.getUser(Long.valueOf(shUserId)).getIssueTag();
        String newIssueTag = TagUtils.addTag(tagObject,tag);
        userService.setIssueTag(Long.valueOf(shUserId),newIssueTag);
        idleItemModel.setUserId(Long.valueOf(shUserId));
        idleItemModel.setIdleStatus((byte) 1);
        idleItemModel.setReleaseTime(new Date());
        if(idleItemService.addIdleItem(idleItemModel)){
            return ResultVo.success(idleItemModel);
        }
        return ResultVo.fail(ErrorMsg.SYSTEM_ERROR);
    }

    @GetMapping("info")
    public ResultVo getIdleItem(@RequestParam(value = "id") Long id,
                                @RequestParam(value = "userId") String userId){
        if(!userId.isEmpty()){
            String tag = idleItemService.getIdleItem(id).getIdleTag();
            String tagObject = userService.getUser(Long.valueOf(userId)).getSkimTag() == null ? "" : userService.getUser(Long.valueOf(userId)).getSkimTag();
            String newSkimTag = TagUtils.addTag(tagObject,tag);
            userService.setSkimTag(Long.valueOf(userId),newSkimTag);
            idleItemService.addSkimCount(id);
        }
        return ResultVo.success(idleItemService.getIdleItem(id));
    }

    @GetMapping("all")
    public ResultVo getAllIdleItem(@CookieValue("shUserId")
                                   @NotNull(message = "登录异常 请重新登录")
                                   @NotEmpty(message = "登录异常 请重新登录") String shUserId){
        return ResultVo.success(idleItemService.getAllIdelItem(Long.valueOf(shUserId)));
    }

    @GetMapping("find")
    public ResultVo findIdleItem(@RequestParam(value = "userId",required = false) Long userId,
                                 @RequestParam(value = "findValue",required = false) String findValue,
                                 @RequestParam(value = "page",required = false) Integer page,
                                 @RequestParam(value = "nums",required = false) Integer nums){
        if(null==findValue){
            findValue="";
        }
        int p=1;
        int n=8;
        if(null!=page){
            p=page>0?page:1;
        }
        if(null!=nums){
            n=nums>0?nums:8;
        }
        return ResultVo.success(idleItemService.findIdleItem(userId,findValue,p,n));
    }

    @GetMapping("lable")
    public ResultVo findIdleItemByLable(@RequestParam(value = "userId",required = false) Long userId,
                                        @RequestParam(value = "idleLabel") Integer idleLabel,
                                        @RequestParam(value = "page",required = false) Integer page,
                                        @RequestParam(value = "nums",required = false) Integer nums){
        int p=1;
        int n=8;
        if(null!=page){
            p=page>0?page:1;
        }
        if(null!=nums){
            n=nums>0?nums:8;
        }
        return ResultVo.success(idleItemService.findIdleItemByLable(userId,idleLabel,p,n));
    }

    @PostMapping("update")
    public ResultVo updateIdleItem(@CookieValue("shUserId")
                                   @NotNull(message = "登录异常 请重新登录")
                                   @NotEmpty(message = "登录异常 请重新登录") String shUserId,
                                   @RequestBody IdleItemModel idleItemModel){
        idleItemModel.setUserId(Long.valueOf(shUserId));
        if(idleItemService.updateIdleItem(idleItemModel)){
            return ResultVo.success();
        }
        return ResultVo.fail(ErrorMsg.SYSTEM_ERROR);
    }

    @PostMapping("getUserIdle")
    public ResultVo getUserIdleItem(@RequestParam Long id){
        return ResultVo.success(idleItemService.getAllIdelItem(id));
    }

}
