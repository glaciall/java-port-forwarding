package cn.org.hentai.server.proxy;

import cn.org.hentai.server.dao.HostDAO;
import cn.org.hentai.server.dao.PortDAO;
import cn.org.hentai.server.model.Host;
import cn.org.hentai.server.model.Port;
import org.springframework.beans.factory.annotation.Autowired;

import java.net.Socket;

/**
 * Created by Expect on 2018/1/25.
 */
public class ProxySession extends SocketSession
{
    private int listenPort;         // 此连接的监听端口
    private Socket client;          // 客户端连接
    private Socket host;            // 被代理的主机端连接

    @Autowired
    HostDAO hostDAO;

    @Autowired
    PortDAO portDAO;

    public ProxySession(Socket client, int port)
    {
        this.client = client;
        this.listenPort = port;
    }

    // 与主机端的连接关联起来
    public void attachTo(Socket host)
    {
        this.host = host;
    }

    // 初始化，得到连接后，通知指令服务去要求客户端开始端口代理
    // 得到连接后，将socket转交给自己


    @Override
    protected void converse()
    {
        // 确定我的主机与端口
        Port port = portDAO.getByPort(listenPort);

        // 让指令服务下发通知给主机端


        // 等待
    }

    @Override
    protected void release()
    {
        try { client.close(); } catch(Exception e) { }
        try { host.close(); } catch(Exception e) { }
    }
}
