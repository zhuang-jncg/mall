package com.macro.mall.service.impl;

import com.macro.mall.mapper.UmsResourceMapper;
import com.macro.mall.model.UmsResource;
import com.macro.mall.model.UmsResourceExample;
import com.macro.mall.service.UmsResourceService;
import com.marco.mall.common.constant.AuthConstant;
import com.marco.mall.common.service.RedisService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;

import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.TreeMap;

@Service
public class UmsResourceServiceImpl implements UmsResourceService {

    @Autowired
    private UmsResourceMapper resourceMapper;

    @Autowired
    private RedisService redisService;

    @Value("${spring.application.name}")
    private String applicationName;

    @Override
    public Map<String, String> initPathResourceMap() {
        Map<String,String> pathResourceMap = new TreeMap<>();
        List<UmsResource> umsResources = resourceMapper.selectByExample(new UmsResourceExample());
        for (UmsResource umsResource : umsResources) {
            pathResourceMap.put("/" + applicationName + umsResource.getUrl(), umsResource.getId()+":"+umsResource.getName());
        }
        redisService.del(AuthConstant.PATH_RESOURCE_MAP);
        redisService.hSetAll(AuthConstant.PATH_RESOURCE_MAP, pathResourceMap);

        return pathResourceMap;
    }
}
