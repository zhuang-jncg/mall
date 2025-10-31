package com.macro.mall.service;

import cn.dev33.satoken.stp.SaTokenInfo;
import com.macro.mall.model.UmsAdmin;
import com.macro.mall.model.UmsResource;

import java.util.List;

/**
 * 后台用户管理Service
 */
public interface UmsAdminService {



    /**
     * 获取指定用户的可访问资源
     */
    List<UmsResource> getResourceList(Long adminId);

    /**
     * 根据用户名获取后台管理员用户
     * @param username
     * @return
     */
    UmsAdmin getAdminByUsername(String username);

    /**
     * 登陆功能
     * @param username 用户名
     * @param password 密码
     */
    SaTokenInfo login(String username, String password);
}
