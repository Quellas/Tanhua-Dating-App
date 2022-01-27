package com.tanhua.dubbo.api.mongo;

import com.tanhua.domain.mongo.AudioRecommendRecord;
import com.tanhua.domain.mongo.RecommendUser;
import com.tanhua.domain.vo.PageResult;

/**
 * 喜欢服务接口
 */
public interface UserLikeApi {
    /**
     * 根据当前用户id查询喜欢数量
     * @param userId
     * @return
     */
    Long findCountByUserId(Long userId);

    /**
     * 根据当前用户id查询粉丝数量
     * @param userId
     * @return
     */
    Long findCountByLikeUserId(Long userId);

    /**
     *互相喜欢
     * @param userId
     * @param page
     * @param pagesize
     * @return
     */
    PageResult<RecommendUser> findPageLikeEachOther(Long userId, int page, int pagesize);

    /**
     *我喜欢
     * @param userId
     * @param page
     * @param pagesize
     * @return
     */
    PageResult<RecommendUser> findPageOneSideLike(Long userId, int page, int pagesize);

    /**
     *粉丝
     * @param userId
     * @param page
     * @param pagesize
     * @return
     */
    PageResult<RecommendUser> findPageFens(Long userId, int page, int pagesize);

    /**
     * 谁看过我
     * @param userId
     * @param page
     * @param pagesize
     * @return
     */
    PageResult<RecommendUser> findPageMyVisitors(Long userId, int page, int pagesize);

    /**
     * 粉丝-喜欢
     */
    void removeFansLike(Long fansUserId, Long userId);

    /*
    * 探花-喜欢
    * */
    void insertLoveUserId(AudioRecommendRecord audioRecommendRecord);
}
