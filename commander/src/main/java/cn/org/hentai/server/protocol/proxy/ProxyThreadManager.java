package cn.org.hentai.server.protocol.proxy;

import cn.org.hentai.server.dao.PortDAO;
import cn.org.hentai.server.model.Port;
import cn.org.hentai.server.util.BeanUtils;
import cn.org.hentai.server.util.Log;
import org.springframework.context.ApplicationContext;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * Created by Expect on 2018/3/5.
 */
public final class ProxyThreadManager
{
    private ProxyThreadManager()
    {
        // ..
    }
    private static ProxyThreadManager instance;
    public static ProxyThreadManager init()
    {
        return getInstance();
    }

    public synchronized static ProxyThreadManager getInstance()
    {
        if (instance != null) return instance;
        instance = new ProxyThreadManager();
        return instance;
    }

    private Map<Integer, Thread> threads = new HashMap<Integer, Thread>();

    /**
     * 开启对指定端口的转发监听
     * @param port
     */
    public void start(Port port)
    {
        Thread thread = null;
        synchronized (threads)
        {
            thread = threads.get(port.getListenPort());
        }
        if (thread != null && thread.getState() != Thread.State.TERMINATED) throw new RuntimeException("代理转发服务不能重复启动");
        thread = new Thread(new ProxyServer(port));
        threads.put(port.getListenPort(), thread);
        thread.start();
    }

    /**
     * 停止对指定端口的转发监听服务
     * @param port
     */
    public void stop(Port port)
    {
        Thread thread = null;
        synchronized (threads)
        {
            thread = threads.get(port.getListenPort());
        }
        if (thread != null) throw new RuntimeException("转发服务未启动");
        try
        {
            thread.stop();
        }
        catch(Exception e)
        {
            Log.error(e);
        }
    }

    /**
     * 启动数据库中己配置的所有转发监听服务
     * @param context
     */
    public void load(ApplicationContext context)
    {
        PortDAO portDAO = null;
        try
        {
            portDAO = BeanUtils.create(PortDAO.class);
            List<Port> ports = portDAO.listAll();
            for (int i = 0; i < ports.size(); i++)
                start(ports.get(i));
        }
        catch(Exception e)
        {
            Log.error(e);
        }
    }
}
