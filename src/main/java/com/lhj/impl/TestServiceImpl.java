package com.lhj.impl;

import com.lhj.feamework.v1.annotation.LhjService;
import com.lhj.service.TestService;

/**
 * @Description:
 * @Author: lhj
 * @Time: 2019/5/4 16:14
 * @Version: 1.0
 */
@LhjService
public class TestServiceImpl implements TestService {
    @Override
    public String returnStr() {
        return "hello this is service return";
    }
}
