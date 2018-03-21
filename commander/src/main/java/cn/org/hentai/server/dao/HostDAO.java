package cn.org.hentai.server.dao;

import cn.org.hentai.server.model.Host;
import cn.org.hentai.server.util.db.DBAccess;
import org.springframework.stereotype.Repository;

import java.util.List;

/**
 * Created by Expect on 2017/12/27.
 */
@Repository
public class HostDAO extends DBAccess
{
    public Host getById(int id)
    {
        return select().byId(id).query(Host.class);
    }

    public long update(Host host)
    {
        return update().valueWith(host).byId().execute();
    }

    public long delete(long id)
    {
        return execute("delete from clients where id = ?", id);
    }

    public int save(Host host)
    {
        int id = insertInto().valueWith(host).save();
        host.setId(id);
        return id;
    }

    public List<Host> find(int pageIndex, int pageSize)
    {
        return select()
                .orderBy("id", "desc")
                .queryForPaginate(Host.class, pageIndex, pageSize);
    }

    public long findCount()
    {
        return select().queryForCount();
    }

    @Override
    public String[] configureFields()
    {
        return new String[] { "id", "user_id", "name", "state", "accesstoken", "ip", "last_active_time" };
    }

    @Override
    public String configureTableName()
    {
        return "hosts";
    }
}
