package cn.org.hentai.messenger.app;

import cn.org.hentai.messenger.worker.CommandListener;

/**
 * Created by matrixy on 2018/3/21.
 */
public class HostApp
{
    public static void main(String[] args) throws Exception
    {
        // 开启指令待命线程
        new Thread(new CommandListener()).start();

        // TODO: 注册线程异常拦截处理器
    }
}
