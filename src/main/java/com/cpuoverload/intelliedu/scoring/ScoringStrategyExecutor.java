package com.cpuoverload.intelliedu.scoring;

import com.cpuoverload.intelliedu.exception.BusinessException;
import com.cpuoverload.intelliedu.exception.Err;
import com.cpuoverload.intelliedu.model.entity.AnswerRecord;
import com.cpuoverload.intelliedu.model.entity.Application;
import com.cpuoverload.intelliedu.scoring.annotation.ScoringStrategyConfig;
import org.springframework.stereotype.Service;

import javax.annotation.Resource;
import java.util.List;

/**
 * 评分策略执行器
 */
@Service
public class ScoringStrategyExecutor {

    // 策略列表
    @Resource
    private List<ScoringStrategy> scoringStrategyList;


    /**
     * 评分
     *
     * @param answerList
     * @param application
     * @return
     * @throws Exception
     */
    public AnswerRecord doScore(List<String> answerList, Application application) throws Exception {
        Integer appType = application.getType();
        Integer appScoringStrategy = application.getStrategy();
        if (appType == null || appScoringStrategy == null) {
            throw new BusinessException(Err.SYSTEM_ERROR, "Scoring Strategy not found");
        }
        // 根据注解获取策略
        for (ScoringStrategy strategy : scoringStrategyList) {
            if (strategy.getClass().isAnnotationPresent(ScoringStrategyConfig.class)) {
                ScoringStrategyConfig scoringStrategyConfig = strategy.getClass().getAnnotation(ScoringStrategyConfig.class);
                if (scoringStrategyConfig.appType() == appType && scoringStrategyConfig.scoringStrategy() == appScoringStrategy) {
                    return strategy.doScore(answerList, application);
                }
            }
        }
        throw new BusinessException(Err.SYSTEM_ERROR, "Scoring strategy not found");
    }
}
