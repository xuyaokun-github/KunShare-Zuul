package cn.com.kun.springcloud.zuul.component.ratelimit;

import cn.com.kun.springcloud.zuul.config.limit.ApiLimit;
import cn.com.kun.springcloud.zuul.config.limit.IPLimit;
import cn.com.kun.springcloud.zuul.config.limit.RateLimiterProperties;
import cn.com.kun.springcloud.zuul.config.limit.ServiceLimitProperties;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import javax.validation.ConstraintValidator;
import javax.validation.ConstraintValidatorContext;
import java.util.Collections;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.stream.Collector;
import java.util.stream.Collectors;

public class RateLimitConfigValidator implements ConstraintValidator<RateLimitConfigValidation, RateLimiterProperties> {

    public final static Logger LOGGER = LoggerFactory.getLogger(RateLimitConfigValidator.class);

    @Override
    public void initialize(RateLimitConfigValidation constraintAnnotation) {

    }

    @Override
    public boolean isValid(RateLimiterProperties rateLimiterProperties, ConstraintValidatorContext constraintValidatorContext) {

        if (!rateLimiterProperties.isEnabled()){
            //无需验证
            LOGGER.info("限流功能未开启，无需验证");
            return true;
        }

        //
        Map<String, ServiceLimitProperties>  serviceLimitPropertiesMap = rateLimiterProperties.getServicesLimit();
        Iterator iterator = serviceLimitPropertiesMap.entrySet().iterator();
        while (iterator.hasNext()){
            Map.Entry entry = (Map.Entry) iterator.next();
            String serviceId = (String) entry.getKey();
            ServiceLimitProperties serviceLimitProperties = (ServiceLimitProperties) entry.getValue();

            if (serviceLimitProperties.getRate() == null){
                LOGGER.error("限流配置验证失败，服务[]的微服务级别限流值rate有误，请检查", serviceId);
                return false;
            }

            List<ApiLimit> apiLimitList = serviceLimitProperties.getApiLimit();
            Double apiRateSum = apiLimitList.stream().collect(Collectors.summingDouble((apiLimit)->{
                    return apiLimit.getApiRate();
            }));
            if (apiRateSum.compareTo(serviceLimitProperties.getRate()) > 0){
                LOGGER.error("限流配置验证失败，服务[{}]的api级别限流值apiRate总和大于微服务级别rate值，请检查", serviceId);
                return false;
            }
            List<IPLimit> ipLimitList = serviceLimitProperties.getIpLimit();
            Double ipRateSum = ipLimitList.stream().collect(Collectors.summingDouble((ipLimit)->{
                return ipLimit.getIpRate();
            }));
            if (ipRateSum.compareTo(serviceLimitProperties.getRate()) > 0){
                LOGGER.error("限流配置验证失败，服务[{}]的IP级别限流值ipRate总和大于微服务级别rate值，请检查", serviceId);
                return false;
            }
        }
        LOGGER.info("限流配置验证成功！！");
        return true;
    }


}
