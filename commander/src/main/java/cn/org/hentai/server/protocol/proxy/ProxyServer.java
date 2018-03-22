package cn.org.hentai.server.protocol.proxy;

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
            Log.debug("Proxy[" + port.getListenPort() + " <---> " + port.getHostPort() + " ] started...");
            while (true)
            {
                Socket client = server.accept();
                new Thread(new ProxySession(port, client, this.port.getConnectTimeout())).start();
            }
        }
        catch(Exception e)
        {
            Log.error(e);
        }
    }
}
