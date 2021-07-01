package cn.com.kun.springcloud.zuul.config.limit;

import cn.com.kun.springcloud.zuul.component.ratelimit.RateLimitConfigValidation;
import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.cloud.context.config.annotation.RefreshScope;
import org.springframework.stereotype.Component;
import org.springframework.validation.annotation.Validated;

import java.io.Serializable;
import java.util.Map;

/**
 * 限流的配置
 *
 * TODO 该类还可以配合@RefreshScope注解做限流配置的刷新
 * 访问/refresh接口，会触发该bean的刷新，然后定时启动一个线程不停地刷新重新构建限流器
 * 即可实现动态限流！
 *
 * author:xuyaokun_kzx
 * date:2021/6/30
 * desc:
*/
@RefreshScope
@Component
@ConfigurationProperties(prefix = "limit")
@RateLimitConfigValidation
@Validated //必须加这个注解，自定义的@RateLimitConfigValidation才会生效
public class RateLimiterProperties implements Serializable {

    private boolean enabled;

    /**
     * 不需要限流的url集合
     */
    private String noLimitUrls;

    /**
     * 定义每个微服务的限流
     */
    private Map<String, ServiceLimitProperties> servicesLimit;

    public boolean isEnabled() {
        return enabled;
    }

    public void setEnabled(boolean enabled) {
        this.enabled = enabled;
    }

    public String getNoLimitUrls() {
        return noLimitUrls;
    }

    public void setNoLimitUrls(String noLimitUrls) {
        this.noLimitUrls = noLimitUrls;
    }

    public Map<String, ServiceLimitProperties> getServicesLimit() {
        return servicesLimit;
    }

    public void setServicesLimit(Map<String, ServiceLimitProperties> servicesLimit) {
        this.servicesLimit = servicesLimit;
    }

    @Override
    public String toString() {
        return "RateLimiterProperties{" +
                "enabled=" + enabled +
                ", noLimitUrls='" + noLimitUrls + '\'' +
                ", servicesLimit=" + servicesLimit +
                '}';
    }
}
