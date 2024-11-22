package com.cpuoverload.intelliedu.utils;

import com.cpuoverload.intelliedu.model.entity.Application;
import com.cpuoverload.intelliedu.model.enums.AppType;

import static com.cpuoverload.intelliedu.constant.AIConstant.GENERATE_EVALUATION_QUESTION_SYSTEM_MESSAGE;
import static com.cpuoverload.intelliedu.constant.AIConstant.GENERATE_GRADE_QUESTION_SYSTEM_MESSAGE;

public class AIMessageUtil {

    /**
     * 获取用户提示词
     * @param application
     * @param questionNumber
     * @param optionNumber
     * @return
     */
    public static String getUserMessage(Application application, int questionNumber, int optionNumber) {
        return String.format(
                "Application name: %s%n" +
                        "Application description: %s%n" +
                        "Application category: %s type%n" +
                        "Number of questions to generate: %d%n" +
                        "Number of options per question: %d",
                application.getAppName(),
                application.getDescription(),
                AppType.fromCode(application.getType()).getDescription(),
                questionNumber,
                optionNumber
        );
    }

    /**
     * 获取系统提示词
     * @param application
     * @return
     */
    public static String getSystemMessage(Application application) {
        String systemMessage = null;

        if (application.getType() == AppType.EVALUATION.getCode()) {
            systemMessage = GENERATE_EVALUATION_QUESTION_SYSTEM_MESSAGE;
        } else if (application.getType() == AppType.GRADE.getCode()) {
            systemMessage = GENERATE_GRADE_QUESTION_SYSTEM_MESSAGE;
        }

        return systemMessage;
    }
}
