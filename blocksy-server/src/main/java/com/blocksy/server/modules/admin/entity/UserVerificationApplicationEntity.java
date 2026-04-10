package com.blocksy.server.modules.admin.entity;

import com.baomidou.mybatisplus.annotation.IdType;
import com.baomidou.mybatisplus.annotation.TableField;
import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableName;

import java.time.LocalDateTime;

@TableName("user_verification_applications")
public class UserVerificationApplicationEntity {
    @TableId(type = IdType.AUTO)
    private Long id;
    @TableField("user_id")
    private Long userId;
    @TableField("verify_type")
    private String verifyType;
    @TableField("real_name")
    private String realName;
    @TableField("id_card_mask")
    private String idCardMask;
    @TableField("material_urls")
    private String materialUrls;
    @TableField("process_status")
    private String processStatus;
    @TableField("review_note")
    private String reviewNote;
    @TableField("reviewer_user_id")
    private Long reviewerUserId;
    @TableField("reviewed_at")
    private LocalDateTime reviewedAt;
    private Integer status;
    @TableField("created_at")
    private LocalDateTime createdAt;
    @TableField("updated_at")
    private LocalDateTime updatedAt;

    public Long getId() { return id; }
    public void setId(Long id) { this.id = id; }
    public Long getUserId() { return userId; }
    public void setUserId(Long userId) { this.userId = userId; }
    public String getVerifyType() { return verifyType; }
    public void setVerifyType(String verifyType) { this.verifyType = verifyType; }
    public String getRealName() { return realName; }
    public void setRealName(String realName) { this.realName = realName; }
    public String getIdCardMask() { return idCardMask; }
    public void setIdCardMask(String idCardMask) { this.idCardMask = idCardMask; }
    public String getMaterialUrls() { return materialUrls; }
    public void setMaterialUrls(String materialUrls) { this.materialUrls = materialUrls; }
    public String getProcessStatus() { return processStatus; }
    public void setProcessStatus(String processStatus) { this.processStatus = processStatus; }
    public String getReviewNote() { return reviewNote; }
    public void setReviewNote(String reviewNote) { this.reviewNote = reviewNote; }
    public Long getReviewerUserId() { return reviewerUserId; }
    public void setReviewerUserId(Long reviewerUserId) { this.reviewerUserId = reviewerUserId; }
    public LocalDateTime getReviewedAt() { return reviewedAt; }
    public void setReviewedAt(LocalDateTime reviewedAt) { this.reviewedAt = reviewedAt; }
    public Integer getStatus() { return status; }
    public void setStatus(Integer status) { this.status = status; }
    public LocalDateTime getCreatedAt() { return createdAt; }
    public void setCreatedAt(LocalDateTime createdAt) { this.createdAt = createdAt; }
    public LocalDateTime getUpdatedAt() { return updatedAt; }
    public void setUpdatedAt(LocalDateTime updatedAt) { this.updatedAt = updatedAt; }
}
