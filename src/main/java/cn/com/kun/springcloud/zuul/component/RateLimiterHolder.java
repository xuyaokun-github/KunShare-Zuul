package cn.com.kun.springcloud.zuul.component;

import cn.com.kun.springcloud.zuul.common.utils.JacksonUtils;
import cn.com.kun.springcloud.zuul.config.limit.ApiLimit;
import cn.com.kun.springcloud.zuul.config.limit.IPLimit;
import cn.com.kun.springcloud.zuul.config.limit.RateLimiterProperties;
import cn.com.kun.springcloud.zuul.config.limit.ServiceLimitProperties;
import cn.com.kun.springcloud.zuul.filter.pre.RateLimiterFilter;
import com.google.common.util.concurrent.RateLimiter;
import com.netflix.zuul.context.RequestContext;
import org.apache.commons.lang3.StringUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.cloud.netflix.zuul.filters.Route;
import org.springframework.cloud.netflix.zuul.filters.RouteLocator;
import org.springframework.stereotype.Component;
import org.springframework.util.AntPathMatcher;
import org.springframework.web.util.UrlPathHelper;

import javax.annotation.PostConstruct;
import javax.servlet.http.HttpServletRequest;
import java.util.*;
import java.util.concurrent.ConcurrentHashMap;

@Component
public class RateLimiterHolder {

    public final static Logger LOGGER = LoggerFactory.getLogger(RateLimiterHolder.class);

    @Autowired
    private RateLimiterProperties rateLimiterProperties;

    @Autowired
    private RouteLocator routeLocator;

    private final UrlPathHelper pathHelper = new UrlPathHelper();

    /**
     * 全局限流器-每秒产生100个令牌
     */
    private static final RateLimiter GLOBAL_RATE_LIMITER = RateLimiter.create(100);

    /**
     * 微服务级别--限流器配置
     */
    private Map<String, RateLimiter> serviceRateLimiterMap = new ConcurrentHashMap<>();

    /**
     * 接口级别--限流器配置
     * key-> serviceId+api
     */
    private Map<String, RateLimiter> apiRateLimiterMap = new ConcurrentHashMap<>();

    /**
     * IP级别--限流器配置
     */
    private Map<String, RateLimiter> ipRateLimiterMap = new ConcurrentHashMap<>();

    @PostConstruct
    public void init(){
        resloveRateLimiterMap();
    }

    private void resloveRateLimiterMap() {

        Map<String, RateLimiter> newServiceRateLimiterMap = new ConcurrentHashMap<>();
        Map<String, RateLimiter> newApiRateLimiterMap = new ConcurrentHashMap<>();
        Map<String, RateLimiter> newIpRateLimiterMap = new ConcurrentHashMap<>();

        //解析
        Map<String, ServiceLimitProperties> servicesLimit = rateLimiterProperties.getServicesLimit();
        Iterator iterator = servicesLimit.entrySet().iterator();
        while (iterator.hasNext()){
            Map.Entry entry = (Map.Entry) iterator.next();
            String key = (String) entry.getKey();//服务ID
            ServiceLimitProperties serviceLimitProperties = (ServiceLimitProperties) entry.getValue();
            newServiceRateLimiterMap.put(key, RateLimiter.create(serviceLimitProperties.getRate()));

            List<ApiLimit> apiLimitList = serviceLimitProperties.getApiLimit();
            apiLimitList.forEach(apiLimit -> {
                newApiRateLimiterMap.put("/" + key + apiLimit.getApi(), RateLimiter.create(apiLimit.getApiRate()));
            });
            List<IPLimit> ipLimitList = serviceLimitProperties.getIpLimit();
            ipLimitList.forEach(ipLimit -> {
                newIpRateLimiterMap.put("/" + key + ipLimit.getIp(), RateLimiter.create(ipLimit.getIpRate()));
            });
        }

        this.serviceRateLimiterMap = newServiceRateLimiterMap;
        this.apiRateLimiterMap = newApiRateLimiterMap;
        this.ipRateLimiterMap = newIpRateLimiterMap;
    }


    /**
     * 选择限流器
     * @param request
     * @return 限流器列表
     */
    public List<RateLimiter> chooseRateLimiter(HttpServletRequest request) {

        List<RateLimiter> rateLimiterList = new ArrayList<>();
        /*
        这个serviceId是在哪里放的？
        PreDecorationFilter过滤器里
         */
        //获取该请求对应的微服务名称
        String serviceId = getServiceId();
        //根据uri选限流配置器
        String requestUri = request.getRequestURI();
        String ip = request.getHeader("X-Real-IP");
        //遍历，然后比较是否匹配
        if (!serviceRateLimiterMap.containsKey(serviceId)){
            //假如没有限流配置，则用全局限流器
            return Arrays.asList(GLOBAL_RATE_LIMITER);
        }else {
            //先添加微服务级别的限流器
            rateLimiterList.add(serviceRateLimiterMap.get(serviceId));
            //再根据接口判断，是否需要添加接口级别的限流器
            //遍历所有apiRateLimiterMap的key
            Iterator iterator = apiRateLimiterMap.entrySet().iterator();
            while (iterator.hasNext()){
                Map.Entry entry = (Map.Entry) iterator.next();
                String key = (String) entry.getKey();
                if (requestUri.contains(key)){
                    //访问的请求地址和key匹配，说明存在限流器
                    //这里假如做到精确匹配，不需要遍历整个map的话，就支持不了模糊匹配的效果，用contains才支持模糊匹配的效果
                    rateLimiterList.add((RateLimiter) entry.getValue());
                }
            }
            //同理，判断IP是否存在 TODO

        }

        return rateLimiterList;
    }

    private String getServiceId() {

        RequestContext context = RequestContext.getCurrentContext();
        HttpServletRequest request = context.getRequest();
        String serviceId = (String) context.get("serviceId");
        if (StringUtils.isEmpty(serviceId)){
            //服务ID为空
            //解析Zuul路由对象
            Route matchingRoute = routeLocator.getMatchingRoute(pathHelper.getPathWithinApplication(request));
            /*
                service-id (用户配置的并不一定是eureka里的东西，可能这个路由对应的微服务并没有注册到注册中心)
                准确来说，应该是路由的ID
             */

            serviceId = matchingRoute.getId(); //当前路由服务名，即service-id
            String path = matchingRoute.getPath(); //摘除service-id后的请求路径
            LOGGER.info("current serviceId: {}, request path: {}", matchingRoute.getId(), matchingRoute.getPath());
        }
        return serviceId;
    }


    public void refresh() {
        Map<String, ServiceLimitProperties> serviceLimitPropertiesMap = rateLimiterProperties.getServicesLimit();
        //重新解析所有限流器map
        LOGGER.info("当前限流配置类内容：{}, serviceLimitPropertiesMap：{}", rateLimiterProperties.toString(),
                JacksonUtils.toJSONString(serviceLimitPropertiesMap));
        this.resloveRateLimiterMap();
    }
}
