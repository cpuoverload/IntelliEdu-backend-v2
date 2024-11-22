package com.cpuoverload.intelliedu.scoring.strategy;

import cn.hutool.json.JSONUtil;
import com.cpuoverload.intelliedu.manager.AiManager;
import com.cpuoverload.intelliedu.model.dto.question.QuestionContent;
import com.cpuoverload.intelliedu.model.dto.scoring.QuestionAnswer;
import com.cpuoverload.intelliedu.model.entity.AnswerRecord;
import com.cpuoverload.intelliedu.model.entity.Application;
import com.cpuoverload.intelliedu.model.entity.Question;
import com.cpuoverload.intelliedu.model.vo.QuestionVo;
import com.cpuoverload.intelliedu.scoring.ScoringStrategy;
import com.cpuoverload.intelliedu.scoring.annotation.ScoringStrategyConfig;
import com.cpuoverload.intelliedu.service.QuestionService;

import javax.annotation.Resource;
import java.util.ArrayList;
import java.util.List;

import static com.cpuoverload.intelliedu.constant.AIConstant.AI_EVALUATION_SCORING_SYSTEM_MESSAGE;


/**
 * AI 测评类应用评分策略
 */
@ScoringStrategyConfig(appType = 1, scoringStrategy = 1)
public class AiEvaluationScoringStrategy implements ScoringStrategy {

    @Resource
    private QuestionService questionService;

    @Resource
    private AiManager aiManager;

    private String getAiEvaluationScoringUserMessage(Application application, List<QuestionContent> questionContentList, List<String> answerList) {
        StringBuilder userMessage = new StringBuilder();
        userMessage.append("Application name: ").append(application.getAppName()).append("\n");
        userMessage.append("Application description: ").append(application.getDescription()).append("\n");

        List<QuestionAnswer> questionAnswerList = new ArrayList<>();
        for (int i = 0; i < questionContentList.size(); i++) {
            QuestionContent questionContent = questionContentList.get(i);
            String answer = answerList.get(i);
            // 根据用户选项（如 A）查找对应的选项内容
            QuestionContent.Option option = questionContent.getOptions().stream()
                    .filter(opt -> opt.getKey().equals(answer))
                    .findFirst()
                    .get();
            QuestionAnswer questionAnswer = new QuestionAnswer();
            questionAnswer.setTitle(questionContent.getTitle());
            questionAnswer.setUserAnswerText(option.getValue());
            questionAnswerList.add(questionAnswer);
        }

        userMessage.append("List of questions and user answers: ").append(JSONUtil.toJsonStr(questionAnswerList));
        return userMessage.toString();
    }

    @Override
    public AnswerRecord doScore(List<String> answerList, Application application) throws Exception {
        Long appId = application.getId();
        // 1. 根据 appId 查询到对应题目
        Question question = questionService.getQuestionByAppId(appId);
        QuestionVo questionVo = QuestionVo.objToVo(question);
        List<QuestionContent> questionContent = questionVo.getQuestions();
        // 2. 调用 AI 获取结果
        // 封装 Prompt
        String userMessage = getAiEvaluationScoringUserMessage(application, questionContent, answerList);
        // AI 生成
        String result = aiManager.doRequest(AI_EVALUATION_SCORING_SYSTEM_MESSAGE, userMessage);
        // 结果处理
        int start = result.indexOf("{");
        int end = result.lastIndexOf("}");
        String json = result.substring(start, end + 1);
        // 3. 构造返回值，填充答案对象的属性
        AnswerRecord answerRecord = JSONUtil.toBean(json, AnswerRecord.class);
        answerRecord.setAppId(appId);
        answerRecord.setAppType(application.getType());
        answerRecord.setStrategy(application.getStrategy());
        answerRecord.setAnswers(answerList);
        return answerRecord;
    }
}
