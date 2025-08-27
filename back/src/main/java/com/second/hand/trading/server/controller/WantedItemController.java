package com.second.hand.trading.server.controller;

import com.second.hand.trading.server.model.WantedItemModel;
import com.second.hand.trading.server.service.WantedItemService;
import com.second.hand.trading.server.vo.ResultVo;
import org.springframework.web.bind.annotation.*;

import javax.annotation.Resource;
import java.util.Date;
import java.util.Map;
import com.second.hand.trading.server.enums.ErrorMsg;

@RestController
@RequestMapping("/wantedItem")
public class WantedItemController {

    @Resource
    private WantedItemService wantedItemService;

    @PostMapping
    public ResultVo addWantedItem(@RequestBody WantedItemModel wantedItemModel) {
        wantedItemModel.setPostTime(new Date());
        wantedItemModel.setWantedStatus((byte)1);
        WantedItemModel result = wantedItemService.addWantedItem(wantedItemModel);
        if (result != null) {
            return ResultVo.success(result);
        }
        return ResultVo.fail(ErrorMsg.SYSTEM_ERROR);
    }

    @GetMapping
    public ResultVo getWantedItemList(@RequestParam(value = "page", defaultValue = "1") int page,
                                      @RequestParam(value = "limit", defaultValue = "10") int limit,
                                      @RequestParam(value = "wantedName", defaultValue = "") String wantedName) {
        Map<String, Object> map = wantedItemService.getWantedItemList(page, limit, wantedName);
        return ResultVo.success(map);
    }

    @GetMapping("/{id}")
    public ResultVo getWantedItemById(@PathVariable("id") Long id) {
        WantedItemModel wantedItem = wantedItemService.getWantedItemById(id);
        if (wantedItem != null) {
            return ResultVo.success(wantedItem);
        }
        return ResultVo.fail(ErrorMsg.SYSTEM_ERROR);
    }

    @PutMapping
    public ResultVo updateWantedItem(@RequestBody WantedItemModel wantedItemModel) {
        if (wantedItemService.updateWantedItem(wantedItemModel)) {
            return ResultVo.success();
        }
        return ResultVo.fail(ErrorMsg.SYSTEM_ERROR);
    }

    @DeleteMapping("/{id}")
    public ResultVo deleteWantedItem(@PathVariable("id") Long id) {
        if (wantedItemService.deleteWantedItem(id)) {
            return ResultVo.success();
        }
        return ResultVo.fail(ErrorMsg.SYSTEM_ERROR);
    }
}
