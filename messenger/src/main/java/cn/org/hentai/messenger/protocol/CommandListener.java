package cn.org.hentai.messenger.protocol;

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
    private int hostId;
    private int serverPort;
    private int iowaitTimeout;
    private String accessToken;
    private String serverAddr;
    private long lastExchangeTime = 0L;
    public CommandListener()
    {
        hostId = Configs.getInt("host.id", 0);
        serverPort = Configs.getInt("server.command.port", 1122);
        accessToken = Configs.get("host.accesstoken");
        serverAddr = Configs.get("server.addr");
        iowaitTimeout = Configs.getInt("server.test-packet.timeout", 20000);

        Log.info("主机ID: " + hostId);
        Log.info("访问令牌: " + accessToken);
        Log.info("服务器地址: " + serverAddr);
        Log.info("服务器端口: " + serverPort);
    }

    // 待命
    private void listen() throws Exception
    {
        Socket socket = new Socket(serverAddr, serverPort);
        socket.setSoTimeout(iowaitTimeout);
        InputStream inputStream = socket.getInputStream();
        OutputStream outputStream = socket.getOutputStream();

        // 1. 先向服务器端上发一个认证数据包
        Command cmd = new AuthCommand();
        byte[] resp, packet = Packet.create(hostId, Packet.ENCRYPT_TYPE_DES, cmd, accessToken);
        outputStream.write(packet);
        outputStream.flush();
        Packet.read(inputStream, true);
        Log.debug("己连接到服务器端...");
        lastExchangeTime = System.currentTimeMillis();

        // 2. 等待服务器的心跳测试包或是指令包
        while (true)
        {
            resp = Packet.read(inputStream,true);
            if (null == resp)
            {
                if (System.currentTimeMillis() - lastExchangeTime > iowaitTimeout) break;
                continue;
            }

            lastExchangeTime = System.currentTimeMillis();

            // 处理几种数据包
            // Log.debug("Recv: " + ByteUtils.toString(resp));
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
                String nonce = new String(data, 8, 64);
                int len = (int)(data[8 + 64] & 0xff);
                String hostIp = new String(data, 8 + 64 + 1, len);
                Log.debug("请求转发: " + hostIp + ":" + port);
                ForwardWorker worker = new ForwardWorker(seqId, hostIp, port, nonce);
                SessionManager.getInstance().register(worker);
                worker.start();
                outputStream.write(Packet.create(hostId, Packet.ENCRYPT_TYPE_DES, new ForwardRespCommand(), accessToken));
            }
        }
        Log.debug("己断开连接...");
        try { inputStream.close(); } catch(Exception e) { }
        try { outputStream.close(); } catch(Exception e) { }
        try { socket.close(); } catch(Exception e) { }
    }

    public void run()
    {
        while (true)
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
            try
            {
                Thread.sleep(4000);
            }
            catch(Exception e) { }
        }
    }
}
