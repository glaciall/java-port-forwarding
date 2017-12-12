package cn.org.hentai.server.model;

import cn.org.hentai.server.util.db.DBField;

/**
 * Created by matrixy on 2017/12/12.
 */
public class Admin extends BaseModel
{
    private long id;
    private String name;

    public long getId() {
        return id;
    }

    public void setId(long id) {
        this.id = id;
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    @Override
    public String toString() {
        return "Admin{" +
                "id=" + id +
                ", name='" + name + '\'' +
                '}';
    }
}
