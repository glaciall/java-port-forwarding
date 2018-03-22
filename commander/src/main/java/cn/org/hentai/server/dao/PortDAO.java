package cn.org.hentai.server.dao;

import cn.org.hentai.server.model.Port;
import cn.org.hentai.server.util.db.DBAccess;
import org.springframework.stereotype.Repository;

import java.util.List;

/**
 * Created by Expect on 2018/1/25.
 */
@Repository
public class PortDAO extends DBAccess
{
    public Port getById(int id)
    {
        return select().byId(id).query(Port.class);
    }

    public Port getByPort(int port)
    {
        return select()
                .where(clause("port = ?", port))
                .query(Port.class);
    }

    @Override
    public String[] configureFields()
    {
        return new String[] { "id", "user_id", "host_id", "listen_port",
                "connect_timeout", "host_port", "state", "create_time",
                "last_active_time", "so_timeout", "concurrent_connections" };
    }

    @Override
    public String configureTableName()
    {
        return "ports";
    }

    public List<Port> list(int userId, int hostId)
    {
        return select()
                .where(clause("user_id = ?", gtz(userId)).and("host_id = ?", gtz(hostId)))
                .queryForList(Port.class);
    }
}
