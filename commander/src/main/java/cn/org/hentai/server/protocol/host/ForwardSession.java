package cn.org.hentai.server.protocol.host;

import cn.org.hentai.server.protocol.commander.HostConnectionManager;
import cn.org.hentai.server.util.ByteUtils;
import cn.org.hentai.server.util.Configs;
import cn.org.hentai.server.util.Log;

import java.io.InputStream;
import java.net.ServerSocket;
import java.net.Socket;

/**
 * Created by matrixy on 2018/3/22.
 */
public class ForwardSession implements Runnable
{
    private Socket hostConnection;
    public ForwardSession(Socket hostConnection)
    {
        this.hostConnection = hostConnection;
    }

    public void run()
    {
        try
        {
            // 读取4字节，确定一下流水号
            InputStream inputStream = hostConnection.getInputStream();
            byte[] data = new byte[4];
            if (inputStream.read(data) != 4) throw new RuntimeException("读取流水号失败，无法关联会话");
            int sequenceId = ByteUtils.toInt(data);
            Log.debug("Forward for: " + sequenceId);
            HostConnectionManager.getInstance().attach(sequenceId, hostConnection);
        }
        catch(Exception e)
        {
            Log.error(e);
        }
    }
}
