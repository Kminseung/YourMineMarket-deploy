package com.bit.yourmine.config;

import com.bit.yourmine.config.interceptor.CustomInterceptor;
import com.navercorp.lucy.security.xss.servletfilter.XssEscapeServletFilter;
import lombok.RequiredArgsConstructor;
import org.springframework.boot.web.servlet.FilterRegistrationBean;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.core.Ordered;
import org.springframework.web.servlet.config.annotation.InterceptorRegistry;
import org.springframework.web.servlet.config.annotation.WebMvcConfigurer;

@RequiredArgsConstructor
@Configuration
public class WebMvcConfig implements WebMvcConfigurer {

    private final CustomInterceptor customInterceptor;

    @Override
    public void addInterceptors(InterceptorRegistry registry) {
        registry.addInterceptor(customInterceptor)
                .addPathPatterns("/chat/**")
                .addPathPatterns("/posts/modify/*")
                .addPathPatterns("/posts/delete/*")
                .addPathPatterns("/review/**")
                .addPathPatterns("/request/users/*")
                .excludePathPatterns("/review/set", "/chat/db/**");
    }

    @Bean
    public FilterRegistrationBean xssFilterBean() {
        FilterRegistrationBean filterRegistrationBean = new FilterRegistrationBean();
        filterRegistrationBean.setFilter(new XssEscapeServletFilter());
        filterRegistrationBean.setOrder(Ordered.LOWEST_PRECEDENCE);
        filterRegistrationBean.addUrlPatterns("/*");
        return filterRegistrationBean;
    }
}