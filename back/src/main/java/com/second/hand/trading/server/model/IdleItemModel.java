package com.second.hand.trading.server.model;

import lombok.Getter;
import lombok.Setter;

import java.io.Serializable;
import java.math.BigDecimal;
import java.util.Date;

@Getter
@Setter
public class IdleItemModel implements Serializable {
    /**
     * 自增主键
     */
    private Long id;

    /**
     * 闲置物名称
     */
    private String idleName;

    /**
     * 详情
     */
    private String idleDetails;

    /**
     * 图集
     */
    private String pictureList;

    /**
     * 价格
     */
    private BigDecimal idlePrice;

    /**
     * 发货地区
     */
    private String idlePlace;

    /**
     * 分类标签
     */
    private Integer idleLabel;

    /**
     * 发布时间
     */
    private Date releaseTime;

    /**
     * 状态（发布1、下架2、删除0）
     */
    private Byte idleStatus;

    /**
     * 用户主键id
     */
    private Long userId;

    private UserModel user;

    private static final long serialVersionUID = 1L;

    private String idleTag;

    private Long skimCount;

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
        IdleItemModel other = (IdleItemModel) that;
        return (this.getId() == null ? other.getId() == null : this.getId().equals(other.getId()))
                && (this.getIdleName() == null ? other.getIdleName() == null : this.getIdleName().equals(other.getIdleName()))
                && (this.getIdleDetails() == null ? other.getIdleDetails() == null : this.getIdleDetails().equals(other.getIdleDetails()))
                && (this.getPictureList() == null ? other.getPictureList() == null : this.getPictureList().equals(other.getPictureList()))
                && (this.getIdlePrice() == null ? other.getIdlePrice() == null : this.getIdlePrice().equals(other.getIdlePrice()))
                && (this.getIdlePlace() == null ? other.getIdlePlace() == null : this.getIdlePlace().equals(other.getIdlePlace()))
                && (this.getIdleLabel() == null ? other.getIdleLabel() == null : this.getIdleLabel().equals(other.getIdleLabel()))
                && (this.getReleaseTime() == null ? other.getReleaseTime() == null : this.getReleaseTime().equals(other.getReleaseTime()))
                && (this.getIdleStatus() == null ? other.getIdleStatus() == null : this.getIdleStatus().equals(other.getIdleStatus()))
                && (this.getUserId() == null ? other.getUserId() == null : this.getUserId().equals(other.getUserId()));
    }

    @Override
    public int hashCode() {
        final int prime = 31;
        int result = 1;
        result = prime * result + ((getId() == null) ? 0 : getId().hashCode());
        result = prime * result + ((getIdleName() == null) ? 0 : getIdleName().hashCode());
        result = prime * result + ((getIdleDetails() == null) ? 0 : getIdleDetails().hashCode());
        result = prime * result + ((getPictureList() == null) ? 0 : getPictureList().hashCode());
        result = prime * result + ((getIdlePrice() == null) ? 0 : getIdlePrice().hashCode());
        result = prime * result + ((getIdlePlace() == null) ? 0 : getIdlePlace().hashCode());
        result = prime * result + ((getIdleLabel() == null) ? 0 : getIdleLabel().hashCode());
        result = prime * result + ((getReleaseTime() == null) ? 0 : getReleaseTime().hashCode());
        result = prime * result + ((getIdleStatus() == null) ? 0 : getIdleStatus().hashCode());
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
        sb.append(", idleName=").append(idleName);
        sb.append(", idleDetails=").append(idleDetails);
        sb.append(", pictureList=").append(pictureList);
        sb.append(", idlePrice=").append(idlePrice);
        sb.append(", idlePlace=").append(idlePlace);
        sb.append(", idleLabel=").append(idleLabel);
        sb.append(", releaseTime=").append(releaseTime);
        sb.append(", idleStatus=").append(idleStatus);
        sb.append(", userId=").append(userId);
        sb.append(", serialVersionUID=").append(serialVersionUID);
        sb.append("]");
        return sb.toString();
    }
}