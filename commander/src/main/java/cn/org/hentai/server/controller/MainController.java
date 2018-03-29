package cn.org.hentai.server.controller;

import cn.org.hentai.server.dao.HostDAO;
import cn.org.hentai.server.dao.PortDAO;
import cn.org.hentai.server.dao.UserDAO;
import cn.org.hentai.server.model.*;
import cn.org.hentai.server.protocol.commander.HostConnectionManager;
import cn.org.hentai.server.protocol.proxy.ProxyThreadManager;
import cn.org.hentai.server.util.MD5;
import cn.org.hentai.server.util.NonceStr;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.util.StringUtils;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.ResponseBody;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
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
        // boolean hasDB = new File("jforwarding.sqlite").exists();
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

    @RequestMapping("/logout")
    public String logout(HttpSession session, HttpServletRequest request, HttpServletResponse response)
    {
        session.removeAttribute("loginUser");
        return "/";
    }

    @RequestMapping("/manage/user/passwd/reset")
    @ResponseBody
    public Result resetPasswd(@RequestParam String oldPwd, @RequestParam String password, @RequestParam String password2)
    {
        Result result = new Result();
        try
        {
            if (StringUtils.isEmpty(oldPwd)) throw new RuntimeException("请填写旧的登陆密码");
            if (StringUtils.isEmpty(password)) throw new RuntimeException("请填写新的登陆密码");
            if (!password.equals(password2)) throw new RuntimeException("两次输入的新的登陆密码不一致");
            if (oldPwd.equals(password)) throw new RuntimeException("新旧密码不能一样");

            User user = getLoginUser();
            String pwd = MD5.encode(oldPwd + "<===>" + user.getSalt());
            if (!pwd.equals(user.getPassword())) throw new RuntimeException("旧的登陆密码不正确");
            user.setSalt(NonceStr.generate(12));
            user.setPassword(MD5.encode(password + "<===>" + user.getSalt()));
            userDAO.update(user);
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
        Page<Host> hosts = new Page(pageIndex, pageSize);
        List<Host> hostList = hostDAO.find(user.getId(), pageIndex, pageSize);
        HostConnectionManager mgr = HostConnectionManager.getInstance();
        for (int i = 0; i < hostList.size(); i++)
        {
            Host host = hostList.get(i);
            host.setOnline(mgr.isOnline(host.getId()));
            host.setIp(mgr.getHostIp(host.getId()));
        }
        hosts.setList(hostList);
        hosts.setRecordCount(hostDAO.findCount(user.getId()));
        result.setData(hosts);
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

            if (portDAO.list(getLoginUser().getId(), host.getId()).size() > 0)
                throw new RuntimeException("请先删除该主机下的所有端口转发配置");

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
            ProxyThreadManager mgr = ProxyThreadManager.getInstance();
            for (int i = 0; i < portList.size(); i++)
            {
                Port port = portList.get(i);
                port.setOnline(mgr.isOnline(port.getListenPort()));
            }
            page.setList(portList);
            page.setRecordCount(portList.size());
            result.setData(page);
        }
        catch(Exception e)
        {
            e.printStackTrace();
            result.setError(e);
        }
        return result;
    }

    // 添加新端口
    @RequestMapping("/manage/port/save")
    @ResponseBody
    public Result save(@RequestParam int hostId,
                       @RequestParam String name,
                       @RequestParam int listenPort,
                       @RequestParam(defaultValue = "localhost") String hostIp,
                       @RequestParam int hostPort,
                       @RequestParam(defaultValue = "30") int soTimeout,
                       @RequestParam(defaultValue = "30") int connectTimeout,
                       @RequestParam(defaultValue = "10") int concurrentConnections)
    {
        User user = this.getLoginUser();
        Result result = new Result();
        try
        {
            Host host = hostDAO.getById(hostId);
            if (null == host) throw new RuntimeException("无此主机");
            if (host.getUserId() != user.getId()) throw new RuntimeException("无权添加");

            if (StringUtils.isEmpty(name) || name.length() > 20) throw new RuntimeException("服务名称不能为空，最多只能20个字符");
            if (StringUtils.isEmpty(hostIp)) throw new RuntimeException("请填写被代理主机的IP或域名");
            if (listenPort < 1 || listenPort > 65535) throw new RuntimeException("请填写正确的代理服务端口");
            if (hostPort < 1 || listenPort > 65535) throw new RuntimeException("请填写正确的被代理端口");

            Port port = new Port();
            port.setHostId(host.getId());
            port.setUserId(user.getId());
            port.setName(name);
            port.setListenPort(listenPort);
            port.setHostIp(hostIp);
            port.setHostPort(hostPort);
            port.setSoTimeout(soTimeout);
            port.setConnectTimeout(connectTimeout);
            port.setConcurrentConnections(concurrentConnections);
            port.setState(1);
            port.setLastActiveTime(0);
            port.setCreateTime(System.currentTimeMillis());

            portDAO.save(port);

            // 启动此端口的监听服务
            ProxyThreadManager.getInstance().start(port);
        }
        catch(Exception e)
        {
            result.setError(e);
        }
        return result;
    }

    // 删除端口转发
    @RequestMapping("/manage/port/remove")
    @ResponseBody
    public Result remove(@RequestParam int portId)
    {
        Result result = new Result();
        try
        {
            Port port = portDAO.getById(portId);
            if (null == port) throw new RuntimeException("无此端口设置");
            if (port.getUserId() != getLoginUser().getId()) throw new RuntimeException("无权删除");

            portDAO.delete(port);

            // 停止此端口的监听服务
            ProxyThreadManager.getInstance().stop(port);
        }
        catch(Exception e)
        {
            result.setError(e);
        }
        return result;
    }

    // 启用端口转发
    @RequestMapping("/manage/port/enable")
    @ResponseBody
    public Result enable(@RequestParam int portId)
    {
        Result result = new Result();
        try
        {
            Port port = portDAO.getById(portId);
            if (null == port) throw new RuntimeException("无此端口设置");
            if (port.getUserId() != getLoginUser().getId()) throw new RuntimeException("无权修改");
            if (port.getState() == 1) return result;

            port.setState(1);
            portDAO.update(port);

            // 开启此端口的监听服务
            ProxyThreadManager.getInstance().start(port);
        }
        catch(Exception e)
        {
            result.setError(e);
        }
        return result;
    }

    // 禁用端口转发
    @RequestMapping("/manage/port/disable")
    @ResponseBody
    public Result disable(@RequestParam int portId)
    {
        Result result = new Result();
        try
        {
            Port port = portDAO.getById(portId);
            if (null == port) throw new RuntimeException("无此端口设置");
            if (port.getUserId() != getLoginUser().getId()) throw new RuntimeException("无权修改");
            if (port.getState() == 2) return result;

            port.setState(2);
            portDAO.update(port);

            // 停止此端口的监听服务
            ProxyThreadManager.getInstance().stop(port);
        }
        catch(Exception e)
        {
            result.setError(e);
        }
        return result;
    }

    // TODO: 系统设置
}
