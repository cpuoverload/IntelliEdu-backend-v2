package com.cpuoverload.intelliedu.service;

import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.baomidou.mybatisplus.extension.service.IService;
import com.cpuoverload.intelliedu.common.dto.IdRequest;
import com.cpuoverload.intelliedu.model.dto.application.ListAppRequest;
import com.cpuoverload.intelliedu.model.dto.application.ListMyAppRequest;
import com.cpuoverload.intelliedu.model.dto.application.ListPublicAppRequest;
import com.cpuoverload.intelliedu.model.entity.Application;
import com.cpuoverload.intelliedu.model.vo.ApplicationVo;

import javax.servlet.http.HttpServletRequest;

public interface ApplicationService extends IService<Application> {
    Page<ApplicationVo> listPublicApplication(ListPublicAppRequest listPublicAppRequest);

    Long addMyApplication(Application application, HttpServletRequest request);

    Page<ApplicationVo> listMyApplication(ListMyAppRequest listMyAppRequest, HttpServletRequest request);

    Boolean updateMyApplication(Application application, HttpServletRequest request);

    Boolean deleteMyApplication(IdRequest idRequest, HttpServletRequest request);

    Page<ApplicationVo> listApplication(ListAppRequest listAppRequest);

    Boolean updateApplication(Application application);

    Boolean deleteApplication(IdRequest idRequest);

    Boolean auditApplication(Application application, HttpServletRequest request);

    Application getApplicationById(Long id);
}
