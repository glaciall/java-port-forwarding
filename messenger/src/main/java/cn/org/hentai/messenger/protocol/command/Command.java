package cn.org.hentai.messenger.protocol.command;

/**
 * Created by Expect on 2018/3/5.
 */
public abstract class Command
{
    public static final int CODE_AUTHENTICATE = 0x01;           // 身份验证包
    public static final int CODE_TEST = 0x02;                   // 连接测试包
    public static final int CODE_START_FORWARD = 0x03;          // 开始转发

    int code;

    public void setCode(int code)
    {
        this.code = code & 0x03ff;
    }

    public int getCode()
    {
        return this.code;
    }

    public abstract byte[] getBytes();
}
