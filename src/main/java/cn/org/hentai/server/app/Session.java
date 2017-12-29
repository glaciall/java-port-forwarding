package cn.org.hentai.server.app;

import cn.org.hentai.server.util.Configs;
import cn.org.hentai.server.util.Log;

import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.net.Socket;
import java.net.SocketTimeoutException;

/**
 * Created by matrixy on 2017/12/29.
 */
public abstract class Session extends Thread
{
    private Socket socket;
    protected InputStream reader;
    private OutputStream writer;

    public Session(Socket socket) throws Exception
    {
        this.socket = socket;
        socket.setSoTimeout(Configs.getInt("session.proxy.timeout", 2000));
        this.reader = socket.getInputStream();
        this.writer = socket.getOutputStream();
    }

    protected abstract void onReceive(byte[] data);

    /**
     * 主动下发
     * @param data
     * @return
     * @throws IOException
     */
    protected byte[] send(byte[] data) throws Exception
    {
        writer.write(data);
        return readPacket();
    }

    /**
     * 回应上发上来的数据包
     * @param data
     * @throws IOException
     */
    protected void reply(byte[] data) throws IOException
    {
        writer.write(data);
    }

    /**
     * 根据预定义的数据包结构读取整个数据包
     * @return
     */
    protected byte[] readPacket() throws Exception
    {
        return null;
    }

    protected void work() throws Exception
    {
        // 是否有上行数据包？
        byte[] packet = readPacket();
        if (packet != null) onReceive(packet);

        // 如果有，交给onReceive


        // 如果到达心跳时间则下发心跳包
    }

    public void run()
    {
        while (true)
        {
            try
            {
                work();
                Thread.sleep(100);
            }
            catch(Exception e)
            {
                Log.error(e);
                return;
            }
        }
    }
}
