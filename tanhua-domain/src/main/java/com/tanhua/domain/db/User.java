package com.tanhua.domain.db;

import lombok.Data;
import java.io.Serializable;
import java.util.Date;

/**
 * com.tanhua.domain.db：mysql数据库相关的实体对象
 * com.tanhua.domain.mongo: mongo数据库相关的实体对象
 *
 */
@Data
public class User extends BasePojo {
    private Long id;
    private String mobile; //手机号
    private String password; //密码，json序列化时忽略
    //private Date created;
    //private Date updated;
}