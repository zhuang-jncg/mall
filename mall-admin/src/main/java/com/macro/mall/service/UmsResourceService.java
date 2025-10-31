package com.macro.mall.service;

import java.util.Map;

/**
 * 后台资源管理Service
 * Created by macro on 2020/2/2.
 */
public interface UmsResourceService {

    /**
     * 初始化路径与资源访问规则
     */
    Map<String,String> initPathResourceMap();
}
