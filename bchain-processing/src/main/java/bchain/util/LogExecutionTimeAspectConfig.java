package bchain.util;

import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.EnableAspectJAutoProxy;
import org.springframework.stereotype.Component;

@Component
@EnableAspectJAutoProxy(proxyTargetClass = true)
public class LogExecutionTimeAspectConfig {
    @Bean
    public LogExcutionTimeAspect logExcutionTimeAspect() {
        return new LogExcutionTimeAspect();
    }
}
