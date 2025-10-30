package com.macro.mall.config;

import cn.dev33.satoken.context.SaHolder;
import cn.dev33.satoken.exception.NotLoginException;
import cn.dev33.satoken.exception.NotPermissionException;
import cn.dev33.satoken.reactor.context.SaReactorSyncHolder;
import cn.dev33.satoken.reactor.filter.SaReactorFilter;
import cn.dev33.satoken.router.SaHttpMethod;
import cn.dev33.satoken.router.SaRouter;
import cn.dev33.satoken.stp.StpUtil;
import cn.hutool.core.collection.CollUtil;
import cn.hutool.core.convert.Convert;
import com.macro.mall.util.StpMemberUtil;
import com.marco.mall.common.api.CommonResult;
import com.marco.mall.common.constant.AuthConstant;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.http.HttpHeaders;
import org.springframework.util.AntPathMatcher;
import org.springframework.util.PathMatcher;
import org.springframework.web.server.ServerWebExchange;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.Set;

/**
 * Sa-Token相关配置
 * Created by macro on 2020/6/19.
 */
@Configuration
public class SaTokenConfig {

    @Autowired
    private RedisTemplate<String,Object> redisTemplate;

    @Bean
    public SaReactorFilter getSaReactorFilter(IgnoreUrlsConfig ignoreUrlsConfig) {
        return new SaReactorFilter()
                    // 拦截地址
                .addInclude("/**")
                    // 配置白名单路径
                .setExcludeList(ignoreUrlsConfig.getUrls())
                    // 鉴权方法：每次访问进入
                .setAuth(obj -> {
                    // 对于OPTIONS预检请求直接放行
                    SaRouter.match(SaHttpMethod.OPTIONS).stop();
                    // 登录认证：商城前台会员认证
                    SaRouter.match("/mall-portal/**", r -> StpMemberUtil.checkLogin()).stop();
                    // 登录认证：管理后台用户认证
                    SaRouter.match("/mall-admin/**", r -> StpUtil.checkLogin());

                    // 权限认证：管理后台用户权限校验
                    // 获取Redis中缓存的各个接口路径所需权限规则
                    Map<Object, Object> pathResourceMap = redisTemplate.opsForHash().entries(AuthConstant.PATH_RESOURCE_MAP);
                    // 获取到访问当前接口所需权限（一个路径对应多个资源时，拥有任意一个资源都可以访问该路径）
                    List<String> needPermissionList = new ArrayList<>();
                    // 获取当前请求路径
                    String requestPath = SaHolder.getRequest().getRequestPath();
                    // 创建路径匹配器
                    PathMatcher pathMatcher = new AntPathMatcher();
                    Set<Map.Entry<Object, Object>> entries = pathResourceMap.entrySet();
                    for (Map.Entry<Object, Object> entry : entries) {
                        String pattern = (String) entry.getKey();
                        if(pathMatcher.match(pattern, requestPath)){
                            needPermissionList.add((String) entry.getValue());
                        }
                    }

                    // 进行权限校验
                    if(CollUtil.isNotEmpty(needPermissionList)){
                        SaRouter.match(requestPath,r -> StpUtil.checkPermissionOr(Convert.toStrArray(needPermissionList)));
                    }

                })
                //setAuth方法结束
                    // 异常处理：每次setAuth函数出现异常时进入
                .setError(this::handleException);
    }

    /**
     * 自定义异常处理
     */
    private CommonResult handleException(Throwable ex) {
        ServerWebExchange context = SaReactorSyncHolder.getContext();
        // 设置错误返回格式为JSON
        HttpHeaders headers = context.getResponse().getHeaders();
        headers.set("Content-Type", "application/json; charset=utf-8");
        headers.set("Access-Control-Allow-Origin", "*");
        headers.set("Cache-Control","no-cache");
        CommonResult result = null;
        if(ex instanceof NotLoginException) {
            result = CommonResult.unauthorized(null);
        } else if (ex instanceof NotPermissionException) {
            result = CommonResult.forbidden(null);
        } else {
            result = CommonResult.failed(ex.getMessage());
        }
        return result;
    }
}
