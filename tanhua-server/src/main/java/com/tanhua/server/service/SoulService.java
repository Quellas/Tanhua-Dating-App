package com.tanhua.server.service;

import cn.hutool.core.date.DateUtil;
import com.tanhua.domain.db.UserInfo;
import com.tanhua.domain.mongo.SoulConclusion;
import com.tanhua.domain.mongo.SoulUserResult;
import com.tanhua.domain.vo.AnswerVo;
import com.tanhua.domain.vo.Dimension;
import com.tanhua.domain.vo.ReportVo;
import com.tanhua.domain.vo.SoulQuestionVo;
import com.tanhua.domain.mongo.*;
import com.tanhua.domain.vo.*;
import com.tanhua.dubbo.api.db.UserInfoApi;
import com.tanhua.dubbo.api.mongo.SoulApi;
import com.tanhua.server.interceptor.UserHolder;
import org.apache.commons.collections.CollectionUtils;
import com.tanhua.dubbo.api.mongo.*;
import com.tanhua.server.interceptor.UserHolder;
import org.apache.dubbo.config.annotation.Reference;
import org.springframework.beans.BeanUtils;
import org.springframework.stereotype.Service;
import org.springframework.util.StringUtils;


import java.util.ArrayList;
import java.util.Calendar;
import java.util.Date;
import java.util.List;

/**
 * 测灵魂的业务层
 */
@Service
public class SoulService {

    @Reference
    private SoulApi soulApi;

    @Reference
    private UserInfoApi userInfoApi;

    @Reference
    private SoulQuestionApi soulQuestionApi;

    @Reference
    private QuestionnaireApi questionnaireApi;

    @Reference
    private OptionApi optionApi;

    @Reference
    private SoulUserResultApi soulUserResultApi;

    /**
     * 问卷列表
     * @return
     */
    public List<QuestionnaireVo> questionList() {
        // 获取当前登录用户Id
        Long userId = UserHolder.getUserId();
        // 1.查询问卷表
        List<Questionnaire> questionnaireList = questionnaireApi.findQuestionnaires();
        if (questionnaireList == null) {
            // 设置默认的问卷数据 设置为锁定状态
            return getDefaultQuestionnaireVoList();
        }
        // 遍历问卷列表
        ArrayList<QuestionnaireVo> questionnaireVoList = new ArrayList<>();
        for (Questionnaire questionnaire : questionnaireList) {
            QuestionnaireVo questionnaireVo = new QuestionnaireVo();
            // 2.将Questionnaire转为QuestionnaireVo
            BeanUtils.copyProperties(questionnaire, questionnaireVo); // 问卷名称 级别 星级
            questionnaireVo.setCover(questionnaire.getCoverUrl()); // 问卷封面 字段名称可在优化 todo
            questionnaireVo.setId(questionnaire.getQuestionnaireId().toString()); // 问卷编号

            // 2.1根据问卷id查询问题表
            List<SoulQuestion> soulQuestionList = soulQuestionApi.findQuestions(questionnaire.getQuestionnaireId());
            if (soulQuestionList == null) {
                // 设置默认的问卷数据 设置为锁定状态
                return getDefaultQuestionnaireVoList();
            }
            // 遍历问题列表
            List<SoulQuestionVo> soulQuestionVoList = new ArrayList<>();
            for (SoulQuestion soulQuestion : soulQuestionList) {
                // 2.1.1将SoulQuestion转化为SoulQuestionVo 问题id 问题内容
                SoulQuestionVo soulQuestionVo = new SoulQuestionVo();
                soulQuestionVo.setId(soulQuestion.getQuestionId().toString());
                soulQuestionVo.setQuestion(soulQuestion.getQuestionName());

                // 2.1.2根据问题id查询选项列表
                List<Option> optionList = optionApi.findOptions(soulQuestion.getQuestionId());
                if (optionList == null) {
                    // 设置默认的问卷数据 设置为锁定状态
                    return getDefaultQuestionnaireVoList();
                }
                // 遍历选项列表
                List<OptionVo> optionVoList = new ArrayList<>();
                for (Option option : optionList) {
                    // 将Option转化为OptionVo 选项id 选项内容
                    OptionVo optionVo = new OptionVo();
                    optionVo.setId(option.getId().toHexString());
                    optionVo.setOption(option.getOption());
                    // 将Option加到optionVoList
                    optionVoList.add(optionVo);
                }
                // 2.2设置问题的选项属性
                soulQuestionVo.setOptions(optionVoList);
                // 将soulQuestionVo添加到soulQuestionVoList
                soulQuestionVoList.add(soulQuestionVo);
            }

            // 2.3设置问卷的问题列表属性值
            questionnaireVo.setQuestions(soulQuestionVoList);

            // 2.4根据用户id和level查询用户答题结果表,设置问卷的锁定状态 报告id
            questionnaireVo = setIsLockAndReportIdOfQuestionnaireVo(questionnaireVo);

            // 2.5将QuestionnaireVo添加到questionnaireVoList
            questionnaireVoList.add(questionnaireVo);

            /*
            String level = questionnaire.getLevel();
            // 如果是初级问卷
            if (level.equals("初级")) {
                // 根据用户id和level查询用户答题结果表
                SoulUserResult soulUserResult = soulUserResultApi.findByUserIdAndLevel(userId, level);// 中级问卷答题记录
                // 设置初级问卷的锁定状态
                questionnaireVo.setIsLock(0); // 解锁状态
                // 设置初级问卷的报告id
                if (soulUserResult != null) {
                    questionnaireVo.setReportId(soulUserResult.getId().toHexString());
                }
            }
            // 如果是中级问卷
            if (level.equals("中级")) {
                // 根据用户id和level查询用户答题结果表
                SoulUserResult soulUserResult = soulUserResultApi.findByUserIdAndLevel(userId, level);// 中级问卷答题记录
                SoulUserResult soulUserResult1 = soulUserResultApi.findByUserIdAndLevel(userId, "初级");
                // 如果用户已有初级问卷答题记录,且中级问卷答题记录为null,则设置中级问卷为解锁状态
                if (soulUserResult1 != null && soulUserResult == null) {
                    questionnaireVo.setIsLock(0); // 解锁状态
                }
                // 如果用户已有初级问卷答题记录,且也有中级问卷答题记录,则设置中级问卷为解锁状态,并设置中级报告id属性
                if (soulUserResult1 != null && soulUserResult != null) {
                    questionnaireVo.setIsLock(0); // 解锁状态
                    // 设置问卷的报告id
                    questionnaireVo.setReportId(soulUserResult.getId().toHexString());
                }
                // 如果用户没有初级问卷答题记录,则设置中级问卷为解锁状态
                if (soulUserResult1 == null) {
                    questionnaireVo.setIsLock(1); // 锁定状态
                }
            }
            // 如果是高级问卷
            if (level.equals("高级")) {
                questionnaireVo = setIsLockAndReportIdOfQuestionnaireVo(questionnaireVo,)
                // 根据用户id和level查询用户答题结果表
                SoulUserResult soulUserResult = soulUserResultApi.findByUserIdAndLevel(userId, level); // 高级问卷答题记录
                SoulUserResult soulUserResult2 = soulUserResultApi.findByUserIdAndLevel(userId, "中级");
                // 如果用户已有中级问卷答题记录,且高级问卷答题记录为null,则设置中级问卷为解锁状态
                if (soulUserResult2 != null && soulUserResult == null) {
                    questionnaireVo.setIsLock(0); // 解锁状态
                }
                // 如果用户已有中级问卷答题记录,且也有高级问卷答题记录,则设置中级问卷为解锁状态,并设置高级报告id属性
                if (soulUserResult2 != null && soulUserResult != null) {
                    questionnaireVo.setIsLock(0); // 解锁状态
                    // 设置问卷的报告id
                    questionnaireVo.setReportId(soulUserResult.getId().toHexString());
                }
                // 如果用户没有中级问卷答题记录,则设置高级问卷为解锁状态
                if (soulUserResult2 == null) {
                    questionnaireVo.setIsLock(1); // 锁定状态
                }
            }
            // 2.5将QuestionnaireVo添加到questionnaireVoList
            questionnaireVoList.add(questionnaireVo);
             */
        }
        // 3.返回结果
        return questionnaireVoList;
    }

    /**
     * 设置问卷的锁定状态以及报告id
     * @param questionnaireVo
     * @return
     */
    public QuestionnaireVo setIsLockAndReportIdOfQuestionnaireVo(QuestionnaireVo questionnaireVo){
        // 根据用户id和level查询用户答题结果表
        SoulUserResult soulUserResult = soulUserResultApi.findByUserIdAndLevel(UserHolder.getUserId(), questionnaireVo.getLevel());// 中(高)级问卷答题记录

        // 设置初级问卷
        if (questionnaireVo.getLevel().equals("初级")) {
            // 设置初级问卷的锁定状态
            questionnaireVo.setIsLock(0); // 解锁状态
            // 设置初级问卷的报告id
            if (soulUserResult != null) {
                questionnaireVo.setReportId(soulUserResult.getId().toHexString());
            }
            return questionnaireVo;
        }

        // 构造设置初级/高级问卷的条件
        SoulUserResult soulUserResult1 = new SoulUserResult();
        if (questionnaireVo.getLevel().equals("中级")){
            soulUserResult1 = soulUserResultApi.findByUserIdAndLevel(UserHolder.getUserId(), "初级");
        }
        if (questionnaireVo.getLevel().equals("高级")){
            soulUserResult1 = soulUserResultApi.findByUserIdAndLevel(UserHolder.getUserId(), "中级");
        }

        // 如果用户已有初(中)级问卷答题记录,且中(高)级问卷答题记录为null,则设置中(高)级问卷为解锁状态
        if (soulUserResult1 != null && soulUserResult == null) {
            questionnaireVo.setIsLock(0); // 解锁状态
        }
        // 如果用户已有初(中)级问卷答题记录,且也有中(高)级问卷答题记录,则设置中(高)级问卷为解锁状态,并设置中(高)级报告id属性
        if (soulUserResult1 != null && soulUserResult != null) {
            questionnaireVo.setIsLock(0); // 解锁状态
            // 设置问卷的报告id
            questionnaireVo.setReportId(soulUserResult.getId().toHexString());
        }
        // 如果用户没有初(中)级问卷答题记录,则设置中(高)级问卷为解锁状态
        if (soulUserResult1 == null) {
            questionnaireVo.setIsLock(1); // 锁定状态
        }
        return questionnaireVo;
    }


    /**
     * 设置问卷的默认数据,针对系统无问卷数据时设置锁定状态
     * @return
     */
    public List<QuestionnaireVo> getDefaultQuestionnaireVoList(){
        List<QuestionnaireVo> questionnaireVoList = new ArrayList<>();
        // 初级问卷
        QuestionnaireVo questionnaireVo1 = new QuestionnaireVo(
                "1",
                "初级问卷", // todo
                "xxx", // todo,下面同理
                "初级",
                2,
                1,
                "",
                null
        );
        // 中级问卷
        QuestionnaireVo questionnaireVo2 = new QuestionnaireVo(
                "2",
                "中级问卷",
                "xxx",
                "中级",
                3,
                1,
                "",
                null
        );
        // 高级问卷
        QuestionnaireVo questionnaireVo3 = new QuestionnaireVo(
                "3",
                "高级问卷",
                "xxx",
                "高级",
                5,
                1,
                "",
                null
        );
        // 添加到集合中
        questionnaireVoList.add(questionnaireVo1);
        questionnaireVoList.add(questionnaireVo2);
        questionnaireVoList.add(questionnaireVo3);
        return questionnaireVoList;
    }


    /**
     * 查看结果
     * @return
     */
    public ReportVo soulReport(String resultId) {
        //查询
        SoulUserResult soulUserResult = soulApi.findResult(resultId);

        //封装
        ReportVo reportVo = new ReportVo();

        //查询相同类型的用户
        List<SoulUserResult> soulUserResults =  soulApi.findUserByconclusionType(soulUserResult.getConclusionType(),soulUserResult.getLevel());
        if (CollectionUtils.isEmpty(soulUserResults) || soulUserResults.size() == 1) {
            soulUserResults = defaultSoulUserResult();
        }
        List<SimilarYouVo> similarYou = getSimliarYou(soulUserResults);
        if (CollectionUtils.isEmpty(similarYou)) {
            soulUserResults = defaultSoulUserResult();
            similarYou = getSimliarYou(soulUserResults);
        }
        reportVo.setSimilarYou(similarYou);

        //维度
        List<Dimension> dimensions = new ArrayList<>();
        for (int i = 0; i < 4; i++) {
            Dimension dimension = new Dimension();
            String key = (i == 0) ? "外向" : (i == 1) ? "判断" : (i ==2) ? "抽象" : "理性";
            String value = (i == 0) ? soulUserResult.getOutGoing() : (i == 1) ? soulUserResult.getJudge() : (i ==2) ? soulUserResult.getAbstraction() : soulUserResult.getReason();
            dimension.setKey(key);
            dimension.setValue(value);
            dimensions.add(dimension);
        }
        reportVo.setDimensions(dimensions);

        //查询鉴定结果
        SoulConclusion soulConclusion =  soulApi.findConclusion(soulUserResult.getConclusionType());
        reportVo.setConclusion(soulConclusion.getConclusion());

        //封面
        reportVo.setCover(soulConclusion.getCoverUrl());
        return reportVo;
    }

    /**
     * 查询同类型用户信息
     * @param soulUserResults
     * @return
     */
    private List<SimilarYouVo> getSimliarYou(List<SoulUserResult> soulUserResults) {
        List<SimilarYouVo> simliarYou = new ArrayList<>();
        UserInfo currentUserinfo = userInfoApi.findUserInfoById(UserHolder.getUserId());
        for (SoulUserResult userResult : soulUserResults) {
            UserInfo userInfo = userInfoApi.findUserInfoById(userResult.getUserId());
            //排除同性
            if (userInfo.getGender().equals(currentUserinfo.getGender())){
                continue;
            }
            // 创建并设置vo的属性值
            SimilarYouVo similarYouVo = new SimilarYouVo();
            similarYouVo.setId(Integer.parseInt(userInfo.getId().toString()));
            similarYouVo.setAvatar(userInfo.getAvatar());
            simliarYou.add(similarYouVo);
            //限制5条
            if (simliarYou.size() == 5) {
                break;
            }
        }
        return simliarYou;
    }


    //构造默认数据
    private List<SoulUserResult> defaultSoulUserResult() {
        String ids = "1,2,3,4,5,6,7,8,9,10,11,12,13,14,15,16,20";
        List<SoulUserResult> records = new ArrayList<>();
        for (String id : ids.split(",")) {
            SoulUserResult soulUserResult = new SoulUserResult();
            soulUserResult.setUserId(Long.valueOf(id));
            records.add(soulUserResult);
        }
        return records;
    }


    /**
     * 测灵魂-提交问卷
     */
    public String submitQuestionnaire(List<AnswerVo> answers) {
        //遍历
        int outGoingInt = 0;  //外向得分
        int judgeInt = 0;  //判断得分
        int abstractionInt = 0; //抽象得分
        int reasonInt = 0; //理性得分
        for (AnswerVo answer : answers) {
            //获得问题id及选项id
            //String questionId = answer.getQuestionId();  //试题编号
            String optionId = answer.getOptionId();  //选项编号

            //查询问题选项
            Option option = optionApi.findOption(optionId);

            //计算得分
            switch (option.getDimensionValue()) {
                case "外向" : outGoingInt += 1;
                    break;
                case "判断" : judgeInt += 1;
                    break;
                case "抽象" : abstractionInt += 1;
                    break;
                case "理性" : reasonInt += 1;
                    break;
                default:break;
            }
        }
        //根据得分计算维度值和用户类型
        String outGoing = outGoingInt * 10 + "%";
        String judge = judgeInt * 10 + "%";
        String abstraction = abstractionInt * 10 + "%";
        String reason = reasonInt * 10 + "%";

        int source = (outGoingInt + judgeInt * 2 + abstractionInt * 3 + reasonInt * 4);
        String conclusionType = (source > 0 && source <= 10 ) ? "猫头鹰" : (source > 11 && source <= 20 ) ? "小白兔" : (source > 21 && source <= 30 ) ? "小狐狸" : "小狮子";

        //查询问卷等级
        Questionnaire questionnaire = questionnaireApi.findLevel(answers.get(1).getQuestionId());

        //封装数据保存
        SoulUserResult soulUserResult = new SoulUserResult();
        soulUserResult.setUserId(UserHolder.getUserId());  //用户id
        soulUserResult.setOutGoing(outGoing);  //外向维度
        soulUserResult.setJudge(judge); //判断维度
        soulUserResult.setAbstraction(abstraction);  //抽象维度
        soulUserResult.setReason(reason); //理性维度
        soulUserResult.setConclusionType(conclusionType);  //鉴定类型
        soulUserResult.setLevel(questionnaire.getLevel()); //问卷等级

        SoulUserResult result = soulUserResultApi.findByUserIdAndLevel(UserHolder.getUserId(), questionnaire.getLevel());
        if(StringUtils.isEmpty(result)){
            return soulUserResultApi.saveSoulUserResult(soulUserResult);

        }else {
            soulUserResult.setId(result.getId());
            return soulUserResultApi.updateSoulUserResult(soulUserResult);
        }

    }

}
