package com.abinbev.b2b.price.api.config;

import java.util.List;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Configuration;
import org.springframework.http.converter.HttpMessageConverter;
import org.springframework.http.converter.json.MappingJackson2HttpMessageConverter;
import org.springframework.web.servlet.config.annotation.InterceptorRegistry;
import org.springframework.web.servlet.config.annotation.ResourceHandlerRegistry;
import org.springframework.web.servlet.config.annotation.WebMvcConfigurer;

import com.abinbev.b2b.price.api.interceptors.ControllerRequestInterceptor;

@Configuration
public class WebMvcConfig implements WebMvcConfigurer {

	@Autowired
	private final ControllerRequestInterceptor controllerRequestInterceptor;

	@Autowired
	private MappingJackson2HttpMessageConverter mappingJackson2HttpMessageConverter;

	@Autowired
	public WebMvcConfig(final ControllerRequestInterceptor controllerRequestInterceptor) {

		this.controllerRequestInterceptor = controllerRequestInterceptor;
	}

	@Override
	public void addInterceptors(final InterceptorRegistry registry) {

		registry.addInterceptor(controllerRequestInterceptor);
	}

	@Override
	public void addResourceHandlers(final ResourceHandlerRegistry registry) {

		registry.addResourceHandler("/swagger-ui.html").addResourceLocations("classpath:/META-INF/resources/");
		registry.addResourceHandler("/webjars/**").addResourceLocations("classpath:/META-INF/resources/webjars/");
	}

	@Override
	public void extendMessageConverters(final List<HttpMessageConverter<?>> converters) {

		int targetIndex = -1;
		for (int index = 0; index < converters.size(); index++) {
			if (converters.get(index) instanceof MappingJackson2HttpMessageConverter) {
				targetIndex = index;
				break;
			}
		}

		if (targetIndex > -1 && mappingJackson2HttpMessageConverter != null) {
			converters.set(targetIndex, mappingJackson2HttpMessageConverter);
		}

	}
}
