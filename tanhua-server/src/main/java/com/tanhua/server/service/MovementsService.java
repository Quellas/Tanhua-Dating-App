package com.tanhua.server.service;

import com.tanhua.commons.templates.OssTemplate;
import com.tanhua.domain.db.UserInfo;
import com.tanhua.domain.mongo.Comment;
import com.tanhua.domain.mongo.Publish;
import com.tanhua.domain.mongo.Visitor;
import com.tanhua.domain.vo.MomentVo;
import com.tanhua.domain.vo.PageResult;
import com.tanhua.domain.vo.PublishVo;
import com.tanhua.domain.vo.VisitorVo;
import com.tanhua.dubbo.api.db.UserInfoApi;
import com.tanhua.dubbo.api.mongo.CommentApi;
import com.tanhua.dubbo.api.mongo.PublishApi;
import com.tanhua.dubbo.api.mongo.VisitorApi;
import com.tanhua.server.interceptor.UserHolder;
import com.tanhua.server.utils.RelativeDateFormat;
import lombok.extern.slf4j.Slf4j;
import org.apache.dubbo.config.annotation.Reference;
import org.apache.rocketmq.spring.core.RocketMQTemplate;
import org.bson.types.ObjectId;
import org.springframework.beans.BeanUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.stereotype.Service;
import org.springframework.util.CollectionUtils;
import org.springframework.util.StringUtils;
import org.springframework.web.multipart.MultipartFile;

import java.io.IOException;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;

/**
 * 动态发布业务逻辑处理类
 */
@Service
@Slf4j
public class MovementsService {

    @Autowired
    private OssTemplate ossTemplate;

    @Reference
    private PublishApi publishApi;
    
    @Reference
    private UserInfoApi userInfoApi;

    @Reference
    private CommentApi commentApi;

    @Reference
    private VisitorApi visitorApi;

    @Autowired
    private RedisTemplate<String,String> redisTemplate;

    @Autowired
    private RocketMQTemplate rocketMQTemplate;


    /**
     * 发布动态-准备封装PublishVo 传入调用服务方法中
     * imageContent:多张图片
     * publishVo：文本数据
     */
    public void savePublish(PublishVo publishVo, MultipartFile[] imageContent) throws IOException {
        //1.获取用户id 设置到publishVo
        Long userId = UserHolder.getUserId();
        //2.将多张图片存入oss 获取图片地址集合
        List<String> medias = new ArrayList<>();
        if(imageContent != null && imageContent.length>0){
            for (MultipartFile multipartFile : imageContent) {
                String imgUrl = ossTemplate.upload(multipartFile.getOriginalFilename(), multipartFile.getInputStream());
                medias.add(imgUrl);
            }
        }
        //3.将图片地址集合设置到publishVo
        publishVo.setUserId(userId);//当前发布动态的用户iD
        publishVo.setMedias(medias);//图片地址集合数据
        //4.调用服务提供者 发布动态（4张表操作）
        String publishId = publishApi.savePublish(publishVo);
        //5.将动态发布id写入中间件
        rocketMQTemplate.convertAndSend("tanhua-publish",publishId);
        log.debug("*************将动态发布id写入中间件成功了******************************");
    }

    /**
     * 好友动态分页查询
     */
    public PageResult<MomentVo> findPagePublishByFriend(int page, int pagesize) {
        Long userId = UserHolder.getUserId();
        //1.根据当前用户id page pagesize 调用服务 分页查询动态数据(先查询时间线表 再查询发布表)
        PageResult<Publish> publishPageResult = publishApi.findPagePublishByFriend(userId,page,pagesize);
        return getMomentVoPageResult(page, pagesize, publishPageResult);
    }

    /**
     * 推荐动态分页查询（陌生人动态数据）
     */
    public PageResult<MomentVo> findPagePublishByRecommend(int page, int pagesize) {
        Long userId = UserHolder.getUserId();
        //1.根据当前用户id page pagesize 调用服务 分页查询动态数据(先查询推荐动态表 再查询发布表)
        PageResult<Publish> publishPageResult = publishApi.findPagePublishByRecommend(userId,page,pagesize);
        return getMomentVoPageResult(page, pagesize, publishPageResult);
    }

    /**
     * 分页展示动态数据的公共方法
     * @param page
     * @param pagesize
     * @param publishPageResult
     * @return
     */
    private PageResult<MomentVo> getMomentVoPageResult(long page, long pagesize, PageResult<Publish> publishPageResult) {
        if(publishPageResult == null || StringUtils.isEmpty(publishPageResult.getItems())){
            return new PageResult<>(0l, pagesize,0l, page,null);
        }
        //2.根据动态发布的用户id 查询 用户信息表
        //将List<Publish> 集合 转为 List<MomentVo>
        List<MomentVo> momentVoList = new ArrayList<>();
        for (Publish publish : publishPageResult.getItems()) {
            MomentVo vo = getMomentVo(publish);
            momentVoList.add(vo);
        }
        //3.将动态数据 与 用户信息 封装返回Vo
        PageResult<MomentVo> voPageResult = new PageResult<>();
        BeanUtils.copyProperties(publishPageResult,voPageResult);
        voPageResult.setItems(momentVoList);
        return voPageResult;
    }

    /**
     * 根据publish 返回MomentVo
     * @param publish
     * @return
     */
    private MomentVo getMomentVo(Publish publish) {
        MomentVo vo = new MomentVo();
        if(publish == null){
            return null;
        }
        Long publishUserId = publish.getUserId();//动态发布者的用户id
        UserInfo publishUserInfo = userInfoApi.findUserInfoById(publishUserId);
        BeanUtils.copyProperties(publishUserInfo,vo);//头像 昵称 性别 年龄
        BeanUtils.copyProperties(publish,vo);//文字动态 点赞数 评论数 喜欢数

        Long userId = UserHolder.getUserId();
        String likeKey = "like_"+userId+"_"+publish.getId().toHexString();
        log.debug("******getMomentVoPageResult**********"+likeKey+"*********************************");
        if(!StringUtils.isEmpty(redisTemplate.opsForValue().get(likeKey))){
            vo.setHasLiked(1);//是否点赞 1：是 0：否 app点赞小手是否选中的效果
        }else {
            vo.setHasLiked(0);//是否点赞 1：是 0：否 app点赞小手是否选中的效果
        }
        vo.setDistance("1米");
        String lovekey = "love_"+userId+"_"+publish.getId().toHexString();
        log.debug("******getMomentVoPageResult**********"+lovekey+"*********************************");
        if(!StringUtils.isEmpty(redisTemplate.opsForValue().get(lovekey))){
            vo.setHasLoved(1);//是否喜欢1：是 0：否 app喜欢爱心是否选中的效果
         }else {
            vo.setHasLoved(0);//是否喜欢1：是 0：否 app喜欢爱心是否选中的效果
        }
        vo.setCreateDate(RelativeDateFormat.format(new Date(publish.getCreated())));// 时间转换  例如：5分钟前 1天前 10个月前
        vo.setImageContent(publish.getMedias().toArray(new String[]{})); //图片地址
        if(!StringUtils.isEmpty(publishUserInfo.getTags())){
            vo.setTags(publishUserInfo.getTags().split(","));//标签
        }
        vo.setUserId(publishUserId);//动态发布者用户id
        vo.setId(publish.getId().toHexString());//设置动态id
        return vo;
    }

    /**
     * 用户动态（我的动态）  userId:用户id
     */
    public PageResult<MomentVo> findPagePublishByUserId(int page, int pagesize, Long userId) {
        //Long userId = UserHolder.getUserId();
        //1.根据当前用户id page pagesize 调用服务 分页查询动态数据(先查询相册表 再查询发布表)
        PageResult<Publish> publishPageResult = publishApi.findPagePublishByUserId(userId,page,pagesize);
        return getMomentVoPageResult(page, pagesize, publishPageResult);
    }
    /**
     * 动态点赞
     * publishId:动态发布id
     */
    public int like(String publishId) {
        Long userId = UserHolder.getUserId();//当前用户id
        Comment comment = new Comment();
        comment.setPublishId(new ObjectId(publishId));//动态发布id
        comment.setCommentType(1);//评论类型，1-点赞，2-评论，3-喜欢
        comment.setPubType(1);//评论内容类型： 1-对动态操作 2-对视频操作 3-对评论操作
        comment.setUserId(userId);//当前对动态评论的用户id
        //1.调用服务 保存点赞记录（评论表-保存记录 发布表-更新+1 查询点赞数量 ）
        int count = commentApi.like(comment);
        //2.将点赞记录写入redis 重点将key定义好即可
        String key = "like_"+userId+"_"+publishId;
        redisTemplate.opsForValue().set(key,"1");//redis有记录说明当前用户对此动态已经点赞了
        //3.返回点赞数量
        return count;
    }

    /**
     * 动态取消点赞
     * publishId:动态发布id
     */
    public int dislike(String publishId) {
        Long userId = UserHolder.getUserId();//当前用户id
        Comment comment = new Comment();
        comment.setPublishId(new ObjectId(publishId));//动态发布id
        comment.setCommentType(1);//评论类型，1-点赞，2-评论，3-喜欢
        comment.setPubType(1);//评论内容类型： 1-对动态操作 2-对视频操作 3-对评论操作
        comment.setUserId(userId);//当前对动态评论的用户id
        //1.调用服务 取消点赞记录（评论表-删除记录 发布表-更新-1 查询点赞数量 ）
        int count = commentApi.dislike(comment);
        //2.将点赞记录从redis删除 key必须跟点赞的要一样
        String key = "like_"+userId+"_"+publishId;
        redisTemplate.delete(key);//redis有记录说明当前用户对此动态已经点赞了
        //3.返回点赞数量
        return count;
    }
    /**
     * 动态喜欢
     * publishId:动态发布id
     */
    public int love(String publishId) {
        Long userId = UserHolder.getUserId();//当前用户id
        Comment comment = new Comment();
        comment.setPublishId(new ObjectId(publishId));//动态发布id
        comment.setCommentType(3);//评论类型，1-点赞，2-评论，3-喜欢
        comment.setPubType(1);//评论内容类型： 1-对动态操作 2-对视频操作 3-对评论操作
        comment.setUserId(userId);//当前对动态评论的用户id
        //1.调用服务 保存喜欢记录（评论表-保存记录 发布表-更新+1 查询喜欢数量 ）
        int count = commentApi.like(comment);
        //2.将喜欢记录写入redis 重点将key定义好即可
        String key = "love_"+userId+"_"+publishId;
        redisTemplate.opsForValue().set(key,"1");//redis有记录说明当前用户对此动态已经喜欢了
        //3.返回喜欢数量
        return count;
    }

    /**
     * 动态取消喜欢
     * publishId:动态发布id
     */
    public int unlove(String publishId) {
        Long userId = UserHolder.getUserId();//当前用户id
        Comment comment = new Comment();
        comment.setPublishId(new ObjectId(publishId));//动态发布id
        comment.setCommentType(3);//评论类型，1-点赞，2-评论，3-喜欢
        comment.setPubType(1);//评论内容类型： 1-对动态操作 2-对视频操作 3-对评论操作
        comment.setUserId(userId);//当前对动态评论的用户id
        //1.调用服务 取消喜欢记录（评论表-删除记录 发布表-更新-1 查询喜欢数量 ）
        int count = commentApi.dislike(comment);
        //2.将喜欢记录从redis删除 key必须跟喜欢的要一样
        String key = "love_"+userId+"_"+publishId;
        redisTemplate.delete(key);//redis有记录说明当前用户对此动态已经喜欢了
        //3.返回喜欢数量
        return count;
    }

    /**
     * 单条动态
     * publishId:动态发布id
     */
    public MomentVo findPublishById(String publishId) {
        Publish publish= publishApi.findPublishById(publishId);
        return getMomentVo(publish);
    }

    /**
     * 谁看过我
     */
    public List<VisitorVo> findVisitors() {
        Long userId = UserHolder.getUserId();
        String key = "visitor_"+userId;
        //1查询redis看上次登录时间是否存在
        String redisTime = redisTemplate.opsForValue().get(key);
        //2如果redis上次登录时间存在 则根据date>redis中上次登录时间 前5条
        List<Visitor> list = new ArrayList<>();
        if(!StringUtils.isEmpty(redisTime)){
            list = visitorApi.findVisitors(userId,redisTime);
        }else {
            //3如果redis上次登录时间不存在 则直接查询前5条
            list = visitorApi.findVisitors(userId);
        }
        //4.根据上面查询的访客记录 查询访客用户基础信息
        List<VisitorVo> visitorVoList = new ArrayList<>();
        if(!CollectionUtils.isEmpty(list)){
            for (Visitor visitor : list) {
                VisitorVo visitorVo = new VisitorVo();

                Long visitorUserId = visitor.getVisitorUserId();//访客用户id
                UserInfo userInfo = userInfoApi.findUserInfoById(visitorUserId);
                BeanUtils.copyProperties(userInfo,visitorVo);//用户id 头像 昵称 性别  年龄
                if(!StringUtils.isEmpty(userInfo.getTags())){
                    visitorVo.setTags(userInfo.getTags().split(","));
                }
                visitorVo.setFateValue(visitor.getScore().intValue());
                visitorVoList.add(visitorVo);
            }
        }
        redisTemplate.opsForValue().set(key,System.currentTimeMillis()+"");
        //5.封装Vo返回
        return visitorVoList;
    }
}
