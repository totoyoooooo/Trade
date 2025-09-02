package com.second.hand.trading.server.service.impl;

import com.second.hand.trading.server.dao.*;
import com.second.hand.trading.server.model.IdleItemModel;
import com.second.hand.trading.server.model.TagModel;
import com.second.hand.trading.server.model.UserModel;
import com.second.hand.trading.server.service.IdleItemService;
import com.second.hand.trading.server.tag.TagUtils;
import com.second.hand.trading.server.vo.PageVo;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import javax.annotation.Resource;
import java.util.*;
import java.util.stream.Collectors;

@Service
public class IdleItemServiceImpl implements IdleItemService {

    @Resource
    private IdleItemDao idleItemDao;

    @Resource
    private UserDao userDao;

    @Resource
    private FavoriteDao favoriteDao;

    @Resource
    private TagDao tagDao;
    @Autowired
    private ShieldDao shieldDao;

    /**
     * 发布闲置
     * @param idleItemModel
     * @return boolean
     */
    public boolean addIdleItem(IdleItemModel idleItemModel) {
        return idleItemDao.insert(idleItemModel) == 1;
    }

    /**
     * 查询闲置信息，同时查出发布者的信息
     * @param id
     * @return
     */
    public IdleItemModel getIdleItem(Long id) {
        IdleItemModel idleItemModel=idleItemDao.selectByPrimaryKey(id);
        if(idleItemModel!=null){
            UserModel user = userDao.selectByPrimaryKey(idleItemModel.getUserId());
            if (user != null) {
                idleItemModel.setUser(user);
            }
        }
        return idleItemModel;
    }

    /**
     * 查询用户发布的所有闲置
     * user_id建索引
     * @param userId
     * @return
     */
    public List<IdleItemModel> getAllIdelItem(Long userId) {
        return idleItemDao.getAllIdleItem(userId);
    }

    /**
     * 搜索，分页
     * 同时查出闲置发布者的信息
     * @param findValue
     * @param page
     * @param nums
     * @return
     */
    public PageVo<IdleItemModel> findIdleItem(Long userId,String findValue, int page, int nums) {
        List<IdleItemModel> list=idleItemDao.findIdleItem(findValue, (page - 1) * nums, nums);
        List<IdleItemModel> removeList = new ArrayList<>();
        for(IdleItemModel i : list){
            i.setUser(userDao.selectByPrimaryKey(i.getUserId()));
            // 如果userId不为空，才进行屏蔽检查
            if (userId != null && shieldDao.checkShield(userId,i.getId()) != null) {
                removeList.add(i);
            }
        }
        list.removeAll(removeList);
        int count=idleItemDao.countIdleItem(findValue) - removeList.size();
        return new PageVo<>(getIdleItemList(userId,list),count);
    }

    /**
     * 分类查询，分页
     * 同时查出闲置发布者的信息，代码结构与上面的类似，可封装优化，或改为join查询
     * @param idleLabel
     * @param page
     * @param nums
     * @return
     */
    public PageVo<IdleItemModel> findIdleItemByLable(Long userId,int idleLabel, int page, int nums) {
        List<IdleItemModel> list = idleItemDao.findIdleItemByLable(idleLabel, (page - 1) * nums, nums);
        List<IdleItemModel> removeList = new ArrayList<>();
        for(IdleItemModel i : list){
            i.setUser(userDao.selectByPrimaryKey(i.getUserId()));
            // 如果userId不为空，才进行屏蔽检查
            if (userId != null && shieldDao.checkShield(userId,i.getId()) != null) {
                removeList.add(i);
            }
        }
        list.removeAll(removeList);
        int count=idleItemDao.countIdleItemByLable(idleLabel) - removeList.size();
        return new PageVo<>(getIdleItemList(userId,list),count);
    }

    /**
     * 更新闲置信息
     * @param idleItemModel
     * @return
     */
    public boolean updateIdleItem(IdleItemModel idleItemModel){
        return idleItemDao.updateByPrimaryKeySelective(idleItemModel)==1;
    }

    @Override
    public int addSkimCount(Long id) {
        // 获取当前闲置商品
        IdleItemModel idleItemModel = idleItemDao.selectByPrimaryKey(id);
        if (idleItemModel == null) {
            return 0; // 或者抛出异常，根据业务需求决定
        }

        // 增加浏览次数
        if (idleItemModel.getSkimCount() == null) {
            idleItemModel.setSkimCount(1L);
        } else {
            idleItemModel.setSkimCount(idleItemModel.getSkimCount() + 1);
        }

        // 更新数据库
        return idleItemDao.updateByPrimaryKeySelective(idleItemModel);
    }

    public PageVo<IdleItemModel> adminGetIdleList(int status, int page, int nums) {
        List<IdleItemModel> list=idleItemDao.getIdleItemByStatus(status, (page - 1) * nums, nums);
        for(IdleItemModel i : list){
            i.setUser(userDao.selectByPrimaryKey(i.getUserId()));
        }
        int count=idleItemDao.countIdleItemByStatus(status);
        return new PageVo<>(list,count);
    }

    /**
     * 标签推荐度(根据用户各种行为进行计算)
     * 行为:浏览（1）收藏（3）发布（5）减少推荐（-3）屏蔽（-5）
     * 每个行为具有额外的加权，需要该行为有一定数量启动
     * 浏览（10）：30%：4/3 60%：5/3 90%：2
     * 收藏（5）：20%：7/2 40%：4 60%：9/2 80%：5
     * 发布（3）：10%：11/2 40%：6 70%：13/2 100%：7
     * 减少推荐（5）：30%：-6 60%：-9 90%：-12
     * 屏蔽（3）：20%：-8 40：-11 60%：-14 80%：-17 100%：-20
     * 标签推荐度等于该标签数量 * 行为权重 * 时间参数（e^(-0.1 * 天数)）
     * 若标签不属于用户行为中的标签，则根据相似度进行计算，等于相似度 * 标签推荐度
     * 若标签与屏蔽标签具有相似度，且为正值则推荐度 * 0.5 负值则推荐度 * 1.5
     * 将最终计算的推荐度加和取绝对值，再用各自推荐度除以和 * 100得到最终的标签推荐度
     * 商品推荐度
     * 参数: 物品浏览量 物品收藏量 买主交易次数 买主好评率
     * 计算公式: 物品浏览量 + 物品收藏量 * 3 + round(买主交易次数 * 买主好评率)
     * 最终推荐度
     * idle_blend_ratio * 商品推荐度 + tag_blend_ratio * 标签推荐度
     * idle_blend_ratio = 0.7 tag_blend_ratio = 0.3(新用户)
     * 当用户浏览标签总数超过20分位数并且收藏标签总数超过20分位数时
     * collectDivide = 收藏70分位数 - 收藏20分位数 == 0 ? 1 : 收藏70分位数 - 收藏20分位数;
     * skimDivide = 浏览70分位数 - 浏览20分位数 == 0 ? 1 : 浏览70分位数 - 浏览20分位数;
     * blendRatio = Math.min(1,(((用户收藏总数 - 收藏20分位数) / collectDivide) + ((用户浏览总数 - 浏览20分位数) / skimDivide)) / 2);
     * idle_ratio = 0.7 - 0.3 * blendRatio;
     * tag_ratio = 0.3 + 0.3 * blendRatio;
     * 根据最终推荐度进行推荐
     * todo 每个行为的加权阈值进行动态调整
     */
    @Override
    public List<IdleItemModel> getIdleItemList(Long userId,List<IdleItemModel> list) {
        if(userId != null){
            //获取用户信息
            UserModel userModel = userDao.selectByPrimaryKey(userId);
            //获取所有标签
            List<TagModel> allTagList = tagDao.getAllTag();
            //获取标签推荐度Map
            Map<TagModel,Double> tagMap = TagUtils.getTagRecommendation(userModel,allTagList);
            //更新标签推荐度值
            double finalTotal1 = Math.abs(tagMap.values().stream()
                    .mapToDouble(Double::doubleValue)
                    .sum() == 0 ? 1 : tagMap.values().stream()
                    .mapToDouble(Double::doubleValue)
                    .sum());
            tagMap.forEach((key, value) -> tagMap.put(key, (value / finalTotal1) * 100));
            //获取用户收藏量和浏览量
            int userCollectCount = TagUtils.getTagCount(userModel.getCollectTag());
            int userSkimCount = TagUtils.getTagCount(userModel.getSkimTag());
            //获取用户分位数列表
            List<Integer> collectCountList = new ArrayList<>();
            List<Integer> skimCountList = new ArrayList<>();
            for(UserModel i : userDao.getUserList()){
                int skimCount = TagUtils.getTagCount(i.getSkimTag());
                int collectCount = TagUtils.getTagCount(i.getCollectTag());
                collectCountList.add(collectCount);
                skimCountList.add(skimCount);
            }
            collectCountList.sort(Comparator.reverseOrder());
            skimCountList.sort(Comparator.reverseOrder());
            //获取20分位数和70分位数
            int collectP20Count = getQuantile(collectCountList,20);
            int skimP20Count = getQuantile(skimCountList,20);
            int collectP70Count = getQuantile(collectCountList,70);
            int skimP70Count = getQuantile(skimCountList,70);
            //商品权重和标签权重
            double idleItemRatio = 0.7;
            double tagRatio = 0.3;
            //根据收藏量和浏览量计算参数
            if(userCollectCount >= collectP20Count && userSkimCount >= skimP20Count){
                double collectDivide = collectP70Count - collectP20Count == 0 ? 1 : collectP70Count - collectP20Count;
                double skimDivide = skimP70Count - skimP20Count == 0 ? 1 : skimP70Count - skimP20Count;
                double blendRatio = Math.min(1,(((userCollectCount - collectP20Count) / collectDivide) + ((userSkimCount - skimP20Count) / skimDivide)) / 2);
                idleItemRatio = 0.7 - 0.3 * blendRatio;
                tagRatio = 0.3 + 0.3 * blendRatio;
            }
            //排序map
            Map<IdleItemModel,Double> sortMap = new LinkedHashMap<>();
            for(IdleItemModel i : list){
                if(i.getIdleStatus() != 1) continue;
                UserModel um = userDao.selectByPrimaryKey(i.getUserId());
                int collectedCount = favoriteDao.getFavoriteByIdleItem(i.getId()).size();
                Long skimCount = i.getSkimCount();
                Long tradeCount = um.getTradeCount();
                int applauseRate = um.getApplauseRate();
                sortMap.put(i, (double) collectedCount * 3L + skimCount + tradeCount * applauseRate);
            }
            //更新商品推荐度值
            double finalTotal = Math.abs(sortMap.values().stream()
                    .mapToDouble(Double::doubleValue)
                    .sum() == 0 ? 1 : sortMap.values().stream()
                    .mapToDouble(Double::doubleValue)
                    .sum());
            Map<IdleItemModel, Double> finalSortMap = sortMap;
            finalSortMap.forEach((key, value) -> finalSortMap.put(key, (value / finalTotal) * 100));
            System.out.println("标签推荐度:" + tagMap);
            System.out.println("商品推荐度:" + finalSortMap);
            Map<IdleItemModel,Double> ans = new LinkedHashMap<>();
            for(Map.Entry<IdleItemModel,Double> e : finalSortMap.entrySet()){
                double finalRecommendation = e.getValue() * idleItemRatio;
                List<String> idleTags = TagUtils.splitTagText(e.getKey().getIdleTag());
                for(String i : idleTags){
                    for(Map.Entry<TagModel,Double> e1 : tagMap.entrySet()){
                        if(i.equals(e1.getKey().getText())){
                            finalRecommendation += e1.getValue() * tagRatio;
                            break;
                        }
                    }
                }
                ans.put(e.getKey(),finalRecommendation);
            }
            System.out.println("最终推荐度为" + ans);
            List<IdleItemModel> sortedIdle = ans.entrySet()
                    .stream()
                    .sorted(Map.Entry.comparingByValue(Comparator.reverseOrder()))
                    .map(Map.Entry::getKey)
                    .collect(Collectors.toList());
            return sortedIdle;
        }else{
            //排序map
            Map<IdleItemModel,Double> sortMap = new LinkedHashMap<>();
            for(IdleItemModel i : list){
                if(i.getIdleStatus() != 1) continue;
                UserModel um = userDao.selectByPrimaryKey(i.getUserId());
                int collectedCount = favoriteDao.getFavoriteByIdleItem(i.getId()).size();
                Long skimCount = i.getSkimCount();
                Long tradeCount = um.getTradeCount();
                int applauseRate = um.getApplauseRate();
                sortMap.put(i, (double) collectedCount * 3L + skimCount + tradeCount * applauseRate);
            }
            sortMap = sortMap.entrySet()
                    .stream()
                    .sorted(Map.Entry.<IdleItemModel, Double>comparingByValue().reversed())
                    .collect(Collectors.toMap(
                            Map.Entry::getKey,
                            Map.Entry::getValue,
                            (e1, e2) -> e1,
                            LinkedHashMap::new));
            System.out.println("商品推荐度为:" + sortMap);
            //更新商品推荐度值
            double finalTotal = Math.abs(sortMap.values().stream()
                    .mapToDouble(Double::doubleValue)
                    .sum() == 0 ? 1 : sortMap.values().stream()
                    .mapToDouble(Double::doubleValue)
                    .sum());
            Map<IdleItemModel, Double> finalSortMap = sortMap;
            finalSortMap.forEach((key, value) -> finalSortMap.put(key, (value / finalTotal) * 100));
            System.out.println("最终推荐度为:" + finalSortMap);
            return new ArrayList<>(finalSortMap.keySet());
        }
    }

    private int getQuantile(List<Integer> list,int quantile){
        int size = list.size();
        if(size == 0 || quantile < 0 || quantile > 1) return -1;
        double index = (size - 1) * quantile;
        int lowerIndex = (int) Math.floor(index);
        int upperIndex = (int) Math.ceil(index);
        if(lowerIndex == upperIndex) return list.get(lowerIndex);
        return (int) Math.round(list.get(lowerIndex) + (list.get(upperIndex) - list.get(lowerIndex)) * (index - lowerIndex));
    }

}
