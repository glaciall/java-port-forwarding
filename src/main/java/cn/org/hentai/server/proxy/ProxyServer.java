package cn.org.hentai.server.proxy;

import cn.org.hentai.server.model.Port;

/**
 * Created by Expect on 2018/1/25.
 * 代理服务
 */
public class ProxyServer implements Runnable
{
    Port port = null;
    public ProxyServer(Port port)
    {
        this.port = port;
    }

    public void run()
    {
        // TODO：给自己注册注册
    }

    public void start()
    {
        new Thread(this).start();
    }
}
