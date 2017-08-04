package bchain.util;

import lombok.extern.slf4j.Slf4j;
import org.aspectj.lang.ProceedingJoinPoint;
import org.aspectj.lang.annotation.Around;
import org.aspectj.lang.annotation.Aspect;
import org.springframework.stereotype.Component;
import org.springframework.util.StopWatch;

@Component
@Aspect
@Slf4j
public class LogExcutionTimeAspect {
    @Around("@annotation(bchain.util.LogExecutionTime) || execution(* org.springframework.jdbc.core.JdbcTemplate.*(..)) || execution(* org.springframework.data.redis.core.RedisTemplate.*(..))")
    public Object logTimeMethod(ProceedingJoinPoint joinPoint) throws Throwable {

        StopWatch stopWatch = new StopWatch();
        stopWatch.start();

        Object retVal = joinPoint.proceed();

        stopWatch.stop();

        double t = stopWatch.getTotalTimeMillis() / 1000.0;

        if (t > 0.01) {
            StringBuilder msg = new StringBuilder();

            msg.append(String.format("%8.3f ms ", t));

            msg.append(joinPoint.getTarget().getClass().getName());
            msg.append(".");
            msg.append(joinPoint.getSignature().getName());
            msg.append("(");

            // append args
            Object[] args = joinPoint.getArgs();
            for (int i = 0; i < args.length; i++) {
                msg.append(args[i]).append(",");
            }

            if (args.length > 0) {
                msg.deleteCharAt(msg.length() - 1);
            }

            msg.append(")");


            log.info(msg.toString());
        }

        return retVal;
    }

}
