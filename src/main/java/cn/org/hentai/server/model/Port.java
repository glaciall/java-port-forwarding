package cn.org.hentai.server.model;

/**
 * Created by Expect on 2018/1/25.
 */
public class Port
{
    // ID主键
    private int id;

    // 所属用户ID
    private int userId;

    // 服务器端监听端口
    private int listenPort;

    // 主机端被代理端口
    private int hostPort;

    // 状态
    private int state;

    // 添加时间
    private long createTime;

    // 最近连接通信时间
    private long lastActiveTime;

    // socket IO超时时长，单位为秒
    private int soTimeout;

    // 最大并发连接数
    private int concurrentConnections;

    // 连接超时
    private int connectTimeout;

    public int getConnectTimeout()
    {
        return connectTimeout;
    }

    public void setConnectTimeout(int connectTimeout)
    {
        this.connectTimeout = connectTimeout;
    }

    public int getId()
    {
        return id;
    }

    public void setId(int id)
    {
        this.id = id;
    }

    public int getUserId()
    {
        return userId;
    }

    public void setUserId(int userId)
    {
        this.userId = userId;
    }

    public int getListenPort()
    {
        return listenPort;
    }

    public void setListenPort(int listenPort)
    {
        this.listenPort = listenPort;
    }

    public int getHostPort()
    {
        return hostPort;
    }

    public void setHostPort(int hostPort)
    {
        this.hostPort = hostPort;
    }

    public int getState()
    {
        return state;
    }

    public void setState(int state)
    {
        this.state = state;
    }

    public long getCreateTime()
    {
        return createTime;
    }

    public void setCreateTime(long createTime)
    {
        this.createTime = createTime;
    }

    public long getLastActiveTime()
    {
        return lastActiveTime;
    }

    public void setLastActiveTime(long lastActiveTime)
    {
        this.lastActiveTime = lastActiveTime;
    }

    public int getSoTimeout()
    {
        return soTimeout;
    }

    public void setSoTimeout(int soTimeout)
    {
        this.soTimeout = soTimeout;
    }

    public int getConcurrentConnections()
    {
        return concurrentConnections;
    }

    public void setConcurrentConnections(int concurrentConnections)
    {
        this.concurrentConnections = concurrentConnections;
    }

    @Override
    public String toString()
    {
        return "Port{" +
                "id=" + id +
                ", userId=" + userId +
                ", listenPort=" + listenPort +
                ", hostPort=" + hostPort +
                ", state=" + state +
                ", createTime=" + createTime +
                ", lastActiveTime=" + lastActiveTime +
                ", soTimeout=" + soTimeout +
                ", concurrentConnections=" + concurrentConnections +
                '}';
    }
}
