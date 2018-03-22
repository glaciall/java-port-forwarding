package cn.org.hentai.messenger.protocol.command;

/**
 * Created by matrixy on 2018/3/21.
 */
public class ForwardRespCommand extends Command
{
    public ForwardRespCommand()
    {
        this.setCode(Command.CODE_START_FORWARD);
    }

    @Override
    public byte[] getBytes()
    {
        return "proxy".getBytes();
    }
}
