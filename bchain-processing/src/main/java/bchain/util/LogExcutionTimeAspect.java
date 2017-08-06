package bchain.util;

import lombok.extern.slf4j.Slf4j;
import org.aspectj.lang.ProceedingJoinPoint;
import org.aspectj.lang.annotation.Around;
import org.aspectj.lang.annotation.Aspect;
import org.springframework.util.StopWatch;

@Aspect
@Slf4j
public class LogExcutionTimeAspect {
    @Around("@annotation(bchain.util.LogExecutionTime)")
    public Object logTimeMethod(ProceedingJoinPoint joinPoint) throws Throwable {

        StopWatch stopWatch = new StopWatch();
        stopWatch.start();

        Object retVal = joinPoint.proceed();

        stopWatch.stop();

        double t = stopWatch.getTotalTimeMillis() / 1000.0;

        if (t > 0.1) {
            StringBuilder msg = new StringBuilder();

            msg.append(String.format("%8.3f s ", t));

            msg.append(joinPoint.getTarget()
                    .getClass()
                    .getName()
                    .replaceAll("bchain\\.(app|util)\\.", ""));
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
