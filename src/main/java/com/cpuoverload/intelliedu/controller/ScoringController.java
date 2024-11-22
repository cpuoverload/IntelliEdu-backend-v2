package com.cpuoverload.intelliedu.controller;

import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.cpuoverload.intelliedu.common.dto.IdRequest;
import com.cpuoverload.intelliedu.common.response.ApiResponse;
import com.cpuoverload.intelliedu.exception.BusinessException;
import com.cpuoverload.intelliedu.exception.Err;
import com.cpuoverload.intelliedu.model.dto.scoring.*;
import com.cpuoverload.intelliedu.model.entity.AnswerRecord;
import com.cpuoverload.intelliedu.model.entity.Scoring;
import com.cpuoverload.intelliedu.model.vo.ScoringVo;
import com.cpuoverload.intelliedu.scoring.ScoringStrategyExecutor;
import com.cpuoverload.intelliedu.service.ScoringService;
import org.springframework.beans.BeanUtils;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import javax.annotation.Resource;
import javax.servlet.http.HttpServletRequest;
import java.util.List;
import java.util.stream.Collectors;

@RestController
@RequestMapping("/scoring")
public class ScoringController {

    @Resource
    private ScoringService scoringService;

    @Resource
    private ScoringStrategyExecutor scoringStrategyExecutor;

    // 普通用户添加评分规则
    @PostMapping("/add/me")
    public ApiResponse<Boolean> addMyScoring(@RequestBody AddMyScoringRequest addMyScoringRequest, HttpServletRequest request) {
        if (addMyScoringRequest == null) {
            throw new BusinessException(Err.PARAMS_ERROR);
        }
        if (addMyScoringRequest.getAppId() == null || addMyScoringRequest.getResultName() == null || addMyScoringRequest.getResultAttributes() == null) {
            throw new BusinessException(Err.PARAMS_ERROR);
        }

        Scoring scoring = new Scoring();
        BeanUtils.copyProperties(addMyScoringRequest, scoring);
        Boolean success = scoringService.addMyScoring(scoring, request);
        if (!success) {
            throw new BusinessException(Err.SYSTEM_ERROR);
        }

        return ApiResponse.success(true);
    }

    // 普通用户批量添加评分规则
    @PostMapping("/add/me/batch")
    public ApiResponse<Boolean> addMyScoringBatch(@RequestBody AddMyScoringBatchRequest addMyScoringBatchRequest, HttpServletRequest request) {
        if (addMyScoringBatchRequest == null || addMyScoringBatchRequest.getScorings() == null || addMyScoringBatchRequest.getScorings().isEmpty()) {
            throw new BusinessException(Err.PARAMS_ERROR);
        }

        List<AddMyScoringRequest> scoringsRequest = addMyScoringBatchRequest.getScorings();

        List<Scoring> scoringList = scoringsRequest.stream()
                .map(scoringRequest -> {
                    // dto -> entity
                    Scoring scoring = new Scoring();
                    BeanUtils.copyProperties(scoringRequest, scoring);
                    return scoring;

                })
                .collect(Collectors.toList());

        boolean success = scoringService.addMyScoringBatch(scoringList, request);
        if (!success) {
            throw new BusinessException(Err.SYSTEM_ERROR);
        }

        return ApiResponse.success(true);

    }

    // 普通用户查看评分规则
    @PostMapping("/list/me")
    public ApiResponse<Page<ScoringVo>> listMyScoring(@RequestBody ListMyScoringRequest listMyScoringRequest, HttpServletRequest request) {
        if (listMyScoringRequest == null) {
            throw new BusinessException(Err.PARAMS_ERROR);
        }

        Page<ScoringVo> scorigVoPage = scoringService.listMyScoring(listMyScoringRequest, request);
        return ApiResponse.success(scorigVoPage);
    }

    // 普通用户更新评分规则
    @PostMapping("/update/me")
    public ApiResponse<Boolean> updateMyScoring(@RequestBody UpdateMyScoringRequest updateMyScoringRequest, HttpServletRequest request) {
        if (updateMyScoringRequest == null) {
            throw new BusinessException(Err.PARAMS_ERROR);
        }
        Scoring scoring = new Scoring();
        BeanUtils.copyProperties(updateMyScoringRequest, scoring);
        Boolean success = scoringService.updateMyScoring(scoring, request);
        if (!success) {
            throw new BusinessException(Err.SYSTEM_ERROR);
        }
        return ApiResponse.success(true);
    }

    // 普通用户删除评分规则
    @PostMapping("/delete/me")
    public ApiResponse<Boolean> deleteMyScoring(@RequestBody IdRequest idRequest, HttpServletRequest request) {
        if (idRequest == null) {
            throw new BusinessException(Err.PARAMS_ERROR);
        }
        Boolean success = scoringService.deleteMyScoring(idRequest, request);
        if (!success) {
            throw new BusinessException(Err.SYSTEM_ERROR);
        }
        return ApiResponse.success(true);
    }

    // 管理员添加评分规则
    @PostMapping("/add")
    public ApiResponse<Boolean> addScoring(@RequestBody AddScoringRequest addScoringRequest, HttpServletRequest request) {
        if (addScoringRequest == null) {
            throw new BusinessException(Err.PARAMS_ERROR);
        }
        if (addScoringRequest.getAppId() == null || addScoringRequest.getResultName() == null || addScoringRequest.getResultAttributes() == null) {
            throw new BusinessException(Err.PARAMS_ERROR);
        }

        Scoring scoring = new Scoring();
        BeanUtils.copyProperties(addScoringRequest, scoring);
        Boolean success = scoringService.addScoring(scoring, request);
        if (!success) {
            throw new BusinessException(Err.SYSTEM_ERROR);
        }
        return ApiResponse.success(true);
    }

    // 管理员查看评分规则
    @PostMapping("/list")
    public ApiResponse<Page<ScoringVo>> listScoring(@RequestBody ListScoringRequest listScoringRequest, HttpServletRequest request) {
        if (listScoringRequest == null) {
            throw new BusinessException(Err.PARAMS_ERROR);
        }
        Page<ScoringVo> scoringVoPage = scoringService.listScoring(listScoringRequest);
        return ApiResponse.success(scoringVoPage);

    }

    // 管理员更新评分规则
    @PostMapping("/update")
    public ApiResponse<Boolean> updateScoring(@RequestBody UpdateScoringRequest updateScoringRequest) {
        if (updateScoringRequest == null || updateScoringRequest.getId() == null) {
            throw new BusinessException(Err.PARAMS_ERROR);
        }
        Scoring scoring = new Scoring();
        BeanUtils.copyProperties(updateScoringRequest, scoring);
        Boolean success = scoringService.updateScoring(scoring);
        if (!success) {
            throw new BusinessException(Err.SYSTEM_ERROR);
        }
        return ApiResponse.success(true);
    }

    // 管理员删除评分规则
    @PostMapping("/delete")
    public ApiResponse<Boolean> deleteScoring(@RequestBody IdRequest idRequest, HttpServletRequest request) {
        if (idRequest == null) {
            throw new BusinessException(Err.PARAMS_ERROR);
        }
        Boolean success = scoringService.deleteScoring(idRequest);
        if (!success) {
            throw new BusinessException(Err.SYSTEM_ERROR);
        }
        return ApiResponse.success(true);
    }

    @PostMapping("/doScore")
    public AnswerRecord doScore(@RequestBody DoScoreRequest doScoreRequest) throws Exception {
        if (doScoreRequest == null) {
            throw new BusinessException(Err.PARAMS_ERROR);
        }
        return scoringStrategyExecutor.doScore(doScoreRequest.getAnswerList(), doScoreRequest.getApplication());

    }

}
