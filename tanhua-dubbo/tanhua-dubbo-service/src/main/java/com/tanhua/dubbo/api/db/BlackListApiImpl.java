package com.tanhua.dubbo.api.db;

import com.baomidou.mybatisplus.core.conditions.Wrapper;
import com.baomidou.mybatisplus.core.conditions.query.QueryWrapper;
import com.baomidou.mybatisplus.core.metadata.IPage;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.tanhua.domain.db.BlackList;
import com.tanhua.domain.db.UserInfo;
import com.tanhua.domain.mongo.AudioRecommendRecord;
import com.tanhua.domain.vo.PageResult;
import com.tanhua.dubbo.mapper.BlackListMapper;
import org.apache.dubbo.config.annotation.Service;
import org.springframework.beans.factory.annotation.Autowired;

import java.util.List;

/**
 * 黑名单管理服务接口实现类
 */
@Service
public class BlackListApiImpl implements BlackListApi{

    @Autowired
    private BlackListMapper blackListMapper;

    /**
     *黑名单分页查询
     * select tui.* from tb_user_info tui,tb_black_list tbl where tui.id = tbl.black_user_id  and tbl.user_id = 10005 limit 0,10
     * //方式一：先查询A表select black_user_id from tb_black_list where user_id = 10005  再根据A表结果查询B表select * from tb_user_info where id in()
     * @param page
     * @param pagesize
     * @param userId
     * @return
     */
    @Override
    public PageResult<UserInfo> findPageBlackList(int page, int pagesize, Long userId) {
        //自定义分页方法 传入参数page对象 返回值 IPage<UserInfo>
        Page<UserInfo> queryPage = new Page(page,pagesize);
        IPage<UserInfo> uip = blackListMapper.findPageBlackList(queryPage,userId);
        return new PageResult<>(uip.getTotal(),uip.getSize(),uip.getPages(),uip.getCurrent(),uip.getRecords());
    }

    /**
     * 移除黑名单
     * @param userId
     * @param blackUserId
     */
    @Override
    public void deleteBlackUser(Long userId, Long blackUserId) {
        QueryWrapper<BlackList> queryWrapper = new QueryWrapper<>();
        queryWrapper.eq("user_id",userId);
        queryWrapper.eq("black_user_id",blackUserId);
        blackListMapper.delete(queryWrapper);
    }

    @Override
    public List<BlackList> findByUserId(Long userId) {
        QueryWrapper<BlackList> queryWrapper=new QueryWrapper<>();
        queryWrapper.eq("user_id",userId);
        return blackListMapper.selectList(queryWrapper);
    }


    /**
     * 探花-不喜欢
    * */
    @Override
    public void insertUnloveUserId(AudioRecommendRecord audioRecommendRecord) {
        BlackList blackList = new BlackList();
        blackList.setUserId(audioRecommendRecord.getToUserId());
        blackList.setBlackUserId(audioRecommendRecord.getUserId());
        blackListMapper.insert(blackList);
    }
}
