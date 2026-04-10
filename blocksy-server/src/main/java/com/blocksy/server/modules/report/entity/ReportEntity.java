package com.blocksy.server.modules.report.entity;

import com.baomidou.mybatisplus.annotation.IdType;
import com.baomidou.mybatisplus.annotation.TableField;
import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableName;

import java.time.LocalDateTime;

@TableName("reports")
public class ReportEntity {

    @TableId(type = IdType.AUTO)
    private Long id;
    @TableField("reporter_user_id")
    private Long reporterUserId;
    @TableField("target_type")
    private String targetType;
    @TableField("target_id")
    private Long targetId;
    private String reason;
    private String description;
    @TableField("process_status")
    private String processStatus;
    @TableField("handler_user_id")
    private Long handlerUserId;
    @TableField("handler_note")
    private String handlerNote;
    @TableField("handled_at")
    private LocalDateTime handledAt;
    private Integer status;
    @TableField("created_at")
    private LocalDateTime createdAt;
    @TableField("updated_at")
    private LocalDateTime updatedAt;

    public Long getId() { return id; }
    public void setId(Long id) { this.id = id; }
    public Long getReporterUserId() { return reporterUserId; }
    public void setReporterUserId(Long reporterUserId) { this.reporterUserId = reporterUserId; }
    public String getTargetType() { return targetType; }
    public void setTargetType(String targetType) { this.targetType = targetType; }
    public Long getTargetId() { return targetId; }
    public void setTargetId(Long targetId) { this.targetId = targetId; }
    public String getReason() { return reason; }
    public void setReason(String reason) { this.reason = reason; }
    public String getDescription() { return description; }
    public void setDescription(String description) { this.description = description; }
    public String getProcessStatus() { return processStatus; }
    public void setProcessStatus(String processStatus) { this.processStatus = processStatus; }
    public Long getHandlerUserId() { return handlerUserId; }
    public void setHandlerUserId(Long handlerUserId) { this.handlerUserId = handlerUserId; }
    public String getHandlerNote() { return handlerNote; }
    public void setHandlerNote(String handlerNote) { this.handlerNote = handlerNote; }
    public LocalDateTime getHandledAt() { return handledAt; }
    public void setHandledAt(LocalDateTime handledAt) { this.handledAt = handledAt; }
    public Integer getStatus() { return status; }
    public void setStatus(Integer status) { this.status = status; }
    public LocalDateTime getCreatedAt() { return createdAt; }
    public void setCreatedAt(LocalDateTime createdAt) { this.createdAt = createdAt; }
    public LocalDateTime getUpdatedAt() { return updatedAt; }
    public void setUpdatedAt(LocalDateTime updatedAt) { this.updatedAt = updatedAt; }
}
