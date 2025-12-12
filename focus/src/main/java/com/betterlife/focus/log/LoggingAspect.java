package com.betterlife.focus.log;

import lombok.extern.slf4j.Slf4j;
import org.aspectj.lang.ProceedingJoinPoint;
import org.aspectj.lang.annotation.Around;
import org.aspectj.lang.annotation.Aspect;
import org.aspectj.lang.annotation.Pointcut;
import org.aspectj.lang.reflect.MethodSignature;
import org.springframework.stereotype.Component;

import java.lang.reflect.Method;

@Aspect
@Component
@Slf4j
public class LoggingAspect {

    @Pointcut("execution(* com.betterlife.focus.controller..*.*(..))")
    private void cut(){}

    @Around("cut()")
    public Object aroundLog(ProceedingJoinPoint proceedingJoinPoint) throws Throwable {

        // 메서드 정보 받아오기
        Method method = getMethod(proceedingJoinPoint);
        log.info("======= method name = {} =======", method.getName());

        // 파라미터 받아오기
        Object[] args = proceedingJoinPoint.getArgs();
        if (args.length == 0) log.info("no parameter");
        for (Object arg : args) {
            log.info("parameter type = {}", arg.getClass().getSimpleName());
            log.info("parameter value = {}", arg);
        }

        try {
            Object returnObj = proceedingJoinPoint.proceed();
            log.info("return type = {}", returnObj.getClass().getSimpleName());
            log.info("return value = {}", returnObj);
            return returnObj;
        } catch (Exception e) {
            log.error("Exception occurred: {}", e.getMessage());
            throw e;
        }
    }


    private Method getMethod(ProceedingJoinPoint proceedingJoinPoint) {
        MethodSignature signature = (MethodSignature) proceedingJoinPoint.getSignature();
        return signature.getMethod();
    }

}
