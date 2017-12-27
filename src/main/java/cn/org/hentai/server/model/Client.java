package cn.org.hentai.server.model;

/**
 * Created by Expect on 2017/12/27.
 */
public class Client extends BaseModel
{
    private int id;
    private int userId;
    private String name;
    private int state;
    private String accesstoken;
    private String ip;
    private long lastActiveTime;

    public long getLastActiveTime() {
        return lastActiveTime;
    }

    public void setLastActiveTime(long lastActiveTime) {
        this.lastActiveTime = lastActiveTime;
    }

    public int getId() {
        return id;
    }

    public void setId(int id) {
        this.id = id;
    }

    public int getUserId() {
        return userId;
    }

    public void setUserId(int userId) {
        this.userId = userId;
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public int getState() {
        return state;
    }

    public void setState(int state) {
        this.state = state;
    }

    public String getAccesstoken() {
        return accesstoken;
    }

    public void setAccesstoken(String accesstoken) {
        this.accesstoken = accesstoken;
    }

    public String getIp() {
        return ip;
    }

    public void setIp(String ip) {
        this.ip = ip;
    }

    @Override
    public String toString() {
        return "Client{" +
                "id=" + id +
                ", userId=" + userId +
                ", name='" + name + '\'' +
                ", state=" + state +
                ", accesstoken='" + accesstoken + '\'' +
                ", ip='" + ip + '\'' +
                '}';
    }
}
