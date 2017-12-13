package cn.org.hentai.server.dao;

import cn.org.hentai.server.model.User;
import cn.org.hentai.server.util.db.DBAccess;
import org.springframework.stereotype.Repository;

import java.util.List;

/**
 * Created by matrixy on 2017/12/13.
 */
@Repository
public class UserDAO extends DBAccess
{
    public User getById(long id)
    {
        return select().byId(id).query(User.class);
    }

    public User getByName(String name)
    {
        return select().where(clause("name = ?", name)).query(User.class);
    }

    public long update(User user)
    {
        return update().valueWith(user).byId().execute();
    }

    public long save(User user)
    {
        long id = insertInto().valueWith(user).save();
        user.setId(id);
        return id;
    }

    public List<User> find(String name)
    {
        return select().where(clause("name like ?", like(name))).orderBy("id", "desc").queryForList(User.class);
    }

    @Override
    public String[] configureFields()
    {
        return new String[] { "id", "name", "password", "salt", "accesstoken", "rand", "last_login_time", "last_login_ip" };
    }

    @Override
    public String configureTableName()
    {
        return "users";
    }
}
