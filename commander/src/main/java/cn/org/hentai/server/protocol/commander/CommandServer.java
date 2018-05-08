package cn.org.hentai.server.protocol.commander;

import cn.org.hentai.server.util.Configs;
import cn.org.hentai.server.util.Log;

import java.net.InetAddress;
import java.net.ServerSocket;
import java.net.Socket;

/**
 * Created by Expect on 2018/1/25.
 * 指令服务，负责通知主机开始端口转发等
 */
public class CommandServer implements Runnable
{
    public void run()
    {
        int listenPort;
        ServerSocket server = null;
        try
        {
            listenPort = Configs.getInt("server.command.port", 1212);
            server = new ServerSocket(listenPort, 1000, InetAddress.getByName("0.0.0.0"));
            Log.debug("指令服务器监听于: " + listenPort);
            while (true)
            {
                Socket client = server.accept();
                if (Thread.interrupted())
                {
                    client.close();
                    break;
                }
                CommandSession session = new CommandSession(client);
                session.start();
            }
            server.close();
        }
        catch(Exception e)
        {
            Log.error(e);
        }
    }
}
