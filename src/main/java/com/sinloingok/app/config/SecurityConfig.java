package com.sinloingok.app.config;

import org.apache.catalina.Context;
import org.apache.catalina.connector.Connector;
import org.apache.tomcat.util.descriptor.web.SecurityCollection;
import org.apache.tomcat.util.descriptor.web.SecurityConstraint;
import org.springframework.boot.web.embedded.tomcat.TomcatServletWebServerFactory;
import org.springframework.boot.web.servlet.server.ServletWebServerFactory;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.Profile;

@Configuration
public class SecurityConfig {

    @Bean
    @Profile("dev")  // 仅 dev 环境生效
    public ServletWebServerFactory devServletContainer() {
        TomcatServletWebServerFactory tomcat = new TomcatServletWebServerFactory();
        // 仅配置 HTTP 端口（不添加 HTTPS 相关逻辑）
        tomcat.setPort(8081); // 直接设置 HTTP 端口
        return tomcat;
    }

    @Bean
    @Profile("prd")  // 仅 prod 环境生效
    public ServletWebServerFactory prodServletContainer() {
        TomcatServletWebServerFactory tomcat = servletContainer();
        tomcat.addAdditionalTomcatConnectors(prodRedirectConnector());
        return tomcat;
    }

    private TomcatServletWebServerFactory servletContainer() {
        TomcatServletWebServerFactory tomcat = new TomcatServletWebServerFactory() {
            @Override
            protected void postProcessContext(Context context) {
                // 强制 HTTPS
                SecurityConstraint securityConstraint = new SecurityConstraint();
                securityConstraint.setUserConstraint("CONFIDENTIAL");
                SecurityCollection collection = new SecurityCollection();
                collection.addPattern("/*");
                securityConstraint.addCollection(collection);
                context.addConstraint(securityConstraint);
            }
        };
        return tomcat;
    }

    private Connector prodRedirectConnector() {
        Connector connector = new Connector("org.apache.coyote.http11.Http11NioProtocol");
        connector.setScheme("http");
        connector.setPort(8080);  // HTTP 端口
        connector.setSecure(false);
        connector.setRedirectPort(443);  // 重定向到 HTTPS 端口
        return connector;
    }
}