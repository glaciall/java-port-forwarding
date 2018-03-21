package cn.org.hentai.messenger.protocol;

import cn.org.hentai.messenger.util.Configs;
import cn.org.hentai.messenger.util.Log;

import java.io.InputStream;
import java.io.OutputStream;
import java.net.InetAddress;
import java.net.InetSocketAddress;
import java.net.Socket;

/**
 * Created by matrixy on 2018/3/21.
 * 负责主机端的TCP转发服务
 */
public class ForwardWorker implements Runnable
{
    // 服务器端给出的连接请求流水号
    private int seqId = 0;
    // 服务器端给出的需要转发的TCP端口号
    private int port = 0;

    public ForwardWorker(int forwardSeqId, int port)
    {
        this.seqId = forwardSeqId;
        this.port = port;
    }

    // 开始转发
    private void forward() throws Exception
    {
        // TODO: 数据包缓冲区的大小自动调整
        byte[] localBuf = new byte[4096];
        byte[] serverBuf = new byte[4096];
        Socket server = new Socket(Configs.get("server.addr"), Configs.getInt("server.forward.port", 1111));
        Socket local = new Socket(InetAddress.getByName("localhost"), this.port);
        server.setSoTimeout(1000 * 60);
        local.setSoTimeout(1000 * 60);
        InputStream serverIs = server.getInputStream(), localIs = local.getInputStream();
        OutputStream serverOs = server.getOutputStream(), localOs = local.getOutputStream();
        try
        {
            while (true)
            {
                int localBufLength = localIs.available();
                int serverBufLength = serverIs.available();
                if (localBufLength > 0)
                {
                    localIs.read(localBuf, 0, localBufLength);
                    serverOs.write(localBuf, 0, localBufLength);
                }
                if (serverBufLength > 0)
                {
                    serverIs.read(serverBuf, 0, serverBufLength);
                    localOs.write(serverBuf, 0, serverBufLength);
                }
            }
        }
        finally
        {
            try { serverIs.close(); } catch(Exception e) {}
            try { localIs.close(); } catch(Exception e) {}
            try { serverOs.close(); } catch(Exception e) {}
            try { localOs.close(); } catch(Exception e) {}
            try { server.close(); } catch(Exception e) {}
            try { local.close(); } catch(Exception e) {}
        }
    }

    public void run()
    {
        try
        {
            forward();
        }
        catch(Exception e)
        {
            Log.error(e);
            // throw new RuntimeException(e);
        }
    }
}
