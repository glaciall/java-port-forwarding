package cn.org.hentai.server.dao;

import cn.org.hentai.server.model.Client;
import cn.org.hentai.server.util.db.DBAccess;
import org.springframework.stereotype.Repository;

import java.util.List;

/**
 * Created by Expect on 2017/12/27.
 */
@Repository
public class ClientDAO extends DBAccess
{
    public Client getById(int id)
    {
        return select().byId(id).query(Client.class);
    }

    public long update(Client client)
    {
        return update().valueWith(client).byId().execute();
    }

    public long delete(long id)
    {
        return execute("delete from clients where id = ?", id);
    }

    public int save(Client client)
    {
        int id = insertInto().valueWith(client).save();
        client.setId(id);
        return id;
    }

    public List<Client> find(int pageIndex, int pageSize)
    {
        return select()
                .orderBy("id", "desc")
                .queryForPaginate(Client.class, pageIndex, pageSize);
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
        return "clients";
    }
}
