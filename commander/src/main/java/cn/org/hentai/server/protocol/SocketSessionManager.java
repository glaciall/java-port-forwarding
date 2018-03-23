package cn.org.hentai.server.protocol;

import cn.org.hentai.server.protocol.proxy.ProxySession;
import cn.org.hentai.server.util.Log;
import cn.org.hentai.server.util.VLinkedList;

/**
 * Created by matrixy on 2018/3/24.
 * 管理代理转发会话的超时
 */
public class SocketSessionManager
{
    Object lock = new Object();
    VLinkedList<SocketSession> sessions = new VLinkedList<SocketSession>();
    VLinkedList.ListAwalker<SocketSession> listAwalker = new VLinkedList.ListAwalker<SocketSession>()
    {
        @Override
        public void test(SocketSession session)
        {
            // 如果会话己发生IO等待超时，则停止线程
            if (session.timedout())
            {
                Log.debug("Thread[" + session.getName() + "] timedout, stop it...");
                try
                {
                    session.terminate();
                }
                catch(Exception e)
                {
                    Log.error(e);
                }
                sessions.remove(session);
            }
        }
    };

    // 会话线程启动时登记
    public void register(SocketSession session)
    {
        synchronized (lock)
        {
            sessions.add(session);
        }
    }

    // 会话线程主动注销
    public void unregister(SocketSession session)
    {
        synchronized (lock)
        {
            sessions.remove(session);
        }
    }

    // 查找有无IO等待超时的线程，进行中断停止处理
    private void clearSessions()
    {
        synchronized (lock)
        {
            sessions.traverse(listAwalker);
        }
    }

    // 线程IO等待超时处理
    public void startTimeoutMonitor()
    {
        new Thread(new Runnable()
        {
            @Override
            public void run()
            {
                while (true)
                {
                    clearSessions();
                    try { Thread.sleep(1000); } catch(Exception e) { }
                }
            }
        }).start();
    }

    static SocketSessionManager manager = null;
    public static synchronized SocketSessionManager getInstance()
    {
        if (manager == null) manager = new SocketSessionManager();
        return manager;
    }
}
