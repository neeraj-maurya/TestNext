package com.testnext.config;

import org.springframework.boot.web.servlet.ServletRegistrationBean;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.Profile;

import jakarta.servlet.Servlet;

@Configuration
@Profile("dev")
public class H2Config {

    @Bean
    public ServletRegistrationBean<Servlet> h2ConsoleServlet() {
        ServletRegistrationBean<Servlet> registrationBean = new ServletRegistrationBean<>(
                new org.h2.server.web.JakartaWebServlet());
        registrationBean.addUrlMappings("/h2-console/*");
        return registrationBean;
    }
}
