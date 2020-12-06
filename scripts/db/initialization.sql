CREATE DATABASE IF NOT EXISTS aq_config DEFAULT CHARACTER SET = utf8mb4;

USE aq_config;

DROP TABLE IF EXISTS `config`;
CREATE TABLE `config`
(
    `id`          BIGINT      NOT NULL AUTO_INCREMENT COMMENT '自增Id',
    `namespace`   VARCHAR(64) NOT NULL DEFAULT '' COMMENT '命名空间',
    `app_name`    VARCHAR(64) NOT NULL DEFAULT '' COMMENT '应用名称',
    `key`         VARCHAR(64) NOT NULL DEFAULT '' COMMENT '配置项key',
    `value`       LONGTEXT    NOT NULL COMMENT '配置项值',
    `create_time` TIMESTAMP   NOT NULL DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
    `update_time` TIMESTAMP   NOT NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP COMMENT '最后更新时间',
    PRIMARY KEY (`id`),
    UNIQUE KEY `uk_namespace_appname_key` (`namespace`, `app_name`, `key`)
) ENGINE = InnoDB CHARSET = utf8mb4 COMMENT '配置';