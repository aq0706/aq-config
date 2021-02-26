package com.github.aq0706.config.core;

import com.github.aq0706.support.server.Dispatcher;

import java.sql.SQLException;

/**
 * @author lidq
 */
public class ConfigController {

    private final static ConfigService configService;

    static {
        configService = new ConfigService();

        try {
            Dispatcher.registerHandler("/config", "POST", ConfigController.class.getDeclaredMethod("insert", Config.class));
        } catch (NoSuchMethodException e) {
            e.printStackTrace();
        }
    }

    public static Object insert(Config config) throws SQLException {
        //configService.insert(config);
        return config;
    }
}
