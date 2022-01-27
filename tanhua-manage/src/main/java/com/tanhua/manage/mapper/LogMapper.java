package com.tanhua.manage.mapper;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import com.tanhua.manage.domain.Log;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;
import org.apache.ibatis.annotations.Select;

@Mapper
public interface LogMapper extends BaseMapper<Log> {
    /**
     * 根据日期和类型 查询总数
     * @param todayDate
     * @param type
     * @return
     */
    @Select("select count(*) from tb_log where log_time = #{todayDate} and type = #{type}")
    Integer queryNumsByType(@Param("todayDate") String todayDate,@Param("type")String type);

    /**
     * 今日活跃用户数
     * @param todayDate
     * @return
     */
    @Select("select count(DISTINCT user_id) from tb_log where log_time = #{todayDate}")
    Integer queryNumsByDate(@Param("todayDate")String todayDate);

    /**
     * 次日留存用户数
     * @param todayDate
     * @param yesterdayDate
     * @return
     */
    @Select("select count(*) from tb_log where log_time = #{todayDate} and type = '0101'" +
            " and user_id in (" +
            "select user_id from tb_log where log_time = #{yesterdayDate} and type = '0102'" +
            ")")
    Integer queryRetention1d(@Param("todayDate")String todayDate,@Param("yesterdayDate")String yesterdayDate);
}
