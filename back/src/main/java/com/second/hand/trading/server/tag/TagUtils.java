package com.second.hand.trading.server.tag;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.second.hand.trading.server.model.TagModel;
import com.second.hand.trading.server.model.UserModel;
import org.ansj.domain.Term;
import org.ansj.splitWord.analysis.ToAnalysis;

import java.util.*;

public class TagUtils {

    public static List<String> splitTagText(String tagText) {
        List<String> list = new ArrayList<>();
        if(!tagText.isEmpty()){
            String[] tags = tagText.split("#");
            for(int i = 1;i < tags.length;i++){
                list.add("#" + tags[i]);
            }
        }
        return list;
    }

    /**
     * 为用户添加tag,使用json进行储存,返回json.toString
     */
    public static String addTag(String tagObjects,String tagText){
        List<String> tags = splitTagText(tagText);
        ObjectMapper objectMapper = new ObjectMapper();
        List<TagObject> list = new ArrayList<>();
        if(tagObjects.isEmpty()){
            for(String t : tags){
                TagObject tag = new TagObject(t,1L,"");
                tag.setTime();
                list.add(tag);
            }
        }else{
            try {
                List<TagObject> tagObjectList = objectMapper.readValue(tagObjects, objectMapper.getTypeFactory().constructCollectionType(List.class, TagObject.class));
                List<String> removeList = new ArrayList<>();
                for(TagObject tag : tagObjectList){
                    for(String t : tags){
                        if(tag.getText().equals(t)){
                            tag.addCount();
                            tag.setTime();
                            removeList.add(t);
                            break;
                        }
                    }
                    list.add(tag);
                }
                tags.removeAll(removeList);
                if(!tags.isEmpty()){
                    for(String t : tags){
                        TagObject tag = new TagObject(t,1L,"");
                        tag.setTime();
                        list.add(tag);
                    }
                }
            } catch (JsonProcessingException e) {
                throw new RuntimeException(e);
            }
        }
        try {
            return objectMapper.writeValueAsString(list);
        } catch (JsonProcessingException e) {
            throw new RuntimeException(e);
        }
    }

    /**
     * 为用户删除tag,使用json进行储存,返回json.toString
     */
    public static String subtractTag(String tagObjects, String tagText){
        List<String> tags = splitTagText(tagText);
        ObjectMapper objectMapper = new ObjectMapper();
        List<TagObject> list = new ArrayList<>();
        if(!tagObjects.isEmpty()){
            try {
                List<TagObject> tagObjectList = objectMapper.readValue(tagObjects, objectMapper.getTypeFactory().constructCollectionType(List.class, TagObject.class));
                for(TagObject tag : tagObjectList){
                    for(String t : tags){
                        if(tag.getText().equals(t)){
                            tag.subtractCount();
                            tag.setTime();
                            break;
                        }
                    }
                    if(tag.getCount() != 0) list.add(tag);
                }
            } catch (JsonProcessingException e) {
                throw new RuntimeException(e);
            }
        }
        try {
            return objectMapper.writeValueAsString(list);
        } catch (JsonProcessingException e) {
            throw new RuntimeException(e);
        }
    }

    /**
     * 为用户移除tag,使用json进行储存,返回json.toString
     */
    public static String removeTag(String tagObjects, String tagText){
        List<String> tags = splitTagText(tagText);
        ObjectMapper objectMapper = new ObjectMapper();
        List<TagObject> list = new ArrayList<>();
        if(!tagObjects.isEmpty()){
            try {
                List<TagObject> tagObjectList = objectMapper.readValue(tagObjects, objectMapper.getTypeFactory().constructCollectionType(List.class, TagObject.class));
                for(TagObject tag : tagObjectList){
                    for(String t : tags){
                        if(tag.getText().equals(t)){
                            tag.setCount(0L);
                            tag.setTime();
                            break;
                        }
                    }
                    if(tag.getCount() != 0) list.add(tag);
                }
            } catch (JsonProcessingException e) {
                throw new RuntimeException(e);
            }
        }
        try {
            return objectMapper.writeValueAsString(list);
        } catch (JsonProcessingException e) {
            throw new RuntimeException(e);
        }
    }



    /**
     * 获取标签总数
     */
    public static int getTagCount(String tagText){
        ObjectMapper objectMapper = new ObjectMapper();
        if(tagText != null && !tagText.isEmpty()){
            try {
                int count = 0;
                List<TagObject> tagObjectList = objectMapper.readValue(tagText, objectMapper.getTypeFactory().constructCollectionType(List.class, TagObject.class));
                for(TagObject tagObject : tagObjectList){
                    count += tagObject.getCount();
                }
                return count;
            } catch (JsonProcessingException e) {
                System.out.println("错误" + tagText);
                throw new RuntimeException(e);
            }
        }
        return 0;
    }

    /**
     * 根据标签文本返回标签列表
     */
    public static List<TagObject> getTagObjects(String tagText){
        ObjectMapper objectMapper = new ObjectMapper();
        List<TagObject> list = new ArrayList<>();
        if(tagText != null && !tagText.isEmpty()){
            try {
                list = objectMapper.readValue(tagText, objectMapper.getTypeFactory().constructCollectionType(List.class, TagObject.class));
            } catch (Exception e) {
                System.out.println("错误" + tagText);
                throw new RuntimeException(e);
            }
        }
        return list;
    }

    /**
     * 返回基于用户行为计算的标签推荐度
     */
    public static Map<TagModel,Double> getTagRecommendation(UserModel userModel,List<TagModel> allTagList){
        Map<TagModel,Double> recommendationMap = new LinkedHashMap<>();
        //获取用户所有行为标签列表
        List<TagObject> userTagSkimList = TagUtils.getTagObjects(userModel.getSkimTag());
        List<TagObject> userTagCollectList = TagUtils.getTagObjects(userModel.getCollectTag());
        List<TagObject> userTagIssueList = TagUtils.getTagObjects(userModel.getIssueTag());
        List<TagObject> userTagShieldList = TagUtils.getTagObjects(userModel.getShieldTag());
        List<TagObject> userTagDecreaseList = TagUtils.getTagObjects(userModel.getDecreaseTag());
        for(TagModel tagModel : allTagList){
            double recommendationScore = 0;
            //根据浏览标签计算权重
            for(TagObject skimTag : userTagSkimList){
                if(tagModel.getText().equals(skimTag.getText())){
                    int skimPriority = 1;
                    if(getTagCount(userModel.getSkimTag()) >= 10){
                        if(skimTag.getCount() >= getTagCount(userModel.getSkimTag()) * 0.3) skimPriority = 4/3;
                        else if(skimTag.getCount() >= getTagCount(userModel.getSkimTag()) * 0.6) skimPriority = 5/3;
                        else if(skimTag.getCount() >= getTagCount(userModel.getSkimTag()) * 0.9) skimPriority = 2;
                    }
                    recommendationScore += skimTag.getCount() * skimPriority * Math.pow(Math.E,-0.1 * skimTag.calculateTimeDayDistance());
                    break;
                }
            }
            //根据收藏标签计算权重
            for(TagObject collectTag : userTagCollectList){
                if(tagModel.getText().equals(collectTag.getText())){
                    int collectPriority = 3;
                    if(getTagCount(userModel.getCollectTag()) >= 5){
                        if(collectTag.getCount() >= getTagCount(userModel.getCollectTag()) * 0.2) collectPriority = 7/2;
                        else if(collectTag.getCount() >= getTagCount(userModel.getCollectTag()) * 0.4) collectPriority = 4;
                        else if(collectTag.getCount() >= getTagCount(userModel.getCollectTag()) * 0.6) collectPriority = 9/2;
                        else if(collectTag.getCount() >= getTagCount(userModel.getCollectTag()) * 0.8) collectPriority = 5;
                    }
                    recommendationScore += collectTag.getCount() * collectPriority * Math.pow(Math.E,-0.1 * collectTag.calculateTimeDayDistance());
                    break;
                }
            }
            //根据发布标签计算权重
            for(TagObject issueTag : userTagIssueList){
                if(tagModel.getText().equals(issueTag.getText())){
                    int issuePriority = 5;
                    if(getTagCount(userModel.getIssueTag()) >= 3){
                        if(issueTag.getCount() >= getTagCount(userModel.getIssueTag()) * 0.1) issuePriority = 11/2;
                        else if(issueTag.getCount() >= getTagCount(userModel.getIssueTag()) * 0.4) issuePriority = 6;
                        else if(issueTag.getCount() >= getTagCount(userModel.getIssueTag()) * 0.7) issuePriority = 13/2;
                        else if(issueTag.getCount() >= getTagCount(userModel.getIssueTag())) issuePriority = 7;
                    }
                    recommendationScore += issueTag.getCount() * issuePriority * Math.pow(Math.E,-0.1 * issueTag.calculateTimeDayDistance());
                    break;
                }
            }
            //根据减少推荐标签计算权重
            for(TagObject decreaseTag : userTagDecreaseList){
                if(tagModel.getText().equals(decreaseTag.getText())){
                    int decreasePriority = -3;
                    if(getTagCount(userModel.getDecreaseTag()) >= 5){
                        if(decreaseTag.getCount() >= getTagCount(userModel.getDecreaseTag()) * 0.3) decreasePriority -= 3;
                        if(decreaseTag.getCount() >= getTagCount(userModel.getDecreaseTag()) * 0.6) decreasePriority -= 3;
                        if(decreaseTag.getCount() >= getTagCount(userModel.getDecreaseTag()) * 0.9) decreasePriority -= 3;
                    }
                    recommendationScore += decreaseTag.getCount() * decreasePriority * Math.pow(Math.E,-0.1 * decreaseTag.calculateTimeDayDistance());
                    break;
                }
            }
            //根据屏蔽标签计算权重
            for(TagObject shieldTag : userTagShieldList){
                if(tagModel.getText().equals(shieldTag.getText())){
                    int shieldPriority = -5;
                    if(getTagCount(userModel.getShieldTag()) >= 3){
                        if(shieldTag.getCount() >= getTagCount(userModel.getShieldTag()) * 0.2) shieldPriority -= 3;
                        if(shieldTag.getCount() >= getTagCount(userModel.getShieldTag()) * 0.4) shieldPriority -= 3;
                        if(shieldTag.getCount() >= getTagCount(userModel.getShieldTag()) * 0.6) shieldPriority -= 3;
                        if(shieldTag.getCount() >= getTagCount(userModel.getShieldTag()) * 0.8) shieldPriority -= 3;
                        if(shieldTag.getCount() >= getTagCount(userModel.getShieldTag())) shieldPriority -= 3;
                    }
                    recommendationScore += shieldTag.getCount() * shieldPriority * Math.pow(Math.E,-0.1 * shieldTag.calculateTimeDayDistance());
                    break;
                }
            }
            recommendationMap.put(tagModel,recommendationScore);
        }
        allTagList.removeAll(recommendationMap.keySet());
        for(TagModel tagModel : allTagList){
            double max = -1.0;
            double recommendation = 0.0;
            for(Map.Entry<TagModel,Double> entry : recommendationMap.entrySet()){
                double cos;
                if(max < (cos = calculateCosSimilarity(entry.getKey().getText(),tagModel.getText()))){
                    max = cos;
                    recommendation = entry.getValue() * cos;
                }
            }
            for(TagObject t : getTagObjects(userModel.getShieldTag())){
                if(tagModel.getText().equals(t.getText())){
                    if(recommendation >= 0) recommendation *= 0.5;
                    else recommendation *= 1.5;
                    break;
                }
            }
            recommendationMap.put(tagModel,recommendation);
        }
        return recommendationMap;
    }

    /**
     * 计算两个标签的余弦相似度
     */
    public static double calculateCosSimilarity(String tag1, String tag2){
        List<Term> list1 = ToAnalysis.parse(tag1).getTerms();
        List<Term> list2 = ToAnalysis.parse(tag2).getTerms();
        List<Integer> numList1 = new ArrayList<>();
        List<Integer> numList2 = new ArrayList<>();
        Set<Term> allSet = new HashSet<>();
        allSet.addAll(list1);
        allSet.addAll(list2);
        for(Term term : allSet){
            if(list1.contains(term)) numList1.add(1);
            else numList1.add(0);
            if(list2.contains(term)) numList2.add(1);
            else numList2.add(0);
        }
        double dotProduct = 0.0;
        double normA = 0.0;
        double normB = 0.0;
        for(int i = 0;i < numList1.size();i++){
            dotProduct += numList1.get(i) * numList2.get(i);
            normA += Math.pow(numList1.get(i), 2);
            normB += Math.pow(numList2.get(i), 2);
        }
        double ans = dotProduct / (Math.sqrt(normA) * Math.sqrt(normB));
        if(ans < 0.3) return 0;
        else return ans;
    }

}
