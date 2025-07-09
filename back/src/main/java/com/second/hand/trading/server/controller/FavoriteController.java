package com.second.hand.trading.server.controller;

import com.second.hand.trading.server.enums.ErrorMsg;
import com.second.hand.trading.server.model.FavoriteModel;
import com.second.hand.trading.server.service.FavoriteService;
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
@RequestMapping("/favorite")
public class FavoriteController {

    @Autowired
    private FavoriteService favoriteService;

    @Autowired
    private IdleItemService idleItemService;

    @Autowired
    private UserService userService;

    @PostMapping("/add")
    public ResultVo addFavorite(@CookieValue("shUserId")
                                @NotNull(message = "登录异常 请重新登录")
                                @NotEmpty(message = "登录异常 请重新登录") String shUserId,
                                @RequestBody FavoriteModel favoriteModel){
        favoriteModel.setUserId(Long.valueOf(shUserId));
        favoriteModel.setCreateTime(new Date());
        if(favoriteService.addFavorite(favoriteModel)){
            String tag = idleItemService.getIdleItem(favoriteModel.getIdleId()).getIdleTag();
            String tagObject = userService.getUser(Long.valueOf(shUserId)).getCollectTag() == null ? "" : userService.getUser(Long.valueOf(shUserId)).getCollectTag();
            String newCollectTag = TagUtils.addTag(tagObject,tag);
            userService.setCollectTag(Long.valueOf(shUserId),newCollectTag);
            return ResultVo.success(favoriteModel.getId());
        }
        return ResultVo.fail(ErrorMsg.FAVORITE_EXIT);
    }

    @GetMapping("/delete")
    public ResultVo deleteFavorite(@CookieValue("shUserId")
                                   @NotNull(message = "登录异常 请重新登录")
                                   @NotEmpty(message = "登录异常 请重新登录") String shUserId,
                                   @RequestParam Long id){
        FavoriteModel favoriteModel = favoriteService.getFavoriteById(id);
        if(favoriteService.deleteFavorite(id)){
            String tag = idleItemService.getIdleItem(favoriteModel.getIdleId()).getIdleTag();
            String tagObject = userService.getUser(Long.valueOf(shUserId)).getCollectTag() == null ? "" : userService.getUser(Long.valueOf(shUserId)).getCollectTag();
            String newCollectTag = TagUtils.subtractTag(tagObject,tag);
            userService.setCollectTag(Long.valueOf(shUserId),newCollectTag);
            return ResultVo.success();
        }
        return ResultVo.fail(ErrorMsg.SYSTEM_ERROR);
    }

    @GetMapping("/check")
    public ResultVo checkFavorite(@CookieValue("shUserId")
                                  @NotNull(message = "登录异常 请重新登录")
                                  @NotEmpty(message = "登录异常 请重新登录") String shUserId,
                                  @RequestParam Long idleId){
        return ResultVo.success(favoriteService.isFavorite(Long.valueOf(shUserId),idleId));
    }

    @GetMapping("/my")
    public ResultVo getMyFavorite(@CookieValue("shUserId")
                                  @NotNull(message = "登录异常 请重新登录")
                                  @NotEmpty(message = "登录异常 请重新登录") String shUserId){
        return ResultVo.success(favoriteService.getAllFavorite(Long.valueOf(shUserId)));
    }
}
