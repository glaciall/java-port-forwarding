package cn.org.hentai.server.controller;

import org.springframework.boot.autoconfigure.EnableAutoConfiguration;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.ResponseBody;

/**
 * Created by matrixy on 2017-12-12.
 */
@Controller
public class HelloController
{
    @RequestMapping("/hello")
    @ResponseBody
    public String hello()
    {
        return "走你";
    }
}
