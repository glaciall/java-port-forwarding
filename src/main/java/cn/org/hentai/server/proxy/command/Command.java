package cn.org.hentai.server.proxy.command;

/**
 * Created by Expect on 2018/3/5.
 */
public abstract class Command
{
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
