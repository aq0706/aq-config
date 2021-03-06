package com.github.aq0706.config.core;

import com.github.aq0706.support.mysql.DB;

import java.sql.SQLException;

/**
 * @author lidq
 */
public class ConfigService {

    public void insert(Config config) throws SQLException {
        config.id = 0L;
        DB.model(Config.class).insert(config);
    }

    public void modify(Config config) throws SQLException {
        DB.model(Config.class)
                .modify("value", config.value)
                .where("namespace", config.namespace)
                .where("app_name", config.appName)
                .where("`key`", config.key)
                .execute();
    }

    public void delete(Config config) throws SQLException {
        DB.model(Config.class)
                .delete()
                .where("namespace", config.namespace)
                .where("app_name", config.appName)
                .where("`key`", config.key)
                .execute();
    }

    public Config get(String nameSpace, String appName, String key) throws SQLException {
         return DB.model(Config.class)
                .select()
                .where("namespace", nameSpace)
                .where("app_name", appName)
                .where("`key`", key)
                .find();
    }
}
