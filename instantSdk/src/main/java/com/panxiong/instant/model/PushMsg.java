package com.panxiong.instant.model;

import java.io.Serializable;

/**
 * PushMsg 实体类 Wed Jun 15 21:52:14 CST 2016 PX
 */
public class PushMsg implements Serializable {

    public Long _id;
    public Integer createUser;
    public Long createTime;
    public String pushTitle;
    public String pushContent;

    @Override
    public String toString() {
        return "PushMsg [_id=" + _id + ", CreateUser=" + createUser + ", CreateTime=" + createTime + ", PushTitle="
                + pushTitle + ", pushContent=" + pushContent + "]";
    }
}
