CREATE DATABASE IF NOT EXISTS `aq_config` DEFAULT CHARACTER SET = utf8mb4;

DROP TABLE IF EXISTS `config`;
CREATE TABLE `config`
(
  `id`         BIGINT                                                       NOT NULL AUTO_INCREMENT,
  `namespace`  VARCHAR(255)                                           NOT NULL DEFAULT '' COMMENT '命名空间',
  `appName`    VARCHAR(255)                                                       NOT NULL DEFAULT '' COMMENT '应用名称',
  `key`    VARCHAR(255)                                                      NOT NULL DEFAULT '' COMMENT 'key',
  `value`  VARCHAR(255)  NOT NULL DEFAULT '' COMMENT '值',
  `loginPwd`   VARCHAR(64)                                                  NOT NULL COMMENT '登录密码',
  `status`     TINYINT                                                      NOT NULL COMMENT '申请状态: {0: 申请中, 1: 成功 2: 失败}',
  `failReason` VARCHAR(255)                                                 NOT NULL COMMENT '失败原因',
  `applyTime`  TIMESTAMP                                                    NOT NULL COMMENT '申请时间',
  PRIMARY KEY (`id`),
  INDEX        `idx_uid` (`uid`),
  INDEX        `idx_rid` (`rid`),
  INDEX        `idx_applyTime` (`applyTime`)
) ENGINE = MYISAM CHARSET = utf8 COMMENT '租用账号申请记录';