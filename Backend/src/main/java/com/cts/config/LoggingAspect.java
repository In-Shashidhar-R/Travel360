package com.cts.config;

import org.aspectj.lang.ProceedingJoinPoint;
import org.aspectj.lang.annotation.Around;
import org.aspectj.lang.annotation.Aspect;
import org.aspectj.lang.annotation.Pointcut;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Component;

@Aspect
@Component
public class LoggingAspect {

    @Pointcut("execution(public * com.cts.serviceimpl..*(..))")
    public void serviceLayer() { }

    @Pointcut("execution(public * com.cts.controller..*(..))")
    public void controllerLayer() { }

    @Around("serviceLayer() || controllerLayer()")
    public Object logAround(ProceedingJoinPoint joinPoint) throws Throwable {
        Logger log = LoggerFactory.getLogger(joinPoint.getTarget().getClass());
        String method = joinPoint.getSignature().toShortString();

        if (log.isDebugEnabled()) {
            log.debug("→ {} args={}", method, abbreviate(joinPoint.getArgs()));
        }

        long startedAt = System.currentTimeMillis();
        try {
            Object result = joinPoint.proceed();
            long elapsed = System.currentTimeMillis() - startedAt;
            log.info("✓ {} completed in {} ms", method, elapsed);
            return result;
        } catch (Throwable ex) {
            long elapsed = System.currentTimeMillis() - startedAt;
            log.error("✗ {} failed after {} ms — {}: {}",
                    method, elapsed, ex.getClass().getSimpleName(), ex.getMessage());
            throw ex;
        }
    }

    private String abbreviate(Object[] args) {
        if (args == null || args.length == 0) return "()";
        StringBuilder sb = new StringBuilder("(");
        for (int i = 0; i < args.length; i++) {
            if (i > 0) sb.append(", ");
            Object a = args[i];
            String s = (a == null) ? "null" : String.valueOf(a);
            if (s.length() > 80) s = s.substring(0, 80) + "...";
            sb.append(s);
        }
        sb.append(")");
        return sb.length() > 200 ? sb.substring(0, 200) + "...)" : sb.toString();
    }
}
