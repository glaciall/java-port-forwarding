package cn.org.hentai.messenger.protocol;

import cn.org.hentai.messenger.util.Log;
import cn.org.hentai.messenger.util.VLinkedList;

/**
 * Created by matrixy on 2018/3/23.
 * 会话线程管理器，超时处理
 */
public class SessionManager
{
    Object lock = new Object();
    VLinkedList<ForwardWorker> sessions = new VLinkedList<ForwardWorker>();
    VLinkedList.ListAwalker<ForwardWorker> listAwalker = new VLinkedList.ListAwalker<ForwardWorker>()
    {
        @Override
        public void test(ForwardWorker session)
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
    public void register(ForwardWorker session)
    {
        synchronized (lock)
        {
            sessions.add(session);
        }
    }

    // 会话线程主动注销
    public void unregister(ForwardWorker session)
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
    public static void startIOTimeoutMonitor()
    {
        new Thread(new Runnable()
        {
            @Override
            public void run()
            {
                while (true)
                {
                    SessionManager.getInstance().clearSessions();
                    try { Thread.sleep(1000); } catch(Exception e) { }
                }
            }
        }).start();
    }

    static SessionManager manager = null;
    public static synchronized SessionManager getInstance()
    {
        if (manager == null) manager = new SessionManager();
        return manager;
    }
}
