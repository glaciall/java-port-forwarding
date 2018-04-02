package cn.org.hentai.server.protocol.commander;

import cn.org.hentai.server.model.Host;
import cn.org.hentai.server.model.Port;
import cn.org.hentai.server.protocol.proxy.ProxySession;
import cn.org.hentai.server.util.Log;
import cn.org.hentai.server.util.NonceStr;

import java.net.Socket;
import java.util.HashMap;
import java.util.Map;

/**
 * Created by matrixy on 2018/3/22.
 */
public class HostConnectionManager
{
    // 主机会话表
    Map<Integer, CommandSession> hostSessions = new HashMap<Integer, CommandSession>();

    // 客户端请求转发表
    Map<Integer, ProxySession> proxySessions = new HashMap<Integer, ProxySession>();

    // 是否在线
    public boolean isOnline(int hostId)
    {
        return hostSessions.containsKey(hostId);
    }

    public String getHostIp(int hostId)
    {
        if (!hostSessions.containsKey(hostId)) return null;
        else return hostSessions.get(hostId).getHostIP();
    }

    // 主机端连接时注册登记
    public void register(int hostId, CommandSession session)
    {
        // Log.debug("Register: " + hostId);
        // if (hostSessions.containsKey(hostId)) throw new RuntimeException("主机端己连接");
        synchronized (hostSessions)
        {
            hostSessions.put(hostId, session);
        }
    }

    // 主机端的连接断开时取消注册
    public void unregister(int hostId, CommandSession commandSession)
    {
        synchronized (hostSessions)
        {
            CommandSession session = hostSessions.get(hostId);
            if (session != commandSession) return;
            hostSessions.remove(hostId);
        }
    }

    // 请求开始转发，返回本次转发会话的通信密钥
    public String requestForward(ProxySession proxySession, Port port)
    {
        Log.debug("客户端[" + port.getId() + "]: 请求转发 " + port.getHostPort() + " 至 " + port.getListenPort());
        CommandSession commandSession = null;
        synchronized (hostSessions)
        {
            if (!hostSessions.containsKey(port.getHostId())) throw new RuntimeException("主机端尚未连接");
            commandSession = hostSessions.get(port.getHostId());
        }
        if (null == hostSessions) throw new RuntimeException("当前无提供服务的主机连接");
        int seq = getSequence();
        synchronized (proxySessions)
        {
            proxySessions.put(seq, proxySession);
        }
        String nonce = NonceStr.generate(64);
        commandSession.requestForward(seq, nonce, port);
        return nonce;
    }

    // 关联到客户端的连接会话上来
    public void attach(int sequenceId, Socket hostConnection)
    {
        ProxySession session = null;
        synchronized (proxySessions)
        {
            session = proxySessions.remove(sequenceId);
        }
        if (null == session) throw new RuntimeException("无此转发请求的客户端会话");
        session.attach(hostConnection);
    }

    static int sequenceId = 0;
    private synchronized int getSequence()
    {
        return ++sequenceId;
    }

    static HostConnectionManager manager = null;
    public static synchronized HostConnectionManager getInstance()
    {
        if (null == manager) manager = new HostConnectionManager();
        return manager;
    }
}
