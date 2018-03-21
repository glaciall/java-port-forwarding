package cn.org.hentai.server.proxy;

import java.net.Socket;
import java.util.Date;

/**
 * Created by Expect on 2018/1/25.
 */
public class Connection
{
    private Socket socket;
    private long lastActiveTime;

    public Connection(Socket socket)
    {
        this.socket = socket;
        this.lastActiveTime = System.currentTimeMillis();
    }

    public Socket getSocket() {
        return socket;
    }

    public void setSocket(Socket socket) {
        this.socket = socket;
    }

    public long getLastActiveTime() {
        return lastActiveTime;
    }

    public void setLastActiveTime(long lastActiveTime) {
        this.lastActiveTime = lastActiveTime;
    }

    public String toString()
    {
        return "Connection: [" + socket.getInetAddress() + "], LastActiveTime: " + new Date(this.lastActiveTime).toLocaleString();
    }
}
