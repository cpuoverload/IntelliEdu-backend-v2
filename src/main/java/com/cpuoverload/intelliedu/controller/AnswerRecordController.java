package com.cpuoverload.intelliedu.controller;

import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.cpuoverload.intelliedu.common.dto.IdRequest;
import com.cpuoverload.intelliedu.common.response.ApiResponse;
import com.cpuoverload.intelliedu.exception.BusinessException;
import com.cpuoverload.intelliedu.exception.Err;
import com.cpuoverload.intelliedu.model.dto.answerrecord.AddMyAnswerRequest;
import com.cpuoverload.intelliedu.model.dto.answerrecord.ListAnswerRequest;
import com.cpuoverload.intelliedu.model.dto.answerrecord.ListMyAnswerRequest;
import com.cpuoverload.intelliedu.model.entity.AnswerRecord;
import com.cpuoverload.intelliedu.model.vo.AnswerRecordVo;
import com.cpuoverload.intelliedu.service.AnswerRecordService;
import org.springframework.beans.BeanUtils;
import org.springframework.web.bind.annotation.*;

import javax.annotation.Resource;
import javax.servlet.http.HttpServletRequest;

@RestController
@RequestMapping("/answer-record")
public class AnswerRecordController {

    @Resource
    AnswerRecordService answerRecordService;


    // 普通用户添加答题记录
    @PostMapping("/add/me")
    public ApiResponse<Long> addMyAnswerRecord(@RequestBody AddMyAnswerRequest addMyAnswerRequest, HttpServletRequest request) {
        if (addMyAnswerRequest == null) {
            throw new BusinessException(Err.PARAMS_ERROR);
        }
        if (addMyAnswerRequest.getAppId() == null || addMyAnswerRequest.getAnswers() == null) {
            throw new BusinessException(Err.PARAMS_ERROR);
        }
        AnswerRecord answerRecord = new AnswerRecord();
        BeanUtils.copyProperties(addMyAnswerRequest, answerRecord);
        Long id = answerRecordService.addMyAnswerRecord(answerRecord, request);
        return ApiResponse.success(id);
    }

    // 普通用户查看答题记录
    @PostMapping("/list/me")
    public ApiResponse<Page<AnswerRecordVo>> listMyAnswerRecord(@RequestBody ListMyAnswerRequest listMyAnswerRequest, HttpServletRequest request) {
        if (listMyAnswerRequest == null) {
            throw new BusinessException(Err.PARAMS_ERROR);
        }
        Page<AnswerRecordVo> answerRecordVoPage = answerRecordService.listMyAnswerRecord(listMyAnswerRequest, request);
        return ApiResponse.success(answerRecordVoPage);
    }

    @GetMapping("/get/{id}")
    public ApiResponse<AnswerRecordVo> getAnswerRecordById(@PathVariable Long id) {
        if (id == null) {
            throw new BusinessException(Err.PARAMS_ERROR);
        }
        AnswerRecordVo answerRecordVo = answerRecordService.getAnswerRecord(id);
        return ApiResponse.success(answerRecordVo);
    }

    // 管理员查看答题记录
    @PostMapping("/list")
    public ApiResponse<Page<AnswerRecordVo>> listAnswerRecord(@RequestBody ListAnswerRequest listAnswerRequest) {
        if (listAnswerRequest == null) {
            throw new BusinessException(Err.PARAMS_ERROR);
        }
        Page<AnswerRecordVo> answerRecordVoPage = answerRecordService.listAnswerRecord(listAnswerRequest);
        return ApiResponse.success(answerRecordVoPage);
    }

    // 管理员删除答题记录
    @PostMapping("/delete")
    public ApiResponse<Boolean> deleteAnswerRecord(@RequestBody IdRequest idRequest) {
        if (idRequest == null) {
            throw new BusinessException(Err.PARAMS_ERROR);
        }
        Boolean success = answerRecordService.deleteAnswerRecord(idRequest);
        if (!success) {
            throw new BusinessException(Err.SYSTEM_ERROR);
        }
        return ApiResponse.success(true);
    }
}
