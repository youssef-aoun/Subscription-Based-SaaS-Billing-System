package com.youssef.Subscription_Based.SaaS.Billing.System.config.ratelimit;

import org.springframework.boot.web.servlet.FilterRegistrationBean;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

@Configuration
public class FilterRegistrationConfig {
    @Bean
    public FilterRegistrationBean<LoginRateLimitFilter> loginRateLimiterFilter(LoginRateLimitFilter filter) {
        FilterRegistrationBean<LoginRateLimitFilter> registrationBean = new FilterRegistrationBean<>();
        registrationBean.setFilter(filter);
        registrationBean.addUrlPatterns("/api/v1/auth/login");
        registrationBean.setOrder(1); // Ensure it's high priority (before Spring Security)
        return registrationBean;
    }

    @Bean
    public FilterRegistrationBean<RegisterRateLimitFilter> registerRateLimiterFilter(RegisterRateLimitFilter filter) {
        FilterRegistrationBean<RegisterRateLimitFilter> registrationBean = new FilterRegistrationBean<>();
        registrationBean.setFilter(filter);
        registrationBean.addUrlPatterns("/api/v1/auth/register");
        registrationBean.setOrder(1); // Ensure it's high priority (before Spring Security)
        return registrationBean;
    }

    @Bean
    public FilterRegistrationBean<ResetPasswordRateLimitFilter> resetPasswordRateLimiterFilter(ResetPasswordRateLimitFilter filter) {
        FilterRegistrationBean<ResetPasswordRateLimitFilter> registrationBean = new FilterRegistrationBean<>();
        registrationBean.setFilter(filter);
        registrationBean.addUrlPatterns("/api/v1/users/reset-password");
        registrationBean.setOrder(1); // Ensure it's high priority (before Spring Security)
        return registrationBean;
    }
}
