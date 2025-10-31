package com.macro.mall.auth.controller;

import io.swagger.v3.oas.annotations.tags.Tag;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.RequestMapping;

@Controller
@RequestMapping("/auth")
@Tag(name = "AuthController", description = "统一认证授权接口")
public class AuthController {
}
