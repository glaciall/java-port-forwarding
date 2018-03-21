package cn.org.hentai.server.proxy.command;

import cn.org.hentai.server.util.ByteUtils;

/**
 * Created by Expect on 2018/3/5.
 */
public class StartForwardCommand extends Command
{
    // 主机端应该连接到哪个端口来
    private int serverPort;

    // 连接到服务器端时的校验字符串
    private String authcode;

    public void setAuthcode(String authcode)
    {
        this.authcode = authcode;
    }

    public String getAuthcode()
    {
        return this.authcode;
    }

    @Override
    public byte[] getBytes()
    {
        byte[] data = authcode.getBytes();
        return ByteUtils.concat(ByteUtils.toBytes(serverPort), data);
    }
}
