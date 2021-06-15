package cn.com.kun.springcloud.zuul.filter.pre;

import com.netflix.zuul.ZuulFilter;
import com.netflix.zuul.context.RequestContext;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.cloud.netflix.zuul.filters.support.FilterConstants;
import org.springframework.stereotype.Component;
import org.springframework.util.StreamUtils;

import javax.servlet.http.HttpServletRequest;
import java.io.IOException;
import java.io.InputStream;
import java.nio.charset.StandardCharsets;

/**
 * 过滤器--记录网关返回的内容（后续对接安全监控数据上送）
 * Created by xuyaokun On 2021/1/7 23:02
 * @desc:
 */
@Component
public class SessionInfoFilter extends ZuulFilter {

    public final static Logger LOGGER = LoggerFactory.getLogger(SessionInfoFilter.class);

    @Override
    public String filterType() {
        return FilterConstants.PRE_TYPE;
    }

    @Override
    public int filterOrder() {
        //优先级必须小于SendResponseFilter的1000，这样才能确保response对象是未被关闭的
        return 999;
    }

    @Override
    public boolean shouldFilter() {
        //是否要被过滤，返回true,说明下面的run方法后续将会被执行
        return true;
    }

    @Override
    public Object run() {

        //RequestContext是一个ConcurrentHashMap对象
        RequestContext ctx = RequestContext.getCurrentContext();
        HttpServletRequest request = ctx.getRequest();
        LOGGER.info("SessionInfoFilter处理的url:{}", request.getRequestURI());
        /**
         * 常见的一种实践，是在网关工程中用过滤器实现登录校验，假如不通过则跳转到登录页
         * 后端应用不需要添加登录拦截的过滤器，对接sso的工作由网关完成
         * 后端如何获取到用户的会话信息？
         * 通过网关透传，网关可以将需要的信息加密后塞到header头里
         *
         */

        /**
         * 例子-通过header头传递信息
         */
        ctx.addZuulRequestHeader("header-name", "");
        return null;
    }

}
