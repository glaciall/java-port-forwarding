package cn.org.hentai.server.proxy;

import java.util.concurrent.ConcurrentLinkedDeque;

/**
 * Created by Expect on 2018/1/25.
 */
public final class ConnectionPool
{
    private static ConcurrentLinkedDeque<Connection> connections;

    public static void put(Connection conn)
    {
        connections.add(conn);
    }

    // 用于查找连接，比如有连接连到代理端口时，需要由
}
