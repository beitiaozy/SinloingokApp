package com.sinloingok.app.aop;

import org.aspectj.lang.JoinPoint;
import org.aspectj.lang.annotation.Aspect;
import org.aspectj.lang.annotation.Before;
import org.aspectj.lang.annotation.Pointcut;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Component;
import org.springframework.web.context.request.RequestContextHolder;
import org.springframework.web.context.request.ServletRequestAttributes;
import org.springframework.web.multipart.MultipartFile;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import java.util.Arrays;
import java.util.Map;

@Aspect
@Component
public class RequestLoggingAspect {

    private static final Logger logger = LoggerFactory.getLogger(RequestLoggingAspect.class);

    // 攔截所有控制器方法
    @Pointcut("execution(* com.sinloingok.app.controllers..*.*(..))")
    public void controllerMethods() {
    }

    // 攔截所有帶有@RequestMapping或其衍生註解的方法
    @Pointcut("@annotation(org.springframework.web.bind.annotation.RequestMapping) || " +
            "@annotation(org.springframework.web.bind.annotation.GetMapping) || " +
            "@annotation(org.springframework.web.bind.annotation.PostMapping) || " +
            "@annotation(org.springframework.web.bind.annotation.PutMapping) || " +
            "@annotation(org.springframework.web.bind.annotation.DeleteMapping) || " +
            "@annotation(org.springframework.web.bind.annotation.PatchMapping)")
    public void requestMappingMethods() {
    }

    @Before("controllerMethods() && requestMappingMethods()")
    public void logRequestParameters(JoinPoint joinPoint) {
        // 獲取請求對象
        ServletRequestAttributes attributes = (ServletRequestAttributes) RequestContextHolder.getRequestAttributes();
        if (attributes == null) {
            return;
        }

        HttpServletRequest request = attributes.getRequest();

        // 獲取方法名
        String methodName = joinPoint.getSignature().toShortString();

        // 獲取請求參數
        Map<String, String[]> parameterMap = request.getParameterMap();

        // 獲取請求體參數 (通常用於POST/PUT請求)
        Object[] args = joinPoint.getArgs();

        // 記錄請求信息
        logger.debug("======= 請求開始 =======");
        logger.debug("請求URL: {} {}", request.getMethod(), request.getRequestURL());
        logger.debug("請求方法: {}", methodName);

        // 輸出請求參數
        if (!parameterMap.isEmpty()) {
            logger.debug("請求參數:");
            parameterMap.forEach((key, values) -> {
                if (values.length == 1) {
                    logger.debug("  {}: {}", key, values[0]);
                } else {
                    logger.debug("  {}: {}", key, Arrays.toString(values));
                }
            });
        }

        // 輸出請求體參數
        if (args != null && args.length > 0) {
            logger.debug("請求體參數:");
            for (Object arg : args) {
                // 過濾掉HttpServletRequest/HttpServletResponse等對象
                if (!(arg instanceof HttpServletRequest) &&
                        !(arg instanceof HttpServletResponse) &&
                        !(arg instanceof MultipartFile)) {
                    logger.info(" {} {}: {}", methodName, arg.getClass().getSimpleName(), arg);
                }
            }
        }

        logger.debug("======= 請求結束 =======");
    }
}