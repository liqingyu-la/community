package com.lqy.community.controller.advice;


import com.lqy.community.util.CommunityUtil;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.ControllerAdvice;
import org.springframework.web.bind.annotation.ExceptionHandler;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import java.io.IOException;
import java.io.PrintWriter;

@ControllerAdvice(annotations = Controller.class)//只扫描带controller注解的bean即controller组件
public class ExceptionAdvice {

    private static final Logger logger = LoggerFactory.getLogger(ExceptionAdvice.class);

    @ExceptionHandler({Exception.class})
    public void handleException(Exception e, HttpServletRequest request, HttpServletResponse response) throws IOException {
        logger.error("服务器发生异常： " + e.getMessage());
        for (StackTraceElement element : e.getStackTrace()) {
            logger.error(element.toString());
        }

        //判断普通请求还是异步请求
        String xRequestedWith = request.getHeader("x-requested-with");//请求方式
        if ("XMLHttpRequest".equals(xRequestedWith)){//异步请求
            response.setContentType("application/plain;charset=utf-8");//向服务器返回字符串
            //获取输出流
            PrintWriter writer = response.getWriter();
            writer.write(CommunityUtil.getJSONString(1, "服务器异常"));
        } else{//重定向到错误页面
            response.sendRedirect(request.getContextPath() + "/error");
        }

    }
}
