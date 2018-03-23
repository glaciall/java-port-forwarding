package cn.org.hentai.messenger.protocol;

import cn.org.hentai.messenger.util.ByteUtils;
import cn.org.hentai.messenger.util.Configs;
import cn.org.hentai.messenger.util.Log;

import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.net.InetAddress;
import java.net.Socket;

/**
 * Created by matrixy on 2018/3/21.
 * 负责主机端的TCP转发服务
 */
public class ForwardWorker implements Runnable
{
    // 服务器端给出的连接请求流水号
    private int sequenceId = 0;
    // 服务器端给出的需要转发的TCP端口号
    private int port = 0;

    public ForwardWorker(int forwardSeqId, int port)
    {
        this.sequenceId = forwardSeqId;
        this.port = port;
    }

    // 开始转发
    private void forward() throws Exception
    {
        Socket server = new Socket(Configs.get("server.addr"), Configs.getInt("server.forward.port", 11221));
        Socket local = new Socket(InetAddress.getByName("localhost"), this.port);
        server.setSoTimeout(1000 * 60);
        local.setSoTimeout(1000 * 60);
        InputStream serverIs = server.getInputStream(), localIs = local.getInputStream();
        OutputStream serverOs = server.getOutputStream(), localOs = local.getOutputStream();
        try
        {
            // 先发送4字节的流水号
            serverOs.write(ByteUtils.toBytes(this.sequenceId));
            serverOs.flush();

            // 开始转发
            while (true)
            {
                int localBufLength = localIs.available();
                int serverBufLength = serverIs.available();
                if (localBufLength > 0)
                {
                    transfer(localIs, serverOs, localBufLength);
                }
                if (serverBufLength > 0)
                {
                    transfer(serverIs, localOs, serverBufLength);
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

    // 数据包的转发
    private void transfer(InputStream from, OutputStream to, int byteCount) throws IOException
    {
        int len = 4096;
        byte[] buf = new byte[4096];
        for (int i = 0; i < byteCount; i += len)
        {
            len = from.read(buf, 0, Math.min(4096, byteCount - i));
            to.write(buf, 0, len);
        }
        to.flush();
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
