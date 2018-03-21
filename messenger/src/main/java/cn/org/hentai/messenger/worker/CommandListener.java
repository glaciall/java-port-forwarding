package cn.org.hentai.messenger.worker;

import cn.org.hentai.messenger.protocol.ForwardWorker;
import cn.org.hentai.messenger.protocol.Packet;
import cn.org.hentai.messenger.protocol.command.AuthCommand;
import cn.org.hentai.messenger.protocol.command.Command;
import cn.org.hentai.messenger.protocol.command.ForwardRespCommand;
import cn.org.hentai.messenger.protocol.command.TestRespCommand;
import cn.org.hentai.messenger.util.ByteUtils;
import cn.org.hentai.messenger.util.Configs;
import cn.org.hentai.messenger.util.Log;

import java.io.InputStream;
import java.io.OutputStream;
import java.net.Socket;

/**
 * Created by matrixy on 2018/3/21.
 * 等待服务器端下发的指令
 */
public class CommandListener implements Runnable
{
    // 待命
    private void listen() throws Exception
    {
        int hostId = Configs.getInt("host.id", 0);
        String accessToken = Configs.get("host.accesstoken");

        Log.info("HostId: " + hostId);
        Log.info("AccessToken: " + accessToken);
        Log.info("ServerAddr: " + Configs.get("server.addr"));
        Log.info("ServerPort: " + Configs.getInt("server.command.port", 1122));

        Socket socket = new Socket(Configs.get("server.addr"), Configs.getInt("server.command.port", 1122));
        // socket.setSoTimeout(Configs.getInt("server.test-packet.timeout", 20000));
        socket.setSoTimeout(1000);
        InputStream inputStream = socket.getInputStream();
        OutputStream outputStream = socket.getOutputStream();

        // 1. 先向服务器端上发一个认证数据包
        Command cmd = new AuthCommand();
        byte[] resp, packet = Packet.create(hostId, Packet.ENCRYPT_TYPE_DES, cmd, accessToken);
        outputStream.write(packet);
        outputStream.flush();
        resp = Packet.read(inputStream, true);
        Log.info("Connected to server...");

        // 2. 等待服务器的心跳测试包或是指令包
        while (true)
        {
            resp = Packet.read(inputStream);
            if (null == resp)
            {
                Thread.sleep(100);
                continue;
            }

            // 处理几种数据包
            Log.debug("Recv: " + ByteUtils.toString(resp));
            int code = Packet.getCommand(resp);
            if (code == Command.CODE_TEST)
            {
                // 简单回应一下
                outputStream.write(Packet.create(hostId, Packet.ENCRYPT_TYPE_DES, new TestRespCommand(), accessToken));
            }
            else if (code == Command.CODE_START_FORWARD)
            {
                // 开启新的线程来转发数据包
                byte[] data = Packet.getData(resp, accessToken);
                int seqId = ByteUtils.getInt(data, 0, 4);
                int port = ByteUtils.getInt(data, 4, 4);
                new Thread(new ForwardWorker(seqId, port)).start();
                outputStream.write(Packet.create(hostId, Packet.ENCRYPT_TYPE_DES, new ForwardRespCommand(), accessToken));
            }
        }
    }

    public void run()
    {
        try
        {
            listen();
        }
        catch(Exception e)
        {
            Log.error(e);
            // throw new RuntimeException(e);
        }
    }
}
