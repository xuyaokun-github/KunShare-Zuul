package cn.com.kun.springcloud.zuul.config;

import cn.com.kun.springcloud.zuul.interceptor.LogInterceptor;
import org.springframework.beans.BeansException;
import org.springframework.beans.factory.config.InstantiationAwareBeanPostProcessorAdapter;
import org.springframework.cloud.netflix.zuul.web.ZuulHandlerMapping;
import org.springframework.context.annotation.Configuration;

/**
 * 拦截器定义--将拦截器注册到zuul中
 * InstantiationAwareBeanPostProcessorAdapter是spring的类
 *
 * author:xuyaokun_kzx
 * date:2021/6/15
 * desc:
*/
@Configuration
public class InterceptorConfig extends InstantiationAwareBeanPostProcessorAdapter {

    @Override
    public boolean postProcessAfterInstantiation(Object bean, String beanName) throws BeansException {
        if (bean instanceof ZuulHandlerMapping){
            ZuulHandlerMapping zuulHandlerMapping = (ZuulHandlerMapping)bean;
            //设置拦截器组
            zuulHandlerMapping.setInterceptors(new Object[]{new LogInterceptor()});
        }
        return super.postProcessAfterInstantiation(bean, beanName);
    }
}
