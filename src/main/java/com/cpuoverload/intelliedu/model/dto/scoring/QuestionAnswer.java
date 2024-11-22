package com.cpuoverload.intelliedu.model.dto.scoring;

import lombok.Data;

import java.io.Serializable;

@Data
public class QuestionAnswer implements Serializable {
    private static final long serialVersionUID = 4516372154969855974L;

    /**
     * 题目名称
     */
    private String title;

    /**
     * 用户答案（题目选项的文本内容）
     */
    private String userAnswerText;
}
