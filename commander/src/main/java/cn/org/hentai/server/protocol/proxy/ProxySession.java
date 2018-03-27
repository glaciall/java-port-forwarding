package cn.org.hentai.server.protocol.proxy;

import cn.org.hentai.server.model.Port;
import cn.org.hentai.server.protocol.SocketSession;
import cn.org.hentai.server.protocol.commander.HostConnectionManager;
import cn.org.hentai.server.util.ByteUtils;
import cn.org.hentai.server.util.DES;
import cn.org.hentai.server.util.Log;

import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.net.Socket;
import java.net.SocketTimeoutException;

/**
 * Created by Expect on 2018/1/25.
 */
public class ProxySession extends SocketSession
{
    private Port port;                      // 主机端ID
    private Socket clientConnection;        // 客户端连接
    private Socket hostConnection;          // 被代理的主机端连接
    private int connectTimeout = 30000;     // 等待主机端的连接超时时长（秒）
    private long lastExchangeTime = 0;      // 主机与客户端之间最后交换数据包的时间
    private int iowaitTimeout = 30000;      // 网络IO等待超时时长（秒）
    private String nonce = null;            // 本次转发会话的数据加解密密钥

    public ProxySession(Port port, Socket clientConnection)
    {
        this.port = port;
        this.clientConnection = clientConnection;
        this.connectTimeout = port.getConnectTimeout() * 1000;
        this.iowaitTimeout = port.getSoTimeout() * 1000;
        this.setName("Proxy[" + port.getListenPort() + " - " + port.getHostPort() + "]: " + clientConnection.getRemoteSocketAddress());
    }

    // 与主机端的连接关联起来
    public void attach(Socket hostConnection)
    {
        this.hostConnection = hostConnection;
    }

    @Override
    public boolean timedout()
    {
        if (lastExchangeTime == 0) return false;
        return System.currentTimeMillis() - lastExchangeTime > iowaitTimeout;
    }

    @Override
    protected void converse() throws Exception
    {
        // 通知commandserver下发一个开始转发包到主机端
        this.nonce = HostConnectionManager.getInstance().requestForward(this, port);

        Log.debug("等待主机端连接...");
        long stime = System.currentTimeMillis();
        while (this.hostConnection == null)
        {
            if (System.currentTimeMillis() - stime > connectTimeout)
            {
                throw new SocketTimeoutException("等待主机端连接超时");
            }
            sleep(10);
        }
        Log.debug("主机端己连接");

        lastExchangeTime = System.currentTimeMillis();

        this.clientConnection.setSendBufferSize(1024 * 64);
        this.clientConnection.setReceiveBufferSize(1024 * 64);
        this.hostConnection.setSendBufferSize(1024 * 64);
        this.hostConnection.setReceiveBufferSize(1024 * 64);

        // 开始转发
        InputStream clientIS = this.clientConnection.getInputStream();
        OutputStream clientOS = this.clientConnection.getOutputStream();
        InputStream hostIS = this.hostConnection.getInputStream();
        OutputStream hostOS = this.hostConnection.getOutputStream();

        while (true)
        {
            int clientBufLength = clientIS.available();
            if (clientBufLength > 0)
            {
                // 客户端到主机端，需要加密后转发
                encryptAndTransfer(clientIS, hostOS, clientBufLength);
            }
            int hostBufLength = hostIS.available();
            if (hostBufLength > 0)
            {
                // 主机端到客户端，需要解密后转发
                decryptAndTransfer(hostIS, clientOS, hostBufLength);
            }
            sleep(10);
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

    private void sleep(int ms)
    {
        try
        {
            Thread.sleep(ms);
        }
        catch(Exception e) { }
    }

    @Override
    protected void release()
    {
        try { clientConnection.close(); } catch(Exception e) { }
        try { hostConnection.close(); } catch(Exception e) { }
        super.release();
    }

    public void terminate()
    {
        release();
        super.terminate();
    }
}
