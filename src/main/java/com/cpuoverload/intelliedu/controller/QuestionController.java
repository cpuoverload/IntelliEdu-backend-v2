package com.cpuoverload.intelliedu.controller;

import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.cpuoverload.intelliedu.common.dto.IdRequest;
import com.cpuoverload.intelliedu.common.response.ApiResponse;
import com.cpuoverload.intelliedu.exception.BusinessException;
import com.cpuoverload.intelliedu.exception.Err;
import com.cpuoverload.intelliedu.manager.AiManager;
import com.cpuoverload.intelliedu.manager.RedisLimiterManager;
import com.cpuoverload.intelliedu.model.dto.question.*;
import com.cpuoverload.intelliedu.model.entity.Application;
import com.cpuoverload.intelliedu.model.entity.Question;
import com.cpuoverload.intelliedu.model.vo.QuestionVo;
import com.cpuoverload.intelliedu.service.ApplicationService;
import com.cpuoverload.intelliedu.service.QuestionService;
import com.cpuoverload.intelliedu.service.UserService;
import com.cpuoverload.intelliedu.utils.AIMessageUtil;
import dev.ai4j.openai4j.chat.ChatCompletionResponse;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.BeanUtils;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.servlet.mvc.method.annotation.SseEmitter;

import javax.annotation.Resource;
import javax.servlet.http.HttpServletRequest;
import java.io.IOException;
import java.util.concurrent.atomic.AtomicInteger;
import java.util.function.Consumer;


@RestController
@RequestMapping("/application/question")
@Slf4j
public class QuestionController {

    @Resource
    private QuestionService questionService;

    @Resource
    private ApplicationService applicationService;

    @Resource
    private UserService userService;

    @Resource
    AiManager aiManager;

    @Resource
    private RedisLimiterManager redisLimiterManager;

    // 获取一个应用的题目列表（不要分页）
    @PostMapping("/get/public")
    public ApiResponse<QuestionVo> getPublicQuestionOfOneApp(@RequestBody GetPublicQuestionRequest getPublicQuestionRequest) {
        if (getPublicQuestionRequest == null) {
            throw new BusinessException(Err.PARAMS_ERROR);
        }
        QuestionVo questionVo = questionService.getPublicQuestion(getPublicQuestionRequest);
        return ApiResponse.success(questionVo);
    }

    // 普通用户创建题目
    @PostMapping("/add/me")
    public ApiResponse<Boolean> addMyQuestion(@RequestBody AddMyQuestionRequest addMyQuestionRequest, HttpServletRequest request) {
        if (addMyQuestionRequest == null) {
            throw new BusinessException(Err.PARAMS_ERROR);
        }
        if (addMyQuestionRequest.getQuestions() == null || addMyQuestionRequest.getAppId() == null) {
            throw new BusinessException(Err.PARAMS_ERROR);
        }
        Question question = new Question();
        BeanUtils.copyProperties(addMyQuestionRequest, question);
        Boolean success = questionService.addMyQuestion(question, request);
        if (!success) {
            throw new BusinessException(Err.SYSTEM_ERROR);
        }
        return ApiResponse.success(true);
    }

    // 普通用户查看自己的题目（只允许每次查询一个应用的题目，不要分页）
    @PostMapping("/get/me")
    public ApiResponse<QuestionVo> getMyQuestionOfOneApp(@RequestBody GetMyQuestionRequest getMyQuestionRequest, HttpServletRequest request) {
        if (getMyQuestionRequest == null) {
            throw new BusinessException(Err.PARAMS_ERROR);
        }
        QuestionVo questionVo = questionService.getMyQuestion(getMyQuestionRequest, request);
        return ApiResponse.success(questionVo);
    }

    // 普通用户更新题目
    @PostMapping("/update/me")
    public ApiResponse<Boolean> updateMyQuestion(@RequestBody UpdateMyQuestionRequest updateMyQuestionRequest, HttpServletRequest request) {
        if (updateMyQuestionRequest == null) {
            throw new BusinessException(Err.PARAMS_ERROR);
        }
        Question question = new Question();
        BeanUtils.copyProperties(updateMyQuestionRequest, question);
        Boolean success = questionService.updateMyQuestion(question, request);
        if (!success) {
            throw new BusinessException(Err.SYSTEM_ERROR);
        }
        return ApiResponse.success(true);
    }

    // 普通用户删除题目
    @PostMapping("/delete/me")
    public ApiResponse<Boolean> deleteMyQuestion(@RequestBody IdRequest idRequest, HttpServletRequest request) {
        if (idRequest == null) {
            throw new BusinessException(Err.PARAMS_ERROR);
        }
        Boolean success = questionService.deleteMyQuestion(idRequest, request);
        if (!success) {
            throw new BusinessException(Err.SYSTEM_ERROR);
        }
        return ApiResponse.success(true);
    }

    // 管理员查看题目列表
    @PostMapping("/list")
    public ApiResponse<Page<QuestionVo>> listQuestion(@RequestBody ListQuestionRequest listQuestionRequest) {
        if (listQuestionRequest == null) {
            throw new BusinessException(Err.PARAMS_ERROR);
        }
        Page<QuestionVo> questionVoPage = questionService.listQuestion(listQuestionRequest);
        return ApiResponse.success(questionVoPage);
    }

    // 管理员更新题目
    @PostMapping("/update")
    public ApiResponse<Boolean> updateQuestion(@RequestBody UpdateQuestionRequest updateQuestionRequest) {
        if (updateQuestionRequest == null || updateQuestionRequest.getId() == null) {
            throw new BusinessException(Err.PARAMS_ERROR);
        }
        Question question = new Question();
        BeanUtils.copyProperties(updateQuestionRequest, question);
        Boolean success = questionService.updateQuestion(question);
        if (!success) {
            throw new BusinessException(Err.SYSTEM_ERROR);
        }
        return ApiResponse.success(true);
    }

    // 管理员删除题目
    @PostMapping("/delete")
    public ApiResponse<Boolean> deleteQuestion(@RequestBody IdRequest idRequest) {
        if (idRequest == null) {
            throw new BusinessException(Err.PARAMS_ERROR);
        }
        Boolean success = questionService.deleteQuestion(idRequest);
        if (!success) {
            throw new BusinessException(Err.SYSTEM_ERROR);
        }
        return ApiResponse.success(true);
    }

    @GetMapping("/get/{appId}")
    public ApiResponse<Question> getQuestionByAppId(@PathVariable Long appId) {
        return ApiResponse.success(questionService.getQuestionByAppId(appId));
    }

    @GetMapping("/ai_generate/sse")
    public SseEmitter aiGenerateQuestionSse(
            Long appId,
            Integer questionNumber,
            Integer optionNumber,
            HttpServletRequest request) {
        Long userId = userService.getLoginUserId(request);

        // 限流
        redisLimiterManager.doRateLimit(userId + "_aiGenerateQuestionSSE");  // 粒度：用户 + 方法

        if (appId == null || questionNumber == null || optionNumber == null) {
            throw new BusinessException(Err.PARAMS_ERROR);
        }

        // 获取应用信息
        Application application = applicationService.getById(appId);
        if (application == null) {
            throw new BusinessException(Err.NOT_FOUND_ERROR);
        }

        // user prompt and system prompt
        String userMessage = AIMessageUtil.getUserMessage(application, questionNumber, optionNumber);
        String systemMessage = AIMessageUtil.getSystemMessage(application);

        //建立 sse 连接对象，0表示永不超时
        SseEmitter emitter = new SseEmitter(0L);

        Consumer<ChatCompletionResponse> partialResponseCallback = getChatCompletionResponseConsumer(emitter);

        Runnable completionCallback = emitter::complete;

        Consumer<Throwable> errorCallback = throwable -> {
            log.error("Error during chat completion", throwable);
            emitter.completeWithError(throwable);
        };

        aiManager.doStreamRequest(
                systemMessage,
                userMessage,
                partialResponseCallback,
                completionCallback,
                errorCallback
        );

        return emitter;
    }

    private static Consumer<ChatCompletionResponse> getChatCompletionResponseConsumer(SseEmitter emitter) {
        StringBuilder contentBuilder = new StringBuilder();
        AtomicInteger count = new AtomicInteger(0);

        return response -> {
            String message = response.choices().get(0).delta().content();

            if (message == null) return;

            // openai 返回的字符中可能包括换行符 \n，而 SSE 协议中换行符有特殊含义，用于分隔消息的不同字段，若不处理会造成客户端解析出错
            // 解决：将换行符替换为空格，注意不能直接删除所有空白字符，因为单词之间有空格
            message = message.replaceAll("\\R", " ");

            for (char c : message.toCharArray()) {
                if (c == '{') {
                    count.incrementAndGet();
                }
                if (count.get() > 0) {
                    contentBuilder.append(c);
                }
                if (c == '}') {
                    count.decrementAndGet();
                    if (count.get() == 0) {
                        try {
                            emitter.send(contentBuilder.toString());
                        } catch (IOException e) {
                            log.error("Failed to send SSE event: {}", e.getMessage(), e);
                            emitter.completeWithError(e);
                            return;
                        } finally {
                            contentBuilder.setLength(0);
                        }
                    }
                }
            }
        };
    }
}
