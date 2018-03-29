package cn.org.hentai.messenger.app;

import cn.org.hentai.messenger.protocol.CommandListener;
import cn.org.hentai.messenger.protocol.SessionManager;

/**
 * Created by matrixy on 2018/3/21.
 */
public class HostApp
{
    public static void main(String[] args) throws Exception
    {
        // 开启指令待命线程
        new Thread(new CommandListener()).start();

        // 开启转发线程超时监控线程
        SessionManager.startIOTimeoutMonitor();
    }
}
