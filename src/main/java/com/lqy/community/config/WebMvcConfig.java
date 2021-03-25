package com.lqy.community.config;


import com.lqy.community.annotation.LoginRequired;
import com.lqy.community.controller.interceptor.AlphaInterceptor;
import com.lqy.community.controller.interceptor.LoginRequiredInterceptor;
import com.lqy.community.controller.interceptor.LoginTicketInterceptor;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;
import org.springframework.web.servlet.config.annotation.InterceptorRegistry;
import org.springframework.web.servlet.config.annotation.WebMvcConfigurer;

@Component
public class WebMvcConfig implements WebMvcConfigurer {
    @Autowired
    private AlphaInterceptor alphaInterceptor;//注入拦截器

    @Autowired
    private LoginTicketInterceptor loginTicketInterceptor;

    @Autowired
    private LoginRequiredInterceptor loginRequiredInterceptor;

    @Override
    public void addInterceptors(InterceptorRegistry registry){
        registry.addInterceptor(alphaInterceptor)
            .excludePathPatterns("/css/*.css", "/js/*.js", "/img/*.png", "/img/*.jpg", "/img/*.jpeg")//排除静态资源的访问
                .addPathPatterns("/register", "/login");//明确想要拦截的功能路径

        registry.addInterceptor(loginTicketInterceptor)
                .excludePathPatterns("/css/*.css", "/js/*.js", "/img/*.png", "/img/*.jpg", "/img/*.jpeg");//排除静态资源的访问
//                 全都拦截

        registry.addInterceptor(loginRequiredInterceptor)
                .excludePathPatterns("/css/*.css", "/js/*.js", "/img/*.png", "/img/*.jpg", "/img/*.jpeg");//排除静态资源的访问
    }

}
