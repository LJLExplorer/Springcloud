/*
 Navicat Premium Data Transfer

 Source Server         : mysqltest
 Source Server Type    : MySQL
 Source Server Version : 80022
 Source Host           : localhost:3306
 Source Schema         : springcloud

 Target Server Type    : MySQL
 Target Server Version : 80022
 File Encoding         : 65001

 Date: 25/06/2021 18:35:22
*/

SET NAMES utf8mb4;
SET FOREIGN_KEY_CHECKS = 0;

-- ----------------------------
-- Table structure for tb_user
-- ----------------------------
DROP TABLE IF EXISTS `tb_user`;
CREATE TABLE `tb_user`  (
  `id` bigint(0) NOT NULL AUTO_INCREMENT,
  `user_name` varchar(50) CHARACTER SET utf8 COLLATE utf8_general_ci NULL DEFAULT NULL,
  `password` varchar(50) CHARACTER SET utf8 COLLATE utf8_general_ci NULL DEFAULT NULL,
  `name` varchar(50) CHARACTER SET utf8 COLLATE utf8_general_ci NULL DEFAULT NULL,
  `age` int(0) NULL DEFAULT NULL,
  `sex` int(0) NULL DEFAULT NULL,
  `birthday` date NULL DEFAULT NULL,
  `created` date NULL DEFAULT NULL,
  `updated` date NULL DEFAULT NULL,
  `note` varchar(2000) CHARACTER SET utf8 COLLATE utf8_general_ci NULL DEFAULT NULL,
  PRIMARY KEY (`id`) USING BTREE
) ENGINE = InnoDB AUTO_INCREMENT = 7 CHARACTER SET = utf8 COLLATE = utf8_general_ci ROW_FORMAT = Dynamic;

-- ----------------------------
-- Records of tb_user
-- ----------------------------
INSERT INTO `tb_user` VALUES (1, 'zhangsan', '1', '张三a', 18, 1, '2019-02-27', '2019-02-27', '2019-02-27', '在学习Java...');
INSERT INTO `tb_user` VALUES (2, 'lisi', '1', '李四ab', 18, 1, '2019-02-27', '2019-02-27', '2019-02-27', '在学习Java...');
INSERT INTO `tb_user` VALUES (3, 'wangwu', '1', '王五abc', 18, 1, '2019-02-27', '2019-02-27', '2019-02-27', '在学习Java...');
INSERT INTO `tb_user` VALUES (4, 'fanbingbing', '1', '范冰冰aa', 18, 2, '2019-02-27', '2019-02-27', '2019-02-27', '在学习Java...');
INSERT INTO `tb_user` VALUES (5, 'guodegang', '1', '郭德纲bb', 18, 1, '2019-02-27', '2019-02-27', '2019-02-27', '在学习Java...');
INSERT INTO `tb_user` VALUES (6, NULL, NULL, '周星驰cc', 18, NULL, '2020-06-01', '2020-06-01', NULL, NULL);
INSERT INTO `tb_user` VALUES (7, 'yemage', NULL, '野马哥', NULL, NULL, '2021-06-01', '2021-06-02', '2021-06-02', '在学习java');

SET FOREIGN_KEY_CHECKS = 1;
