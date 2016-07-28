package com.panxiong.instant.model;

import java.io.Serializable;

/**
 * Users 实体类 Sun Jun 12 10:54:21 CST 2016 PX
 */

public class Users implements Serializable {

    /* 用户编号 */
    public Integer _id;
    /* 用户名 */
    public String userName;
    /* 用户密码 */
    public String passWord;
    /* 是否在线 (0.否, 1.是) */
    public Integer inLinear;

    @Override
    public String toString() {
        return "Users [_id=" + _id + ", userName=" + userName + ", passWord=" + passWord + ", otherNote=" + inLinear
                + "]";
    }

    public Integer msgSize = 0;

}
