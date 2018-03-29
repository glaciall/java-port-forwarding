package cn.org.hentai.messenger.protocol;

import cn.org.hentai.messenger.util.ByteUtils;
import cn.org.hentai.messenger.util.Configs;
import cn.org.hentai.messenger.util.DES;
import cn.org.hentai.messenger.util.Log;

import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.net.InetAddress;
import java.net.Socket;

/**
 * Created by matrixy on 2018/3/21.
 * 负责主机端的TCP转发服务
 */
public class ForwardWorker extends Thread
{
    // 服务器端给出的连接请求流水号
    private int sequenceId = 0;

    // 服务器端给出的需要转发的主机IP或域名
    private String host = null;

    // 服务器端给出的需要转发的TCP端口号
    private int port = 0;

    // 服务器端给出的本次转发所使用的加密密钥
    private String nonce = null;

    // IO等待超时时长（毫秒）
    private int iowaitTimeout = 30000;

    // 最后交换数据包的时间
    private long lastExchangeTime = 0;

    private Socket server = null, local = null;

    public ForwardWorker(int forwardSeqId, String hostIp, int port, String nonce)
    {
        this.sequenceId = forwardSeqId;
        this.host = hostIp;
        this.port = port;
        this.nonce = nonce;
        this.setName("Forward-" + host + ":" + port);
        iowaitTimeout = Configs.getInt("timeout.iowait", 30000);
    }

    // 是否己经发生了IO等待超时
    public boolean timedout()
    {
        return System.currentTimeMillis() - lastExchangeTime > iowaitTimeout;
    }

    // 开始转发
    private void forward() throws Exception
    {
        lastExchangeTime = System.currentTimeMillis();
        server = new Socket(Configs.get("server.addr"), Configs.getInt("server.forward.port", 11221));
        local = new Socket(InetAddress.getByName(this.host), this.port);
        server.setSoTimeout(1000 * 60);
        local.setSoTimeout(1000 * 60);
        server.setSendBufferSize(1024 * 64);
        server.setReceiveBufferSize(1024 * 64);
        local.setSendBufferSize(1024 * 64);
        local.setReceiveBufferSize(1024 * 64);
        InputStream serverIs = server.getInputStream(), localIs = local.getInputStream();
        OutputStream serverOs = server.getOutputStream(), localOs = local.getOutputStream();
        try
        {
            // 先发送4字节的流水号
            serverOs.write(ByteUtils.toBytes(this.sequenceId));
            serverOs.flush();

            // 开始转发
            while (true)
            {
                int localBufLength = localIs.available();
                int serverBufLength = serverIs.available();
                if (localBufLength > 0)
                {
                    // 本地来的数据包是原始数据包，需要加密后发出
                    encryptAndTransfer(localIs, serverOs, localBufLength);
                }
                if (serverBufLength >= 7)
                {
                    // 服务端来的数据包是加密的，需要解密后发出
                    decryptAndTransfer(serverIs, localOs, serverBufLength);
                }
                if (localBufLength + serverBufLength == 0) Thread.sleep(1);
            }
        }
        finally
        {
            try { serverIs.close(); } catch(Exception e) {}
            try { localIs.close(); } catch(Exception e) {}
            try { serverOs.close(); } catch(Exception e) {}
            try { localOs.close(); } catch(Exception e) {}
            try { server.close(); } catch(Exception e) {}
            try { local.close(); } catch(Exception e) {}
        }
    }

    // 数据包的转发，解密后转发
    private void decryptAndTransfer(InputStream from, OutputStream to, int byteCount) throws Exception
    {
        int len = 4096;
        byte[] buf = new byte[4096];
        byteCount = Math.min(1024 * 64, byteCount);
        ByteArrayOutputStream baos = new ByteArrayOutputStream(byteCount + 64);
        // 先读4字节，确定内容长度
        from.read(buf, 0, 3);
        if ((buf[0] & 0xff) != 0xfa || (buf[1] & 0xff) != 0xfa || (buf[2] & 0xff) != 0xfa) throw new RuntimeException("错误的协议头");
        len = from.read(buf, 0, 4);
        if (len != 4) throw new RuntimeException("读取数据包长度失败");
        byteCount = ByteUtils.toInt(buf);
        for (int i = 0; i < byteCount; i += len)
        {
            len = from.read(buf, 0, Math.min(4096, byteCount - i));
            baos.write(buf, 0, len);
            // to.write(buf, 0, len);
        }
        buf = null;
        buf = DES.decrypt(baos.toByteArray(), this.nonce);
        // to.write(ByteUtils.toBytes(buf.length));
        to.write(buf);
        to.flush();
        lastExchangeTime = System.currentTimeMillis();
    }

    // 数据包的转发：加密后转发
    private void encryptAndTransfer(InputStream from, OutputStream to, int byteCount) throws Exception
    {
        int len = 4096;
        byte[] buf = new byte[4096];
        byteCount = Math.min(1024 * 64, byteCount);
        ByteArrayOutputStream baos = new ByteArrayOutputStream(byteCount + 64);
        for (int i = 0; i < byteCount; i += len)
        {
            len = from.read(buf, 0, Math.min(4096, byteCount - i));
            baos.write(buf, 0, len);
            // to.write(buf, 0, len);
        }
        buf = null;
        buf = DES.encrypt(baos.toByteArray(), this.nonce);
        to.write((byte)0xfa);
        to.write((byte)0xfa);
        to.write((byte)0xfa);
        to.write(ByteUtils.toBytes(buf.length));
        to.write(buf);
        to.flush();
        lastExchangeTime = System.currentTimeMillis();
    }

    public void terminate()
    {
        Log.debug("Terminate: " + this.getName());
        try { server.close(); } catch(Exception e) { }
        try { local.close(); } catch(Exception e) { }
        try { this.stop(); } catch(Exception e) { }
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
            // throw new RuntimeException(e);
        }
    }
}
