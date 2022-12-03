package com.xxxx.crm;

import com.alibaba.fastjson.JSON;
import com.xxxx.crm.base.ResultInfo;
import com.xxxx.crm.exceptions.AuthException;
import com.xxxx.crm.exceptions.NoLoginException;
import com.xxxx.crm.exceptions.ParamsException;
import org.springframework.stereotype.Component;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.ResponseBody;
import org.springframework.web.method.HandlerMethod;
import org.springframework.web.servlet.HandlerExceptionResolver;
import org.springframework.web.servlet.ModelAndView;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import java.io.PrintWriter;

@Component
public class GlobalExceptionResolver implements HandlerExceptionResolver {
    @Override
    public ModelAndView resolveException(HttpServletRequest request, HttpServletResponse response, Object handler, Exception ex) {
        //非法请求拦截异常捕获
        if(ex instanceof NoLoginException){
            ModelAndView mv = new ModelAndView("redirect:/index");
            return mv;
        }
        //设置默认异常处理
        ModelAndView modelAndView = new ModelAndView();
        modelAndView.setViewName("error");
        modelAndView.addObject("code",400);
        modelAndView.addObject("msg","系统异常请重试");
        if (handler instanceof HandlerMethod){

            HandlerMethod handlerMethod = (HandlerMethod) handler;
            ResponseBody responseBody = handlerMethod.getMethod().getDeclaredAnnotation(ResponseBody.class);
            /**
            * 方法响应内容为视图
            */
            if (responseBody==null){
                if (ex instanceof ParamsException){
                    ParamsException pe = (ParamsException) ex;
                    modelAndView.addObject("code",pe.getCode());
                    modelAndView.addObject("msg",pe.getMsg());
                }if (ex instanceof AuthException){
                    AuthException pe = (AuthException) ex;
                    modelAndView.addObject("code",pe.getCode());
                    modelAndView.addObject("msg",pe.getMsg());
                }
                return modelAndView;
            }
            /**
             *  方法响应内容为json
             */
            else{
                ResultInfo resultInfo = new ResultInfo();
                resultInfo.setCode(300);
                resultInfo.setMsg("系统错误,请稍后再试...");

                if (ex instanceof ParamsException){
                    ParamsException pe = (ParamsException) ex;
                    resultInfo.setCode(pe.getCode());
                    resultInfo.setMsg(pe.getMsg());
                }if (ex instanceof AuthException){
                    AuthException pe = (AuthException) ex;
                    resultInfo.setCode(pe.getCode());
                    resultInfo.setMsg(pe.getMsg());
                }
                response.setContentType("application/json;charset=utf-8");
                response.setCharacterEncoding("utf-8");
                PrintWriter pw = null;
                try {
                    pw=response.getWriter();
                    pw.write(JSON.toJSONString(resultInfo));
                    pw.flush();
                } catch (Exception e) {
                    e.printStackTrace();
                }finally {
                    if(null !=pw){
                        pw.close();
                    }
                }
                return null;
            }
        }else {
           return modelAndView;
        }




    }
}
