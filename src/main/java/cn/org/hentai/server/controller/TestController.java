package cn.org.hentai.server.controller;

import cn.org.hentai.server.dao.AdminDAO;
import cn.org.hentai.server.model.Admin;
import cn.org.hentai.server.util.Log;
import org.springframework.beans.factory.annotation.Autowired;
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
    @Autowired
    AdminDAO adminDAO;

    @RequestMapping("/")
    public String home(Model model)
    {
        Admin admin = adminDAO.getById(1);
        Log.debug("Admin: " + admin);

        model.addAttribute("message", "xixi: " + new Date().toLocaleString());
        return "test";
    }
}
