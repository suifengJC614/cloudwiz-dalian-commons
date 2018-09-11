package cn.cloudwiz.dalian.commons.core.orm;

import com.fasterxml.jackson.annotation.JsonIgnore;

import java.sql.Timestamp;

public class TimeDeleteEntity extends BaseEntity {

    private static final long serialVersionUID = 5710574607119638137L;
    private Timestamp createTime;
    private Timestamp updateTime;
    @JsonIgnore
    private boolean delete;

    public Timestamp getCreateTime() {
        return createTime;
    }

    public void setCreateTime(Timestamp createTime) {
        this.createTime = createTime;
    }

    public Timestamp getUpdateTime() {
        return updateTime;
    }

    public void setUpdateTime(Timestamp updateTime) {
        this.updateTime = updateTime;
    }

    public boolean isDelete() {
        return delete;
    }

    public void setDelete(boolean delete) {
        this.delete = delete;
    }

}
