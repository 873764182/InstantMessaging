package com.panxiong.instant.model;

/**
 * 数据基础对象
 */
public class BaseData {

    /* 数据ID （判断消息是否发送成功时用到） */
    public Long dataId;
    /* 数据类型 */
    public Integer dataType;
    /* 数据内容 */
    public String dataContent;

    @Override
    public String toString() {
        return "BaseData{" +
                "dataId=" + dataId +
                ", dataType=" + dataType +
                ", dataContent='" + dataContent + '\'' +
                '}';
    }

    public BaseData() {
    }

    public BaseData(Integer dataType, String dataContent) {
        this.dataType = dataType;
        this.dataContent = dataContent;
    }

    public BaseData(Long dataId, Integer dataType, String dataContent) {
        this.dataId = dataId;
        this.dataType = dataType;
        this.dataContent = dataContent;
    }

}
