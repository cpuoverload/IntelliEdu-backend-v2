package com.cpuoverload.intelliedu.scoring.strategy;

import cn.hutool.core.util.StrUtil;
import cn.hutool.crypto.digest.DigestUtil;
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
import com.github.benmanes.caffeine.cache.Cache;
import com.github.benmanes.caffeine.cache.Caffeine;
import org.redisson.api.RLock;
import org.redisson.api.RedissonClient;

import javax.annotation.Resource;
import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.TimeUnit;

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

    @Resource
    private RedissonClient redissonClient;

    // Redisson 锁 key，用于解决缓存击穿
    private static final String AI_RESULT_LOCK = "AI_RESULT_LOCK";

    // 用 Caffine 缓存 AI 生成的测评结果
    private final Cache<String, String> resultCacheMap = Caffeine.newBuilder()
            .initialCapacity(1024)
            .expireAfterAccess(1, TimeUnit.DAYS)
            .build();

    // 构建缓存 key (appId + 用户答案列表的 MD5)
    private String buildKey(long appId, List<String> answerList) {
        return appId + DigestUtil.md5Hex(JSONUtil.toJsonStr(answerList));
    }

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

        // 尝试从缓存中取结果
        String cacheKey = buildKey(appId, answerList);
        String cacheResult = resultCacheMap.getIfPresent(cacheKey);

        if (StrUtil.isNotBlank(cacheResult)) {
            AnswerRecord answerRecord = JSONUtil.toBean(cacheResult, AnswerRecord.class);
            answerRecord.setAppId(appId);
            answerRecord.setAppType(application.getType());
            answerRecord.setStrategy(application.getStrategy());
            answerRecord.setAnswers(answerList);
            return answerRecord;
        }

        RLock lock = redissonClient.getLock(AI_RESULT_LOCK + cacheKey); // 注意锁粒度

        try {
            boolean haveLock = lock.tryLock(3, 15, TimeUnit.SECONDS);

            // 如果没有抢到锁，认为失败，不再调用 ai 接口（也可以有别的处理，暂时先这么写）
            if (!haveLock) {
                return null;
            }

            // 查询对应题目
            Question question = questionService.getQuestionByAppId(appId);
            QuestionVo questionVo = QuestionVo.objToVo(question);
            List<QuestionContent> questionContent = questionVo.getQuestions();

            // 调用 AI 获取结果
            // 封装 Prompt
            String userMessage = getAiEvaluationScoringUserMessage(application, questionContent, answerList);
            // AI 生成结果
            String result = aiManager.doRequest(AI_EVALUATION_SCORING_SYSTEM_MESSAGE, userMessage);
            // 结果处理
            int start = result.indexOf("{");
            int end = result.lastIndexOf("}");
            String jsonStr = result.substring(start, end + 1);

            // 缓存结果
            resultCacheMap.put(cacheKey, jsonStr);

            // 构造返回值
            AnswerRecord answerRecord = JSONUtil.toBean(jsonStr, AnswerRecord.class);
            answerRecord.setAppId(appId);
            answerRecord.setAppType(application.getType());
            answerRecord.setStrategy(application.getStrategy());
            answerRecord.setAnswers(answerList);
            return answerRecord;
        } finally {
            if (lock != null && lock.isLocked()) {
                // 只允许本线程释放锁，防止被别的线程释放自己的锁（别的线程可能超时，导致释放锁晚了，此时别的线程已经加锁了），可能导致锁的失效
                if (lock.isHeldByCurrentThread()) {
                    lock.unlock();
                }
            }
        }
    }
}
