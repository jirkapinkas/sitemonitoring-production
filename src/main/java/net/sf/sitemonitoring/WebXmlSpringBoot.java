package net.sf.sitemonitoring;

import org.apache.catalina.Context;
import org.apache.tomcat.util.scan.StandardJarScanner;
import org.springframework.boot.web.embedded.tomcat.TomcatServletWebServerFactory;
import org.springframework.boot.web.server.MimeMappings;
import org.springframework.boot.web.server.WebServerFactoryCustomizer;
import org.springframework.boot.web.servlet.server.ConfigurableServletWebServerFactory;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.core.Ordered;
import org.springframework.web.servlet.config.annotation.ViewControllerRegistry;
import org.springframework.web.servlet.config.annotation.WebMvcConfigurer;

@Configuration
public class WebXmlSpringBoot implements WebMvcConfigurer, WebServerFactoryCustomizer<ConfigurableServletWebServerFactory> {

    // turn off tomcat classpath scanning during application startup, it throws weird errors and it's unnecessary
    // https://stackoverflow.com/questions/43264890/after-upgrade-from-spring-boot-1-2-to-1-5-2-filenotfoundexception-during-tomcat
    @Bean
    public TomcatServletWebServerFactory tomcatFactory() {
        return new TomcatServletWebServerFactory() {
            @Override
            protected void postProcessContext(Context context) {
                ((StandardJarScanner) context.getJarScanner()).setScanManifest(false);
            }
        };
    }

    @Override
    public void addViewControllers(ViewControllerRegistry registry) {
        registry.addViewController("/").setViewName("forward:/index.xhtml");
        registry.setOrder(Ordered.HIGHEST_PRECEDENCE);
    }

    @Override
    public void customize(ConfigurableServletWebServerFactory factory) {
        MimeMappings mappings = new MimeMappings(MimeMappings.DEFAULT);
        mappings.add("eot", "application/vnd.ms-fontobject");
        mappings.add("otf", "font/opentype");
        mappings.add("ttf", "application/x-font-ttf");
        mappings.add("woff", "application/x-font-woff");
        mappings.add("svg", "image/svg+xml");
        mappings.add("woff2", "application/x-font-woff2");
        factory.setMimeMappings(mappings);
    }

}