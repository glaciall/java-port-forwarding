package cn.org.hentai.messenger.protocol.command;

import cn.org.hentai.messenger.util.NonceStr;

/**
 * Created by matrixy on 2018/3/21.
 */
public class AuthCommand extends Command
{
    public AuthCommand()
    {
        this.setCode(Command.CODE_AUTHENTICATE);
    }

    @Override
    public byte[] getBytes()
    {
        return "authenticate".getBytes();
    }
}
