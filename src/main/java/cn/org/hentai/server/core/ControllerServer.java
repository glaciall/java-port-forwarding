package cn.org.hentai.server.core;

import cn.org.hentai.server.util.Configs;
import cn.org.hentai.server.util.Log;
import cn.org.hentai.util.ByteUtils;

import java.io.InputStream;
import java.io.OutputStream;
import java.net.ServerSocket;
import java.net.Socket;
import java.util.HashMap;
import java.util.Map;

/**
 * Created by matrixy on 2018/1/5.
 * 控制服务器，负责与主机间的认证、指令传达等
 */
public class ControllerServer extends Thread
{
    Map<Long, ControllerSession> sessions = new HashMap<Long, ControllerSession>();

    protected synchronized void register(long id)
    {
        sessions.put(id, null);
    }

    protected synchronized void unregister(long id)
    {
        sessions.remove(id);
    }

    public void run()
    {
        try
        {
            ServerSocket server = new ServerSocket(Configs.getInt("server.controller.port", 1122), Configs.getInt("server.controller.concurrency", 1000));
            while (true)
            {
                Socket client = server.accept();
                new ControllerSession(this, client).start();
            }
        }
        catch(Exception e)
        {
            Log.error(e);
        }
    }

    static class ControllerSession extends Thread
    {
        Socket client = null;
        ControllerServer server = null;
        long clientId = 0L;

        public ControllerSession(ControllerServer server, Socket client)
        {
            this.client = client;
            this.server = server;
        }

        public void run()
        {
            InputStream reader = null;
            OutputStream writer = null;
            try
            {
                reader = client.getInputStream();
                writer = client.getOutputStream();

                byte[] datagram = Datagram.read(reader);
                clientId = ByteUtils.getLong(datagram, 7, 4);
                String key = "VJ0P927Y893PUFGHVAUOPY43Y87IUDHJK";

                Packet packet = Packet.parse(datagram, key);
                server.register(clientId);

                while (true)
                {
                    // 1. 有没有要下发的指令？

                    // 2. 发个探测包下去
                }
            }
            catch(Exception e)
            {
                Log.error(e);
                server.unregister(clientId);
            }
        }
    }
}
