package cn.org.hentai.server.controller;

import cn.org.hentai.server.dao.ClientDAO;
import cn.org.hentai.server.dao.UserDAO;
import cn.org.hentai.server.model.Client;
import cn.org.hentai.server.model.Page;
import cn.org.hentai.server.model.Result;
import cn.org.hentai.server.model.User;
import cn.org.hentai.server.util.Configs;
import cn.org.hentai.server.util.Log;
import cn.org.hentai.server.util.MD5;
import cn.org.hentai.server.util.NonceStr;
import jdk.nashorn.internal.runtime.regexp.joni.Config;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.ResponseBody;

import javax.servlet.http.HttpSession;
import java.io.File;
import java.util.Date;

/**
 * Created by matrixy on 2017-12-12.
 */

@Controller
public class MainController
{
    @Autowired
    UserDAO userDAO;

    @Autowired
    ClientDAO clientDAO;

    @RequestMapping("/")
    public String index()
    {
        boolean hasDB = new File("jforwarding.sqlite").exists();
        return "login";
    }

    @RequestMapping("/setup")
    public String setup()
    {
        return "setup";
    }

    @RequestMapping("/login")
    @ResponseBody
    public Result login(HttpSession session, @RequestParam String username, @RequestParam String password)
    {
        Result result = new Result();
        try
        {
            User user = userDAO.getByName(username);
            if (null == user) throw new RuntimeException("无此用户");

            // 密码校验
            String pwd = MD5.encode(password + "<===>" + user.getSalt());
            if (!pwd.equals(user.getPassword())) throw new RuntimeException("用户名或密码错误");

            session.setAttribute("loginUser", user);
        }
        catch(Exception e)
        {
            result.setError(e);
        }
        return result;
    }

    @RequestMapping("/manage/host")
    public String host()
    {
        return "host";
    }

    @RequestMapping("/manage/host/json")
    @ResponseBody
    public Result hostJson(@RequestParam(required = false, defaultValue = "1") int pageIndex, @RequestParam(required = false, defaultValue = "50") int pageSize)
    {
        Result result = new Result();
        Page<Client> clients = new Page(pageIndex, pageSize);
        clients.setList(clientDAO.find(pageIndex, pageSize));
        clients.setRecordCount(clientDAO.findCount());
        result.setData(clients);
        return result;
    }

    @RequestMapping("/manage/host/add")
    @ResponseBody
    public Result addHost(@RequestParam String name)
    {
        Result result = new Result();

        try
        {
            Client client = new Client();
            client.setState(1);
            client.setName(name);
            client.setLastActiveTime(0);
            client.setIp(null);
            client.setAccesstoken(NonceStr.generate(64));
            clientDAO.save(client);

            result.setData(client);
        }
        catch(Exception e)
        {
            result.setError(e);
        }
        return result;
    }

    @RequestMapping("/manage/host/renew")
    @ResponseBody
    public Result renewHostToken(@RequestParam int id)
    {
        Result result = new Result();
        try
        {
            Client client = clientDAO.getById(id);
            if (null == client) throw new RuntimeException("无此主机");

            client.setAccesstoken(NonceStr.generate(64));
            clientDAO.update(client);
        }
        catch(Exception e)
        {
            result.setError(e);
        }
        return result;
    }

    @RequestMapping("/manage/host/rename")
    @ResponseBody
    public Result renameHost(@RequestParam int id, @RequestParam String name)
    {
        Result result = new Result();
        try
        {
            Client host = clientDAO.getById(id);
            if (null == host) throw new RuntimeException("无此主机");

            host.setName(name);
            clientDAO.update(host);
        }
        catch(Exception e)
        {
            result.setError(e);
        }
        return result;
    }

    @RequestMapping("/manage/host/remove")
    @ResponseBody
    public Result removeHost(@RequestParam int id)
    {
        Result result = new Result();

        try
        {
            clientDAO.delete(id);
        }
        catch(Exception e)
        {
            result.setError(e);
        }

        return result;
    }

    // TODO: 端口管理
    // TODO: 系统设置？
}
