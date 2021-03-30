package cn.com.kun.springcloud.zuul.filter;

import com.netflix.zuul.ZuulFilter;
import com.netflix.zuul.context.RequestContext;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
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
public class AccessRecordFilter extends ZuulFilter {

    public final static Logger logger = LoggerFactory.getLogger(AccessRecordFilter.class);

    @Override
    public String filterType() {
        return "post";
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
        //默认拿到的是空的，null
        String responseBody = ctx.getResponseBody();
        logger.info("AccessRecordFilter过滤器中拿到的responseBody：" + responseBody);
        InputStream responseInputStream = ctx.getResponseDataStream();
        logger.info("AccessRecordFilter过滤器中拿到的responseInputStream：" + responseInputStream);
        try {
            //这里拿到流转之后的body字符串，可以做业务记录，但可能很多，视业务决定
            String bodyString = StreamUtils.copyToString(responseInputStream, StandardCharsets.UTF_8);
            logger.info("responseInputStream处理后：{}", bodyString);

            if (request.getRequestURI().contains("zuul-demo")){
                //拼接一个内容，演示：可以通过代码修改返回给客户端的response内容
                bodyString = bodyString + "|二次处理后内容";
            }
            ctx.setResponseBody(bodyString);

        } catch (IOException e) {
            logger.error("处理responseInputStream异常", e);
        }
        logger.info("AccessRecordFilter end");

        return null;
    }

}
