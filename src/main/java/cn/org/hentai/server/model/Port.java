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
}
