package cn.org.hentai.server.protocol.host;

import cn.org.hentai.server.protocol.SocketSessionManager;
import cn.org.hentai.server.util.Configs;
import cn.org.hentai.server.util.Log;

import java.net.ServerSocket;
import java.net.Socket;

/**
 * Created by matrixy on 2018/3/22.
 * 提供统一的服务监听，负责将由主机端发起的连接转交给相应的客户端连接线程
 */
public class HostForwardServer implements Runnable
{

    private void forward() throws Exception
    {
        ServerSocket server = new ServerSocket(Configs.getInt("server.forward.port", 11221), 1000);
        while (true)
        {
            Socket hostConnection = server.accept();
            ForwardSession session = new ForwardSession(hostConnection);
            SocketSessionManager.getInstance().register(session);
            session.start();
        }
    }

    public void run()
    {
        try
        {
            forward();
        }
        catch(Exception e)
        {
            Log.error(e);
        }
    }
}
