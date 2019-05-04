package com.lhj.controller;

import com.lhj.feamework.v1.annotation.LhjAutowired;
import com.lhj.feamework.v1.annotation.LhjController;
import com.lhj.feamework.v1.annotation.LhjRequestMapping;
import com.lhj.service.TestService;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import java.io.IOException;

/**
 * @Description:
 * @Author: lhj
 * @Time: 2019/5/4 16:09
 * @Version: 1.0
 */
@LhjController
@LhjRequestMapping("/lhj")
public class TestController {

    @LhjAutowired
    private TestService testService;

    @LhjRequestMapping(value = "/one")
    public void testOne(HttpServletRequest req, HttpServletResponse res){
        try {
            String str = testService.returnStr();
            res.getWriter().write(str);
        } catch (IOException e) {
            e.printStackTrace();
        }
        System.out.println("1");
    }

    public static void main(String[] args) {
        String str = "//lhj//one";
        String s = "";
        s = str.replaceAll("/","/");
        System.out.println(s);
    }
}
