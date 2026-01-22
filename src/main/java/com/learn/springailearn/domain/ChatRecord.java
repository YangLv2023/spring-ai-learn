package com.learn.springailearn.domain;

import com.baomidou.mybatisplus.annotation.*;
import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Data;

import java.time.LocalDateTime;

/**
 * 通义千问对话记录实体类
 * @author ken
 * @date 2026-01-21
 */
@Data
@TableName("chat_record")
@Schema(name = "ChatRecord", description = "通义千问对话记录")
public class ChatRecord {
    /**
     * 主键ID
     */
    @TableId(type = IdType.AUTO)
    @Schema(description = "主键ID")
    private Long id;

    /**
     * 用户ID
     */
    @TableField("user_id")
    @Schema(description = "用户ID")
    private String userId;

    /**
     * 用户提问内容
     */
    @TableField("user_prompt")
    @Schema(description = "用户提问内容")
    private String userPrompt;

    /**
     * AI回答内容
     */
    @TableField("ai_response")
    @Schema(description = "AI回答内容")
    private String aiResponse;

    /**
     * 使用的模型（qwen-turbo/qwen-plus等）
     */
    @TableField("model")
    @Schema(description = "使用的大模型版本")
    private String model;

    /**
     * 创建时间
     */
    @TableField(value = "create_time", fill = FieldFill.INSERT)
    @Schema(description = "创建时间")
    private LocalDateTime createTime;

    /**
     * 更新时间
     */
    @TableField(value = "update_time", fill = FieldFill.INSERT_UPDATE)
    @Schema(description = "更新时间")
    private LocalDateTime updateTime;

    /**
     * 逻辑删除（0-未删除，1-已删除）
     */
    @TableLogic
    @TableField("is_deleted")
    @Schema(description = "逻辑删除标识：0-未删除，1-已删除")
    private Integer isDeleted;
}
