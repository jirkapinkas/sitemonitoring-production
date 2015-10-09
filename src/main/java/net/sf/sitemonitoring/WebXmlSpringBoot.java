package net.sf.sitemonitoring;

import org.springframework.boot.context.embedded.ConfigurableEmbeddedServletContainer;
import org.springframework.boot.context.embedded.EmbeddedServletContainerCustomizer;
import org.springframework.boot.context.embedded.MimeMappings;
import org.springframework.context.annotation.Configuration;
import org.springframework.core.Ordered;
import org.springframework.web.servlet.config.annotation.ViewControllerRegistry;
import org.springframework.web.servlet.config.annotation.WebMvcConfigurerAdapter;

@Configuration
public class WebXmlSpringBoot extends WebMvcConfigurerAdapter implements EmbeddedServletContainerCustomizer {

	@Override
	public void addViewControllers(ViewControllerRegistry registry) {
		registry.addViewController("/").setViewName("forward:/index.xhtml");
		registry.setOrder(Ordered.HIGHEST_PRECEDENCE);
		super.addViewControllers(registry);
	}

	@Override
	public void customize(ConfigurableEmbeddedServletContainer container) {
		MimeMappings mappings = new MimeMappings(MimeMappings.DEFAULT);
		mappings.add("eot", "application/vnd.ms-fontobject");
		mappings.add("otf", "font/opentype");
		mappings.add("ttf", "application/x-font-ttf");
		mappings.add("woff", "application/x-font-woff");
		mappings.add("svg", "image/svg+xml");
		mappings.add("woff2", "application/x-font-woff2");
		container.setMimeMappings(mappings);
	}

}