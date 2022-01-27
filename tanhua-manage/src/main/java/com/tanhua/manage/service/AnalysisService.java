package com.tanhua.manage.service;

import cn.hutool.core.date.DateField;
import cn.hutool.core.date.DateTime;
import cn.hutool.core.date.DateUtil;
import com.baomidou.mybatisplus.core.conditions.Wrapper;
import com.baomidou.mybatisplus.core.conditions.query.QueryWrapper;
import com.baomidou.mybatisplus.core.toolkit.Wrappers;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.tanhua.manage.domain.AnalysisByDay;
import com.tanhua.manage.mapper.AnalysisByDayMapper;
import com.tanhua.manage.mapper.LogMapper;
import com.tanhua.manage.utils.ComputeUtil;
import com.tanhua.manage.vo.AnalysisSummaryVo;
import com.tanhua.manage.vo.AnalysisUsersVo;
import com.tanhua.manage.vo.DataPointVo;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.Date;
import java.util.List;
import java.util.stream.Collectors;

/**
 * 概要统计分析业务逻辑处理类
 */
@Service
@Slf4j
public class AnalysisService extends ServiceImpl<AnalysisByDayMapper, AnalysisByDay> {

    @Autowired
    private LogMapper logMapper;

    @Autowired
    private AnalysisByDayMapper analysisByDayMapper;
 
    /**
     * 首页-概要统计数据展示
     */
    public AnalysisSummaryVo getSummary() {
        AnalysisSummaryVo vo = new AnalysisSummaryVo();
        vo.setCumulativeUsers(getCumulativeUsers());//累计用户数
        vo.setActivePassMonth(queryUserCount(getTime(-30), getTime(0), "num_active"));//过去30天活跃用户数
        vo.setActivePassWeek(queryUserCount(getTime(-7), getTime(0), "num_active"));//过去7天活跃用户

        Long todayRegistered = queryUserCount(getTime(0), getTime(0), "num_registered");
        vo.setNewUsersToday(todayRegistered);//今日新增用户数量
        Long yesterdayRegistered = queryUserCount(getTime(-1), getTime(-1), "num_registered");
        vo.setNewUsersTodayRate(ComputeUtil.computeRate(todayRegistered, yesterdayRegistered));//今日新增用户涨跌率


        Long todaylogin = queryUserCount(getTime(0), getTime(0), "num_login");
        vo.setLoginTimesToday(todaylogin);//今日登录次数
        Long yesterdaylogin = queryUserCount(getTime(-1), getTime(-1), "num_login");
        vo.setLoginTimesTodayRate(ComputeUtil.computeRate(todaylogin, yesterdaylogin));//今日登录次数涨跌率


        Long todayActive = queryUserCount(getTime(0), getTime(0), "num_active");
        vo.setActiveUsersToday(todayActive);//今日活跃用户数量
        Long yesterdayActive = queryUserCount(getTime(-1), getTime(-1), "num_active");
        vo.setActiveUsersTodayRate(ComputeUtil.computeRate(todayActive, yesterdayActive));//今日活跃用户涨跌率
        return vo;
    }

    /**
     * 获取累计用户数量
     */
    public Long getCumulativeUsers() {
        QueryWrapper<AnalysisByDay> queryWrapper = new QueryWrapper<>();
        AnalysisByDay analysisByDay = getOne(queryWrapper.select("sum(num_registered) as numRegistered"));
        return Long.parseLong(analysisByDay.getNumRegistered().toString());
    }

    /**
     * 根据起始时间 与 结束时间 以及 查询sum列 得到结果
     */
    public Long queryUserCount(String startTime, String endTime, String column) {
        QueryWrapper<AnalysisByDay> queryWrapper = new QueryWrapper<>();
        AnalysisByDay analysisByDay = getOne(
                queryWrapper.select("sum(" + column + ") as numRegistered")
                        .ge("record_date", startTime)
                        .le("record_date", endTime)
        );
        return Long.parseLong(analysisByDay.getNumRegistered().toString());
    }

    /**
     * 处理时间的方法
     *
     * @param num
     * @return
     */
    public static String getTime(int num) {
        return DateUtil.offsetDay(new Date(), num).toDateStr();
    }




    /**
     * 新增、活跃用户、次日留存率
     */
    public AnalysisUsersVo queryAnalysisUsersVo(Long sd, Long ed, Integer type) {

        DateTime startDate = DateUtil.date(sd);

        DateTime endDate = DateUtil.date(ed);

        AnalysisUsersVo analysisUsersVo = new AnalysisUsersVo();

        //今年数据
        analysisUsersVo.setThisYear(this.queryDataPointVos(startDate, endDate, type));
        //去年数据
        analysisUsersVo.setLastYear(this.queryDataPointVos(
                DateUtil.offset(startDate, DateField.YEAR, -1),
                DateUtil.offset(endDate, DateField.YEAR, -1), type)
        );

        return analysisUsersVo;
    }

    private List<DataPointVo> queryDataPointVos(DateTime sd, DateTime ed, Integer type) {

        String startDate = sd.toDateStr();

        String endDate = ed.toDateStr();

        String column = null;
        switch (type) { //101 新增 102 活跃用户 103 次日留存率
            case 101:
                column = "num_registered";
                break;
            case 102:
                column = "num_active";
                break;
            case 103:
                column = "num_retention1d";
                break;
            default:
                column = "num_active";
                break;
        }

        List<AnalysisByDay> analysisByDayList = super.list(Wrappers.<AnalysisByDay>query()
                .select("record_date , " + column + " as num_active")
                .ge("record_date", startDate)
                .le("record_date", endDate));

        return analysisByDayList.stream()
                .map(analysisByDay -> new DataPointVo(DateUtil.date(analysisByDay.getRecordDate()).toDateStr(), analysisByDay.getNumActive().longValue()))
                .collect(Collectors.toList());
    }


    /**
     * 概要统计数据-从日志表统计数据到统计分析表中
     */
    public void analysis() {
        //1先根据record_date查询统计分析表记录是否存在
        String todayDate = DateUtil.format(new Date(), "yyyy/MM/dd");
        String yesterdayDate = ComputeUtil.offsetDay(new Date(), -1);//昨天时间
        String newYesterdayDate  = DateUtil.format(DateUtil.parse(yesterdayDate), "yyyy/MM/dd");
        QueryWrapper<AnalysisByDay> queryWrapper = new QueryWrapper<>();
        queryWrapper.eq("record_date",todayDate);//条件
        AnalysisByDay analysisByDay = analysisByDayMapper.selectOne(queryWrapper);
        //1.1 分别查询数据库获取今日新注册用户数
        Integer numRegistered = logMapper.queryNumsByType(todayDate,"0102");
        //1.2 活跃用户数
        Integer numActive=  logMapper.queryNumsByDate(todayDate);
        //1.3 登录次数
        Integer numLogin = logMapper.queryNumsByType(todayDate,"0101");
        //1.4 次日留存用户数
        Integer  numRetention1d = logMapper.queryRetention1d(todayDate,newYesterdayDate);
        Date nowDate = new Date();
        //2如果存在 则根据record_date更新统计分析表记录
        if(analysisByDay != null){
            analysisByDay.setNumRegistered(numRegistered);//新注册用户数
            analysisByDay.setNumActive(numActive);//今日活跃用数
            analysisByDay.setNumLogin(numLogin);//今日登录次数
            analysisByDay.setNumRetention1d(numRetention1d);//次日留存用户数
            analysisByDay.setUpdated(nowDate);
            analysisByDayMapper.updateById(analysisByDay);
        }else {
            //3如果不存在， 直接往统计分析报表保存记录
            analysisByDay = new AnalysisByDay();
            analysisByDay.setRecordDate(DateUtil.parseDate(todayDate));//当前日期
            analysisByDay.setNumRegistered(numRegistered);//新注册用户数
            analysisByDay.setNumActive(numActive);//今日活跃用数
            analysisByDay.setNumLogin(numLogin);//今日登录次数
            analysisByDay.setNumRetention1d(numRetention1d);//次日留存用户数
            analysisByDay.setUpdated(nowDate);
            analysisByDay.setCreated(nowDate);
            analysisByDayMapper.insert(analysisByDay);
        }
    }


}
