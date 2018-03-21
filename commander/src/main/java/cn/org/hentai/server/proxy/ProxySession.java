package cn.org.hentai.server.proxy;

import cn.org.hentai.server.dao.HostDAO;
import cn.org.hentai.server.dao.PortDAO;
import cn.org.hentai.server.model.Port;
import org.springframework.beans.factory.annotation.Autowired;

import java.io.InputStream;
import java.io.OutputStream;
import java.net.Socket;
import java.net.SocketTimeoutException;

/**
 * Created by Expect on 2018/1/25.
 */
public class ProxySession extends SocketSession
{
    private int listenPort;                 // 此连接的监听端口
    private Socket client;                  // 客户端连接
    private Socket host;                    // 被代理的主机端连接
    private int connectTimeout = 30;        // 等待主机端的连接超时时长（秒）

    @Autowired
    HostDAO hostDAO;

    @Autowired
    PortDAO portDAO;

    public ProxySession(Socket client, int connectTimeout)
    {
        this.client = client;
    }

    // 与主机端的连接关联起来
    public void attachTo(Socket host)
    {
        this.host = host;
    }

    @Override
    protected void converse() throws Exception
    {
        // 确定我的主机与端口
        Port port = portDAO.getByPort(listenPort);

        // TODO: 通知commandserver下发一个开始转发包到主机端


        long stime = System.currentTimeMillis();
        while (this.host == null)
        {
            if (System.currentTimeMillis() - stime > connectTimeout * 1000)
            {
                throw new SocketTimeoutException("等待客户端连接超时");
            }
            sleep(10);
        }

        // 开始转发
        InputStream clientIS = this.client.getInputStream();
        OutputStream clientOS = this.client.getOutputStream();
        InputStream hostIS = this.host.getInputStream();
        OutputStream hostOS = this.host.getOutputStream();

        while (true)
        {
            byte[] data = new byte[4096];
            int clientBufLength = clientIS.available();
            if (clientBufLength > 0)
            {
                if (clientBufLength > data.length) data = new byte[clientBufLength];
                clientIS.read(data, 0, clientBufLength);
                hostOS.write(data, 0, clientBufLength);
            }
            int hostBufLength = hostIS.available();
            if (hostBufLength > 0)
            {
                if (hostBufLength > data.length) data = new byte[hostBufLength];
                hostIS.read(data, 0, hostBufLength);
                clientOS.write(data, 0, hostBufLength);
            }
            sleep(10);
        }
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
        try { client.close(); } catch(Exception e) { }
        try { host.close(); } catch(Exception e) { }
    }
}
