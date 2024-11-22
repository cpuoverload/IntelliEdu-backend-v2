package com.cpuoverload.intelliedu.service.impl;

import cn.hutool.core.util.StrUtil;
import com.baomidou.mybatisplus.core.conditions.query.QueryWrapper;
import com.baomidou.mybatisplus.core.metadata.IPage;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.cpuoverload.intelliedu.common.dto.IdRequest;
import com.cpuoverload.intelliedu.exception.BusinessException;
import com.cpuoverload.intelliedu.exception.Err;
import com.cpuoverload.intelliedu.mapper.AnswerRecordMapper;
import com.cpuoverload.intelliedu.model.dto.answerrecord.ListAnswerRequest;
import com.cpuoverload.intelliedu.model.dto.answerrecord.ListMyAnswerRequest;
import com.cpuoverload.intelliedu.model.entity.AnswerRecord;
import com.cpuoverload.intelliedu.model.entity.Application;
import com.cpuoverload.intelliedu.model.vo.AnswerRecordVo;
import com.cpuoverload.intelliedu.scoring.ScoringStrategyExecutor;
import com.cpuoverload.intelliedu.service.AnswerRecordService;
import com.cpuoverload.intelliedu.service.ApplicationService;
import com.cpuoverload.intelliedu.service.UserService;
import org.springframework.beans.BeanUtils;
import org.springframework.stereotype.Service;

import javax.annotation.Resource;
import javax.servlet.http.HttpServletRequest;
import java.util.List;
import java.util.stream.Collectors;

/**
 * @author passion
 * @description 针对表【answer_record(Answer Record)】的数据库操作Service实现
 * @createDate 2024-10-15 16:41:48
 */
@Service
public class AnswerRecordServiceImpl extends ServiceImpl<AnswerRecordMapper, AnswerRecord>
        implements AnswerRecordService {

    @Resource
    private UserService userService;

    @Resource
    private ApplicationService applicationService;

    @Resource
    private ScoringStrategyExecutor scoringStrategyExecutor;

    public void validate(AnswerRecord answerRecord) {
        if (answerRecord == null) {
            throw new BusinessException(Err.PARAMS_ERROR);
        }
        Long appId = answerRecord.getAppId();
        List<String> answers = answerRecord.getAnswers();

        if (appId != null && appId <= 0) {
            throw new BusinessException(Err.PARAMS_ERROR, "appId is invalid");
        }
        if (answers == null || answers.isEmpty()) {
            throw new BusinessException(Err.PARAMS_ERROR, "answers is empty");
        }

        Application application = applicationService.getApplicationById(appId);

        if (application == null) {
            throw new BusinessException(Err.PARAMS_ERROR, "application not found");
        }

        if (application.getAuditStatus() != 1) {
            throw new BusinessException(Err.PARAMS_ERROR, "application not audited");
        }


    }

    @Override
    public Long addMyAnswerRecord(AnswerRecord answerRecord, HttpServletRequest request) {
        // 1. validation
        validate(answerRecord);

        // 2. set not null fields
        Long userId = userService.getLoginUserId(request);
        answerRecord.setUserId(userId);
        Application application = applicationService.getApplicationById(answerRecord.getAppId());

        boolean success = save(answerRecord);
        if (!success) {
            throw new BusinessException(Err.SYSTEM_ERROR, "save answer record failed");
        }
        Long answerRecordId = answerRecord.getId();

        // 3. do score
        try {
            AnswerRecord userAnswerWithResult = scoringStrategyExecutor.doScore(answerRecord.getAnswers(), application);
            userAnswerWithResult.setId(answerRecordId);
            updateById(userAnswerWithResult);
        } catch (Exception e) {
            log.error("do score failed", e);
            throw new BusinessException(Err.SYSTEM_ERROR, "do score failed");
        }

        return answerRecordId;
    }

    @Override
    public Page<AnswerRecordVo> listMyAnswerRecord(ListMyAnswerRequest listMyAnswerRequest, HttpServletRequest request) {
        // 1. set page info
        Long current = listMyAnswerRequest.getCurrent();
        Long pageSize = listMyAnswerRequest.getPageSize();
        IPage<AnswerRecord> page = new Page<>(current, pageSize);

        Long userId = userService.getLoginUserId(request);

        // 2. paged query
        // 由于不是所有字段都是精确查询，有的字段需要模糊查询，有的字段需要排序，所以不能简单地写成 new QueryWrapper(entity)
        // sortField 是动态传入的列名，无法使用 LambdaQueryWrapper
        QueryWrapper<AnswerRecord> queryWrapper = new QueryWrapper<>();
        queryWrapper
                .eq("user_id", userId)
                .orderBy(listMyAnswerRequest.getSortField() != null, listMyAnswerRequest.getIsAscend(), StrUtil.toUnderlineCase(listMyAnswerRequest.getSortField()));
        IPage<AnswerRecord> answerRecordIPage = page(page, queryWrapper);

        // 3. convert entity to vo
        Page<AnswerRecordVo> answerRecordVoPage = new Page<>(current, pageSize, answerRecordIPage.getTotal());
        List<AnswerRecordVo> voRecords = answerRecordIPage.getRecords().stream().map(this::entityToVo).collect(Collectors.toList());
        answerRecordVoPage.setRecords(voRecords);

        return answerRecordVoPage;
    }

    @Override
    public AnswerRecordVo getAnswerRecord(Long id) {
        // 1. validation
        if (id == null || id <= 0) {
            throw new BusinessException(Err.PARAMS_ERROR);
        }

        // 2. get answerRecord
        AnswerRecord answerRecord = getById(id);
        if (answerRecord == null) {
            throw new BusinessException(Err.NOT_FOUND_ERROR);
        }

        // 3. convert entity to vo
        return entityToVo(answerRecord);
    }

    public AnswerRecordVo entityToVo(AnswerRecord answerRecord) {
        AnswerRecordVo answerRecordVo = new AnswerRecordVo();
        BeanUtils.copyProperties(answerRecord, answerRecordVo);
        return answerRecordVo;
    }

    @Override
    public Page<AnswerRecordVo> listAnswerRecord(ListAnswerRequest listAnswerRequest) {
        // 1. set page info
        Long current = listAnswerRequest.getCurrent();
        Long pageSize = listAnswerRequest.getPageSize();
        IPage<AnswerRecord> page = new Page<>(current, pageSize);

        // 2. paged query
        QueryWrapper<AnswerRecord> queryWrapper = new QueryWrapper<>();
        queryWrapper
                .eq(listAnswerRequest.getId() != null, "id", listAnswerRequest.getId())
                .eq(listAnswerRequest.getAppType() != null, "app_type", listAnswerRequest.getAppType())
                .eq(listAnswerRequest.getStrategy() != null, "strategy", listAnswerRequest.getStrategy())
                .eq(listAnswerRequest.getUserId() != null, "user_id", listAnswerRequest.getUserId())
                .eq(listAnswerRequest.getAppId() != null, "app_id", listAnswerRequest.getAppId())
                .eq(listAnswerRequest.getResultId() != null, "result_id", listAnswerRequest.getResultId())
                .eq(listAnswerRequest.getResultGrade() != null, "result_grade", listAnswerRequest.getResultGrade())
                .orderBy(listAnswerRequest.getSortField() != null, listAnswerRequest.getIsAscend(), StrUtil.toUnderlineCase(listAnswerRequest.getSortField()));
        IPage<AnswerRecord> answerRecordIPage = page(page, queryWrapper);

        // 3. convert entity to vo
        Page<AnswerRecordVo> answerRecordVoPage = new Page<>(current, pageSize, answerRecordIPage.getTotal());
        List<AnswerRecordVo> voRecords = answerRecordIPage.getRecords().stream().map(this::entityToVo).collect(Collectors.toList());
        answerRecordVoPage.setRecords(voRecords);

        return answerRecordVoPage;
    }

    @Override
    public Boolean deleteAnswerRecord(IdRequest idRequest) {
        // 1. validation
        if (idRequest == null || idRequest.getId() == null) {
            throw new BusinessException(Err.PARAMS_ERROR);
        }

        // 2. check if the answerRecord exists
        AnswerRecord answerRecord = getById(idRequest.getId());
        if (answerRecord == null) {
            throw new BusinessException(Err.NOT_FOUND_ERROR);
        }

        // 3. delete answerRecord
        return removeById(idRequest.getId());
    }
}




