package cn.org.hentai.server.proxy;

import cn.org.hentai.server.dao.HostDAO;
import cn.org.hentai.server.model.Host;
import cn.org.hentai.server.proxy.command.Command;
import cn.org.hentai.server.util.BeanUtils;
import cn.org.hentai.server.util.Log;
import cn.org.hentai.server.util.NonceStr;
import cn.org.hentai.server.util.ByteUtils;

import java.io.InputStream;
import java.io.OutputStream;
import java.net.Socket;
import java.util.LinkedList;

/**
 * Created by Expect on 2018/1/25.
 */
public class CommandSession extends SocketSession
{
    HostDAO hostDAO;
    Host host = null;
    Socket client = null;
    long lastActiveTime = System.currentTimeMillis();
    LinkedList<Command> commands = new LinkedList<Command>();                     // 待下发的指令

    public CommandSession(Socket client)
    {
        this.client = client;
        hostDAO = BeanUtils.create(HostDAO.class);
    }

    @Override
    protected void converse() throws Exception
    {
        InputStream inputStream = this.client.getInputStream();
        OutputStream outputStream = this.client.getOutputStream();

        // 先读取一个包，确定一下主机端的身份
        host = authenticate(inputStream, outputStream);
        CommandServer.register(host, this);

        while (true)
        {
            // 测试连接的可用性
            testConnection(inputStream, outputStream);

            // 是否有需要下发的指令？
            sendCommand(inputStream, outputStream);

            Thread.sleep(10);
        }
    }

    /**
     * 下发指令，一次只下发一个指令
     * @param inputStream
     * @param outputStream
     */
    private void sendCommand(InputStream inputStream, OutputStream outputStream) throws Exception
    {
        Command cmd = commands.peekFirst();
        if (null == cmd) return;
        byte[] packet = Packet.create(host.getId(), Constants.ENCRYPT_TYPE_DES, cmd.getCode(), cmd.getBytes(), host.getAccesstoken());
        outputStream.write(packet);
        Packet.read(inputStream);
        lastActiveTime = System.currentTimeMillis();
    }

    /**
     * 下发一个无意义的数据包，进行连接测试
     * @param inputStream
     * @param outputStream
     */
    private void testConnection(InputStream inputStream, OutputStream outputStream) throws Exception
    {
        if (System.currentTimeMillis() - lastActiveTime < 1000 * 10) return;
        byte[] data = NonceStr.generate(32).getBytes();
        byte[] packet = Packet.create(host.getId(), Constants.ENCRYPT_TYPE_DES, Constants.COMMAND_TEST_CONNECTION, data, host.getAccesstoken());
        outputStream.write(packet);
        Packet.read(inputStream);
        // lastActiveTime = System.currentTimeMillis();
    }

    /**
     * 读取一个数据包，进行身份验证
     * @param inputStream
     * @param outputStream
     * @return Host
     * @throws Exception
     */
    private Host authenticate(InputStream inputStream, OutputStream outputStream) throws Exception
    {
        byte[] packet = Packet.read(inputStream, true);
        System.out.println("Test XXXX");
        Log.debug("Recv: " + ByteUtils.toString(packet));
        int hostId = Packet.getHostId(packet);
        Host host = hostDAO.getById(hostId);
        if (null == host) throw new RuntimeException("no such host: " + hostId);
        Packet.getData(packet, host.getAccesstoken());
        outputStream.write(Packet.create(hostId, Constants.ENCRYPT_TYPE_DES, Constants.COMMAND_AUTHENTICATION, NonceStr.generate(32).getBytes(), host.getAccesstoken()));
        return host;
    }

    @Override
    protected void release()
    {
        try { this.client.close(); } catch(Exception e) { }
        try { CommandServer.release(host); } catch(Exception e) { }
    }
}
