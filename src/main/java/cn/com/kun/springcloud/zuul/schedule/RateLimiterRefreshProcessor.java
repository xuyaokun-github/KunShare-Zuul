package cn.com.kun.springcloud.zuul.schedule;

import cn.com.kun.springcloud.zuul.component.ratelimit.RateLimiterHolder;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.scheduling.annotation.EnableScheduling;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;

@Component
@EnableScheduling
public class RateLimiterRefreshProcessor {

    public final static Logger LOGGER = LoggerFactory.getLogger(RateLimiterRefreshProcessor.class);

    @Autowired
    RateLimiterHolder rateLimiterHolder;

    @Scheduled(fixedDelay = 30000L)
    public void refresh(){
        LOGGER.info("准备刷新限流器配置");
        rateLimiterHolder.refresh();
    }

}
