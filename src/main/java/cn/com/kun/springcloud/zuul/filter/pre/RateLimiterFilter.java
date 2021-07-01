package cn.com.kun.springcloud.zuul.filter.pre;

import cn.com.kun.springcloud.zuul.common.utils.JacksonUtils;
import cn.com.kun.springcloud.zuul.component.ratelimit.RateLimiterHolder;
import cn.com.kun.springcloud.zuul.config.limit.RateLimiterProperties;
import com.google.common.util.concurrent.RateLimiter;
import com.netflix.zuul.ZuulFilter;
import com.netflix.zuul.context.RequestContext;
import com.netflix.zuul.exception.ZuulException;
import org.apache.commons.lang3.StringUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Component;
import org.springframework.util.AntPathMatcher;

import javax.annotation.PostConstruct;
import javax.servlet.http.HttpServletRequest;
import java.util.*;

import static org.springframework.cloud.netflix.zuul.filters.support.FilterConstants.PRE_TYPE;

/**
 * author:xuyaokun_kzx
 * date:2021/6/30
 * desc:
*/
@Component
public class RateLimiterFilter extends ZuulFilter {

    public final static Logger LOGGER = LoggerFactory.getLogger(RateLimiterFilter.class);

    /**
     * 不需要限流的接口
     */
    private List<String> noLimitUrlList = new ArrayList<>();

    @Autowired
    private RateLimiterProperties rateLimiterProperties;

    @Autowired
    private RateLimiterHolder rateLimiterHolder;

    @PostConstruct
    public void init(){

        String noLimitUrls = rateLimiterProperties.getNoLimitUrls();
        if (StringUtils.isNotEmpty(noLimitUrls)){
            String[] arr = noLimitUrls.split(",");
            noLimitUrlList = Arrays.asList(arr);
        }

        //限流配置的解析，构造成限流器，放入map

    }

    @Override
    public String filterType() {
        //前置类型过滤器
        return PRE_TYPE;
    }

    @Override
    public int filterOrder() {

        /**
         * 设置为6的原因是为了让它在PreDecorationFilter之后再执行
         * PreDecorationFilter的优先级是5
         */
        return 6;
    }

    @Override
    public boolean shouldFilter() {

        if (!rateLimiterProperties.isEnabled()){
            //设置为false,表示限流功能关闭
            return false;
        }

        RequestContext context = RequestContext.getCurrentContext();
        HttpServletRequest request = context.getRequest();

        //request.getRequestURI()： /kun-api-gateway/order/api/v1/order/test
        //request.getRequestURL()：http://127.0.0.1:9000/apigateway/order/api/v1/order/test

        //不需要做限流的接口，直接返回false
        AntPathMatcher matcher = new AntPathMatcher();
        for (String pattern : noLimitUrlList) {//pattern--/user/**
            if (StringUtils.isEmpty(pattern)){
                continue;
            }
            /*
                注意，这里拿到的uri是包含网关上下文的，所以在配置文件里要注意写法
                pattern:/kunsharedemo/zuul-demo/test1 和 /kunshare-zuul/kunsharedemo/kunsharedemo/zuul-demo/test1 匹配结果为false
                pattern:/两个星/kunsharedemo/zuul-demo/test1 和 /kunshare-zuul/kunsharedemo/kunsharedemo/zuul-demo/test1 匹配结果为false
             */
            String requestUri = request.getRequestURI();
            if (matcher.match(pattern, requestUri)) {
                LOGGER.info("url：[{}]无需限流", request.getRequestURI());
                return false;
            }
        }

        //默认情况下，所有接口都要限流，限流100
        return true;
    }

    @Override
    public Object run() throws ZuulException {

        RequestContext context = RequestContext.getCurrentContext();
        HttpServletRequest request = context.getRequest();

        //根据请求选择限流器
        List<RateLimiter> rateLimiterList = rateLimiterHolder.chooseRateLimiter(request);
        if (rateLimiterList != null && !rateLimiterList.isEmpty()){
            if (!tryAcquire(rateLimiterList)) {
                Map<String, Object> result = new HashMap<>();
                //限流返回码，这也是业界常用的表示访问量过大的返回码
                result.put("code", 429);
                result.put("msg", "目前访问量过大,触发限流...");
                String responseBody = JacksonUtils.toJSONString(result);

                //设置无需转发到后端app
                context.setSendZuulResponse(false);

                /**
                 * 这里有个问题需要注意：返回429会导致restTemplate框架抛异常
                 * 可以视业务约定，是否要返回200
                 */
//            context.setResponseStatusCode(HttpStatus.TOO_MANY_REQUESTS.value());
                context.setResponseStatusCode(HttpStatus.OK.value());
                context.setResponseBody(responseBody);
                //解决中文乱码
                context.getResponse().setCharacterEncoding("UTF-8");
                //返回text/html类型，restTemplate框架也可以拿到值，因为它也有对应的消息转换器，但建议统一返回json
//            context.getResponse().setContentType("text/html;charset=UTF-8");
                context.getResponse().setContentType("application/json;charset=UTF-8");
                LOGGER.info("触发限流，request url:{} 返回内容：{}", request.getRequestURI(), responseBody);
            }else {
                //获取令牌，请求放行

            }
        }

        return null;
    }

    private boolean tryAcquire(List<RateLimiter> rateLimiterList) {

        boolean tryAcquire = true;
        /**
         * 按顺序判断，先判断全局微服务级别，然后再判断接口级别
         */
        for (RateLimiter rateLimiter : rateLimiterList){
            if (!rateLimiter.tryAcquire()){
                //假如有一个触发了限流，就返回false,表示没获取到令牌
                return false;
            }
        }
        //全部遍历完，都能获取到令牌，说明没发生限流
        return tryAcquire;
    }

}
