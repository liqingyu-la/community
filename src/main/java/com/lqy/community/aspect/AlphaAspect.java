package com.lqy.community.aspect;

import org.aspectj.lang.annotation.Aspect;
import org.aspectj.lang.annotation.Before;
import org.aspectj.lang.annotation.Pointcut;
import org.springframework.stereotype.Component;

/*@Component
@Aspect*/
public class AlphaAspect {
//      连接点，切点
    @Pointcut("execution(* com.lqy.community.service.*.*(..))")//方法返回值 包名 类 方法 所有参数（筛选方法）
    public void pointCut(){

    }
//    定义通知
    @Before("pointCut()")
    public void before(){

    }
}
