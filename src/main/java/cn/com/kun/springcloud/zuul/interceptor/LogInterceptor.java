package cn.com.kun.springcloud.zuul.interceptor;

import cn.com.kun.springcloud.zuul.filter.post.AccessRecordFilter;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.web.servlet.ModelAndView;
import org.springframework.web.servlet.handler.HandlerInterceptorAdapter;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

/**
 * 耗时记录拦截器
 * HandlerInterceptorAdapter是spring的类，不是zuul特有的
 *
 * author:xuyaokun_kzx
 * date:2021/6/15
 * desc:
*/
public class LogInterceptor extends HandlerInterceptorAdapter {

    public final static Logger LOGGER = LoggerFactory.getLogger(LogInterceptor.class);

    private ThreadLocal<Long> startTime = new ThreadLocal<>();

    public LogInterceptor(){
        //构造器可以省略，假如没其他要传进来的参数，可以省略
    }

    @Override
    public boolean preHandle(HttpServletRequest request, HttpServletResponse response, Object handler) throws Exception {
        LOGGER.info("KunShareZuul Gateway接受到请求 {}", request.getRequestURI());
        startTime.set(System.currentTimeMillis());//记录当前时间
        return true;
    }

    @Override
    public void postHandle(HttpServletRequest request, HttpServletResponse response, Object handler, ModelAndView modelAndView) throws Exception {
        LOGGER.info("KunShareZuul Gateway处理请求耗时：{}ms", System.currentTimeMillis() - startTime.get());
    }

    @Override
    public void afterCompletion(HttpServletRequest request, HttpServletResponse response, Object handler, Exception ex) throws Exception {
        this.startTime.remove();
    }
}
