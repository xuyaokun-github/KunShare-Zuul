package cn.com.kun.springcloud.zuul.controller;

import cn.com.kun.springcloud.zuul.common.vo.ResultVo;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RequestMapping("/zuul-hello")
@RestController
public class ZuulHelloController {

    @RequestMapping("/test1")
    public Object test1(){

        return ResultVo.valueOfSuccess("kunghsu");
    }





}
