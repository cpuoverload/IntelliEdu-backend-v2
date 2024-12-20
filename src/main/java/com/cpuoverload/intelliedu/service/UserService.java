package com.cpuoverload.intelliedu.service;

import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.baomidou.mybatisplus.extension.service.IService;
import com.cpuoverload.intelliedu.model.dto.user.ListUserRequest;
import com.cpuoverload.intelliedu.model.entity.User;
import com.cpuoverload.intelliedu.model.vo.UserVo;

import javax.servlet.http.HttpServletRequest;

public interface UserService extends IService<User> {

    Long getLoginUserId(HttpServletRequest request);

    Long register(User user);

    UserVo login(String username, String password, HttpServletRequest request);

    boolean logout(HttpServletRequest request);

    UserVo getMyInfo(HttpServletRequest request);

    boolean updateMyInfo(User user);

    UserVo getUserById(Long id);

    Page<UserVo> listUser(ListUserRequest listUserRequest);

    Long addUser(User user);

    boolean updateUser(User user);

    boolean deleteUser(Long id);
}
