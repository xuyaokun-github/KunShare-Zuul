package cn.com.kun.springcloud.zuul.controller;

import cn.com.kun.springcloud.zuul.common.vo.ResultVo;
import cn.com.kun.springcloud.zuul.config.limit.RateLimiterProperties;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.HashMap;
import java.util.Map;

@RequestMapping("/zuul-main")
@RestController
public class ZuulApiGatewayMainController {

    @Autowired
    RateLimiterProperties rateLimiterProperties;

    @RequestMapping("/routes")
    public ResultVo routes(){

        Map<String, String> map = new HashMap<>();

        return ResultVo.valueOfSuccess("kunghsu");
    }





}
