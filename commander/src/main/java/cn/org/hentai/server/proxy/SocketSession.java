package cn.org.hentai.server.proxy;

import cn.org.hentai.server.util.Log;

/**
 * Created by Expect on 2018/1/25.
 */
public abstract class SocketSession implements Runnable
{
    // 连接对话
    protected abstract void converse() throws Exception;

    // 资源释放
    protected abstract void release();

    public final void run()
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
