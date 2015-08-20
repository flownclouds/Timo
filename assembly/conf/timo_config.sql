CREATE DATABASE IF NOT EXISTS `timo_config` CHARACTER SET 'utf8' COLLATE 'utf8_general_ci';
USE `timo_config`;

CREATE TABLE IF NOT EXISTS `datanodes`(
 `id` INT(10) UNSIGNED NOT NULL AUTO_INCREMENT,
 `strategy` TINYINT UNSIGNED NOT NULL DEFAULT 0 COMMENT '读写分离策略，0-仅主节点读写；1-主节点读写，从节点读；2-主节点只写，从节点读',
 PRIMARY KEY(`id`) )ENGINE=INNODB CHARSET=utf8 COMMENT '数据节点';

CREATE TABLE IF NOT EXISTS `datasources`(
 `id` INT(10) UNSIGNED NOT NULL AUTO_INCREMENT,
 `datanode_id` INT(10) UNSIGNED NOT NULL,
 `host` VARCHAR(16) NOT NULL DEFAULT '127.0.0.1',
 `port` SMALLINT UNSIGNED NOT NULL DEFAULT '3306',
 `username` VARCHAR(16) NOT NULL DEFAULT 'root',
 `password` VARCHAR(128) NOT NULL DEFAULT '123456',
 `db` VARCHAR(64) NOT NULL DEFAULT '',
 `datasource_type` TINYINT UNSIGNED NOT NULL DEFAULT 1 COMMENT '1-可读可写，2-只读',
 `datasource_status` TINYINT UNSIGNED NOT NULL DEFAULT 1 COMMENT '1-可用，0-不可用',
 `character_type` VARCHAR(16) NOT NULL DEFAULT 'utf8',
 `init_con` INT UNSIGNED NOT NULL DEFAULT 5 COMMENT '初始化连接数',
 `max_con` INT UNSIGNED NOT NULL DEFAULT 128 COMMENT '允许的最大连接数',
 `min_idle` INT UNSIGNED NOT NULL DEFAULT 5 COMMENT '最小的空闲连接数',
 `max_idle` INT UNSIGNED NOT NULL DEFAULT 6 COMMENT '允许最大的空闲连接数',
 `idle_check_period` INT UNSIGNED NOT NULL DEFAULT 0 COMMENT '连接空闲检查间隔(秒)',
 `priority` TINYINT UNSIGNED NOT NULL DEFAULT 1 COMMENT '初始化优先级,越小越优先',
 primary key(`id`) )ENGINE=INNODB CHARSET=utf8 COMMENT '数据源';

CREATE TABLE IF NOT EXISTS `users`(
 `username` VARCHAR(64) NOT NULL,
 `password` VARCHAR(128) NOT NULL DEFAULT '',
 `dbs` VARCHAR(1024) NOT NULL DEFAULT '',
 `hosts` VARCHAR(1024) NOT NULL DEFAULT '' COMMENT '设置后仅限该IP登陆，逗号隔开',
 `privilege` TINYINT UNSIGNED NOT NULL DEFAULT 0 COMMENT '用户权限等级，0-普通用户，1-超级用户',
 PRIMARY KEY(`username`) )ENGINE=INNODB CHARSET=utf8 COMMENT '账户';

CREATE TABLE IF NOT EXISTS `dbs`(
 `id` SMALLINT UNSIGNED NOT NULL AUTO_INCREMENT,
 `name` VARCHAR(64) NOT NULL DEFAULT 'timo' COMMENT '逻辑库名称',
 PRIMARY KEY(`id`) )ENGINE=INNODB CHARSET=utf8 COMMENT '逻辑库';

CREATE TABLE IF NOT EXISTS `tables`(
 `id` INT(10) UNSIGNED NOT NULL AUTO_INCREMENT,
 `name` VARCHAR(64) NOT NULL DEFAULT '',
 `db_id` SMALLINT UNSIGNED NOT NULL DEFAULT 0 COMMENT '逻辑库ID',
 `type` TINYINT UNSIGNED NOT NULL DEFAULT 1 COMMENT '表类型：0-全局表，1-分片表',
 `datanodes` VARCHAR(64) NOT NULL DEFAULT '' COMMENT '数据节点ID，逗号隔开',
 `rule_id` SMALLINT UNSIGNED NOT NULL DEFAULT 0 COMMENT '分片规则ID',
 PRIMARY KEY(`id`) )ENGINE=INNODB CHARSET=utf8 COMMENT '逻辑表';

CREATE TABLE IF NOT EXISTS `rules`(
 `id` SMALLINT UNSIGNED NOT NULL AUTO_INCREMENT,
 `column_name` VARCHAR(64) NOT NULL DEFAULT 'id' COMMENT '分片字段名称',
 `function_id` INT UNSIGNED NOT NULL DEFAULT 0 COMMENT '分片函数ID',
 `comment` VARCHAR(64) NOT NULL DEFAULT '',
 PRIMARY KEY(`id`) )ENGINE=INNODB CHARSET=utf8 COMMENT '分片规则';

CREATE TABLE IF NOT EXISTS `functions`(
 `id` INT UNSIGNED NOT NULL AUTO_INCREMENT,
 `type`VARCHAR(64) NOT NULL DEFAULT '' COMMENT '函数类型',
 `comment` VARCHAR(64) NOT NULL DEFAULT '',
 PRIMARY KEY(`id`) )ENGINE=INNODB CHARSET=utf8 COMMENT '分片函数';

CREATE TABLE IF NOT EXISTS `function_args`(
 `function_id` INT UNSIGNED NOT NULL,
 `args` VARCHAR(64) NOT NULL DEFAULT '',
 `datanode_id` INT UNSIGNED NOT NULL DEFAULT 1,
 PRIMARY KEY(`function_id`,
 	`args`) )ENGINE=INNODB CHARSET=utf8 COMMENT '分片函数参数';

CREATE TABLE IF NOT EXISTS `handovers`(
 `datasource_id` INT UNSIGNED NOT NULL DEFAULT 0,
 `handover_id` INT UNSIGNED NOT NULL DEFAULT 0 COMMENT '切换的datasource_id',
 `priority` TINYINT UNSIGNED NOT NULL DEFAULT 1 COMMENT '故障切换优先级,越小越优先',
 PRIMARY KEY(`datasource_id`,
 	`handover_id`) )ENGINE=INNODB CHARSET=utf8 COMMENT '数据节点切换';

CREATE TABLE IF NOT EXISTS `timonodes`(
 `id` INT UNSIGNED NOT NULL AUTO_INCREMENT,
 `host` VARCHAR(16) NOT NULL DEFAULT '127.0.0.1',
 `port` SMALLINT UNSIGNED NOT NULL DEFAULT '3306',
 `weight` TINYINT UNSIGNED NOT NULL DEFAULT 0 COMMENT '权重',
 PRIMARY KEY(`id`) )ENGINE=INNODB CHARSET=utf8 COMMENT 'Timo节点';





-- CREATE DATABASE `timo1`;
-- CREATE DATABASE `timo2`;
-- CREATE DATABASE `timo3`;

-- USE `timo_config`;

-- INSERT INTO datanodes SET id=1,strategy=1;
-- INSERT INTO datanodes SET id=2,strategy=1;
-- INSERT INTO datasources SET id=1,datanode_id=1,host='localhost',port=3306,username='root',password='123456',db='timo1',datasource_type=1,datasource_status=1,character_type='utf8',init_con=5,max_con=512,min_idle=5,max_idle=10,idle_check_period=600,priority=1;
-- INSERT INTO datasources SET id=3,datanode_id=1,host='localhost',port=3306,username='root',password='123456',db='timo3',datasource_type=2,datasource_status=1,character_type='utf8',init_con=5,max_con=512,min_idle=5,max_idle=10,idle_check_period=600,priority=2;
-- INSERT INTO datasources SET id=2,datanode_id=2,host='localhost',port=3306,username='root',password='123456',db='timo2',datasource_type=1,datasource_status=1,character_type='utf8',init_con=5,max_con=512,min_idle=5,max_idle=10,idle_check_period=600,priority=1;
-- INSERT INTO users SET username='root',password='123456',dbs='timo',hosts='127.0.0.1',privilege=1;
-- INSERT INTO users SET username='test',password='test',dbs='timo',hosts='',privilege=0;
-- INSERT INTO dbs SET id=1,name='timo';
-- INSERT INTO tables SET id=1,name='company',db_id=1,type=1,datanodes='1,2',rule_id=1;
-- INSERT INTO tables SET id=2,name='product',db_id=1,type=1,datanodes='1,2',rule_id=2;
-- INSERT INTO tables SET id=3,name='customer',db_id=1,type=1,datanodes='1,2',rule_id=3;
-- INSERT INTO tables SET id=4,name='orderlist',db_id=1,type=1,datanodes='1,2',rule_id=4;
-- INSERT INTO rules SET id=1,column_name='id',function_id=1,comment='根据ID做自动哈希拆分';
-- INSERT INTO rules SET id=2,column_name='id',function_id=2,comment='根据ID做哈希后按范围拆分';
-- INSERT INTO rules SET id=3,column_name='type',function_id=3,comment='根据TYPE做匹配拆分';
-- INSERT INTO rules SET id=4,column_name='id',function_id=4,comment='根据ID做范围拆分';
-- INSERT INTO functions SET id=1,type='AUTO',comment='节点数为2的自动哈希';
-- INSERT INTO functions SET id=2,type='HASH',comment='哈希后0-255在节点1上，256-1023在节点2上';
-- INSERT INTO functions SET id=3,type='MATCH',comment='A类型在节点1上，B类型在节点2上，其他在节点2上';
-- INSERT INTO functions SET id=4,type='RANGE',comment='0-1023在节点1上，1024-2047在节点2上，其他在节点1上';
-- INSERT INTO function_args SET function_id=1,args='2',datanode_id=0;
-- INSERT INTO function_args SET function_id=2,args='0:255',datanode_id=1;
-- INSERT INTO function_args SET function_id=2,args='256:1023',datanode_id=2;
-- INSERT INTO function_args SET function_id=3,args='A',datanode_id=1;
-- INSERT INTO function_args SET function_id=3,args='B',datanode_id=2;
-- INSERT INTO function_args SET function_id=3,args='2',datanode_id=0;
-- INSERT INTO function_args SET function_id=4,args='0:1023',datanode_id=1;
-- INSERT INTO function_args SET function_id=4,args='1024:2047',datanode_id=2;
-- INSERT INTO function_args SET function_id=4,args='1',datanode_id=0;
-- INSERT INTO handovers SET datasource_id=1,handover_id=3,priority=1;
