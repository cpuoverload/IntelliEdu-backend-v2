package com.cpuoverload.intelliedu.manager;

import dev.ai4j.openai4j.OpenAiClient;
import dev.ai4j.openai4j.chat.ChatCompletionRequest;
import dev.ai4j.openai4j.chat.ChatCompletionResponse;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;

import javax.annotation.Resource;
import java.util.function.Consumer;

import static com.cpuoverload.intelliedu.constant.AIConstant.*;
import static com.cpuoverload.intelliedu.constant.AIConstant.TEMPERATURE;

@Component
@Slf4j
public class AiManager {

    @Resource
    private OpenAiClient openAiClient;

    /**
     * 同步请求
     */
    public String doRequest(String systemMessage, String userMessage) {
        ChatCompletionRequest request = ChatCompletionRequest.builder()
                .model(MODEL)
                .maxTokens(MAX_TOKEN_LENGTH)
                .addSystemMessage(systemMessage)
                .addUserMessage(userMessage)
                .temperature(TEMPERATURE)
                .build();

        ChatCompletionResponse response = openAiClient.chatCompletion(request).execute();
        return response.choices().get(0).message().content();
    }

    /**
     * 流式请求
     */
    public void doStreamRequest(
            String systemMessage,
            String userMessage,
            Consumer<ChatCompletionResponse> partialResponseCallback,
            Runnable completionCallback,
            Consumer<Throwable> errorCallback) {
        ChatCompletionRequest request = ChatCompletionRequest.builder()
                .model(MODEL)
                // .stream(true) 不需要加
                .maxTokens(MAX_TOKEN_LENGTH)
                .addSystemMessage(systemMessage)
                .addUserMessage(userMessage)
                .temperature(TEMPERATURE)
                .build();

        openAiClient.chatCompletion(request)
                .onPartialResponse(partialResponseCallback)
                .onComplete(completionCallback)
                .onError(errorCallback)
                .execute();
    }
}
