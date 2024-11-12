package com.mattelogic.inchfab.core.config;

import java.util.concurrent.Executor;
import java.util.concurrent.ThreadPoolExecutor;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.scheduling.annotation.EnableAsync;
import org.springframework.scheduling.concurrent.ThreadPoolTaskExecutor;
import org.springframework.web.context.request.RequestAttributes;
import org.springframework.web.context.request.RequestContextHolder;

@Configuration
@EnableAsync
public class AsyncConfiguration {

  @Bean(name = "processTaskExecutor")
  public Executor processTaskExecutor() {
    ThreadPoolTaskExecutor executor = new ThreadPoolTaskExecutor();
    executor.setCorePoolSize(10);
    executor.setMaxPoolSize(20);
    executor.setQueueCapacity(500);
    executor.setThreadNamePrefix("ProcessAsync-");
    executor.setRejectedExecutionHandler(new ThreadPoolExecutor.CallerRunsPolicy());

    // Updated task decorator to handle non-web requests
    executor.setTaskDecorator(task -> () -> {
      RequestAttributes context = null;
      context = RequestContextHolder.getRequestAttributes();

      if (context != null) {
        try {
          RequestContextHolder.setRequestAttributes(context, true);
          task.run();
        } finally {
          RequestContextHolder.resetRequestAttributes();
        }
      } else {
        task.run();
      }
    });

    executor.initialize();
    return executor;
  }
}