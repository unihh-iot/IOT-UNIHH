/*
 Navicat Premium Data Transfer

 Source Server         : Local_DB
 Source Server Type    : MySQL
 Source Server Version : 80012
 Source Host           : localhost:3306
 Source Schema         : unihh_iot

 Target Server Type    : MySQL
 Target Server Version : 80012
 File Encoding         : 65001

 Date: 09/10/2026 10:02:06
*/

SET NAMES utf8mb4;
SET FOREIGN_KEY_CHECKS = 0;

-- ----------------------------
-- Table structure for iot_alarm
-- ----------------------------
DROP TABLE IF EXISTS `iot_alarm`;
CREATE TABLE `iot_alarm`  (
  `id` bigint(20) NOT NULL AUTO_INCREMENT COMMENT '自增主键ID',
  `alarm_id` varchar(64) CHARACTER SET utf8mb4 COLLATE utf8mb4_0900_ai_ci NOT NULL COMMENT '告警事件唯一ID',
  `rule_id` varchar(64) CHARACTER SET utf8mb4 COLLATE utf8mb4_0900_ai_ci NULL DEFAULT NULL COMMENT '关联规则ID',
  `device_id` varchar(64) CHARACTER SET utf8mb4 COLLATE utf8mb4_0900_ai_ci NOT NULL COMMENT '告警源设备ID',
  `device_name` varchar(128) CHARACTER SET utf8mb4 COLLATE utf8mb4_0900_ai_ci NULL DEFAULT NULL COMMENT '设备名称',
  `model_key` varchar(64) CHARACTER SET utf8mb4 COLLATE utf8mb4_0900_ai_ci NULL DEFAULT NULL COMMENT '关联物模型Key',
  `metric` varchar(64) CHARACTER SET utf8mb4 COLLATE utf8mb4_0900_ai_ci NULL DEFAULT NULL COMMENT '告警触发点位',
  `alarm_level` varchar(16) CHARACTER SET utf8mb4 COLLATE utf8mb4_0900_ai_ci NOT NULL DEFAULT 'WARN' COMMENT '告警级别 (INFO, WARN, HIGH, CRITICAL)',
  `alarm_title` varchar(256) CHARACTER SET utf8mb4 COLLATE utf8mb4_0900_ai_ci NOT NULL COMMENT '告警标题',
  `trigger_value` varchar(128) CHARACTER SET utf8mb4 COLLATE utf8mb4_0900_ai_ci NULL DEFAULT NULL COMMENT '触发时实际测得数值',
  `threshold_desc` varchar(128) CHARACTER SET utf8mb4 COLLATE utf8mb4_0900_ai_ci NULL DEFAULT NULL COMMENT '告警判定阈值描述',
  `status` tinyint(4) NOT NULL DEFAULT 0 COMMENT '告警处理状态 (0:未处理, 1:已确认, 2:已恢复结单)',
  `alarm_time` datetime(0) NOT NULL DEFAULT CURRENT_TIMESTAMP(0) COMMENT '告警发生时间',
  `handle_time` datetime(0) NULL DEFAULT NULL COMMENT '最后处理时间',
  `handle_user` varchar(64) CHARACTER SET utf8mb4 COLLATE utf8mb4_0900_ai_ci NULL DEFAULT NULL COMMENT '处理人账号',
  `remark` varchar(512) CHARACTER SET utf8mb4 COLLATE utf8mb4_0900_ai_ci NULL DEFAULT NULL COMMENT '处理备注记录',
  PRIMARY KEY (`id`) USING BTREE,
  UNIQUE INDEX `alarm_id`(`alarm_id`) USING BTREE
) ENGINE = InnoDB AUTO_INCREMENT = 53 CHARACTER SET = utf8mb4 COLLATE = utf8mb4_0900_ai_ci COMMENT = '告警事件明细表' ROW_FORMAT = Dynamic;

-- ----------------------------
-- Records of iot_alarm
-- ----------------------------
INSERT INTO `iot_alarm` VALUES (1, 'ALM_20260909_001', 'RULE_101', 'DEV_001', '1号高压车间电动机传感器', 'pk_demo', 'temperature', 'CRITICAL', '1号电机主轴承温度过高', '82.4 ℃', '> 75.0 ℃', 0, '2026-09-09 20:04:04', NULL, NULL, NULL);
INSERT INTO `iot_alarm` VALUES (2, 'ALM_20260909_002', 'RULE_102', 'DEV_002', '2号储罐压力PLC', 'pk_plc_s7', 'pressure', 'HIGH', '2号储罐压力超界报警', '105.2 kPa', 'Out of 10~100 kPa', 1, '2026-09-09 19:04:04', '2026-09-09 20:04:04', 'admin', '现场值班员已确认，降压阀正常排气');
INSERT INTO `iot_alarm` VALUES (3, 'ALM_20260909_003', 'RULE_103', 'DEV_001', '1号高压车间电动机传感器', 'pk_demo', 'humidity', 'WARN', '1号车间环境湿度偏高', '87.1 %RH', '> 85.0 %RH', 2, '2026-09-09 18:04:04', '2026-09-09 19:04:04', 'system', '湿度自然回落至安全区间，系统自动关单');
INSERT INTO `iot_alarm` VALUES (4, 'ALM_1789047588799_C48EEA', 'RULE_484487', 'DEV_OGY82W69', NULL, NULL, 'P4', 'CRITICAL', '告警', '200.0', '== 200.0', 2, '2026-09-10 21:39:49', '2026-09-10 21:40:27', 'admin', '值班人员手动确认告警，现场排查正常');
INSERT INTO `iot_alarm` VALUES (5, 'ALM_1789047788797_4DD9E6', 'RULE_484487', 'DEV_OGY82W69', NULL, NULL, 'P4', 'CRITICAL', '告警', '200.0', '== 200.0', 0, '2026-09-10 21:43:09', NULL, NULL, NULL);
INSERT INTO `iot_alarm` VALUES (6, 'ALM_1789047968845_8AE382', 'RULE_484487', 'DEV_OGY82W69', NULL, NULL, 'P4', 'CRITICAL', '告警', '200.0', '== 200.0', 0, '2026-09-10 21:46:09', NULL, NULL, NULL);
INSERT INTO `iot_alarm` VALUES (7, 'ALM_1789129402702_404F4B', 'RULE_484487', 'DEV_OGY82W69', NULL, NULL, 'P4', 'CRITICAL', '告警', '200.0', '== 200.0', 0, '2026-09-11 20:23:23', NULL, NULL, NULL);
INSERT INTO `iot_alarm` VALUES (8, 'ALM_1789129615285_F0343A', 'RULE_484487', 'DEV_OGY82W69', NULL, NULL, 'P4', 'CRITICAL', '告警', '200.0', '== 200.0', 0, '2026-09-11 20:26:55', NULL, NULL, NULL);
INSERT INTO `iot_alarm` VALUES (9, 'ALM_1789129831522_B07B8F', 'RULE_484487', 'DEV_OGY82W69', NULL, NULL, 'P4', 'CRITICAL', '告警', '200.0', '== 200.0', 0, '2026-09-11 20:30:32', NULL, NULL, NULL);
INSERT INTO `iot_alarm` VALUES (10, 'ALM_1789130048399_767B89', 'RULE_484487', 'DEV_OGY82W69', NULL, NULL, 'P4', 'CRITICAL', '告警', '200.0', '== 200.0', 0, '2026-09-11 20:34:08', NULL, NULL, NULL);
INSERT INTO `iot_alarm` VALUES (11, 'ALM_1789130262473_58F25E', 'RULE_484487', 'DEV_OGY82W69', NULL, NULL, 'P4', 'CRITICAL', '告警', '200.0', '== 200.0', 0, '2026-09-11 20:37:42', NULL, NULL, NULL);
INSERT INTO `iot_alarm` VALUES (12, 'ALM_1789130477227_A9A871', 'RULE_484487', 'DEV_OGY82W69', NULL, NULL, 'P4', 'CRITICAL', '告警', '200.0', '== 200.0', 0, '2026-09-11 20:41:17', NULL, NULL, NULL);
INSERT INTO `iot_alarm` VALUES (13, 'ALM_1789130693583_9C1E48', 'RULE_484487', 'DEV_OGY82W69', NULL, NULL, 'P4', 'CRITICAL', '告警', '200.0', '== 200.0', 0, '2026-09-11 20:44:54', NULL, NULL, NULL);
INSERT INTO `iot_alarm` VALUES (14, 'ALM_1789130911195_40B577', 'RULE_484487', 'DEV_OGY82W69', NULL, NULL, 'P4', 'CRITICAL', '告警', '200.0', '== 200.0', 0, '2026-09-11 20:48:31', NULL, NULL, NULL);
INSERT INTO `iot_alarm` VALUES (15, 'ALM_1789131130110_C4B1F6', 'RULE_484487', 'DEV_OGY82W69', NULL, NULL, 'P4', 'CRITICAL', '告警', '200.0', '== 200.0', 0, '2026-09-11 20:52:10', NULL, NULL, NULL);
INSERT INTO `iot_alarm` VALUES (16, 'ALM_1789131349581_113A96', 'RULE_484487', 'DEV_OGY82W69', NULL, NULL, 'P4', 'CRITICAL', '告警', '200.0', '== 200.0', 0, '2026-09-11 20:55:50', NULL, NULL, NULL);
INSERT INTO `iot_alarm` VALUES (17, 'ALM_1789131570212_477230', 'RULE_484487', 'DEV_OGY82W69', NULL, NULL, 'P4', 'CRITICAL', '告警', '200.0', '== 200.0', 0, '2026-09-11 20:59:30', NULL, NULL, NULL);
INSERT INTO `iot_alarm` VALUES (18, 'ALM_1789131789523_169C40', 'RULE_484487', 'DEV_OGY82W69', NULL, NULL, 'P4', 'CRITICAL', '告警', '200.0', '== 200.0', 0, '2026-09-11 21:03:10', NULL, NULL, NULL);
INSERT INTO `iot_alarm` VALUES (19, 'ALM_1789132010064_43D2AE', 'RULE_484487', 'DEV_OGY82W69', NULL, NULL, 'P4', 'CRITICAL', '告警', '200.0', '== 200.0', 0, '2026-09-11 21:06:50', NULL, NULL, NULL);
INSERT INTO `iot_alarm` VALUES (20, 'ALM_1789132234481_0D4685', 'RULE_484487', 'DEV_OGY82W69', NULL, NULL, 'P4', 'CRITICAL', '告警', '200.0', '== 200.0', 0, '2026-09-11 21:10:34', NULL, NULL, NULL);
INSERT INTO `iot_alarm` VALUES (21, 'ALM_1789132456317_2F4004', 'RULE_484487', 'DEV_OGY82W69', NULL, NULL, 'P4', 'CRITICAL', '告警', '200.0', '== 200.0', 0, '2026-09-11 21:14:16', NULL, NULL, NULL);
INSERT INTO `iot_alarm` VALUES (22, 'ALM_1789132680086_C0C0A0', 'RULE_484487', 'DEV_OGY82W69', NULL, NULL, 'P4', 'CRITICAL', '告警', '200.0', '== 200.0', 0, '2026-09-11 21:18:00', NULL, NULL, NULL);
INSERT INTO `iot_alarm` VALUES (23, 'ALM_1789132904051_455FCA', 'RULE_484487', 'DEV_OGY82W69', NULL, NULL, 'P4', 'CRITICAL', '告警', '200.0', '== 200.0', 0, '2026-09-11 21:21:44', NULL, NULL, NULL);
INSERT INTO `iot_alarm` VALUES (24, 'ALM_1789133125752_E81697', 'RULE_484487', 'DEV_OGY82W69', NULL, NULL, 'P4', 'CRITICAL', '告警', '200.0', '== 200.0', 0, '2026-09-11 21:25:26', NULL, NULL, NULL);
INSERT INTO `iot_alarm` VALUES (25, 'ALM_1789133349828_047C0A', 'RULE_484487', 'DEV_OGY82W69', NULL, NULL, 'P4', 'CRITICAL', '告警', '200.0', '== 200.0', 0, '2026-09-11 21:29:10', NULL, NULL, NULL);
INSERT INTO `iot_alarm` VALUES (26, 'ALM_1789133575364_800AE6', 'RULE_484487', 'DEV_OGY82W69', NULL, NULL, 'P4', 'CRITICAL', '告警', '200.0', '== 200.0', 0, '2026-09-11 21:32:55', NULL, NULL, NULL);
INSERT INTO `iot_alarm` VALUES (27, 'ALM_1789133799489_8669FA', 'RULE_484487', 'DEV_OGY82W69', NULL, NULL, 'P4', 'CRITICAL', '告警', '200.0', '== 200.0', 0, '2026-09-11 21:36:39', NULL, NULL, NULL);
INSERT INTO `iot_alarm` VALUES (28, 'ALM_1789134022502_B93CBC', 'RULE_484487', 'DEV_OGY82W69', NULL, NULL, 'P4', 'CRITICAL', '告警', '200.0', '== 200.0', 0, '2026-09-11 21:40:23', NULL, NULL, NULL);
INSERT INTO `iot_alarm` VALUES (29, 'ALM_1789134248098_57C668', 'RULE_484487', 'DEV_OGY82W69', NULL, NULL, 'P4', 'CRITICAL', '告警', '200.0', '== 200.0', 0, '2026-09-11 21:44:08', NULL, NULL, NULL);
INSERT INTO `iot_alarm` VALUES (30, 'ALM_1789134476407_B7394D', 'RULE_484487', 'DEV_OGY82W69', NULL, NULL, 'P4', 'CRITICAL', '告警', '200.0', '== 200.0', 0, '2026-09-11 21:47:56', NULL, NULL, NULL);
INSERT INTO `iot_alarm` VALUES (31, 'ALM_1789134699076_125090', 'RULE_484487', 'DEV_OGY82W69', NULL, NULL, 'P4', 'CRITICAL', '告警', '200.0', '== 200.0', 0, '2026-09-11 21:51:39', NULL, NULL, NULL);
INSERT INTO `iot_alarm` VALUES (32, 'ALM_1789135503744_A08726', 'RULE_484487', 'DEV_OGY82W69', NULL, NULL, 'P4', 'CRITICAL', '告警', '200.0', '== 200.0', 0, '2026-09-11 22:05:04', NULL, NULL, NULL);
INSERT INTO `iot_alarm` VALUES (33, 'ALM_1789390118512_69CAF4', 'RULE_484487', 'DEV_OGY82W69', NULL, NULL, 'P4', 'CRITICAL', '告警', '331.0', '>= 200.0', 0, '2026-09-14 20:48:39', NULL, NULL, NULL);
INSERT INTO `iot_alarm` VALUES (34, 'ALM_1789390334315_337039', 'RULE_484487', 'DEV_OGY82W69', NULL, NULL, 'P4', 'CRITICAL', '告警', '200.0', '>= 200.0', 0, '2026-09-14 20:52:14', NULL, NULL, NULL);
INSERT INTO `iot_alarm` VALUES (35, 'ALM_1789390551053_7B7F4D', 'RULE_484487', 'DEV_OGY82W69', NULL, NULL, 'P4', 'CRITICAL', '告警', '201.0', '>= 200.0', 0, '2026-09-14 20:55:51', NULL, NULL, NULL);
INSERT INTO `iot_alarm` VALUES (36, 'ALM_1789390771073_F1CC64', 'RULE_484487', 'DEV_OGY82W69', NULL, NULL, 'P4', 'CRITICAL', '告警', '200.0', '>= 200.0', 0, '2026-09-14 20:59:31', NULL, NULL, NULL);
INSERT INTO `iot_alarm` VALUES (37, 'ALM_1789390988293_7B91B0', 'RULE_484487', 'DEV_OGY82W69', NULL, NULL, 'P4', 'CRITICAL', '告警', '200.0', '>= 200.0', 0, '2026-09-14 21:03:08', NULL, NULL, NULL);
INSERT INTO `iot_alarm` VALUES (38, 'ALM_1789391209179_FC6347', 'RULE_484487', 'DEV_OGY82W69', NULL, NULL, 'P4', 'CRITICAL', '告警', '200.0', '>= 200.0', 0, '2026-09-14 21:06:49', NULL, NULL, NULL);
INSERT INTO `iot_alarm` VALUES (39, 'ALM_1789391429625_D90BAA', 'RULE_484487', 'DEV_OGY82W69', NULL, NULL, 'P4', 'CRITICAL', '告警', '200.0', '>= 200.0', 0, '2026-09-14 21:10:30', NULL, NULL, NULL);
INSERT INTO `iot_alarm` VALUES (40, 'ALM_1789391652645_EA7A53', 'RULE_484487', 'DEV_OGY82W69', NULL, NULL, 'P4', 'CRITICAL', '告警', '200.0', '>= 200.0', 0, '2026-09-14 21:14:13', NULL, NULL, NULL);
INSERT INTO `iot_alarm` VALUES (41, 'ALM_1789391872641_A2C476', 'RULE_484487', 'DEV_OGY82W69', NULL, NULL, 'P4', 'CRITICAL', '告警', '200.0', '>= 200.0', 0, '2026-09-14 21:17:53', NULL, NULL, NULL);
INSERT INTO `iot_alarm` VALUES (42, 'ALM_1789392093409_DCF0AA', 'RULE_484487', 'DEV_OGY82W69', NULL, NULL, 'P4', 'CRITICAL', '告警', '200.0', '>= 200.0', 0, '2026-09-14 21:21:33', NULL, NULL, NULL);
INSERT INTO `iot_alarm` VALUES (43, 'ALM_1789392315897_B42963', 'RULE_484487', 'DEV_OGY82W69', NULL, NULL, 'P4', 'CRITICAL', '告警', '200.0', '>= 200.0', 0, '2026-09-14 21:25:16', NULL, NULL, NULL);
INSERT INTO `iot_alarm` VALUES (44, 'ALM_1789392537911_D0C2A6', 'RULE_484487', 'DEV_OGY82W69', NULL, NULL, 'P4', 'CRITICAL', '告警', '200.0', '>= 200.0', 0, '2026-09-14 21:28:58', NULL, NULL, NULL);
INSERT INTO `iot_alarm` VALUES (45, 'ALM_1789392758627_17816C', 'RULE_484487', 'DEV_OGY82W69', NULL, NULL, 'P4', 'CRITICAL', '告警', '200.0', '>= 200.0', 0, '2026-09-14 21:32:39', NULL, NULL, NULL);
INSERT INTO `iot_alarm` VALUES (46, 'ALM_1789392979103_573E7B', 'RULE_484487', 'DEV_OGY82W69', NULL, NULL, 'P4', 'CRITICAL', '告警', '200.0', '>= 200.0', 0, '2026-09-14 21:36:19', NULL, NULL, NULL);
INSERT INTO `iot_alarm` VALUES (47, 'ALM_1789393198836_C7FF98', 'RULE_484487', 'DEV_OGY82W69', NULL, NULL, 'P4', 'CRITICAL', '告警', '200.0', '>= 200.0', 0, '2026-09-14 21:39:59', NULL, NULL, NULL);
INSERT INTO `iot_alarm` VALUES (48, 'ALM_1789393418587_057958', 'RULE_484487', 'DEV_OGY82W69', NULL, NULL, 'P4', 'CRITICAL', '告警', '200.0', '>= 200.0', 0, '2026-09-14 21:43:39', NULL, NULL, NULL);
INSERT INTO `iot_alarm` VALUES (49, 'ALM_1789393639175_B865D9', 'RULE_484487', 'DEV_OGY82W69', NULL, NULL, 'P4', 'CRITICAL', '告警', '200.0', '>= 200.0', 0, '2026-09-14 21:47:19', NULL, NULL, NULL);
INSERT INTO `iot_alarm` VALUES (50, 'ALM_1789393859207_E931BD', 'RULE_484487', 'DEV_OGY82W69', NULL, NULL, 'P4', 'CRITICAL', '告警', '200.0', '>= 200.0', 0, '2026-09-14 21:50:59', NULL, NULL, NULL);
INSERT INTO `iot_alarm` VALUES (51, 'ALM_1789394082020_9BF5A9', 'RULE_484487', 'DEV_OGY82W69', NULL, NULL, 'P4', 'CRITICAL', '告警', '200.0', '>= 200.0', 0, '2026-09-14 21:54:42', NULL, NULL, NULL);
INSERT INTO `iot_alarm` VALUES (52, 'ALM_1789394303568_329271', 'RULE_484487', 'DEV_OGY82W69', NULL, NULL, 'P4', 'CRITICAL', '告警', '200.0', '>= 200.0', 0, '2026-09-14 21:58:24', NULL, NULL, NULL);

-- ----------------------------
-- Table structure for iot_device
-- ----------------------------
DROP TABLE IF EXISTS `iot_device`;
CREATE TABLE `iot_device`  (
  `id` bigint(20) NOT NULL AUTO_INCREMENT COMMENT '自增主键ID',
  `device_id` varchar(64) CHARACTER SET utf8mb4 COLLATE utf8mb4_0900_ai_ci NOT NULL COMMENT '物理设备唯一标识ID (如 dev_001)',
  `device_name` varchar(128) CHARACTER SET utf8mb4 COLLATE utf8mb4_0900_ai_ci NOT NULL COMMENT '物理设备显示名称',
  `model_key` varchar(64) CHARACTER SET utf8mb4 COLLATE utf8mb4_0900_ai_ci NOT NULL COMMENT '关联物模型Key',
  `driver_code` varchar(64) CHARACTER SET utf8mb4 COLLATE utf8mb4_0900_ai_ci NOT NULL COMMENT '绑定协议驱动Code (如 modbus-tcp)',
  `host` varchar(64) CHARACTER SET utf8mb4 COLLATE utf8mb4_0900_ai_ci NULL DEFAULT '192.168.1.100' COMMENT '设备通信IP地址/串口号',
  `port` int(11) NULL DEFAULT 502 COMMENT '设备通信端口号',
  `connection_params` text CHARACTER SET utf8mb4 COLLATE utf8mb4_0900_ai_ci NULL COMMENT '驱动拓展连接参数 (JSON格式)',
  `status` tinyint(4) NOT NULL DEFAULT 1 COMMENT '物理网络通道在线状态 (1:在线 Online, 0:离线 Offline, 2:通信异常 Error)',
  `acq_status` tinyint(4) NOT NULL DEFAULT 1 COMMENT '数据采集使能状态 (1:采集运行中 Active, 0:手动暂停采集 Paused)',
  `last_online_time` datetime(0) NULL DEFAULT NULL COMMENT '最后在线通信时间',
  `create_time` datetime(0) NOT NULL DEFAULT CURRENT_TIMESTAMP(0) COMMENT '设备创建时间',
  `update_time` datetime(0) NOT NULL DEFAULT CURRENT_TIMESTAMP(0) ON UPDATE CURRENT_TIMESTAMP(0) COMMENT '设备更新时间',
  PRIMARY KEY (`id`) USING BTREE,
  UNIQUE INDEX `device_id`(`device_id`) USING BTREE
) ENGINE = InnoDB AUTO_INCREMENT = 5 CHARACTER SET = utf8mb4 COLLATE = utf8mb4_0900_ai_ci COMMENT = '物理设备明细表' ROW_FORMAT = Dynamic;

-- ----------------------------
-- Records of iot_device
-- ----------------------------
INSERT INTO `iot_device` VALUES (1, 'DEV_001', '1号高压车间电动机传感器', 'pk_demo', 'modbus-tcp', '192.168.1.100', 502, '{\"host\":\"192.168.1.100\",\"port\":502,\"slaveId\":1}', 0, 0, NULL, '2026-09-09 20:04:04', '2026-09-10 20:23:20');
INSERT INTO `iot_device` VALUES (2, 'DEV_002', '2号储罐压力PLC', 'pk_plc_s7', 's7-driver', '192.168.2.12', 102, '{\"rack\":0,\"port\":102,\"host\":\"192.168.2.12\",\"slot\":1,\"plcType\":\"S7-1200\",\"byteOrder\":\"ABCD\"}', 0, 1, '2026-09-14 20:51:07', '2026-09-09 20:04:04', '2026-10-09 09:26:49');
INSERT INTO `iot_device` VALUES (3, 'DEV_003', '3号流量计OPC Server', 'pk_opcua_server', 'opc-ua', '192.168.1.50', 4840, '{\"endpointUrl\":\"opc.tcp://192.168.1.50:4840\",\"securityPolicy\":\"None\"}', 0, 1, NULL, '2026-09-09 20:04:04', '2026-10-09 09:26:49');
INSERT INTO `iot_device` VALUES (4, 'DEV_OGY82W69', 'Modbus设备调试', 'PK_6Z6UNCJ6', 'modbus-tcp', '192.168.2.12', 502, '{\"slaveId\":1,\"port\":502,\"host\":\"192.168.2.12\",\"byteOrder\":\"CDAB\"}', 0, 1, '2026-09-14 21:58:24', '2026-09-09 20:32:36', '2026-10-09 09:26:49');

-- ----------------------------
-- Table structure for iot_device_point_mapping
-- ----------------------------
DROP TABLE IF EXISTS `iot_device_point_mapping`;
CREATE TABLE `iot_device_point_mapping`  (
  `id` bigint(20) NOT NULL AUTO_INCREMENT COMMENT '自增主键ID',
  `device_id` varchar(64) CHARACTER SET utf8mb4 COLLATE utf8mb4_0900_ai_ci NOT NULL COMMENT '关联设备ID',
  `model_key` varchar(64) CHARACTER SET utf8mb4 COLLATE utf8mb4_0900_ai_ci NOT NULL COMMENT '关联物模型Key',
  `point_code` varchar(64) CHARACTER SET utf8mb4 COLLATE utf8mb4_0900_ai_ci NOT NULL COMMENT '物模型点位编码',
  `point_name` varchar(128) CHARACTER SET utf8mb4 COLLATE utf8mb4_0900_ai_ci NULL DEFAULT NULL COMMENT '点位中文名称',
  `physical_address` varchar(255) CHARACTER SET utf8mb4 COLLATE utf8mb4_0900_ai_ci NULL DEFAULT NULL COMMENT '底层物理协议地址',
  `data_type` varchar(32) CHARACTER SET utf8mb4 COLLATE utf8mb4_0900_ai_ci NULL DEFAULT 'DOUBLE' COMMENT '数据类型',
  `access_mode` varchar(16) CHARACTER SET utf8mb4 COLLATE utf8mb4_0900_ai_ci NULL DEFAULT 'R' COMMENT '读写模式',
  `scale` double NULL DEFAULT 1 COMMENT '换算缩放系数 Scale',
  `offset` double NULL DEFAULT 0 COMMENT '换算零点偏置 Offset',
  `remark` varchar(255) CHARACTER SET utf8mb4 COLLATE utf8mb4_0900_ai_ci NULL DEFAULT NULL COMMENT '备注信息',
  `create_time` datetime(0) NULL DEFAULT CURRENT_TIMESTAMP(0) COMMENT '创建时间',
  `update_time` datetime(0) NULL DEFAULT CURRENT_TIMESTAMP(0) ON UPDATE CURRENT_TIMESTAMP(0) COMMENT '更新时间',
  PRIMARY KEY (`id`) USING BTREE,
  UNIQUE INDEX `uk_device_point`(`device_id`, `point_code`) USING BTREE,
  INDEX `idx_device_id`(`device_id`) USING BTREE,
  INDEX `idx_model_key`(`model_key`) USING BTREE
) ENGINE = InnoDB AUTO_INCREMENT = 16 CHARACTER SET = utf8mb4 COLLATE = utf8mb4_0900_ai_ci COMMENT = '设备物模型点位物理地址映射表' ROW_FORMAT = Dynamic;

-- ----------------------------
-- Records of iot_device_point_mapping
-- ----------------------------
INSERT INTO `iot_device_point_mapping` VALUES (1, 'DEV_001', 'pk_demo', 'temperature', '主轴承实时温度', '40001', 'DOUBLE', 'R', 1, 0, NULL, '2026-09-09 20:04:04', '2026-09-09 20:04:04');
INSERT INTO `iot_device_point_mapping` VALUES (2, 'DEV_001', 'pk_demo', 'humidity', '环境相对湿度', '40003', 'DOUBLE', 'R', 1, 0, NULL, '2026-09-09 20:04:04', '2026-09-09 20:04:04');
INSERT INTO `iot_device_point_mapping` VALUES (3, 'DEV_001', 'pk_demo', 'pressure', '进气口设定压力', '40005', 'DOUBLE', 'RW', 1, 0, NULL, '2026-09-09 20:04:04', '2026-09-09 20:04:04');
INSERT INTO `iot_device_point_mapping` VALUES (4, 'DEV_001', 'pk_demo', 'targetSpeed', '变频目标转速', '40007', 'INT', 'RW', 1, 0, NULL, '2026-09-09 20:04:04', '2026-09-09 20:04:04');
INSERT INTO `iot_device_point_mapping` VALUES (5, 'DEV_001', 'pk_demo', 'autoCruiseMode', '全自动巡航开关', '00001', 'BOOL', 'RW', 1, 0, NULL, '2026-09-09 20:04:04', '2026-09-09 20:04:04');
INSERT INTO `iot_device_point_mapping` VALUES (6, 'DEV_001', 'pk_demo', 'workMode', '工况模式选择', '40008', 'ENUM', 'RW', 1, 0, NULL, '2026-09-09 20:04:04', '2026-09-09 20:04:04');
INSERT INTO `iot_device_point_mapping` VALUES (7, 'DEV_OGY82W69', 'PK_6Z6UNCJ6', 'temperture', '温度', '40001', 'FLOAT', 'RW', 1, 0, NULL, '2026-09-09 20:34:06', '2026-09-10 20:27:03');
INSERT INTO `iot_device_point_mapping` VALUES (9, 'DEV_OGY82W69', 'PK_6Z6UNCJ6', 'model', '模式', '40003', 'ENUM', 'RW', 1, 0, NULL, '2026-09-10 20:22:04', '2026-09-10 20:22:04');
INSERT INTO `iot_device_point_mapping` VALUES (10, 'DEV_OGY82W69', 'PK_6Z6UNCJ6', 'P3', '点位3', '40004', 'DOUBLE', 'RW', 1, 0, NULL, '2026-09-10 20:22:04', '2026-09-10 20:22:04');
INSERT INTO `iot_device_point_mapping` VALUES (11, 'DEV_OGY82W69', 'PK_6Z6UNCJ6', 'P4', '点位4', '40008', 'INT', 'RW', 1, 0, NULL, '2026-09-10 20:22:04', '2026-09-10 20:22:04');
INSERT INTO `iot_device_point_mapping` VALUES (12, 'DEV_002', 'pk_plc_s7', 'plcRunStatus', 'PLC 运行状态', 'DB10.DBX0.0', 'BOOL', 'RW', 1, 0, NULL, '2026-09-11 20:28:20', '2026-09-11 20:31:26');
INSERT INTO `iot_device_point_mapping` VALUES (13, 'DEV_002', 'pk_plc_s7', 'errorCode', '系统故障代码', 'DB10.DBD2', 'INT', 'RW', 1, 0, NULL, '2026-09-11 20:28:20', '2026-09-11 21:30:11');
INSERT INTO `iot_device_point_mapping` VALUES (14, 'DEV_002', 'pk_plc_s7', 'targetPressure', '储罐加压目标设定值', 'DB10.DBD10', 'DOUBLE', 'RW', 1, 0, NULL, '2026-09-11 20:28:20', '2026-09-11 21:21:18');
INSERT INTO `iot_device_point_mapping` VALUES (15, 'DEV_002', 'pk_plc_s7', 'remoteControlCmd', '远程启停遥控开关', 'DB10.DBX0.1', 'BOOL', 'RW', 1, 0, NULL, '2026-09-11 20:28:20', '2026-09-11 20:28:20');

-- ----------------------------
-- Table structure for iot_driver_info
-- ----------------------------
DROP TABLE IF EXISTS `iot_driver_info`;
CREATE TABLE `iot_driver_info`  (
  `id` bigint(20) NOT NULL AUTO_INCREMENT COMMENT '自增主键ID',
  `driver_code` varchar(64) CHARACTER SET utf8mb4 COLLATE utf8mb4_0900_ai_ci NOT NULL COMMENT '驱动唯一Code (如 modbus-tcp)',
  `driver_name` varchar(128) CHARACTER SET utf8mb4 COLLATE utf8mb4_0900_ai_ci NOT NULL COMMENT '驱动显示名称',
  `version` varchar(32) CHARACTER SET utf8mb4 COLLATE utf8mb4_0900_ai_ci NOT NULL DEFAULT '1.0.0' COMMENT '驱动版本号',
  `jar_file_name` varchar(256) CHARACTER SET utf8mb4 COLLATE utf8mb4_0900_ai_ci NULL DEFAULT NULL COMMENT '驱动Jar包文件名',
  `status` tinyint(4) NOT NULL DEFAULT 1 COMMENT '驱动激活状态 (1:启用, 0:停用)',
  `description` varchar(512) CHARACTER SET utf8mb4 COLLATE utf8mb4_0900_ai_ci NULL DEFAULT NULL COMMENT '驱动功能与协议详细描述',
  `manifest_yaml` text CHARACTER SET utf8mb4 COLLATE utf8mb4_0900_ai_ci NULL COMMENT '驱动Manifest YAML定义原文',
  `service_url` varchar(256) CHARACTER SET utf8mb4 COLLATE utf8mb4_0900_ai_ci NULL DEFAULT NULL COMMENT '驱动微服务运行URL地址 (如 http://localhost:28011)',
  `create_time` datetime(0) NOT NULL DEFAULT CURRENT_TIMESTAMP(0) COMMENT '注册时间',
  PRIMARY KEY (`id`) USING BTREE,
  UNIQUE INDEX `driver_code`(`driver_code`) USING BTREE
) ENGINE = InnoDB AUTO_INCREMENT = 7 CHARACTER SET = utf8mb4 COLLATE = utf8mb4_0900_ai_ci COMMENT = '协议驱动注册信息表' ROW_FORMAT = Dynamic;

-- ----------------------------
-- Records of iot_driver_info
-- ----------------------------
INSERT INTO `iot_driver_info` VALUES (5, 'modbus-tcp', 'Modbus TCP 工业驱动插件', '2.0.0', 'unihh-driver-modbus.jar', 1, 'modbus-tcp', 'driverCode: modbus-tcp\ndriverName: Modbus TCP 工业驱动插件\nversion: 2.0.0\nparams:\n  - key: host\n    name: IP地址\n    dataType: STRING\n    required: true\n    defaultValue: \'192.168.1.100\'\n  - key: port\n    name: 端口号\n    dataType: INT\n    required: true\n    defaultValue: \'502\'\n  - key: slaveId\n    name: 从站 ID\n    dataType: INT\n    required: true\n    defaultValue: \'1\'\n  - key: byteOrder\n    name: 寄存器字节序\n    dataType: SELECT\n    required: false\n    defaultValue: \'CDAB\'\n    description: \'32位/64位整型与浮点数在Modbus寄存器中的存储排列顺序 (推荐 CDAB)\'\n    options:\n      - label: \'CDAB (工业推荐 / 32位小端字大端字节)\'\n        value: \'CDAB\'\n      - label: \'ABCD (标准大端模式 / Big-Endian)\'\n        value: \'ABCD\'\n      - label: \'DCBA (完全小端模式 / Little-Endian)\'\n        value: \'DCBA\'\n      - label: \'BADC (字节交换模式 / Byte Swap)\'\n        value: \'BADC\'\n      - label: \'AB (16位标准大端)\'\n        value: \'AB\'\n      - label: \'BA (16位字节交换)\'\n        value: \'BA\'\n      - label: \'ABCDEFGH (64位标准大端)\'\n        value: \'ABCDEFGH\'\n      - label: \'GHEFCDAB (64位双字交换)\'\n        value: \'GHEFCDAB\'\n      - label: \'HGFEDCBA (64位完全小端)\'\n        value: \'HGFEDCBA\'\n      - label: \'BADCFEHG (64位字节交换)\'\n        value: \'BADCFEHG\'\n', 'http://192.168.204.1:28011', '2026-09-09 21:38:41');
INSERT INTO `iot_driver_info` VALUES (6, 's7-driver', '西门子 S7 纯正工业驱动插件', '2.0.0', NULL, 0, 's7-driver', 'driverCode: s7-driver\ndriverName: 西门子 S7 纯正工业驱动插件\nversion: 2.0.0\nparams:\n  - key: host\n    name: PLC IP地址\n    dataType: STRING\n    required: true\n    defaultValue: \'192.168.1.200\'\n  - key: port\n    name: 端口号\n    dataType: INT\n    required: true\n    defaultValue: \'102\'\n  - key: rack\n    name: 机架号 (Rack)\n    dataType: INT\n    required: true\n    defaultValue: \'0\'\n  - key: slot\n    name: 插槽号 (Slot)\n    dataType: INT\n    required: true\n    defaultValue: \'1\'\n  - key: plcType\n    name: PLC 型号\n    dataType: SELECT\n    required: false\n    defaultValue: \'S7-1200\'\n    description: \'西门子 S7 系列控制器型号 (自动适配机架与插槽)\'\n    options:\n      - label: \'S7-1200 (默认 Rack 0, Slot 1)\'\n        value: \'S7-1200\'\n      - label: \'S7-1500 (默认 Rack 0, Slot 1)\'\n        value: \'S7-1500\'\n      - label: \'S7-300 (默认 Rack 0, Slot 2)\'\n        value: \'S7-300\'\n      - label: \'S7-400 (默认 Rack 0, Slot 2)\'\n        value: \'S7-400\'\n      - label: \'S7-200 SMART (默认 Rack 0, Slot 1)\'\n        value: \'S7-200-SMART\'\n  - key: byteOrder\n    name: 寄存器字节序\n    dataType: SELECT\n    required: false\n    defaultValue: \'ABCD\'\n    description: \'32位/64位整型与浮点数在S7数据块中的存储排列顺序 (西门子原生推荐 ABCD)\'\n    options:\n      - label: \'ABCD (西门子原生标准大端模式 / Big-Endian)\'\n        value: \'ABCD\'\n      - label: \'CDAB (32位小端字大端字节)\'\n        value: \'CDAB\'\n      - label: \'DCBA (完全小端模式 / Little-Endian)\'\n        value: \'DCBA\'\n      - label: \'BADC (字节交换模式 / Byte Swap)\'\n        value: \'BADC\'\n      - label: \'AB (16位标准大端)\'\n        value: \'AB\'\n      - label: \'BA (16位字节交换)\'\n        value: \'BA\'\n      - label: \'ABCDEFGH (64位标准大端)\'\n        value: \'ABCDEFGH\'\n      - label: \'GHEFCDAB (64位双字交换)\'\n        value: \'GHEFCDAB\'\n      - label: \'HGFEDCBA (64位完全小端)\'\n        value: \'HGFEDCBA\'\n      - label: \'BADCFEHG (64位字节交换)\'\n        value: \'BADCFEHG\'\n', 'http://192.168.204.1:28012', '2026-09-11 20:27:21');

-- ----------------------------
-- Table structure for iot_log
-- ----------------------------
DROP TABLE IF EXISTS `iot_log`;
CREATE TABLE `iot_log`  (
  `id` bigint(20) NOT NULL AUTO_INCREMENT COMMENT '自增主键ID',
  `log_id` varchar(64) CHARACTER SET utf8mb4 COLLATE utf8mb4_0900_ai_ci NOT NULL COMMENT '日志记录唯一ID',
  `log_type` varchar(32) CHARACTER SET utf8mb4 COLLATE utf8mb4_0900_ai_ci NOT NULL DEFAULT 'DEVICE' COMMENT '日志类型 (DEVICE:设备日志, COMMAND:指令反控, SYSTEM:系统日志)',
  `device_id` varchar(64) CHARACTER SET utf8mb4 COLLATE utf8mb4_0900_ai_ci NULL DEFAULT NULL COMMENT '关联设备ID',
  `model_key` varchar(64) CHARACTER SET utf8mb4 COLLATE utf8mb4_0900_ai_ci NULL DEFAULT NULL COMMENT '关联物模型Key',
  `level` varchar(16) CHARACTER SET utf8mb4 COLLATE utf8mb4_0900_ai_ci NOT NULL DEFAULT 'INFO' COMMENT '日志级别 (DEBUG, INFO, WARN, ERROR)',
  `content` text CHARACTER SET utf8mb4 COLLATE utf8mb4_0900_ai_ci NOT NULL COMMENT '日志详细正文内容',
  `operator` varchar(64) CHARACTER SET utf8mb4 COLLATE utf8mb4_0900_ai_ci NULL DEFAULT 'SYSTEM' COMMENT '操作人/触发源',
  `create_time` datetime(0) NOT NULL DEFAULT CURRENT_TIMESTAMP(0) COMMENT '日志产生时间',
  PRIMARY KEY (`id`) USING BTREE,
  UNIQUE INDEX `log_id`(`log_id`) USING BTREE
) ENGINE = InnoDB AUTO_INCREMENT = 113 CHARACTER SET = utf8mb4 COLLATE = utf8mb4_0900_ai_ci COMMENT = '系统运行与审计日志表' ROW_FORMAT = Dynamic;

-- ----------------------------
-- Records of iot_log
-- ----------------------------
INSERT INTO `iot_log` VALUES (1, 'LOG_20260909_0001', 'COMMAND', 'DEV_001', 'pk_demo', 'INFO', '规则引擎 [RULE_101] 触发反控: 下发服务 startCoolingFan, 参数: {\"fanId\":1,\"speed\":3000}', 'RULE_ENGINE', '2026-09-09 20:04:04');
INSERT INTO `iot_log` VALUES (2, 'LOG_20260909_0002', 'COMMAND', 'DEV_002', 'pk_plc_s7', 'INFO', '强降温联动 - 开启高功率风机: 下发服务 switchPower, 参数: {\"powerState\":\"ON\",\"speedLevel\":3}', 'RULE_ENGINE', '2026-09-09 20:04:04');
INSERT INTO `iot_log` VALUES (3, 'LOG_20260909_0003', 'COMMAND', 'DEV_003', 'pk_opcua_server', 'ERROR', '自动关阀 - 管道压力异常紧急响应: 下发服务 closeValve 失败 [超时 5000ms]', 'RULE_ENGINE', '2026-09-09 20:04:04');
INSERT INTO `iot_log` VALUES (4, 'LOG_20260909_0004', 'COMMAND', 'DEV_001', 'pk_demo', 'INFO', '控制指令下发: 设置变频目标转速 targetSpeed=1500', 'admin', '2026-09-09 20:04:04');
INSERT INTO `iot_log` VALUES (5, 'LOG_20260909_0005', 'COMMAND', 'DEV_002', 'pk_plc_s7', 'INFO', '控制指令下发: 储罐加压复位 resetFault, 参数: {\"clearAll\":true}', 'operator01', '2026-09-09 20:04:04');
INSERT INTO `iot_log` VALUES (6, 'LOG_20260909_0006', 'SYSTEM', 'DEV_001', 'pk_demo', 'INFO', '用户 admin 成功登录系统管理控制台 [IP: 192.168.1.120, 客户端: Chrome/Win11]', 'admin', '2026-09-09 20:04:04');
INSERT INTO `iot_log` VALUES (7, 'LOG_20260909_0007', 'SYSTEM', 'DEV_002', 'pk_plc_s7', 'INFO', '用户 operator01 成功登录系统控制台 [IP: 192.168.1.145, 客户端: Edge/Win10]', 'operator01', '2026-09-09 20:04:04');
INSERT INTO `iot_log` VALUES (8, 'LOG_20260909_0008', 'SYSTEM', 'DEV_003', 'pk_opcua_server', 'WARN', '用户 test_user 尝试登录系统失败 [密码错误三次, IP: 222.128.10.88]', 'test_user', '2026-09-09 20:04:04');
INSERT INTO `iot_log` VALUES (9, 'LOG_20260909_0009', 'OPERATION', 'DEV_001', 'pk_demo', 'INFO', '规则管理: 修改告警规则 [RULE_101] 联动动作参数, 将临界阈值从 70.0℃ 调整为 75.0℃', 'admin', '2026-09-09 20:04:04');
INSERT INTO `iot_log` VALUES (10, 'LOG_20260909_0010', 'OPERATION', 'DEV_003', 'pk_opcua_server', 'INFO', '设备管理: 新增设备节点 [DEV_003], 注册 OPC UA 工业网关', 'admin', '2026-09-09 20:04:04');
INSERT INTO `iot_log` VALUES (11, 'LOG_20260909_0011', 'OPERATION', 'DEV_002', 'pk_plc_s7', 'INFO', '物模型管理: 更新物模型 [pk_plc_s7] 储罐压力点位物理映射', 'operator01', '2026-09-09 20:04:04');
INSERT INTO `iot_log` VALUES (12, 'LOG_20260909_0012', 'DEVICE', 'DEV_001', 'pk_demo', 'INFO', '1号高压车间电动机传感器 Modbus TCP 通讯握手正常', 'modbus-tcp-driver', '2026-09-09 20:04:04');
INSERT INTO `iot_log` VALUES (13, 'LOG_20260909_0013', 'DEVICE', 'DEV_002', 'pk_plc_s7', 'INFO', '2号储罐压力PLC 西门子 S7 协议轮询采集数据正常', 's7-driver', '2026-09-09 20:04:04');
INSERT INTO `iot_log` VALUES (14, 'LOG_20260909_0014', 'DEVICE', 'DEV_003', 'pk_opcua_server', 'WARN', 'OPC UA 驱动网络闪断重新建立长连接', 'opc-ua-driver', '2026-09-09 20:04:04');
INSERT INTO `iot_log` VALUES (15, 'LOG_20260909_0015', 'DEVICE', 'DEV_001', 'pk_demo', 'ERROR', '1号电机温度点位采集超时，触发硬件通道重试', 'modbus-tcp-driver', '2026-09-09 20:04:04');
INSERT INTO `iot_log` VALUES (16, 'LOG_1789043191401', 'COMMAND', 'DEV_OGY82W69', 'PK_6Z6UNCJ6', 'INFO', '控制指令下发: 设置点位 [temperture] (物理地址: 40001, 数据类型: DOUBLE, 字节序: CDAB, 系数: 1.0) 目标值: 0.88', 'admin', '2026-09-10 20:26:31');
INSERT INTO `iot_log` VALUES (17, 'LOG_1789043199808', 'COMMAND', 'DEV_OGY82W69', 'PK_6Z6UNCJ6', 'INFO', '控制指令下发: 设置点位 [P3] (物理地址: 40004, 数据类型: DOUBLE, 字节序: CDAB, 系数: 1.0) 目标值: 0.66', 'admin', '2026-09-10 20:26:39');
INSERT INTO `iot_log` VALUES (18, 'LOG_1789043248054', 'COMMAND', 'DEV_OGY82W69', 'PK_6Z6UNCJ6', 'INFO', '控制指令下发: 设置点位 [temperture] (物理地址: 40001, 数据类型: FLOAT, 字节序: CDAB, 系数: 1.0) 目标值: 0.88', 'admin', '2026-09-10 20:27:28');
INSERT INTO `iot_log` VALUES (19, 'LOG_1789043298410', 'COMMAND', 'DEV_OGY82W69', 'PK_6Z6UNCJ6', 'INFO', '控制指令下发: 调用服务 [Init_Modbus_Tcp], 参数: {\"temperture\":0.88,\"model\":0,\"P3\":0.66,\"P4\":0}', 'admin', '2026-09-10 20:28:18');
INSERT INTO `iot_log` VALUES (20, 'LOG_1789043309638', 'COMMAND', 'DEV_OGY82W69', 'PK_6Z6UNCJ6', 'INFO', '控制指令下发: 设置点位 [temperture] (物理地址: 40001, 数据类型: FLOAT, 字节序: CDAB, 系数: 1.0) 目标值: 25.880001', 'admin', '2026-09-10 20:28:29');
INSERT INTO `iot_log` VALUES (21, 'LOG_1789043315387', 'COMMAND', 'DEV_OGY82W69', 'PK_6Z6UNCJ6', 'INFO', '控制指令下发: 设置点位 [temperture] (物理地址: 40001, 数据类型: FLOAT, 字节序: CDAB, 系数: 1.0) 目标值: 25.880001', 'admin', '2026-09-10 20:28:35');
INSERT INTO `iot_log` VALUES (22, 'LOG_1789044027979', 'COMMAND', 'DEV_OGY82W69', 'PK_6Z6UNCJ6', 'INFO', '控制指令下发: 设置点位 [temperture] (物理地址: 40001, 数据类型: FLOAT, 字节序: CDAB, 系数: 1.0) 目标值: 0.88', 'admin', '2026-09-10 20:40:27');
INSERT INTO `iot_log` VALUES (23, 'LOG_1789044310014', 'COMMAND', 'DEV_OGY82W69', 'PK_6Z6UNCJ6', 'INFO', '控制指令下发: 调用服务 [Init_Modbus_Tcp], 参数: {\"temperture\":0.88,\"model\":0,\"P3\":0.66,\"P4\":0}', 'admin', '2026-09-10 20:45:10');
INSERT INTO `iot_log` VALUES (24, 'LOG_1789045378373', 'COMMAND', 'DEV_OGY82W69', 'PK_6Z6UNCJ6', 'INFO', '控制指令下发: 调用服务 [Init_Modbus_Tcp], 参数: {\"temperture\":0.88,\"model\":0,\"P3\":0.66,\"P4\":0}', 'admin', '2026-09-10 21:02:58');
INSERT INTO `iot_log` VALUES (25, 'LOG_1789129400254', 'COMMAND', 'DEV_OGY82W69', 'PK_6Z6UNCJ6', 'INFO', '控制指令下发: 设置点位 [P4] (物理地址: 40008, 数据类型: INT, 字节序: CDAB, 系数: 1.0) 目标值: 198', 'admin', '2026-09-11 20:23:20');
INSERT INTO `iot_log` VALUES (26, 'LOG_20260911_202320_E404', 'OPERATION', 'DEV_OGY82W69', NULL, 'INFO', '{\"module\":\"设备管理\",\"action\":\"下发设备点位写入控制\",\"params\":\"{\\\"deviceId\\\":\\\"DEV_OGY82W69\\\",\\\"body\\\":{\\\"pointCode\\\":\\\"P4\\\",\\\"value\\\":198,\\\"dataType\\\":\\\"INT\\\"}}\",\"ip\":\"127.0.0.1\",\"costMs\":7,\"status\":\"SUCCESS\"}', 'admin', '2026-09-11 20:23:20');
INSERT INTO `iot_log` VALUES (27, 'LOG_1789129402691_3114', 'COMMAND', 'DEV_OGY82W69', 'PK_6Z6UNCJ6', 'INFO', '规则联动控制指令下发成功: 规则 [RULE_484487 - Modbus测试设备重置], 动作 [Modbus设备重置默认值], 目标设备 [DEV_OGY82W69], 驱动 [modbus-tcp (192.168.2.12:502)], 服务 [Init_Modbus_Tcp], 物理下发点位: [temperture=0.88 (40001, FLOAT, CDAB), model=0 (40003, ENUM, CDAB), P3=0.66 (40004, DOUBLE, CDAB), P4=0 (40008, INT, CDAB)]', 'RULE_ENGINE', '2026-09-11 20:23:22');
INSERT INTO `iot_log` VALUES (28, 'LOG_1789129615282_03E4', 'COMMAND', 'DEV_OGY82W69', 'PK_6Z6UNCJ6', 'INFO', '规则联动控制指令下发成功: 规则 [RULE_484487 - Modbus测试设备重置], 动作 [Modbus设备重置默认值], 目标设备 [DEV_OGY82W69], 驱动 [modbus-tcp (192.168.2.12:502)], 服务 [Init_Modbus_Tcp], 物理下发点位: [temperture=0.88 (40001, FLOAT, CDAB), model=0 (40003, ENUM, CDAB), P3=0.66 (40004, DOUBLE, CDAB), P4=0 (40008, INT, CDAB)]', 'RULE_ENGINE', '2026-09-11 20:26:55');
INSERT INTO `iot_log` VALUES (29, 'LOG_20260911_202721_E620', 'OPERATION', NULL, NULL, 'INFO', '{\"module\":\"驱动管理\",\"action\":\"注册协议驱动\",\"params\":\"{\\\"driverInfo\\\":{\\\"description\\\":\\\"支持西门子 S7-200/300/400/1200/1500 PLC 工业以太网 ISO-on-TCP (TPKT/COTP) 协议点位读写与解析\\\",\\\"driverCode\\\":\\\"s7-driver\\\",\\\"driverName\\\":\\\"西门子 S7 工业协议驱动\\\",\\\"jarFileName\\\":\\\"unihh-driver-s7.jar\\\",\\\"manifestYaml\\\":\\\"driverCode: s7-driver\\\\ndriverName: 西门子 S7 工业协议驱动\\\\nserviceUrl: http://172.18.0.1:28012\\\\nversion: 2.0.0\\\\ndescription: 支持西门子 S7-200/300/400/1200/1500 PLC 工业以太网 ISO-on-TCP (TPKT/COTP) 协议点位读写与解析\\\\nparams:\\\\n  - key: host\\\\n    name: PLC IP地址\\\\n    dataType: STRING\\\\n    required: true\\\\n    defaultValue: \'192.168.1.200\'\\\\n  - key: port\\\\n    name: 端口号\\\\n    dataType: INT\\\\n    required: true\\\\n    defaultValue: \'102\'\\\\n  - key: rack\\\\n    name: 机架号 (Rack)\\\\n    dataType: INT\\\\n    required: true\\\\n    defaultValue: \'0\'\\\\n  - key: slot\\\\n    name: 插槽号 (Slot)\\\\n    dataType: INT\\\\n    required: true\\\\n    defa...(截断)\",\"ip\":\"127.0.0.1\",\"costMs\":6,\"status\":\"SUCCESS\"}', 'admin', '2026-09-11 20:27:21');
INSERT INTO `iot_log` VALUES (30, 'LOG_20260911_202820_8ECC', 'OPERATION', 'DEV_002', NULL, 'INFO', '{\"module\":\"设备管理\",\"action\":\"保存物理点位地址映射\",\"params\":\"{\\\"deviceId\\\":\\\"DEV_002\\\",\\\"mappings\\\":[{\\\"accessMode\\\":\\\"R\\\",\\\"dataType\\\":\\\"BOOL\\\",\\\"deviceId\\\":\\\"DEV_002\\\",\\\"modelKey\\\":\\\"pk_plc_s7\\\",\\\"offset\\\":0.0,\\\"physicalAddress\\\":\\\"DB10.DBX0.0\\\",\\\"pointCode\\\":\\\"plcRunStatus\\\",\\\"pointName\\\":\\\"PLC 运行状态\\\",\\\"scale\\\":1.0},{\\\"accessMode\\\":\\\"R\\\",\\\"dataType\\\":\\\"INT\\\",\\\"deviceId\\\":\\\"DEV_002\\\",\\\"modelKey\\\":\\\"pk_plc_s7\\\",\\\"offset\\\":0.0,\\\"physicalAddress\\\":\\\"DB10.DBW2\\\",\\\"pointCode\\\":\\\"errorCode\\\",\\\"pointName\\\":\\\"系统故障代码\\\",\\\"scale\\\":1.0},{\\\"accessMode\\\":\\\"RW\\\",\\\"dataType\\\":\\\"DOUBLE\\\",\\\"deviceId\\\":\\\"DEV_002\\\",\\\"modelKey\\\":\\\"pk_plc_s7\\\",\\\"offset\\\":0.0,\\\"physicalAddress\\\":\\\"DB10.DBW10\\\",\\\"pointCode\\\":\\\"targetPressure\\\",\\\"pointName\\\":\\\"储罐加压目标设定值\\\",\\\"scale\\\":1.0},{\\\"accessMode\\\":\\\"RW\\\",\\\"dataType\\\":\\\"BOOL\\\",\\\"deviceId\\\":\\\"DEV_002\\\",\\\"modelKey\\\":\\\"pk_plc_s7\\\",\\\"offset\\\":0.0,\\\"physicalAddress\\\":\\\"DB10.DBX0.1\\\",\\\"pointCode\\\":\\\"remoteControlCmd\\\",\\\"pointName\\\":\\\"远程启停遥控开关\\\",\\\"scale\\\":1.0}]}\",\"ip\":\"127.0.0.1\",\"costMs\":6,\"status\":\"SUCCESS\"}', 'admin', '2026-09-11 20:28:20');
INSERT INTO `iot_log` VALUES (31, 'LOG_20260911_202849_4F20', 'OPERATION', 'DEV_002', NULL, 'INFO', '{\"module\":\"设备管理\",\"action\":\"更新物理设备配置\",\"params\":\"{\\\"deviceId\\\":\\\"DEV_002\\\",\\\"device\\\":{\\\"acqStatus\\\":1,\\\"baseIntervalMs\\\":1000,\\\"connectionParams\\\":{\\\"rack\\\":0,\\\"port\\\":102,\\\"host\\\":\\\"192.168.2.12\\\",\\\"slot\\\":1},\\\"deviceId\\\":\\\"DEV_002\\\",\\\"deviceName\\\":\\\"2号储罐压力PLC\\\",\\\"driverCode\\\":\\\"s7-driver\\\",\\\"host\\\":\\\"192.168.2.12\\\",\\\"modelKey\\\":\\\"pk_plc_s7\\\",\\\"port\\\":102,\\\"status\\\":0}}\",\"ip\":\"127.0.0.1\",\"costMs\":67,\"status\":\"SUCCESS\"}', 'admin', '2026-09-11 20:28:49');
INSERT INTO `iot_log` VALUES (32, 'LOG_1789129831519_3706', 'COMMAND', 'DEV_OGY82W69', 'PK_6Z6UNCJ6', 'INFO', '规则联动控制指令下发成功: 规则 [RULE_484487 - Modbus测试设备重置], 动作 [Modbus设备重置默认值], 目标设备 [DEV_OGY82W69], 驱动 [modbus-tcp (192.168.2.12:502)], 服务 [Init_Modbus_Tcp], 物理下发点位: [temperture=0.88 (40001, FLOAT, CDAB), model=0 (40003, ENUM, CDAB), P3=0.66 (40004, DOUBLE, CDAB), P4=0 (40008, INT, CDAB)]', 'RULE_ENGINE', '2026-09-11 20:30:31');
INSERT INTO `iot_log` VALUES (33, 'LOG_20260911_203126_3F40', 'OPERATION', NULL, 'pk_plc_s7', 'INFO', '{\"module\":\"物模型管理\",\"action\":\"更新属性点位\",\"params\":\"{\\\"modelKey\\\":\\\"pk_plc_s7\\\",\\\"pointCode\\\":\\\"plcRunStatus\\\",\\\"prop\\\":{\\\"accessMode\\\":\\\"RW\\\",\\\"dataType\\\":\\\"BOOL\\\",\\\"description\\\":\\\"PLC 当前 RUN/STOP 标志\\\",\\\"enumOptions\\\":[],\\\"offset\\\":0.0,\\\"pointCode\\\":\\\"plcRunStatus\\\",\\\"pointName\\\":\\\"PLC 运行状态\\\",\\\"sampleFactor\\\":1,\\\"scale\\\":1.0,\\\"triggerMode\\\":\\\"PERIODIC\\\",\\\"unit\\\":\\\"\\\"}}\",\"ip\":\"127.0.0.1\",\"costMs\":6,\"status\":\"SUCCESS\"}', 'admin', '2026-09-11 20:31:26');
INSERT INTO `iot_log` VALUES (34, 'LOG_20260911_203130_9626', 'OPERATION', NULL, 'pk_plc_s7', 'INFO', '{\"module\":\"物模型管理\",\"action\":\"更新属性点位\",\"params\":\"{\\\"modelKey\\\":\\\"pk_plc_s7\\\",\\\"pointCode\\\":\\\"errorCode\\\",\\\"prop\\\":{\\\"accessMode\\\":\\\"RW\\\",\\\"dataType\\\":\\\"INT\\\",\\\"description\\\":\\\"故障报警码\\\",\\\"enumOptions\\\":[],\\\"offset\\\":0.0,\\\"pointCode\\\":\\\"errorCode\\\",\\\"pointName\\\":\\\"系统故障代码\\\",\\\"sampleFactor\\\":1,\\\"scale\\\":1.0,\\\"triggerMode\\\":\\\"PERIODIC\\\",\\\"unit\\\":\\\"\\\"}}\",\"ip\":\"127.0.0.1\",\"costMs\":4,\"status\":\"SUCCESS\"}', 'admin', '2026-09-11 20:31:30');
INSERT INTO `iot_log` VALUES (35, 'LOG_1789129909745', 'COMMAND', 'DEV_002', 'pk_plc_s7', 'INFO', '控制指令下发: 设置点位 [plcRunStatus] (物理地址: DB10.DBX0.0, 数据类型: BOOL, 字节序: CDAB, 系数: 1.0) 目标值: false', 'admin', '2026-09-11 20:31:49');
INSERT INTO `iot_log` VALUES (36, 'LOG_20260911_203149_BFB3', 'OPERATION', 'DEV_002', NULL, 'INFO', '{\"module\":\"设备管理\",\"action\":\"下发设备点位写入控制\",\"params\":\"{\\\"deviceId\\\":\\\"DEV_002\\\",\\\"body\\\":{\\\"pointCode\\\":\\\"plcRunStatus\\\",\\\"value\\\":false,\\\"dataType\\\":\\\"BOOL\\\"}}\",\"ip\":\"127.0.0.1\",\"costMs\":5,\"status\":\"SUCCESS\"}', 'admin', '2026-09-11 20:31:49');
INSERT INTO `iot_log` VALUES (37, 'LOG_1789129915123', 'COMMAND', 'DEV_002', 'pk_plc_s7', 'INFO', '控制指令下发: 设置点位 [plcRunStatus] (物理地址: DB10.DBX0.0, 数据类型: BOOL, 字节序: CDAB, 系数: 1.0) 目标值: true', 'admin', '2026-09-11 20:31:55');
INSERT INTO `iot_log` VALUES (38, 'LOG_20260911_203155_96AC', 'OPERATION', 'DEV_002', NULL, 'INFO', '{\"module\":\"设备管理\",\"action\":\"下发设备点位写入控制\",\"params\":\"{\\\"deviceId\\\":\\\"DEV_002\\\",\\\"body\\\":{\\\"pointCode\\\":\\\"plcRunStatus\\\",\\\"value\\\":true,\\\"dataType\\\":\\\"BOOL\\\"}}\",\"ip\":\"127.0.0.1\",\"costMs\":3,\"status\":\"SUCCESS\"}', 'admin', '2026-09-11 20:31:55');
INSERT INTO `iot_log` VALUES (39, 'LOG_1789130048396_8534', 'COMMAND', 'DEV_OGY82W69', 'PK_6Z6UNCJ6', 'INFO', '规则联动控制指令下发成功: 规则 [RULE_484487 - Modbus测试设备重置], 动作 [Modbus设备重置默认值], 目标设备 [DEV_OGY82W69], 驱动 [modbus-tcp (192.168.2.12:502)], 服务 [Init_Modbus_Tcp], 物理下发点位: [temperture=0.88 (40001, FLOAT, CDAB), model=0 (40003, ENUM, CDAB), P3=0.66 (40004, DOUBLE, CDAB), P4=0 (40008, INT, CDAB)]', 'RULE_ENGINE', '2026-09-11 20:34:08');
INSERT INTO `iot_log` VALUES (40, 'LOG_1789130262470_5FCC', 'COMMAND', 'DEV_OGY82W69', 'PK_6Z6UNCJ6', 'INFO', '规则联动控制指令下发成功: 规则 [RULE_484487 - Modbus测试设备重置], 动作 [Modbus设备重置默认值], 目标设备 [DEV_OGY82W69], 驱动 [modbus-tcp (192.168.2.12:502)], 服务 [Init_Modbus_Tcp], 物理下发点位: [temperture=0.88 (40001, FLOAT, CDAB), model=0 (40003, ENUM, CDAB), P3=0.66 (40004, DOUBLE, CDAB), P4=0 (40008, INT, CDAB)]', 'RULE_ENGINE', '2026-09-11 20:37:42');
INSERT INTO `iot_log` VALUES (41, 'LOG_1789130477224_A18F', 'COMMAND', 'DEV_OGY82W69', 'PK_6Z6UNCJ6', 'INFO', '规则联动控制指令下发成功: 规则 [RULE_484487 - Modbus测试设备重置], 动作 [Modbus设备重置默认值], 目标设备 [DEV_OGY82W69], 驱动 [modbus-tcp (192.168.2.12:502)], 服务 [Init_Modbus_Tcp], 物理下发点位: [temperture=0.88 (40001, FLOAT, CDAB), model=0 (40003, ENUM, CDAB), P3=0.66 (40004, DOUBLE, CDAB), P4=0 (40008, INT, CDAB)]', 'RULE_ENGINE', '2026-09-11 20:41:17');
INSERT INTO `iot_log` VALUES (42, 'LOG_1789130693581_1A09', 'COMMAND', 'DEV_OGY82W69', 'PK_6Z6UNCJ6', 'INFO', '规则联动控制指令下发成功: 规则 [RULE_484487 - Modbus测试设备重置], 动作 [Modbus设备重置默认值], 目标设备 [DEV_OGY82W69], 驱动 [modbus-tcp (192.168.2.12:502)], 服务 [Init_Modbus_Tcp], 物理下发点位: [temperture=0.88 (40001, FLOAT, CDAB), model=0 (40003, ENUM, CDAB), P3=0.66 (40004, DOUBLE, CDAB), P4=0 (40008, INT, CDAB)]', 'RULE_ENGINE', '2026-09-11 20:44:53');
INSERT INTO `iot_log` VALUES (43, 'LOG_1789130911192_1AC5', 'COMMAND', 'DEV_OGY82W69', 'PK_6Z6UNCJ6', 'INFO', '规则联动控制指令下发成功: 规则 [RULE_484487 - Modbus测试设备重置], 动作 [Modbus设备重置默认值], 目标设备 [DEV_OGY82W69], 驱动 [modbus-tcp (192.168.2.12:502)], 服务 [Init_Modbus_Tcp], 物理下发点位: [temperture=0.88 (40001, FLOAT, CDAB), model=0 (40003, ENUM, CDAB), P3=0.66 (40004, DOUBLE, CDAB), P4=0 (40008, INT, CDAB)]', 'RULE_ENGINE', '2026-09-11 20:48:31');
INSERT INTO `iot_log` VALUES (44, 'LOG_1789131130108_4FC7', 'COMMAND', 'DEV_OGY82W69', 'PK_6Z6UNCJ6', 'INFO', '规则联动控制指令下发成功: 规则 [RULE_484487 - Modbus测试设备重置], 动作 [Modbus设备重置默认值], 目标设备 [DEV_OGY82W69], 驱动 [modbus-tcp (192.168.2.12:502)], 服务 [Init_Modbus_Tcp], 物理下发点位: [temperture=0.88 (40001, FLOAT, CDAB), model=0 (40003, ENUM, CDAB), P3=0.66 (40004, DOUBLE, CDAB), P4=0 (40008, INT, CDAB)]', 'RULE_ENGINE', '2026-09-11 20:52:10');
INSERT INTO `iot_log` VALUES (45, 'LOG_1789131349577_584C', 'COMMAND', 'DEV_OGY82W69', 'PK_6Z6UNCJ6', 'INFO', '规则联动控制指令下发成功: 规则 [RULE_484487 - Modbus测试设备重置], 动作 [Modbus设备重置默认值], 目标设备 [DEV_OGY82W69], 驱动 [modbus-tcp (192.168.2.12:502)], 服务 [Init_Modbus_Tcp], 物理下发点位: [temperture=0.88 (40001, FLOAT, CDAB), model=0 (40003, ENUM, CDAB), P3=0.66 (40004, DOUBLE, CDAB), P4=0 (40008, INT, CDAB)]', 'RULE_ENGINE', '2026-09-11 20:55:49');
INSERT INTO `iot_log` VALUES (46, 'LOG_1789131570209_13E4', 'COMMAND', 'DEV_OGY82W69', 'PK_6Z6UNCJ6', 'INFO', '规则联动控制指令下发成功: 规则 [RULE_484487 - Modbus测试设备重置], 动作 [Modbus设备重置默认值], 目标设备 [DEV_OGY82W69], 驱动 [modbus-tcp (192.168.2.12:502)], 服务 [Init_Modbus_Tcp], 物理下发点位: [temperture=0.88 (40001, FLOAT, CDAB), model=0 (40003, ENUM, CDAB), P3=0.66 (40004, DOUBLE, CDAB), P4=0 (40008, INT, CDAB)]', 'RULE_ENGINE', '2026-09-11 20:59:30');
INSERT INTO `iot_log` VALUES (47, 'LOG_1789131789519_68D9', 'COMMAND', 'DEV_OGY82W69', 'PK_6Z6UNCJ6', 'INFO', '规则联动控制指令下发成功: 规则 [RULE_484487 - Modbus测试设备重置], 动作 [Modbus设备重置默认值], 目标设备 [DEV_OGY82W69], 驱动 [modbus-tcp (192.168.2.12:502)], 服务 [Init_Modbus_Tcp], 物理下发点位: [temperture=0.88 (40001, FLOAT, CDAB), model=0 (40003, ENUM, CDAB), P3=0.66 (40004, DOUBLE, CDAB), P4=0 (40008, INT, CDAB)]', 'RULE_ENGINE', '2026-09-11 21:03:09');
INSERT INTO `iot_log` VALUES (48, 'LOG_1789132010061_99F5', 'COMMAND', 'DEV_OGY82W69', 'PK_6Z6UNCJ6', 'INFO', '规则联动控制指令下发成功: 规则 [RULE_484487 - Modbus测试设备重置], 动作 [Modbus设备重置默认值], 目标设备 [DEV_OGY82W69], 驱动 [modbus-tcp (192.168.2.12:502)], 服务 [Init_Modbus_Tcp], 物理下发点位: [temperture=0.88 (40001, FLOAT, CDAB), model=0 (40003, ENUM, CDAB), P3=0.66 (40004, DOUBLE, CDAB), P4=0 (40008, INT, CDAB)]', 'RULE_ENGINE', '2026-09-11 21:06:50');
INSERT INTO `iot_log` VALUES (49, 'LOG_1789132174629', 'COMMAND', 'DEV_002', 'pk_plc_s7', 'INFO', '控制指令下发: 设置点位 [remoteControlCmd] (物理地址: DB10.DBX0.1, 数据类型: BOOL, 字节序: ABCD, 系数: 1.0) 目标值: true', 'admin', '2026-09-11 21:09:34');
INSERT INTO `iot_log` VALUES (50, 'LOG_20260911_210934_C851', 'OPERATION', 'DEV_002', NULL, 'INFO', '{\"module\":\"设备管理\",\"action\":\"下发设备点位写入控制\",\"params\":\"{\\\"deviceId\\\":\\\"DEV_002\\\",\\\"body\\\":{\\\"pointCode\\\":\\\"remoteControlCmd\\\",\\\"value\\\":true}}\",\"ip\":\"127.0.0.1\",\"costMs\":15,\"status\":\"SUCCESS\"}', 'admin', '2026-09-11 21:09:34');
INSERT INTO `iot_log` VALUES (51, 'LOG_1789132234470_A6A8', 'COMMAND', 'DEV_OGY82W69', 'PK_6Z6UNCJ6', 'INFO', '规则联动控制指令下发成功: 规则 [RULE_484487 - Modbus测试设备重置], 动作 [Modbus设备重置默认值], 目标设备 [DEV_OGY82W69], 驱动 [modbus-tcp (192.168.2.12:502)], 服务 [Init_Modbus_Tcp], 物理下发点位: [temperture=0.88 (40001, FLOAT, CDAB), model=0 (40003, ENUM, CDAB), P3=0.66 (40004, DOUBLE, CDAB), P4=0 (40008, INT, CDAB)]', 'RULE_ENGINE', '2026-09-11 21:10:34');
INSERT INTO `iot_log` VALUES (52, 'LOG_1789132310663', 'COMMAND', 'DEV_002', 'pk_plc_s7', 'INFO', '控制指令下发: 设置点位 [targetPressure] (物理地址: DB10.DBW10, 数据类型: DOUBLE, 字节序: ABCD, 系数: 1.0) 目标值: 68.5', 'admin', '2026-09-11 21:11:50');
INSERT INTO `iot_log` VALUES (53, 'LOG_20260911_211150_92B0', 'OPERATION', 'DEV_002', NULL, 'INFO', '{\"module\":\"设备管理\",\"action\":\"下发设备点位写入控制\",\"params\":\"{\\\"deviceId\\\":\\\"DEV_002\\\",\\\"body\\\":{\\\"pointCode\\\":\\\"targetPressure\\\",\\\"value\\\":68.5}}\",\"ip\":\"127.0.0.1\",\"costMs\":6,\"status\":\"SUCCESS\"}', 'admin', '2026-09-11 21:11:50');
INSERT INTO `iot_log` VALUES (54, 'LOG_1789132456312_8011', 'COMMAND', 'DEV_OGY82W69', 'PK_6Z6UNCJ6', 'INFO', '规则联动控制指令下发成功: 规则 [RULE_484487 - Modbus测试设备重置], 动作 [Modbus设备重置默认值], 目标设备 [DEV_OGY82W69], 驱动 [modbus-tcp (192.168.2.12:502)], 服务 [Init_Modbus_Tcp], 物理下发点位: [temperture=0.88 (40001, FLOAT, CDAB), model=0 (40003, ENUM, CDAB), P3=0.66 (40004, DOUBLE, CDAB), P4=0 (40008, INT, CDAB)]', 'RULE_ENGINE', '2026-09-11 21:14:16');
INSERT INTO `iot_log` VALUES (55, 'LOG_1789132680078_CF39', 'COMMAND', 'DEV_OGY82W69', 'PK_6Z6UNCJ6', 'INFO', '规则联动控制指令下发成功: 规则 [RULE_484487 - Modbus测试设备重置], 动作 [Modbus设备重置默认值], 目标设备 [DEV_OGY82W69], 驱动 [modbus-tcp (192.168.2.12:502)], 服务 [Init_Modbus_Tcp], 物理下发点位: [temperture=0.88 (40001, FLOAT, CDAB), model=0 (40003, ENUM, CDAB), P3=0.66 (40004, DOUBLE, CDAB), P4=0 (40008, INT, CDAB)]', 'RULE_ENGINE', '2026-09-11 21:18:00');
INSERT INTO `iot_log` VALUES (56, 'LOG_20260911_212006_90D6', 'OPERATION', 'DEV_002', NULL, 'INFO', '{\"module\":\"设备管理\",\"action\":\"更新物理设备配置\",\"params\":\"{\\\"deviceId\\\":\\\"DEV_002\\\",\\\"device\\\":{\\\"acqStatus\\\":1,\\\"baseIntervalMs\\\":1000,\\\"connectionParams\\\":{\\\"rack\\\":0,\\\"port\\\":102,\\\"host\\\":\\\"192.168.2.12\\\",\\\"slot\\\":1,\\\"plcType\\\":\\\"S7-1200\\\",\\\"byteOrder\\\":\\\"ABCD\\\"},\\\"deviceId\\\":\\\"DEV_002\\\",\\\"deviceName\\\":\\\"2号储罐压力PLC\\\",\\\"driverCode\\\":\\\"s7-driver\\\",\\\"host\\\":\\\"192.168.2.12\\\",\\\"modelKey\\\":\\\"pk_plc_s7\\\",\\\"port\\\":102,\\\"status\\\":1}}\",\"ip\":\"127.0.0.1\",\"costMs\":61,\"status\":\"SUCCESS\"}', 'admin', '2026-09-11 21:20:06');
INSERT INTO `iot_log` VALUES (57, 'LOG_1789132828429', 'COMMAND', 'DEV_002', 'pk_plc_s7', 'INFO', '控制指令下发: 设置点位 [remoteControlCmd] (物理地址: DB10.DBX0.1, 数据类型: BOOL, 字节序: ABCD, 系数: 1.0) 目标值: false', 'admin', '2026-09-11 21:20:28');
INSERT INTO `iot_log` VALUES (58, 'LOG_20260911_212028_3897', 'OPERATION', 'DEV_002', NULL, 'INFO', '{\"module\":\"设备管理\",\"action\":\"下发设备点位写入控制\",\"params\":\"{\\\"deviceId\\\":\\\"DEV_002\\\",\\\"body\\\":{\\\"pointCode\\\":\\\"remoteControlCmd\\\",\\\"value\\\":false,\\\"dataType\\\":\\\"BOOL\\\"}}\",\"ip\":\"127.0.0.1\",\"costMs\":12,\"status\":\"SUCCESS\"}', 'admin', '2026-09-11 21:20:28');
INSERT INTO `iot_log` VALUES (59, 'LOG_20260911_212118_2554', 'OPERATION', 'DEV_002', NULL, 'INFO', '{\"module\":\"设备管理\",\"action\":\"保存物理点位地址映射\",\"params\":\"{\\\"deviceId\\\":\\\"DEV_002\\\",\\\"mappings\\\":[{\\\"accessMode\\\":\\\"RW\\\",\\\"createTime\\\":\\\"2026-09-11 20:28:20\\\",\\\"dataType\\\":\\\"BOOL\\\",\\\"deviceId\\\":\\\"DEV_002\\\",\\\"id\\\":12,\\\"modelKey\\\":\\\"pk_plc_s7\\\",\\\"offset\\\":0.0,\\\"physicalAddress\\\":\\\"DB10.DBX0.0\\\",\\\"pointCode\\\":\\\"plcRunStatus\\\",\\\"pointName\\\":\\\"PLC 运行状态\\\",\\\"scale\\\":1.0,\\\"updateTime\\\":\\\"2026-09-11 20:31:26\\\"},{\\\"accessMode\\\":\\\"RW\\\",\\\"createTime\\\":\\\"2026-09-11 20:28:20\\\",\\\"dataType\\\":\\\"INT\\\",\\\"deviceId\\\":\\\"DEV_002\\\",\\\"id\\\":13,\\\"modelKey\\\":\\\"pk_plc_s7\\\",\\\"offset\\\":0.0,\\\"physicalAddress\\\":\\\"DB10.DBW2\\\",\\\"pointCode\\\":\\\"errorCode\\\",\\\"pointName\\\":\\\"系统故障代码\\\",\\\"scale\\\":1.0,\\\"updateTime\\\":\\\"2026-09-11 20:31:30\\\"},{\\\"accessMode\\\":\\\"RW\\\",\\\"createTime\\\":\\\"2026-09-11 20:28:20\\\",\\\"dataType\\\":\\\"DOUBLE\\\",\\\"deviceId\\\":\\\"DEV_002\\\",\\\"id\\\":14,\\\"modelKey\\\":\\\"pk_plc_s7\\\",\\\"offset\\\":0.0,\\\"physicalAddress\\\":\\\"DB10.DBD10\\\",\\\"pointCode\\\":\\\"targetPressure\\\",\\\"pointName\\\":\\\"储罐加压目标设定值\\\",\\\"scale\\\":1.0,\\\"upda...(截断)\",\"ip\":\"127.0.0.1\",\"costMs\":5,\"status\":\"SUCCESS\"}', 'admin', '2026-09-11 21:21:18');
INSERT INTO `iot_log` VALUES (60, 'LOG_1789132904048_78B0', 'COMMAND', 'DEV_OGY82W69', 'PK_6Z6UNCJ6', 'INFO', '规则联动控制指令下发成功: 规则 [RULE_484487 - Modbus测试设备重置], 动作 [Modbus设备重置默认值], 目标设备 [DEV_OGY82W69], 驱动 [modbus-tcp (192.168.2.12:502)], 服务 [Init_Modbus_Tcp], 物理下发点位: [temperture=0.88 (40001, FLOAT, CDAB), model=0 (40003, ENUM, CDAB), P3=0.66 (40004, DOUBLE, CDAB), P4=0 (40008, INT, CDAB)]', 'RULE_ENGINE', '2026-09-11 21:21:44');
INSERT INTO `iot_log` VALUES (61, 'LOG_1789133125749_E52F', 'COMMAND', 'DEV_OGY82W69', 'PK_6Z6UNCJ6', 'INFO', '规则联动控制指令下发成功: 规则 [RULE_484487 - Modbus测试设备重置], 动作 [Modbus设备重置默认值], 目标设备 [DEV_OGY82W69], 驱动 [modbus-tcp (192.168.2.12:502)], 服务 [Init_Modbus_Tcp], 物理下发点位: [temperture=0.88 (40001, FLOAT, CDAB), model=0 (40003, ENUM, CDAB), P3=0.66 (40004, DOUBLE, CDAB), P4=0 (40008, INT, CDAB)]', 'RULE_ENGINE', '2026-09-11 21:25:25');
INSERT INTO `iot_log` VALUES (62, 'LOG_1789133148814', 'COMMAND', 'DEV_002', 'pk_plc_s7', 'INFO', '控制指令下发: 设置点位 [targetPressure] (物理地址: DB10.DBD10, 数据类型: DOUBLE, 字节序: ABCD, 系数: 1.0) 目标值: 2.8', 'admin', '2026-09-11 21:25:48');
INSERT INTO `iot_log` VALUES (63, 'LOG_20260911_212548_BC68', 'OPERATION', 'DEV_002', NULL, 'INFO', '{\"module\":\"设备管理\",\"action\":\"下发设备点位写入控制\",\"params\":\"{\\\"deviceId\\\":\\\"DEV_002\\\",\\\"body\\\":{\\\"pointCode\\\":\\\"targetPressure\\\",\\\"value\\\":2.8}}\",\"ip\":\"127.0.0.1\",\"costMs\":6,\"status\":\"SUCCESS\"}', 'admin', '2026-09-11 21:25:48');
INSERT INTO `iot_log` VALUES (64, 'LOG_1789133349825_C509', 'COMMAND', 'DEV_OGY82W69', 'PK_6Z6UNCJ6', 'INFO', '规则联动控制指令下发成功: 规则 [RULE_484487 - Modbus测试设备重置], 动作 [Modbus设备重置默认值], 目标设备 [DEV_OGY82W69], 驱动 [modbus-tcp (192.168.2.12:502)], 服务 [Init_Modbus_Tcp], 物理下发点位: [temperture=0.88 (40001, FLOAT, CDAB), model=0 (40003, ENUM, CDAB), P3=0.66 (40004, DOUBLE, CDAB), P4=0 (40008, INT, CDAB)]', 'RULE_ENGINE', '2026-09-11 21:29:09');
INSERT INTO `iot_log` VALUES (65, 'LOG_1789133575361_B622', 'COMMAND', 'DEV_OGY82W69', 'PK_6Z6UNCJ6', 'INFO', '规则联动控制指令下发成功: 规则 [RULE_484487 - Modbus测试设备重置], 动作 [Modbus设备重置默认值], 目标设备 [DEV_OGY82W69], 驱动 [modbus-tcp (192.168.2.12:502)], 服务 [Init_Modbus_Tcp], 物理下发点位: [temperture=0.88 (40001, FLOAT, CDAB), model=0 (40003, ENUM, CDAB), P3=0.66 (40004, DOUBLE, CDAB), P4=0 (40008, INT, CDAB)]', 'RULE_ENGINE', '2026-09-11 21:32:55');
INSERT INTO `iot_log` VALUES (66, 'LOG_1789133630868', 'COMMAND', 'DEV_002', 'pk_plc_s7', 'INFO', '控制指令下发: 设置点位 [errorCode] (物理地址: DB10.DBD2, 数据类型: INT, 字节序: ABCD, 系数: 1.0) 目标值: 88', 'admin', '2026-09-11 21:33:50');
INSERT INTO `iot_log` VALUES (67, 'LOG_20260911_213350_5DA3', 'OPERATION', 'DEV_002', NULL, 'INFO', '{\"module\":\"设备管理\",\"action\":\"下发设备点位写入控制\",\"params\":\"{\\\"deviceId\\\":\\\"DEV_002\\\",\\\"body\\\":{\\\"pointCode\\\":\\\"errorCode\\\",\\\"value\\\":88,\\\"dataType\\\":\\\"INT\\\"}}\",\"ip\":\"127.0.0.1\",\"costMs\":3,\"status\":\"SUCCESS\"}', 'admin', '2026-09-11 21:33:50');
INSERT INTO `iot_log` VALUES (68, 'LOG_1789133663730', 'COMMAND', 'DEV_002', 'pk_plc_s7', 'INFO', '控制指令下发: 设置点位 [targetPressure] (物理地址: DB10.DBD10, 数据类型: DOUBLE, 字节序: ABCD, 系数: 1.0) 目标值: 8.8', 'admin', '2026-09-11 21:34:23');
INSERT INTO `iot_log` VALUES (69, 'LOG_20260911_213423_188B', 'OPERATION', 'DEV_002', NULL, 'INFO', '{\"module\":\"设备管理\",\"action\":\"下发设备点位写入控制\",\"params\":\"{\\\"deviceId\\\":\\\"DEV_002\\\",\\\"body\\\":{\\\"pointCode\\\":\\\"targetPressure\\\",\\\"value\\\":8.8,\\\"dataType\\\":\\\"DOUBLE\\\"}}\",\"ip\":\"127.0.0.1\",\"costMs\":3,\"status\":\"SUCCESS\"}', 'admin', '2026-09-11 21:34:23');
INSERT INTO `iot_log` VALUES (70, 'LOG_1789133799487_35AD', 'COMMAND', 'DEV_OGY82W69', 'PK_6Z6UNCJ6', 'INFO', '规则联动控制指令下发成功: 规则 [RULE_484487 - Modbus测试设备重置], 动作 [Modbus设备重置默认值], 目标设备 [DEV_OGY82W69], 驱动 [modbus-tcp (192.168.2.12:502)], 服务 [Init_Modbus_Tcp], 物理下发点位: [temperture=0.88 (40001, FLOAT, CDAB), model=0 (40003, ENUM, CDAB), P3=0.66 (40004, DOUBLE, CDAB), P4=0 (40008, INT, CDAB)]', 'RULE_ENGINE', '2026-09-11 21:36:39');
INSERT INTO `iot_log` VALUES (71, 'LOG_1789134022499_9A7E', 'COMMAND', 'DEV_OGY82W69', 'PK_6Z6UNCJ6', 'INFO', '规则联动控制指令下发成功: 规则 [RULE_484487 - Modbus测试设备重置], 动作 [Modbus设备重置默认值], 目标设备 [DEV_OGY82W69], 驱动 [modbus-tcp (192.168.2.12:502)], 服务 [Init_Modbus_Tcp], 物理下发点位: [temperture=0.88 (40001, FLOAT, CDAB), model=0 (40003, ENUM, CDAB), P3=0.66 (40004, DOUBLE, CDAB), P4=0 (40008, INT, CDAB)]', 'RULE_ENGINE', '2026-09-11 21:40:22');
INSERT INTO `iot_log` VALUES (72, 'LOG_1789134248095_DBDA', 'COMMAND', 'DEV_OGY82W69', 'PK_6Z6UNCJ6', 'INFO', '规则联动控制指令下发成功: 规则 [RULE_484487 - Modbus测试设备重置], 动作 [Modbus设备重置默认值], 目标设备 [DEV_OGY82W69], 驱动 [modbus-tcp (192.168.2.12:502)], 服务 [Init_Modbus_Tcp], 物理下发点位: [temperture=0.88 (40001, FLOAT, CDAB), model=0 (40003, ENUM, CDAB), P3=0.66 (40004, DOUBLE, CDAB), P4=0 (40008, INT, CDAB)]', 'RULE_ENGINE', '2026-09-11 21:44:08');
INSERT INTO `iot_log` VALUES (73, 'LOG_1789134476405_BD40', 'COMMAND', 'DEV_OGY82W69', 'PK_6Z6UNCJ6', 'INFO', '规则联动控制指令下发成功: 规则 [RULE_484487 - Modbus测试设备重置], 动作 [Modbus设备重置默认值], 目标设备 [DEV_OGY82W69], 驱动 [modbus-tcp (192.168.2.12:502)], 服务 [Init_Modbus_Tcp], 物理下发点位: [temperture=0.88 (40001, FLOAT, CDAB), model=0 (40003, ENUM, CDAB), P3=0.66 (40004, DOUBLE, CDAB), P4=0 (40008, INT, CDAB)]', 'RULE_ENGINE', '2026-09-11 21:47:56');
INSERT INTO `iot_log` VALUES (74, 'LOG_1789134699074_5F97', 'COMMAND', 'DEV_OGY82W69', 'PK_6Z6UNCJ6', 'INFO', '规则联动控制指令下发成功: 规则 [RULE_484487 - Modbus测试设备重置], 动作 [Modbus设备重置默认值], 目标设备 [DEV_OGY82W69], 驱动 [modbus-tcp (192.168.2.12:502)], 服务 [Init_Modbus_Tcp], 物理下发点位: [temperture=0.88 (40001, FLOAT, CDAB), model=0 (40003, ENUM, CDAB), P3=0.66 (40004, DOUBLE, CDAB), P4=0 (40008, INT, CDAB)]', 'RULE_ENGINE', '2026-09-11 21:51:39');
INSERT INTO `iot_log` VALUES (75, 'LOG_1789135503733_BE6C', 'COMMAND', 'DEV_OGY82W69', 'PK_6Z6UNCJ6', 'INFO', '规则联动控制指令下发成功: 规则 [RULE_484487 - Modbus测试设备重置], 动作 [Modbus设备重置默认值], 目标设备 [DEV_OGY82W69], 驱动 [modbus-tcp (192.168.2.12:502)], 服务 [Init_Modbus_Tcp], 物理下发点位: [temperture=0.88 (40001, FLOAT, CDAB), model=0 (40003, ENUM, CDAB), P3=0.66 (40004, DOUBLE, CDAB), P4=0 (40008, INT, CDAB)]', 'RULE_ENGINE', '2026-09-11 22:05:03');
INSERT INTO `iot_log` VALUES (76, 'LOG_20260914_204837_DBFA', 'OPERATION', NULL, NULL, 'INFO', '{\"module\":\"规则管理\",\"action\":\"修改告警联动规则\",\"params\":\"{\\\"ruleId\\\":\\\"RULE_484487\\\",\\\"rule\\\":{\\\"actions\\\":[{\\\"actionId\\\":\\\"act_245595\\\",\\\"actionName\\\":\\\"Modbus设备重置默认值\\\",\\\"actionType\\\":\\\"SERVICE\\\",\\\"alarmContent\\\":\\\"\\\",\\\"alarmLevel\\\":\\\"CRITICAL\\\",\\\"enabled\\\":true,\\\"logContent\\\":\\\"\\\",\\\"logLevel\\\":\\\"INFO\\\",\\\"serviceInputValues\\\":\\\"{\\\\\\\"temperture\\\\\\\":0.88,\\\\\\\"model\\\\\\\":0,\\\\\\\"P3\\\\\\\":0.66,\\\\\\\"P4\\\\\\\":0}\\\",\\\"targetDeviceId\\\":\\\"DEV_OGY82W69\\\",\\\"targetServiceCode\\\":\\\"Init_Modbus_Tcp\\\"},{\\\"actionId\\\":\\\"act_535491\\\",\\\"actionName\\\":\\\"Modbus设备重置告警\\\",\\\"actionType\\\":\\\"ALARM\\\",\\\"alarmContent\\\":\\\"告警\\\",\\\"alarmLevel\\\":\\\"CRITICAL\\\",\\\"enabled\\\":true,\\\"logContent\\\":\\\"\\\",\\\"logLevel\\\":\\\"INFO\\\",\\\"serviceInputValues\\\":\\\"{\\\\\\\"temperture\\\\\\\":0.88,\\\\\\\"model\\\\\\\":0,\\\\\\\"P3\\\\\\\":0.66,\\\\\\\"P4\\\\\\\":0}\\\",\\\"targetDeviceId\\\":\\\"DEV_OGY82W69\\\",\\\"targetServiceCode\\\":\\\"Init_Modbus_Tcp\\\"}],\\\"conditionMode\\\":\\\"SINGLE\\\",\\\"enabled\\\":true,\\\"id\\\":4,\\\"maxVal\\\":80.0,\\\"minVal\\\":20.0,\\\"modelKey\\\":\\\"PK_6Z6UNCJ6\\\",\\\"operator\\\":\\\">=\\\",\\\"ruleId...(截断)\",\"ip\":\"127.0.0.1\",\"costMs\":11,\"status\":\"SUCCESS\"}', 'admin', '2026-09-14 20:48:37');
INSERT INTO `iot_log` VALUES (77, 'LOG_1789390118499_B934', 'COMMAND', 'DEV_OGY82W69', 'PK_6Z6UNCJ6', 'INFO', '规则联动控制指令下发成功: 规则 [RULE_484487 - Modbus测试设备重置], 动作 [Modbus设备重置默认值], 目标设备 [DEV_OGY82W69], 驱动 [modbus-tcp (192.168.2.12:502)], 服务 [Init_Modbus_Tcp], 物理下发点位: [temperture=0.88 (40001, FLOAT, CDAB), model=0 (40003, ENUM, CDAB), P3=0.66 (40004, DOUBLE, CDAB), P4=0 (40008, INT, CDAB)]', 'RULE_ENGINE', '2026-09-14 20:48:38');
INSERT INTO `iot_log` VALUES (78, 'LOG_1789390235744', 'COMMAND', 'DEV_002', 'pk_plc_s7', 'INFO', '控制指令下发: 设置点位 [plcRunStatus] (物理地址: DB10.DBX0.0, 数据类型: BOOL, 字节序: ABCD, 系数: 1.0) 目标值: true', 'admin', '2026-09-14 20:50:35');
INSERT INTO `iot_log` VALUES (79, 'LOG_20260914_205035_79B5', 'OPERATION', 'DEV_002', NULL, 'INFO', '{\"module\":\"设备管理\",\"action\":\"下发设备点位写入控制\",\"params\":\"{\\\"deviceId\\\":\\\"DEV_002\\\",\\\"body\\\":{\\\"pointCode\\\":\\\"plcRunStatus\\\",\\\"value\\\":true,\\\"dataType\\\":\\\"BOOL\\\"}}\",\"ip\":\"127.0.0.1\",\"costMs\":5,\"status\":\"SUCCESS\"}', 'admin', '2026-09-14 20:50:35');
INSERT INTO `iot_log` VALUES (80, 'LOG_1789390248339', 'COMMAND', 'DEV_002', 'pk_plc_s7', 'INFO', '控制指令下发: 设置点位 [errorCode] (物理地址: DB10.DBD2, 数据类型: INT, 字节序: ABCD, 系数: 1.0) 目标值: 99', 'admin', '2026-09-14 20:50:48');
INSERT INTO `iot_log` VALUES (81, 'LOG_20260914_205048_2C44', 'OPERATION', 'DEV_002', NULL, 'INFO', '{\"module\":\"设备管理\",\"action\":\"下发设备点位写入控制\",\"params\":\"{\\\"deviceId\\\":\\\"DEV_002\\\",\\\"body\\\":{\\\"pointCode\\\":\\\"errorCode\\\",\\\"value\\\":99,\\\"dataType\\\":\\\"INT\\\"}}\",\"ip\":\"127.0.0.1\",\"costMs\":6,\"status\":\"SUCCESS\"}', 'admin', '2026-09-14 20:50:48');
INSERT INTO `iot_log` VALUES (82, 'LOG_1789390254771', 'COMMAND', 'DEV_002', 'pk_plc_s7', 'INFO', '控制指令下发: 设置点位 [remoteControlCmd] (物理地址: DB10.DBX0.1, 数据类型: BOOL, 字节序: ABCD, 系数: 1.0) 目标值: true', 'admin', '2026-09-14 20:50:54');
INSERT INTO `iot_log` VALUES (83, 'LOG_20260914_205054_F097', 'OPERATION', 'DEV_002', NULL, 'INFO', '{\"module\":\"设备管理\",\"action\":\"下发设备点位写入控制\",\"params\":\"{\\\"deviceId\\\":\\\"DEV_002\\\",\\\"body\\\":{\\\"pointCode\\\":\\\"remoteControlCmd\\\",\\\"value\\\":true,\\\"dataType\\\":\\\"BOOL\\\"}}\",\"ip\":\"127.0.0.1\",\"costMs\":11,\"status\":\"SUCCESS\"}', 'admin', '2026-09-14 20:50:54');
INSERT INTO `iot_log` VALUES (84, 'LOG_1789390267889', 'COMMAND', 'DEV_002', 'pk_plc_s7', 'INFO', '控制指令下发: 设置点位 [targetPressure] (物理地址: DB10.DBD10, 数据类型: DOUBLE, 字节序: ABCD, 系数: 1.0) 目标值: 8.88', 'admin', '2026-09-14 20:51:07');
INSERT INTO `iot_log` VALUES (85, 'LOG_20260914_205107_9042', 'OPERATION', 'DEV_002', NULL, 'INFO', '{\"module\":\"设备管理\",\"action\":\"下发设备点位写入控制\",\"params\":\"{\\\"deviceId\\\":\\\"DEV_002\\\",\\\"body\\\":{\\\"pointCode\\\":\\\"targetPressure\\\",\\\"value\\\":8.88,\\\"dataType\\\":\\\"DOUBLE\\\"}}\",\"ip\":\"127.0.0.1\",\"costMs\":5,\"status\":\"SUCCESS\"}', 'admin', '2026-09-14 20:51:07');
INSERT INTO `iot_log` VALUES (86, 'LOG_1789390334312_4277', 'COMMAND', 'DEV_OGY82W69', 'PK_6Z6UNCJ6', 'INFO', '规则联动控制指令下发成功: 规则 [RULE_484487 - Modbus测试设备重置], 动作 [Modbus设备重置默认值], 目标设备 [DEV_OGY82W69], 驱动 [modbus-tcp (192.168.2.12:502)], 服务 [Init_Modbus_Tcp], 物理下发点位: [temperture=0.88 (40001, FLOAT, CDAB), model=0 (40003, ENUM, CDAB), P3=0.66 (40004, DOUBLE, CDAB), P4=0 (40008, INT, CDAB)]', 'RULE_ENGINE', '2026-09-14 20:52:14');
INSERT INTO `iot_log` VALUES (87, 'LOG_1789390551050_EF93', 'COMMAND', 'DEV_OGY82W69', 'PK_6Z6UNCJ6', 'INFO', '规则联动控制指令下发成功: 规则 [RULE_484487 - Modbus测试设备重置], 动作 [Modbus设备重置默认值], 目标设备 [DEV_OGY82W69], 驱动 [modbus-tcp (192.168.2.12:502)], 服务 [Init_Modbus_Tcp], 物理下发点位: [temperture=0.88 (40001, FLOAT, CDAB), model=0 (40003, ENUM, CDAB), P3=0.66 (40004, DOUBLE, CDAB), P4=0 (40008, INT, CDAB)]', 'RULE_ENGINE', '2026-09-14 20:55:51');
INSERT INTO `iot_log` VALUES (88, 'LOG_1789390771070_C77B', 'COMMAND', 'DEV_OGY82W69', 'PK_6Z6UNCJ6', 'INFO', '规则联动控制指令下发成功: 规则 [RULE_484487 - Modbus测试设备重置], 动作 [Modbus设备重置默认值], 目标设备 [DEV_OGY82W69], 驱动 [modbus-tcp (192.168.2.12:502)], 服务 [Init_Modbus_Tcp], 物理下发点位: [temperture=0.88 (40001, FLOAT, CDAB), model=0 (40003, ENUM, CDAB), P3=0.66 (40004, DOUBLE, CDAB), P4=0 (40008, INT, CDAB)]', 'RULE_ENGINE', '2026-09-14 20:59:31');
INSERT INTO `iot_log` VALUES (89, 'LOG_1789390988289_7EC9', 'COMMAND', 'DEV_OGY82W69', 'PK_6Z6UNCJ6', 'INFO', '规则联动控制指令下发成功: 规则 [RULE_484487 - Modbus测试设备重置], 动作 [Modbus设备重置默认值], 目标设备 [DEV_OGY82W69], 驱动 [modbus-tcp (192.168.2.12:502)], 服务 [Init_Modbus_Tcp], 物理下发点位: [temperture=0.88 (40001, FLOAT, CDAB), model=0 (40003, ENUM, CDAB), P3=0.66 (40004, DOUBLE, CDAB), P4=0 (40008, INT, CDAB)]', 'RULE_ENGINE', '2026-09-14 21:03:08');
INSERT INTO `iot_log` VALUES (90, 'LOG_1789391209175_1D19', 'COMMAND', 'DEV_OGY82W69', 'PK_6Z6UNCJ6', 'INFO', '规则联动控制指令下发成功: 规则 [RULE_484487 - Modbus测试设备重置], 动作 [Modbus设备重置默认值], 目标设备 [DEV_OGY82W69], 驱动 [modbus-tcp (192.168.2.12:502)], 服务 [Init_Modbus_Tcp], 物理下发点位: [temperture=0.88 (40001, FLOAT, CDAB), model=0 (40003, ENUM, CDAB), P3=0.66 (40004, DOUBLE, CDAB), P4=0 (40008, INT, CDAB)]', 'RULE_ENGINE', '2026-09-14 21:06:49');
INSERT INTO `iot_log` VALUES (91, 'LOG_1789391429622_4139', 'COMMAND', 'DEV_OGY82W69', 'PK_6Z6UNCJ6', 'INFO', '规则联动控制指令下发成功: 规则 [RULE_484487 - Modbus测试设备重置], 动作 [Modbus设备重置默认值], 目标设备 [DEV_OGY82W69], 驱动 [modbus-tcp (192.168.2.12:502)], 服务 [Init_Modbus_Tcp], 物理下发点位: [temperture=0.88 (40001, FLOAT, CDAB), model=0 (40003, ENUM, CDAB), P3=0.66 (40004, DOUBLE, CDAB), P4=0 (40008, INT, CDAB)]', 'RULE_ENGINE', '2026-09-14 21:10:29');
INSERT INTO `iot_log` VALUES (92, 'LOG_1789391652641_97DB', 'COMMAND', 'DEV_OGY82W69', 'PK_6Z6UNCJ6', 'INFO', '规则联动控制指令下发成功: 规则 [RULE_484487 - Modbus测试设备重置], 动作 [Modbus设备重置默认值], 目标设备 [DEV_OGY82W69], 驱动 [modbus-tcp (192.168.2.12:502)], 服务 [Init_Modbus_Tcp], 物理下发点位: [temperture=0.88 (40001, FLOAT, CDAB), model=0 (40003, ENUM, CDAB), P3=0.66 (40004, DOUBLE, CDAB), P4=0 (40008, INT, CDAB)]', 'RULE_ENGINE', '2026-09-14 21:14:12');
INSERT INTO `iot_log` VALUES (93, 'LOG_1789391872638_8F1D', 'COMMAND', 'DEV_OGY82W69', 'PK_6Z6UNCJ6', 'INFO', '规则联动控制指令下发成功: 规则 [RULE_484487 - Modbus测试设备重置], 动作 [Modbus设备重置默认值], 目标设备 [DEV_OGY82W69], 驱动 [modbus-tcp (192.168.2.12:502)], 服务 [Init_Modbus_Tcp], 物理下发点位: [temperture=0.88 (40001, FLOAT, CDAB), model=0 (40003, ENUM, CDAB), P3=0.66 (40004, DOUBLE, CDAB), P4=0 (40008, INT, CDAB)]', 'RULE_ENGINE', '2026-09-14 21:17:52');
INSERT INTO `iot_log` VALUES (94, 'LOG_20260914_212051_BBA0', 'OPERATION', NULL, NULL, 'INFO', '{\"module\":\"用户认证\",\"action\":\"用户登录\",\"params\":\"{\\\"request\\\":{\\\"password\\\":\\\"admin123\\\",\\\"rememberMe\\\":true,\\\"username\\\":\\\"admin\\\"}}\",\"ip\":\"127.0.0.1\",\"costMs\":18,\"status\":\"SUCCESS\"}', 'admin', '2026-09-14 21:20:51');
INSERT INTO `iot_log` VALUES (95, 'LOG_20260914_212129_4289', 'OPERATION', NULL, NULL, 'INFO', '{\"module\":\"用户认证\",\"action\":\"注销登录\",\"params\":\"\",\"ip\":\"127.0.0.1\",\"costMs\":0,\"status\":\"SUCCESS\"}', 'admin', '2026-09-14 21:21:29');
INSERT INTO `iot_log` VALUES (96, 'LOG_20260914_212131_904A', 'OPERATION', NULL, NULL, 'INFO', '{\"module\":\"用户认证\",\"action\":\"用户登录\",\"params\":\"{\\\"request\\\":{\\\"password\\\":\\\"admin123\\\",\\\"rememberMe\\\":true,\\\"username\\\":\\\"admin\\\"}}\",\"ip\":\"127.0.0.1\",\"costMs\":6,\"status\":\"SUCCESS\"}', 'admin', '2026-09-14 21:21:31');
INSERT INTO `iot_log` VALUES (97, 'LOG_1789392093400_EE7D', 'COMMAND', 'DEV_OGY82W69', 'PK_6Z6UNCJ6', 'INFO', '规则联动控制指令下发成功: 规则 [RULE_484487 - Modbus测试设备重置], 动作 [Modbus设备重置默认值], 目标设备 [DEV_OGY82W69], 驱动 [modbus-tcp (192.168.2.12:502)], 服务 [Init_Modbus_Tcp], 物理下发点位: [temperture=0.88 (40001, FLOAT, CDAB), model=0 (40003, ENUM, CDAB), P3=0.66 (40004, DOUBLE, CDAB), P4=0 (40008, INT, CDAB)]', 'RULE_ENGINE', '2026-09-14 21:21:33');
INSERT INTO `iot_log` VALUES (98, 'LOG_1789392315893_0342', 'COMMAND', 'DEV_OGY82W69', 'PK_6Z6UNCJ6', 'INFO', '规则联动控制指令下发成功: 规则 [RULE_484487 - Modbus测试设备重置], 动作 [Modbus设备重置默认值], 目标设备 [DEV_OGY82W69], 驱动 [modbus-tcp (192.168.2.12:502)], 服务 [Init_Modbus_Tcp], 物理下发点位: [temperture=0.88 (40001, FLOAT, CDAB), model=0 (40003, ENUM, CDAB), P3=0.66 (40004, DOUBLE, CDAB), P4=0 (40008, INT, CDAB)]', 'RULE_ENGINE', '2026-09-14 21:25:15');
INSERT INTO `iot_log` VALUES (99, 'LOG_1789392537908_F93F', 'COMMAND', 'DEV_OGY82W69', 'PK_6Z6UNCJ6', 'INFO', '规则联动控制指令下发成功: 规则 [RULE_484487 - Modbus测试设备重置], 动作 [Modbus设备重置默认值], 目标设备 [DEV_OGY82W69], 驱动 [modbus-tcp (192.168.2.12:502)], 服务 [Init_Modbus_Tcp], 物理下发点位: [temperture=0.88 (40001, FLOAT, CDAB), model=0 (40003, ENUM, CDAB), P3=0.66 (40004, DOUBLE, CDAB), P4=0 (40008, INT, CDAB)]', 'RULE_ENGINE', '2026-09-14 21:28:57');
INSERT INTO `iot_log` VALUES (100, 'LOG_1789392758625_A5DF', 'COMMAND', 'DEV_OGY82W69', 'PK_6Z6UNCJ6', 'INFO', '规则联动控制指令下发成功: 规则 [RULE_484487 - Modbus测试设备重置], 动作 [Modbus设备重置默认值], 目标设备 [DEV_OGY82W69], 驱动 [modbus-tcp (192.168.2.12:502)], 服务 [Init_Modbus_Tcp], 物理下发点位: [temperture=0.88 (40001, FLOAT, CDAB), model=0 (40003, ENUM, CDAB), P3=0.66 (40004, DOUBLE, CDAB), P4=0 (40008, INT, CDAB)]', 'RULE_ENGINE', '2026-09-14 21:32:38');
INSERT INTO `iot_log` VALUES (101, 'LOG_1789392979101_5C42', 'COMMAND', 'DEV_OGY82W69', 'PK_6Z6UNCJ6', 'INFO', '规则联动控制指令下发成功: 规则 [RULE_484487 - Modbus测试设备重置], 动作 [Modbus设备重置默认值], 目标设备 [DEV_OGY82W69], 驱动 [modbus-tcp (192.168.2.12:502)], 服务 [Init_Modbus_Tcp], 物理下发点位: [temperture=0.88 (40001, FLOAT, CDAB), model=0 (40003, ENUM, CDAB), P3=0.66 (40004, DOUBLE, CDAB), P4=0 (40008, INT, CDAB)]', 'RULE_ENGINE', '2026-09-14 21:36:19');
INSERT INTO `iot_log` VALUES (102, 'LOG_1789393198833_0A28', 'COMMAND', 'DEV_OGY82W69', 'PK_6Z6UNCJ6', 'INFO', '规则联动控制指令下发成功: 规则 [RULE_484487 - Modbus测试设备重置], 动作 [Modbus设备重置默认值], 目标设备 [DEV_OGY82W69], 驱动 [modbus-tcp (192.168.2.12:502)], 服务 [Init_Modbus_Tcp], 物理下发点位: [temperture=0.88 (40001, FLOAT, CDAB), model=0 (40003, ENUM, CDAB), P3=0.66 (40004, DOUBLE, CDAB), P4=0 (40008, INT, CDAB)]', 'RULE_ENGINE', '2026-09-14 21:39:58');
INSERT INTO `iot_log` VALUES (103, 'LOG_1789393418584_6E28', 'COMMAND', 'DEV_OGY82W69', 'PK_6Z6UNCJ6', 'INFO', '规则联动控制指令下发成功: 规则 [RULE_484487 - Modbus测试设备重置], 动作 [Modbus设备重置默认值], 目标设备 [DEV_OGY82W69], 驱动 [modbus-tcp (192.168.2.12:502)], 服务 [Init_Modbus_Tcp], 物理下发点位: [temperture=0.88 (40001, FLOAT, CDAB), model=0 (40003, ENUM, CDAB), P3=0.66 (40004, DOUBLE, CDAB), P4=0 (40008, INT, CDAB)]', 'RULE_ENGINE', '2026-09-14 21:43:38');
INSERT INTO `iot_log` VALUES (104, 'LOG_1789393639173_AAE2', 'COMMAND', 'DEV_OGY82W69', 'PK_6Z6UNCJ6', 'INFO', '规则联动控制指令下发成功: 规则 [RULE_484487 - Modbus测试设备重置], 动作 [Modbus设备重置默认值], 目标设备 [DEV_OGY82W69], 驱动 [modbus-tcp (192.168.2.12:502)], 服务 [Init_Modbus_Tcp], 物理下发点位: [temperture=0.88 (40001, FLOAT, CDAB), model=0 (40003, ENUM, CDAB), P3=0.66 (40004, DOUBLE, CDAB), P4=0 (40008, INT, CDAB)]', 'RULE_ENGINE', '2026-09-14 21:47:19');
INSERT INTO `iot_log` VALUES (105, 'LOG_1789393859204_0496', 'COMMAND', 'DEV_OGY82W69', 'PK_6Z6UNCJ6', 'INFO', '规则联动控制指令下发成功: 规则 [RULE_484487 - Modbus测试设备重置], 动作 [Modbus设备重置默认值], 目标设备 [DEV_OGY82W69], 驱动 [modbus-tcp (192.168.2.12:502)], 服务 [Init_Modbus_Tcp], 物理下发点位: [temperture=0.88 (40001, FLOAT, CDAB), model=0 (40003, ENUM, CDAB), P3=0.66 (40004, DOUBLE, CDAB), P4=0 (40008, INT, CDAB)]', 'RULE_ENGINE', '2026-09-14 21:50:59');
INSERT INTO `iot_log` VALUES (106, 'LOG_1789394082017_7F20', 'COMMAND', 'DEV_OGY82W69', 'PK_6Z6UNCJ6', 'INFO', '规则联动控制指令下发成功: 规则 [RULE_484487 - Modbus测试设备重置], 动作 [Modbus设备重置默认值], 目标设备 [DEV_OGY82W69], 驱动 [modbus-tcp (192.168.2.12:502)], 服务 [Init_Modbus_Tcp], 物理下发点位: [temperture=0.88 (40001, FLOAT, CDAB), model=0 (40003, ENUM, CDAB), P3=0.66 (40004, DOUBLE, CDAB), P4=0 (40008, INT, CDAB)]', 'RULE_ENGINE', '2026-09-14 21:54:42');
INSERT INTO `iot_log` VALUES (107, 'LOG_20260914_215710_7C71', 'OPERATION', NULL, NULL, 'INFO', '{\"module\":\"用户认证\",\"action\":\"注销登录\",\"params\":\"\",\"ip\":\"127.0.0.1\",\"costMs\":9,\"status\":\"SUCCESS\"}', 'admin', '2026-09-14 21:57:10');
INSERT INTO `iot_log` VALUES (108, 'LOG_1789394303547_0DBE', 'COMMAND', 'DEV_OGY82W69', 'PK_6Z6UNCJ6', 'INFO', '规则联动控制指令下发成功: 规则 [RULE_484487 - Modbus测试设备重置], 动作 [Modbus设备重置默认值], 目标设备 [DEV_OGY82W69], 驱动 [modbus-tcp (192.168.2.12:502)], 服务 [Init_Modbus_Tcp], 物理下发点位: [temperture=0.88 (40001, FLOAT, CDAB), model=0 (40003, ENUM, CDAB), P3=0.66 (40004, DOUBLE, CDAB), P4=0 (40008, INT, CDAB)]', 'RULE_ENGINE', '2026-09-14 21:58:23');
INSERT INTO `iot_log` VALUES (109, 'LOG_20260921_193022_D769', 'OPERATION', NULL, NULL, 'INFO', '{\"module\":\"用户认证\",\"action\":\"用户登录\",\"params\":\"{\\\"request\\\":{\\\"password\\\":\\\"admin123\\\",\\\"rememberMe\\\":true,\\\"username\\\":\\\"admin\\\"}}\",\"ip\":\"127.0.0.1\",\"costMs\":18,\"status\":\"SUCCESS\"}', 'admin', '2026-09-21 19:30:22');
INSERT INTO `iot_log` VALUES (110, 'LOG_20260921_202013_3961', 'OPERATION', NULL, NULL, 'INFO', '{\"module\":\"用户认证\",\"action\":\"注销登录\",\"params\":\"\",\"ip\":\"127.0.0.1\",\"costMs\":3,\"status\":\"SUCCESS\"}', 'admin', '2026-09-21 20:20:13');
INSERT INTO `iot_log` VALUES (111, 'LOG_20260921_202014_915F', 'OPERATION', NULL, NULL, 'INFO', '{\"module\":\"用户认证\",\"action\":\"用户登录\",\"params\":\"{\\\"request\\\":{\\\"password\\\":\\\"admin123\\\",\\\"rememberMe\\\":true,\\\"username\\\":\\\"admin\\\"}}\",\"ip\":\"127.0.0.1\",\"costMs\":11,\"status\":\"SUCCESS\"}', 'admin', '2026-09-21 20:20:14');
INSERT INTO `iot_log` VALUES (112, 'LOG_20260921_202606_F730', 'OPERATION', NULL, NULL, 'INFO', '{\"module\":\"用户认证\",\"action\":\"用户登录\",\"params\":\"{\\\"request\\\":{\\\"password\\\":\\\"admin123\\\",\\\"username\\\":\\\"admin\\\"}}\",\"ip\":\"127.0.0.1\",\"costMs\":8,\"status\":\"SUCCESS\"}', 'admin', '2026-09-21 20:26:06');
INSERT INTO `iot_log` VALUES (113, 'LOG_20261009_090511_6D9D', 'OPERATION', NULL, NULL, 'ERROR', '{\"module\":\"用户认证\",\"action\":\"用户登录\",\"params\":\"{\\\"request\\\":{\\\"password\\\":\\\"unihhunihh\\\",\\\"rememberMe\\\":false,\\\"username\\\":\\\"admin\\\"}}\",\"ip\":\"127.0.0.1\",\"costMs\":17,\"status\":\"FAILED\",\"error\":\"用户名或密码错误\"}', 'admin', '2026-10-09 09:05:11');
INSERT INTO `iot_log` VALUES (114, 'LOG_20261009_090518_F902', 'OPERATION', NULL, NULL, 'ERROR', '{\"module\":\"用户认证\",\"action\":\"用户登录\",\"params\":\"{\\\"request\\\":{\\\"password\\\":\\\"123456\\\",\\\"rememberMe\\\":false,\\\"username\\\":\\\"admin\\\"}}\",\"ip\":\"127.0.0.1\",\"costMs\":1,\"status\":\"FAILED\",\"error\":\"用户名或密码错误\"}', 'admin', '2026-10-09 09:05:18');
INSERT INTO `iot_log` VALUES (115, 'LOG_20261009_090530_F345', 'OPERATION', NULL, NULL, 'ERROR', '{\"module\":\"用户认证\",\"action\":\"用户登录\",\"params\":\"{\\\"request\\\":{\\\"password\\\":\\\"unihhunihh\\\",\\\"rememberMe\\\":false,\\\"username\\\":\\\"admin\\\"}}\",\"ip\":\"127.0.0.1\",\"costMs\":1,\"status\":\"FAILED\",\"error\":\"用户名或密码错误\"}', 'admin', '2026-10-09 09:05:30');
INSERT INTO `iot_log` VALUES (116, 'LOG_20261009_090541_1D1A', 'OPERATION', NULL, NULL, 'ERROR', '{\"module\":\"用户认证\",\"action\":\"用户登录\",\"params\":\"{\\\"request\\\":{\\\"password\\\":\\\"unihhiot\\\",\\\"rememberMe\\\":false,\\\"username\\\":\\\"admin\\\"}}\",\"ip\":\"127.0.0.1\",\"costMs\":1,\"status\":\"FAILED\",\"error\":\"用户名或密码错误\"}', 'admin', '2026-10-09 09:05:41');
INSERT INTO `iot_log` VALUES (117, 'LOG_20261009_090548_7A46', 'OPERATION', NULL, NULL, 'INFO', '{\"module\":\"用户认证\",\"action\":\"用户登录\",\"params\":\"{\\\"request\\\":{\\\"password\\\":\\\"admin123\\\",\\\"rememberMe\\\":false,\\\"username\\\":\\\"admin\\\"}}\",\"ip\":\"127.0.0.1\",\"costMs\":102,\"status\":\"SUCCESS\"}', 'admin', '2026-10-09 09:05:48');
INSERT INTO `iot_log` VALUES (118, 'LOG_20261009_091308_E0AB', 'OPERATION', NULL, NULL, 'INFO', '{\"module\":\"用户认证\",\"action\":\"注销登录\",\"params\":\"\",\"ip\":\"127.0.0.1\",\"costMs\":1,\"status\":\"SUCCESS\"}', 'admin', '2026-10-09 09:13:08');
INSERT INTO `iot_log` VALUES (119, 'LOG_20261009_091322_B9A0', 'OPERATION', NULL, NULL, 'INFO', '{\"module\":\"用户认证\",\"action\":\"用户登录\",\"params\":\"{\\\"request\\\":{\\\"password\\\":\\\"admin123\\\",\\\"rememberMe\\\":false,\\\"username\\\":\\\"admin\\\"}}\",\"ip\":\"127.0.0.1\",\"costMs\":95,\"status\":\"SUCCESS\"}', 'admin', '2026-10-09 09:13:22');
INSERT INTO `iot_log` VALUES (120, 'LOG_20261009_093802_4A8E', 'OPERATION', NULL, NULL, 'INFO', '{\"module\":\"驱动管理\",\"action\":\"通道心跳自检\",\"params\":\"{\\\"driverCode\\\":\\\"modbus-tcp\\\"}\",\"ip\":\"127.0.0.1\",\"costMs\":2022,\"status\":\"SUCCESS\"}', 'admin', '2026-10-09 09:38:02');
INSERT INTO `iot_log` VALUES (121, 'LOG_20261009_093826_05DF', 'OPERATION', NULL, NULL, 'INFO', '{\"module\":\"驱动管理\",\"action\":\"通道心跳自检\",\"params\":\"{\\\"driverCode\\\":\\\"modbus-tcp\\\"}\",\"ip\":\"127.0.0.1\",\"costMs\":166,\"status\":\"SUCCESS\"}', 'admin', '2026-10-09 09:38:26');
INSERT INTO `iot_log` VALUES (122, 'LOG_20261009_095604_931C', 'OPERATION', NULL, NULL, 'INFO', '{\"module\":\"用户认证\",\"action\":\"注销登录\",\"params\":\"\",\"ip\":\"127.0.0.1\",\"costMs\":17,\"status\":\"SUCCESS\"}', 'admin', '2026-10-09 09:56:04');
INSERT INTO `iot_log` VALUES (123, 'LOG_20261009_095723_FEE3', 'OPERATION', NULL, NULL, 'INFO', '{\"module\":\"用户认证\",\"action\":\"用户登录\",\"params\":\"{\\\"request\\\":{\\\"password\\\":\\\"admin123\\\",\\\"rememberMe\\\":false,\\\"username\\\":\\\"admin\\\"}}\",\"ip\":\"127.0.0.1\",\"costMs\":30,\"status\":\"SUCCESS\"}', 'admin', '2026-10-09 09:57:23');
INSERT INTO `iot_log` VALUES (124, 'LOG_20261009_095753_CDC6', 'OPERATION', NULL, NULL, 'INFO', '{\"module\":\"用户管理\",\"action\":\"重置用户密码\",\"params\":\"{\\\"id\\\":1,\\\"body\\\":{\\\"password\\\":\\\"unihhiot\\\"}}\",\"ip\":\"127.0.0.1\",\"costMs\":9,\"status\":\"SUCCESS\"}', 'admin', '2026-10-09 09:57:53');
INSERT INTO `iot_log` VALUES (125, 'LOG_20261009_095757_A64A', 'OPERATION', NULL, NULL, 'INFO', '{\"module\":\"用户认证\",\"action\":\"注销登录\",\"params\":\"\",\"ip\":\"127.0.0.1\",\"costMs\":0,\"status\":\"SUCCESS\"}', 'admin', '2026-10-09 09:57:57');
INSERT INTO `iot_log` VALUES (126, 'LOG_20261009_095802_D850', 'OPERATION', NULL, NULL, 'INFO', '{\"module\":\"用户认证\",\"action\":\"用户登录\",\"params\":\"{\\\"request\\\":{\\\"password\\\":\\\"unihhiot\\\",\\\"rememberMe\\\":false,\\\"username\\\":\\\"admin\\\"}}\",\"ip\":\"127.0.0.1\",\"costMs\":27,\"status\":\"SUCCESS\"}', 'admin', '2026-10-09 09:58:02');

-- ----------------------------
-- Table structure for iot_rule
-- ----------------------------
DROP TABLE IF EXISTS `iot_rule`;
CREATE TABLE `iot_rule`  (
  `id` bigint(20) NOT NULL AUTO_INCREMENT COMMENT '自增主键ID',
  `rule_id` varchar(64) CHARACTER SET utf8mb4 COLLATE utf8mb4_0900_ai_ci NOT NULL COMMENT '规则唯一ID (如 RULE-101)',
  `rule_name` varchar(128) CHARACTER SET utf8mb4 COLLATE utf8mb4_0900_ai_ci NOT NULL COMMENT '规则名称',
  `model_key` varchar(64) CHARACTER SET utf8mb4 COLLATE utf8mb4_0900_ai_ci NULL DEFAULT NULL COMMENT '关联物模型Key',
  `source_device_id` varchar(64) CHARACTER SET utf8mb4 COLLATE utf8mb4_0900_ai_ci NOT NULL COMMENT '触发源设备ID',
  `source_point` varchar(64) CHARACTER SET utf8mb4 COLLATE utf8mb4_0900_ai_ci NOT NULL COMMENT '触发源点位Code',
  `condition_mode` varchar(32) CHARACTER SET utf8mb4 COLLATE utf8mb4_0900_ai_ci NOT NULL DEFAULT 'SINGLE' COMMENT '条件类型 (SINGLE:单条件阈值, RANGE_OUTSIDE:区间越界)',
  `operator` varchar(16) CHARACTER SET utf8mb4 COLLATE utf8mb4_0900_ai_ci NULL DEFAULT '>' COMMENT '判断操作符 (>, >=, <, <=, ==, !=)',
  `threshold_value` double NULL DEFAULT 0 COMMENT '比较阈值',
  `min_val` double NULL DEFAULT 0 COMMENT '区间最小值',
  `max_val` double NULL DEFAULT 0 COMMENT '区间最大值',
  `enabled` tinyint(4) NOT NULL DEFAULT 1 COMMENT '规则开关 (1:启用, 0:禁用)',
  `description` varchar(512) CHARACTER SET utf8mb4 COLLATE utf8mb4_0900_ai_ci NULL DEFAULT NULL COMMENT '规则描述',
  `create_time` datetime(0) NOT NULL DEFAULT CURRENT_TIMESTAMP(0) COMMENT '创建时间',
  `update_time` datetime(0) NOT NULL DEFAULT CURRENT_TIMESTAMP(0) ON UPDATE CURRENT_TIMESTAMP(0) COMMENT '更新时间',
  PRIMARY KEY (`id`) USING BTREE,
  UNIQUE INDEX `rule_id`(`rule_id`) USING BTREE
) ENGINE = InnoDB AUTO_INCREMENT = 5 CHARACTER SET = utf8mb4 COLLATE = utf8mb4_0900_ai_ci COMMENT = '规则引擎定义表' ROW_FORMAT = Dynamic;

-- ----------------------------
-- Records of iot_rule
-- ----------------------------
INSERT INTO `iot_rule` VALUES (1, 'RULE_101', '1号主轴承过热防护与自动降速规则', 'pk_demo', 'DEV_001', 'temperature', 'SINGLE', '>', 75, 0, 0, 1, '当1号轴承温度超过75℃时触发自动开启风扇及急停预警', '2026-09-09 20:04:04', '2026-09-09 20:04:04');
INSERT INTO `iot_rule` VALUES (2, 'RULE_102', '2号储罐压力上限反控开阀规则', 'pk_plc_s7', 'DEV_002', 'pressure', 'RANGE_OUTSIDE', '>', 0, 10, 100, 1, '储罐压力不在10~100kPa安全范围自动报警', '2026-09-09 20:04:04', '2026-09-09 20:04:04');
INSERT INTO `iot_rule` VALUES (3, 'RULE_103', '车间环境湿度过高排风规则', 'pk_demo', 'DEV_001', 'humidity', 'SINGLE', '>', 85, 0, 0, 0, '湿度超过85%RH时自动发送预警提示', '2026-09-09 20:04:04', '2026-09-09 20:04:04');
INSERT INTO `iot_rule` VALUES (4, 'RULE_484487', 'Modbus测试设备重置', 'PK_6Z6UNCJ6', 'DEV_OGY82W69', 'P4', 'SINGLE', '>=', 200, 20, 80, 1, NULL, '2026-09-10 21:05:36', '2026-09-14 20:48:37');

-- ----------------------------
-- Table structure for iot_rule_action
-- ----------------------------
DROP TABLE IF EXISTS `iot_rule_action`;
CREATE TABLE `iot_rule_action`  (
  `id` bigint(20) NOT NULL AUTO_INCREMENT COMMENT '自增主键ID',
  `rule_id` varchar(64) CHARACTER SET utf8mb4 COLLATE utf8mb4_0900_ai_ci NOT NULL COMMENT '所属规则ID',
  `action_id` varchar(64) CHARACTER SET utf8mb4 COLLATE utf8mb4_0900_ai_ci NOT NULL COMMENT '动作唯一ID',
  `action_type` varchar(32) CHARACTER SET utf8mb4 COLLATE utf8mb4_0900_ai_ci NOT NULL DEFAULT 'SERVICE' COMMENT '动作类型 (SERVICE, ALARM, LOG)',
  `action_name` varchar(128) CHARACTER SET utf8mb4 COLLATE utf8mb4_0900_ai_ci NOT NULL COMMENT '动作名称',
  `target_device_id` varchar(64) CHARACTER SET utf8mb4 COLLATE utf8mb4_0900_ai_ci NULL DEFAULT NULL COMMENT '目标反控设备ID',
  `target_service_code` varchar(64) CHARACTER SET utf8mb4 COLLATE utf8mb4_0900_ai_ci NULL DEFAULT NULL COMMENT '目标服务指令Code',
  `service_input_values` text CHARACTER SET utf8mb4 COLLATE utf8mb4_0900_ai_ci NULL COMMENT '物模型入参JSON',
  `alarm_level` varchar(32) CHARACTER SET utf8mb4 COLLATE utf8mb4_0900_ai_ci NULL DEFAULT NULL COMMENT '告警级别',
  `alarm_content` varchar(512) CHARACTER SET utf8mb4 COLLATE utf8mb4_0900_ai_ci NULL DEFAULT NULL COMMENT '告警文本',
  `log_level` varchar(32) CHARACTER SET utf8mb4 COLLATE utf8mb4_0900_ai_ci NULL DEFAULT NULL COMMENT '日志级别',
  `log_content` varchar(512) CHARACTER SET utf8mb4 COLLATE utf8mb4_0900_ai_ci NULL DEFAULT NULL COMMENT '日志文本',
  `enabled` tinyint(4) NOT NULL DEFAULT 1 COMMENT '独立开关 (1:启用, 0:禁用)',
  `sort_order` int(11) NOT NULL DEFAULT 0 COMMENT '执行次序',
  `create_time` datetime(0) NOT NULL DEFAULT CURRENT_TIMESTAMP(0) COMMENT '创建时间',
  PRIMARY KEY (`id`) USING BTREE,
  INDEX `idx_rule_id`(`rule_id`) USING BTREE,
  INDEX `idx_target_device`(`target_device_id`) USING BTREE
) ENGINE = InnoDB AUTO_INCREMENT = 10 CHARACTER SET = utf8mb4 COLLATE = utf8mb4_0900_ai_ci COMMENT = '规则联动响应动作关联明细表' ROW_FORMAT = Dynamic;

-- ----------------------------
-- Records of iot_rule_action
-- ----------------------------
INSERT INTO `iot_rule_action` VALUES (1, 'RULE_101', 'ACT_101', 'ALARM', '触发电机主轴承急停告警', 'DEV_001', NULL, NULL, 'CRITICAL', '当1号轴承温度超过75℃时触发自动开启风扇及急停预警', NULL, NULL, 1, 0, '2026-09-09 20:04:04');
INSERT INTO `iot_rule_action` VALUES (2, 'RULE_101', 'ACT_102', 'SERVICE', '开启辅助冷却风扇系统', 'DEV_001', 'startCoolingFan', '{\"speed\":3000,\"mode\":\"AUTO\"}', NULL, NULL, NULL, NULL, 1, 1, '2026-09-09 20:04:04');
INSERT INTO `iot_rule_action` VALUES (3, 'RULE_102', 'ACT_201', 'ALARM', '储罐高压越界告警', 'DEV_002', NULL, NULL, 'HIGH', '储罐压力不在10~100kPa安全范围自动报警', NULL, NULL, 1, 0, '2026-09-09 20:04:04');
INSERT INTO `iot_rule_action` VALUES (4, 'RULE_103', 'ACT_301', 'LOG', '发布湿度偏高审计日志', 'DEV_001', NULL, NULL, NULL, NULL, 'INFO', '[规则引擎] 检测到1号电机车间湿度偏高', 1, 0, '2026-09-09 20:04:04');
INSERT INTO `iot_rule_action` VALUES (8, 'RULE_484487', 'act_245595', 'SERVICE', 'Modbus设备重置默认值', 'DEV_OGY82W69', 'Init_Modbus_Tcp', '{\"temperture\":0.88,\"model\":0,\"P3\":0.66,\"P4\":0}', 'CRITICAL', '', 'INFO', '', 1, 0, '2026-09-14 20:48:37');
INSERT INTO `iot_rule_action` VALUES (9, 'RULE_484487', 'act_535491', 'ALARM', 'Modbus设备重置告警', 'DEV_OGY82W69', 'Init_Modbus_Tcp', '{\"temperture\":0.88,\"model\":0,\"P3\":0.66,\"P4\":0}', 'CRITICAL', '告警', 'INFO', '', 1, 1, '2026-09-14 20:48:37');

-- ----------------------------
-- Table structure for iot_thing_model
-- ----------------------------
DROP TABLE IF EXISTS `iot_thing_model`;
CREATE TABLE `iot_thing_model`  (
  `id` bigint(20) NOT NULL AUTO_INCREMENT COMMENT '自增主键ID',
  `model_key` varchar(64) CHARACTER SET utf8mb4 COLLATE utf8mb4_0900_ai_ci NOT NULL COMMENT '关联物模型Key',
  `identifier` varchar(64) CHARACTER SET utf8mb4 COLLATE utf8mb4_0900_ai_ci NOT NULL COMMENT '点位/服务标识符 (如 temperature)',
  `name` varchar(128) CHARACTER SET utf8mb4 COLLATE utf8mb4_0900_ai_ci NOT NULL COMMENT '显示名称',
  `model_type` varchar(32) CHARACTER SET utf8mb4 COLLATE utf8mb4_0900_ai_ci NOT NULL DEFAULT 'PROPERTY' COMMENT '物模型类型 (PROPERTY:属性, SERVICE:服务, EVENT:事件)',
  `data_type` varchar(32) CHARACTER SET utf8mb4 COLLATE utf8mb4_0900_ai_ci NOT NULL DEFAULT 'DOUBLE' COMMENT '数据类型 (INT, DOUBLE, STRING, BOOL, ENUM)',
  `access_mode` varchar(16) CHARACTER SET utf8mb4 COLLATE utf8mb4_0900_ai_ci NOT NULL DEFAULT 'R' COMMENT '读写模式 (R:只读, RW:读写)',
  `unit` varchar(32) CHARACTER SET utf8mb4 COLLATE utf8mb4_0900_ai_ci NULL DEFAULT NULL COMMENT '物理测量单位',
  `schema_json` text CHARACTER SET utf8mb4 COLLATE utf8mb4_0900_ai_ci NULL COMMENT '属性/服务扩展Schema定义 (JSON格式)',
  `create_time` datetime(0) NOT NULL DEFAULT CURRENT_TIMESTAMP(0) COMMENT '创建时间',
  PRIMARY KEY (`id`) USING BTREE
) ENGINE = InnoDB AUTO_INCREMENT = 25 CHARACTER SET = utf8mb4 COLLATE = utf8mb4_0900_ai_ci COMMENT = '物模型元数据定义表' ROW_FORMAT = Dynamic;

-- ----------------------------
-- Records of iot_thing_model
-- ----------------------------
INSERT INTO `iot_thing_model` VALUES (1, 'pk_demo', 'temperature', '主轴承实时温度', 'PROPERTY', 'DOUBLE', 'R', '℃', '{\"description\":\"电机主轴承温度传感器只读数据\"}', '2026-09-09 20:04:04');
INSERT INTO `iot_thing_model` VALUES (2, 'pk_demo', 'humidity', '环境相对湿度', 'PROPERTY', 'DOUBLE', 'R', '%RH', '{\"description\":\"电机安装车间湿度\"}', '2026-09-09 20:04:04');
INSERT INTO `iot_thing_model` VALUES (3, 'pk_demo', 'pressure', '进气口设定压力', 'PROPERTY', 'DOUBLE', 'RW', 'kPa', '{\"description\":\"控制风机目标进气口压力\"}', '2026-09-09 20:04:04');
INSERT INTO `iot_thing_model` VALUES (4, 'pk_demo', 'targetSpeed', '变频目标转速', 'PROPERTY', 'INT', 'RW', 'RPM', '{\"description\":\"额定转速设定值\"}', '2026-09-09 20:04:04');
INSERT INTO `iot_thing_model` VALUES (5, 'pk_demo', 'startCoolingFan', '开启/调节冷却风扇', 'SERVICE', 'VOID', 'W', NULL, '{\"description\":\"下发风扇编号及目标风速指令\",\"inputParams\":[{\"pointCode\":\"fanId\",\"pointName\":\"风扇编号\",\"dataType\":\"INT\"},{\"pointCode\":\"speed\",\"pointName\":\"目标转速\",\"dataType\":\"INT\"}]}', '2026-09-09 20:04:04');
INSERT INTO `iot_thing_model` VALUES (6, 'pk_demo', 'setRelay', '继电器开关控制', 'SERVICE', 'VOID', 'W', NULL, '{\"description\":\"控制输出通道闭合/断开\",\"inputParams\":[{\"pointCode\":\"channel\",\"pointName\":\"通道号\",\"dataType\":\"INT\"},{\"pointCode\":\"state\",\"pointName\":\"开关状态\",\"dataType\":\"BOOL\"}]}', '2026-09-09 20:04:04');
INSERT INTO `iot_thing_model` VALUES (7, 'pk_demo', 'OVER_TEMP_WARN', '电机轴承超温自保护', 'EVENT', 'VOID', 'R', NULL, '{\"eventType\":\"ALERT\",\"eventLevel\":\"WARN\",\"description\":\"硬件微控制器检测到轴承温度异常过热主动关断并上报\"}', '2026-09-09 20:04:04');
INSERT INTO `iot_thing_model` VALUES (8, 'pk_plc_s7', 'plcRunStatus', 'PLC 运行状态', 'PROPERTY', 'BOOL', 'RW', '', '{\"description\":\"PLC 当前 RUN/STOP 标志\",\"triggerMode\":\"PERIODIC\",\"sampleFactor\":1}', '2026-09-09 20:04:04');
INSERT INTO `iot_thing_model` VALUES (9, 'pk_plc_s7', 'errorCode', '系统故障代码', 'PROPERTY', 'INT', 'RW', '', '{\"description\":\"故障报警码\",\"triggerMode\":\"PERIODIC\",\"sampleFactor\":1}', '2026-09-09 20:04:04');
INSERT INTO `iot_thing_model` VALUES (10, 'pk_plc_s7', 'targetPressure', '储罐加压目标设定值', 'PROPERTY', 'DOUBLE', 'RW', 'kPa', '{\"description\":\"西门子 PLC 储罐远控加压设定值 (DB1.DBD0)\",\"min\":0.0,\"max\":200.0}', '2026-09-09 20:04:04');
INSERT INTO `iot_thing_model` VALUES (11, 'pk_plc_s7', 'remoteControlCmd', '远程启停遥控开关', 'PROPERTY', 'BOOL', 'RW', '', '{\"description\":\"S7 PLC 远程运行/停止逻辑反写控制 (DB1.DBX4.0)\"}', '2026-09-09 20:04:04');
INSERT INTO `iot_thing_model` VALUES (12, 'pk_plc_s7', 'resetFault', '告警故障一键复位', 'SERVICE', 'VOID', 'W', NULL, '{\"description\":\"清除 PLC 故障状态与历史锁死标志\",\"inputParams\":[{\"pointCode\":\"clearAll\",\"pointName\":\"清除告警\",\"dataType\":\"BOOL\"}]}', '2026-09-09 20:04:04');
INSERT INTO `iot_thing_model` VALUES (13, 'pk_plc_s7', 'OVER_PRESSURE_ALARM', '储罐超压紧急防护报警', 'EVENT', 'VOID', 'R', NULL, '{\"eventType\":\"ALERT\",\"eventLevel\":\"CRITICAL\",\"description\":\"S7 PLC 监测到储罐压力越界触发高压切断系统并上报\"}', '2026-09-09 20:04:04');
INSERT INTO `iot_thing_model` VALUES (14, 'pk_opcua_server', 'flowRate', '管道实时流量', 'PROPERTY', 'DOUBLE', 'R', 'm³/h', '{\"description\":\"OPC UA 流量计实时只读测点\"}', '2026-09-09 20:04:04');
INSERT INTO `iot_thing_model` VALUES (15, 'pk_opcua_server', 'valvePosition', '调节阀目标开度', 'PROPERTY', 'DOUBLE', 'RW', '%', '{\"description\":\"OPC UA 节点 Valve.Pos 远程反写阀门开度 (0 ~ 100%)\",\"min\":0.0,\"max\":100.0}', '2026-09-09 20:04:04');
INSERT INTO `iot_thing_model` VALUES (16, 'pk_opcua_server', 'pumpSwitch', '循环泵远程开关', 'PROPERTY', 'BOOL', 'RW', '', '{\"description\":\"OPC UA 节点 Pump.Switch 逻辑控制开关\"}', '2026-09-09 20:04:04');
INSERT INTO `iot_thing_model` VALUES (17, 'pk_opcua_server', 'calibrateSensor', '传感器零点校准', 'SERVICE', 'VOID', 'W', NULL, '{\"description\":\"下发 OPC UA 传感器校准数值\",\"inputParams\":[{\"pointCode\":\"offset\",\"pointName\":\"零点偏移量\",\"dataType\":\"DOUBLE\"}]}', '2026-09-09 20:04:04');
INSERT INTO `iot_thing_model` VALUES (18, 'pk_opcua_server', 'SENSOR_FAULT_ALARM', '节点传感器通讯异常', 'EVENT', 'VOID', 'R', NULL, '{\"eventType\":\"FAULT\",\"eventLevel\":\"HIGH\",\"description\":\"OPC UA 节点通讯握手超时报警\"}', '2026-09-09 20:04:04');
INSERT INTO `iot_thing_model` VALUES (19, 'PK_6Z6UNCJ6', '_META_', 'Modelbus协议调试', 'META', 'NONE', 'NONE', NULL, '{\"description\":\"调试物模型\",\"category\":\"ROTATING_EQUIP\"}', '2026-09-09 20:30:28');
INSERT INTO `iot_thing_model` VALUES (20, 'PK_6Z6UNCJ6', 'temperture', '温度', 'PROPERTY', 'FLOAT', 'RW', '', '{\"enumOptions\":[{\"value\":0,\"label\":\"关 / OFF / 模式0\"},{\"value\":1,\"label\":\"开 / ON / 模式1\"}],\"address\":\"\",\"min\":0.0,\"max\":500.0,\"description\":\"\",\"triggerMode\":\"PERIODIC\",\"sampleFactor\":1}', '2026-09-09 20:31:09');
INSERT INTO `iot_thing_model` VALUES (21, 'PK_6Z6UNCJ6', 'model', '模式', 'PROPERTY', 'ENUM', 'RW', '', '{\"enumOptions\":[{\"value\":0,\"label\":\"关 / OFF / 手动\"},{\"value\":1,\"label\":\"开 / ON / 自动\"}],\"address\":\"\",\"min\":0.0,\"max\":500.0,\"description\":\"\",\"triggerMode\":\"PERIODIC\",\"sampleFactor\":1}', '2026-09-10 20:18:38');
INSERT INTO `iot_thing_model` VALUES (22, 'PK_6Z6UNCJ6', 'P3', '点位3', 'PROPERTY', 'DOUBLE', 'RW', '', '{\"enumOptions\":[{\"value\":0,\"label\":\"关 / OFF / 模式0\"},{\"value\":1,\"label\":\"开 / ON / 模式1\"}],\"address\":\"\",\"min\":0.0,\"max\":500.0,\"description\":\"\",\"triggerMode\":\"PERIODIC\",\"sampleFactor\":1}', '2026-09-10 20:19:01');
INSERT INTO `iot_thing_model` VALUES (23, 'PK_6Z6UNCJ6', 'P4', '点位4', 'PROPERTY', 'INT', 'RW', '', '{\"enumOptions\":[{\"value\":0,\"label\":\"关 / OFF / 模式0\"},{\"value\":1,\"label\":\"开 / ON / 模式1\"}],\"address\":\"\",\"min\":0.0,\"max\":500.0,\"description\":\"\",\"triggerMode\":\"PERIODIC\",\"sampleFactor\":1}', '2026-09-10 20:19:20');
INSERT INTO `iot_thing_model` VALUES (24, 'PK_6Z6UNCJ6', 'Init_Modbus_Tcp', '初始化Modbus设备', 'SERVICE', 'VOID', 'W', NULL, '{\"inputParams\":[{\"dataType\":\"FLOAT\",\"defaultValue\":0.88,\"enumOptions\":[],\"offset\":0.0,\"pointCode\":\"temperture\",\"pointName\":\"温度\",\"sampleFactor\":1,\"scale\":1.0,\"triggerMode\":\"PERIODIC\"},{\"dataType\":\"INT\",\"defaultValue\":0,\"enumOptions\":[],\"offset\":0.0,\"pointCode\":\"model\",\"pointName\":\"模式\",\"sampleFactor\":1,\"scale\":1.0,\"triggerMode\":\"PERIODIC\"},{\"dataType\":\"DOUBLE\",\"defaultValue\":0.66,\"enumOptions\":[],\"offset\":0.0,\"pointCode\":\"P3\",\"pointName\":\"点位3\",\"sampleFactor\":1,\"scale\":1.0,\"triggerMode\":\"PERIODIC\"},{\"dataType\":\"INT\",\"defaultValue\":0,\"enumOptions\":[],\"offset\":0.0,\"pointCode\":\"P4\",\"pointName\":\"点位4\",\"sampleFactor\":1,\"scale\":1.0,\"triggerMode\":\"PERIODIC\"}],\"description\":\"初始化设备参数\"}', '2026-09-10 20:21:19');

-- ----------------------------
-- Table structure for sys_dict_data
-- ----------------------------
DROP TABLE IF EXISTS `sys_dict_data`;
CREATE TABLE `sys_dict_data`  (
  `id` bigint(20) NOT NULL AUTO_INCREMENT COMMENT '自增字典数据ID',
  `dict_type` varchar(100) CHARACTER SET utf8mb4 COLLATE utf8mb4_0900_ai_ci NOT NULL COMMENT '字典类型 (关联 sys_dict_type.dict_type)',
  `dict_label` varchar(100) CHARACTER SET utf8mb4 COLLATE utf8mb4_0900_ai_ci NOT NULL COMMENT '字典标签名称',
  `dict_value` varchar(100) CHARACTER SET utf8mb4 COLLATE utf8mb4_0900_ai_ci NOT NULL COMMENT '字典键值',
  `sort_order` int(11) NOT NULL DEFAULT 0 COMMENT '字典排序',
  `css_class` varchar(100) CHARACTER SET utf8mb4 COLLATE utf8mb4_0900_ai_ci NULL DEFAULT NULL COMMENT '样式属性',
  `list_class` varchar(100) CHARACTER SET utf8mb4 COLLATE utf8mb4_0900_ai_ci NULL DEFAULT NULL COMMENT '表格回显样式',
  `is_default` tinyint(4) NOT NULL DEFAULT 0 COMMENT '是否默认 (1:是, 0:否)',
  `status` tinyint(4) NOT NULL DEFAULT 1 COMMENT '状态 (1:正常, 0:停用)',
  `remark` varchar(500) CHARACTER SET utf8mb4 COLLATE utf8mb4_0900_ai_ci NULL DEFAULT NULL COMMENT '备注',
  `create_time` datetime(0) NOT NULL DEFAULT CURRENT_TIMESTAMP(0) COMMENT '创建时间',
  `update_time` datetime(0) NOT NULL DEFAULT CURRENT_TIMESTAMP(0) ON UPDATE CURRENT_TIMESTAMP(0) COMMENT '更新时间',
  PRIMARY KEY (`id`) USING BTREE,
  INDEX `idx_dict_data_type`(`dict_type`, `sort_order`) USING BTREE
) ENGINE = InnoDB AUTO_INCREMENT = 15 CHARACTER SET = utf8mb4 COLLATE = utf8mb4_0900_ai_ci COMMENT = '系统字典键值数据表' ROW_FORMAT = Dynamic;

-- ----------------------------
-- Records of sys_dict_data
-- ----------------------------
INSERT INTO `sys_dict_data` VALUES (1, 'sys_user_status', '正常启用', '1', 1, 'el-tag--success', 'success', 1, 1, '用户账号正常使用', '2026-09-14 21:20:32', '2026-09-14 21:20:32');
INSERT INTO `sys_dict_data` VALUES (2, 'sys_user_status', '停用冻结', '0', 2, 'el-tag--danger', 'danger', 0, 1, '账号已被管理员停用', '2026-09-14 21:20:32', '2026-09-14 21:20:32');
INSERT INTO `sys_dict_data` VALUES (3, 'sys_device_status', '在线运行', '1', 1, 'el-tag--success', 'success', 1, 1, '通道网络心跳正常', '2026-09-14 21:20:32', '2026-09-14 21:20:32');
INSERT INTO `sys_dict_data` VALUES (4, 'sys_device_status', '未连接/离线', '0', 2, 'el-tag--warning', 'warning', 0, 1, 'TCP/串口未建立连接', '2026-09-14 21:20:32', '2026-09-14 21:20:32');
INSERT INTO `sys_dict_data` VALUES (5, 'sys_device_status', '连接异常', '2', 3, 'el-tag--danger', 'danger', 0, 1, '通信超时或数据校验失败', '2026-09-14 21:20:32', '2026-09-14 21:20:32');
INSERT INTO `sys_dict_data` VALUES (6, 'iot_alarm_level', '普通提示', 'INFO', 1, 'el-tag--info', 'info', 0, 1, '常规参数越界提示', '2026-09-14 21:20:32', '2026-09-14 21:20:32');
INSERT INTO `sys_dict_data` VALUES (7, 'iot_alarm_level', '一般告警', 'WARN', 2, 'el-tag--warning', 'warning', 1, 1, '非致命异常', '2026-09-14 21:20:32', '2026-09-14 21:20:32');
INSERT INTO `sys_dict_data` VALUES (8, 'iot_alarm_level', '严重超限', 'HIGH', 3, 'el-tag--danger', 'danger', 0, 1, '工艺参数严重超标', '2026-09-14 21:20:32', '2026-09-14 21:20:32');
INSERT INTO `sys_dict_data` VALUES (9, 'iot_alarm_level', '紧急险情', 'CRITICAL', 4, 'el-tag--danger', 'danger', 0, 1, '触发设备急停保护', '2026-09-14 21:20:32', '2026-09-14 21:20:32');
INSERT INTO `sys_dict_data` VALUES (10, 'iot_data_type', '双精度浮点 (Double)', 'DOUBLE', 1, '', 'primary', 1, 1, '64位 IEEE 754 浮点', '2026-09-14 21:20:32', '2026-09-14 21:20:32');
INSERT INTO `sys_dict_data` VALUES (11, 'iot_data_type', '整型 (Integer)', 'INT', 2, '', 'primary', 0, 1, '16位/32位整型', '2026-09-14 21:20:32', '2026-09-14 21:20:32');
INSERT INTO `sys_dict_data` VALUES (12, 'iot_data_type', '布尔开关 (Boolean)', 'BOOL', 3, '', 'success', 0, 1, '0 或 1 逻辑开关', '2026-09-14 21:20:32', '2026-09-14 21:20:32');
INSERT INTO `sys_dict_data` VALUES (13, 'iot_data_type', '文本字符串 (String)', 'STRING', 4, '', 'info', 0, 1, '字符串文本', '2026-09-14 21:20:32', '2026-09-14 21:20:32');
INSERT INTO `sys_dict_data` VALUES (14, 'iot_data_type', '枚举状态 (Enum)', 'ENUM', 5, '', 'warning', 0, 1, '离散枚举代码', '2026-09-14 21:20:32', '2026-09-14 21:20:32');

-- ----------------------------
-- Table structure for sys_dict_type
-- ----------------------------
DROP TABLE IF EXISTS `sys_dict_type`;
CREATE TABLE `sys_dict_type`  (
  `id` bigint(20) NOT NULL AUTO_INCREMENT COMMENT '自增字典类型ID',
  `dict_name` varchar(100) CHARACTER SET utf8mb4 COLLATE utf8mb4_0900_ai_ci NOT NULL COMMENT '字典名称',
  `dict_type` varchar(100) CHARACTER SET utf8mb4 COLLATE utf8mb4_0900_ai_ci NOT NULL COMMENT '字典类型唯一标识',
  `status` tinyint(4) NOT NULL DEFAULT 1 COMMENT '状态 (1:正常, 0:停用)',
  `remark` varchar(500) CHARACTER SET utf8mb4 COLLATE utf8mb4_0900_ai_ci NULL DEFAULT NULL COMMENT '备注',
  `create_time` datetime(0) NOT NULL DEFAULT CURRENT_TIMESTAMP(0) COMMENT '创建时间',
  `update_time` datetime(0) NOT NULL DEFAULT CURRENT_TIMESTAMP(0) ON UPDATE CURRENT_TIMESTAMP(0) COMMENT '更新时间',
  PRIMARY KEY (`id`) USING BTREE,
  UNIQUE INDEX `dict_type`(`dict_type`) USING BTREE
) ENGINE = InnoDB AUTO_INCREMENT = 5 CHARACTER SET = utf8mb4 COLLATE = utf8mb4_0900_ai_ci COMMENT = '系统字典类型表' ROW_FORMAT = Dynamic;

-- ----------------------------
-- Records of sys_dict_type
-- ----------------------------
INSERT INTO `sys_dict_type` VALUES (1, '系统用户状态', 'sys_user_status', 1, '用户账号可用状态', '2026-09-14 21:20:32', '2026-09-14 21:20:32');
INSERT INTO `sys_dict_type` VALUES (2, '设备通信在线状态', 'sys_device_status', 1, '物理设备通道网络状态', '2026-09-14 21:20:32', '2026-09-14 21:20:32');
INSERT INTO `sys_dict_type` VALUES (3, '告警事件紧急等级', 'iot_alarm_level', 1, '系统告警级别', '2026-09-14 21:20:32', '2026-09-14 21:20:32');
INSERT INTO `sys_dict_type` VALUES (4, '物模型数据类型', 'iot_data_type', 1, '物模型点位与参数数据类型', '2026-09-14 21:20:32', '2026-09-14 21:20:32');

-- ----------------------------
-- Table structure for sys_menu
-- ----------------------------
DROP TABLE IF EXISTS `sys_menu`;
CREATE TABLE `sys_menu`  (
  `id` bigint(20) NOT NULL AUTO_INCREMENT COMMENT '自增菜单ID',
  `parent_id` bigint(20) NOT NULL DEFAULT 0 COMMENT '父菜单ID (0为顶级目录)',
  `menu_name` varchar(64) CHARACTER SET utf8mb4 COLLATE utf8mb4_0900_ai_ci NOT NULL COMMENT '菜单名称',
  `path` varchar(128) CHARACTER SET utf8mb4 COLLATE utf8mb4_0900_ai_ci NOT NULL COMMENT '路由访问路径',
  `component` varchar(128) CHARACTER SET utf8mb4 COLLATE utf8mb4_0900_ai_ci NULL DEFAULT NULL COMMENT '前端组件路径',
  `icon` varchar(64) CHARACTER SET utf8mb4 COLLATE utf8mb4_0900_ai_ci NULL DEFAULT NULL COMMENT '菜单图标',
  `sort_order` int(11) NOT NULL DEFAULT 0 COMMENT '显示顺序',
  `menu_type` char(1) CHARACTER SET utf8mb4 COLLATE utf8mb4_0900_ai_ci NOT NULL DEFAULT 'C' COMMENT '菜单类型 (M:目录, C:菜单, F:按钮)',
  `perms` varchar(128) CHARACTER SET utf8mb4 COLLATE utf8mb4_0900_ai_ci NULL DEFAULT NULL COMMENT '权限标识 (如 system:user:view)',
  `visible` tinyint(4) NOT NULL DEFAULT 1 COMMENT '是否显示 (1:显示, 0:隐藏)',
  `status` tinyint(4) NOT NULL DEFAULT 1 COMMENT '状态 (1:启用, 0:停用)',
  `remark` varchar(500) CHARACTER SET utf8mb4 COLLATE utf8mb4_0900_ai_ci NULL DEFAULT NULL COMMENT '备注',
  `create_time` datetime(0) NOT NULL DEFAULT CURRENT_TIMESTAMP(0) COMMENT '创建时间',
  `update_time` datetime(0) NOT NULL DEFAULT CURRENT_TIMESTAMP(0) ON UPDATE CURRENT_TIMESTAMP(0) COMMENT '更新时间',
  PRIMARY KEY (`id`) USING BTREE,
  INDEX `idx_menu_parent`(`parent_id`) USING BTREE
) ENGINE = InnoDB AUTO_INCREMENT = 14 CHARACTER SET = utf8mb4 COLLATE = utf8mb4_0900_ai_ci COMMENT = '系统菜单与动态路由配置表' ROW_FORMAT = Dynamic;

-- ----------------------------
-- Records of sys_menu
-- ----------------------------
INSERT INTO `sys_menu` VALUES (1, 0, '系统仪表盘', '/dashboard', 'views/dashboard/OverviewDashboard.vue', 'activity', 1, 'C', 'dashboard:view', 1, 1, '全景数据监控与概览', '2026-09-14 21:20:32', '2026-09-14 21:20:32');
INSERT INTO `sys_menu` VALUES (2, 0, '驱动管理', '/drivers', 'views/protocol/ProtocolDrivers.vue', 'server', 2, 'C', 'driver:list', 1, 1, '工业协议驱动管理', '2026-09-14 21:20:32', '2026-09-14 21:20:32');
INSERT INTO `sys_menu` VALUES (3, 0, '设备管理', '/devices', 'views/devices/DeviceList.vue', 'cpu', 3, 'C', 'device:list', 1, 1, '边缘硬件网关与设备管理', '2026-09-14 21:20:32', '2026-09-14 21:20:32');
INSERT INTO `sys_menu` VALUES (4, 0, '数据管理', '/data', 'views/data/PointDataMonitor.vue', 'database', 4, 'C', 'data:monitor', 1, 1, '点位实时与历史数据', '2026-09-14 21:20:32', '2026-09-14 21:20:32');
INSERT INTO `sys_menu` VALUES (5, 0, '物模型管理', '/thingmodels', 'views/thingmodels/ThingModelManager.vue', 'database', 5, 'C', 'thingmodel:list', 1, 1, '物模型Schema元数据定义', '2026-09-14 21:20:32', '2026-09-14 21:20:32');
INSERT INTO `sys_menu` VALUES (6, 0, '规则管理', '/rules', 'views/rules/RuleManager.vue', 'refresh', 6, 'C', 'rule:list', 1, 1, '轻量级联动规则引擎', '2026-09-14 21:20:32', '2026-09-14 21:20:32');
INSERT INTO `sys_menu` VALUES (7, 0, '告警中心', '/alarms', 'views/alarms/AlarmEvents.vue', 'bell', 7, 'C', 'alarm:list', 1, 1, '异常状态与事件报警', '2026-09-14 21:20:32', '2026-09-14 21:20:32');
INSERT INTO `sys_menu` VALUES (8, 0, '日志管理', '/logs', 'views/logs/LogManager.vue', 'file-text', 8, 'C', 'log:list', 1, 1, '运行与审计日志中心', '2026-09-14 21:20:32', '2026-09-14 21:20:32');
INSERT INTO `sys_menu` VALUES (9, 0, '系统管理', '/system', 'Layout', 'shield', 9, 'M', 'system:manage', 1, 1, '系统与用户权限管理目录', '2026-09-14 21:20:32', '2026-09-14 21:20:32');
INSERT INTO `sys_menu` VALUES (10, 9, '用户管理', '/system/users', 'views/system/UserList.vue', 'user', 1, 'C', 'system:user:list', 1, 1, '系统账号管理', '2026-09-14 21:20:32', '2026-09-14 21:20:32');
INSERT INTO `sys_menu` VALUES (11, 9, '角色权限', '/system/roles', 'views/system/RoleList.vue', 'shield', 2, 'C', 'system:role:list', 1, 1, '角色与权限分配', '2026-09-14 21:20:32', '2026-09-14 21:20:32');
INSERT INTO `sys_menu` VALUES (12, 9, '菜单路由', '/system/menus', 'views/system/MenuList.vue', 'menu', 3, 'C', 'system:menu:list', 1, 1, '动态路由菜单维护', '2026-09-14 21:20:32', '2026-09-14 21:20:32');
INSERT INTO `sys_menu` VALUES (13, 9, '字典管理', '/system/dicts', 'views/system/DictList.vue', 'file-text', 4, 'C', 'system:dict:list', 1, 1, '数据字典与常量配置', '2026-09-14 21:20:32', '2026-09-14 21:20:32');

-- ----------------------------
-- Table structure for sys_permission
-- ----------------------------
DROP TABLE IF EXISTS `sys_permission`;
CREATE TABLE `sys_permission`  (
  `id` bigint(20) NOT NULL AUTO_INCREMENT COMMENT '自增权限ID',
  `perm_key` varchar(128) CHARACTER SET utf8mb4 COLLATE utf8mb4_0900_ai_ci NOT NULL COMMENT '权限标识编码 (如 device:control)',
  `perm_name` varchar(128) CHARACTER SET utf8mb4 COLLATE utf8mb4_0900_ai_ci NOT NULL COMMENT '权限名称',
  `module` varchar(64) CHARACTER SET utf8mb4 COLLATE utf8mb4_0900_ai_ci NULL DEFAULT NULL COMMENT '所属业务模块',
  `remark` varchar(500) CHARACTER SET utf8mb4 COLLATE utf8mb4_0900_ai_ci NULL DEFAULT NULL COMMENT '备注',
  `create_time` datetime(0) NOT NULL DEFAULT CURRENT_TIMESTAMP(0) COMMENT '创建时间',
  PRIMARY KEY (`id`) USING BTREE,
  UNIQUE INDEX `perm_key`(`perm_key`) USING BTREE
) ENGINE = InnoDB AUTO_INCREMENT = 16 CHARACTER SET = utf8mb4 COLLATE = utf8mb4_0900_ai_ci COMMENT = '系统功能权限表' ROW_FORMAT = Dynamic;

-- ----------------------------
-- Records of sys_permission
-- ----------------------------
INSERT INTO `sys_permission` VALUES (1, 'system:user:list', '用户查询', '系统管理', '查看用户列表与账号详情', '2026-09-14 21:20:32');
INSERT INTO `sys_permission` VALUES (2, 'system:user:add', '用户新增', '系统管理', '创建系统操作人员账号', '2026-09-14 21:20:32');
INSERT INTO `sys_permission` VALUES (3, 'system:user:edit', '用户编辑', '系统管理', '修改信息、重置密码与启停状态', '2026-09-14 21:20:32');
INSERT INTO `sys_permission` VALUES (4, 'device:list', '设备查看', '设备管理', '查看物理设备列表与状态', '2026-09-14 21:20:32');
INSERT INTO `sys_permission` VALUES (5, 'device:add', '设备新增', '设备管理', '注册接入物理硬件设备', '2026-09-14 21:20:32');
INSERT INTO `sys_permission` VALUES (6, 'device:control', '设备反控', '设备管理', '下发设备物模型服务指令与控制调试', '2026-09-14 21:20:32');
INSERT INTO `sys_permission` VALUES (7, 'driver:list', '驱动查看', '驱动管理', '查看协议驱动列表与状态', '2026-09-14 21:20:32');
INSERT INTO `sys_permission` VALUES (8, 'thingmodel:list', '物模型查看', '物模型管理', '查看物模型属性与服务', '2026-09-14 21:20:32');
INSERT INTO `sys_permission` VALUES (9, 'thingmodel:edit', '物模型编辑', '物模型管理', '编辑物模型模板名称、编码与分类', '2026-09-14 21:20:32');
INSERT INTO `sys_permission` VALUES (10, 'data:monitor', '数据实时监控', '数据中心', '查看点位实时趋势', '2026-09-14 21:20:32');
INSERT INTO `sys_permission` VALUES (11, 'rule:list', '规则查看', '规则引擎', '查看阈值与告警联动规则', '2026-09-14 21:20:32');
INSERT INTO `sys_permission` VALUES (12, 'rule:edit', '规则维护', '规则引擎', '配置与修改联动规则判定与反控动作', '2026-09-14 21:20:32');
INSERT INTO `sys_permission` VALUES (13, 'alarm:list', '告警查看', '告警中心', '查看系统告警事件记录', '2026-09-14 21:20:32');
INSERT INTO `sys_permission` VALUES (14, 'alarm:handle', '告警处理', '告警中心', '确认与闭环恢复告警', '2026-09-14 21:20:32');
INSERT INTO `sys_permission` VALUES (15, 'log:list', '日志查看', '日志管理', '查看系统运行与操作审计日志', '2026-09-14 21:20:32');
INSERT INTO `sys_permission` VALUES (16, 'system:user:delete', '用户删除', '系统管理', '删除操作人员账号', '2026-10-09 09:03:36');
INSERT INTO `sys_permission` VALUES (17, 'system:role:list', '角色查询', '系统管理', '查看系统角色列表', '2026-10-09 09:03:36');
INSERT INTO `sys_permission` VALUES (18, 'system:role:add', '角色新增', '系统管理', '新建平台角色身份', '2026-10-09 09:03:36');
INSERT INTO `sys_permission` VALUES (19, 'system:role:edit', '角色编辑', '系统管理', '修改角色、授权功能权限与分配菜单', '2026-10-09 09:03:36');
INSERT INTO `sys_permission` VALUES (20, 'system:role:delete', '角色删除', '系统管理', '删除平台角色', '2026-10-09 09:03:36');
INSERT INTO `sys_permission` VALUES (21, 'system:menu:list', '菜单查询', '系统管理', '查看动态路由与菜单树', '2026-10-09 09:03:36');
INSERT INTO `sys_permission` VALUES (22, 'system:menu:add', '菜单新增', '系统管理', '新增顶级菜单或子项', '2026-10-09 09:03:36');
INSERT INTO `sys_permission` VALUES (23, 'system:menu:edit', '菜单编辑', '系统管理', '修改菜单信息与权限标识', '2026-10-09 09:03:36');
INSERT INTO `sys_permission` VALUES (24, 'system:menu:delete', '菜单删除', '系统管理', '删除系统菜单项', '2026-10-09 09:03:36');
INSERT INTO `sys_permission` VALUES (25, 'system:dict:list', '字典查询', '系统管理', '查看数据字典类型与键值', '2026-10-09 09:03:36');
INSERT INTO `sys_permission` VALUES (26, 'system:dict:add', '字典新增', '系统管理', '新增字典类型与字典键值', '2026-10-09 09:03:36');
INSERT INTO `sys_permission` VALUES (27, 'system:dict:edit', '字典修改', '系统管理', '修改字典类型与字典键值', '2026-10-09 09:03:36');
INSERT INTO `sys_permission` VALUES (28, 'system:dict:delete', '字典删除', '系统管理', '删除字典类型与字典键值', '2026-10-09 09:03:36');
INSERT INTO `sys_permission` VALUES (29, 'device:edit', '设备编辑', '设备管理', '编辑修改物理设备配置与点位映射', '2026-10-09 09:03:36');
INSERT INTO `sys_permission` VALUES (30, 'device:delete', '设备删除', '设备管理', '移除边缘网关或终端设备', '2026-10-09 09:03:36');
INSERT INTO `sys_permission` VALUES (31, 'driver:edit', '驱动管理', '驱动管理', '注册、更新驱动配置与启停驱动', '2026-10-09 09:03:36');
INSERT INTO `sys_permission` VALUES (32, 'thingmodel:add', '物模型新增', '物模型管理', '新建物模型基础模板', '2026-10-09 09:03:36');
INSERT INTO `sys_permission` VALUES (33, 'thingmodel:design', '设计物模型', '物模型管理', '设计物模型 Schema，定义与导入点位属性、控制服务与告警事件', '2026-10-09 09:03:36');
INSERT INTO `sys_permission` VALUES (34, 'thingmodel:delete', '物模型删除', '物模型管理', '删除未被引用的物模型模板', '2026-10-09 09:03:36');
INSERT INTO `sys_permission` VALUES (35, 'rule:add', '规则新增', '规则引擎', '新建联动告警与自动化反控规则', '2026-10-09 09:03:36');
INSERT INTO `sys_permission` VALUES (36, 'rule:delete', '规则删除', '规则引擎', '删除联动规则', '2026-10-09 09:03:36');
INSERT INTO `sys_permission` VALUES (37, 'alarm:delete', '告警删除', '告警中心', '物理删除与清理历史告警记录', '2026-10-09 09:03:36');
INSERT INTO `sys_permission` VALUES (38, 'log:clear', '日志清理', '日志管理', '清空与归档审计历史日志', '2026-10-09 09:03:36');
INSERT INTO `sys_permission` VALUES (39, 'dashboard:view', '大屏监控', '监控大屏', '查看全景数据吞吐与设备拓扑大屏', '2026-10-09 09:03:36');

-- ----------------------------
-- Table structure for sys_role
-- ----------------------------
DROP TABLE IF EXISTS `sys_role`;
CREATE TABLE `sys_role`  (
  `id` bigint(20) NOT NULL AUTO_INCREMENT COMMENT '自增角色ID',
  `role_key` varchar(64) CHARACTER SET utf8mb4 COLLATE utf8mb4_0900_ai_ci NOT NULL COMMENT '角色权限字符 (如 admin, operator, viewer)',
  `role_name` varchar(64) CHARACTER SET utf8mb4 COLLATE utf8mb4_0900_ai_ci NOT NULL COMMENT '角色名称',
  `sort_order` int(11) NOT NULL DEFAULT 0 COMMENT '显示排序',
  `status` tinyint(4) NOT NULL DEFAULT 1 COMMENT '角色状态 (1:启用, 0:停用)',
  `remark` varchar(500) CHARACTER SET utf8mb4 COLLATE utf8mb4_0900_ai_ci NULL DEFAULT NULL COMMENT '备注',
  `create_time` datetime(0) NOT NULL DEFAULT CURRENT_TIMESTAMP(0) COMMENT '创建时间',
  `update_time` datetime(0) NOT NULL DEFAULT CURRENT_TIMESTAMP(0) ON UPDATE CURRENT_TIMESTAMP(0) COMMENT '更新时间',
  PRIMARY KEY (`id`) USING BTREE,
  UNIQUE INDEX `role_key`(`role_key`) USING BTREE
) ENGINE = InnoDB AUTO_INCREMENT = 4 CHARACTER SET = utf8mb4 COLLATE = utf8mb4_0900_ai_ci COMMENT = '系统角色表' ROW_FORMAT = Dynamic;

-- ----------------------------
-- Records of sys_role
-- ----------------------------
INSERT INTO `sys_role` VALUES (1, 'admin', '超级管理员', 1, 1, '系统内置最高权限管理员', '2026-09-14 21:20:32', '2026-09-14 21:20:32');
INSERT INTO `sys_role` VALUES (2, 'operator', '工控运维员', 2, 1, '负责现场设备调试、规则与告警处置', '2026-09-14 21:20:32', '2026-09-14 21:20:32');
INSERT INTO `sys_role` VALUES (3, 'viewer', '访客观察员', 3, 1, '仅具有平台数据只读监控权限', '2026-09-14 21:20:32', '2026-09-14 21:20:32');

-- ----------------------------
-- Table structure for sys_role_menu
-- ----------------------------
DROP TABLE IF EXISTS `sys_role_menu`;
CREATE TABLE `sys_role_menu`  (
  `id` bigint(20) NOT NULL AUTO_INCREMENT COMMENT '自增主键ID',
  `role_id` bigint(20) NOT NULL COMMENT '角色ID',
  `menu_id` bigint(20) NOT NULL COMMENT '菜单ID',
  PRIMARY KEY (`id`) USING BTREE,
  UNIQUE INDEX `uk_role_menu`(`role_id`, `menu_id`) USING BTREE,
  INDEX `idx_role_menu_role`(`role_id`) USING BTREE,
  INDEX `idx_role_menu_menu`(`menu_id`) USING BTREE
) ENGINE = InnoDB AUTO_INCREMENT = 22 CHARACTER SET = utf8mb4 COLLATE = utf8mb4_0900_ai_ci COMMENT = '角色菜单关联表' ROW_FORMAT = Dynamic;

-- ----------------------------
-- Records of sys_role_menu
-- ----------------------------
INSERT INTO `sys_role_menu` VALUES (1, 1, 1);
INSERT INTO `sys_role_menu` VALUES (2, 1, 2);
INSERT INTO `sys_role_menu` VALUES (3, 1, 3);
INSERT INTO `sys_role_menu` VALUES (4, 1, 4);
INSERT INTO `sys_role_menu` VALUES (5, 1, 5);
INSERT INTO `sys_role_menu` VALUES (6, 1, 6);
INSERT INTO `sys_role_menu` VALUES (7, 1, 7);
INSERT INTO `sys_role_menu` VALUES (8, 1, 8);
INSERT INTO `sys_role_menu` VALUES (9, 1, 9);
INSERT INTO `sys_role_menu` VALUES (10, 1, 10);
INSERT INTO `sys_role_menu` VALUES (11, 1, 11);
INSERT INTO `sys_role_menu` VALUES (12, 1, 12);
INSERT INTO `sys_role_menu` VALUES (13, 1, 13);
INSERT INTO `sys_role_menu` VALUES (14, 2, 1);
INSERT INTO `sys_role_menu` VALUES (15, 2, 2);
INSERT INTO `sys_role_menu` VALUES (16, 2, 3);
INSERT INTO `sys_role_menu` VALUES (17, 2, 4);
INSERT INTO `sys_role_menu` VALUES (18, 2, 5);
INSERT INTO `sys_role_menu` VALUES (19, 2, 6);
INSERT INTO `sys_role_menu` VALUES (20, 2, 7);
INSERT INTO `sys_role_menu` VALUES (21, 2, 8);

-- ----------------------------
-- Table structure for sys_role_permission
-- ----------------------------
DROP TABLE IF EXISTS `sys_role_permission`;
CREATE TABLE `sys_role_permission`  (
  `id` bigint(20) NOT NULL AUTO_INCREMENT COMMENT '自增主键ID',
  `role_id` bigint(20) NOT NULL COMMENT '角色ID',
  `permission_id` bigint(20) NOT NULL COMMENT '权限ID',
  PRIMARY KEY (`id`) USING BTREE,
  UNIQUE INDEX `uk_role_perm`(`role_id`, `permission_id`) USING BTREE,
  INDEX `idx_role_perm_role`(`role_id`) USING BTREE,
  INDEX `idx_role_perm_perm`(`permission_id`) USING BTREE
) ENGINE = InnoDB AUTO_INCREMENT = 28 CHARACTER SET = utf8mb4 COLLATE = utf8mb4_0900_ai_ci COMMENT = '角色权限关联表' ROW_FORMAT = Dynamic;

-- ----------------------------
-- Records of sys_role_permission
-- ----------------------------
INSERT INTO `sys_role_permission` VALUES (1, 1, 1);
INSERT INTO `sys_role_permission` VALUES (2, 1, 2);
INSERT INTO `sys_role_permission` VALUES (3, 1, 3);
INSERT INTO `sys_role_permission` VALUES (4, 1, 4);
INSERT INTO `sys_role_permission` VALUES (5, 1, 5);
INSERT INTO `sys_role_permission` VALUES (6, 1, 6);
INSERT INTO `sys_role_permission` VALUES (7, 1, 7);
INSERT INTO `sys_role_permission` VALUES (8, 1, 8);
INSERT INTO `sys_role_permission` VALUES (9, 1, 9);
INSERT INTO `sys_role_permission` VALUES (10, 1, 10);
INSERT INTO `sys_role_permission` VALUES (11, 1, 11);
INSERT INTO `sys_role_permission` VALUES (12, 1, 12);
INSERT INTO `sys_role_permission` VALUES (13, 1, 13);
INSERT INTO `sys_role_permission` VALUES (14, 1, 14);
INSERT INTO `sys_role_permission` VALUES (15, 1, 15);
INSERT INTO `sys_role_permission` VALUES (48, 1, 16);
INSERT INTO `sys_role_permission` VALUES (47, 1, 17);
INSERT INTO `sys_role_permission` VALUES (44, 1, 18);
INSERT INTO `sys_role_permission` VALUES (46, 1, 19);
INSERT INTO `sys_role_permission` VALUES (45, 1, 20);
INSERT INTO `sys_role_permission` VALUES (43, 1, 21);
INSERT INTO `sys_role_permission` VALUES (40, 1, 22);
INSERT INTO `sys_role_permission` VALUES (42, 1, 23);
INSERT INTO `sys_role_permission` VALUES (41, 1, 24);
INSERT INTO `sys_role_permission` VALUES (39, 1, 25);
INSERT INTO `sys_role_permission` VALUES (36, 1, 26);
INSERT INTO `sys_role_permission` VALUES (38, 1, 27);
INSERT INTO `sys_role_permission` VALUES (37, 1, 28);
INSERT INTO `sys_role_permission` VALUES (31, 1, 29);
INSERT INTO `sys_role_permission` VALUES (30, 1, 30);
INSERT INTO `sys_role_permission` VALUES (32, 1, 31);
INSERT INTO `sys_role_permission` VALUES (49, 1, 32);
INSERT INTO `sys_role_permission` VALUES (51, 1, 33);
INSERT INTO `sys_role_permission` VALUES (50, 1, 34);
INSERT INTO `sys_role_permission` VALUES (34, 1, 35);
INSERT INTO `sys_role_permission` VALUES (35, 1, 36);
INSERT INTO `sys_role_permission` VALUES (28, 1, 37);
INSERT INTO `sys_role_permission` VALUES (33, 1, 38);
INSERT INTO `sys_role_permission` VALUES (29, 1, 39);
INSERT INTO `sys_role_permission` VALUES (16, 2, 4);
INSERT INTO `sys_role_permission` VALUES (17, 2, 5);
INSERT INTO `sys_role_permission` VALUES (18, 2, 6);
INSERT INTO `sys_role_permission` VALUES (19, 2, 7);
INSERT INTO `sys_role_permission` VALUES (20, 2, 8);
INSERT INTO `sys_role_permission` VALUES (21, 2, 9);
INSERT INTO `sys_role_permission` VALUES (22, 2, 10);
INSERT INTO `sys_role_permission` VALUES (23, 2, 11);
INSERT INTO `sys_role_permission` VALUES (24, 2, 12);
INSERT INTO `sys_role_permission` VALUES (25, 2, 13);
INSERT INTO `sys_role_permission` VALUES (26, 2, 14);
INSERT INTO `sys_role_permission` VALUES (27, 2, 15);
INSERT INTO `sys_role_permission` VALUES (60, 2, 29);
INSERT INTO `sys_role_permission` VALUES (62, 2, 32);
INSERT INTO `sys_role_permission` VALUES (63, 2, 33);
INSERT INTO `sys_role_permission` VALUES (61, 2, 35);
INSERT INTO `sys_role_permission` VALUES (59, 2, 39);

-- ----------------------------
-- Table structure for sys_user
-- ----------------------------
DROP TABLE IF EXISTS `sys_user`;
CREATE TABLE `sys_user`  (
  `id` bigint(20) NOT NULL AUTO_INCREMENT COMMENT '自增主键ID',
  `username` varchar(64) CHARACTER SET utf8mb4 COLLATE utf8mb4_0900_ai_ci NOT NULL COMMENT '登录用户名',
  `nickname` varchar(64) CHARACTER SET utf8mb4 COLLATE utf8mb4_0900_ai_ci NOT NULL COMMENT '用户昵称',
  `password` varchar(128) CHARACTER SET utf8mb4 COLLATE utf8mb4_0900_ai_ci NOT NULL COMMENT '加盐加密后的密码Hash (SHA-256)',
  `salt` varchar(64) CHARACTER SET utf8mb4 COLLATE utf8mb4_0900_ai_ci NOT NULL COMMENT '密码独立随机盐 (Salt)',
  `email` varchar(128) CHARACTER SET utf8mb4 COLLATE utf8mb4_0900_ai_ci NULL DEFAULT NULL COMMENT '电子邮箱',
  `phone` varchar(32) CHARACTER SET utf8mb4 COLLATE utf8mb4_0900_ai_ci NULL DEFAULT NULL COMMENT '联系手机号',
  `avatar` varchar(255) CHARACTER SET utf8mb4 COLLATE utf8mb4_0900_ai_ci NULL DEFAULT NULL COMMENT '用户头像地址',
  `status` tinyint(4) NOT NULL DEFAULT 1 COMMENT '账号状态 (1:启用, 0:停用/冻结)',
  `last_login_ip` varchar(64) CHARACTER SET utf8mb4 COLLATE utf8mb4_0900_ai_ci NULL DEFAULT NULL COMMENT '最后登录IP',
  `last_login_time` datetime(0) NULL DEFAULT NULL COMMENT '最后登录时间',
  `remark` varchar(500) CHARACTER SET utf8mb4 COLLATE utf8mb4_0900_ai_ci NULL DEFAULT NULL COMMENT '备注信息',
  `create_time` datetime(0) NOT NULL DEFAULT CURRENT_TIMESTAMP(0) COMMENT '创建时间',
  `update_time` datetime(0) NOT NULL DEFAULT CURRENT_TIMESTAMP(0) ON UPDATE CURRENT_TIMESTAMP(0) COMMENT '更新时间',
  PRIMARY KEY (`id`) USING BTREE,
  UNIQUE INDEX `username`(`username`) USING BTREE
) ENGINE = InnoDB AUTO_INCREMENT = 3 CHARACTER SET = utf8mb4 COLLATE = utf8mb4_0900_ai_ci COMMENT = '系统用户表' ROW_FORMAT = Dynamic;

-- ----------------------------
-- Records of sys_user
-- ----------------------------
INSERT INTO `sys_user` VALUES (1, 'admin', '超级管理员', 'e83a9095b54c85cc533e0dc66b59eb6ea644aebc631bb13c9cc979cf62454df1', 'f641c7d363db759d71c6e39cca046732', 'admin@unihh.com', '13800138000', NULL, 1, '127.0.0.1', '2026-10-09 09:58:02', '系统预设超级管理员 (密码: admin123)', '2026-09-14 21:20:32', '2026-10-09 09:58:02');
INSERT INTO `sys_user` VALUES (2, 'operator', '工控运维专员', 'fb79c7a14084b53980735dda477fee1f3d8b3e6e75b516f0c05c5c6e4f37eb22', '4d1e0a7f5b3a6c9e2d8f1b4a7c9f2b8c', 'operator@unihh.com', '13900139000', NULL, 1, NULL, NULL, '现场工控与设备采集运维人员 (密码: operator123)', '2026-09-14 21:20:32', '2026-09-14 21:20:32');

-- ----------------------------
-- Table structure for sys_user_role
-- ----------------------------
DROP TABLE IF EXISTS `sys_user_role`;
CREATE TABLE `sys_user_role`  (
  `id` bigint(20) NOT NULL AUTO_INCREMENT COMMENT '自增主键ID',
  `user_id` bigint(20) NOT NULL COMMENT '用户ID',
  `role_id` bigint(20) NOT NULL COMMENT '角色ID',
  PRIMARY KEY (`id`) USING BTREE,
  UNIQUE INDEX `uk_user_role`(`user_id`, `role_id`) USING BTREE,
  INDEX `idx_user_role_user`(`user_id`) USING BTREE,
  INDEX `idx_user_role_role`(`role_id`) USING BTREE
) ENGINE = InnoDB AUTO_INCREMENT = 3 CHARACTER SET = utf8mb4 COLLATE = utf8mb4_0900_ai_ci COMMENT = '用户角色关联表' ROW_FORMAT = Dynamic;

-- ----------------------------
-- Records of sys_user_role
-- ----------------------------
INSERT INTO `sys_user_role` VALUES (1, 1, 1);
INSERT INTO `sys_user_role` VALUES (2, 2, 2);

SET FOREIGN_KEY_CHECKS = 1;
