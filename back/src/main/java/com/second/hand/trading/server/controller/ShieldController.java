package com.second.hand.trading.server.controller;

import com.second.hand.trading.server.enums.ErrorMsg;
import com.second.hand.trading.server.model.ShieldModel;
import com.second.hand.trading.server.service.IdleItemService;
import com.second.hand.trading.server.service.ShieldService;
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
@RequestMapping("/shield")
public class ShieldController {

    @Autowired
    private ShieldService shieldService;

    @Autowired
    private IdleItemService idleItemService;

    @Autowired
    private UserService userService;

    @PostMapping("/add")
    public ResultVo addShield(@CookieValue("shUserId")
                              @NotNull(message = "登录异常 请重新登录")
                              @NotEmpty(message = "登录异常 请重新登录") String shUserId,
                              @RequestBody ShieldModel shieldModel){
        shieldModel.setUserId(Long.valueOf(shUserId));
        shieldModel.setCreateTime(new Date());
        if(shieldService.addShield(shieldModel)){
            String tag = idleItemService.getIdleItem(shieldModel.getIdleId()).getIdleTag();
            String tagObject = userService.getUser(Long.valueOf(shUserId)).getShieldTag() == null ? "" : userService.getUser(Long.valueOf(shUserId)).getShieldTag();
            String newShieldTag = TagUtils.addTag(tagObject,tag);
            userService.setShieldTag(Long.valueOf(shUserId),newShieldTag);
            return ResultVo.success(shieldModel.getId());
        }
        return ResultVo.fail(ErrorMsg.FAVORITE_EXIT);
    }

    @GetMapping("/delete")
    public ResultVo deleteShield(@CookieValue("shUserId")
                                 @NotNull(message = "登录异常 请重新登录")
                                 @NotEmpty(message = "登录异常 请重新登录") String shUserId,
                                 @RequestParam Long id){
        ShieldModel shieldModel = shieldService.getShieldById(id);
        if(shieldService.deleteShield(id)){
            String tag = idleItemService.getIdleItem(shieldModel.getIdleId()).getIdleTag();
            String tagObject = userService.getUser(Long.valueOf(shUserId)).getShieldTag() == null ? "" : userService.getUser(Long.valueOf(shUserId)).getShieldTag();
            String newShield = TagUtils.subtractTag(tagObject,tag);
            userService.setShieldTag(Long.valueOf(shUserId),newShield);
            return ResultVo.success();
        }
        return ResultVo.fail(ErrorMsg.SYSTEM_ERROR);
    }

    @GetMapping("/check")
    public ResultVo checkShield(@CookieValue("shUserId")
                                @NotNull(message = "登录异常 请重新登录")
                                @NotEmpty(message = "登录异常 请重新登录") String shUserId,
                                @RequestParam Long idleId){
        return ResultVo.success(shieldService.isShield(Long.valueOf(shUserId),idleId));
    }

    @GetMapping("/my")
    public ResultVo getMyShield(@CookieValue("shUserId")
                                @NotNull(message = "登录异常 请重新登录")
                                @NotEmpty(message = "登录异常 请重新登录") String shUserId){
        return ResultVo.success(shieldService.getAllShield(Long.valueOf(shUserId)));
    }

    @GetMapping("/decrease")
    public ResultVo decreaseRecommendation(@CookieValue("shUserId")
                                           @NotNull(message = "登录异常 请重新登录")
                                           @NotEmpty(message = "登录异常 请重新登录") String shUserId,
                                           @RequestParam Long id){
        String tag = idleItemService.getIdleItem(id).getIdleTag();
        String tagObject = userService.getUser(Long.valueOf(shUserId)).getDecreaseTag() == null ? "" : userService.getUser(Long.valueOf(shUserId)).getDecreaseTag();
        String newDecreaseTag = TagUtils.addTag(tagObject,tag);
        userService.setDecreaseTag(Long.valueOf(shUserId),newDecreaseTag);
        tagObject = userService.getUser(Long.valueOf(shUserId)).getSkimTag() == null ? "" : userService.getUser(Long.valueOf(shUserId)).getSkimTag();
        String newSkimTag = TagUtils.removeTag(tagObject,tag);
        userService.setSkimTag(Long.valueOf(shUserId),newSkimTag);
        return ResultVo.success();
    }

}
