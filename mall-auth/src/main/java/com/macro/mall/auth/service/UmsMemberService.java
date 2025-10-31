package com.macro.mall.auth.service;

import com.marco.mall.common.api.CommonResult;
import org.springframework.cloud.openfeign.FeignClient;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestParam;

@FeignClient("mall-portal")
public interface UmsMemberService {

    @PostMapping("/sso/login")
    CommonResult login(@RequestParam("username") String username, @RequestParam("password") String password);
}
