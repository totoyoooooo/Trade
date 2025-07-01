/*
SQLyog Ultimate v11.33 (64 bit)
MySQL - 5.7.18-log : Database - db_second_hand_trading
*********************************************************************
*/

/*!40101 SET NAMES utf8 */;

/*!40101 SET SQL_MODE=''*/;

/*!40014 SET @OLD_UNIQUE_CHECKS=@@UNIQUE_CHECKS, UNIQUE_CHECKS=0 */;
/*!40014 SET @OLD_FOREIGN_KEY_CHECKS=@@FOREIGN_KEY_CHECKS, FOREIGN_KEY_CHECKS=0 */;
/*!40101 SET @OLD_SQL_MODE=@@SQL_MODE, SQL_MODE='NO_AUTO_VALUE_ON_ZERO' */;
/*!40111 SET @OLD_SQL_NOTES=@@SQL_NOTES, SQL_NOTES=0 */;
CREATE DATABASE /*!32312 IF NOT EXISTS*/`db_second_hand_trading` /*!40100 DEFAULT CHARACTER SET utf8 */;

USE `db_second_hand_trading`;

/*Table structure for table `sh_address` */

DROP TABLE IF EXISTS `sh_address`;

CREATE TABLE `sh_address` (
  `id` bigint(20) NOT NULL AUTO_INCREMENT COMMENT '自增主键',
  `consignee_name` varchar(32) NOT NULL COMMENT '收货人姓名',
  `consignee_phone` varchar(16) NOT NULL COMMENT '收货人手机号',
  `province_name` varchar(32) NOT NULL COMMENT '省',
  `city_name` varchar(32) NOT NULL COMMENT '市',
  `region_name` varchar(32) NOT NULL COMMENT '区',
  `detail_address` varchar(64) NOT NULL COMMENT '详细地址',
  `default_flag` tinyint(4) NOT NULL COMMENT '是否默认地址',
  `user_id` bigint(20) NOT NULL COMMENT '用户主键id',
  PRIMARY KEY (`id`) USING BTREE,
  KEY `user_id_index` (`user_id`) USING BTREE
) ENGINE=InnoDB AUTO_INCREMENT=30 DEFAULT CHARSET=utf8 ROW_FORMAT=COMPACT;

/*Data for the table `sh_address` */

insert  into `sh_address`(`id`,`consignee_name`,`consignee_phone`,`province_name`,`city_name`,`region_name`,`detail_address`,`default_flag`,`user_id`) values (1,'user9','13011110009','江苏省','南通市','崇川区','花园一单元一号楼',1,10),(2,'user8','13011110008','江苏省','南通市','崇川区','888',1,9),(27,'MZ','13011110000','河南省','新乡市','原阳县','测试测试',1,1),(28,'1211111111','1212','天津市','市辖区','河东区','212',1,2),(29,'下雨','1788888888','天津市','市辖区','河西区','北京',1,4);

/*Table structure for table `sh_admin` */

DROP TABLE IF EXISTS `sh_admin`;

CREATE TABLE `sh_admin` (
  `id` bigint(20) NOT NULL AUTO_INCREMENT COMMENT '自增主键',
  `account_number` varchar(16) NOT NULL COMMENT '管理员账号',
  `admin_password` varchar(16) NOT NULL COMMENT '密码',
  `admin_name` varchar(8) NOT NULL COMMENT '管理员名字',
  PRIMARY KEY (`id`) USING BTREE,
  UNIQUE KEY `account_number` (`account_number`) USING BTREE
) ENGINE=InnoDB AUTO_INCREMENT=13 DEFAULT CHARSET=utf8 ROW_FORMAT=COMPACT;

/*Data for the table `sh_admin` */

insert  into `sh_admin`(`id`,`account_number`,`admin_password`,`admin_name`) values (1,'java1234','123456','超级管理员'),(2,'22','123123','管理员1'),(3,'33','123123','管理员2'),(11,'44','123123','管理员3'),(12,'55','123123','管理员4');

/*Table structure for table `sh_favorite` */

DROP TABLE IF EXISTS `sh_favorite`;

CREATE TABLE `sh_favorite` (
  `id` bigint(20) NOT NULL AUTO_INCREMENT COMMENT '自增主键id',
  `create_time` datetime NOT NULL COMMENT '加入收藏的时间',
  `user_id` bigint(20) NOT NULL COMMENT '用户主键id',
  `idle_id` bigint(20) NOT NULL COMMENT '闲置物主键id',
  PRIMARY KEY (`id`) USING BTREE,
  UNIQUE KEY `user_id` (`user_id`,`idle_id`) USING BTREE
) ENGINE=InnoDB AUTO_INCREMENT=47 DEFAULT CHARSET=utf8 ROW_FORMAT=COMPACT;

/*Data for the table `sh_favorite` */

insert  into `sh_favorite`(`id`,`create_time`,`user_id`,`idle_id`) values (42,'2021-09-02 07:21:59',9,80),(43,'2022-10-22 22:20:08',4,90),(44,'2023-01-08 10:23:09',3,93),(45,'2023-01-08 17:56:57',3,98),(46,'2023-01-08 17:58:11',3,97);

/*Table structure for table `sh_idle_item` */

DROP TABLE IF EXISTS `sh_idle_item`;

CREATE TABLE `sh_idle_item` (
  `id` bigint(20) NOT NULL AUTO_INCREMENT COMMENT '自增主键',
  `idle_name` varchar(64) NOT NULL COMMENT '闲置物名称',
  `idle_details` varchar(2048) NOT NULL COMMENT '详情',
  `picture_list` varchar(1024) NOT NULL COMMENT '图集',
  `idle_price` decimal(10,2) NOT NULL COMMENT '价格',
  `idle_place` varchar(32) NOT NULL COMMENT '发货地区',
  `idle_label` int(11) NOT NULL COMMENT '分类标签',
  `release_time` datetime NOT NULL COMMENT '发布时间',
  `idle_status` tinyint(4) NOT NULL COMMENT '状态（发布1、下架2、删除0）',
  `user_id` bigint(20) NOT NULL COMMENT '用户主键id',
  PRIMARY KEY (`id`) USING BTREE,
  KEY `user_id_index` (`user_id`) USING BTREE
) ENGINE=InnoDB AUTO_INCREMENT=106 DEFAULT CHARSET=utf8 ROW_FORMAT=COMPACT;

/*Data for the table `sh_idle_item` */

insert  into `sh_idle_item`(`id`,`idle_name`,`idle_details`,`picture_list`,`idle_price`,`idle_place`,`idle_label`,`release_time`,`idle_status`,`user_id`) values (1,'111','111','[\"http://localhost:8080/image?imageName=file16866307653031002壁纸.jpg\"]','20.01','市辖区',1,'2023-06-13 04:32:47',2,1),(2,'运动鞋','跑步非常舒服 回弹性好','[\"http://localhost:8080/image?imageName=file16868385096121002运动鞋.jpg\"]','100.00','市辖区',3,'2023-06-15 14:15:11',1,1),(3,'电风扇','贼凉快','[\"http://localhost:8080/image?imageName=file16868385807991003风扇.jpg\"]','120.00','唐山市',2,'2023-06-15 14:16:37',2,1),(4,'加湿器','干燥空气瞬间变湿','[\"http://localhost:8080/image?imageName=file16868386408251004file16513152624181010加湿器.jpg\"]','50.00','阳泉市',1,'2023-06-15 14:19:20',1,1),(5,'考研单词书','考研背单词必备','[\"http://localhost:8080/image?imageName=file16868387852141005file16527758200311002考研单词书.jpg\"]','10.00','市辖区',4,'2023-06-15 14:20:34',2,1),(105,'袜子','没穿过。','[\"http://localhost:8080/image?imageName=file17023382374621002企业微信截图_20231212074317.png\"]','19.99','秦皇岛市',5,'2023-12-11 23:44:01',1,1);

/*Table structure for table `sh_message` */

DROP TABLE IF EXISTS `sh_message`;

CREATE TABLE `sh_message` (
  `id` bigint(20) NOT NULL AUTO_INCREMENT COMMENT '自增主键',
  `user_id` bigint(20) NOT NULL COMMENT '用户主键id',
  `idle_id` bigint(20) NOT NULL COMMENT '闲置主键id',
  `content` varchar(256) NOT NULL COMMENT '留言内容',
  `create_time` datetime NOT NULL COMMENT '留言时间',
  `to_user` bigint(20) NOT NULL COMMENT '所回复的用户',
  `to_message` bigint(20) DEFAULT NULL COMMENT '所回复的留言',
  PRIMARY KEY (`id`) USING BTREE,
  KEY `user_id_index` (`user_id`) USING BTREE,
  KEY `idle_id_index` (`idle_id`) USING BTREE,
  KEY `to_user_index` (`to_user`) USING BTREE
) ENGINE=InnoDB AUTO_INCREMENT=48 DEFAULT CHARSET=utf8 ROW_FORMAT=COMPACT;

/*Data for the table `sh_message` */

insert  into `sh_message`(`id`,`user_id`,`idle_id`,`content`,`create_time`,`to_user`,`to_message`) values (34,1,27,'测试测试','2021-09-03 01:24:00',7,NULL),(35,2,3,'测试测试','2021-09-10 00:55:32',1,NULL),(36,2,82,'俺想要','2022-08-09 20:30:12',2,NULL),(37,4,90,'一寸光阴一寸金，寸金难买寸光阴。','2022-10-22 22:18:53',3,NULL),(38,2,97,'不错不错','2023-01-08 15:50:23',2,NULL),(39,2,95,'我喜欢这个自行车，请问怎么进行提货啊','2023-01-08 17:53:13',2,NULL),(40,3,98,'你这个电风扇很不错啊，我很喜欢','2023-01-08 17:56:46',2,NULL),(41,3,97,'喜欢','2023-01-08 17:58:40',2,38),(42,2,97,'眼光不错','2023-01-08 18:00:23',3,41),(43,2,103,'1111','2023-09-10 03:40:15',1,NULL),(44,2,103,'111','2023-09-10 03:40:22',2,43),(45,1,102,'111111','2023-09-10 03:40:49',1,NULL),(46,1,102,'1111','2023-09-10 03:40:51',1,NULL),(47,1,102,'1111','2023-09-10 03:40:55',1,45);

/*Table structure for table `sh_order` */

DROP TABLE IF EXISTS `sh_order`;

CREATE TABLE `sh_order` (
  `id` bigint(20) NOT NULL AUTO_INCREMENT COMMENT '自增主键',
  `order_number` varchar(32) NOT NULL COMMENT '订单编号',
  `user_id` bigint(20) NOT NULL COMMENT '用户主键id',
  `idle_id` bigint(20) NOT NULL COMMENT '闲置物品主键id',
  `order_price` decimal(10,2) NOT NULL COMMENT '订单总价',
  `payment_status` tinyint(4) NOT NULL COMMENT '支付状态',
  `payment_way` varchar(16) DEFAULT NULL COMMENT '支付方式',
  `create_time` datetime NOT NULL COMMENT '创建时间',
  `payment_time` datetime DEFAULT NULL COMMENT '支付时间',
  `order_status` tinyint(4) NOT NULL COMMENT '订单状态',
  `is_deleted` tinyint(4) DEFAULT NULL COMMENT '是否删除',
  PRIMARY KEY (`id`) USING BTREE
) ENGINE=InnoDB AUTO_INCREMENT=86 DEFAULT CHARSET=utf8 ROW_FORMAT=COMPACT;

/*Data for the table `sh_order` */

insert  into `sh_order`(`id`,`order_number`,`user_id`,`idle_id`,`order_price`,`payment_status`,`payment_way`,`create_time`,`payment_time`,`order_status`,`is_deleted`) values (65,'166644845940310002',2,89,'52.00',0,NULL,'2022-10-22 22:20:59',NULL,4,NULL),(66,'166644847386610003',2,90,'100.00',0,NULL,'2022-10-22 22:21:13',NULL,4,NULL),(73,'167316390306810008',2,91,'20.00',0,NULL,'2023-01-08 15:45:03',NULL,4,NULL),(74,'167316390677010009',2,90,'100.00',0,NULL,'2023-01-08 15:45:06',NULL,4,NULL),(75,'167316391048610010',2,87,'52.00',0,NULL,'2023-01-08 15:45:10',NULL,4,NULL),(76,'167317079822610011',2,91,'20.00',0,NULL,'2023-01-08 17:39:58',NULL,0,NULL),(77,'167317080313310012',2,90,'100.00',0,NULL,'2023-01-08 17:40:03',NULL,0,NULL),(78,'167317080721110013',2,87,'52.00',0,NULL,'2023-01-08 17:40:07',NULL,0,NULL),(79,'167317197510610014',3,98,'30.00',1,'支付宝','2023-01-08 17:59:35','2023-01-08 17:59:39',2,NULL),(80,'169868139833810002',2,3,'120.00',0,NULL,'2023-10-30 15:56:38',NULL,4,NULL),(81,'169868142096810003',2,5,'10.00',0,NULL,'2023-10-30 15:57:01',NULL,4,NULL),(82,'169868167883610004',2,5,'10.00',0,NULL,'2023-10-30 16:01:19',NULL,4,NULL),(83,'169868176136110005',2,5,'10.00',1,'支付宝','2023-10-30 16:02:41','2023-10-30 16:03:06',1,NULL),(84,'170233774650610002',7,3,'120.00',0,NULL,'2023-12-11 23:35:47',NULL,0,NULL),(85,'170233778849310003',4,5,'10.00',1,'支付宝','2023-12-11 23:36:28','2023-12-11 23:37:07',3,NULL);

/*Table structure for table `sh_order_address` */

DROP TABLE IF EXISTS `sh_order_address`;

CREATE TABLE `sh_order_address` (
  `id` bigint(20) NOT NULL AUTO_INCREMENT COMMENT '自增id',
  `order_id` bigint(20) NOT NULL COMMENT '订单id',
  `consignee_name` varchar(32) NOT NULL COMMENT '收货人',
  `consignee_phone` varchar(32) NOT NULL COMMENT '电话',
  `detail_address` varchar(128) NOT NULL COMMENT '收货地址',
  PRIMARY KEY (`id`) USING BTREE,
  UNIQUE KEY `orderId` (`order_id`) USING BTREE,
  KEY `order_id_index` (`order_id`) USING BTREE
) ENGINE=InnoDB AUTO_INCREMENT=22 DEFAULT CHARSET=utf8 ROW_FORMAT=COMPACT;

/*Data for the table `sh_order_address` */

insert  into `sh_order_address`(`id`,`order_id`,`consignee_name`,`consignee_phone`,`detail_address`) values (1,58,'user9','13011110009','江苏省南通市崇川区花园一单元一号楼'),(2,59,'user8','13011110008','江苏省南通市崇川区888'),(3,60,'user9','13011110009','江苏省南通市崇川区花园一单元一号楼'),(4,62,'MZ','13011110000','河南省新乡市原阳县测试测试'),(5,61,'MZ','13011110000','河南省新乡市原阳县测试测试'),(6,63,'1211111111','1212','天津市市辖区河东区212'),(7,64,'1211111111','1212','天津市市辖区河东区212'),(8,65,'1211111111','1212','天津市市辖区河东区212'),(9,66,'1211111111','1212','天津市市辖区河东区212'),(10,73,'1211111111','1212','天津市市辖区河东区212'),(11,74,'1211111111','1212','天津市市辖区河东区212'),(12,75,'1211111111','1212','天津市市辖区河东区212'),(13,76,'1211111111','1212','天津市市辖区河东区212'),(14,77,'1211111111','1212','天津市市辖区河东区212'),(15,78,'1211111111','1212','天津市市辖区河东区212'),(16,79,'下雨','1788888888','天津市市辖区河西区北京'),(17,80,'1211111111','1212','天津市市辖区河东区212'),(18,81,'1211111111','1212','天津市市辖区河东区212'),(19,82,'1211111111','1212','天津市市辖区河东区212'),(20,83,'1211111111','1212','天津市市辖区河东区212'),(21,85,'下雨','1788888888','天津市市辖区河西区北京');

/*Table structure for table `sh_user` */

DROP TABLE IF EXISTS `sh_user`;

CREATE TABLE `sh_user` (
  `id` bigint(20) NOT NULL AUTO_INCREMENT COMMENT '自增主键',
  `account_number` varchar(16) NOT NULL COMMENT '账号（手机号）',
  `user_password` varchar(16) NOT NULL COMMENT '登录密码',
  `nickname` varchar(32) NOT NULL COMMENT '昵称',
  `avatar` varchar(256) NOT NULL COMMENT '头像',
  `sign_in_time` datetime NOT NULL COMMENT '注册时间',
  `user_status` tinyint(4) DEFAULT NULL COMMENT '状态（1代表封禁）',
  PRIMARY KEY (`id`) USING BTREE,
  UNIQUE KEY `account_number` (`account_number`) USING BTREE
) ENGINE=InnoDB AUTO_INCREMENT=11 DEFAULT CHARSET=utf8 ROW_FORMAT=COMPACT;

/*Data for the table `sh_user` */

insert  into `sh_user`(`id`,`account_number`,`user_password`,`nickname`,`avatar`,`sign_in_time`,`user_status`) values (1,'13011110000','123456','MZZ','http://localhost:8080/image?imageName=file168663143432210031.jpg','2021-09-01 10:49:01',0),(2,'10086','123456','沈一一','http://localhost:8080/image?imageName=file168663143432210030.png','2021-09-01 11:47:19',0),(3,'10087','123456','夏目','http://localhost:8080/image?imageName=file16731717831451008file16512163108731007tx10.jpg','2021-09-01 11:47:30',0),(4,'10088','123456','下雨','http://localhost:8080/image?imageName=file16664482422991023伞.jpg','2021-09-01 11:53:15',0),(5,'13011110004','123456','user4','http://localhost:8080/image?imageName=file16600445641151003109951165625541004.jpg','2021-09-01 12:21:50',1),(6,'13011110005','123456','user5','http://localhost:8080/image?imageName=file16600445641151003109951165625541004.jpg','2021-09-01 12:22:33',1),(7,'13011110006','123456','user6','http://localhost:8080/image?imageName=file16600445641151003109951165625541004.jpg','2021-09-01 12:23:15',0),(8,'13011110007','123456','user7','http://localhost:8080/image?imageName=file16600445641151003109951165625541004.jpg','2021-09-01 12:25:36',1),(9,'13011110008','123456','user8','http://localhost:8080/image?imageName=file16600445641151003109951165625541004.jpg','2021-09-01 13:24:04',1),(10,'13011110009','123456','user9','http://localhost:8080/image?imageName=file16600445641151003109951165625541004.jpg','2021-09-01 13:49:01',1);

/*!40101 SET SQL_MODE=@OLD_SQL_MODE */;
/*!40014 SET FOREIGN_KEY_CHECKS=@OLD_FOREIGN_KEY_CHECKS */;
/*!40014 SET UNIQUE_CHECKS=@OLD_UNIQUE_CHECKS */;
/*!40111 SET SQL_NOTES=@OLD_SQL_NOTES */;
