package com.lqy.community.aspect;

import org.aspectj.lang.JoinPoint;
import org.aspectj.lang.annotation.Aspect;
import org.aspectj.lang.annotation.Before;
import org.aspectj.lang.annotation.Pointcut;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Component;
import org.springframework.web.context.request.RequestAttributes;
import org.springframework.web.context.request.RequestContextHolder;
import org.springframework.web.context.request.ServletRequestAttributes;

import javax.servlet.http.HttpServletRequest;
import java.text.SimpleDateFormat;
import java.util.Date;

@Component
@Aspect
public class ServiceLogAspect {
    private static final Logger logger = LoggerFactory.getLogger(ServiceLogAspect.class);

    //连接点
    @Pointcut("execution(* com.lqy.community.service.*.*(..))")//方法返回值 包名 类 方法 所有参数（筛选方法）
    public void pointCut(){

    }

    @Before("pointCut()")
    public void before(JoinPoint joinPoint){
        //用户[1.2.3.4],在[时间],访问了[com.lqy.community.service.xxx()]

        //取ip地址
        ServletRequestAttributes attributes = (ServletRequestAttributes) RequestContextHolder.getRequestAttributes();

        if (attributes == null){
            return;
        }

        HttpServletRequest request = attributes.getRequest();

        String ip = request.getRemoteHost();
        //时间
        String now = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss").format(new Date());
        //目标
        String target = joinPoint.getSignature().getDeclaringTypeName() + joinPoint.getSignature().getName();//类名+方法名

        logger.info(String.format("用户[%s],在[%s],访问了[%s].", ip, now, target));

    }
}
