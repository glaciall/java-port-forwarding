package cn.org.hentai.server.protocol.proxy;

import cn.org.hentai.server.model.Port;
import cn.org.hentai.server.protocol.SocketSessionManager;
import cn.org.hentai.server.util.Log;

import java.net.InetAddress;
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
        ServerSocket server = null;
        try
        {
            server = new ServerSocket(this.port.getListenPort(), this.port.getConcurrentConnections(), InetAddress.getByName("0.0.0.0"));
            Log.debug("Proxy[" + port.getListenPort() + " <---> " + port.getHostPort() + " ] started...");
            while (true)
            {
                Socket client = server.accept();
                if (Thread.interrupted())
                {
                    client.close();
                    break;
                }
                ProxySession session = new ProxySession(port, client);
                SocketSessionManager.getInstance().register(session);
                session.start();
            }
        }
        catch(Exception e)
        {
            Log.error(e);
        }
        finally
        {
            try { server.close(); } catch(Exception e) { }
        }
    }

    public static void main(String[] args) throws Exception
    {
        InetAddress addr = InetAddress.getByAddress(new byte[]{ 0, 0, 0, 0 });
        System.out.println(addr);
    }
}
