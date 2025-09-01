package com.second.hand.trading.server.model;

import java.io.Serializable;
import java.math.BigDecimal;
import java.util.Date;

/**
 * sh_wanted_item
 */
public class WantedItemModel implements Serializable {
    /**
     * 自增主键
     */
    private Long id;

    /**
     * 求购物品名称
     */
    private String wantedName;

    /**
     * 详情
     */
    private String wantedDetails;

    /**
     * 最高预算
     */
    private BigDecimal maxPrice;

    /**
     * 发布时间
     */
    private Date releaseTime;

    /**
     * 状态（发布1、已找到2、过期3、删除0）
     */
    private Byte wantedStatus;

    /**
     * 用户主键id
     */
    private Long userId;

    private UserModel user;

    private static final long serialVersionUID = 1L;

    public Long getId() {
        return id;
    }

    public void setId(Long id) {
        this.id = id;
    }

    public String getWantedName() {
        return wantedName;
    }

    public void setWantedName(String wantedName) {
        this.wantedName = wantedName;
    }

    public String getWantedDetails() {
        return wantedDetails;
    }

    public void setWantedDetails(String wantedDetails) {
        this.wantedDetails = wantedDetails;
    }

    public BigDecimal getMaxPrice() {
        return maxPrice;
    }

    public void setMaxPrice(BigDecimal maxPrice) {
        this.maxPrice = maxPrice;
    }

    public Date getReleaseTime() {
        return releaseTime;
    }

    public void setReleaseTime(Date releaseTime) {
        this.releaseTime = releaseTime;
    }

    public Byte getWantedStatus() {
        return wantedStatus;
    }

    public void setWantedStatus(Byte wantedStatus) {
        this.wantedStatus = wantedStatus;
    }

    public Long getUserId() {
        return userId;
    }

    public void setUserId(Long userId) {
        this.userId = userId;
    }

    public UserModel getUser() {
        return user;
    }

    public void setUser(UserModel user) {
        this.user = user;
    }

    @Override
    public boolean equals(Object that) {
        if (this == that) {
            return true;
        }
        if (that == null) {
            return false;
        }
        if (getClass() != that.getClass()) {
            return false;
        }
        WantedItemModel other = (WantedItemModel) that;
        return (this.getId() == null ? other.getId() == null : this.getId().equals(other.getId()))
            && (this.getWantedName() == null ? other.getWantedName() == null : this.getWantedName().equals(other.getWantedName()))
            && (this.getWantedDetails() == null ? other.getWantedDetails() == null : this.getWantedDetails().equals(other.getWantedDetails()))
            && (this.getMaxPrice() == null ? other.getMaxPrice() == null : this.getMaxPrice().equals(other.getMaxPrice()))
            && (this.getReleaseTime() == null ? other.getReleaseTime() == null : this.getReleaseTime().equals(other.getReleaseTime()))
            && (this.getWantedStatus() == null ? other.getWantedStatus() == null : this.getWantedStatus().equals(other.getWantedStatus()))
            && (this.getUserId() == null ? other.getUserId() == null : this.getUserId().equals(other.getUserId()));
    }

    @Override
    public int hashCode() {
        final int prime = 31;
        int result = 1;
        result = prime * result + ((getId() == null) ? 0 : getId().hashCode());
        result = prime * result + ((getWantedName() == null) ? 0 : getWantedName().hashCode());
        result = prime * result + ((getWantedDetails() == null) ? 0 : getWantedDetails().hashCode());
        result = prime * result + ((getMaxPrice() == null) ? 0 : getMaxPrice().hashCode());
        result = prime * result + ((getReleaseTime() == null) ? 0 : getReleaseTime().hashCode());
        result = prime * result + ((getWantedStatus() == null) ? 0 : getWantedStatus().hashCode());
        result = prime * result + ((getUserId() == null) ? 0 : getUserId().hashCode());
        return result;
    }

    @Override
    public String toString() {
        StringBuilder sb = new StringBuilder();
        sb.append(getClass().getSimpleName());
        sb.append(" [");
        sb.append("Hash = ").append(hashCode());
        sb.append(", id=").append(id);
        sb.append(", wantedName=").append(wantedName);
        sb.append(", wantedDetails=").append(wantedDetails);
        sb.append(", maxPrice=").append(maxPrice);
        sb.append(", releaseTime=").append(releaseTime);
        sb.append(", wantedStatus=").append(wantedStatus);
        sb.append(", userId=").append(userId);
        sb.append(", serialVersionUID=").append(serialVersionUID);
        sb.append("]");
        return sb.toString();
    }
}

