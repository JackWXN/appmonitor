package com.app.monitor.common;

import lombok.extern.slf4j.Slf4j;
import org.aspectj.lang.JoinPoint;
import org.aspectj.lang.ProceedingJoinPoint;
import org.aspectj.lang.annotation.*;
import org.springframework.stereotype.Component;
import org.springframework.web.context.request.RequestContextHolder;
import org.springframework.web.context.request.ServletRequestAttributes;
import org.springframework.web.servlet.ModelAndView;

import javax.servlet.http.HttpServletRequest;

/**
 * aop
 *
 * @author wxn
 * @date 2020/7/17
 */
@Component
@Aspect
@Slf4j
public class RequestAOP {
    private static long startTime;
    private static long endTime;

    private ExpiredCache expiredCache = new ExpiredCache();

    @Pointcut("execution(public * com.app.monitor.controller.MonitorController.*(..))")
    public void all() {
    }

    @Pointcut("all()")
    public void log() {
    }

    @Before("log()")
    public void doBefore(JoinPoint joinPoint) {
        log.info("方法执行前...");
        startTime = System.currentTimeMillis();
        ServletRequestAttributes sra = (ServletRequestAttributes) RequestContextHolder.getRequestAttributes();
        HttpServletRequest request = sra.getRequest();
        log.info("url:" + request.getRequestURI());
        log.info("ip:" + request.getRemoteHost());
        log.info("method:" + request.getMethod());
        log.info("class_method:" + joinPoint.getSignature().getDeclaringTypeName() + "."
                + joinPoint.getSignature().getName());
        log.info("args:" + joinPoint.getArgs());

    }

    @AfterReturning(returning = "result", pointcut = "log()")
    public void doAfterReturning(Object result) {
        log.info("本次接口耗时={}ms", endTime);
        if(result.getClass().equals(ModelAndView.class)) {
            ServletRequestAttributes sra = (ServletRequestAttributes) RequestContextHolder.getRequestAttributes();
            HttpServletRequest request = sra.getRequest();
            String requestURI = request.getRequestURI();

            // 获取接口名
            String cacheKey = requestURI.substring(requestURI.lastIndexOf("/") + 1);

            // 加入到缓存，并设置过期时间为1s
            long qps = expiredCache.set(cacheKey, 1).get();

            ModelAndView modelAndView = (ModelAndView) result;
            modelAndView.addObject("time", endTime + "ms");
            modelAndView.addObject("qps", qps);
            result = modelAndView;
        }
        log.info("执行返回值：" + result);
    }

    @AfterThrowing("log()")
    public void throwsError(JoinPoint joinPoint){
        log.info("方法异常时执行.....");
    }

    @After("log()")
    public void doAfter(JoinPoint joinPoint) {
        endTime = System.currentTimeMillis() - startTime;
        log.info("方法执行后...");
    }

    @Around("log()")
    public Object trackInfo(ProceedingJoinPoint pjp) throws Throwable {

        ServletRequestAttributes sra = (ServletRequestAttributes) RequestContextHolder.getRequestAttributes();
        HttpServletRequest request = sra.getRequest();
        return pjp.proceed();
    }
}
