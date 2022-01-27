package com.tanhua.server.service;

import com.tanhua.domain.db.Question;
import com.tanhua.domain.mongo.RecommendUser;
import com.tanhua.domain.db.UserInfo;
import com.tanhua.domain.vo.PageResult;
import com.tanhua.domain.vo.RecommendUserQueryParam;
import com.tanhua.domain.vo.TodayBestVo;
import com.tanhua.dubbo.api.db.QuestionApi;
import com.tanhua.dubbo.api.db.UserInfoApi;
import com.tanhua.dubbo.api.mongo.RecommendUserApi;
import com.tanhua.server.interceptor.UserHolder;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.lang3.RandomUtils;
import org.apache.dubbo.config.annotation.Reference;
import org.springframework.beans.BeanUtils;
import org.springframework.stereotype.Service;
import org.springframework.util.CollectionUtils;
import org.springframework.util.StringUtils;

import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;

/**
 * 今日佳人业务处理层
 */
@Service
@Slf4j
public class TodayBestService {

    @Reference
    private RecommendUserApi recommendUserApi;
    
    @Reference
    private UserInfoApi userInfoApi;

    @Reference
    private QuestionApi questionApi;

    /**
     * 今日佳人
     */
    public TodayBestVo todayBest() {
        Long currentUserId = UserHolder.getUserId();
        //1.根据当前登录的用户id调用服务 查询今日佳人用户id、缘分值
        RecommendUser recommendUser = recommendUserApi.findByUserId(currentUserId);
        //2.如果今日佳人数据为空，则设置默认用户数据（为了提升用户体验）
        if(recommendUser == null){
            recommendUser = new RecommendUser();
            recommendUser.setUserId(1l);//默认1号用户
            recommendUser.setToUserId(currentUserId);//推荐给当前登录的用户id
            recommendUser.setScore(99.9d);///缘分值
            recommendUser.setDate(new SimpleDateFormat("yyyy-MM-dd").format(new Date()));//每天都会推荐今日佳人
        }
        //3.跟佳人用户id查询tb_userInfo表用户基础信息
        UserInfo userInfo = userInfoApi.findUserInfoById(recommendUser.getUserId());
        //4.构造vo返回
        TodayBestVo vo = new TodayBestVo();
        BeanUtils.copyProperties(userInfo,vo);//将头像 昵称 性别 年龄 copy到vo中
        if(!StringUtils.isEmpty(userInfo.getTags())){
            vo.setTags(userInfo.getTags().split(","));//标签
        }
        vo.setId(recommendUser.getUserId());//佳人用户id
        vo.setFateValue(recommendUser.getScore().longValue());//缘分值
        return vo;



    }

    /**
     * 推荐佳人分页列表数据
     */
    public PageResult<TodayBestVo> recommendation(RecommendUserQueryParam param) {
        Integer page = param.getPage();//当前页码
        Integer pagesize = param.getPagesize();//每页记录数
        Long currentUserId = UserHolder.getUserId();//获取当前用户id
        //1.调用服务-推荐佳人分页列表数据查询
        PageResult<RecommendUser> pageResult = recommendUserApi.recommendation(page,pagesize,currentUserId);
        //2.如果推荐佳人分页列表数据为空，构造默认数据
        if(pageResult == null || CollectionUtils.isEmpty(pageResult.getItems())){
            pageResult.setCounts(10l);
            pageResult.setPagesize(pagesize.longValue());
            pageResult.setPages(1l);
            pageResult.setPage(page.longValue());
            pageResult.setItems(defaultRecommend());
        }
        //3.如果推荐佳人分页列表数据不为空（userid 缘分值），
        List<RecommendUser> recommendUserList = pageResult.getItems();
        //4.根据推荐佳人用户id 查询用户基础信息
        List<TodayBestVo> todayBestVoList = new ArrayList<>();
        for (RecommendUser recommendUser : recommendUserList) {
            TodayBestVo vo = new TodayBestVo();
            Long userId = recommendUser.getUserId();//推荐佳人用户id
            UserInfo userInfo = userInfoApi.findUserInfoById(userId);
            BeanUtils.copyProperties(userInfo,vo);//将头像 昵称 性别 年龄 copy到vo中
            if(!StringUtils.isEmpty(userInfo.getTags())){
                vo.setTags(userInfo.getTags().split(","));//标签
            }
            vo.setId(recommendUser.getUserId());//佳人用户id
            vo.setFateValue(recommendUser.getScore().longValue());//缘分值
            todayBestVoList.add(vo);
        }
        PageResult<TodayBestVo> bestVoPageResult = new PageResult<>();
        BeanUtils.copyProperties(pageResult,bestVoPageResult);
        bestVoPageResult.setItems(todayBestVoList);
        //5.构造vo返回
        return bestVoPageResult;
    }


    //构造默认数据
    private List<RecommendUser> defaultRecommend() {
        String ids = "1,2,3,4,5,6,7,8,9,10";
        List<RecommendUser> records = new ArrayList<>();
        for (String id : ids.split(",")) {
            RecommendUser recommendUser = new RecommendUser();
            recommendUser.setUserId(Long.valueOf(id));
            recommendUser.setScore(RandomUtils.nextDouble(70, 98));
            records.add(recommendUser);
        }
        return records;
    }

    /**
     * 查看用户详情
     * personUserId:佳人的用户id
     */
    public TodayBestVo personalInfo(Long personUserId) {
        Long userId = UserHolder.getUserId();
        //1 查询userInfo
        UserInfo userInfo = userInfoApi.findUserInfoById(personUserId);
        //2 查询recommend_user 获取缘分值
        Double score = recommendUserApi.findByUserIdAndPersonId(personUserId,userId);
        //3 封装Vo返回
        TodayBestVo vo = new TodayBestVo();
        BeanUtils.copyProperties(userInfo,vo);//将头像 昵称 性别 年龄 copy到vo中
        if(!StringUtils.isEmpty(userInfo.getTags())){
            vo.setTags(userInfo.getTags().split(","));//标签
        }
        vo.setId(personUserId);//佳人用户id
        vo.setFateValue(score.longValue());//缘分值
        return vo;
    }

    /**
     * 查询陌生人问题
     */
    public String strangerQuestions(Long userId) {
        Question question = questionApi.findByUserId(userId);
        String content = "约吗?";
        if(question != null && !StringUtils.isEmpty(question.getTxt())){
            content = question.getTxt();
        }
        log.debug("************************查询陌生人问题***********************"+content);
        return content;
    }
}
