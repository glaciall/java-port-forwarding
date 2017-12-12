package cn.org.hentai.server.dao;

import cn.org.hentai.server.model.Admin;
import cn.org.hentai.server.util.db.DBAccess;
import org.springframework.stereotype.Repository;

/**
 * Created by matrixy on 2017/12/12.
 */
@Repository
public class AdminDAO extends DBAccess
{
    public Admin getById(long id)
    {
        return select().byId(id).query(Admin.class);
    }

    @Override
    public String[] configureFields()
    {
        return new String[] { "id", "name" };
    }

    @Override
    public String configureTableName()
    {
        return "admins";
    }
}
