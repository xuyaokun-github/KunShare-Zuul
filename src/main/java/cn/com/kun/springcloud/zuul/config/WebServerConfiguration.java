package cn.com.kun.springcloud.zuul.config;

import org.apache.catalina.connector.Connector;
import org.apache.coyote.http11.Http11NioProtocol;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.boot.web.embedded.tomcat.TomcatServletWebServerFactory;
import org.springframework.boot.web.server.ConfigurableWebServerFactory;
import org.springframework.boot.web.server.WebServerFactoryCustomizer;
import org.springframework.boot.web.servlet.ServletContextInitializer;
import org.springframework.stereotype.Component;

/**
 * Springboot1 自定义Tomcat配置
 *
 * author:xuyaokun_kzx
 * date:2023/9/6
 * desc:
*/
@ConditionalOnProperty(prefix = "kunsharezuul.custom-tomcat.config", value = {"enabled"}, havingValue = "true", matchIfMissing = true)
@Component //做keep alive实验时再启用，平时不要打开
public class WebServerConfiguration implements WebServerFactoryCustomizer<ConfigurableWebServerFactory> {

    @Value("${kunsharezuul.custom-tomcat.keepAliveTimeout:60000}")
    private int keepAliveTimeout;

    @Override
    public void customize(ConfigurableWebServerFactory configurableWebServerFactory) {
        //使用对应工厂类提供给我们的接口定制化我们的tomcat connector
        ((TomcatServletWebServerFactory) configurableWebServerFactory).addConnectorCustomizers(
                (connector) -> {
                    Http11NioProtocol protocol = (Http11NioProtocol) connector.getProtocolHandler();
                    //定制化keepalivetimeout,设置30秒内没有请求则服务端自动断开keepalive链接
                    protocol.setKeepAliveTimeout(keepAliveTimeout);
                    //当客户端发送超过10000个请求则自动断开keepalive链接
                    protocol.setMaxKeepAliveRequests(10000);
                }
        );
    }


}
