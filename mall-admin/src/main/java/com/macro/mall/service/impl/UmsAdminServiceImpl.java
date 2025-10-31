package com.macro.mall.service.impl;

import cn.dev33.satoken.secure.BCrypt;
import cn.dev33.satoken.stp.SaTokenInfo;
import cn.dev33.satoken.stp.StpUtil;
import cn.hutool.core.util.StrUtil;
import com.macro.mall.dao.UmsAdminRoleRelationDao;
import com.macro.mall.mapper.UmsAdminLoginLogMapper;
import com.macro.mall.mapper.UmsAdminMapper;
import com.macro.mall.model.UmsAdmin;
import com.macro.mall.model.UmsAdminExample;
import com.macro.mall.model.UmsAdminLoginLog;
import com.macro.mall.model.UmsResource;
import com.macro.mall.service.UmsAdminService;
import com.marco.mall.common.constant.AuthConstant;
import com.marco.mall.common.dto.UserDto;
import com.marco.mall.common.exception.Asserts;
import jakarta.servlet.http.HttpServletRequest;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.web.context.request.RequestAttributes;
import org.springframework.web.context.request.RequestContextHolder;
import org.springframework.web.context.request.ServletRequestAttributes;

import java.util.Date;
import java.util.List;
import java.util.stream.Collectors;

@Service
public class UmsAdminServiceImpl implements UmsAdminService {

    @Autowired
    private UmsAdminRoleRelationDao adminRoleRelationDao;

    @Autowired
    private UmsAdminMapper adminMapper;

    @Autowired
    private UmsAdminLoginLogMapper loginLogMapper;

    @Override
    public SaTokenInfo login(String username, String password) {
        if (StrUtil.isEmpty(username) || StrUtil.isEmpty(password)){
            Asserts.fail("用户名或密码不能为空");
        }
        UmsAdmin admin = getAdminByUsername(username);
        if (admin == null) {
            Asserts.fail("找不到该用户");
        }
        if (!BCrypt.checkpw(password, admin.getPassword())) {
            Asserts.fail("密码错误");
        }
        if (admin.getStatus() != 1) {
            Asserts.fail("该用户已被禁用");
        }

        //校验成功后，登陆
        StpUtil.login(admin);

        UserDto userDto = new UserDto();
        userDto.setUsername(admin.getUsername());
        userDto.setId(admin.getId());
        userDto.setClientId(AuthConstant.ADMIN_CLIENT_ID);
        List<UmsResource> resourceList = getResourceList(userDto.getId());
        List<String> permissionList = resourceList.stream().map(item -> item.getId() + ":" + item.getName()).toList();
        userDto.setPermissionList(permissionList);

        // 将用户信息存储到Session中
        StpUtil.getSession().set(AuthConstant.STP_ADMIN_INFO, userDto);
        // 获取当前登录用户Token信息
        SaTokenInfo tokenInfo = StpUtil.getTokenInfo();

        //输出日志
        insertLoginLog(admin);

        return tokenInfo;
    }

    private void insertLoginLog(UmsAdmin admin) {
        if(admin == null) return;
        UmsAdminLoginLog umsAdminLoginLog = new UmsAdminLoginLog();
        umsAdminLoginLog.setAdminId(admin.getId());
        umsAdminLoginLog.setCreateTime(new Date());
        ServletRequestAttributes requestAttributes = (ServletRequestAttributes)RequestContextHolder.getRequestAttributes();
        HttpServletRequest request = requestAttributes.getRequest();

        umsAdminLoginLog.setIp(request.getRemoteAddr());

        loginLogMapper.insert(umsAdminLoginLog);

    }

    @Override
    public List<UmsResource> getResourceList(Long adminId) {
        return adminRoleRelationDao.getResourceList(adminId);
    }

    @Override
    public UmsAdmin getAdminByUsername(String username){
        UmsAdminExample example = new UmsAdminExample();
        example.createCriteria().andUsernameEqualTo(username);
        List<UmsAdmin> adminList = adminMapper.selectByExample(example);
        if(adminList != null && adminList.size() > 0) {
            return adminList.get(0);
        }
        return null;
    }
}
