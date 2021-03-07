package com.github.aq0706.config.core;

import com.github.aq0706.support.server.Controller;
import com.github.aq0706.support.server.RequestBody;
import com.github.aq0706.support.server.RequestMapping;
import com.github.aq0706.support.server.RequestParam;

import java.sql.SQLException;

/**
 * @author lidq
 */
@Controller()
public class ConfigController {

    private final ConfigService configService = new ConfigService();

    @RequestMapping(path = "/config", method = "PUT")
    public Config insert(@RequestBody Config config) throws SQLException {
        configService.insert(config);
        return config;
    }

    @RequestMapping(path = "/config", method = "POST")
    public Config modify(@RequestBody Config config) throws SQLException {
        configService.modify(config);
        return config;
    }

    @RequestMapping(path = "/config", method = "DELETE")
    public String delete(@RequestBody Config config) throws SQLException {
        configService.delete(config);
        return "success";
    }

    @RequestMapping(path = "/config", method = "GET")
    public String find(@RequestParam(name = "namespace") String namespace,
                       @RequestParam(name = "appName") String appName,
                       @RequestParam(name = "key") String key) throws SQLException {
        Config config = configService.get(namespace, appName, key);
        if (config == null) {
            return "";
        } else {
            return config.value;
        }
    }
}
