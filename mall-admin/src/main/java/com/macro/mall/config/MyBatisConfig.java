package com.macro.mall.config;

import org.mybatis.spring.annotation.MapperScan;
import org.springframework.context.annotation.Configuration;
import org.springframework.transaction.annotation.EnableTransactionManagement;
import org.springframework.transaction.annotation.Transactional;

@MapperScan({"com.macro.mall.mapper","com.macro.mall.dao"})
@EnableTransactionManagement
@Configuration
public class MyBatisConfig {
}
