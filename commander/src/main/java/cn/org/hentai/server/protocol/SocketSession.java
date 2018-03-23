package cn.org.hentai.server.protocol;

import cn.org.hentai.server.util.Log;

/**
 * Created by Expect on 2018/1/25.
 */
public abstract class SocketSession extends Thread
{
    // 连接对话
    protected abstract void converse() throws Exception;

    // 资源释放
    protected void release()
    {
        SocketSessionManager.getInstance().unregister(this);
    }

    // 是否超时
    public boolean timedout()
    {
        return false;
    }

    // 终止会话
    public void terminate()
    {
        try
        {
            this.stop();
        }
        catch(Exception e) { }
        Log.debug("Terminated: " + this.getName());
    }

    public void run()
    {
        try
        {
            converse();
        }
        catch(Exception e)
        {
            Log.error(e);
        }
        finally
        {
            release();
        }
    }
}
