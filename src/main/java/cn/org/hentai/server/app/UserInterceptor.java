package cn.org.hentai.server.app;

import cn.org.hentai.server.dao.UserDAO;
import cn.org.hentai.server.model.User;
import cn.org.hentai.server.util.Configs;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.servlet.ModelAndView;
import org.springframework.web.servlet.handler.HandlerInterceptorAdapter;

import javax.annotation.Resource;
import javax.servlet.http.Cookie;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * Created by matrixy on 2017/8/26.
 */
public class UserInterceptor extends HandlerInterceptorAdapter
{
    @Autowired
    UserDAO userDAO;

    @Override
    public boolean preHandle(HttpServletRequest request,
                             HttpServletResponse response,
                             Object handler) throws Exception
    {
        // 获取登陆的用户身份
        Map<String, String> cookies = new HashMap<String, String>();
        Cookie[] cks = request.getCookies();
        cks = cks == null ? new Cookie[0] : cks;
        for (int i = 0; i < cks.length; i++) cookies.put(cks[i].getName(), cks[i].getValue());

        if (!cookies.containsKey("user_id") || !cookies.containsKey("token"))
        {
            response.sendRedirect(request.getContextPath() + "/");
            return false;
        }
        long userId = Long.parseLong(cookies.get("user_id"));
        String token = cookies.get("token");

        User user = userDAO.getById(userId);
        if (null == user)
        {
            response.sendRedirect(request.getContextPath() + "/");
            return false;
        }

        if (!token.equals(user.getAccesstoken()))
        {
            response.sendRedirect(request.getContextPath() + "/");
            return false;
        }

        int minutes = Configs.getInt("user.token.expire.minutes", 60 * 24);
        if (user.getLastLoginTime().getTime() + 1000L * 60 * minutes < System.currentTimeMillis())
        {
            response.sendRedirect(request.getContextPath() + "/");
            return false;
        }

        request.setAttribute("loginUser", user);
        return true;
    }

    @Override
    public void postHandle(HttpServletRequest request,
                           HttpServletResponse response,
                           Object handler,
                           ModelAndView modelAndView) throws Exception
    {
        // do nothing here...
    }
}
