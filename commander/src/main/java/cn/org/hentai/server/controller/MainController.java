package cn.org.hentai.server.controller;

import cn.org.hentai.server.dao.HostDAO;
import cn.org.hentai.server.dao.PortDAO;
import cn.org.hentai.server.dao.UserDAO;
import cn.org.hentai.server.model.*;
import cn.org.hentai.server.util.MD5;
import cn.org.hentai.server.util.NonceStr;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.ResponseBody;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpSession;
import java.io.File;
import java.util.List;

/**
 * Created by matrixy on 2017-12-12.
 */

@Controller
public class MainController
{
    @Autowired
    UserDAO userDAO;

    @Autowired
    HostDAO hostDAO;

    @Autowired
    PortDAO portDAO;

    @Autowired
    HttpServletRequest request;

    private User getLoginUser()
    {
        return (User)request.getAttribute("loginUser");
    }

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
        User user = this.getLoginUser();
        Result result = new Result();
        Page<Host> clients = new Page(pageIndex, pageSize);
        clients.setList(hostDAO.find(user.getId(), pageIndex, pageSize));
        clients.setRecordCount(hostDAO.findCount(user.getId()));
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
            Host host = new Host();
            host.setState(1);
            host.setUserId(this.getLoginUser().getId());
            host.setName(name);
            host.setLastActiveTime(0);
            host.setIp(null);
            host.setAccesstoken(NonceStr.generate(64));
            hostDAO.save(host);

            result.setData(host);
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
            Host host = hostDAO.getById(id);
            if (null == host) throw new RuntimeException("无此主机");
            if (host.getUserId() != this.getLoginUser().getId()) throw new RuntimeException("无权操作");

            host.setAccesstoken(NonceStr.generate(64));
            hostDAO.update(host);
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
            Host host = hostDAO.getById(id);
            if (null == host) throw new RuntimeException("无此主机");
            if (host.getUserId() != this.getLoginUser().getId()) throw new RuntimeException("无权操作");

            host.setName(name);
            hostDAO.update(host);
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
            Host host = hostDAO.getById(id);
            if (null == host) throw new RuntimeException("无此主机");
            if (host.getUserId() != this.getLoginUser().getId()) throw new RuntimeException("无权操作");
            hostDAO.delete(id);
        }
        catch(Exception e)
        {
            result.setError(e);
        }

        return result;
    }

    // 端口管理
    @RequestMapping("/manage/port")
    public String port(@RequestParam int hostId, Model model)
    {
        model.addAttribute("hostId", hostId);
        return "port";
    }

    @RequestMapping("/manage/port/json")
    @ResponseBody
    public Result portJson(@RequestParam int hostId)
    {
        Result result = new Result();
        try
        {
            Page<Port> page = new Page(1, 10000);
            List<Port> portList = portDAO.list(this.getLoginUser().getId(), hostId);
            page.setList(portList);
            page.setRecordCount(portList.size());
            result.setData(page);
        }
        catch(Exception e)
        {
            result.setError(e);
        }
        return result;
    }

    // TODO: 系统设置
}
