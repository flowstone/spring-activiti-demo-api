# ************************************************************
# Sequel Pro SQL dump
# Version 4541
#
# http://www.sequelpro.com/
# https://github.com/sequelpro/sequelpro
#
# Host: 127.0.0.1 (MySQL 5.6.41)
# Database: spring-activiti-demo-api
# Generation Time: 2021-02-08 05:11:45 +0000
# ************************************************************


/*!40101 SET @OLD_CHARACTER_SET_CLIENT=@@CHARACTER_SET_CLIENT */;
/*!40101 SET @OLD_CHARACTER_SET_RESULTS=@@CHARACTER_SET_RESULTS */;
/*!40101 SET @OLD_COLLATION_CONNECTION=@@COLLATION_CONNECTION */;
/*!40101 SET NAMES utf8 */;
/*!40014 SET @OLD_FOREIGN_KEY_CHECKS=@@FOREIGN_KEY_CHECKS, FOREIGN_KEY_CHECKS=0 */;
/*!40101 SET @OLD_SQL_MODE=@@SQL_MODE, SQL_MODE='NO_AUTO_VALUE_ON_ZERO' */;
/*!40111 SET @OLD_SQL_NOTES=@@SQL_NOTES, SQL_NOTES=0 */;


# Dump of table biz_leave
# ------------------------------------------------------------

DROP TABLE IF EXISTS `biz_leave`;

CREATE TABLE `biz_leave` (
  `id` bigint(20) NOT NULL AUTO_INCREMENT COMMENT '主键ID',
  `type` char(20) DEFAULT NULL COMMENT '请假类型',
  `title` varchar(100) DEFAULT NULL COMMENT '标题',
  `reason` varchar(500) DEFAULT NULL COMMENT '原因',
  `start_time` datetime DEFAULT NULL COMMENT '开始时间',
  `end_time` datetime DEFAULT NULL COMMENT '结束时间',
  `total_time` bigint(11) DEFAULT NULL COMMENT '请假时长，单位秒',
  `instance_id` varchar(32) DEFAULT NULL COMMENT '流程实例ID',
  `create_by` varchar(64) DEFAULT '' COMMENT '创建者',
  `create_time` datetime DEFAULT NULL COMMENT '创建时间',
  `update_by` varchar(64) DEFAULT '' COMMENT '更新者',
  `update_time` datetime DEFAULT NULL COMMENT '更新时间',
  `apply_user` varchar(64) DEFAULT NULL COMMENT '申请人',
  `apply_time` datetime DEFAULT NULL COMMENT '申请时间',
  `reality_start_time` datetime DEFAULT NULL COMMENT '实际开始时间',
  `reality_end_time` datetime DEFAULT NULL COMMENT '实际结束时间',
  PRIMARY KEY (`id`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8;


DROP TABLE IF EXISTS `biz_todo_item`;

CREATE TABLE `biz_todo_item` (
  `id` bigint(20) NOT NULL AUTO_INCREMENT COMMENT '主键 ID',
  `item_name` varchar(100) DEFAULT NULL COMMENT '事项标题',
  `item_content` varchar(500) DEFAULT NULL COMMENT '事项内容',
  `module` varchar(50) DEFAULT NULL COMMENT '模块名称 (必须以 uri 一致)',
  `task_id` varchar(64) DEFAULT NULL COMMENT '任务 ID',
  `instance_id` varchar(32) DEFAULT NULL COMMENT '流程实例 ID',
  `task_name` varchar(50) DEFAULT NULL COMMENT '任务名称 (必须以表单页面名称一致)',
  `node_name` varchar(50) DEFAULT NULL COMMENT '节点名称',
  `is_view` char(1) DEFAULT '0' COMMENT '是否查看 default 0 (0 否 1 是)',
  `is_handle` char(1) DEFAULT '0' COMMENT '是否处理 default 0 (0 否 1 是)',
  `todo_user_id` varchar(20) DEFAULT NULL COMMENT '待办人 ID',
  `todo_user_name` varchar(30) DEFAULT NULL COMMENT '待办人名称',
  `handle_user_id` varchar(20) DEFAULT NULL COMMENT '处理人 ID',
  `handle_user_name` varchar(30) DEFAULT NULL COMMENT '处理人名称',
  `todo_time` datetime DEFAULT NULL COMMENT '通知时间',
  `handle_time` datetime DEFAULT NULL COMMENT '处理时间',
  PRIMARY KEY (`id`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8 COMMENT='待办事项表';




# Dump of table sys_dept
# ------------------------------------------------------------

DROP TABLE IF EXISTS `sys_dept`;

CREATE TABLE `sys_dept` (
  `dept_id` bigint(20) NOT NULL AUTO_INCREMENT COMMENT '部门id',
  `parent_id` bigint(20) DEFAULT '0' COMMENT '父部门id',
  `ancestors` varchar(50) DEFAULT '' COMMENT '祖级列表',
  `dept_name` varchar(30) DEFAULT '' COMMENT '部门名称',
  `order_num` int(4) DEFAULT '0' COMMENT '显示顺序',
  `leader` varchar(20) DEFAULT NULL COMMENT '负责人',
  `phone` varchar(11) DEFAULT NULL COMMENT '联系电话',
  `email` varchar(50) DEFAULT NULL COMMENT '邮箱',
  `status` char(1) DEFAULT '0' COMMENT '部门状态（0正常 1停用）',
  `del_flag` char(1) DEFAULT '0' COMMENT '删除标志（0代表存在 2代表删除）',
  `create_by` varchar(64) DEFAULT '' COMMENT '创建者',
  `create_time` datetime DEFAULT NULL COMMENT '创建时间',
  `update_by` varchar(64) DEFAULT '' COMMENT '更新者',
  `update_time` datetime DEFAULT NULL COMMENT '更新时间',
  PRIMARY KEY (`dept_id`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8 COMMENT='部门表';

LOCK TABLES `sys_dept` WRITE;
/*!40000 ALTER TABLE `sys_dept` DISABLE KEYS */;

INSERT INTO `sys_dept` (`dept_id`, `parent_id`, `ancestors`, `dept_name`, `order_num`, `leader`, `phone`, `email`, `status`, `del_flag`, `create_by`, `create_time`, `update_by`, `update_time`)
VALUES
	(100,0,'0','若依科技',0,'若依','15888888888','ry@qq.com','0','0','admin','2018-03-16 11:33:00','ry','2018-03-16 11:33:00'),
	(101,100,'0,100','深圳总公司',1,'若依','15888888888','ry@qq.com','0','0','admin','2018-03-16 11:33:00','ry','2018-03-16 11:33:00'),
	(102,100,'0,100','长沙分公司',2,'若依','15888888888','ry@qq.com','0','0','admin','2018-03-16 11:33:00','ry','2018-03-16 11:33:00'),
	(103,101,'0,100,101','研发部门',1,'若依','15888888888','ry@qq.com','0','0','admin','2018-03-16 11:33:00','ry','2018-03-16 11:33:00'),
	(104,101,'0,100,101','市场部门',2,'若依','15888888888','ry@qq.com','0','0','admin','2018-03-16 11:33:00','ry','2018-03-16 11:33:00'),
	(105,101,'0,100,101','测试部门',3,'若依','15888888888','ry@qq.com','0','0','admin','2018-03-16 11:33:00','ry','2018-03-16 11:33:00'),
	(106,101,'0,100,101','财务部门',4,'若依','15888888888','ry@qq.com','0','0','admin','2018-03-16 11:33:00','ry','2018-03-16 11:33:00'),
	(107,101,'0,100,101','运维部门',5,'若依','15888888888','ry@qq.com','0','0','admin','2018-03-16 11:33:00','ry','2018-03-16 11:33:00'),
	(108,102,'0,100,102','市场部门',1,'若依','15888888888','ry@qq.com','0','0','admin','2018-03-16 11:33:00','ry','2018-03-16 11:33:00'),
	(109,102,'0,100,102','财务部门',2,'若依','15888888888','ry@qq.com','0','0','admin','2018-03-16 11:33:00','ry','2018-03-16 11:33:00');

/*!40000 ALTER TABLE `sys_dept` ENABLE KEYS */;
UNLOCK TABLES;


# Dump of table sys_role
# ------------------------------------------------------------

DROP TABLE IF EXISTS `sys_role`;

CREATE TABLE `sys_role` (
  `role_id` bigint(20) NOT NULL AUTO_INCREMENT COMMENT '角色ID',
  `role_name` varchar(30) NOT NULL COMMENT '角色名称',
  `role_key` varchar(100) NOT NULL COMMENT '角色权限字符串',
  `role_sort` int(4) NOT NULL COMMENT '显示顺序',
  `data_scope` char(1) DEFAULT '1' COMMENT '数据范围（1：全部数据权限 2：自定数据权限 3：本部门数据权限 4：本部门及以下数据权限）',
  `status` char(1) NOT NULL COMMENT '角色状态（0正常 1停用）',
  `del_flag` char(1) DEFAULT '0' COMMENT '删除标志（0代表存在 2代表删除）',
  `create_by` varchar(64) DEFAULT '' COMMENT '创建者',
  `create_time` datetime DEFAULT NULL COMMENT '创建时间',
  `update_by` varchar(64) DEFAULT '' COMMENT '更新者',
  `update_time` datetime DEFAULT NULL COMMENT '更新时间',
  `remark` varchar(500) DEFAULT NULL COMMENT '备注',
  PRIMARY KEY (`role_id`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8 COMMENT='角色信息表';

LOCK TABLES `sys_role` WRITE;
/*!40000 ALTER TABLE `sys_role` DISABLE KEYS */;

INSERT INTO `sys_role` (`role_id`, `role_name`, `role_key`, `role_sort`, `data_scope`, `status`, `del_flag`, `create_by`, `create_time`, `update_by`, `update_time`, `remark`)
VALUES
	(1,'管理员','admin',1,'1','0','0','admin','2018-03-16 11:33:00','ry','2018-03-16 11:33:00','管理员'),
	(2,'普通角色','common',2,'2','0','0','admin','2018-03-16 11:33:00','ry','2018-03-16 11:33:00','普通角色'),
	(100,'部门领导','deptLeader',3,'1','0','0','admin','2020-02-27 19:37:37','',NULL,'请假流程测试角色 -> 流程用户组'),
	(101,'人事','hr',4,'1','0','0','admin','2020-02-27 19:38:41','',NULL,'请假流程测试角色 -> 流程用户组'),
	(102,'流程角色','processRole',5,'1','0','0','admin','2020-02-28 21:31:15','admin','2020-04-04 17:38:54',''),
	(109,'财务','financial',6,'1','0','0','admin','2020-04-05 17:51:04','admin','2020-04-05 17:55:45',''),
	(110,'总经理','generalManager',7,'1','0','0','admin','2020-04-05 18:10:13','',NULL,NULL);

/*!40000 ALTER TABLE `sys_role` ENABLE KEYS */;
UNLOCK TABLES;


# Dump of table sys_role_dept
# ------------------------------------------------------------

DROP TABLE IF EXISTS `sys_role_dept`;

CREATE TABLE `sys_role_dept` (
  `role_id` bigint(20) NOT NULL COMMENT '角色ID',
  `dept_id` bigint(20) NOT NULL COMMENT '部门ID',
  PRIMARY KEY (`role_id`,`dept_id`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8 COMMENT='角色和部门关联表';

LOCK TABLES `sys_role_dept` WRITE;
/*!40000 ALTER TABLE `sys_role_dept` DISABLE KEYS */;

INSERT INTO `sys_role_dept` (`role_id`, `dept_id`)
VALUES
	(2,100),
	(2,101),
	(2,105);

/*!40000 ALTER TABLE `sys_role_dept` ENABLE KEYS */;
UNLOCK TABLES;


# Dump of table sys_user
# ------------------------------------------------------------

DROP TABLE IF EXISTS `sys_user`;

CREATE TABLE `sys_user` (
  `user_id` bigint(20) NOT NULL AUTO_INCREMENT COMMENT '用户ID',
  `dept_id` bigint(20) DEFAULT NULL COMMENT '部门ID',
  `login_name` varchar(30) NOT NULL COMMENT '登录账号',
  `user_name` varchar(30) NOT NULL COMMENT '用户昵称',
  `user_type` varchar(2) DEFAULT '00' COMMENT '用户类型（00系统用户）',
  `email` varchar(50) DEFAULT '' COMMENT '用户邮箱',
  `phonenumber` varchar(11) DEFAULT '' COMMENT '手机号码',
  `sex` char(1) DEFAULT '0' COMMENT '用户性别（0男 1女 2未知）',
  `avatar` varchar(100) DEFAULT '' COMMENT '头像路径',
  `password` varchar(50) DEFAULT '' COMMENT '密码',
  `salt` varchar(20) DEFAULT '' COMMENT '盐加密',
  `status` char(1) DEFAULT '0' COMMENT '帐号状态（0正常 1停用）',
  `del_flag` char(1) DEFAULT '0' COMMENT '删除标志（0代表存在 2代表删除）',
  `login_ip` varchar(50) DEFAULT '' COMMENT '最后登陆IP',
  `login_date` datetime DEFAULT NULL COMMENT '最后登陆时间',
  `create_by` varchar(64) DEFAULT '' COMMENT '创建者',
  `create_time` datetime DEFAULT NULL COMMENT '创建时间',
  `update_by` varchar(64) DEFAULT '' COMMENT '更新者',
  `update_time` datetime DEFAULT NULL COMMENT '更新时间',
  `remark` varchar(500) DEFAULT NULL COMMENT '备注',
  PRIMARY KEY (`user_id`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8 COMMENT='用户信息表';

LOCK TABLES `sys_user` WRITE;
/*!40000 ALTER TABLE `sys_user` DISABLE KEYS */;

INSERT INTO `sys_user` (`user_id`, `dept_id`, `login_name`, `user_name`, `user_type`, `email`, `phonenumber`, `sex`, `avatar`, `password`, `salt`, `status`, `del_flag`, `login_ip`, `login_date`, `create_by`, `create_time`, `update_by`, `update_time`, `remark`)
VALUES
	(1,103,'admin','若依','00','ry@163.com','15888888888','1','','29c67a30398638269fe600f73a054934','111111','0','0','127.0.0.1','2021-02-04 16:31:37','admin','2018-03-16 11:33:00','ry','2021-02-04 16:31:36','管理员'),
	(2,105,'ry','若依','00','ry@qq.com','15666666666','1','','8e6d98b90472783cc73c17047ddccf36','222222','0','0','127.0.0.1','2020-04-05 21:13:17','admin','2018-03-16 11:33:00','ry','2020-04-05 21:13:17','测试员'),
	(100,105,'chengxy','程序猿','00','110@gmail.com','13000000000','0','','49c6d6132c83d595864ef013a6b7a579','0093f2','0','0','127.0.0.1','2021-02-04 17:39:13','admin','2020-02-27 19:43:45','','2021-02-04 17:39:13','注意该用户暂时没有分配角色（即没有分配到流程用户组）'),
	(101,105,'axianlu','一只闲鹿','00','114@gmail.com','13000000001','0','','49c6d6132c83d595864ef013a6b7a579','309e5c','0','0','127.0.0.1','2021-02-04 18:46:50','admin','2020-02-27 19:45:15','','2021-02-04 18:46:49','密码123456'),
	(102,105,'rensm','人事喵','00','115@gmail.com','13000000002','1','','49c6d6132c83d595864ef013a6b7a579','e12d1b','0','0','127.0.0.1','2020-03-31 20:20:27','admin','2020-02-27 19:46:20','','2020-03-31 20:20:27',NULL),
	(103,105,'11','caiwu','00','110@qq.com','13111111111','0','','9a07736daa48f0586dad267ea360ff7c','2d7a22','0','0','127.0.0.1','2020-04-05 21:13:37','admin','2020-03-31 22:58:21','admin','2020-04-05 21:13:37',''),
	(104,105,'22','zongjingli','00','111@qq.com','13111111112','0','','6c8017bb59ac49b861067437d88de4e9','727312','0','0','127.0.0.1','2020-04-05 21:14:01','admin','2020-03-31 22:59:00','admin','2020-04-05 21:14:01',''),
	(105,105,'33','33','00','113@qq.com','13111111113','0','','8cff2807327e928e05f206497eec5a3d','da2c2e','0','0','',NULL,'admin','2020-03-31 22:59:31','',NULL,NULL),
	(106,105,'44','44','00','114@qq.com','13111111114','0','','341fc5512c7c010fa8f151b2e0598e11','4d3ad4','0','0','',NULL,'admin','2020-03-31 22:59:56','',NULL,NULL),
	(107,105,'55','55','00','115@qq.com','13111111115','0','','d0e407615613a3281c66edbdcae2f205','0d3114','0','0','',NULL,'admin','2020-03-31 23:00:20','',NULL,NULL),
	(108,105,'66','66','00','116@qq.com','13111111116','0','','89f3963f73ad41d4103e969d8dbf1377','5c6554','0','0','',NULL,'admin','2020-03-31 23:00:45','',NULL,NULL),
	(109,105,'chairman','chairman','00','119@qq.com','13111111119','0','','2dc7933681b07e3bcca0e1ce6e8554ec','e4c935','0','0','127.0.0.1','2020-04-05 21:14:31','admin','2020-04-05 18:14:12','','2020-04-05 21:14:31',NULL);

/*!40000 ALTER TABLE `sys_user` ENABLE KEYS */;
UNLOCK TABLES;


# Dump of table sys_user_role
# ------------------------------------------------------------

DROP TABLE IF EXISTS `sys_user_role`;

CREATE TABLE `sys_user_role` (
  `user_id` bigint(20) NOT NULL COMMENT '用户ID',
  `role_id` bigint(20) NOT NULL COMMENT '角色ID',
  PRIMARY KEY (`user_id`,`role_id`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8 COMMENT='用户和角色关联表';

LOCK TABLES `sys_user_role` WRITE;
/*!40000 ALTER TABLE `sys_user_role` DISABLE KEYS */;

INSERT INTO `sys_user_role` (`user_id`, `role_id`)
VALUES
	(1,1),
	(2,2),
	(2,102),
	(100,102),
	(101,100),
	(101,102),
	(102,101),
	(102,102),
	(103,102),
	(103,109),
	(104,102),
	(104,110),
	(109,102);

/*!40000 ALTER TABLE `sys_user_role` ENABLE KEYS */;
UNLOCK TABLES;

-- --------------------------------
-- 添加三张视图，代替Activiti生成的三张表
-- ----------------------------
-- View structure for act_id_group
-- ----------------------------
DROP VIEW IF EXISTS `ACT_ID_GROUP`;
CREATE ALGORITHM=UNDEFINED DEFINER=`root`@`localhost` SQL SECURITY DEFINER VIEW `ACT_ID_GROUP` AS select `r`.`role_key` AS `ID_`,NULL AS `REV_`,`r`.`role_name` AS `NAME_`,'assignment' AS `TYPE_` from `sys_role` `r` ;

-- ----------------------------
-- View structure for act_id_membership
-- ----------------------------
DROP VIEW IF EXISTS `ACT_ID_MEMBERSHIP`;
CREATE ALGORITHM=UNDEFINED DEFINER=`root`@`localhost` SQL SECURITY DEFINER VIEW `ACT_ID_MEMBERSHIP` AS select (select `u`.`login_name` from `sys_user` `u` where (`u`.`user_id` = `ur`.`user_id`)) AS `USER_ID_`,(select `r`.`role_key` from `sys_role` `r` where (`r`.`role_id` = `ur`.`role_id`)) AS `GROUP_ID_` from `sys_user_role` `ur` ;

-- ----------------------------
-- View structure for act_id_user
-- ----------------------------
DROP VIEW IF EXISTS `ACT_ID_USER`;
CREATE ALGORITHM=UNDEFINED DEFINER=`root`@`localhost` SQL SECURITY DEFINER VIEW `ACT_ID_USER` AS select `u`.`login_name` AS `ID_`,0 AS `REV_`,`u`.`user_name` AS `FIRST_`,'' AS `LAST_`,`u`.`email` AS `EMAIL_`,`u`.`password` AS `PWD_`,'' AS `PICTURE_ID_` from `sys_user` `u` ;
