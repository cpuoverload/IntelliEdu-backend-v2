package com.cpuoverload.intelliedu.service;

import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.baomidou.mybatisplus.extension.service.IService;
import com.cpuoverload.intelliedu.common.dto.IdRequest;
import com.cpuoverload.intelliedu.model.dto.answerrecord.ListAnswerRequest;
import com.cpuoverload.intelliedu.model.dto.answerrecord.ListMyAnswerRequest;
import com.cpuoverload.intelliedu.model.entity.AnswerRecord;
import com.cpuoverload.intelliedu.model.vo.AnswerRecordVo;

import javax.servlet.http.HttpServletRequest;

/**
* @author passion
* @description 针对表【answer_record(Answer Record)】的数据库操作Service
* @createDate 2024-10-15 16:41:48
*/
public interface AnswerRecordService extends IService<AnswerRecord> {
    Long addMyAnswerRecord(AnswerRecord answerRecord, HttpServletRequest request);

    Page<AnswerRecordVo> listMyAnswerRecord(ListMyAnswerRequest listMyAnswerRequest, HttpServletRequest request);

    AnswerRecordVo getAnswerRecord(Long id);

    Page<AnswerRecordVo> listAnswerRecord(ListAnswerRequest listAnswerRequest);

    Boolean deleteAnswerRecord(IdRequest idRequest);
}
