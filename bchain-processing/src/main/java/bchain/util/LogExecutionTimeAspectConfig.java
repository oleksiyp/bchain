package bchain.util;

import org.springframework.context.annotation.Bean;
import org.springframework.stereotype.Component;

@Component
public class LogExecutionTimeAspectConfig {
    @Bean
    public LogExcutionTimeAspect logExcutionTimeAspect() {
        return new LogExcutionTimeAspect();
    }
}
