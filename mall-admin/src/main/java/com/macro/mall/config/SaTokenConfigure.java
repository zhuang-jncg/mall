package com.macro.mall.config;

import cn.dev33.satoken.jwt.StpLogicJwtForSimple;
import cn.dev33.satoken.stp.StpLogic;
import org.springframework.context.annotation.Configuration;

@Configuration
public class SaTokenConfigure {

    public StpLogic getStpLogicJwt() {
        return new StpLogicJwtForSimple();
    }
}