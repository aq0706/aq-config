package com.github.aq0706.config.core;

import com.github.aq0706.support.mysql.mapping.Column;
import com.github.aq0706.support.mysql.mapping.Key;
import com.github.aq0706.support.mysql.mapping.Table;

import java.sql.Timestamp;

/**
 * @author lidq
 */
@Table(name = "config", schema = "aq_config")
public class Config {

    @Key(isAutoIncrement = true)
    @Column(name = "id")
    public long id;

    @Column(name = "namespace")
    String namespace;

    @Column(name = "app_name")
    String appName;

    @Column(name = "`key`")
    String key;

    @Column(name = "value")
    String value;

    @Column(name = "create_time")
    Timestamp createTime;

    @Column(name = "update_time")
    Timestamp updateTime;
}
