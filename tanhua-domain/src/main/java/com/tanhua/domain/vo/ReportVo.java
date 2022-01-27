package com.tanhua.domain.vo;

import com.tanhua.domain.db.User;
import com.tanhua.domain.db.UserInfo;
import lombok.Data;

import java.io.Serializable;
import java.util.List;

/**
 * 接收问卷结果数据
 *
 */
@Data
public class ReportVo implements Serializable {

    private String conclusion;  //鉴定结果
    private String cover;   //鉴定封面
    private List<Dimension> dimensions; //纬度
    private List<SimilarYouVo> similarYou; //与你相似

}
