package cn.com.kun.springcloud.zuul.common;

import cn.com.kun.springcloud.zuul.common.utils.JacksonUtils;
import org.springframework.util.AntPathMatcher;

import java.util.HashMap;
import java.util.Map;

public class Test {

    public static void main(String[] args) {
        AntPathMatcher matcher = new AntPathMatcher();
        String str = "/kunshare-zuul/kunsharedemo/kunsharedemo/zuul-demo/test1";
        System.out.println(matcher.match("/kunsharedemo/zuul-demo/test1", str));//false
        System.out.println(matcher.match("/**/kunsharedemo/zuul-demo/test1", str));//true

    }
}
