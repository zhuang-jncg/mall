package com.marco.mall.common.log;

import cn.hutool.core.util.StrUtil;
import cn.hutool.core.util.URLUtil;
import cn.hutool.json.JSONUtil;
import com.marco.mall.common.domain.WebLog;
import io.swagger.v3.oas.annotations.Operation;
import jakarta.servlet.http.HttpServletRequest;
import net.logstash.logback.marker.Markers;
import org.aspectj.lang.JoinPoint;
import org.aspectj.lang.ProceedingJoinPoint;
import org.aspectj.lang.Signature;
import org.aspectj.lang.annotation.*;
import org.aspectj.lang.reflect.MethodSignature;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.core.annotation.Order;
import org.springframework.stereotype.Component;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.ResponseBody;
import org.springframework.web.context.request.RequestAttributes;
import org.springframework.web.context.request.RequestContextHolder;
import org.springframework.web.context.request.ServletRequestAttributes;
import org.springframework.web.servlet.support.RequestContext;

import javax.xml.crypto.dsig.SignatureMethod;
import java.lang.reflect.Method;
import java.lang.reflect.Parameter;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

@Aspect
@Component
@Order(1)
public class WebLogAspect {

    private static final Logger LOGGER = LoggerFactory.getLogger(WebLogAspect.class);

    @Pointcut("execution(public * com.macro.mall.controller.*.*(..))||execution(public * com.macro.mall.*.controller.*.*(..)))")
    public void weLog() {}

    @Before(value = "weLog()")
    public void doBefore() throws  Throwable {
    }

    @AfterReturning(value = "weLog()",returning = "ret")
    public void doAfterReturning(Object ret) throws Throwable {}

    @Around("weLog()")
    public Object doAround(ProceedingJoinPoint joinPoint) throws Throwable {
        //获取当前时间
        long startTime = System.currentTimeMillis();

        //获取当前请求对象
        ServletRequestAttributes requestAttributes = (ServletRequestAttributes) RequestContextHolder.getRequestAttributes();
        HttpServletRequest request = requestAttributes.getRequest();


        //记录请求信息(通过Logstash传入Elasticsearch)
        WebLog webLog = new WebLog();

        //执行方法
        Object result = joinPoint.proceed();

        //获取方法
        MethodSignature signature = (MethodSignature)joinPoint.getSignature();
        Method method = signature.getMethod();


        //判断当前方法是否有 @Operation 注解，如果有，则获取该注解，并将其 summary 属性值设置为 webLog 的描述
        if(method.isAnnotationPresent(Operation.class))
        {
            Operation operation = method.getAnnotation(Operation.class);
            webLog.setDescription(operation.summary());
        }

        long endTime = System.currentTimeMillis();
        String url = request.getRequestURL().toString();
        webLog.setIp(request.getRemoteAddr());
        webLog.setUrl(url);
        webLog.setBasePath(StrUtil.removeSuffix(url, URLUtil.url(url).getPath()));
        webLog.setMethod(request.getMethod());
        webLog.setParameter(getParameter(method,joinPoint.getArgs()));
        webLog.setResult(result);
        webLog.setStartTime(startTime);
        webLog.setSpendTime((int) (endTime - startTime));
        webLog.setUri(request.getRequestURI());
        Map<String,Object> logMap = new HashMap<>();
        logMap.put("url",webLog.getUrl());
        logMap.put("method",webLog.getMethod());
        logMap.put("parameter",webLog.getParameter());
        logMap.put("spendTime",webLog.getSpendTime());
        logMap.put("description",webLog.getDescription());

        LOGGER.info(Markers.appendEntries(logMap), JSONUtil.parse(webLog).toString());
        return result;
    }

    private Object getParameter(Method method, Object[] args) {
        List<Object> argList = new ArrayList<>();
        Parameter[] parameters = method.getParameters();

        for (int i = 0; i < parameters.length; i++) {
            ResponseBody responseBody = parameters[i].getAnnotation(ResponseBody.class);
            if(responseBody != null){
                argList.add(args[i]);
            }

            RequestParam requestParam = parameters[i].getAnnotation(RequestParam.class);
            if (requestParam != null) {
                Map<String, Object> map = new HashMap<>();
                String key = parameters[i].getName();
                if(StrUtil.isNotEmpty(requestParam.value())){
                    key = requestParam.value();
                }
                map.put(key, args[i]);
                argList.add(map);
            }
        }
        if(argList.size() == 0){
            return null;
        }else if (argList.size() == 1){
            return argList.get(0);
        } else
            return argList;
    }
}
