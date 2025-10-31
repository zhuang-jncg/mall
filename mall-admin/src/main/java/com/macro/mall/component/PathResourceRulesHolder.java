package com.macro.mall.component;

import com.macro.mall.service.UmsResourceService;
import jakarta.annotation.PostConstruct;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

@Component
public class PathResourceRulesHolder {

    @Autowired
    private UmsResourceService resourceService;

    @PostConstruct
    public void initPathResourceMap(){
        resourceService.initPathResourceMap();
    }

}
