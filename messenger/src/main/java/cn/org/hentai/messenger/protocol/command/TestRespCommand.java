package cn.org.hentai.messenger.protocol.command;

import cn.org.hentai.messenger.util.NonceStr;

/**
 * Created by matrixy on 2018/3/21.
 */
public class TestRespCommand extends Command
{
    public TestRespCommand()
    {
        this.setCode(Command.CODE_TEST);
    }

    @Override
    public byte[] getBytes()
    {
        return ("test:" + NonceStr.generate()).getBytes();
    }
}
