package cn.com.kun.springcloud.zuul.config;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.cloud.client.discovery.DiscoveryClient;
import org.springframework.cloud.netflix.zuul.ZuulProxyAutoConfiguration;
import org.springframework.cloud.netflix.zuul.filters.discovery.DiscoveryClientRouteLocator;
import org.springframework.cloud.netflix.zuul.filters.discovery.ServiceRouteMapper;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

/**
 * TODO 动态路由
 * author:xuyaokun_kzx
 * date:2021/6/30
 * desc:
*/
//@Configuration
public class ZuulRouteLocatorConfig extends ZuulProxyAutoConfiguration {

    @Autowired
    private DiscoveryClient discovery;

    @Autowired
    private ServiceRouteMapper serviceRouteMapper;

    @Bean
    public DiscoveryClientRouteLocator bean(){
        DiscoveryClientRouteLocator discoveryClientRouteLocator = new DiscoveryClientRouteLocator(this.server.getServlet().getContextPath(),
                discovery, this.zuulProperties, serviceRouteMapper);
        return discoveryClientRouteLocator;
    }

}
