package com.tanhua.dubbo.api.mongo;

import com.tanhua.domain.mongo.Friend;
import com.tanhua.domain.vo.PageResult;

/**
 * 好友服务接口
 */
public interface FriendApi {
    /**
     * 联系人添加
     */
    void saveContacts(Long userId, Long personUserId);


    /**
     * 根据当前用户id 查询联系人列表分页列表数据
     * @param page
     * @param pagesize
     * @param userId
     * @return
     */
    PageResult<Friend> findPageByFriend(int page, int pagesize, Long userId);

    /**
     * 查询互相喜欢数量
     * @param userId
     * @return
     */
    Long findCountById(Long userId);
}
