package cn.org.hentai.server.protocol.proxy;

import cn.org.hentai.server.model.Port;
import cn.org.hentai.server.protocol.SocketSession;
import cn.org.hentai.server.protocol.commander.HostConnectionManager;
import cn.org.hentai.server.util.ByteUtils;
import cn.org.hentai.server.util.Log;

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
    private int connectTimeout = 30;        // 等待主机端的连接超时时长（秒）

    public ProxySession(Port port, Socket clientConnection, int connectTimeout)
    {
        this.port = port;
        this.clientConnection = clientConnection;
    }

    // 与主机端的连接关联起来
    public void attach(Socket hostConnection)
    {
        this.hostConnection = hostConnection;
    }

    @Override
    protected void converse() throws Exception
    {
        // TODO: 通知commandserver下发一个开始转发包到主机端
        HostConnectionManager.getInstance().requestForward(this, port);

        Log.debug("Wait for host connection...");
        long stime = System.currentTimeMillis();
        while (this.hostConnection == null)
        {
            if (System.currentTimeMillis() - stime > connectTimeout * 1000)
            {
                throw new SocketTimeoutException("等待主机端连接超时");
            }
            sleep(10);
        }
        Log.debug("Host connected...");

        // 开始转发
        InputStream clientIS = this.clientConnection.getInputStream();
        OutputStream clientOS = this.clientConnection.getOutputStream();
        InputStream hostIS = this.hostConnection.getInputStream();
        OutputStream hostOS = this.hostConnection.getOutputStream();

        while (true)
        {
            byte[] data = new byte[40960];
            int clientBufLength = clientIS.available();
            if (clientBufLength > 0)
            {
                // if (clientBufLength > data.length) data = new byte[clientBufLength];
                // clientIS.read(data, 0, clientBufLength);
                // hostOS.write(data, 0, clientBufLength);
                // hostOS.flush();
                transfer(clientIS, hostOS, clientBufLength);
            }
            int hostBufLength = hostIS.available();
            if (hostBufLength > 0)
            {
                // if (hostBufLength > data.length) data = new byte[hostBufLength];
                // hostIS.read(data, 0, hostBufLength);
                // clientOS.write(data, 0, hostBufLength);
                // clientOS.flush();;
                transfer(hostIS, clientOS, hostBufLength);
            }
            sleep(10);
        }
    }

    // 数据包的转发
    private void transfer(InputStream from, OutputStream to, int byteCount) throws IOException
    {
        int len = 4096;
        byte[] buf = new byte[4096];
        for (int i = 0; i < byteCount; i += len)
        {
            len = from.read(buf, 0, Math.min(4096, byteCount - i));
            Log.debug("Trans: " + ByteUtils.toString(buf, len));
            to.write(buf, 0, len);
        }
        to.flush();
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
    }
}
