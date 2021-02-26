package com.github.aq0706.config.core;

import com.github.aq0706.support.mysql.DB;

import java.sql.SQLException;

/**
 * @author lidq
 */
public class ConfigService {

    public void insert(Config config) throws SQLException {
        DB.model(Config.class).insert(config);
    }

    public void modify(Config config) {

    }

    public void delete(Config config) {

    }
}
