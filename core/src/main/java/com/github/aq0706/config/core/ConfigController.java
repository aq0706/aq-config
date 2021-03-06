package com.github.aq0706.config.core;

import com.github.aq0706.support.server.Controller;
import com.github.aq0706.support.server.RequestMapping;

import java.sql.SQLException;

/**
 * @author lidq
 */
@Controller()
public class ConfigController {

    private final ConfigService configService = new ConfigService();

    @RequestMapping(path = "/config", method = "PUT")
    public Object insert(Config config) throws SQLException {
        configService.insert(config);
        return config;
    }

    @RequestMapping(path = "/config", method = "POST")
    public Object modify(Config config) throws SQLException {
        configService.modify(config);
        return config;
    }

    @RequestMapping(path = "/config", method = "DELETE")
    public Object delete(Config config) throws SQLException {
        configService.delete(config);
        return "success";
    }

    @RequestMapping(path = "/config", method = "GET")
    public Object find(Config config) throws SQLException {
        return configService.get(config.namespace, config.appName, config.key);
    }
}
