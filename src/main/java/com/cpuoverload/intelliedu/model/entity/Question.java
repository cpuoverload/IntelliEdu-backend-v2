package com.cpuoverload.intelliedu.model.entity;

import com.baomidou.mybatisplus.annotation.*;
import com.baomidou.mybatisplus.extension.handlers.JacksonTypeHandler;
import com.cpuoverload.intelliedu.model.dto.question.QuestionContent;
import lombok.Data;

import java.io.Serializable;
import java.util.Date;
import java.util.List;

/**
 * Question
 * @TableName question
 */
@TableName(value ="question", autoResultMap = true)
@Data
public class Question implements Serializable {
    /**
     * ID
     */
    @TableId(type = IdType.AUTO)
    private Long id;

    /**
     * Question List (JSON)
     * <a href="https://baomidou.com/guides/type-handler/">JSON 字符串与 Java 对象相互转换</a>
     * 需要升级 mybatis-plus 到 3.5.6 版本（fix: 修复Json类型处理器反序列化泛型丢失原始类型），否则解析出的是 List<LinkedHashMap> 类型，导致类型转换报错
     */
    @TableField(typeHandler = JacksonTypeHandler.class)
    private List<QuestionContent> questions;

    /**
     * Application ID
     */
    private Long appId;

    /**
     * Creator User ID
     */
    private Long userId;

    /**
     * Creation Time
     */
    private Date createTime;

    /**
     * Update Time
     */
    private Date updateTime;

    /**
     * Is Deleted
     */
    @TableLogic
    private Integer deleted;

    @TableField(exist = false)
    private static final long serialVersionUID = 1L;
}