package net.sf.sitemonitoring.service.check;

import net.sf.sitemonitoring.service.check.util.PagingServlet;
import net.sf.sitemonitoring.service.check.util.SpiderListingServlet;
import org.springframework.boot.web.servlet.ServletRegistrationBean;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

@Configuration
public class CustomConfiguration {


    @Bean
    public ServletRegistrationBean<PagingServlet> pagingServletServletRegistrationBean() {
        return new ServletRegistrationBean<>(new PagingServlet(), "/spider/page");
    }

    @Bean
    public ServletRegistrationBean<SpiderListingServlet> spiderListingServletServletRegistrationBean() {
        return new ServletRegistrationBean<>(new SpiderListingServlet(), "/spider/");
    }

}
