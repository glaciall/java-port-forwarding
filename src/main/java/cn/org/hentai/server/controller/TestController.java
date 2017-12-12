package cn.org.hentai.server.controller;

import org.springframework.boot.autoconfigure.EnableAutoConfiguration;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.ResponseBody;
import org.springframework.web.servlet.ModelAndView;

import java.util.Date;

/**
 * Created by matrixy on 2017-12-12.
 */

@Controller
public class TestController
{
    @RequestMapping("/")
    public String home(Model model)
    {
        model.addAttribute("message", "xixi: " + new Date().toLocaleString());
        return "test";
    }
}
