package com.sinloingok.app.aop;

import com.sinloingok.app.constant.SinloingokUserStatus;
import com.sinloingok.app.constant.SysConstant;
import com.sinloingok.app.dtos.StandardRtnDto;
import com.sinloingok.app.kits.MD5;
import com.sinloingok.app.models.user.ManageUser;
import com.sinloingok.app.models.user.User;
import com.sinloingok.app.service.user.ManageUserService;
import com.sinloingok.app.service.user.UserService;
import lombok.extern.slf4j.Slf4j;
import org.aspectj.lang.ProceedingJoinPoint;
import org.aspectj.lang.annotation.Around;
import org.aspectj.lang.annotation.Aspect;
import org.aspectj.lang.reflect.MethodSignature;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;
import org.springframework.web.context.request.RequestContextHolder;
import org.springframework.web.context.request.ServletRequestAttributes;

import javax.servlet.http.HttpServletRequest;

@Aspect
@Slf4j
@Component
public class AuthAspect {

    @Autowired
    private UserService authService;
    @Autowired
    private ManageUserService manageUserService;

    @Around("@annotation(authCheck)")
    public Object checkAuth(ProceedingJoinPoint joinPoint, AuthCheck authCheck) throws Throwable {
        HttpServletRequest request = ((ServletRequestAttributes) RequestContextHolder.getRequestAttributes()).getRequest();
        boolean authCheckPass;
        switch (authCheck.value()) {
            case ANONYMOUS:
                authCheckPass = true; // 匿名访问
                break;
            case USER:
                authCheckPass = checkUserLogin(request);
                break;
            case MANAGER:
                authCheckPass = checkManagerLogin(request);
                if(authCheckPass){
                    ManageUser user = UserContext.getMUser();
                    if (SinloingokUserStatus.ManageUserStatus.NO_USING.equals(user.getStatus())) {
                        return handleFail(joinPoint, "账号异常！");
                    }
                }
                break;
            case AGENT:
                authCheckPass = checkAgentLogin(request);
                break;
            default:
                throw new IllegalStateException("未知的登录类型: " + authCheck.value());
        }
        if (authCheckPass) {
            try {
                return joinPoint.proceed();
            } finally {
                UserContext.clear();
            }
        }else {
            return handleFail(joinPoint, "操作需要用戶需要登录！");
        }
    }

    private boolean checkUserLogin(HttpServletRequest request) throws Exception {
        String token = request.getHeader("lt-token");
        String id = request.getHeader("lt-id");
        String addressIdStr = request.getHeader("address-id");
        long addressId = addressIdStr == null ?  SysConstant.DEFAULT_ADDRESS : Long.valueOf(addressIdStr);
        id = id == null ? "-1" : id;
        User user = authService.findById(Long.valueOf(id), Long.valueOf(addressId));

        if (user == null) return false;

        String text = SysConstant.COMMON_CODE + user.getId() + user.getRandomCode();
        if (MD5.md5(text, "UTF-8").equals(token)) {
            UserContext.setUser(user);
            return true;
        }
        return false;
    }

    private boolean checkManagerLogin(HttpServletRequest request) {
        String token = request.getHeader("agent-token");
        String id = request.getHeader("agent-id");
        id = id == null ? "-1" : id;
        ManageUser user = manageUserService.findById(Long.valueOf(id));
        if (user == null) {
            return false;
        }
        try {
            String text = SysConstant.COMMON_CODE + user.getId() + user.getRandomCode();
            if (MD5.md5(text, "UTF-8").equals(token)) {
                UserContext.setMUser(user);
                return true;
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
        return false;
    }

    private boolean checkAgentLogin(HttpServletRequest request) {
        return false;
    }


    private Object handleFail(ProceedingJoinPoint joinPoint, String msg) {
        MethodSignature sig = (MethodSignature) joinPoint.getSignature();
        Class<?> returnType = sig.getReturnType();
        if (StandardRtnDto.class.isAssignableFrom(returnType)) {
            return new StandardRtnDto<>(SysConstant.ResultCode.ERROR, msg);
        }
        return new StandardRtnDto<>(SysConstant.ResultCode.ERROR, msg);
    }
}