package cn.org.hentai.server.proxy.forward;

import cn.org.hentai.server.model.Port;
import cn.org.hentai.server.util.Log;

import java.net.ServerSocket;
import java.net.Socket;

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
        try
        {
            ServerSocket server = new ServerSocket(this.port.getListenPort(), this.port.getConcurrentConnections());
            while (true)
            {
                Socket client = server.accept();
                new Thread(new ProxySession(client, this.port.getConnectTimeout())).start();
            }
        }
        catch(Exception e)
        {
            Log.error(e);
        }

        // 1. 开始监听
        // 当有连接时：
        //      1. 让commandserver下发开始转发的指令
        //      2. 等待客户机的连接
        //      3. 开始转发
    }
}
