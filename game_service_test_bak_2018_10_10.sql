/*
 Navicat Premium Data Transfer

 Source Server         : test_game_service_db
 Source Server Type    : MySQL
 Source Server Version : 100213
 Source Host           : 10.200.201.14:3306
 Source Schema         : game_service

 Target Server Type    : MySQL
 Target Server Version : 100213
 File Encoding         : 65001

 Date: 10/10/2018 19:57:06
*/

SET NAMES utf8mb4;
SET FOREIGN_KEY_CHECKS = 0;

-- ----------------------------
-- Table structure for 1001_ds_check_id
-- ----------------------------
DROP TABLE IF EXISTS `1001_ds_check_id`;
CREATE TABLE `1001_ds_check_id`  (
  `id` varchar(50) CHARACTER SET utf8 COLLATE utf8_general_ci NOT NULL,
  `trans_id` varchar(200) CHARACTER SET utf8 COLLATE utf8_general_ci NOT NULL,
  `status` varchar(10) CHARACTER SET utf8 COLLATE utf8_general_ci NULL DEFAULT NULL,
  PRIMARY KEY (`id`) USING BTREE,
  UNIQUE INDEX `trans_id`(`trans_id`) USING BTREE
) ENGINE = InnoDB CHARACTER SET = utf8 COLLATE = utf8_general_ci ROW_FORMAT = Dynamic;

-- ----------------------------
-- Table structure for 1001_ds_member
-- ----------------------------
DROP TABLE IF EXISTS `1001_ds_member`;
CREATE TABLE `1001_ds_member`  (
  `id` bigint(20) NOT NULL AUTO_INCREMENT,
  `username` varchar(50) CHARACTER SET utf8 COLLATE utf8_bin NULL DEFAULT NULL,
  `site_id` int(11) NULL DEFAULT NULL COMMENT '网站名称',
  `site_name` varchar(12) CHARACTER SET utf8 COLLATE utf8_general_ci NULL DEFAULT NULL COMMENT '外接代理名称',
  `create_time` datetime(0) NULL DEFAULT NULL COMMENT '创建时间',
  `agents` varchar(50) CHARACTER SET utf8 COLLATE utf8_bin NULL DEFAULT NULL,
  `world` varchar(50) CHARACTER SET utf8 COLLATE utf8_bin NULL DEFAULT NULL,
  `corprator` varchar(50) CHARACTER SET utf8 COLLATE utf8_bin NULL DEFAULT NULL,
  `superior` varchar(50) CHARACTER SET utf8 COLLATE utf8_bin NULL DEFAULT NULL,
  `company` varchar(50) CHARACTER SET utf8 COLLATE utf8_bin NULL DEFAULT NULL,
  `comm_agent` decimal(18, 2) NULL DEFAULT NULL,
  `comm_world` decimal(18, 2) NULL DEFAULT NULL,
  `comm_corprator` decimal(18, 2) NULL DEFAULT NULL,
  `comm_superior` decimal(18, 2) NULL DEFAULT NULL,
  `comm_branch` decimal(18, 2) NULL DEFAULT NULL,
  `update_time` datetime(0) NULL DEFAULT NULL,
  PRIMARY KEY (`id`) USING BTREE,
  UNIQUE INDEX `username`(`username`, `site_id`) USING BTREE
) ENGINE = InnoDB AUTO_INCREMENT = 326492 CHARACTER SET = utf8 COLLATE = utf8_general_ci COMMENT = '会员代理信息' ROW_FORMAT = Dynamic;

-- ----------------------------
-- Table structure for 1001_ds_member_money
-- ----------------------------
DROP TABLE IF EXISTS `1001_ds_member_money`;
CREATE TABLE `1001_ds_member_money`  (
  `id` bigint(20) NOT NULL AUTO_INCREMENT COMMENT '网站标识',
  `site_id` int(11) NULL DEFAULT NULL,
  `username` varchar(50) CHARACTER SET utf8 COLLATE utf8_bin NULL DEFAULT NULL,
  `money` decimal(18, 5) NOT NULL COMMENT '用户金额',
  `password` varchar(32) CHARACTER SET utf8 COLLATE utf8_general_ci NULL DEFAULT NULL,
  `state` int(11) NULL DEFAULT 50,
  `version` int(11) NULL DEFAULT NULL,
  PRIMARY KEY (`id`) USING BTREE,
  UNIQUE INDEX `site_id`(`site_id`, `username`) USING BTREE
) ENGINE = InnoDB AUTO_INCREMENT = 673871 CHARACTER SET = utf8 COLLATE = utf8_general_ci ROW_FORMAT = Dynamic;

-- ----------------------------
-- Table structure for 1001_ds_member_money_log
-- ----------------------------
DROP TABLE IF EXISTS `1001_ds_member_money_log`;
CREATE TABLE `1001_ds_member_money_log`  (
  `id` bigint(20) NOT NULL AUTO_INCREMENT,
  `username` varchar(50) CHARACTER SET utf8 COLLATE utf8_bin NULL DEFAULT NULL,
  `hashcode` varchar(50) CHARACTER SET utf8 COLLATE utf8_general_ci NULL DEFAULT NULL,
  `site_id` int(11) NULL DEFAULT NULL COMMENT '网站标识',
  `sitename` varchar(50) CHARACTER SET utf8 COLLATE utf8_general_ci NULL DEFAULT NULL COMMENT '网站名称',
  `from_key` varchar(50) CHARACTER SET utf8 COLLATE utf8_general_ci NULL DEFAULT NULL COMMENT '请求者网站url',
  `request_ip` varchar(15) CHARACTER SET utf8 COLLATE utf8_general_ci NULL DEFAULT NULL,
  `trans_id` varchar(200) CHARACTER SET utf8 COLLATE utf8_general_ci NOT NULL,
  `from_key_type` int(11) NULL DEFAULT NULL,
  `before_money` decimal(20, 5) NULL DEFAULT NULL,
  `remit` decimal(20, 5) NOT NULL COMMENT '交易金额',
  `after_money` decimal(20, 5) NOT NULL COMMENT '交易后用户金额',
  `trans_type` varchar(3) CHARACTER SET utf8 COLLATE utf8_general_ci NOT NULL COMMENT '交易方式\r\n            in:转入          out：转出',
  `game_id` varchar(50) CHARACTER SET utf8 COLLATE utf8_general_ci NULL DEFAULT NULL,
  `game_type` varchar(50) CHARACTER SET utf8 COLLATE utf8_general_ci NULL DEFAULT NULL,
  `create_time` datetime(0) NOT NULL COMMENT '创建时间',
  `memo` varchar(200) CHARACTER SET utf8 COLLATE utf8_general_ci NULL DEFAULT NULL COMMENT '备注',
  PRIMARY KEY (`id`) USING BTREE,
  INDEX `site_id_index`(`site_id`) USING BTREE,
  INDEX `from_key_type_index`(`from_key_type`) USING BTREE,
  INDEX `username_index`(`username`) USING BTREE,
  INDEX `trans_id_index`(`trans_id`) USING BTREE,
  INDEX `create_time`(`create_time`) USING BTREE
) ENGINE = InnoDB AUTO_INCREMENT = 48061406 CHARACTER SET = utf8 COLLATE = utf8_general_ci ROW_FORMAT = Dynamic;

-- ----------------------------
-- Table structure for ag_api_user
-- ----------------------------
DROP TABLE IF EXISTS `ag_api_user`;
CREATE TABLE `ag_api_user`  (
  `id` int(11) NOT NULL AUTO_INCREMENT,
  `username` varchar(30) CHARACTER SET utf8 COLLATE utf8_general_ci NULL DEFAULT NULL COMMENT '用户名',
  `password` varchar(50) CHARACTER SET utf8 COLLATE utf8_general_ci NULL DEFAULT NULL COMMENT '密码',
  `api_info_id` int(3) NULL DEFAULT NULL COMMENT '对应 hasdCode id',
  `site_id` int(5) NULL DEFAULT NULL COMMENT '对应网站 id',
  `site_name` varchar(20) CHARACTER SET utf8 COLLATE utf8_general_ci NULL DEFAULT NULL COMMENT '网站名称',
  `agent_name` varchar(20) CHARACTER SET utf8 COLLATE utf8_general_ci NULL DEFAULT NULL COMMENT '代理名称',
  `oddtype` varchar(10) CHARACTER SET utf8 COLLATE utf8_general_ci NULL DEFAULT NULL COMMENT '限红',
  `currency_type` char(5) CHARACTER SET utf8 COLLATE utf8_general_ci NULL DEFAULT NULL COMMENT '币种',
  `create_time` varchar(35) CHARACTER SET utf8 COLLATE utf8_general_ci NULL DEFAULT NULL COMMENT '创建时间',
  `user_status` int(5) NULL DEFAULT NULL COMMENT '会员状态',
  PRIMARY KEY (`id`) USING BTREE,
  INDEX `username`(`username`) USING BTREE,
  INDEX `index_username`(`username`) USING BTREE
) ENGINE = InnoDB AUTO_INCREMENT = 617090 CHARACTER SET = utf8 COLLATE = utf8_general_ci ROW_FORMAT = Dynamic;

-- ----------------------------
-- Records of ag_api_user
-- ----------------------------
INSERT INTO `ag_api_user` VALUES (617087, 'yucstest004', '0cc538f369', 100228, 3000, 'dstest-ag', 'CS2_AGIN', 'A', 'CNY', '2018-08-10 16:28:38', 1);
INSERT INTO `ag_api_user` VALUES (617088, 'yucstest005', '0cc538f369', 100228, 3000, 'dstest-ag', 'CS2_AGIN', 'A', 'CNY', '2018-08-24 13:53:53', 1);
INSERT INTO `ag_api_user` VALUES (617089, 'yucstest006', '0cc538f369', 100228, 3000, 'dstest-ag', 'CS2_AGIN', 'A', 'CNY', '2018-08-24 14:11:11', 1);

-- ----------------------------
-- Table structure for api_info
-- ----------------------------
DROP TABLE IF EXISTS `api_info`;
CREATE TABLE `api_info`  (
  `id` bigint(20) NOT NULL COMMENT '唯一 id ',
  `hashcode` varchar(20) CHARACTER SET utf8 COLLATE utf8_general_ci NULL DEFAULT NULL COMMENT '唯一的 hashCode 标示',
  `site_id` int(11) NULL DEFAULT NULL COMMENT '网站名称',
  `site_name` varchar(20) CHARACTER SET utf8 COLLATE utf8_general_ci NULL DEFAULT NULL,
  `prefix` char(15) CHARACTER SET utf8 COLLATE utf8_general_ci NULL DEFAULT NULL COMMENT '项目前缀',
  `agent` varchar(50) CHARACTER SET utf8 COLLATE utf8_general_ci NULL DEFAULT NULL COMMENT '对接的代理',
  `web_url` varchar(50) CHARACTER SET utf8 COLLATE utf8_general_ci NULL DEFAULT NULL,
  `reporturl` varchar(50) CHARACTER SET utf8 COLLATE utf8_general_ci NULL DEFAULT NULL COMMENT '请求视讯的路径 ',
  `remark` varchar(200) CHARACTER SET utf8 COLLATE utf8_general_ci NULL DEFAULT NULL COMMENT '备注',
  `ip` varchar(200) CHARACTER SET utf8 COLLATE utf8_general_ci NULL DEFAULT NULL COMMENT '允许访问的 IP',
  `keyB` varchar(50) CHARACTER SET utf8 COLLATE utf8_general_ci NULL DEFAULT NULL COMMENT 'keyB 值',
  `state` smallint(6) NULL DEFAULT NULL COMMENT '0：未启用\r\n            50：正常\r\n            -50：已删除',
  `live_id` int(11) NULL DEFAULT NULL COMMENT '2:AG视讯厅\r\n            3:OG视讯厅\r\n            11:BBIN视讯厅\r\n            12:DS视讯厅\r\n            ',
  `live_name` varchar(20) CHARACTER SET utf8 COLLATE utf8_general_ci NULL DEFAULT NULL,
  `live_key` varchar(50) CHARACTER SET utf8 COLLATE utf8_general_ci NULL DEFAULT NULL,
  PRIMARY KEY (`id`) USING BTREE
) ENGINE = InnoDB CHARACTER SET = utf8 COLLATE = utf8_general_ci ROW_FORMAT = Dynamic;

-- ----------------------------
-- Table structure for bbin_api_user
-- ----------------------------
DROP TABLE IF EXISTS `bbin_api_user`;
CREATE TABLE `bbin_api_user`  (
  `id` int(11) NOT NULL AUTO_INCREMENT,
  `username` varchar(30) CHARACTER SET utf8 COLLATE utf8_general_ci NULL DEFAULT NULL COMMENT '用户名',
  `password` varchar(35) CHARACTER SET utf8 COLLATE utf8_general_ci NULL DEFAULT NULL COMMENT '密码',
  `agent_name` varchar(25) CHARACTER SET utf8 COLLATE utf8_general_ci NULL DEFAULT NULL COMMENT '视讯上级代理',
  `api_info_id` int(3) NULL DEFAULT NULL COMMENT '对应hashCode id',
  `site_id` int(5) NULL DEFAULT NULL COMMENT '对应网站 id',
  `site_name` varchar(20) CHARACTER SET utf8 COLLATE utf8_general_ci NULL DEFAULT NULL COMMENT '网站名称',
  `oddtype` varchar(10) CHARACTER SET utf8 COLLATE utf8_general_ci NULL DEFAULT NULL COMMENT '下注范围',
  `currency_type` char(6) CHARACTER SET utf8 COLLATE utf8_general_ci NULL DEFAULT NULL COMMENT '货币类型',
  `create_time` varchar(35) CHARACTER SET utf8 COLLATE utf8_general_ci NULL DEFAULT NULL COMMENT '创建时间',
  `user_status` int(2) NULL DEFAULT NULL COMMENT '会员状态',
  PRIMARY KEY (`id`) USING BTREE,
  INDEX `username`(`username`) USING BTREE,
  INDEX `index_username`(`username`) USING BTREE
) ENGINE = InnoDB AUTO_INCREMENT = 768863 CHARACTER SET = utf8 COLLATE = utf8_general_ci ROW_FORMAT = Dynamic;

-- ----------------------------
-- Records of bbin_api_user
-- ----------------------------
INSERT INTO `bbin_api_user` VALUES (768862, 'yucstest004', '122b620068', 'dyuceshi01', 100229, 3000, 'dstest-bbin', 'CNY', 'CNY', '2018-08-10 16:34:15', 1);

-- ----------------------------
-- Table structure for dictionary_table
-- ----------------------------
DROP TABLE IF EXISTS `dictionary_table`;
CREATE TABLE `dictionary_table`  (
  `id` int(10) NOT NULL AUTO_INCREMENT,
  `status_code` varchar(50) CHARACTER SET utf8 COLLATE utf8_general_ci NULL DEFAULT NULL COMMENT '状态码',
  `status_type` varchar(50) CHARACTER SET utf8 COLLATE utf8_general_ci NULL DEFAULT NULL COMMENT '状态类型',
  `status_desc` varchar(50) CHARACTER SET utf8 COLLATE utf8_general_ci NULL DEFAULT NULL COMMENT '状态描述',
  `parent_id` int(10) NULL DEFAULT NULL COMMENT '父节点id',
  PRIMARY KEY (`id`) USING BTREE
) ENGINE = MyISAM AUTO_INCREMENT = 215 CHARACTER SET = utf8 COLLATE = utf8_general_ci ROW_FORMAT = Dynamic;

-- ----------------------------
-- Table structure for ds_agent
-- ----------------------------
DROP TABLE IF EXISTS `ds_agent`;
CREATE TABLE `ds_agent`  (
  `id` bigint(20) NOT NULL AUTO_INCREMENT,
  `site_id` int(11) NULL DEFAULT NULL COMMENT '关联网站或公司管理员id',
  `agent_level` char(1) CHARACTER SET utf8 COLLATE utf8_general_ci NULL DEFAULT NULL COMMENT 'A 大股东 B 股东 c 总代理 d 代理',
  `loginname` varchar(30) CHARACTER SET utf8 COLLATE utf8_general_ci NULL DEFAULT NULL COMMENT '登录名',
  `alias` varchar(30) CHARACTER SET utf8 COLLATE utf8_general_ci NULL DEFAULT NULL COMMENT '别名',
  `status` tinyint(3) NULL DEFAULT 1 COMMENT '0停用-1启用-2冻结-3停权 0，1，2，3',
  `realname` varchar(30) CHARACTER SET utf8 COLLATE utf8_general_ci NULL DEFAULT NULL COMMENT '用户真实姓名',
  `username` varchar(50) CHARACTER SET utf8 COLLATE utf8_general_ci NULL DEFAULT NULL COMMENT '用户名',
  `game_json` varchar(60) CHARACTER SET utf8 COLLATE utf8_general_ci NULL DEFAULT NULL COMMENT '支持的游戏',
  `safe_password` varchar(32) CHARACTER SET utf8 COLLATE utf8_general_ci NULL DEFAULT NULL COMMENT '安全密码',
  `addtime` int(11) NULL DEFAULT NULL COMMENT '新增日期',
  `loginurl` varchar(200) CHARACTER SET utf8 COLLATE utf8_general_ci NULL DEFAULT NULL COMMENT '登录URl',
  `logintime` int(11) NULL DEFAULT NULL COMMENT '登录时间',
  `last_password_time` int(11) NULL DEFAULT NULL COMMENT '最后修改密码时间',
  `userpass` char(32) CHARACTER SET utf8 COLLATE utf8_general_ci NULL DEFAULT NULL COMMENT '用户密码',
  `loginip` varchar(15) CHARACTER SET utf8 COLLATE utf8_general_ci NULL DEFAULT NULL COMMENT '登录ip',
  `super` varchar(50) CHARACTER SET utf8 COLLATE utf8_general_ci NULL DEFAULT NULL COMMENT '所属大股东名',
  `corprator` varchar(50) CHARACTER SET utf8 COLLATE utf8_general_ci NULL DEFAULT NULL COMMENT '所属股东名',
  `world` varchar(50) CHARACTER SET utf8 COLLATE utf8_general_ci NULL DEFAULT NULL COMMENT '所属总代理名',
  `upid` int(11) NULL DEFAULT NULL COMMENT '所属上级ip,默认为零',
  `bank_id` int(11) NULL DEFAULT NULL COMMENT '银行id',
  `bank_adress` varchar(100) CHARACTER SET utf8 COLLATE utf8_general_ci NULL DEFAULT NULL COMMENT '银行地址',
  `bank_account` varbinary(200) NULL DEFAULT NULL COMMENT '银行帐号',
  `get_password` varchar(32) CHARACTER SET utf8 COLLATE utf8_general_ci NULL DEFAULT NULL COMMENT '提款密码',
  `email` varbinary(100) NULL DEFAULT NULL COMMENT '邮箱',
  `mobile` varbinary(100) NULL DEFAULT NULL COMMENT '手机',
  `phone` varbinary(100) NULL DEFAULT NULL COMMENT '电话',
  `adress` varchar(60) CHARACTER SET utf8 COLLATE utf8_general_ci NULL DEFAULT NULL COMMENT '地址',
  `qq` varbinary(100) NULL DEFAULT NULL COMMENT 'qq号码',
  `credit` decimal(15, 2) NULL DEFAULT 0.00 COMMENT '信用额度',
  `water_credit` decimal(10, 2) NOT NULL DEFAULT 0.00 COMMENT '返水额度',
  `user_type` tinyint(2) NULL DEFAULT 2 COMMENT '//用户类型，10主站正式会员，20副站正式会员，11主站试玩会员，21副站试玩会员，12主站测试会员，22副站测试会员',
  `credit_yh` decimal(10, 2) NULL DEFAULT NULL COMMENT '返水额度',
  `set_ts_json` varchar(300) CHARACTER SET utf8 COLLATE utf8_general_ci NULL DEFAULT NULL COMMENT '代理占成',
  `fingerprint` varchar(32) CHARACTER SET utf8 COLLATE utf8_general_ci NULL DEFAULT NULL COMMENT '指纹',
  `deposit` tinyint(3) NULL DEFAULT 1 COMMENT '是否允许会员存取款，1允许，2不允许,允许会员为现金，不允许会员可以信用存提',
  `mark_profit` tinyint(3) NULL DEFAULT 1 COMMENT '是否允许代理抽水会员，1允许，2不允许',
  `mobile_pass` tinyint(4) NULL DEFAULT 1 COMMENT '1为不加密，2为加密',
  `return_water_group_id` int(11) NULL DEFAULT NULL COMMENT '返水群组id',
  `my_live_ds` decimal(5, 2) NULL DEFAULT 0.00 COMMENT 'DS视讯抽点',
  `my_live_bb` decimal(5, 2) NULL DEFAULT 0.00 COMMENT 'BB视讯抽点',
  `my_live_ag` decimal(5, 2) NULL DEFAULT 0.00 COMMENT 'AG视讯抽点',
  `my_hongkong_jd` decimal(5, 2) NULL DEFAULT 0.00 COMMENT '经典香港彩抽点',
  `my_hongkong_ds` decimal(5, 2) NULL DEFAULT 0.00 COMMENT 'DS香港彩抽点',
  `my_lottery_jd` decimal(5, 2) NULL DEFAULT 0.00 COMMENT '经典彩票抽点',
  `my_lottery_ff` decimal(5, 2) NULL DEFAULT 0.00 COMMENT '分分彩票抽点',
  `my_lottery_ct` decimal(5, 2) NULL DEFAULT 0.00 COMMENT '传统彩抽点',
  `my_sport_h8` decimal(5, 2) NULL DEFAULT 0.00 COMMENT 'H8体育抽点',
  `my_sport_bb` decimal(5, 2) NULL DEFAULT 0.00 COMMENT 'BB体育抽点',
  `my_rate_ds` decimal(5, 2) NULL DEFAULT 0.00 COMMENT 'DS机率抽点',
  `my_rate_bb` decimal(5, 2) NULL DEFAULT 0.00 COMMENT 'BB机率抽点',
  `my_rate_ag` decimal(5, 2) NULL DEFAULT 0.00 COMMENT 'AG机率抽点',
  `my_water_detail` text CHARACTER SET utf8 COLLATE utf8_general_ci NULL COMMENT '大股东到代理,自己的返水方案',
  PRIMARY KEY (`id`) USING BTREE
) ENGINE = InnoDB AUTO_INCREMENT = 1 CHARACTER SET = utf8 COLLATE = utf8_general_ci COMMENT = '代理表' ROW_FORMAT = Dynamic;

-- ----------------------------
-- Table structure for ds_api_user
-- ----------------------------
DROP TABLE IF EXISTS `ds_api_user`;
CREATE TABLE `ds_api_user`  (
  `id` int(11) NOT NULL AUTO_INCREMENT,
  `username` varchar(30) CHARACTER SET utf8 COLLATE utf8_general_ci NULL DEFAULT NULL,
  `password` varchar(30) CHARACTER SET utf8 COLLATE utf8_general_ci NULL DEFAULT NULL,
  `api_info_id` int(5) NULL DEFAULT NULL,
  `site_id` int(5) NULL DEFAULT NULL,
  `oddtype` varchar(10) CHARACTER SET utf8 COLLATE utf8_general_ci NULL DEFAULT NULL,
  `currency_type` char(5) CHARACTER SET utf8 COLLATE utf8_general_ci NULL DEFAULT NULL,
  `create_time` varchar(35) CHARACTER SET utf8 COLLATE utf8_general_ci NULL DEFAULT NULL,
  `user_status` int(2) NULL DEFAULT NULL,
  `uppername` varchar(20) CHARACTER SET utf8 COLLATE utf8_general_ci NULL DEFAULT NULL,
  PRIMARY KEY (`id`) USING BTREE
) ENGINE = InnoDB AUTO_INCREMENT = 1 CHARACTER SET = utf8 COLLATE = utf8_general_ci ROW_FORMAT = Dynamic;

-- ----------------------------
-- Table structure for ds_from_key_type
-- ----------------------------
DROP TABLE IF EXISTS `ds_from_key_type`;
CREATE TABLE `ds_from_key_type`  (
  `id` bigint(20) NOT NULL,
  `type_name` varchar(20) CHARACTER SET utf8 COLLATE utf8_general_ci NULL DEFAULT NULL,
  `fk_from_key` varchar(50) CHARACTER SET utf8 COLLATE utf8_general_ci NULL DEFAULT NULL,
  `game_type` varchar(50) CHARACTER SET utf8 COLLATE utf8_general_ci NULL DEFAULT NULL,
  `state` int(11) NULL DEFAULT 50,
  PRIMARY KEY (`id`) USING BTREE
) ENGINE = InnoDB CHARACTER SET = utf8 COLLATE = utf8_general_ci ROW_FORMAT = Dynamic;

-- ----------------------------
-- Records of ds_from_key_type
-- ----------------------------
INSERT INTO `ds_from_key_type` VALUES (2000, '下注', 'ds_money_key', 'LIVE_OUT', 50);
INSERT INTO `ds_from_key_type` VALUES (2001, '派彩', 'ds_money_key', 'LIVE_IN', 50);
INSERT INTO `ds_from_key_type` VALUES (2002, '和局返还本金', 'ds_money_key', NULL, 50);
INSERT INTO `ds_from_key_type` VALUES (2003, '注单撤销', 'ds_money_key', NULL, 50);
INSERT INTO `ds_from_key_type` VALUES (10001, '公司入款', 'ds_money_key', NULL, 50);
INSERT INTO `ds_from_key_type` VALUES (10002, '公司入款优惠', 'ds_money_key', NULL, 50);
INSERT INTO `ds_from_key_type` VALUES (10003, '公司汇款优惠', 'ds_money_key', NULL, 50);
INSERT INTO `ds_from_key_type` VALUES (10004, '返点', 'ds_money_key', NULL, 50);
INSERT INTO `ds_from_key_type` VALUES (10005, '线上存款', 'ds_money_key', NULL, 50);
INSERT INTO `ds_from_key_type` VALUES (10006, '线上存款手续费', 'ds_money_key', NULL, 50);
INSERT INTO `ds_from_key_type` VALUES (10007, '线上存款优惠', 'ds_money_key', NULL, 50);
INSERT INTO `ds_from_key_type` VALUES (10008, '人工存入', 'ds_money_key', NULL, 50);
INSERT INTO `ds_from_key_type` VALUES (10009, '出款', 'ds_money_key', NULL, 50);
INSERT INTO `ds_from_key_type` VALUES (10010, '系统取消出款', 'ds_money_key', NULL, 50);
INSERT INTO `ds_from_key_type` VALUES (10011, '人工存入取消出款', 'ds_money_key', NULL, 50);
INSERT INTO `ds_from_key_type` VALUES (10012, '重复出款', 'ds_money_key', NULL, 50);
INSERT INTO `ds_from_key_type` VALUES (10013, '沖銷', 'ds_money_key', NULL, 50);
INSERT INTO `ds_from_key_type` VALUES (10014, '会员出款被扣除金额', 'ds_money_key', NULL, 50);
INSERT INTO `ds_from_key_type` VALUES (10015, '给予反水', 'ds_money_key', NULL, 50);
INSERT INTO `ds_from_key_type` VALUES (10016, '存款优惠', 'ds_money_key', NULL, 50);
INSERT INTO `ds_from_key_type` VALUES (10017, '汇款优惠', 'ds_money_key', NULL, 50);
INSERT INTO `ds_from_key_type` VALUES (10018, '退佣优惠', 'ds_money_key', NULL, 50);
INSERT INTO `ds_from_key_type` VALUES (10019, '负数额度归零', 'ds_money_key', NULL, 50);
INSERT INTO `ds_from_key_type` VALUES (10020, '写入退佣费用', 'ds_money_key', NULL, 50);
INSERT INTO `ds_from_key_type` VALUES (10021, '其他人工存入', 'ds_money_key', NULL, 50);
INSERT INTO `ds_from_key_type` VALUES (10022, '手动申请出款', 'ds_money_key', NULL, 50);
INSERT INTO `ds_from_key_type` VALUES (10023, '放弃存款优惠', 'ds_money_key', NULL, 50);
INSERT INTO `ds_from_key_type` VALUES (10024, '公司入款误存', 'ds_money_key', NULL, 50);
INSERT INTO `ds_from_key_type` VALUES (10025, '会员负数回冲', 'ds_money_key', NULL, 50);
INSERT INTO `ds_from_key_type` VALUES (10026, '扣除非法下注派彩', 'ds_money_key', NULL, 50);
INSERT INTO `ds_from_key_type` VALUES (10027, '其他人工提出', 'ds_money_key', NULL, 50);
INSERT INTO `ds_from_key_type` VALUES (10028, '系统-新增快开额度', 'ds_money_key', NULL, 50);
INSERT INTO `ds_from_key_type` VALUES (10029, '活动优惠', 'ds_money_key', NULL, 50);
INSERT INTO `ds_from_key_type` VALUES (10030, '返点优惠', 'ds_money_key', NULL, 50);
INSERT INTO `ds_from_key_type` VALUES (10031, '回复删单', 'ds_money_key', NULL, 50);
INSERT INTO `ds_from_key_type` VALUES (10032, '删单', 'ds_money_key', NULL, 50);
INSERT INTO `ds_from_key_type` VALUES (10033, '手续费扣除', 'ds_money_key', NULL, 50);
INSERT INTO `ds_from_key_type` VALUES (10034, '优惠金额扣除', 'ds_money_key', NULL, 50);
INSERT INTO `ds_from_key_type` VALUES (10035, '球类返点', 'ds_money_key', NULL, 50);
INSERT INTO `ds_from_key_type` VALUES (10036, '视讯返点', 'ds_money_key', NULL, 50);
INSERT INTO `ds_from_key_type` VALUES (10037, '体育返点', 'ds_money_key', NULL, 50);
INSERT INTO `ds_from_key_type` VALUES (10038, '机率返点', 'ds_money_key', NULL, 50);
INSERT INTO `ds_from_key_type` VALUES (10039, '彩票返点', 'ds_money_key', NULL, 50);
INSERT INTO `ds_from_key_type` VALUES (10040, '球类冲销', 'ds_money_key', NULL, 50);
INSERT INTO `ds_from_key_type` VALUES (10041, '视讯冲销', 'ds_money_key', NULL, 50);
INSERT INTO `ds_from_key_type` VALUES (10042, '体育冲销', 'ds_money_key', NULL, 50);
INSERT INTO `ds_from_key_type` VALUES (10043, '机率冲销', 'ds_money_key', NULL, 50);
INSERT INTO `ds_from_key_type` VALUES (10044, '彩票冲销', 'ds_money_key', NULL, 50);
INSERT INTO `ds_from_key_type` VALUES (10045, '佣金返还', 'ds_money_key', NULL, 50);
INSERT INTO `ds_from_key_type` VALUES (10046, '佣金注销', 'ds_money_key', NULL, 50);
INSERT INTO `ds_from_key_type` VALUES (10047, '奖金提取', 'ds_money_key', NULL, 50);
INSERT INTO `ds_from_key_type` VALUES (10048, '上线获得返点', 'ds_money_key', NULL, 50);
INSERT INTO `ds_from_key_type` VALUES (10049, '上线获得返点冲销', 'ds_money_key', NULL, 50);
INSERT INTO `ds_from_key_type` VALUES (10050, '信用存入', 'ds_money_key', NULL, 50);
INSERT INTO `ds_from_key_type` VALUES (10051, '信用取出', 'ds_money_key', NULL, 50);
INSERT INTO `ds_from_key_type` VALUES (10052, '推广好友优惠', 'ds_money_key', NULL, 50);
INSERT INTO `ds_from_key_type` VALUES (10053, '现金存入', 'ds_money_key', NULL, 50);
INSERT INTO `ds_from_key_type` VALUES (10054, '现金提出', 'ds_money_key', NULL, 50);
INSERT INTO `ds_from_key_type` VALUES (10055, '信用存入失败返还', 'ds_money_key', NULL, 50);
INSERT INTO `ds_from_key_type` VALUES (10056, '现金存入失败返还', 'ds_money_key', NULL, 50);
INSERT INTO `ds_from_key_type` VALUES (10060, '代理返水', 'ds_money_key', NULL, 50);
INSERT INTO `ds_from_key_type` VALUES (10061, '除去代理返水', 'ds_money_key', NULL, 50);
INSERT INTO `ds_from_key_type` VALUES (10062, '总代理返水', 'ds_money_key', NULL, 50);
INSERT INTO `ds_from_key_type` VALUES (10063, '除去总代理返水', 'ds_money_key', NULL, 50);
INSERT INTO `ds_from_key_type` VALUES (10064, '股东返水', 'ds_money_key', NULL, 50);
INSERT INTO `ds_from_key_type` VALUES (10065, '除去股东返水', 'ds_money_key', NULL, 50);
INSERT INTO `ds_from_key_type` VALUES (10066, '大股东返水', 'ds_money_key', NULL, 50);
INSERT INTO `ds_from_key_type` VALUES (10067, '除去大股东返水', 'ds_money_key', NULL, 50);
INSERT INTO `ds_from_key_type` VALUES (20001, '主账户转至BBIN投注', 'ds_money_key', NULL, 50);
INSERT INTO `ds_from_key_type` VALUES (20002, '主账户转至AG投注', 'ds_money_key', NULL, 50);
INSERT INTO `ds_from_key_type` VALUES (20003, '主账户转至H8投注', 'ds_money_key', NULL, 50);
INSERT INTO `ds_from_key_type` VALUES (20004, '由BBIN转入主账户', 'ds_money_key', NULL, 50);
INSERT INTO `ds_from_key_type` VALUES (20005, '由AG转入主账户', 'ds_money_key', NULL, 50);
INSERT INTO `ds_from_key_type` VALUES (20006, '由H8转入主账户', 'ds_money_key', NULL, 50);
INSERT INTO `ds_from_key_type` VALUES (20007, '转至BBIN失败-返还', 'ds_money_key', NULL, 50);
INSERT INTO `ds_from_key_type` VALUES (20008, '转至AG失败-返还', 'ds_money_key', NULL, 50);
INSERT INTO `ds_from_key_type` VALUES (20009, '转至H8失败-返还', 'ds_money_key', NULL, 50);
INSERT INTO `ds_from_key_type` VALUES (20010, '转至OG', 'ds_money_key', NULL, 50);
INSERT INTO `ds_from_key_type` VALUES (20011, '由OG转入DS主账户', 'ds_money_key', NULL, 50);
INSERT INTO `ds_from_key_type` VALUES (20012, 'DS主账户转至OG失败-返还', 'ds_money_key', NULL, 50);
INSERT INTO `ds_from_key_type` VALUES (20013, '主账户转账至MG', 'ds_money_key', NULL, 50);
INSERT INTO `ds_from_key_type` VALUES (20014, 'MG转账至主账户', 'ds_money_key', NULL, 50);
INSERT INTO `ds_from_key_type` VALUES (20015, 'PT账户转至主账户', 'ds_money_key', NULL, 50);
INSERT INTO `ds_from_key_type` VALUES (20016, '主账户转至PT账户', 'ds_money_key', NULL, 50);
INSERT INTO `ds_from_key_type` VALUES (20017, 'PMG账户转至主账户', 'ds_money_key', NULL, 50);
INSERT INTO `ds_from_key_type` VALUES (20018, '主账户转至PMG账户', 'ds_money_key', NULL, 50);
INSERT INTO `ds_from_key_type` VALUES (20019, 'LMG账户转至主账户', 'ds_money_key', NULL, 50);
INSERT INTO `ds_from_key_type` VALUES (20020, '主账户转至LMG账户', 'ds_money_key', NULL, 50);
INSERT INTO `ds_from_key_type` VALUES (30001, 'BBIN投注', 'ds_money_key', NULL, 50);
INSERT INTO `ds_from_key_type` VALUES (30002, 'AG投注', 'ds_money_key', NULL, 50);
INSERT INTO `ds_from_key_type` VALUES (30003, 'M8投注', 'ds_money_key', NULL, 50);
INSERT INTO `ds_from_key_type` VALUES (30004, 'BBIN派彩', 'ds_money_key', NULL, 50);
INSERT INTO `ds_from_key_type` VALUES (30005, 'AG派彩', 'ds_money_key', NULL, 50);
INSERT INTO `ds_from_key_type` VALUES (30006, 'H8派彩', 'ds_money_key', NULL, 50);
INSERT INTO `ds_from_key_type` VALUES (30007, 'DS视讯投注', 'ds_money_key', NULL, 50);
INSERT INTO `ds_from_key_type` VALUES (30008, 'DS视讯派彩', 'ds_money_key', NULL, 50);
INSERT INTO `ds_from_key_type` VALUES (30009, 'DS传统彩票投注', 'ds_money_key', 'LOTTERY_OUT', 50);
INSERT INTO `ds_from_key_type` VALUES (30010, 'DS传统彩票派彩', 'ds_money_key', 'LOTTERY_IN', 50);
INSERT INTO `ds_from_key_type` VALUES (30011, 'DS电子游戏投注', 'ds_money_key', NULL, 50);
INSERT INTO `ds_from_key_type` VALUES (30012, 'DS电子游戏派彩', 'ds_money_key', NULL, 50);
INSERT INTO `ds_from_key_type` VALUES (30013, 'DS香港彩投注', 'ds_money_key', 'LOTTO_OUT', 50);
INSERT INTO `ds_from_key_type` VALUES (30014, 'DS香港彩派彩', 'ds_money_key', 'LOTTO_IN', 50);
INSERT INTO `ds_from_key_type` VALUES (30015, 'DS经典彩投注', 'ds_money_key', NULL, 50);
INSERT INTO `ds_from_key_type` VALUES (30016, 'DS经典彩派彩', 'ds_money_key', NULL, 50);
INSERT INTO `ds_from_key_type` VALUES (50001, 'DS分分彩下注', 'ds_money_key', NULL, 50);
INSERT INTO `ds_from_key_type` VALUES (50002, 'DS分分彩派彩', 'ds_money_key', NULL, 50);
INSERT INTO `ds_from_key_type` VALUES (50004, '彩票取消返还本金', 'ds_money_key', NULL, 50);
INSERT INTO `ds_from_key_type` VALUES (50005, '彩票反结算', 'ds_money_key', NULL, 50);
INSERT INTO `ds_from_key_type` VALUES (60000, '代理平台转入至KG主账户', 'ds_money_key', NULL, 50);
INSERT INTO `ds_from_key_type` VALUES (60001, 'KG主账户转出至代理平台', 'ds_money_key', NULL, 50);
INSERT INTO `ds_from_key_type` VALUES (80000, '转入到经典彩', 'ds_money_key', NULL, 50);
INSERT INTO `ds_from_key_type` VALUES (80001, '从经典彩转出', 'ds_money_key', NULL, 50);

-- ----------------------------
-- Table structure for ds_game_info
-- ----------------------------
DROP TABLE IF EXISTS `ds_game_info`;
CREATE TABLE `ds_game_info`  (
  `id` int(11) NOT NULL AUTO_INCREMENT,
  `fk_from_key_id` int(11) NULL DEFAULT NULL,
  `game_type` varchar(50) CHARACTER SET utf8 COLLATE utf8_general_ci NULL DEFAULT NULL,
  `game_name` varchar(50) CHARACTER SET utf8 COLLATE utf8_general_ci NULL DEFAULT NULL,
  `state` int(11) NULL DEFAULT NULL,
  `memo` varchar(50) CHARACTER SET utf8 COLLATE utf8_general_ci NULL DEFAULT NULL,
  PRIMARY KEY (`id`) USING BTREE
) ENGINE = InnoDB AUTO_INCREMENT = 31 CHARACTER SET = utf8 COLLATE = utf8_general_ci ROW_FORMAT = Dynamic;

-- ----------------------------
-- Records of ds_game_info
-- ----------------------------
INSERT INTO `ds_game_info` VALUES (1, NULL, 'KLC', '广东快乐十分', 50, '');
INSERT INTO `ds_game_info` VALUES (2, NULL, 'BJC', '北京赛车(PK10)', 50, '');
INSERT INTO `ds_game_info` VALUES (3, NULL, 'JSC', '江苏骰宝(快3)', 50, '');
INSERT INTO `ds_game_info` VALUES (4, NULL, 'XYC', '幸运农场', 50, '');
INSERT INTO `ds_game_info` VALUES (5, NULL, 'SSC', '重庆时时彩', 50, '');
INSERT INTO `ds_game_info` VALUES (6, NULL, '1', '百家乐', 50, 'gameType是桌号');
INSERT INTO `ds_game_info` VALUES (7, NULL, '8', '免佣百家乐', 50, 'gameType是桌号');
INSERT INTO `ds_game_info` VALUES (8, NULL, '4', '百家乐', 50, 'gameType是桌号');
INSERT INTO `ds_game_info` VALUES (10, NULL, '9', '极速百家乐', 50, 'gameType是桌号');
INSERT INTO `ds_game_info` VALUES (12, NULL, '2', '百家乐', 50, 'gameType是桌号');
INSERT INTO `ds_game_info` VALUES (13, NULL, '3', '百家乐', 50, 'gameType是桌号');
INSERT INTO `ds_game_info` VALUES (14, NULL, '5', '龙虎', 50, 'gameType是桌号');
INSERT INTO `ds_game_info` VALUES (15, NULL, '6', '骰宝', 50, 'gameType是桌号');
INSERT INTO `ds_game_info` VALUES (16, NULL, '7', '百家乐', 50, 'gameType是桌号');
INSERT INTO `ds_game_info` VALUES (17, NULL, 'LOTTO', '香港彩', 50, NULL);
INSERT INTO `ds_game_info` VALUES (18, NULL, 'TJSC', '天津时时彩', 50, NULL);
INSERT INTO `ds_game_info` VALUES (19, NULL, 'XJSC', '新疆时时彩', 50, NULL);
INSERT INTO `ds_game_info` VALUES (20, NULL, 'JXSC', '江西时时彩', 50, NULL);
INSERT INTO `ds_game_info` VALUES (21, NULL, 'YNSC', '云南时时彩', 50, NULL);
INSERT INTO `ds_game_info` VALUES (22, NULL, 'SHSC', '上海时时彩', 50, NULL);
INSERT INTO `ds_game_info` VALUES (23, NULL, 'TJKC', '天津快乐十分', 50, NULL);
INSERT INTO `ds_game_info` VALUES (24, NULL, 'GXKC', '广西快乐十分', 50, NULL);
INSERT INTO `ds_game_info` VALUES (25, NULL, 'HNKC', '湖南快乐十分', 50, NULL);
INSERT INTO `ds_game_info` VALUES (26, NULL, 'AHK3', '安徽快3', 50, NULL);
INSERT INTO `ds_game_info` VALUES (27, NULL, 'GXK3', '广西快3', 50, NULL);
INSERT INTO `ds_game_info` VALUES (28, NULL, 'JLK3', '吉林快3', 50, NULL);
INSERT INTO `ds_game_info` VALUES (29, NULL, '11', '轮盘', 50, 'gameType是桌号');
INSERT INTO `ds_game_info` VALUES (30, NULL, '10', '斗牛', 50, 'gameType是桌号');

-- ----------------------------
-- Table structure for ds_ip_list
-- ----------------------------
DROP TABLE IF EXISTS `ds_ip_list`;
CREATE TABLE `ds_ip_list`  (
  `id` int(11) NOT NULL AUTO_INCREMENT,
  `ip` varchar(15) CHARACTER SET utf8 COLLATE utf8_general_ci NOT NULL DEFAULT '',
  `name` varchar(50) CHARACTER SET utf8 COLLATE utf8_general_ci NULL DEFAULT NULL,
  `state` int(11) NULL DEFAULT NULL,
  `memo` varchar(50) CHARACTER SET utf8 COLLATE utf8_general_ci NULL DEFAULT NULL,
  PRIMARY KEY (`id`) USING BTREE
) ENGINE = InnoDB AUTO_INCREMENT = 137 CHARACTER SET = utf8 COLLATE = utf8_general_ci ROW_FORMAT = Dynamic;

-- ----------------------------
-- Table structure for ds_money_config
-- ----------------------------
DROP TABLE IF EXISTS `ds_money_config`;
CREATE TABLE `ds_money_config`  (
  `id` bigint(20) NOT NULL AUTO_INCREMENT,
  `from_key` varchar(30) CHARACTER SET utf8 COLLATE utf8_general_ci NULL DEFAULT NULL,
  `web_id` int(11) NULL DEFAULT NULL,
  `web_name` varchar(30) CHARACTER SET utf8 COLLATE utf8_general_ci NULL DEFAULT NULL,
  `hashcode` varchar(30) CHARACTER SET utf8 COLLATE utf8_general_ci NULL DEFAULT NULL,
  `iplist` varchar(1000) CHARACTER SET utf8 COLLATE utf8_general_ci NULL DEFAULT NULL,
  `state` smallint(6) NULL DEFAULT NULL,
  PRIMARY KEY (`id`) USING BTREE
) ENGINE = InnoDB AUTO_INCREMENT = 3 CHARACTER SET = utf8 COLLATE = utf8_general_ci ROW_FORMAT = Dynamic;

-- ----------------------------
-- Records of ds_money_config
-- ----------------------------
INSERT INTO `ds_money_config` VALUES (1, 'ds_money_key', 1001, '鸿运', NULL, '203.88.171.175,103.9.188.157,203.88.160.144,103.21.171.214,119.9.108.252,103.242.14.68,103.242.14.90,119.28.6.130,119.28.13.134,119.9.92.211,119.9.92.77,119.28.13.102,119.9.108.150,119.9.105.91,119.9.77.169,119.9.114.10,10.189.254.10', 50);
INSERT INTO `ds_money_config` VALUES (2, 'ds_money_key', 9999, '线上测试', NULL, '192.168.0.1', 50);

-- ----------------------------
-- Table structure for ds_site_hashcode
-- ----------------------------
DROP TABLE IF EXISTS `ds_site_hashcode`;
CREATE TABLE `ds_site_hashcode`  (
  `site_id` int(11) NOT NULL,
  `hashcode` varchar(50) CHARACTER SET utf8 COLLATE utf8_general_ci NULL DEFAULT NULL
) ENGINE = InnoDB CHARACTER SET = utf8 COLLATE = utf8_general_ci ROW_FORMAT = Dynamic;

-- ----------------------------
-- Records of ds_site_hashcode
-- ----------------------------
INSERT INTO `ds_site_hashcode` VALUES (1001, 'hongfa-wqp84spe-fefae-ape09nsd-iejjw');
INSERT INTO `ds_site_hashcode` VALUES (1001, 'hongfatest-testopqpsee-pqwwageng-soode');
INSERT INTO `ds_site_hashcode` VALUES (1002, 'aomenyulechengtest-dlorep-oyieuw-dleow');
INSERT INTO `ds_site_hashcode` VALUES (1002, 'aomenyulecheng-sdlkrow-hltiepw-bbbplt');
INSERT INTO `ds_site_hashcode` VALUES (99999, 'dsjishucs-xleosi-rlofoe-yorrr-wwdgh88');
INSERT INTO `ds_site_hashcode` VALUES (1003, 'yitongyulecheng-dlekwo-vofke-rdkeo');
INSERT INTO `ds_site_hashcode` VALUES (1004, 'sijiyucheng-elwocl-puorle-bmldpe');
INSERT INTO `ds_site_hashcode` VALUES (1005, 'ds100588-scvrow-hffepw-byilt');
INSERT INTO `ds_site_hashcode` VALUES (1007, 'ds100788-mlpok-ijnbhu-ygvctf');
INSERT INTO `ds_site_hashcode` VALUES (1006, 'ds100666-poiuy-rgile-nmosd');
INSERT INTO `ds_site_hashcode` VALUES (1008, 'ds100888-nmlde-hokew-nmlfo');
INSERT INTO `ds_site_hashcode` VALUES (1009, 'ds100999-misdf-weflw-eryoe');
INSERT INTO `ds_site_hashcode` VALUES (1010, 'ds101010_rleoe-modfu-woeud');
INSERT INTO `ds_site_hashcode` VALUES (1011, 'ds101116-mlodf-moier-weriyu');
INSERT INTO `ds_site_hashcode` VALUES (1012, 'ds101201-lioweu-qwovi-nmloo');
INSERT INTO `ds_site_hashcode` VALUES (1013, 'ds101314-rwqorl-fgfgl-blwoq');
INSERT INTO `ds_site_hashcode` VALUES (1014, 'ds101413-mlope-diufer-cvmeo');
INSERT INTO `ds_site_hashcode` VALUES (1015, 'ds101565-hgfkd-eruoy-norur');
INSERT INTO `ds_site_hashcode` VALUES (1016, 'ds101688-ytrew-poigt-okmfr');
INSERT INTO `ds_site_hashcode` VALUES (1017, 'ds101759-lvpew-tiuyr-coflkr');
INSERT INTO `ds_site_hashcode` VALUES (1025, 'ds102588-rtui5-fgor3-56fhr');
INSERT INTO `ds_site_hashcode` VALUES (1022, 'ds102299-dlreo-kgfoe-eorui');
INSERT INTO `ds_site_hashcode` VALUES (1024, 'ds102400-seirg-covur-noftr-piore');
INSERT INTO `ds_site_hashcode` VALUES (1026, 'ds102636-1e809-48ee3-dc2ad');
INSERT INTO `ds_site_hashcode` VALUES (1019, 'ds101996-04e1d-654bc-0a482');
INSERT INTO `ds_site_hashcode` VALUES (1029, 'ds102998-63d9b-5474a-25c30');
INSERT INTO `ds_site_hashcode` VALUES (1023, 'ds102303-43e61-d0424-944fa');
INSERT INTO `ds_site_hashcode` VALUES (1027, 'ds102775-b957d-a6d24-a4d54');
INSERT INTO `ds_site_hashcode` VALUES (1018, 'ds101899-3851a-5d786-936fe');
INSERT INTO `ds_site_hashcode` VALUES (1030, 'ds103030-12687-a653a-214c6');
INSERT INTO `ds_site_hashcode` VALUES (1031, 'ds103141-937d3-827ec-03721');
INSERT INTO `ds_site_hashcode` VALUES (1021, 'ds102132-8c66e-d234b-3e9a3');
INSERT INTO `ds_site_hashcode` VALUES (1015, 'ds101555-d8d8a-9ed10-58503');
INSERT INTO `ds_site_hashcode` VALUES (1032, 'ds103266-0ceed-17db0-e4e24');
INSERT INTO `ds_site_hashcode` VALUES (1033, 'ds103333-0d93f-cdf72-30867');
INSERT INTO `ds_site_hashcode` VALUES (1035, 'ds103535 -66a20-486be-97a7a');
INSERT INTO `ds_site_hashcode` VALUES (1036, 'ds103668 -330b1-9ffeb-b0571');
INSERT INTO `ds_site_hashcode` VALUES (1037, 'ds103737 -1d47e-0fa98-3dc10');
INSERT INTO `ds_site_hashcode` VALUES (1038, 'ds103838-70b82-d9c7c-377d8');
INSERT INTO `ds_site_hashcode` VALUES (1040, 'ds104040-76760-33ac7-7f1ff');
INSERT INTO `ds_site_hashcode` VALUES (1044, 'ds104444-29a26-cdc5b-ae27b');
INSERT INTO `ds_site_hashcode` VALUES (1043, 'ds104343-3b904-683c9-389be');
INSERT INTO `ds_site_hashcode` VALUES (1039, 'ds103939-1b40a-221d5-88dfc');
INSERT INTO `ds_site_hashcode` VALUES (1041, 'ds104141-9e307-9fec8-821e6');
INSERT INTO `ds_site_hashcode` VALUES (1042, 'ds104242-c6cdd-4037e-8da16');
INSERT INTO `ds_site_hashcode` VALUES (1045, 'ds104545-3a9ed-b556e-d115a');
INSERT INTO `ds_site_hashcode` VALUES (1028, 'ds102828-486cc-e3eab-c39a1');
INSERT INTO `ds_site_hashcode` VALUES (1046, 'ds104646-9fcee-7c32f-08655');
INSERT INTO `ds_site_hashcode` VALUES (1047, 'ds104747-1da72-6b819-992ad');
INSERT INTO `ds_site_hashcode` VALUES (1048, 'ds104848-96f60-fa6ff-0b4b9');
INSERT INTO `ds_site_hashcode` VALUES (1049, 'ds104949-328b6-d7249-15b36');
INSERT INTO `ds_site_hashcode` VALUES (1050, 'ds105050-498fd-76c6c-80f37');
INSERT INTO `ds_site_hashcode` VALUES (1051, 'ds105151-b33a2-51746-46650');
INSERT INTO `ds_site_hashcode` VALUES (1052, 'ds105252-60a65-5c052-a7f36');
INSERT INTO `ds_site_hashcode` VALUES (1054, 'ds105454-981cb-e5e41-abc5e');
INSERT INTO `ds_site_hashcode` VALUES (1053, 'ds105353-a2912-b58e3-6d12b');
INSERT INTO `ds_site_hashcode` VALUES (1055, 'ds105555-52264-19c6c-1b90e');
INSERT INTO `ds_site_hashcode` VALUES (1056, 'ds105656-858db-d8607-5e6a9');
INSERT INTO `ds_site_hashcode` VALUES (1057, 'ds105757-d0b5e-9e65a-f79c4');
INSERT INTO `ds_site_hashcode` VALUES (1058, 'ds105858-ddde4-06d0d-04b2b');
INSERT INTO `ds_site_hashcode` VALUES (1059, 'ds105959-8bf95-f5d53-e939e');

-- ----------------------------
-- Table structure for dubbo_invoke
-- ----------------------------
DROP TABLE IF EXISTS `dubbo_invoke`;
CREATE TABLE `dubbo_invoke`  (
  `id` varchar(255) CHARACTER SET utf8 COLLATE utf8_general_ci NOT NULL DEFAULT '',
  `invoke_date` date NOT NULL,
  `service` varchar(255) CHARACTER SET utf8 COLLATE utf8_general_ci NULL DEFAULT NULL,
  `method` varchar(255) CHARACTER SET utf8 COLLATE utf8_general_ci NULL DEFAULT NULL,
  `consumer` varchar(255) CHARACTER SET utf8 COLLATE utf8_general_ci NULL DEFAULT NULL,
  `provider` varchar(255) CHARACTER SET utf8 COLLATE utf8_general_ci NULL DEFAULT NULL,
  `type` varchar(255) CHARACTER SET utf8 COLLATE utf8_general_ci NULL DEFAULT '',
  `invoke_time` bigint(20) NULL DEFAULT NULL,
  `success` int(11) NULL DEFAULT NULL,
  `failure` int(11) NULL DEFAULT NULL,
  `elapsed` int(11) NULL DEFAULT NULL,
  `concurrent` int(11) NULL DEFAULT NULL,
  `max_elapsed` int(11) NULL DEFAULT NULL,
  `max_concurrent` int(11) NULL DEFAULT NULL,
  PRIMARY KEY (`id`) USING BTREE,
  INDEX `index_service`(`service`) USING BTREE,
  INDEX `index_method`(`method`) USING BTREE
) ENGINE = InnoDB CHARACTER SET = utf8 COLLATE = utf8_general_ci ROW_FORMAT = Dynamic;

-- ----------------------------
-- Table structure for h8_api_user
-- ----------------------------
DROP TABLE IF EXISTS `h8_api_user`;
CREATE TABLE `h8_api_user`  (
  `id` int(11) NOT NULL AUTO_INCREMENT,
  `username` varchar(25) CHARACTER SET utf8 COLLATE utf8_general_ci NULL DEFAULT NULL,
  `password` varchar(30) CHARACTER SET utf8 COLLATE utf8_general_ci NULL DEFAULT NULL,
  `agent_name` varchar(25) CHARACTER SET utf8 COLLATE utf8_general_ci NULL DEFAULT NULL,
  `api_info_id` int(5) NULL DEFAULT NULL,
  `site_id` int(5) NULL DEFAULT NULL,
  `site_name` varchar(20) CHARACTER SET utf8 COLLATE utf8_general_ci NULL DEFAULT NULL,
  `oddtype` varchar(10) CHARACTER SET utf8 COLLATE utf8_general_ci NULL DEFAULT NULL,
  `currency_type` char(6) CHARACTER SET utf8 COLLATE utf8_general_ci NULL DEFAULT NULL,
  `create_time` varchar(35) CHARACTER SET utf8 COLLATE utf8_general_ci NULL DEFAULT NULL,
  `user_status` int(2) NULL DEFAULT NULL,
  PRIMARY KEY (`id`) USING BTREE,
  INDEX `index_site_id`(`site_id`) USING BTREE,
  INDEX `index_username`(`username`) USING BTREE
) ENGINE = MyISAM AUTO_INCREMENT = 714107 CHARACTER SET = utf8 COLLATE = utf8_general_ci ROW_FORMAT = Dynamic;

-- ----------------------------
-- Table structure for limit_amount_info
-- ----------------------------
DROP TABLE IF EXISTS `limit_amount_info`;
CREATE TABLE `limit_amount_info`  (
  `id` int(10) NOT NULL AUTO_INCREMENT,
  `site_id` varchar(10) CHARACTER SET utf8 COLLATE utf8_general_ci NOT NULL COMMENT '网站ID',
  `site_desc` varchar(50) CHARACTER SET utf8 COLLATE utf8_general_ci NULL DEFAULT NULL COMMENT '网站中文描述',
  `limit_amount` varchar(15) CHARACTER SET utf8 COLLATE utf8_general_ci NOT NULL COMMENT '限红',
  `status` varchar(4) CHARACTER SET utf8 COLLATE utf8_general_ci NOT NULL COMMENT '状态[100 启用,50 停用]',
  `create_time` timestamp(0) NOT NULL DEFAULT current_timestamp() ON UPDATE CURRENT_TIMESTAMP(0) COMMENT '创建时间',
  `update_time` timestamp(0) NOT NULL COMMENT '修改时间',
  PRIMARY KEY (`id`) USING BTREE
) ENGINE = MyISAM AUTO_INCREMENT = 5 CHARACTER SET = utf8 COLLATE = utf8_general_ci ROW_FORMAT = Dynamic;

-- ----------------------------
-- Table structure for mg_api_user
-- ----------------------------
DROP TABLE IF EXISTS `mg_api_user`;
CREATE TABLE `mg_api_user`  (
  `id` int(11) NOT NULL AUTO_INCREMENT,
  `username` varchar(30) CHARACTER SET utf8 COLLATE utf8_general_ci NULL DEFAULT NULL COMMENT '用户名',
  `password` varchar(50) CHARACTER SET utf8 COLLATE utf8_general_ci NULL DEFAULT NULL COMMENT '密码',
  `api_info_id` int(3) NULL DEFAULT NULL COMMENT '对应 hasdCode id',
  `site_id` int(5) NULL DEFAULT NULL COMMENT '对应网站 id',
  `site_name` varchar(20) CHARACTER SET utf8 COLLATE utf8_general_ci NULL DEFAULT NULL COMMENT '网站名称',
  `agent_name` varchar(20) CHARACTER SET utf8 COLLATE utf8_general_ci NULL DEFAULT NULL COMMENT '代理名称',
  `oddtype` varchar(10) CHARACTER SET utf8 COLLATE utf8_general_ci NULL DEFAULT NULL COMMENT '限红',
  `currency_type` char(5) CHARACTER SET utf8 COLLATE utf8_general_ci NULL DEFAULT NULL COMMENT '币种',
  `create_time` varchar(35) CHARACTER SET utf8 COLLATE utf8_general_ci NULL DEFAULT NULL COMMENT '创建时间',
  `user_status` int(5) NULL DEFAULT NULL COMMENT '会员状态',
  PRIMARY KEY (`id`) USING BTREE,
  INDEX `username`(`username`) USING BTREE
) ENGINE = InnoDB AUTO_INCREMENT = 487360 CHARACTER SET = utf8 COLLATE = utf8_general_ci ROW_FORMAT = Dynamic;

-- ----------------------------
-- Table structure for og_api_user
-- ----------------------------
DROP TABLE IF EXISTS `og_api_user`;
CREATE TABLE `og_api_user`  (
  `id` int(11) NOT NULL AUTO_INCREMENT,
  `username` varchar(30) CHARACTER SET utf8 COLLATE utf8_general_ci NULL DEFAULT NULL COMMENT '用户名',
  `password` varchar(50) CHARACTER SET utf8 COLLATE utf8_general_ci NULL DEFAULT NULL COMMENT '密码',
  `api_info_id` int(3) NULL DEFAULT NULL COMMENT '对应 hasdCode id',
  `site_id` int(5) NULL DEFAULT NULL COMMENT '对应网站 id',
  `site_name` varchar(20) CHARACTER SET utf8 COLLATE utf8_general_ci NULL DEFAULT NULL COMMENT '网站名称',
  `agent_name` varchar(20) CHARACTER SET utf8 COLLATE utf8_general_ci NULL DEFAULT NULL COMMENT '代理名称',
  `oddtype` varchar(10) CHARACTER SET utf8 COLLATE utf8_general_ci NULL DEFAULT NULL COMMENT '限红',
  `currency_type` char(5) CHARACTER SET utf8 COLLATE utf8_general_ci NULL DEFAULT NULL COMMENT '币种',
  `create_time` varchar(35) CHARACTER SET utf8 COLLATE utf8_general_ci NULL DEFAULT NULL COMMENT '创建时间',
  `user_status` int(5) NULL DEFAULT NULL COMMENT '会员状态',
  PRIMARY KEY (`id`) USING BTREE,
  INDEX `username`(`username`) USING BTREE,
  INDEX `index_username`(`username`) USING BTREE
) ENGINE = InnoDB AUTO_INCREMENT = 86056 CHARACTER SET = utf8 COLLATE = utf8_general_ci ROW_FORMAT = Dynamic;

-- ----------------------------
-- Table structure for platform_url
-- ----------------------------
DROP TABLE IF EXISTS `platform_url`;
CREATE TABLE `platform_url`  (
  `id` int(11) NOT NULL,
  `platform_id` int(11) NULL DEFAULT NULL COMMENT '平台id',
  `platform_type` varchar(50) CHARACTER SET utf8 COLLATE utf8_general_ci NULL DEFAULT NULL COMMENT '平台类型',
  `platform_url` varchar(250) CHARACTER SET utf8 COLLATE utf8_general_ci NULL DEFAULT NULL COMMENT '平台url',
  `status` int(11) NULL DEFAULT NULL COMMENT '状态',
  `memo` varchar(500) CHARACTER SET utf8 COLLATE utf8_general_ci NULL DEFAULT NULL COMMENT '备注',
  `create_time` timestamp(0) NOT NULL DEFAULT current_timestamp() ON UPDATE CURRENT_TIMESTAMP(0) COMMENT '创建时间',
  `update_time` timestamp(0) NOT NULL COMMENT '更新时间',
  PRIMARY KEY (`id`) USING BTREE
) ENGINE = InnoDB CHARACTER SET = utf8 COLLATE = utf8_general_ci ROW_FORMAT = Dynamic;

-- ----------------------------
-- Table structure for pt_api_user
-- ----------------------------
DROP TABLE IF EXISTS `pt_api_user`;
CREATE TABLE `pt_api_user`  (
  `id` int(11) NOT NULL AUTO_INCREMENT,
  `username` varchar(50) CHARACTER SET utf8 COLLATE utf8_general_ci NULL DEFAULT NULL,
  `password` varchar(35) CHARACTER SET utf8 COLLATE utf8_general_ci NULL DEFAULT NULL COMMENT '密码',
  `agent_name` varchar(25) CHARACTER SET utf8 COLLATE utf8_general_ci NULL DEFAULT NULL COMMENT '上级代理',
  `api_info_id` int(3) NULL DEFAULT NULL COMMENT '对应hashCode id',
  `site_id` int(5) NULL DEFAULT NULL COMMENT '对应网站 id',
  `site_name` varchar(20) CHARACTER SET utf8 COLLATE utf8_general_ci NULL DEFAULT NULL COMMENT '网站名称',
  `oddtype` varchar(10) CHARACTER SET utf8 COLLATE utf8_general_ci NULL DEFAULT NULL COMMENT '下注范围',
  `currency_type` char(6) CHARACTER SET utf8 COLLATE utf8_general_ci NULL DEFAULT NULL COMMENT '货币类型',
  `create_time` varchar(35) CHARACTER SET utf8 COLLATE utf8_general_ci NULL DEFAULT NULL COMMENT '创建时间',
  `user_status` int(2) NULL DEFAULT NULL COMMENT '会员状态',
  PRIMARY KEY (`id`) USING BTREE,
  UNIQUE INDEX `username_2`(`username`) USING BTREE,
  INDEX `username`(`username`) USING BTREE,
  INDEX `index_username`(`username`) USING BTREE
) ENGINE = InnoDB AUTO_INCREMENT = 114520 CHARACTER SET = utf8 COLLATE = utf8_general_ci ROW_FORMAT = Dynamic;

-- ----------------------------
-- Table structure for transfer_alarm
-- ----------------------------
DROP TABLE IF EXISTS `transfer_alarm`;
CREATE TABLE `transfer_alarm`  (
  `id` int(10) NOT NULL AUTO_INCREMENT,
  `billno` varchar(50) CHARACTER SET utf8 COLLATE utf8_general_ci NULL DEFAULT NULL COMMENT '转账单号',
  `status` int(10) NULL DEFAULT NULL COMMENT '转账是否成功,0=正在转账,1=转账成功 50=转账失败 20=转账异常',
  `version` int(10) NULL DEFAULT 0 COMMENT '报警修改次数',
  `create_time` datetime(0) NULL DEFAULT NULL COMMENT '创建时间',
  `update_time` datetime(0) NULL DEFAULT NULL COMMENT '最后一次更新时间',
  `remark` varchar(255) CHARACTER SET utf8 COLLATE utf8_general_ci NULL DEFAULT NULL COMMENT '备注',
  PRIMARY KEY (`id`) USING BTREE
) ENGINE = MyISAM AUTO_INCREMENT = 3660 CHARACTER SET = utf8 COLLATE = utf8_general_ci ROW_FORMAT = Dynamic;

-- ----------------------------
-- Records of transfer_alarm
-- ----------------------------
INSERT INTO `transfer_alarm` VALUES (3655, 'test004', 20, 0, '2018-08-24 13:23:00', NULL, '转入DS主账户异常或失败:system error!');
INSERT INTO `transfer_alarm` VALUES (3656, 'test004555555', 20, 0, '2018-08-24 13:25:00', NULL, '转入DS主账户异常或失败:system error!');
INSERT INTO `transfer_alarm` VALUES (3657, 'test004555555R', 20, 4761, '2018-08-24 13:26:00', '2018-08-27 20:48:00', '转入DS主账户异常或失败:query transfer is exception : fromKey=ds_money_key&remitno=test004555555RIN&wagerCancel=0&username=test004&siteId=3000&key=hfmmze12119800889eadac6f65b2b2d9f33b1iercoc');
INSERT INTO `transfer_alarm` VALUES (3658, 'test00488877777', 20, 0, '2018-08-24 13:57:00', NULL, '转入DS主账户异常或失败:system error!');
INSERT INTO `transfer_alarm` VALUES (3659, 'test00488877777R', 20, 4729, '2018-08-24 13:58:00', '2018-08-27 20:48:00', '转入DS主账户异常或失败:query transfer is exception : fromKey=ds_money_key&remitno=test00488877777RIN&wagerCancel=0&username=test005&siteId=3000&key=xdoxi9b2113010c67c38693f656efc1dfe688cqixni');

-- ----------------------------
-- Table structure for transfer_money_key
-- ----------------------------
DROP TABLE IF EXISTS `transfer_money_key`;
CREATE TABLE `transfer_money_key`  (
  `id` int(10) NOT NULL AUTO_INCREMENT,
  `type` varchar(20) CHARACTER SET utf8 COLLATE utf8_general_ci NULL DEFAULT NULL COMMENT 'IN 转入, OUT 转出',
  `live_id` int(10) NULL DEFAULT NULL COMMENT 'AG=2  BBIN=11  DS=12 H8=13',
  `from_key_type` int(10) NULL DEFAULT NULL COMMENT '钱包中心交互的fromKeyType',
  PRIMARY KEY (`id`) USING BTREE
) ENGINE = InnoDB AUTO_INCREMENT = 28 CHARACTER SET = utf8 COLLATE = utf8_general_ci ROW_FORMAT = Dynamic;

-- ----------------------------
-- Records of transfer_money_key
-- ----------------------------
INSERT INTO `transfer_money_key` VALUES (1, 'OUT', 2, 20002);
INSERT INTO `transfer_money_key` VALUES (2, 'IN', 2, 20005);
INSERT INTO `transfer_money_key` VALUES (3, 'OUT', 11, 20001);
INSERT INTO `transfer_money_key` VALUES (4, 'IN', 11, 20004);
INSERT INTO `transfer_money_key` VALUES (5, 'OUT', 13, 20003);
INSERT INTO `transfer_money_key` VALUES (6, 'IN', 13, 20006);
INSERT INTO `transfer_money_key` VALUES (7, 'OUT', 14, 20010);
INSERT INTO `transfer_money_key` VALUES (8, 'IN', 14, 20011);
INSERT INTO `transfer_money_key` VALUES (9, 'OUT', 12, 60000);
INSERT INTO `transfer_money_key` VALUES (10, 'IN', 12, 60001);
INSERT INTO `transfer_money_key` VALUES (12, 'IN', 15, 20013);
INSERT INTO `transfer_money_key` VALUES (14, 'OUT', 15, 20014);
INSERT INTO `transfer_money_key` VALUES (16, 'OUT', 21, 60000);
INSERT INTO `transfer_money_key` VALUES (18, 'IN', 21, 60001);
INSERT INTO `transfer_money_key` VALUES (20, 'IN', 3, 20011);
INSERT INTO `transfer_money_key` VALUES (22, 'OUT', 3, 20010);
INSERT INTO `transfer_money_key` VALUES (24, 'IN', 16, 20015);
INSERT INTO `transfer_money_key` VALUES (26, 'OUT', 16, 20016);

-- ----------------------------
-- Table structure for transfer_record
-- ----------------------------
DROP TABLE IF EXISTS `transfer_record`;
CREATE TABLE `transfer_record`  (
  `id` int(11) NOT NULL AUTO_INCREMENT,
  `username` varchar(30) CHARACTER SET utf8 COLLATE utf8_general_ci NULL DEFAULT NULL COMMENT '用户名',
  `password` varchar(35) CHARACTER SET utf8 COLLATE utf8_general_ci NULL DEFAULT NULL COMMENT '密码',
  `transfer_money` varchar(15) CHARACTER SET utf8 COLLATE utf8_general_ci NULL DEFAULT NULL COMMENT '转账金额',
  `trans_billno` varchar(50) CHARACTER SET utf8 COLLATE utf8_general_ci NULL DEFAULT NULL COMMENT '转账编号',
  `trans_status` int(5) NULL DEFAULT NULL COMMENT '转账是否成功,0=正在转账,1=转账成功 50=转账失败 20=转账异常 10=客服处理',
  `trans_type` varchar(10) CHARACTER SET utf8 COLLATE utf8_general_ci NULL DEFAULT NULL COMMENT '转账类型 IN 转入 OUT 转出',
  `trans_before_money` double(18, 2) NULL DEFAULT NULL COMMENT '转账前金额',
  `trans_after_money` double(18, 2) NULL DEFAULT NULL COMMENT '转账后金额',
  `live_id` int(10) NULL DEFAULT NULL COMMENT '视讯id',
  `live_type` varchar(10) CHARACTER SET utf8 COLLATE utf8_general_ci NULL DEFAULT NULL COMMENT 'bbin  ag  h8 og center',
  `trans_record_id` bigint(11) NULL DEFAULT NULL,
  `finger` varchar(50) CHARACTER SET utf8 COLLATE utf8_general_ci NULL DEFAULT NULL COMMENT '指纹',
  `remark` varchar(100) CHARACTER SET utf8 COLLATE utf8_general_ci NULL DEFAULT NULL COMMENT '转账备注',
  `create_time` varchar(35) CHARACTER SET utf8 COLLATE utf8_general_ci NULL DEFAULT NULL COMMENT '转账时间',
  `update_time` varchar(35) CHARACTER SET utf8 COLLATE utf8_general_ci NULL DEFAULT NULL COMMENT '修改时间',
  `versions` int(10) NULL DEFAULT 0 COMMENT '请求的次数',
  `site_id` int(10) NULL DEFAULT NULL COMMENT '网站编号',
  PRIMARY KEY (`id`) USING BTREE,
  INDEX `index_status`(`trans_status`) USING BTREE
) ENGINE = InnoDB AUTO_INCREMENT = 22327151 CHARACTER SET = utf8 COLLATE = utf8_general_ci ROW_FORMAT = Dynamic;

-- ----------------------------
-- Records of transfer_record
-- ----------------------------
INSERT INTO `transfer_record` VALUES (22327137, 'test004', '0cc538f369', '50', 'test004', 50, 'OUT', NULL, NULL, 2, 'center', 13897543, 'YEBnynxzARqi7mUKnA3/uA==', 'DS主账户转账失败', '2018-08-24 13:23:33', '2018-08-24 13:23:33', NULL, 3000);
INSERT INTO `transfer_record` VALUES (22327138, 'test004', '0cc538f369', '50', 'test004555555', 1, 'OUT', NULL, NULL, 2, 'center', 13897544, 'WwNskyz5iDSqaB5RRSQf/w==', 'DS主账户转账成功', '2018-08-24 13:24:29', '2018-08-24 13:24:29', NULL, 3000);
INSERT INTO `transfer_record` VALUES (22327139, 'test004', '0cc538f369', '50', 'test004555555', 50, 'IN', NULL, NULL, 2, 'ag', 13897544, 'BmYNjKWZEQiU0BdtE0/XOw==', 'AG转账异常,重新转入DS主账户失败,需客服手动处理!', '2018-08-24 13:24:29', '2018-08-24 13:25:00', NULL, 3000);
INSERT INTO `transfer_record` VALUES (22327140, 'test004', 'admin123', '50', 'test004555555R', 20, 'IN', NULL, NULL, 2, 'center', 13897544, 'sVtKrYLxWI/nxcDHab6KLQ==', 'DS主账户转账异常需人工处理', '2018-08-24 13:25:00', '2018-08-24 13:25:00', NULL, 3000);
INSERT INTO `transfer_record` VALUES (22327141, 'test005', '0cc538f369', '50', 'test0048888888888', 50, 'OUT', NULL, NULL, 2, 'center', 13897545, 'AJwfW57kgiUHBMwdbQC63w==', 'DS主账户余额不足', '2018-08-24 13:55:12', '2018-08-24 13:55:12', NULL, 3000);
INSERT INTO `transfer_record` VALUES (22327142, 'test005', '0cc538f369', '50', 'test00488877777', 1, 'OUT', NULL, NULL, 2, 'center', 13897546, '1WnOghVpldNvxJUWB6WyZQ==', 'DS主账户转账成功', '2018-08-24 13:56:20', '2018-08-24 13:56:20', NULL, 3000);
INSERT INTO `transfer_record` VALUES (22327143, 'test005', '0cc538f369', '50', 'test00488877777', 50, 'IN', NULL, NULL, 2, 'ag', 13897546, 'qhAUPz/xjBkIOFMTLhahaA==', 'AG转账异常,重新转入DS主账户失败,需客服手动处理!', '2018-08-24 13:56:20', '2018-08-24 13:57:00', NULL, 3000);
INSERT INTO `transfer_record` VALUES (22327144, 'test005', 'admin123', '50', 'test00488877777R', 20, 'IN', NULL, NULL, 2, 'center', 13897546, 'b+QzOg/GF0S6vwwuPCHn8Q==', 'DS主账户转账异常需人工处理', '2018-08-24 13:57:00', '2018-08-24 13:57:00', NULL, 3000);
INSERT INTO `transfer_record` VALUES (22327145, 'test006', '0cc538f369', '20', 'test00688877777', 1, 'OUT', NULL, NULL, 2, 'center', 13897547, 'H+Udcv5mEzgWkVCSqB7QYg==', 'DS主账户转账成功', '2018-08-24 14:18:10', '2018-08-24 14:18:10', NULL, 3000);
INSERT INTO `transfer_record` VALUES (22327146, 'test006', '0cc538f369', '20', 'test00688877777', 1, 'IN', NULL, NULL, 2, 'ag', 13897547, 'H+Udcv5mEzgWkVCSqB7QYg==', 'AG转账成功', '2018-08-24 14:18:10', '2018-08-24 14:18:14', NULL, 3000);
INSERT INTO `transfer_record` VALUES (22327147, 'test006', '0cc538f369', '20', 'test00688877777', 50, 'OUT', NULL, NULL, 2, 'center', 13897548, '56qAhj8STwzdr3UfhwHBfA==', 'DS主账户转账失败', '2018-09-06 12:13:29', '2018-09-06 12:13:29', NULL, 3000);
INSERT INTO `transfer_record` VALUES (22327148, 'test006', '0cc538f369', '20', 'test00688877777', 50, 'OUT', NULL, NULL, 2, 'center', 13897549, '56qAhj8STwzdr3UfhwHBfA==', 'DS主账户转账失败', '2018-09-06 12:13:43', '2018-09-06 12:13:43', NULL, 3000);
INSERT INTO `transfer_record` VALUES (22327149, 'test006', '0cc538f369', '20', 'test00688877777', 50, 'OUT', NULL, NULL, 2, 'center', 13897550, '56qAhj8STwzdr3UfhwHBfA==', 'DS主账户转账失败', '2018-09-06 15:06:09', '2018-09-06 15:06:09', NULL, 3000);
INSERT INTO `transfer_record` VALUES (22327150, 'test006', '0cc538f369', '20', 'test00688877777', 50, 'OUT', NULL, NULL, 2, 'center', 13897551, '56qAhj8STwzdr3UfhwHBfA==', 'DS主账户转账失败', '2018-09-06 15:07:21', '2018-09-06 15:07:21', NULL, 3000);

-- ----------------------------
-- Table structure for transfer_record_detail
-- ----------------------------
DROP TABLE IF EXISTS `transfer_record_detail`;
CREATE TABLE `transfer_record_detail`  (
  `id` bigint(11) NOT NULL AUTO_INCREMENT,
  `username` varchar(50) CHARACTER SET utf8 COLLATE utf8_general_ci NULL DEFAULT NULL COMMENT '用户名',
  `operator` varchar(50) CHARACTER SET utf8 COLLATE utf8_general_ci NULL DEFAULT NULL COMMENT '转账操作者',
  `password` varchar(35) CHARACTER SET utf8 COLLATE utf8_general_ci NULL DEFAULT NULL COMMENT '密码',
  `credit` varchar(15) CHARACTER SET utf8 COLLATE utf8_general_ci NULL DEFAULT NULL COMMENT '转账金额',
  `billno` varchar(50) CHARACTER SET utf8 COLLATE utf8_general_ci NULL DEFAULT NULL COMMENT '转账订单号;bbin只支持数字',
  `status` int(5) NULL DEFAULT 0 COMMENT '转账是否成功,0=正在转账,1=转账成功 50=转账失败 20=转账异常 10=客服处理',
  `type` varchar(10) CHARACTER SET utf8 COLLATE utf8_general_ci NULL DEFAULT NULL COMMENT '转账类型 IN 转入 OUT 转出',
  `transfer_before_money` varchar(15) CHARACTER SET utf8 COLLATE utf8_general_ci NULL DEFAULT NULL,
  `transfer_after_money` varchar(15) CHARACTER SET utf8 COLLATE utf8_general_ci NULL DEFAULT NULL,
  `site_id` int(10) NULL DEFAULT NULL,
  `live_id` varchar(15) CHARACTER SET utf8 COLLATE utf8_general_ci NULL DEFAULT NULL,
  `version` int(5) NULL DEFAULT 0 COMMENT '请求的次数',
  `remark` varchar(255) CHARACTER SET utf8 COLLATE utf8_general_ci NULL DEFAULT NULL,
  `create_time` varchar(35) CHARACTER SET utf8 COLLATE utf8_general_ci NULL DEFAULT NULL,
  `update_time` varchar(35) CHARACTER SET utf8 COLLATE utf8_general_ci NULL DEFAULT NULL,
  PRIMARY KEY (`id`) USING BTREE
) ENGINE = InnoDB AUTO_INCREMENT = 13897552 CHARACTER SET = utf8 COLLATE = utf8_general_ci ROW_FORMAT = Dynamic;

-- ----------------------------
-- Records of transfer_record_detail
-- ----------------------------
INSERT INTO `transfer_record_detail` VALUES (13897543, 'test004', NULL, NULL, '50', 'test004', 20, 'IN', NULL, NULL, 3000, '2', 2, 'ag', '2018-08-24 13:23:33', '2018-08-24 13:23:33');
INSERT INTO `transfer_record_detail` VALUES (13897544, 'test004', NULL, NULL, '50', 'test004555555', 50, 'IN', NULL, NULL, 3000, '2', 2, 'ag,重新转入DS主账户失败,需客服手动处理!', '2018-08-24 13:24:29', '2018-08-24 13:25:00');
INSERT INTO `transfer_record_detail` VALUES (13897545, 'test005', NULL, NULL, '50', 'test0048888888888', 20, 'IN', NULL, NULL, 3000, '2', 2, 'ag', '2018-08-24 13:55:12', '2018-08-24 13:55:12');
INSERT INTO `transfer_record_detail` VALUES (13897546, 'test005', NULL, NULL, '50', 'test00488877777', 50, 'IN', NULL, NULL, 3000, '2', 2, 'ag,重新转入DS主账户失败,需客服手动处理!', '2018-08-24 13:56:20', '2018-08-24 13:57:00');
INSERT INTO `transfer_record_detail` VALUES (13897547, 'test006', NULL, NULL, '20', 'test00688877777', 1, 'IN', NULL, NULL, 3000, '2', 2, 'ag', '2018-08-24 14:18:10', '2018-08-24 14:18:14');
INSERT INTO `transfer_record_detail` VALUES (13897548, 'test006', NULL, NULL, '20', 'test00688877777', 20, 'IN', NULL, NULL, 3000, '2', 2, 'ag', '2018-09-06 12:13:29', '2018-09-06 12:13:29');
INSERT INTO `transfer_record_detail` VALUES (13897549, 'test006', NULL, NULL, '20', 'test00688877777', 20, 'IN', NULL, NULL, 3000, '2', 2, 'ag', '2018-09-06 12:13:43', '2018-09-06 12:13:43');
INSERT INTO `transfer_record_detail` VALUES (13897550, 'test006', NULL, NULL, '20', 'test00688877777', 20, 'IN', NULL, NULL, 3000, '2', 2, 'ag', '2018-09-06 15:06:09', '2018-09-06 15:06:09');
INSERT INTO `transfer_record_detail` VALUES (13897551, 'test006', NULL, NULL, '20', 'test00688877777', 20, 'IN', NULL, NULL, 3000, '2', 2, 'ag', '2018-09-06 15:07:21', '2018-09-06 15:07:21');

SET FOREIGN_KEY_CHECKS = 1;
