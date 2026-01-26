package com.betterlife.auth.log;

import com.fasterxml.jackson.databind.ObjectMapper;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.aspectj.lang.ProceedingJoinPoint;
import org.aspectj.lang.annotation.Around;
import org.aspectj.lang.annotation.Aspect;
import org.aspectj.lang.annotation.Pointcut;
import org.aspectj.lang.reflect.MethodSignature;
import org.springframework.stereotype.Component;
import org.springframework.web.context.request.RequestContextHolder;
import org.springframework.web.context.request.ServletRequestAttributes;

import java.lang.reflect.Method;
import java.util.LinkedHashMap;
import java.util.Map;

@Aspect
@Component
@Slf4j
@RequiredArgsConstructor
public class LoggingAspect {

    private final ObjectMapper objectMapper;

    @Pointcut("execution(* com.betterlife.auth.controller..*.*(..))")
    private void cut(){}

    @Around("cut()")
    public Object aroundLog(ProceedingJoinPoint proceedingJoinPoint) throws Throwable {
        Method method = getMethod(proceedingJoinPoint);

        // request/response 가져오기
        ServletRequestAttributes attrs = (ServletRequestAttributes) RequestContextHolder.getRequestAttributes();
        HttpServletRequest request = (attrs != null) ? attrs.getRequest() : null;
        HttpServletResponse response = (attrs != null) ? attrs.getResponse() : null;

        // 요청 로그 JSON으로
        Map<String, Object> reqLog = new LinkedHashMap<>();
        reqLog.put("type", "http_request");
        reqLog.put("httpMethod", request != null ? request.getMethod() : null);
        reqLog.put("path", request != null ? request.getRequestURI() : null);
        reqLog.put("controllerMethod", method.getDeclaringClass().getSimpleName() + "." + method.getName());

        Object[] args = proceedingJoinPoint.getArgs();
        reqLog.put("paramCount", args.length);
        for (int i = 0; i < args.length; i++) {
            Object arg = args[i];
            reqLog.put("paramType[" + i + "]", arg != null ? arg.getClass().getSimpleName() : "null");
        }

        log.info(objectMapper.writeValueAsString(reqLog));

        long start = System.currentTimeMillis();
        try {
            Object returnObj = proceedingJoinPoint.proceed();
            long elapsed = System.currentTimeMillis() - start;
            // === 응답 로그(JSON) ===
            Map<String, Object> resLog = new LinkedHashMap<>();
            resLog.put("type", "http_response");
            resLog.put("httpMethod", request != null ? request.getMethod() : null);
            resLog.put("path", request != null ? request.getRequestURI() : null);
            resLog.put("controllerMethod", method.getDeclaringClass().getSimpleName() + "." + method.getName());
            resLog.put("time", elapsed + "ms");
            resLog.put("status", response != null ? response.getStatus() : null);
            resLog.put("returnType", returnObj != null ? returnObj.getClass().getSimpleName() : "null");

            log.info(objectMapper.writeValueAsString(resLog));

            return returnObj;
        } catch (Exception e) {
            // === 에러 로그(JSON) ===
            long elapsed = System.currentTimeMillis() - start;
            Map<String, Object> errLog = new LinkedHashMap<>();
            errLog.put("type", "http_error");
            errLog.put("httpMethod", request != null ? request.getMethod() : null);
            errLog.put("path", request != null ? request.getRequestURI() : null);
            errLog.put("controllerMethod", method.getDeclaringClass().getSimpleName() + "." + method.getName());
            errLog.put("time", elapsed + "ms");
            errLog.put("errorType", e.getClass().getSimpleName());
            errLog.put("message", e.getMessage());
            log.error(objectMapper.writeValueAsString(errLog));
            throw e;
        }
    }

    private Method getMethod(ProceedingJoinPoint proceedingJoinPoint) {
        MethodSignature signature = (MethodSignature) proceedingJoinPoint.getSignature();
        return signature.getMethod();
    }
}
