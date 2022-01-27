package com.tanhua.server.service;

import com.alibaba.fastjson.JSON;
import com.tanhua.commons.templates.HuanXinTemplate;
import com.tanhua.domain.db.Question;
import com.tanhua.domain.db.UserInfo;
import com.tanhua.domain.mongo.Comment;
import com.tanhua.domain.mongo.Friend;
import com.tanhua.domain.vo.ContactVo;
import com.tanhua.domain.vo.MessageVo;
import com.tanhua.domain.vo.PageResult;
import com.tanhua.dubbo.api.db.QuestionApi;
import com.tanhua.dubbo.api.db.UserInfoApi;
import com.tanhua.dubbo.api.mongo.CommentApi;
import com.tanhua.dubbo.api.mongo.FriendApi;
import com.tanhua.server.interceptor.UserHolder;
import com.tanhua.server.utils.RelativeDateFormat;
import lombok.extern.slf4j.Slf4j;
import org.apache.dubbo.config.annotation.Reference;
import org.springframework.beans.BeanUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.util.StringUtils;

import java.util.*;

/**
 * 即时通讯业务逻辑处理类
 */
@Service
@Slf4j
public class IMService {

    @Autowired
    private HuanXinTemplate huanXinTemplate;

    @Reference
    private UserInfoApi userInfoApi;

    @Reference
    private QuestionApi questionApi;

    @Reference
    private FriendApi friendApi;

    @Reference
    private CommentApi commentApi;

    /**
     * 回复陌生人问题
     */
    public void replyStrangerQuestions(Long personUserId, String reply) {
        Long userId = UserHolder.getUserId();
        //1.根据当前登录用户查询当前用户信息
        UserInfo userInfo = userInfoApi.findUserInfoById(userId);
        //2.根据陌生人的用户id 查询陌生人问题
        Question question = questionApi.findByUserId(personUserId);
        String content = "约吗?";
        if(question != null && !StringUtils.isEmpty(question.getTxt())){
            content = question.getTxt();
        }
        //3.调用环信sendMsg方法发送消息
        //String target:给谁发送消息的用户id  String msg消息内容
        Map<String, Object> map = new HashMap<>();
        map.put("userId", userId.toString());//当前登录用户的id
        map.put("nickname", userInfo.getNickname());//当前登录用户的昵称
        map.put("strangerQuestion", content);//佳人的陌生人问题
        map.put("reply",reply);
        String msg = JSON.toJSONString(map);
        huanXinTemplate.sendMsg(personUserId.toString(),msg);
        log.debug("************{}给{}发送陌聊消息，消息内容{}********************",userId,personUserId,msg);
    }

    /**
     * 联系人添加
     */
    public void saveContacts(Long personUserId) {
        Long userId = UserHolder.getUserId();
        //1调用服务 让当前用户 跟 陌生人 成为好友
        friendApi.saveContacts(userId,personUserId);
        //2调用环信通信 让当前用户 跟 陌生人 成为好友
        huanXinTemplate.makeFriends(userId,personUserId);
    }

    /**
     * 联系人列表分页列表数据
     */
    public PageResult<ContactVo> findPageByFriend(int page, int pagesize) {
        Long userId = UserHolder.getUserId();
        //1.根据当前用户id 查询联系人列表分页列表数据
        PageResult<Friend> friendPageResult = friendApi.findPageByFriend(page,pagesize,userId);
        if(friendPageResult == null || StringUtils.isEmpty(friendPageResult.getItems())){
            return new PageResult<>(0l, (long)pagesize,0l, (long)page,null);
        }
        //2.根据好友ids查询userInfo
        //将List<Friend> 转为List<ContactVo>
        List<ContactVo> contactVoList = new ArrayList<>();
        long count = 1l;
        for (Friend friend : friendPageResult.getItems()) {
            ContactVo contactVo= new ContactVo();
            Long friendId = friend.getFriendId();
            UserInfo userInfo = userInfoApi.findUserInfoById(friendId);
            BeanUtils.copyProperties(userInfo,contactVo);//头像 昵称 性别 年龄 城市
            contactVo.setUserId(userInfo.getId().toString());//好友id
            contactVo.setId(count);//编号
            contactVoList.add(contactVo);
            count++;
        }
        //3.封装Vo返回
        PageResult<ContactVo> voPageResult = new PageResult<>();
        BeanUtils.copyProperties(friendPageResult,voPageResult);
        voPageResult.setItems(contactVoList);
        return voPageResult;
    }

    /**
     * 点赞 评论 喜欢列表分页列表数据
     * //评论类型，1-点赞，2-评论，3-喜欢
     */
    public PageResult<MessageVo> messageCommentList(int type, int page, int pagesize) {
        Long userId = UserHolder.getUserId();
        //1.根据发布动态用户id 和 评论类型 分页查询动态评论表
        PageResult<Comment> pageResult = commentApi.findPageByType(userId,type,page,pagesize);
        if(pageResult == null || StringUtils.isEmpty(pageResult.getItems())){
            return new PageResult<>(0l, (long)pagesize,0l, (long)page,null);
        }
        //2.根据评论的用户id 查询UserInfo
        //将List<Comment>转为 List<MessageVo>
        List<MessageVo> messageVoList = new ArrayList<>();
        for (Comment comment : pageResult.getItems()) {
            MessageVo messageVo = new MessageVo();
            Long commentUserId = comment.getUserId(); //评论人的用户id
            UserInfo userInfo = userInfoApi.findUserInfoById(commentUserId);
            BeanUtils.copyProperties(userInfo,messageVo);//头像 昵称
            messageVo.setCreateDate(RelativeDateFormat.format(new Date(comment.getCreated())));//xx分钟之前
            messageVo.setId(commentUserId.toString());//评论人的用户id
            messageVoList.add(messageVo);
        }
        //3.封装Vo返回
        PageResult<MessageVo> voPageResult = new PageResult<>();
        BeanUtils.copyProperties(pageResult,voPageResult);
        voPageResult.setItems(messageVoList);
        return voPageResult;
    }
}
