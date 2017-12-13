package cn.org.hentai.server.controller;

import cn.org.hentai.server.dao.UserDAO;
import cn.org.hentai.server.model.Result;
import cn.org.hentai.server.model.User;
import cn.org.hentai.server.util.Configs;
import cn.org.hentai.server.util.Log;
import cn.org.hentai.server.util.MD5;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.ResponseBody;

import java.util.Date;

/**
 * Created by matrixy on 2017-12-12.
 */

@Controller
public class MainController
{
    @Autowired
    UserDAO userDAO;

    @RequestMapping("/")
    public String index(Model model)
    {
        model.addAttribute("user", userDAO.getById(1));
        return "login";
    }

    @RequestMapping("/login")
    @ResponseBody
    public Result login(@RequestParam String name, @RequestParam String password)
    {
        Result result = new Result();
        User user = userDAO.getByName(name);
        if (null == user) throw new RuntimeException("无此用户");

        // 密码校验
        String pwd = MD5.encode(password + user.getSalt() + Configs.get("user.token.key"));
        if (!pwd.equals(user.getPassword())) throw new RuntimeException("用户名或密码错误");

        // 生成accesstoken

        // TODO: 做一个简单的限制，在5分钟内不允许有3次以上的登陆失败

        return result;
    }

    // TODO: 登陆后的首页
    // TODO: 用户管理
    // TODO: 主机管理
    // TODO: 端口管理
    // TODO: 系统设置？
}
