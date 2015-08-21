# Quick Start

## 目录结构
下载Timo压缩包，解压后结构如下：

```
 ─timo-server
    ├─bin
    │      startup.bat
    ├─conf
    │      server.xml
    │      timo_config.sql
    │      tinylog.properties
    ├─lib
    │      mysql-connector-java-5.1.27.jar
    │      timo-server-1.0.1.jar
    │      tinylog-2.0.0.jar
    └─logs
```
其中：
- `bin`：启动脚本
- `conf`：配置文件和配置库脚本
- `lib` ：依赖的jar包
- `logs`	：日志

## 开始配置
### 测试环境
由于本次部署仅为验证性测试，以下运行环境皆默认在本机部署：
- Timo 1.0.1
- JRE 1.8.0_45+
- MySQL 5.1+ (MySQL用户名密码皆为`test`)
(注：本次测试在Windows下完成，Linux环境操作基本一致)

### 执行配置脚本
安装路径下（例如`C:\Users\liuhuanting\temp\timo-server\conf`）执行：
```
mysql -utest -ptest -h127.0.0.1 -P3306 --default-character-set=utf8 < timo_config.sql
```
### 修改配置文件
将`timo-server/conf/server.xml`配置文件中的`url`,`username`,`password`修改为对应的数据库配置
```
<property name="url">jdbc:mysql://127.0.0.1:3306/timo_config</property> <!-- 配置库连接信息 -->
<property name="username">test</property> <!-- 配置库用户名 -->
<property name="password">test</property> <!-- 配置库密码 -->
```
### 启动Timo

运行`timo-server/bin/startup.bat`文件即可

##功能验证
###情况说明
如果按照`timo_config.sql`文件配置的话，Timo服务的拓扑结构应该如下图所示：

![image](7xl98h.com1.z0.glb.clouddn.com/timo1440128274100.png)

其中`timo1/2/3`是三个实际的物理数据库(`datasource`)，`timo1`和`timo3`组成分片1(`datanode1`)，`timo2`组成分片2(`datanode2`)。`timo1`和`timo3`理论上应该是一个`双主`或者`主从`的同步架构，本次测试比较简单，就没有再去部署多个实例。开启读写分离后，分片2由于只有一个数据源，因此`timo2`负责该分片的读写操作，而在分片1中，主节点（`timo1`）负责该分片的所有写和部分读操作，从节点（`timo3`）负责该分片的部分读操作。

### 创建测试表

由于timo3和timo1并没有搭建实际的主从复制结构，需要在timo3上手动创建以上数据表：
```
mysql -utest -ptest -h127.0.0.1 -P3306
use timo3;
CREATE TABLE `company`(`id` int NOT NULL ,`name` varchar(50) NOT NULL,PRIMARY KEY(`id`));
CREATE TABLE `product`(`id` int NOT NULL ,`name` varchar(50) NOT NULL,`company` varchar(50) NOT NULL,`price` decimal(10,2) NOT NULL,PRIMARY KEY(`id`));
CREATE TABLE `customer`(`id` int NOT NULL ,`name` varchar(50) NOT NULL,`type` varchar(11) NOT NULL,`address` varchar(50) NULL,PRIMARY KEY(`id`));
CREATE TABLE `orderlist`(`id` int NOT NULL ,`customerid` varchar(50) NOT NULL,`productid` varchar(50) NOT NULL,`createtime` timestamp NOT NULL,PRIMARY KEY(`id`));
```
命令行执行下面的命令登陆Timo：
```
 mysql -uroot -p123456 -h127.0.0.1 -P8066
```
创建测试表：
```
use timo;
CREATE TABLE `company`(`id` int NOT NULL ,`name` varchar(50) NOT NULL,PRIMARY KEY(`id`));
CREATE TABLE `product`(`id` int NOT NULL ,`name` varchar(50) NOT NULL,`company` varchar(50) NOT NULL,`price` decimal(10,2) NOT NULL,PRIMARY KEY(`id`));
CREATE TABLE `customer`(`id` int NOT NULL ,`name` varchar(50) NOT NULL,`type` varchar(11) NOT NULL,`address` varchar(50) NULL,PRIMARY KEY(`id`));
CREATE TABLE `orderlist`(`id` int NOT NULL ,`customerid` varchar(50) NOT NULL,`productid` varchar(50) NOT NULL,`createtime` timestamp NOT NULL,PRIMARY KEY(`id`));
```
### 分片说明
由配置表中可以查询到各表的具体分片规则：
```sql
mysql> select * from tables,rules,functions where tables.rule_id=rules.id and rules.function_id=functions.id;
+----+-----------+-------+------+-----------+---------+----+-------------+-------------+--------------------------+----+-------+----------------------------------------------------+
| id | name      | db_id | type | datanodes | rule_id | id | column_name | function_id | comment                  | id | type  | comment                                            |
+----+-----------+-------+------+-----------+---------+----+-------------+-------------+--------------------------+----+-------+----------------------------------------------------+
|  1 | company   |     1 |    1 | 1,2       |       1 |  1 | id          |           1 | 根据ID做自动哈希拆分     |  1 | AUTO  | 节点数为2的自动哈希                                |
|  2 | product   |     1 |    1 | 1,2       |       2 |  2 | id          |           2 | 根据ID做哈希后按范围拆分 |  2 | HASH  | 哈希后0-255在节点1上，256-1023在节点2上            |
|  3 | customer  |     1 |    1 | 1,2       |       3 |  3 | type        |           3 | 根据TYPE做匹配拆分       |  3 | MATCH | A类型在节点1上，B类型在节点2上，其他在节点2上      |
|  4 | orderlist |     1 |    1 | 1,2       |       4 |  4 | id          |           4 | 根据ID做范围拆分         |  4 | RANGE | 0-1023在节点1上，1024-2047在节点2上，其他在节点1上 |
+----+-----------+-------+------+-----------+---------+----+-------------+-------------+--------------------------+----+-------+----------------------------------------------------+
4 rows in set (0.00 sec)
```
###数据测试
（注：在语句前加`explain`可以查看该语句将会被路由到哪个节点）
分片测试仅用`orderlist`作为示范，其中id在0-1023的将路由到节点1,1024-2047的将路由到节点2，其他将路由到节点1上：

```sql
mysql>  explain insert into orderlist(id,customerid,productid) values (1,1,1);
+-----------+--------------------------------------------------------------------+
| DATA_NODE | SQL                                                                |
+-----------+--------------------------------------------------------------------+
| 1         | INSERT INTO orderlist (id, customerid, productid) VALUES (1, 1, 1) |
+-----------+--------------------------------------------------------------------+
1 row in set (0.00 sec)

mysql>  explain insert into orderlist(id,customerid,productid) values (2,1,1),(2015,2,3),(9527,4,5);
+-----------+----------------------------------------------------------------------------------+
| DATA_NODE | SQL                                                                              |
+-----------+----------------------------------------------------------------------------------+
| 1         | INSERT INTO orderlist (id, customerid, productid) VALUES (2, 1, 1), (9527, 4, 5) |
| 2         | INSERT INTO orderlist (id, customerid, productid) VALUES (2015, 2, 3)            |
+-----------+----------------------------------------------------------------------------------+
2 rows in set (0.00 sec)
```
插入数据：

```sql
mysql> insert into orderlist(id,customerid,productid) values (2,1,1),(2015,2,3),(9527,4,5);
Query OK, 3 rows affected (0.00 sec)
```
查询数据：

```sql
mysql> select * from orderlist order by id desc;
+------+------------+-----------+---------------------+
| id   | customerid | productid | createtime          |
+------+------------+-----------+---------------------+
| 9527 | 4          | 5         | 2015-08-21 13:11:16 |
| 2015 | 2          | 3         | 2015-08-21 13:11:16 |
|    2 | 1          | 1         | 2015-08-21 13:11:16 |
+------+------------+-----------+---------------------+
3 rows in set (0.00 sec)
```
由于负载均衡的设定，节点1可能会向`timo3`进行查询，而`timo3`中因为没有配置同步所以没有数据，因此查询的结果集偶尔会出现这样的情况：
```sql
mysql> select * from orderlist order by id desc;
+------+------------+-----------+---------------------+
| id   | customerid | productid | createtime          |
+------+------------+-----------+---------------------+
| 2015 | 2          | 3         | 2015-08-21 13:11:16 |
+------+------------+-----------+---------------------+
1 row in set (0.00 sec)
```
我们在后台MySQL中给timo3手动输入数据：
```sql
mysql> use timo3;
Database changed
mysql> insert into orderlist select * from timo1.orderlist;
Query OK, 2 rows affected (0.00 sec)
Records: 2  Duplicates: 0  Warnings: 0
```
可以看到分片1上确实只存储了两条数据。这时针对`orderlist`的查询便没有问题了。

###切换测试
Timo为每个数据源建立了一张心跳表(`timo_heartbeat`)，以提供对数据源可用性的检测。Timo会定时对该表进行更新操作确保改数据源可用，当心跳错误时，Timo会自动切换到可用的数据源上。
后端MySQL查看心跳表情况：

```sql
mysql> use timo1;
Database changed
mysql> select * from timo_heartbeat;
+----+---------------------+---------------+
| id | last_heartbeat_time | master_record |
+----+---------------------+---------------+
|  1 | 2015-08-21 13:32:58 |          NULL |
+----+---------------------+---------------+
1 row in set (0.00 sec)
```
可用Timo自带的管理功能查看Timo的心跳检测情况：
命令行登陆Timo的管理端口：
```
mysql -uroot -p123456 -h127.0.0.1 -P9066
```
通过show @@help命令可以查看支持的语句：

```sql
mysql> show @@help;
+------------------------------------------+-----------------------------------------------------------+
| commands                                 | comments                                                  |
+------------------------------------------+-----------------------------------------------------------+
| handover @@datasource [datanode_id]      | handover datanode's datasource to the next                |
| kill @@connection [connection_id]        | kill the connection you've chosen                         |
| offline                                  | turn timo-server to offline                               |
| online                                   | turn timo-server to online                                |
| reload @@config                          | reload the config online                                  |
| rollback @@config                        | rollback the config to the early time                     |
| show @@backend                           | show the status of backend connections                    |
| show @@command                           | unsupported yet                                           |
| show @@connection                        | show the status of current connections                    |
| show @@database                          | show the logic database in timo-server                    |
| show @@datanode                          | show status of datanodes on timo-server                   |
| show @@datasource                        | show status of datasources on timo-server                 |
| show @@heartbeat                         | show the status of heartbeat check                        |
| show @@latency                           | unsupported yet                                           |
| show @@operation                         | unsupported yet                                           |
| show @@processor                         | show the status of processors                             |
| show @@server                            | show the status of timo-server                            |
| show @@session                           | show the status of current sessions                       |
| show @@table                             | unsupported yet                                           |
| show @@thread                            | show the status of thread pool                            |
| show @@version                           | show the version of timo-server                           |
| stop @@heartbeat [datanode_id]:[time(s)] | pause heartbeat for a while on the datanode you've chosen |
+------------------------------------------+-----------------------------------------------------------+
22 rows in set (0.00 sec)
```
查看心跳情况：

```sql
mysql> show @@heartbeat;
+------+--------+-------------+----------------+-------+-------+--------+---------------------+--------+
| node | source | source_type | host           | db    | retry | status | last_active_time    | stoped |
+------+--------+-------------+----------------+-------+-------+--------+---------------------+--------+
| 1    | 1      | RW          | localhost:3306 | timo1 | 0     | OK     | 2015-08-21 01:35:38 | false  |
| 1    | 3      | R           | localhost:3306 | timo3 | 0     | OK     | 2015-08-21 01:35:38 | false  |
| 2    | 2      | RW          | localhost:3306 | timo2 | 0     | OK     | 2015-08-21 01:35:38 | false  |
+------+--------+-------------+----------------+-------+-------+--------+---------------------+--------+
3 rows in set (0.01 sec)
```
查看当前节点数据源使用：

```sql
mysql> show @@datanode;
+------+----------------------+-----------+------+-----------+------------+----------+
| node | source               | source_id | type | idle_size | total_size | strategy |
+------+----------------------+-----------+------+-----------+------------+----------+
| 1    | localhost:3306/timo1 | 1         | RW   | 3         | 10         | MRW_SR   |
| 2    | localhost:3306/timo2 | 2         | RW   | 3         | 10         | MRW_SR   |
+------+----------------------+-----------+------+-----------+------------+----------+
2 rows in set (0.00 sec)
```
后台MySQL上锁住心跳表：

```
mysql> use timo1;
Database changed
mysql> lock table timo_heartbeat write;
Query OK, 0 rows affected (0.00 sec)
```
管理端口查看心跳和节点情况：

```sql
mysql> show @@heartbeat;
+------+--------+-------------+----------------+-------+-------+--------+---------------------+--------+
| node | source | source_type | host           | db    | retry | status | last_active_time    | stoped |
+------+--------+-------------+----------------+-------+-------+--------+---------------------+--------+
| 1    | 1      | RW          | localhost:3306 | timo1 | 4     | STOPED | 2015-08-21 01:40:50 | true   |
| 1    | 3      | R           | localhost:3306 | timo3 | 0     | OK     | 2015-08-21 01:41:24 | false  |
| 2    | 2      | RW          | localhost:3306 | timo2 | 0     | OK     | 2015-08-21 01:41:24 | false  |
+------+--------+-------------+----------------+-------+-------+--------+---------------------+--------+
3 rows in set (0.00 sec)

mysql> show @@datanode;
+------+----------------------+-----------+------+-----------+------------+----------+
| node | source               | source_id | type | idle_size | total_size | strategy |
+------+----------------------+-----------+------+-----------+------------+----------+
| 1    | localhost:3306/timo3 | 3         | R    | 5         | 6          | MRW_SR   |
| 2    | localhost:3306/timo2 | 2         | RW   | 5         | 6          | MRW_SR   |
+------+----------------------+-----------+------+-----------+------------+----------+
2 rows in set (0.00 sec)
```
可以看到节点1的数据源已经从timo1切换到timo3上了。
在服务端口依然可以正常查询：
```sql
mysql> select * from orderlist order by id;
+------+------------+-----------+---------------------+
| id   | customerid | productid | createtime          |
+------+------------+-----------+---------------------+
|    2 | 1          | 1         | 2015-08-21 13:11:16 |
| 2015 | 2          | 3         | 2015-08-21 13:11:16 |
| 9527 | 4          | 5         | 2015-08-21 13:11:16 |
+------+------------+-----------+---------------------+
3 rows in set (0.02 sec)
```
解锁心跳表后不会改变当前的节点情况。
