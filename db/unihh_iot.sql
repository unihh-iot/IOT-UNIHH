

SET SQL_MODE = "NO_AUTO_VALUE_ON_ZERO";
START TRANSACTION;
SET time_zone = "+00:00";


/*!40101 SET @OLD_CHARACTER_SET_CLIENT=@@CHARACTER_SET_CLIENT */;
/*!40101 SET @OLD_CHARACTER_SET_RESULTS=@@CHARACTER_SET_RESULTS */;
/*!40101 SET @OLD_COLLATION_CONNECTION=@@COLLATION_CONNECTION */;
/*!40101 SET NAMES utf8mb4 */;

--
-- 数据库： `unihh_iot`
--

-- --------------------------------------------------------

--
-- 表的结构 `iot_device`
--
DROP TABLE IF EXISTS `iot_device`;
CREATE TABLE `iot_device` (
                              `id` bigint(20) NOT NULL COMMENT '自增主键ID',
                              `device_id` varchar(64) COLLATE utf8mb4_unicode_ci NOT NULL COMMENT '物理设备唯一标识ID (如 dev_001)',
                              `device_name` varchar(128) COLLATE utf8mb4_unicode_ci NOT NULL COMMENT '物理设备显示名称',
                              `model_key` varchar(64) COLLATE utf8mb4_unicode_ci NOT NULL COMMENT '关联物模型Key',
                              `driver_code` varchar(64) COLLATE utf8mb4_unicode_ci NOT NULL COMMENT '绑定协议驱动Code (如 modbus-tcp)',
                              `host` varchar(64) COLLATE utf8mb4_unicode_ci DEFAULT '192.168.1.100' COMMENT '设备通信IP地址/串口号',
                              `port` int(11) DEFAULT '502' COMMENT '设备通信端口号',
                              `connection_params` text COLLATE utf8mb4_unicode_ci COMMENT '驱动拓展连接参数 (JSON格式)',
                              `status` tinyint(4) NOT NULL DEFAULT '1' COMMENT '物理网络通道在线状态 (1:在线 Online, 0:离线 Offline, 2:通信异常 Error)',
                              `acq_status` tinyint(4) NOT NULL DEFAULT '1' COMMENT '数据采集使能状态 (1:采集运行中 Active, 0:手动暂停采集 Paused)',
                              `last_online_time` datetime DEFAULT NULL COMMENT '最后在线通信时间',
                              `create_time` datetime NOT NULL DEFAULT CURRENT_TIMESTAMP COMMENT '设备创建时间',
                              `update_time` datetime NOT NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP COMMENT '设备更新时间'
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci COMMENT='物理设备明细表' ROW_FORMAT=DYNAMIC;

--
-- 转存表中的数据 `iot_device`
--

INSERT INTO `iot_device` (`id`, `device_id`, `device_name`, `model_key`, `driver_code`, `host`, `port`, `connection_params`, `status`, `acq_status`, `last_online_time`, `create_time`, `update_time`) VALUES
                                                                                                                                                                                                           (1, 'DEV_001', '1号高压车间电动机传感器', 'pk_demo', 'modbus-tcp', '192.168.1.100', 502, '{\"host\":\"192.168.1.100\",\"port\":502,\"slaveId\":1}', 0, 0, NULL, '2026-09-09 20:04:04', '2026-09-10 20:23:20'),
                                                                                                                                                                                                           (2, 'DEV_002', '2号储罐压力PLC', 'pk_plc_s7', 's7-driver', '120.48.7.142', 5102, '{\"plcType\":\"S7-1200\",\"rack\":0,\"port\":5102,\"host\":\"120.48.7.142\",\"slot\":1,\"byteOrder\":\"ABCD\"}', 0, 1, '2026-11-04 16:59:38', '2026-09-09 20:04:04', '2026-11-04 17:22:50'),
                                                                                                                                                                                                           (3, 'DEV_003', '3号流量计OPC Server', 'pk_opcua_server', 'opc-ua', '192.168.1.50', 4840, '{\"endpointUrl\":\"opc.tcp://192.168.1.50:4840\",\"securityPolicy\":\"None\"}', 0, 1, NULL, '2026-09-09 20:04:04', '2026-10-09 09:26:49'),
                                                                                                                                                                                                           (4, 'DEV_OGY82W69', 'Modbus设备调试', 'PK_6Z6UNCJ6', 'modbus-tcp', '120.48.7.142', 5002, '{\"slaveId\":1,\"port\":5002,\"host\":\"120.48.7.142\",\"byteOrder\":\"CDAB\"}', 1, 1, '2026-11-04 19:46:48', '2026-09-09 20:32:36', '2026-11-04 19:46:48');

-- --------------------------------------------------------

--
-- 表的结构 `iot_device_point_mapping`
--
DROP TABLE IF EXISTS `iot_device_point_mapping`;
CREATE TABLE `iot_device_point_mapping` (
                                            `id` bigint(20) NOT NULL COMMENT '自增主键ID',
                                            `device_id` varchar(64) COLLATE utf8mb4_unicode_ci NOT NULL COMMENT '关联设备ID',
                                            `model_key` varchar(64) COLLATE utf8mb4_unicode_ci NOT NULL COMMENT '关联物模型Key',
                                            `point_code` varchar(64) COLLATE utf8mb4_unicode_ci NOT NULL COMMENT '物模型点位编码',
                                            `point_name` varchar(128) COLLATE utf8mb4_unicode_ci DEFAULT NULL COMMENT '点位中文名称',
                                            `physical_address` varchar(255) COLLATE utf8mb4_unicode_ci DEFAULT NULL COMMENT '底层物理协议地址',
                                            `data_type` varchar(32) COLLATE utf8mb4_unicode_ci DEFAULT 'DOUBLE' COMMENT '数据类型',
                                            `access_mode` varchar(16) COLLATE utf8mb4_unicode_ci DEFAULT 'R' COMMENT '读写模式',
                                            `scale` double DEFAULT '1' COMMENT '换算缩放系数 Scale',
                                            `offset` double DEFAULT '0' COMMENT '换算零点偏置 Offset',
                                            `remark` varchar(255) COLLATE utf8mb4_unicode_ci DEFAULT NULL COMMENT '备注信息',
                                            `create_time` datetime DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
                                            `update_time` datetime DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP COMMENT '更新时间'
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci COMMENT='设备物模型点位物理地址映射表' ROW_FORMAT=DYNAMIC;

--
-- 转存表中的数据 `iot_device_point_mapping`
--

INSERT INTO `iot_device_point_mapping` (`id`, `device_id`, `model_key`, `point_code`, `point_name`, `physical_address`, `data_type`, `access_mode`, `scale`, `offset`, `remark`, `create_time`, `update_time`) VALUES
                                                                                                                                                                                                                   (1, 'DEV_001', 'pk_demo', 'temperature', '主轴承实时温度', '40001', 'DOUBLE', 'R', 1, 0, NULL, '2026-09-09 20:04:04', '2026-09-09 20:04:04'),
                                                                                                                                                                                                                   (2, 'DEV_001', 'pk_demo', 'humidity', '环境相对湿度', '40003', 'DOUBLE', 'R', 1, 0, NULL, '2026-09-09 20:04:04', '2026-09-09 20:04:04'),
                                                                                                                                                                                                                   (3, 'DEV_001', 'pk_demo', 'pressure', '进气口设定压力', '40005', 'DOUBLE', 'RW', 1, 0, NULL, '2026-09-09 20:04:04', '2026-09-09 20:04:04'),
                                                                                                                                                                                                                   (4, 'DEV_001', 'pk_demo', 'targetSpeed', '变频目标转速', '40007', 'INT', 'RW', 1, 0, NULL, '2026-09-09 20:04:04', '2026-09-09 20:04:04'),
                                                                                                                                                                                                                   (5, 'DEV_001', 'pk_demo', 'autoCruiseMode', '全自动巡航开关', '00001', 'BOOL', 'RW', 1, 0, NULL, '2026-09-09 20:04:04', '2026-09-09 20:04:04'),
                                                                                                                                                                                                                   (6, 'DEV_001', 'pk_demo', 'workMode', '工况模式选择', '40008', 'ENUM', 'RW', 1, 0, NULL, '2026-09-09 20:04:04', '2026-09-09 20:04:04'),
                                                                                                                                                                                                                   (7, 'DEV_OGY82W69', 'PK_6Z6UNCJ6', 'temperture', '温度', '40001', 'FLOAT', 'RW', 1, 0, NULL, '2026-09-09 20:34:06', '2026-09-10 20:27:03'),
                                                                                                                                                                                                                   (9, 'DEV_OGY82W69', 'PK_6Z6UNCJ6', 'model', '模式', '40003', 'ENUM', 'RW', 1, 0, NULL, '2026-09-10 20:22:04', '2026-09-10 20:22:04'),
                                                                                                                                                                                                                   (10, 'DEV_OGY82W69', 'PK_6Z6UNCJ6', 'P3', '点位3', '40004', 'DOUBLE', 'RW', 1, 0, NULL, '2026-09-10 20:22:04', '2026-09-10 20:22:04'),
                                                                                                                                                                                                                   (11, 'DEV_OGY82W69', 'PK_6Z6UNCJ6', 'P4', '点位4', '40008', 'INT', 'RW', 1, 0, NULL, '2026-09-10 20:22:04', '2026-09-10 20:22:04'),
                                                                                                                                                                                                                   (12, 'DEV_002', 'pk_plc_s7', 'plcRunStatus', 'PLC 运行状态', 'DB10.DBX0.0', 'BOOL', 'RW', 1, 0, NULL, '2026-09-11 20:28:20', '2026-09-11 20:31:26'),
                                                                                                                                                                                                                   (13, 'DEV_002', 'pk_plc_s7', 'errorCode', '系统故障代码', 'DB10.DBD2', 'INT', 'RW', 1, 0, NULL, '2026-09-11 20:28:20', '2026-09-11 21:30:11'),
                                                                                                                                                                                                                   (14, 'DEV_002', 'pk_plc_s7', 'targetPressure', '储罐加压目标设定值', 'DB10.DBD10', 'DOUBLE', 'RW', 1, 0, NULL, '2026-09-11 20:28:20', '2026-09-11 21:21:18'),
                                                                                                                                                                                                                   (15, 'DEV_002', 'pk_plc_s7', 'remoteControlCmd', '远程启停遥控开关', 'DB10.DBX0.1', 'BOOL', 'RW', 1, 0, NULL, '2026-09-11 20:28:20', '2026-09-11 20:28:20');

-- --------------------------------------------------------

--
-- 表的结构 `iot_driver_info`
--
DROP TABLE IF EXISTS `iot_driver_info`;
CREATE TABLE `iot_driver_info` (
                                   `id` bigint(20) NOT NULL COMMENT '自增主键ID',
                                   `driver_code` varchar(64) COLLATE utf8mb4_unicode_ci NOT NULL COMMENT '驱动唯一Code (如 modbus-tcp)',
                                   `driver_name` varchar(128) COLLATE utf8mb4_unicode_ci NOT NULL COMMENT '驱动显示名称',
                                   `version` varchar(32) COLLATE utf8mb4_unicode_ci NOT NULL DEFAULT '1.0.0' COMMENT '驱动版本号',
                                   `jar_file_name` varchar(256) COLLATE utf8mb4_unicode_ci DEFAULT NULL COMMENT '驱动Jar包文件名',
                                   `status` tinyint(4) NOT NULL DEFAULT '1' COMMENT '驱动激活状态 (1:启用, 0:停用)',
                                   `description` varchar(512) COLLATE utf8mb4_unicode_ci DEFAULT NULL COMMENT '驱动功能与协议详细描述',
                                   `manifest_yaml` text COLLATE utf8mb4_unicode_ci COMMENT '驱动Manifest YAML定义原文',
                                   `service_url` varchar(256) COLLATE utf8mb4_unicode_ci DEFAULT NULL COMMENT '驱动微服务运行URL地址 (如 http://localhost:28011)',
                                   `create_time` datetime NOT NULL DEFAULT CURRENT_TIMESTAMP COMMENT '注册时间'
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci COMMENT='协议驱动注册信息表' ROW_FORMAT=DYNAMIC;

--
-- 转存表中的数据 `iot_driver_info`
--

INSERT INTO `iot_driver_info` (`id`, `driver_code`, `driver_name`, `version`, `jar_file_name`, `status`, `description`, `manifest_yaml`, `service_url`, `create_time`) VALUES
                                                                                                                                                                           (5, 'modbus-tcp', 'Modbus TCP 工业驱动插件', '2.0.0', 'unihh-driver-modbus.jar', 1, '支持标准 Modbus TCP 保持寄存器与输入寄存器读写 (FC01-FC06/FC15/FC16 功能码)。', 'driverCode: modbus-tcp\ndriverName: Modbus TCP 工业协议驱动\nversion: 2.0.0\nparams:\n  - key: host\n    name: IP地址\n    dataType: STRING\n    required: true\n    defaultValue: \'192.168.1.100\'\n  - key: port\n    name: 通信端口\n    dataType: INT\n    required: true\n    defaultValue: \'502\'\n  - key: slaveId\n    name: 从站 ID\n    dataType: INT\n    required: true\n    defaultValue: \'1\'\n  - key: byteOrder\n    name: 寄存器字节序\n    dataType: SELECT\n    required: false\n    defaultValue: \'CDAB\'\n    description: \'32位/64位整型与浮点数在寄存器中的存储排列顺序 (推荐CDAB)\'\n    options:\n      - label: \'CDAB (工业推荐 / 32位小端字大端字节)\'\n        value: \'CDAB\'\n      - label: \'ABCD (标准大端模式 / Big-Endian)\'\n        value: \'ABCD\'\n      - label: \'DCBA (完全小端模式 / Little-Endian)\'\n        value: \'DCBA\'\n      - label: \'BADC (字节交换模式 / Byte Swap)\'\n        value: \'BADC\'\n      - label: \'AB (16位标准大端)\'\n        value: \'AB\'\n      - label: \'BA (16位字节交换)\'\n        value: \'BA\'\n      - label: \'ABCDEFGH (64位标准大端)\'\n        value: \'ABCDEFGH\'\n      - label: \'GHEFCDAB (64位双字交换)\'\n        value: \'GHEFCDAB\'\n      - label: \'HGFEDCBA (64位完全小端)\'\n        value: \'HGFEDCBA\'\n      - label: \'BADCFEHG (64位字节交换)\'\n        value: \'BADCFEHG\'\naddressRule:\n  ruleType: \'MODBUS\'\n  placeholder: \'例: 40001 (保持寄存器) 或 00001 (线圈)\'\n  startOffset: 40001\n  coilStartOffset: 1\n  typeSteps:\n    INT: 1\n    ENUM: 1\n    INT32: 2\n    FLOAT: 2\n    DOUBLE: 4\n    INT64: 4\n    BOOL: 1\n', 'http://172.24.0.6:28011', '2026-09-09 21:38:41'),
                                                                                                                                                                           (6, 's7-driver', '西门子 S7 纯正工业驱动插件', '2.0.0', NULL, 1, '支持西门子 S7-200 SMART / S7-300 / S7-1200 / S7-1500 PLC DB 块、M 区、I/Q 区数据读写。', 'driverCode: s7-driver\ndriverName: 西门子 S7 工业 PLC 协议驱动\nversion: 2.0.0\nparams:\n  - key: host\n    name: PLC IP地址\n    dataType: STRING\n    required: true\n    defaultValue: \'192.168.1.200\'\n  - key: port\n    name: 端口号\n    dataType: INT\n    required: true\n    defaultValue: \'102\'\n  - key: rack\n    name: 机架号 (Rack)\n    dataType: INT\n    required: true\n    defaultValue: \'0\'\n  - key: slot\n    name: 插槽号 (Slot)\n    dataType: INT\n    required: true\n    defaultValue: \'1\'\n  - key: plcType\n    name: PLC 型号\n    dataType: SELECT\n    required: false\n    defaultValue: \'S7-1200\'\n    description: \'西门子 S7 系列控制器型号 (自动适配机架与插槽)\'\n    options:\n      - label: \'S7-1200 (默认 Rack 0, Slot 1)\'\n        value: \'S7-1200\'\n      - label: \'S7-1500 (默认 Rack 0, Slot 1)\'\n        value: \'S7-1500\'\n      - label: \'S7-300 (默认 Rack 0, Slot 2)\'\n        value: \'S7-300\'\n      - label: \'S7-400 (默认 Rack 0, Slot 2)\'\n        value: \'S7-400\'\n      - label: \'S7-200 SMART (默认 Rack 0, Slot 1)\'\n        value: \'S7-200-SMART\'\n  - key: byteOrder\n    name: 寄存器字节序\n    dataType: SELECT\n    required: false\n    defaultValue: \'ABCD\'\n    description: \'32位/64位整型与浮点数在S7数据块中的存储排列顺序 (推荐ABCD)\'\n    options:\n      - label: \'ABCD (西门子原生标准大端模式 / Big-Endian)\'\n        value: \'ABCD\'\n      - label: \'CDAB (32位小端字大端字节)\'\n        value: \'CDAB\'\n      - label: \'DCBA (完全小端模式 / Little-Endian)\'\n        value: \'DCBA\'\n      - label: \'BADC (字节交换模式 / Byte Swap)\'\n        value: \'BADC\'\n      - label: \'AB (16位标准大端)\'\n        value: \'AB\'\n      - label: \'BA (16位字节交换)\'\n        value: \'BA\'\n      - label: \'ABCDEFGH (64位标准大端)\'\n        value: \'ABCDEFGH\'\n      - label: \'GHEFCDAB (64位双字交换)\'\n        value: \'GHEFCDAB\'\n      - label: \'HGFEDCBA (64位完全小端)\'\n        value: \'HGFEDCBA\'\n      - label: \'BADCFEHG (64位字节交换)\'\n        value: \'BADCFEHG\'\naddressRule:\n  ruleType: \'S7\'\n  placeholder: \'例: DB1.DBD0 (Float/DInt) 或 DB1.DBW2 (Int)\'\n  defaultArea: \'DB1\'\n  startOffset: 0\n  typeSteps:\n    BOOL: 1\n    INT: 2\n    INT32: 4\n    FLOAT: 4\n    DOUBLE: 8\n    INT64: 8\n', 'http://172.24.0.7:28012', '2026-09-11 20:27:21');





-- --------------------------------------------------------

--
-- 表的结构 `iot_thing_model`
--
DROP TABLE IF EXISTS `iot_thing_model`;
CREATE TABLE `iot_thing_model` (
                                   `id` bigint(20) NOT NULL COMMENT '自增主键ID',
                                   `model_key` varchar(64) COLLATE utf8mb4_unicode_ci NOT NULL COMMENT '关联物模型Key',
                                   `identifier` varchar(64) COLLATE utf8mb4_unicode_ci NOT NULL COMMENT '点位/服务标识符 (如 temperature)',
                                   `name` varchar(128) COLLATE utf8mb4_unicode_ci NOT NULL COMMENT '显示名称',
                                   `model_type` varchar(32) COLLATE utf8mb4_unicode_ci NOT NULL DEFAULT 'PROPERTY' COMMENT '物模型类型 (PROPERTY:属性, SERVICE:服务, EVENT:事件)',
                                   `data_type` varchar(32) COLLATE utf8mb4_unicode_ci NOT NULL DEFAULT 'DOUBLE' COMMENT '数据类型 (INT, DOUBLE, STRING, BOOL, ENUM)',
                                   `access_mode` varchar(16) COLLATE utf8mb4_unicode_ci NOT NULL DEFAULT 'R' COMMENT '读写模式 (R:只读, RW:读写)',
                                   `unit` varchar(32) COLLATE utf8mb4_unicode_ci DEFAULT NULL COMMENT '物理测量单位',
                                   `schema_json` text COLLATE utf8mb4_unicode_ci COMMENT '属性/服务扩展Schema定义 (JSON格式)',
                                   `create_time` datetime NOT NULL DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间'
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci COMMENT='物模型元数据定义表' ROW_FORMAT=DYNAMIC;

--
-- 转存表中的数据 `iot_thing_model`
--

INSERT INTO `iot_thing_model` (`id`, `model_key`, `identifier`, `name`, `model_type`, `data_type`, `access_mode`, `unit`, `schema_json`, `create_time`) VALUES
                                                                                                                                                            (1, 'pk_demo', 'temperature', '主轴承实时温度', 'PROPERTY', 'DOUBLE', 'R', '℃', '{\"description\":\"电机主轴承温度传感器只读数据\",\"triggerMode\":\"PERIODIC\",\"sampleFactor\":1}', '2026-09-09 20:04:04'),
                                                                                                                                                            (2, 'pk_demo', 'humidity', '环境相对湿度', 'PROPERTY', 'DOUBLE', 'R', '%RH', '{\"description\":\"电机安装车间湿度\"}', '2026-09-09 20:04:04'),
                                                                                                                                                            (3, 'pk_demo', 'pressure', '进气口设定压力', 'PROPERTY', 'DOUBLE', 'RW', 'kPa', '{\"description\":\"控制风机目标进气口压力\"}', '2026-09-09 20:04:04'),
                                                                                                                                                            (4, 'pk_demo', 'targetSpeed', '变频目标转速', 'PROPERTY', 'INT', 'RW', 'RPM', '{\"description\":\"额定转速设定值\"}', '2026-09-09 20:04:04'),
                                                                                                                                                            (5, 'pk_demo', 'startCoolingFan', '开启/调节冷却风扇', 'SERVICE', 'VOID', 'W', NULL, '{\"description\":\"下发风扇编号及目标风速指令\",\"inputParams\":[{\"pointCode\":\"fanId\",\"pointName\":\"风扇编号\",\"dataType\":\"INT\"},{\"pointCode\":\"speed\",\"pointName\":\"目标转速\",\"dataType\":\"INT\"}]}', '2026-09-09 20:04:04'),
                                                                                                                                                            (6, 'pk_demo', 'setRelay', '继电器开关控制', 'SERVICE', 'VOID', 'W', NULL, '{\"description\":\"控制输出通道闭合/断开\",\"inputParams\":[{\"pointCode\":\"channel\",\"pointName\":\"通道号\",\"dataType\":\"INT\"},{\"pointCode\":\"state\",\"pointName\":\"开关状态\",\"dataType\":\"BOOL\"}]}', '2026-09-09 20:04:04'),
                                                                                                                                                            (7, 'pk_demo', 'OVER_TEMP_WARN', '电机轴承超温自保护', 'EVENT', 'VOID', 'R', NULL, '{\"eventType\":\"ALERT\",\"eventLevel\":\"WARN\",\"description\":\"硬件微控制器检测到轴承温度异常过热主动关断并上报\"}', '2026-09-09 20:04:04'),
                                                                                                                                                            (8, 'pk_plc_s7', 'plcRunStatus', 'PLC 运行状态', 'PROPERTY', 'BOOL', 'RW', '', '{\"description\":\"PLC 当前 RUN/STOP 标志\",\"triggerMode\":\"PERIODIC\",\"sampleFactor\":1}', '2026-09-09 20:04:04'),
                                                                                                                                                            (9, 'pk_plc_s7', 'errorCode', '系统故障代码', 'PROPERTY', 'INT', 'RW', '', '{\"description\":\"故障报警码\",\"triggerMode\":\"PERIODIC\",\"sampleFactor\":1}', '2026-09-09 20:04:04'),
                                                                                                                                                            (10, 'pk_plc_s7', 'targetPressure', '储罐加压目标设定值', 'PROPERTY', 'DOUBLE', 'RW', 'kPa', '{\"description\":\"西门子 PLC 储罐远控加压设定值 (DB1.DBD0)\",\"min\":0.0,\"max\":200.0}', '2026-09-09 20:04:04'),
                                                                                                                                                            (11, 'pk_plc_s7', 'remoteControlCmd', '远程启停遥控开关', 'PROPERTY', 'BOOL', 'RW', '', '{\"description\":\"S7 PLC 远程运行/停止逻辑反写控制 (DB1.DBX4.0)\"}', '2026-09-09 20:04:04'),
                                                                                                                                                            (12, 'pk_plc_s7', 'resetFault', '告警故障一键复位', 'SERVICE', 'VOID', 'W', NULL, '{\"description\":\"清除 PLC 故障状态与历史锁死标志\",\"inputParams\":[{\"pointCode\":\"clearAll\",\"pointName\":\"清除告警\",\"dataType\":\"BOOL\"}]}', '2026-09-09 20:04:04'),
                                                                                                                                                            (13, 'pk_plc_s7', 'OVER_PRESSURE_ALARM', '储罐超压紧急防护报警', 'EVENT', 'VOID', 'R', NULL, '{\"eventType\":\"ALERT\",\"eventLevel\":\"CRITICAL\",\"description\":\"S7 PLC 监测到储罐压力越界触发高压切断系统并上报\"}', '2026-09-09 20:04:04'),
                                                                                                                                                            (14, 'pk_opcua_server', 'flowRate', '管道实时流量', 'PROPERTY', 'DOUBLE', 'R', 'm³/h', '{\"description\":\"OPC UA 流量计实时只读测点\"}', '2026-09-09 20:04:04'),
                                                                                                                                                            (15, 'pk_opcua_server', 'valvePosition', '调节阀目标开度', 'PROPERTY', 'DOUBLE', 'RW', '%', '{\"description\":\"OPC UA 节点 Valve.Pos 远程反写阀门开度 (0 ~ 100%)\",\"min\":0.0,\"max\":100.0}', '2026-09-09 20:04:04'),
                                                                                                                                                            (16, 'pk_opcua_server', 'pumpSwitch', '循环泵远程开关', 'PROPERTY', 'BOOL', 'RW', '', '{\"description\":\"OPC UA 节点 Pump.Switch 逻辑控制开关\"}', '2026-09-09 20:04:04'),
                                                                                                                                                            (17, 'pk_opcua_server', 'calibrateSensor', '传感器零点校准', 'SERVICE', 'VOID', 'W', NULL, '{\"description\":\"下发 OPC UA 传感器校准数值\",\"inputParams\":[{\"pointCode\":\"offset\",\"pointName\":\"零点偏移量\",\"dataType\":\"DOUBLE\"}]}', '2026-09-09 20:04:04'),
                                                                                                                                                            (18, 'pk_opcua_server', 'SENSOR_FAULT_ALARM', '节点传感器通讯异常', 'EVENT', 'VOID', 'R', NULL, '{\"eventType\":\"FAULT\",\"eventLevel\":\"HIGH\",\"description\":\"OPC UA 节点通讯握手超时报警\"}', '2026-09-09 20:04:04'),
                                                                                                                                                            (19, 'PK_6Z6UNCJ6', '_META_', 'Modelbus协议调试', 'META', 'NONE', 'NONE', NULL, '{\"description\":\"调试物模型\",\"category\":\"ROTATING_EQUIP\"}', '2026-09-09 20:30:28'),
                                                                                                                                                            (20, 'PK_6Z6UNCJ6', 'temperture', '温度', 'PROPERTY', 'FLOAT', 'RW', '', '{\"enumOptions\":[{\"value\":0,\"label\":\"关 / OFF / 模式0\"},{\"value\":1,\"label\":\"开 / ON / 模式1\"}],\"address\":\"\",\"min\":0.0,\"max\":500.0,\"description\":\"\",\"triggerMode\":\"PERIODIC\",\"sampleFactor\":1}', '2026-09-09 20:31:09'),
                                                                                                                                                            (21, 'PK_6Z6UNCJ6', 'model', '模式', 'PROPERTY', 'ENUM', 'RW', '', '{\"enumOptions\":[{\"value\":0,\"label\":\"关 / OFF / 手动\"},{\"value\":1,\"label\":\"开 / ON / 自动\"}],\"address\":\"\",\"min\":0.0,\"max\":500.0,\"description\":\"\",\"triggerMode\":\"PERIODIC\",\"sampleFactor\":1}', '2026-09-10 20:18:38'),
                                                                                                                                                            (22, 'PK_6Z6UNCJ6', 'P3', '点位3', 'PROPERTY', 'DOUBLE', 'RW', '', '{\"enumOptions\":[{\"value\":0,\"label\":\"关 / OFF / 模式0\"},{\"value\":1,\"label\":\"开 / ON / 模式1\"}],\"address\":\"\",\"min\":0.0,\"max\":500.0,\"description\":\"\",\"triggerMode\":\"PERIODIC\",\"sampleFactor\":1}', '2026-09-10 20:19:01'),
                                                                                                                                                            (23, 'PK_6Z6UNCJ6', 'P4', '点位4', 'PROPERTY', 'INT', 'RW', '', '{\"enumOptions\":[{\"value\":0,\"label\":\"关 / OFF / 模式0\"},{\"value\":1,\"label\":\"开 / ON / 模式1\"}],\"address\":\"\",\"min\":0.0,\"max\":500.0,\"description\":\"\",\"triggerMode\":\"PERIODIC\",\"sampleFactor\":1}', '2026-09-10 20:19:20'),
                                                                                                                                                            (24, 'PK_6Z6UNCJ6', 'Init_Modbus_Tcp', '初始化Modbus设备', 'SERVICE', 'VOID', 'W', NULL, '{\"inputParams\":[{\"dataType\":\"FLOAT\",\"defaultValue\":0.88,\"enumOptions\":[],\"offset\":0.0,\"pointCode\":\"temperture\",\"pointName\":\"温度\",\"sampleFactor\":1,\"scale\":1.0,\"triggerMode\":\"PERIODIC\"},{\"dataType\":\"INT\",\"defaultValue\":0,\"enumOptions\":[],\"offset\":0.0,\"pointCode\":\"model\",\"pointName\":\"模式\",\"sampleFactor\":1,\"scale\":1.0,\"triggerMode\":\"PERIODIC\"},{\"dataType\":\"DOUBLE\",\"defaultValue\":0.66,\"enumOptions\":[],\"offset\":0.0,\"pointCode\":\"P3\",\"pointName\":\"点位3\",\"sampleFactor\":1,\"scale\":1.0,\"triggerMode\":\"PERIODIC\"},{\"dataType\":\"INT\",\"defaultValue\":0,\"enumOptions\":[],\"offset\":0.0,\"pointCode\":\"P4\",\"pointName\":\"点位4\",\"sampleFactor\":1,\"scale\":1.0,\"triggerMode\":\"PERIODIC\"}],\"description\":\"初始化设备参数\"}', '2026-09-10 20:21:19'),
                                                                                                                                                            (26, 'TM_TUNNEL_SIGNAL', '_META_', '上元隧道-交通信号与三态车道指示器', 'META', 'NONE', 'NONE', NULL, '{\"description\":\"上元隧道-交通信号与三态车道指示器\",\"category\":\"PLC_AUTOMATION\"}', '2026-11-04 11:40:39'),
                                                                                                                                                            (27, 'TM_TUNNEL_FAN', '_META_', '上元隧道-射流风机YF1-1', 'META', 'NONE', 'NONE', NULL, '{\"description\":\"上元隧道-射流风机YF1-1\",\"category\":\"ROTATING_EQUIP\"}', '2026-11-04 11:41:21'),
                                                                                                                                                            (28, 'TM_TUNNEL_LIGHT_R', '_META_', '上元隧道-右洞照明', 'META', 'NONE', 'NONE', NULL, '{\"description\":\"上元隧道-右洞照明\",\"category\":\"PLC_AUTOMATION\"}', '2026-11-04 11:41:53'),
                                                                                                                                                            (29, 'TM_TUNNEL_LIGHT_L', '_META_', '上元隧道-左洞照明', 'META', 'NONE', 'NONE', NULL, '{\"description\":\"上元隧道-左洞照明\",\"category\":\"PLC_AUTOMATION\"}', '2026-11-04 11:42:13'),
                                                                                                                                                            (30, 'TM_TUNNEL_LANE2', '_META_', '上元隧道-两态车道指示器', 'META', 'NONE', 'NONE', NULL, '{\"description\":\"上元隧道-两态车道指示器\",\"category\":\"PLC_AUTOMATION\"}', '2026-11-04 11:42:41'),
                                                                                                                                                            (31, 'TM_TUNNEL_DOOR', '_META_', '上元隧道-车行横洞防火卷帘门', 'META', 'NONE', 'NONE', NULL, '{\"description\":\"上元隧道-车行横洞防火卷帘门\",\"category\":\"PLC_AUTOMATION\"}', '2026-11-04 11:43:00'),
                                                                                                                                                            (32, 'TM_TUNNEL_ENV_READ', '_META_', '上元隧道-洞内环境检测(读数)', 'META', 'NONE', 'NONE', NULL, '{\"description\":\"上元隧道-洞内环境检测(读数)\",\"category\":\"ENV_MONITOR\"}', '2026-11-04 11:43:14'),
                                                                                                                                                            (33, 'TM_TUNNEL_ENV_FAULT', '_META_', '上元隧道-环境检测故障与风向', 'META', 'NONE', 'NONE', NULL, '{\"description\":\"上元隧道-环境检测故障与风向\",\"category\":\"ROTATING_EQUIP\"}', '2026-11-04 11:43:29'),
                                                                                                                                                            (34, 'TM_TUNNEL_ENV_FAULT', 'wind_dir', '风向', 'PROPERTY', 'ENUM', 'R', '', '{\"enumOptions\":[{\"value\":0,\"label\":\"反向\"},{\"value\":1,\"label\":\"正向\"}],\"description\":\"风向指示，地址 403097\",\"triggerMode\":\"PERIODIC\",\"sampleFactor\":1}', '2026-11-04 11:45:42'),
                                                                                                                                                            (35, 'TM_TUNNEL_ENV_FAULT', 'light1_fault', '光强检测器1故障', 'PROPERTY', 'ENUM', 'R', '', '{\"enumOptions\":[{\"value\":1,\"label\":\"断路\"},{\"value\":2,\"label\":\"短路\"}],\"description\":\"光强检测器1故障码，地址 403129\",\"triggerMode\":\"PERIODIC\",\"sampleFactor\":1}', '2026-11-04 11:45:42'),
                                                                                                                                                            (36, 'TM_TUNNEL_ENV_FAULT', 'light2_fault', '光强检测器2故障', 'PROPERTY', 'ENUM', 'R', '', '{\"enumOptions\":[{\"value\":1,\"label\":\"断路\"},{\"value\":2,\"label\":\"短路\"}],\"description\":\"光强检测器2故障码，地址 403130\",\"triggerMode\":\"PERIODIC\",\"sampleFactor\":1}', '2026-11-04 11:45:42'),
                                                                                                                                                            (37, 'TM_TUNNEL_ENV_FAULT', 'co_fault', 'CO检测器故障', 'PROPERTY', 'ENUM', 'R', '', '{\"enumOptions\":[{\"value\":1,\"label\":\"断路\"},{\"value\":2,\"label\":\"短路\"}],\"description\":\"CO检测器1故障码，地址 403131\",\"triggerMode\":\"PERIODIC\",\"sampleFactor\":1}', '2026-11-04 11:45:42'),
                                                                                                                                                            (38, 'TM_TUNNEL_ENV_FAULT', 'vi_fault', 'VI检测器故障', 'PROPERTY', 'ENUM', 'R', '', '{\"enumOptions\":[{\"value\":1,\"label\":\"断路\"},{\"value\":2,\"label\":\"短路\"}],\"description\":\"VI检测器2故障码，地址 403132\",\"triggerMode\":\"PERIODIC\",\"sampleFactor\":1}', '2026-11-04 11:45:42'),
                                                                                                                                                            (39, 'TM_TUNNEL_ENV_FAULT', 'wind_fault', '风速传感器故障', 'PROPERTY', 'ENUM', 'R', '', '{\"enumOptions\":[{\"value\":1,\"label\":\"断路\"},{\"value\":2,\"label\":\"短路\"}],\"description\":\"风速传感器故障码，地址 403133\",\"triggerMode\":\"PERIODIC\",\"sampleFactor\":1}', '2026-11-04 11:45:42'),
                                                                                                                                                            (40, 'TM_TUNNEL_ENV_READ', 'light_out', '洞外光强', 'PROPERTY', 'FLOAT', 'R', 'cd/m²', '{\"description\":\"光强检测器1(洞外)，REAL 32位浮点，地址 403001\",\"triggerMode\":\"PERIODIC\",\"sampleFactor\":1}', '2026-11-04 11:46:13'),
                                                                                                                                                            (41, 'TM_TUNNEL_ENV_READ', 'light_in', '洞内光强', 'PROPERTY', 'FLOAT', 'R', 'cd/m²', '{\"description\":\"光强检测器2(洞内)，REAL 32位浮点，地址 403003\",\"triggerMode\":\"PERIODIC\",\"sampleFactor\":1}', '2026-11-04 11:46:13'),
                                                                                                                                                            (42, 'TM_TUNNEL_ENV_READ', 'co_value', 'CO浓度', 'PROPERTY', 'FLOAT', 'R', 'ppm', '{\"description\":\"CO检测器1，REAL 32位浮点，地址 403005\",\"triggerMode\":\"PERIODIC\",\"sampleFactor\":1}', '2026-11-04 11:46:13'),
                                                                                                                                                            (43, 'TM_TUNNEL_ENV_READ', 'vi_value', '能见度VI', 'PROPERTY', 'FLOAT', 'R', '1/m', '{\"description\":\"VI检测器2，REAL 32位浮点，地址 403007\",\"triggerMode\":\"PERIODIC\",\"sampleFactor\":1}', '2026-11-04 11:46:13'),
                                                                                                                                                            (44, 'TM_TUNNEL_ENV_READ', 'wind_speed', '风速', 'PROPERTY', 'FLOAT', 'R', 'm/s', '{\"description\":\"风速风向传感器，REAL 32位浮点，地址 403009\",\"triggerMode\":\"PERIODIC\",\"sampleFactor\":1}', '2026-11-04 11:46:13'),
                                                                                                                                                            (45, 'TM_TUNNEL_DOOR', 'door_remote', '远程/就地反馈', 'PROPERTY', 'ENUM', 'R', '', '{\"enumOptions\":[{\"value\":0,\"label\":\"就地\"},{\"value\":1,\"label\":\"远程\"}],\"description\":\"卷帘门远程/就地反馈，地址 403092\",\"triggerMode\":\"PERIODIC\",\"sampleFactor\":1}', '2026-11-04 11:46:31'),
                                                                                                                                                            (46, 'TM_TUNNEL_DOOR', 'door_direction', '运行方向反馈', 'PROPERTY', 'ENUM', 'R', '', '{\"enumOptions\":[{\"value\":1,\"label\":\"上升\"},{\"value\":2,\"label\":\"下降\"}],\"description\":\"卷帘门运行方向反馈，地址 403093\",\"triggerMode\":\"PERIODIC\",\"sampleFactor\":1}', '2026-11-04 11:46:31'),
                                                                                                                                                            (47, 'TM_TUNNEL_DOOR', 'door_limit', '限位开关反馈', 'PROPERTY', 'ENUM', 'R', '', '{\"enumOptions\":[{\"value\":1,\"label\":\"上限\"},{\"value\":2,\"label\":\"下限\"}],\"description\":\"卷帘门限位开关反馈，地址 403094\",\"triggerMode\":\"PERIODIC\",\"sampleFactor\":1}', '2026-11-04 11:46:31'),
                                                                                                                                                            (48, 'TM_TUNNEL_DOOR', 'door_fault', '故障反馈', 'PROPERTY', 'ENUM', 'R', '', '{\"enumOptions\":[{\"value\":0,\"label\":\"正常\"},{\"value\":1,\"label\":\"故障\"}],\"description\":\"卷帘门故障反馈，地址 403095\",\"triggerMode\":\"PERIODIC\",\"sampleFactor\":1}', '2026-11-04 11:46:31'),
                                                                                                                                                            (49, 'TM_TUNNEL_DOOR', 'door_light', '照明反馈', 'PROPERTY', 'ENUM', 'R', '', '{\"enumOptions\":[{\"value\":0,\"label\":\"关闭\"},{\"value\":1,\"label\":\"打开\"}],\"description\":\"卷帘门照明反馈，地址 403096\",\"triggerMode\":\"PERIODIC\",\"sampleFactor\":1}', '2026-11-04 11:46:31'),
                                                                                                                                                            (50, 'TM_TUNNEL_DOOR', 'door_ctrl', '卷帘门升降控制', 'PROPERTY', 'ENUM', 'RW', '', '{\"enumOptions\":[{\"value\":1,\"label\":\"上升\"},{\"value\":2,\"label\":\"下降\"},{\"value\":3,\"label\":\"停止\"}],\"description\":\"卷帘门升降控制，地址 403128\",\"triggerMode\":\"PERIODIC\",\"sampleFactor\":1}', '2026-11-04 11:46:31'),
                                                                                                                                                            (51, 'TM_TUNNEL_LANE2', 'lane2_1_status', '两态车道指示器1状态反馈', 'PROPERTY', 'ENUM', 'R', '', '{\"enumOptions\":[{\"value\":1,\"label\":\"正向通行\"},{\"value\":2,\"label\":\"反向通行\"},{\"value\":3,\"label\":\"禁止通行\"}],\"description\":\"两态车道指示器1 反馈，地址 403082\",\"triggerMode\":\"PERIODIC\",\"sampleFactor\":1}', '2026-11-04 11:46:50'),
                                                                                                                                                            (52, 'TM_TUNNEL_LANE2', 'lane2_1_ctl', '两态车道指示器1控制', 'PROPERTY', 'ENUM', 'RW', '', '{\"enumOptions\":[{\"value\":1,\"label\":\"正向通行\"},{\"value\":2,\"label\":\"反向通行\"},{\"value\":3,\"label\":\"禁止通行\"}],\"description\":\"两态车道指示器1 控制，地址 403123\",\"triggerMode\":\"PERIODIC\",\"sampleFactor\":1}', '2026-11-04 11:46:50'),
                                                                                                                                                            (53, 'TM_TUNNEL_LANE2', 'lane2_2_status', '两态车道指示器2状态反馈', 'PROPERTY', 'ENUM', 'R', '', '{\"enumOptions\":[{\"value\":1,\"label\":\"正向通行\"},{\"value\":2,\"label\":\"反向通行\"},{\"value\":3,\"label\":\"禁止通行\"}],\"description\":\"两态车道指示器2 反馈，地址 403084\",\"triggerMode\":\"PERIODIC\",\"sampleFactor\":1}', '2026-11-04 11:46:51'),
                                                                                                                                                            (54, 'TM_TUNNEL_LANE2', 'lane2_2_ctl', '两态车道指示器2控制', 'PROPERTY', 'ENUM', 'RW', '', '{\"enumOptions\":[{\"value\":1,\"label\":\"正向通行\"},{\"value\":2,\"label\":\"反向通行\"},{\"value\":3,\"label\":\"禁止通行\"}],\"description\":\"两态车道指示器2 控制，地址 403124\",\"triggerMode\":\"PERIODIC\",\"sampleFactor\":1}', '2026-11-04 11:46:51'),
                                                                                                                                                            (55, 'TM_TUNNEL_LANE2', 'lane2_3_status', '两态车道指示器3状态反馈', 'PROPERTY', 'ENUM', 'R', '', '{\"enumOptions\":[{\"value\":1,\"label\":\"正向通行\"},{\"value\":2,\"label\":\"反向通行\"},{\"value\":3,\"label\":\"禁止通行\"}],\"description\":\"两态车道指示器3 反馈，地址 403086\",\"triggerMode\":\"PERIODIC\",\"sampleFactor\":1}', '2026-11-04 11:46:51'),
                                                                                                                                                            (56, 'TM_TUNNEL_LANE2', 'lane2_3_ctl', '两态车道指示器3控制', 'PROPERTY', 'ENUM', 'RW', '', '{\"enumOptions\":[{\"value\":1,\"label\":\"正向通行\"},{\"value\":2,\"label\":\"反向通行\"},{\"value\":3,\"label\":\"禁止通行\"}],\"description\":\"两态车道指示器3 控制，地址 403125\",\"triggerMode\":\"PERIODIC\",\"sampleFactor\":1}', '2026-11-04 11:46:51'),
                                                                                                                                                            (57, 'TM_TUNNEL_LANE2', 'lane2_4_status', '两态车道指示器4状态反馈', 'PROPERTY', 'ENUM', 'R', '', '{\"enumOptions\":[{\"value\":1,\"label\":\"正向通行\"},{\"value\":2,\"label\":\"反向通行\"},{\"value\":3,\"label\":\"禁止通行\"}],\"description\":\"两态车道指示器4 反馈，地址 403088\",\"triggerMode\":\"PERIODIC\",\"sampleFactor\":1}', '2026-11-04 11:46:51'),
                                                                                                                                                            (58, 'TM_TUNNEL_LANE2', 'lane2_4_ctl', '两态车道指示器4控制', 'PROPERTY', 'ENUM', 'RW', '', '{\"enumOptions\":[{\"value\":1,\"label\":\"正向通行\"},{\"value\":2,\"label\":\"反向通行\"},{\"value\":3,\"label\":\"禁止通行\"}],\"description\":\"两态车道指示器4 控制，地址 403126\",\"triggerMode\":\"PERIODIC\",\"sampleFactor\":1}', '2026-11-04 11:46:51'),
                                                                                                                                                            (59, 'TM_TUNNEL_LANE2', 'lane2_5_status', '两态车道指示器5状态反馈', 'PROPERTY', 'ENUM', 'R', '', '{\"enumOptions\":[{\"value\":1,\"label\":\"正向通行\"},{\"value\":2,\"label\":\"反向通行\"},{\"value\":3,\"label\":\"禁止通行\"}],\"description\":\"两态车道指示器5 反馈，地址 403090\",\"triggerMode\":\"PERIODIC\",\"sampleFactor\":1}', '2026-11-04 11:46:51'),
                                                                                                                                                            (60, 'TM_TUNNEL_LANE2', 'lane2_5_ctl', '两态车道指示器5控制', 'PROPERTY', 'ENUM', 'RW', '', '{\"enumOptions\":[{\"value\":1,\"label\":\"正向通行\"},{\"value\":2,\"label\":\"反向通行\"},{\"value\":3,\"label\":\"禁止通行\"}],\"description\":\"两态车道指示器5 控制，地址 403127\",\"triggerMode\":\"PERIODIC\",\"sampleFactor\":1}', '2026-11-04 11:46:51'),
                                                                                                                                                            (61, 'TM_TUNNEL_LIGHT_L', 'zjq01_fb', 'ZJQ01入口加强照明1开关反馈', 'PROPERTY', 'ENUM', 'R', '', '{\"enumOptions\":[{\"value\":1,\"label\":\"开\"},{\"value\":2,\"label\":\"关\"}],\"description\":\"ZJQ01入口加强照明1 状态反馈，地址 403074\",\"triggerMode\":\"PERIODIC\",\"sampleFactor\":1}', '2026-11-04 11:47:18'),
                                                                                                                                                            (62, 'TM_TUNNEL_LIGHT_L', 'zjq01_ctl', 'ZJQ01入口加强照明1开关控制', 'PROPERTY', 'ENUM', 'RW', '', '{\"enumOptions\":[{\"value\":1,\"label\":\"开\"},{\"value\":2,\"label\":\"关\"}],\"description\":\"ZJQ01入口加强照明1 开关控制，地址 403115\",\"triggerMode\":\"PERIODIC\",\"sampleFactor\":1}', '2026-11-04 11:47:18'),
                                                                                                                                                            (63, 'TM_TUNNEL_LIGHT_L', 'zjq02_fb', 'ZJQ02入口加强照明2开关反馈', 'PROPERTY', 'ENUM', 'R', '', '{\"enumOptions\":[{\"value\":1,\"label\":\"开\"},{\"value\":2,\"label\":\"关\"}],\"description\":\"ZJQ02入口加强照明2 状态反馈，地址 403075\",\"triggerMode\":\"PERIODIC\",\"sampleFactor\":1}', '2026-11-04 11:47:18'),
                                                                                                                                                            (64, 'TM_TUNNEL_LIGHT_L', 'zjq02_ctl', 'ZJQ02入口加强照明2开关控制', 'PROPERTY', 'ENUM', 'RW', '', '{\"enumOptions\":[{\"value\":1,\"label\":\"开\"},{\"value\":2,\"label\":\"关\"}],\"description\":\"ZJQ02入口加强照明2 开关控制，地址 403116\",\"triggerMode\":\"PERIODIC\",\"sampleFactor\":1}', '2026-11-04 11:47:18'),
                                                                                                                                                            (65, 'TM_TUNNEL_LIGHT_L', 'zgd01_fb', 'ZGD01入口过渡照明1开关反馈', 'PROPERTY', 'ENUM', 'R', '', '{\"enumOptions\":[{\"value\":1,\"label\":\"开\"},{\"value\":2,\"label\":\"关\"}],\"description\":\"ZGD01入口过渡照明1 状态反馈，地址 403076\",\"triggerMode\":\"PERIODIC\",\"sampleFactor\":1}', '2026-11-04 11:47:18'),
                                                                                                                                                            (66, 'TM_TUNNEL_LIGHT_L', 'zgd01_ctl', 'ZGD01入口过渡照明1开关控制', 'PROPERTY', 'ENUM', 'RW', '', '{\"enumOptions\":[{\"value\":1,\"label\":\"开\"},{\"value\":2,\"label\":\"关\"}],\"description\":\"ZGD01入口过渡照明1 开关控制，地址 403117\",\"triggerMode\":\"PERIODIC\",\"sampleFactor\":1}', '2026-11-04 11:47:18'),
                                                                                                                                                            (67, 'TM_TUNNEL_LIGHT_L', 'zgd02_fb', 'ZGD02入口过渡照明2开关反馈', 'PROPERTY', 'ENUM', 'R', '', '{\"enumOptions\":[{\"value\":1,\"label\":\"开\"},{\"value\":2,\"label\":\"关\"}],\"description\":\"ZGD02入口过渡照明2 状态反馈，地址 403077\",\"triggerMode\":\"PERIODIC\",\"sampleFactor\":1}', '2026-11-04 11:47:18'),
                                                                                                                                                            (68, 'TM_TUNNEL_LIGHT_L', 'zgd02_ctl', 'ZGD02入口过渡照明2开关控制', 'PROPERTY', 'ENUM', 'RW', '', '{\"enumOptions\":[{\"value\":1,\"label\":\"开\"},{\"value\":2,\"label\":\"关\"}],\"description\":\"ZGD02入口过渡照明2 开关控制，地址 403118\",\"triggerMode\":\"PERIODIC\",\"sampleFactor\":1}', '2026-11-04 11:47:18'),
                                                                                                                                                            (69, 'TM_TUNNEL_LIGHT_L', 'zjb_fb', 'ZJB基本照明开关反馈', 'PROPERTY', 'ENUM', 'R', '', '{\"enumOptions\":[{\"value\":1,\"label\":\"开\"},{\"value\":2,\"label\":\"关\"}],\"description\":\"ZJB基本照明 状态反馈，地址 403078\",\"triggerMode\":\"PERIODIC\",\"sampleFactor\":1}', '2026-11-04 11:47:18'),
                                                                                                                                                            (70, 'TM_TUNNEL_LIGHT_L', 'zjb_ctl', 'ZJB基本照明开关控制', 'PROPERTY', 'ENUM', 'RW', '', '{\"enumOptions\":[{\"value\":1,\"label\":\"开\"},{\"value\":2,\"label\":\"关\"}],\"description\":\"ZJB基本照明 开关控制，地址 403119\",\"triggerMode\":\"PERIODIC\",\"sampleFactor\":1}', '2026-11-04 11:47:18'),
                                                                                                                                                            (71, 'TM_TUNNEL_LIGHT_L', 'zyj_fb', 'ZYJ应急照明开关反馈', 'PROPERTY', 'ENUM', 'R', '', '{\"enumOptions\":[{\"value\":1,\"label\":\"开\"},{\"value\":2,\"label\":\"关\"}],\"description\":\"ZYJ应急照明 状态反馈，地址 403079\",\"triggerMode\":\"PERIODIC\",\"sampleFactor\":1}', '2026-11-04 11:47:18'),
                                                                                                                                                            (72, 'TM_TUNNEL_LIGHT_L', 'zyj_ctl', 'ZYJ应急照明开关控制', 'PROPERTY', 'ENUM', 'RW', '', '{\"enumOptions\":[{\"value\":1,\"label\":\"开\"},{\"value\":2,\"label\":\"关\"}],\"description\":\"ZYJ应急照明 开关控制，地址 403120\",\"triggerMode\":\"PERIODIC\",\"sampleFactor\":1}', '2026-11-04 11:47:18'),
                                                                                                                                                            (73, 'TM_TUNNEL_LIGHT_L', 'zcq01_fb', 'ZCQ01出口加强照明1开关反馈', 'PROPERTY', 'ENUM', 'R', '', '{\"enumOptions\":[{\"value\":1,\"label\":\"开\"},{\"value\":2,\"label\":\"关\"}],\"description\":\"ZCQ01出口加强照明1 状态反馈，地址 403080\",\"triggerMode\":\"PERIODIC\",\"sampleFactor\":1}', '2026-11-04 11:47:19'),
                                                                                                                                                            (74, 'TM_TUNNEL_LIGHT_L', 'zcq01_ctl', 'ZCQ01出口加强照明1开关控制', 'PROPERTY', 'ENUM', 'RW', '', '{\"enumOptions\":[{\"value\":1,\"label\":\"开\"},{\"value\":2,\"label\":\"关\"}],\"description\":\"ZCQ01出口加强照明1 开关控制，地址 403121\",\"triggerMode\":\"PERIODIC\",\"sampleFactor\":1}', '2026-11-04 11:47:19'),
                                                                                                                                                            (75, 'TM_TUNNEL_LIGHT_L', 'zcq02_fb', 'ZCQ02出口加强照明2开关反馈', 'PROPERTY', 'ENUM', 'R', '', '{\"enumOptions\":[{\"value\":1,\"label\":\"开\"},{\"value\":2,\"label\":\"关\"}],\"description\":\"ZCQ02出口加强照明2 状态反馈，地址 403081\",\"triggerMode\":\"PERIODIC\",\"sampleFactor\":1}', '2026-11-04 11:47:19'),
                                                                                                                                                            (76, 'TM_TUNNEL_LIGHT_L', 'zcq02_ctl', 'ZCQ02出口加强照明2开关控制', 'PROPERTY', 'ENUM', 'RW', '', '{\"enumOptions\":[{\"value\":1,\"label\":\"开\"},{\"value\":2,\"label\":\"关\"}],\"description\":\"ZCQ02出口加强照明2 开关控制，地址 403122\",\"triggerMode\":\"PERIODIC\",\"sampleFactor\":1}', '2026-11-04 11:47:19'),
                                                                                                                                                            (77, 'TM_TUNNEL_FAN', 'fan_remote', '远程/就地指示', 'PROPERTY', 'ENUM', 'R', '', '{\"enumOptions\":[{\"value\":1,\"label\":\"就地\"},{\"value\":2,\"label\":\"远程\"}],\"description\":\"射流风机YF1-1 远程/就地指示，地址 403054\",\"triggerMode\":\"PERIODIC\",\"sampleFactor\":1}', '2026-11-04 11:50:23'),
                                                                                                                                                            (78, 'TM_TUNNEL_FAN', 'fan_direction', '运行方向', 'PROPERTY', 'ENUM', 'R', '', '{\"enumOptions\":[{\"value\":1,\"label\":\"正转\"},{\"value\":2,\"label\":\"反转\"}],\"description\":\"射流风机YF1-1 运行方向，地址 403055\",\"triggerMode\":\"PERIODIC\",\"sampleFactor\":1}', '2026-11-04 11:50:23'),
                                                                                                                                                            (79, 'TM_TUNNEL_FAN', 'fan_stop', '停止指示', 'PROPERTY', 'ENUM', 'R', '', '{\"enumOptions\":[{\"value\":0,\"label\":\"运行\"},{\"value\":1,\"label\":\"停止\"}],\"description\":\"射流风机YF1-1 启停指示，地址 403056\",\"triggerMode\":\"PERIODIC\",\"sampleFactor\":1}', '2026-11-04 11:50:23'),
                                                                                                                                                            (80, 'TM_TUNNEL_FAN', 'fan_fault', '故障反馈', 'PROPERTY', 'ENUM', 'R', '', '{\"enumOptions\":[{\"value\":0,\"label\":\"正常\"},{\"value\":1,\"label\":\"故障\"}],\"description\":\"射流风机YF1-1 故障反馈，地址 403057\",\"triggerMode\":\"PERIODIC\",\"sampleFactor\":1}', '2026-11-04 11:50:23'),
                                                                                                                                                            (81, 'TM_TUNNEL_FAN', 'fan_ctrl', '风机启停控制', 'PROPERTY', 'ENUM', 'RW', '', '{\"enumOptions\":[{\"value\":1,\"label\":\"正转\"},{\"value\":2,\"label\":\"反转\"},{\"value\":3,\"label\":\"停止\"}],\"description\":\"射流风机YF1-1 启停控制，地址 403103\",\"triggerMode\":\"PERIODIC\",\"sampleFactor\":1}', '2026-11-04 11:50:23'),
                                                                                                                                                            (82, 'TM_TUNNEL_SIGNAL', 'tls_status', '交通信号灯状态反馈', 'PROPERTY', 'ENUM', 'R', '', '{\"enumOptions\":[{\"value\":1,\"label\":\"红灯\"},{\"value\":2,\"label\":\"绿灯\"},{\"value\":3,\"label\":\"黄灯\"},{\"value\":4,\"label\":\"转向灯\"}],\"description\":\"交通信号灯反馈，地址 403051\",\"triggerMode\":\"PERIODIC\",\"sampleFactor\":1}', '2026-11-04 11:50:37'),
                                                                                                                                                            (83, 'TM_TUNNEL_SIGNAL', 'tls_ctrl', '交通信号灯控制', 'PROPERTY', 'ENUM', 'RW', '', '{\"enumOptions\":[{\"value\":1,\"label\":\"红灯\"},{\"value\":2,\"label\":\"绿灯\"},{\"value\":3,\"label\":\"黄灯\"},{\"value\":4,\"label\":\"转向灯\"}],\"description\":\"交通信号灯控制，地址 403101\",\"triggerMode\":\"PERIODIC\",\"sampleFactor\":1}', '2026-11-04 11:50:37'),
                                                                                                                                                            (84, 'TM_TUNNEL_SIGNAL', 'lane3_status', '三态车道指示器状态反馈', 'PROPERTY', 'ENUM', 'R', '', '{\"enumOptions\":[{\"value\":1,\"label\":\"正向通行\"},{\"value\":2,\"label\":\"反向通行\"},{\"value\":3,\"label\":\"禁止通行\"},{\"value\":4,\"label\":\"转向通行\"}],\"description\":\"三态车道指示器反馈，地址 403052\",\"triggerMode\":\"PERIODIC\",\"sampleFactor\":1}', '2026-11-04 11:50:37'),
                                                                                                                                                            (85, 'TM_TUNNEL_SIGNAL', 'lane3_ctrl', '三态车道指示器控制', 'PROPERTY', 'ENUM', 'RW', '', '{\"enumOptions\":[{\"value\":1,\"label\":\"正向通行\"},{\"value\":2,\"label\":\"反向通行\"},{\"value\":3,\"label\":\"禁止通行\"},{\"value\":4,\"label\":\"转向通行\"}],\"description\":\"三态车道指示器控制，地址 403102\",\"triggerMode\":\"PERIODIC\",\"sampleFactor\":1}', '2026-11-04 11:50:37'),
                                                                                                                                                            (102, 'TM_TUNNEL_LIGHT_R', 'yjq01_fb', 'YJQ01入口加强照明1开关反馈', 'PROPERTY', 'ENUM', 'R', '', '{\"enumOptions\":[{\"value\":1,\"label\":\"开\"},{\"value\":2,\"label\":\"关\"}],\"description\":\"YJQ01入口加强照明1 状态反馈，地址 403066\",\"triggerMode\":\"PERIODIC\",\"sampleFactor\":1}', '2026-11-04 13:46:52'),
                                                                                                                                                            (103, 'TM_TUNNEL_LIGHT_R', 'yjq01_ctl', 'YJQ01入口加强照明1开关控制', 'PROPERTY', 'ENUM', 'RW', '', '{\"enumOptions\":[{\"value\":1,\"label\":\"开\"},{\"value\":2,\"label\":\"关\"}],\"description\":\"YJQ01入口加强照明1 开关控制，地址 403107\",\"triggerMode\":\"PERIODIC\",\"sampleFactor\":1}', '2026-11-04 13:46:52'),
                                                                                                                                                            (104, 'TM_TUNNEL_LIGHT_R', 'yjq02_fb', 'YJQ02入口加强照明2开关反馈', 'PROPERTY', 'ENUM', 'R', '', '{\"enumOptions\":[{\"value\":1,\"label\":\"开\"},{\"value\":2,\"label\":\"关\"}],\"description\":\"YJQ02入口加强照明2 状态反馈，地址 403067\",\"triggerMode\":\"PERIODIC\",\"sampleFactor\":1}', '2026-11-04 13:46:52'),
                                                                                                                                                            (105, 'TM_TUNNEL_LIGHT_R', 'yjq02_ctl', 'YJQ02入口加强照明2开关控制', 'PROPERTY', 'ENUM', 'RW', '', '{\"enumOptions\":[{\"value\":1,\"label\":\"开\"},{\"value\":2,\"label\":\"关\"}],\"description\":\"YJQ02入口加强照明2 开关控制，地址 403108\",\"triggerMode\":\"PERIODIC\",\"sampleFactor\":1}', '2026-11-04 13:46:52'),
                                                                                                                                                            (106, 'TM_TUNNEL_LIGHT_R', 'ygd01_fb', 'YGD01入口过渡照明1开关反馈', 'PROPERTY', 'ENUM', 'R', '', '{\"enumOptions\":[{\"value\":1,\"label\":\"开\"},{\"value\":2,\"label\":\"关\"}],\"description\":\"YGD01入口过渡照明1 状态反馈，地址 403068\",\"triggerMode\":\"PERIODIC\",\"sampleFactor\":1}', '2026-11-04 13:46:52'),
                                                                                                                                                            (107, 'TM_TUNNEL_LIGHT_R', 'ygd01_ctl', 'YGD01入口过渡照明1开关控制', 'PROPERTY', 'ENUM', 'RW', '', '{\"enumOptions\":[{\"value\":1,\"label\":\"开\"},{\"value\":2,\"label\":\"关\"}],\"description\":\"YGD01入口过渡照明1 开关控制，地址 403109\",\"triggerMode\":\"PERIODIC\",\"sampleFactor\":1}', '2026-11-04 13:46:52'),
                                                                                                                                                            (108, 'TM_TUNNEL_LIGHT_R', 'ygd02_fb', 'YGD02入口过渡照明2开关反馈', 'PROPERTY', 'ENUM', 'R', '', '{\"enumOptions\":[{\"value\":1,\"label\":\"开\"},{\"value\":2,\"label\":\"关\"}],\"description\":\"YGD02入口过渡照明2 状态反馈，地址 403069\",\"triggerMode\":\"PERIODIC\",\"sampleFactor\":1}', '2026-11-04 13:46:52'),
                                                                                                                                                            (109, 'TM_TUNNEL_LIGHT_R', 'ygd02_ctl', 'YGD02入口过渡照明2开关控制', 'PROPERTY', 'ENUM', 'RW', '', '{\"enumOptions\":[{\"value\":1,\"label\":\"开\"},{\"value\":2,\"label\":\"关\"}],\"description\":\"YGD02入口过渡照明2 开关控制，地址 403110\",\"triggerMode\":\"PERIODIC\",\"sampleFactor\":1}', '2026-11-04 13:46:52'),
                                                                                                                                                            (110, 'TM_TUNNEL_LIGHT_R', 'yjb_fb', 'YJB基本照明开关反馈', 'PROPERTY', 'ENUM', 'R', '', '{\"enumOptions\":[{\"value\":1,\"label\":\"开\"},{\"value\":2,\"label\":\"关\"}],\"description\":\"YJB基本照明 状态反馈，地址 403070\",\"triggerMode\":\"PERIODIC\",\"sampleFactor\":1}', '2026-11-04 13:46:52'),
                                                                                                                                                            (111, 'TM_TUNNEL_LIGHT_R', 'yjb_ctl', 'YJB基本照明开关控制', 'PROPERTY', 'ENUM', 'RW', '', '{\"enumOptions\":[{\"value\":1,\"label\":\"开\"},{\"value\":2,\"label\":\"关\"}],\"description\":\"YJB基本照明 开关控制，地址 403111\",\"triggerMode\":\"PERIODIC\",\"sampleFactor\":1}', '2026-11-04 13:46:52'),
                                                                                                                                                            (112, 'TM_TUNNEL_LIGHT_R', 'yyj_fb', 'YYJ应急照明开关反馈', 'PROPERTY', 'ENUM', 'R', '', '{\"enumOptions\":[{\"value\":1,\"label\":\"开\"},{\"value\":2,\"label\":\"关\"}],\"description\":\"YYJ应急照明 状态反馈，地址 403071\",\"triggerMode\":\"PERIODIC\",\"sampleFactor\":1}', '2026-11-04 13:46:52'),
                                                                                                                                                            (113, 'TM_TUNNEL_LIGHT_R', 'yyj_ctl', 'YYJ应急照明开关控制', 'PROPERTY', 'ENUM', 'RW', '', '{\"enumOptions\":[{\"value\":1,\"label\":\"开\"},{\"value\":2,\"label\":\"关\"}],\"description\":\"YYJ应急照明 开关控制，地址 403112\",\"triggerMode\":\"PERIODIC\",\"sampleFactor\":1}', '2026-11-04 13:46:52'),
                                                                                                                                                            (114, 'TM_TUNNEL_LIGHT_R', 'ycq01_fb', 'YCQ01出口加强照明1开关反馈', 'PROPERTY', 'ENUM', 'R', '', '{\"enumOptions\":[{\"value\":1,\"label\":\"开\"},{\"value\":2,\"label\":\"关\"}],\"description\":\"YCQ01出口加强照明1 状态反馈，地址 403072\",\"triggerMode\":\"PERIODIC\",\"sampleFactor\":1}', '2026-11-04 13:46:52'),
                                                                                                                                                            (115, 'TM_TUNNEL_LIGHT_R', 'ycq01_ctl', 'YCQ01出口加强照明1开关控制', 'PROPERTY', 'ENUM', 'RW', '', '{\"enumOptions\":[{\"value\":1,\"label\":\"开\"},{\"value\":2,\"label\":\"关\"}],\"description\":\"YCQ01出口加强照明1 开关控制，地址 403113\",\"triggerMode\":\"PERIODIC\",\"sampleFactor\":1}', '2026-11-04 13:46:52'),
                                                                                                                                                            (116, 'TM_TUNNEL_LIGHT_R', 'ycq02_fb', 'YCQ02出口加强照明2开关反馈', 'PROPERTY', 'ENUM', 'R', '', '{\"enumOptions\":[{\"value\":1,\"label\":\"开\"},{\"value\":2,\"label\":\"关\"}],\"description\":\"YCQ02出口加强照明2 状态反馈，地址 403073\",\"triggerMode\":\"PERIODIC\",\"sampleFactor\":1}', '2026-11-04 13:46:52'),
                                                                                                                                                            (117, 'TM_TUNNEL_LIGHT_R', 'ycq02_ctl', 'YCQ02出口加强照明2开关控制', 'PROPERTY', 'ENUM', 'RW', '', '{\"enumOptions\":[{\"value\":1,\"label\":\"开\"},{\"value\":2,\"label\":\"关\"}],\"description\":\"YCQ02出口加强照明2 开关控制，地址 403114\",\"triggerMode\":\"PERIODIC\",\"sampleFactor\":1}', '2026-11-04 13:46:52');

-- --------------------------------------------------------

--
-- 表的结构 `sys_dict_data`
--
DROP TABLE IF EXISTS `sys_dict_data`;
CREATE TABLE `sys_dict_data` (
                                 `id` bigint(20) NOT NULL COMMENT '自增字典数据ID',
                                 `dict_type` varchar(100) COLLATE utf8mb4_unicode_ci NOT NULL COMMENT '字典类型 (关联 sys_dict_type.dict_type)',
                                 `dict_label` varchar(100) COLLATE utf8mb4_unicode_ci NOT NULL COMMENT '字典标签名称',
                                 `dict_value` varchar(100) COLLATE utf8mb4_unicode_ci NOT NULL COMMENT '字典键值',
                                 `sort_order` int(11) NOT NULL DEFAULT '0' COMMENT '字典排序',
                                 `css_class` varchar(100) COLLATE utf8mb4_unicode_ci DEFAULT NULL COMMENT '样式属性',
                                 `list_class` varchar(100) COLLATE utf8mb4_unicode_ci DEFAULT NULL COMMENT '表格回显样式',
                                 `is_default` tinyint(4) NOT NULL DEFAULT '0' COMMENT '是否默认 (1:是, 0:否)',
                                 `status` tinyint(4) NOT NULL DEFAULT '1' COMMENT '状态 (1:正常, 0:停用)',
                                 `remark` varchar(500) COLLATE utf8mb4_unicode_ci DEFAULT NULL COMMENT '备注',
                                 `create_time` datetime NOT NULL DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
                                 `update_time` datetime NOT NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP COMMENT '更新时间'
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci COMMENT='系统字典键值数据表' ROW_FORMAT=DYNAMIC;

--
-- 转存表中的数据 `sys_dict_data`
--

INSERT INTO `sys_dict_data` (`id`, `dict_type`, `dict_label`, `dict_value`, `sort_order`, `css_class`, `list_class`, `is_default`, `status`, `remark`, `create_time`, `update_time`) VALUES
                                                                                                                                                                                         (1, 'sys_user_status', '正常启用', '1', 1, 'el-tag--success', 'success', 1, 1, '用户账号正常使用', '2026-09-14 21:20:32', '2026-09-14 21:20:32'),
                                                                                                                                                                                         (2, 'sys_user_status', '停用冻结', '0', 2, 'el-tag--danger', 'danger', 0, 1, '账号已被管理员停用', '2026-09-14 21:20:32', '2026-09-14 21:20:32'),
                                                                                                                                                                                         (3, 'sys_device_status', '在线运行', '1', 1, 'el-tag--success', 'success', 1, 1, '通道网络心跳正常', '2026-09-14 21:20:32', '2026-09-14 21:20:32'),
                                                                                                                                                                                         (4, 'sys_device_status', '未连接/离线', '0', 2, 'el-tag--warning', 'warning', 0, 1, 'TCP/串口未建立连接', '2026-09-14 21:20:32', '2026-09-14 21:20:32'),
                                                                                                                                                                                         (5, 'sys_device_status', '连接异常', '2', 3, 'el-tag--danger', 'danger', 0, 1, '通信超时或数据校验失败', '2026-09-14 21:20:32', '2026-09-14 21:20:32'),
                                                                                                                                                                                         (6, 'iot_alarm_level', '普通提示', 'INFO', 1, 'el-tag--info', 'info', 0, 1, '常规参数越界提示', '2026-09-14 21:20:32', '2026-09-14 21:20:32'),
                                                                                                                                                                                         (7, 'iot_alarm_level', '一般告警', 'WARN', 2, 'el-tag--warning', 'warning', 1, 1, '非致命异常', '2026-09-14 21:20:32', '2026-09-14 21:20:32'),
                                                                                                                                                                                         (8, 'iot_alarm_level', '严重超限', 'HIGH', 3, 'el-tag--danger', 'danger', 0, 1, '工艺参数严重超标', '2026-09-14 21:20:32', '2026-09-14 21:20:32'),
                                                                                                                                                                                         (9, 'iot_alarm_level', '紧急险情', 'CRITICAL', 4, 'el-tag--danger', 'danger', 0, 1, '触发设备急停保护', '2026-09-14 21:20:32', '2026-09-14 21:20:32'),
                                                                                                                                                                                         (10, 'iot_data_type', '双精度浮点 (Double)', 'DOUBLE', 1, '', 'primary', 1, 1, '64位 IEEE 754 浮点', '2026-09-14 21:20:32', '2026-09-14 21:20:32'),
                                                                                                                                                                                         (11, 'iot_data_type', '整型 (Integer)', 'INT', 2, '', 'primary', 0, 1, '16位/32位整型', '2026-09-14 21:20:32', '2026-09-14 21:20:32'),
                                                                                                                                                                                         (12, 'iot_data_type', '布尔开关 (Boolean)', 'BOOL', 3, '', 'success', 0, 1, '0 或 1 逻辑开关', '2026-09-14 21:20:32', '2026-09-14 21:20:32'),
                                                                                                                                                                                         (13, 'iot_data_type', '文本字符串 (String)', 'STRING', 4, '', 'info', 0, 1, '字符串文本', '2026-09-14 21:20:32', '2026-09-14 21:20:32'),
                                                                                                                                                                                         (14, 'iot_data_type', '枚举状态 (Enum)', 'ENUM', 5, '', 'warning', 0, 1, '离散枚举代码', '2026-09-14 21:20:32', '2026-09-14 21:20:32');

-- --------------------------------------------------------

--
-- 表的结构 `sys_dict_type`
--
DROP TABLE IF EXISTS `sys_dict_type`;
CREATE TABLE `sys_dict_type` (
                                 `id` bigint(20) NOT NULL COMMENT '自增字典类型ID',
                                 `dict_name` varchar(100) COLLATE utf8mb4_unicode_ci NOT NULL COMMENT '字典名称',
                                 `dict_type` varchar(100) COLLATE utf8mb4_unicode_ci NOT NULL COMMENT '字典类型唯一标识',
                                 `status` tinyint(4) NOT NULL DEFAULT '1' COMMENT '状态 (1:正常, 0:停用)',
                                 `remark` varchar(500) COLLATE utf8mb4_unicode_ci DEFAULT NULL COMMENT '备注',
                                 `create_time` datetime NOT NULL DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
                                 `update_time` datetime NOT NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP COMMENT '更新时间'
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci COMMENT='系统字典类型表' ROW_FORMAT=DYNAMIC;

--
-- 转存表中的数据 `sys_dict_type`
--

INSERT INTO `sys_dict_type` (`id`, `dict_name`, `dict_type`, `status`, `remark`, `create_time`, `update_time`) VALUES
                                                                                                                   (1, '系统用户状态', 'sys_user_status', 1, '用户账号可用状态', '2026-09-14 21:20:32', '2026-09-14 21:20:32'),
                                                                                                                   (2, '设备通信在线状态', 'sys_device_status', 1, '物理设备通道网络状态', '2026-09-14 21:20:32', '2026-09-14 21:20:32'),
                                                                                                                   (3, '告警事件紧急等级', 'iot_alarm_level', 1, '系统告警级别', '2026-09-14 21:20:32', '2026-09-14 21:20:32'),
                                                                                                                   (4, '物模型数据类型', 'iot_data_type', 1, '物模型点位与参数数据类型', '2026-09-14 21:20:32', '2026-09-14 21:20:32');

-- --------------------------------------------------------

--
-- 表的结构 `sys_menu`
--
DROP TABLE IF EXISTS `sys_menu`;
CREATE TABLE `sys_menu` (
                            `id` bigint(20) NOT NULL COMMENT '自增菜单ID',
                            `parent_id` bigint(20) NOT NULL DEFAULT '0' COMMENT '父菜单ID (0为顶级目录)',
                            `menu_name` varchar(64) COLLATE utf8mb4_unicode_ci NOT NULL COMMENT '菜单名称',
                            `path` varchar(128) COLLATE utf8mb4_unicode_ci NOT NULL COMMENT '路由访问路径',
                            `component` varchar(128) COLLATE utf8mb4_unicode_ci DEFAULT NULL COMMENT '前端组件路径',
                            `icon` varchar(64) COLLATE utf8mb4_unicode_ci DEFAULT NULL COMMENT '菜单图标',
                            `sort_order` int(11) NOT NULL DEFAULT '0' COMMENT '显示顺序',
                            `menu_type` char(1) COLLATE utf8mb4_unicode_ci NOT NULL DEFAULT 'C' COMMENT '菜单类型 (M:目录, C:菜单, F:按钮)',
                            `perms` varchar(128) COLLATE utf8mb4_unicode_ci DEFAULT NULL COMMENT '权限标识 (如 system:user:view)',
                            `visible` tinyint(4) NOT NULL DEFAULT '1' COMMENT '是否显示 (1:显示, 0:隐藏)',
                            `status` tinyint(4) NOT NULL DEFAULT '1' COMMENT '状态 (1:启用, 0:停用)',
                            `remark` varchar(500) COLLATE utf8mb4_unicode_ci DEFAULT NULL COMMENT '备注',
                            `create_time` datetime NOT NULL DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
                            `update_time` datetime NOT NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP COMMENT '更新时间'
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci COMMENT='系统菜单与动态路由配置表' ROW_FORMAT=DYNAMIC;

--
-- 转存表中的数据 `sys_menu`
--

INSERT INTO `sys_menu` (`id`, `parent_id`, `menu_name`, `path`, `component`, `icon`, `sort_order`, `menu_type`, `perms`, `visible`, `status`, `remark`, `create_time`, `update_time`) VALUES
                                                                                                                                                                                          (1, 0, '系统仪表盘', '/dashboard', 'views/dashboard/OverviewDashboard.vue', 'activity', 1, 'C', 'dashboard:view', 1, 1, '全景数据监控与概览', '2026-09-14 21:20:32', '2026-09-14 21:20:32'),
                                                                                                                                                                                          (2, 0, '驱动管理', '/drivers', 'views/protocol/ProtocolDrivers.vue', 'server', 2, 'C', 'driver:list', 1, 1, '工业协议驱动管理', '2026-09-14 21:20:32', '2026-09-14 21:20:32'),
                                                                                                                                                                                          (3, 0, '设备管理', '/devices', 'views/devices/DeviceList.vue', 'cpu', 3, 'C', 'device:list', 1, 1, '边缘硬件网关与设备管理', '2026-09-14 21:20:32', '2026-09-14 21:20:32'),
                                                                                                                                                                                          (4, 0, '数据管理', '/data', 'views/data/PointDataMonitor.vue', 'database', 4, 'C', 'data:monitor', 1, 1, '点位实时与历史数据', '2026-09-14 21:20:32', '2026-09-14 21:20:32'),
                                                                                                                                                                                          (5, 0, '物模型管理', '/thingmodels', 'views/thingmodels/ThingModelManager.vue', 'database', 5, 'C', 'thingmodel:list', 1, 1, '物模型Schema元数据定义', '2026-09-14 21:20:32', '2026-09-14 21:20:32'),
                                                                                                                                                                                          (6, 0, '规则管理', '/rules', 'views/rules/RuleManager.vue', 'refresh', 6, 'C', 'rule:list', 1, 1, '轻量级联动规则引擎', '2026-09-14 21:20:32', '2026-09-14 21:20:32'),
                                                                                                                                                                                          (7, 0, '告警中心', '/alarms', 'views/alarms/AlarmEvents.vue', 'bell', 7, 'C', 'alarm:list', 1, 1, '异常状态与事件报警', '2026-09-14 21:20:32', '2026-09-14 21:20:32'),
                                                                                                                                                                                          (8, 0, '日志管理', '/logs', 'views/logs/LogManager.vue', 'file-text', 8, 'C', 'log:list', 1, 1, '运行与审计日志中心', '2026-09-14 21:20:32', '2026-09-14 21:20:32'),
                                                                                                                                                                                          (9, 0, '系统管理', '/system', 'Layout', 'shield', 9, 'M', 'system:manage', 1, 1, '系统与用户权限管理目录', '2026-09-14 21:20:32', '2026-09-14 21:20:32'),
                                                                                                                                                                                          (10, 9, '用户管理', '/system/users', 'views/system/UserList.vue', 'user', 1, 'C', 'system:user:list', 1, 1, '系统账号管理', '2026-09-14 21:20:32', '2026-09-14 21:20:32'),
                                                                                                                                                                                          (11, 9, '角色权限', '/system/roles', 'views/system/RoleList.vue', 'shield', 2, 'C', 'system:role:list', 1, 1, '角色与权限分配', '2026-09-14 21:20:32', '2026-09-14 21:20:32'),
                                                                                                                                                                                          (12, 9, '菜单路由', '/system/menus', 'views/system/MenuList.vue', 'menu', 3, 'C', 'system:menu:list', 1, 1, '动态路由菜单维护', '2026-09-14 21:20:32', '2026-09-14 21:20:32'),
                                                                                                                                                                                          (13, 9, '字典管理', '/system/dicts', 'views/system/DictList.vue', 'file-text', 4, 'C', 'system:dict:list', 1, 1, '数据字典与常量配置', '2026-09-14 21:20:32', '2026-09-14 21:20:32');

-- --------------------------------------------------------

--
-- 表的结构 `sys_permission`
--
DROP TABLE IF EXISTS `sys_permission`;
CREATE TABLE `sys_permission` (
                                  `id` bigint(20) NOT NULL COMMENT '自增权限ID',
                                  `perm_key` varchar(128) COLLATE utf8mb4_unicode_ci NOT NULL COMMENT '权限标识编码 (如 device:control)',
                                  `perm_name` varchar(128) COLLATE utf8mb4_unicode_ci NOT NULL COMMENT '权限名称',
                                  `module` varchar(64) COLLATE utf8mb4_unicode_ci DEFAULT NULL COMMENT '所属业务模块',
                                  `remark` varchar(500) COLLATE utf8mb4_unicode_ci DEFAULT NULL COMMENT '备注',
                                  `create_time` datetime NOT NULL DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间'
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci COMMENT='系统功能权限表' ROW_FORMAT=DYNAMIC;

--
-- 转存表中的数据 `sys_permission`
--

INSERT INTO `sys_permission` (`id`, `perm_key`, `perm_name`, `module`, `remark`, `create_time`) VALUES
                                                                                                    (1, 'system:user:list', '用户查询', '系统管理', '查看用户列表与账号详情', '2026-09-14 21:20:32'),
                                                                                                    (2, 'system:user:add', '用户新增', '系统管理', '创建系统操作人员账号', '2026-09-14 21:20:32'),
                                                                                                    (3, 'system:user:edit', '用户编辑', '系统管理', '修改信息、重置密码与启停状态', '2026-09-14 21:20:32'),
                                                                                                    (4, 'device:list', '设备查看', '设备管理', '查看物理设备列表与状态', '2026-09-14 21:20:32'),
                                                                                                    (5, 'device:add', '设备新增', '设备管理', '注册接入物理硬件设备', '2026-09-14 21:20:32'),
                                                                                                    (6, 'device:control', '设备反控', '设备管理', '下发设备物模型服务指令与控制调试', '2026-09-14 21:20:32'),
                                                                                                    (7, 'driver:list', '驱动查看', '驱动管理', '查看协议驱动列表与状态', '2026-09-14 21:20:32'),
                                                                                                    (8, 'thingmodel:list', '物模型查看', '物模型管理', '查看物模型属性与服务', '2026-09-14 21:20:32'),
                                                                                                    (9, 'thingmodel:edit', '物模型编辑', '物模型管理', '编辑物模型模板名称、编码与分类', '2026-09-14 21:20:32'),
                                                                                                    (10, 'data:monitor', '数据实时监控', '数据中心', '查看点位实时趋势', '2026-09-14 21:20:32'),
                                                                                                    (11, 'rule:list', '规则查看', '规则引擎', '查看阈值与告警联动规则', '2026-09-14 21:20:32'),
                                                                                                    (12, 'rule:edit', '规则维护', '规则引擎', '配置与修改联动规则判定与反控动作', '2026-09-14 21:20:32'),
                                                                                                    (13, 'alarm:list', '告警查看', '告警中心', '查看系统告警事件记录', '2026-09-14 21:20:32'),
                                                                                                    (14, 'alarm:handle', '告警处理', '告警中心', '确认与闭环恢复告警', '2026-09-14 21:20:32'),
                                                                                                    (15, 'log:list', '日志查看', '日志管理', '查看系统运行与操作审计日志', '2026-09-14 21:20:32'),
                                                                                                    (16, 'system:user:delete', '用户删除', '系统管理', '删除操作人员账号', '2026-10-09 09:03:36'),
                                                                                                    (17, 'system:role:list', '角色查询', '系统管理', '查看系统角色列表', '2026-10-09 09:03:36'),
                                                                                                    (18, 'system:role:add', '角色新增', '系统管理', '新建平台角色身份', '2026-10-09 09:03:36'),
                                                                                                    (19, 'system:role:edit', '角色编辑', '系统管理', '修改角色、授权功能权限与分配菜单', '2026-10-09 09:03:36'),
                                                                                                    (20, 'system:role:delete', '角色删除', '系统管理', '删除平台角色', '2026-10-09 09:03:36'),
                                                                                                    (21, 'system:menu:list', '菜单查询', '系统管理', '查看动态路由与菜单树', '2026-10-09 09:03:36'),
                                                                                                    (22, 'system:menu:add', '菜单新增', '系统管理', '新增顶级菜单或子项', '2026-10-09 09:03:36'),
                                                                                                    (23, 'system:menu:edit', '菜单编辑', '系统管理', '修改菜单信息与权限标识', '2026-10-09 09:03:36'),
                                                                                                    (24, 'system:menu:delete', '菜单删除', '系统管理', '删除系统菜单项', '2026-10-09 09:03:36'),
                                                                                                    (25, 'system:dict:list', '字典查询', '系统管理', '查看数据字典类型与键值', '2026-10-09 09:03:36'),
                                                                                                    (26, 'system:dict:add', '字典新增', '系统管理', '新增字典类型与字典键值', '2026-10-09 09:03:36'),
                                                                                                    (27, 'system:dict:edit', '字典修改', '系统管理', '修改字典类型与字典键值', '2026-10-09 09:03:36'),
                                                                                                    (28, 'system:dict:delete', '字典删除', '系统管理', '删除字典类型与字典键值', '2026-10-09 09:03:36'),
                                                                                                    (29, 'device:edit', '设备编辑', '设备管理', '编辑修改物理设备配置与点位映射', '2026-10-09 09:03:36'),
                                                                                                    (30, 'device:delete', '设备删除', '设备管理', '移除边缘网关或终端设备', '2026-10-09 09:03:36'),
                                                                                                    (31, 'driver:edit', '驱动管理', '驱动管理', '注册、更新驱动配置与启停驱动', '2026-10-09 09:03:36'),
                                                                                                    (32, 'thingmodel:add', '物模型新增', '物模型管理', '新建物模型基础模板', '2026-10-09 09:03:36'),
                                                                                                    (33, 'thingmodel:design', '设计物模型', '物模型管理', '设计物模型 Schema，定义与导入点位属性、控制服务与告警事件', '2026-10-09 09:03:36'),
                                                                                                    (34, 'thingmodel:delete', '物模型删除', '物模型管理', '删除未被引用的物模型模板', '2026-10-09 09:03:36'),
                                                                                                    (35, 'rule:add', '规则新增', '规则引擎', '新建联动告警与自动化反控规则', '2026-10-09 09:03:36'),
                                                                                                    (36, 'rule:delete', '规则删除', '规则引擎', '删除联动规则', '2026-10-09 09:03:36'),
                                                                                                    (37, 'alarm:delete', '告警删除', '告警中心', '物理删除与清理历史告警记录', '2026-10-09 09:03:36'),
                                                                                                    (38, 'log:clear', '日志清理', '日志管理', '清空与归档审计历史日志', '2026-10-09 09:03:36'),
                                                                                                    (39, 'dashboard:view', '大屏监控', '监控大屏', '查看全景数据吞吐与设备拓扑大屏', '2026-10-09 09:03:36');

-- --------------------------------------------------------

--
-- 表的结构 `sys_role`
--
DROP TABLE IF EXISTS `sys_role`;
CREATE TABLE `sys_role` (
                            `id` bigint(20) NOT NULL COMMENT '自增角色ID',
                            `role_key` varchar(64) COLLATE utf8mb4_unicode_ci NOT NULL COMMENT '角色权限字符 (如 admin, operator, viewer)',
                            `role_name` varchar(64) COLLATE utf8mb4_unicode_ci NOT NULL COMMENT '角色名称',
                            `sort_order` int(11) NOT NULL DEFAULT '0' COMMENT '显示排序',
                            `status` tinyint(4) NOT NULL DEFAULT '1' COMMENT '角色状态 (1:启用, 0:停用)',
                            `remark` varchar(500) COLLATE utf8mb4_unicode_ci DEFAULT NULL COMMENT '备注',
                            `create_time` datetime NOT NULL DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
                            `update_time` datetime NOT NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP COMMENT '更新时间'
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci COMMENT='系统角色表' ROW_FORMAT=DYNAMIC;

--
-- 转存表中的数据 `sys_role`
--

INSERT INTO `sys_role` (`id`, `role_key`, `role_name`, `sort_order`, `status`, `remark`, `create_time`, `update_time`) VALUES
                                                                                                                           (1, 'admin', '超级管理员', 1, 1, '系统内置最高权限管理员', '2026-09-14 21:20:32', '2026-09-14 21:20:32'),
                                                                                                                           (2, 'operator', '工控运维员', 2, 1, '负责现场设备调试、规则与告警处置', '2026-09-14 21:20:32', '2026-09-14 21:20:32'),
                                                                                                                           (3, 'viewer', '访客观察员', 3, 1, '仅具有平台数据只读监控权限', '2026-09-14 21:20:32', '2026-09-14 21:20:32');

-- --------------------------------------------------------

--
-- 表的结构 `sys_role_menu`
--
DROP TABLE IF EXISTS `sys_role_menu`;
CREATE TABLE `sys_role_menu` (
                                 `id` bigint(20) NOT NULL COMMENT '自增主键ID',
                                 `role_id` bigint(20) NOT NULL COMMENT '角色ID',
                                 `menu_id` bigint(20) NOT NULL COMMENT '菜单ID'
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci COMMENT='角色菜单关联表' ROW_FORMAT=DYNAMIC;

--
-- 转存表中的数据 `sys_role_menu`
--

INSERT INTO `sys_role_menu` (`id`, `role_id`, `menu_id`) VALUES
                                                             (1, 1, 1),
                                                             (2, 1, 2),
                                                             (3, 1, 3),
                                                             (4, 1, 4),
                                                             (5, 1, 5),
                                                             (6, 1, 6),
                                                             (7, 1, 7),
                                                             (8, 1, 8),
                                                             (9, 1, 9),
                                                             (10, 1, 10),
                                                             (11, 1, 11),
                                                             (12, 1, 12),
                                                             (13, 1, 13),
                                                             (14, 2, 1),
                                                             (15, 2, 2),
                                                             (16, 2, 3),
                                                             (17, 2, 4),
                                                             (18, 2, 5),
                                                             (19, 2, 6),
                                                             (20, 2, 7),
                                                             (21, 2, 8);

-- --------------------------------------------------------

--
-- 表的结构 `sys_role_permission`
--
DROP TABLE IF EXISTS `sys_role_permission`;
CREATE TABLE `sys_role_permission` (
                                       `id` bigint(20) NOT NULL COMMENT '自增主键ID',
                                       `role_id` bigint(20) NOT NULL COMMENT '角色ID',
                                       `permission_id` bigint(20) NOT NULL COMMENT '权限ID'
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci COMMENT='角色权限关联表' ROW_FORMAT=DYNAMIC;

--
-- 转存表中的数据 `sys_role_permission`
--

INSERT INTO `sys_role_permission` (`id`, `role_id`, `permission_id`) VALUES
                                                                         (1, 1, 1),
                                                                         (2, 1, 2),
                                                                         (3, 1, 3),
                                                                         (4, 1, 4),
                                                                         (5, 1, 5),
                                                                         (6, 1, 6),
                                                                         (7, 1, 7),
                                                                         (8, 1, 8),
                                                                         (9, 1, 9),
                                                                         (10, 1, 10),
                                                                         (11, 1, 11),
                                                                         (12, 1, 12),
                                                                         (13, 1, 13),
                                                                         (14, 1, 14),
                                                                         (15, 1, 15),
                                                                         (48, 1, 16),
                                                                         (47, 1, 17),
                                                                         (44, 1, 18),
                                                                         (46, 1, 19),
                                                                         (45, 1, 20),
                                                                         (43, 1, 21),
                                                                         (40, 1, 22),
                                                                         (42, 1, 23),
                                                                         (41, 1, 24),
                                                                         (39, 1, 25),
                                                                         (36, 1, 26),
                                                                         (38, 1, 27),
                                                                         (37, 1, 28),
                                                                         (31, 1, 29),
                                                                         (30, 1, 30),
                                                                         (32, 1, 31),
                                                                         (49, 1, 32),
                                                                         (51, 1, 33),
                                                                         (50, 1, 34),
                                                                         (34, 1, 35),
                                                                         (35, 1, 36),
                                                                         (28, 1, 37),
                                                                         (33, 1, 38),
                                                                         (29, 1, 39),
                                                                         (16, 2, 4),
                                                                         (17, 2, 5),
                                                                         (18, 2, 6),
                                                                         (19, 2, 7),
                                                                         (20, 2, 8),
                                                                         (21, 2, 9),
                                                                         (22, 2, 10),
                                                                         (23, 2, 11),
                                                                         (24, 2, 12),
                                                                         (25, 2, 13),
                                                                         (26, 2, 14),
                                                                         (27, 2, 15),
                                                                         (60, 2, 29),
                                                                         (62, 2, 32),
                                                                         (63, 2, 33),
                                                                         (61, 2, 35),
                                                                         (59, 2, 39);

-- --------------------------------------------------------

--
-- 表的结构 `sys_user`
--
DROP TABLE IF EXISTS `sys_user`;
CREATE TABLE `sys_user` (
                            `id` bigint(20) NOT NULL COMMENT '自增主键ID',
                            `username` varchar(64) COLLATE utf8mb4_unicode_ci NOT NULL COMMENT '登录用户名',
                            `nickname` varchar(64) COLLATE utf8mb4_unicode_ci NOT NULL COMMENT '用户昵称',
                            `password` varchar(128) COLLATE utf8mb4_unicode_ci NOT NULL COMMENT '加盐加密后的密码Hash (SHA-256)',
                            `salt` varchar(64) COLLATE utf8mb4_unicode_ci NOT NULL COMMENT '密码独立随机盐 (Salt)',
                            `email` varchar(128) COLLATE utf8mb4_unicode_ci DEFAULT NULL COMMENT '电子邮箱',
                            `phone` varchar(32) COLLATE utf8mb4_unicode_ci DEFAULT NULL COMMENT '联系手机号',
                            `avatar` varchar(255) COLLATE utf8mb4_unicode_ci DEFAULT NULL COMMENT '用户头像地址',
                            `status` tinyint(4) NOT NULL DEFAULT '1' COMMENT '账号状态 (1:启用, 0:停用/冻结)',
                            `last_login_ip` varchar(64) COLLATE utf8mb4_unicode_ci DEFAULT NULL COMMENT '最后登录IP',
                            `last_login_time` datetime DEFAULT NULL COMMENT '最后登录时间',
                            `remark` varchar(500) COLLATE utf8mb4_unicode_ci DEFAULT NULL COMMENT '备注信息',
                            `create_time` datetime NOT NULL DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
                            `update_time` datetime NOT NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP COMMENT '更新时间'
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci COMMENT='系统用户表' ROW_FORMAT=DYNAMIC;

--
-- 转存表中的数据 `sys_user`
--

INSERT INTO `sys_user` (`id`, `username`, `nickname`, `password`, `salt`, `email`, `phone`, `avatar`, `status`, `last_login_ip`, `last_login_time`, `remark`, `create_time`, `update_time`) VALUES
                                                                                                                                                                                                (1, 'admin', '超级管理员', 'e83a9095b54c85cc533e0dc66b59eb6ea644aebc631bb13c9cc979cf62454df1', 'f641c7d363db759d71c6e39cca046732', 'admin@unihh.com', '13800138000', NULL, 1, '180.98.148.22', '2026-11-04 18:25:38', '系统预设超级管理员 (密码: admin123)', '2026-09-14 21:20:32', '2026-11-04 18:25:38'),
                                                                                                                                                                                                (2, 'operator', '工控运维专员', 'fb79c7a14084b53980735dda477fee1f3d8b3e6e75b516f0c05c5c6e4f37eb22', '4d1e0a7f5b3a6c9e2d8f1b4a7c9f2b8c', 'operator@unihh.com', '13900139000', NULL, 1, NULL, NULL, '现场工控与设备采集运维人员 (密码: operator123)', '2026-09-14 21:20:32', '2026-09-14 21:20:32');

-- --------------------------------------------------------

--
-- 表的结构 `sys_user_role`
--
DROP TABLE IF EXISTS `sys_user_role`;
CREATE TABLE `sys_user_role` (
                                 `id` bigint(20) NOT NULL COMMENT '自增主键ID',
                                 `user_id` bigint(20) NOT NULL COMMENT '用户ID',
                                 `role_id` bigint(20) NOT NULL COMMENT '角色ID'
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci COMMENT='用户角色关联表' ROW_FORMAT=DYNAMIC;

--
-- 转存表中的数据 `sys_user_role`
--

INSERT INTO `sys_user_role` (`id`, `user_id`, `role_id`) VALUES
                                                             (1, 1, 1),
                                                             (2, 2, 2);

--
-- 转储表的索引
--

--
-- 表的索引 `iot_alarm`
--
ALTER TABLE `iot_alarm`
    ADD PRIMARY KEY (`id`) USING BTREE,
  ADD UNIQUE KEY `alarm_id` (`alarm_id`) USING BTREE;

--
-- 表的索引 `iot_device`
--
ALTER TABLE `iot_device`
    ADD PRIMARY KEY (`id`) USING BTREE,
  ADD UNIQUE KEY `device_id` (`device_id`) USING BTREE;

--
-- 表的索引 `iot_device_point_mapping`
--
ALTER TABLE `iot_device_point_mapping`
    ADD PRIMARY KEY (`id`) USING BTREE,
  ADD UNIQUE KEY `uk_device_point` (`device_id`,`point_code`) USING BTREE,
  ADD KEY `idx_device_id` (`device_id`) USING BTREE,
  ADD KEY `idx_model_key` (`model_key`) USING BTREE;

--
-- 表的索引 `iot_driver_info`
--
ALTER TABLE `iot_driver_info`
    ADD PRIMARY KEY (`id`) USING BTREE,
  ADD UNIQUE KEY `driver_code` (`driver_code`) USING BTREE;

--
-- 表的索引 `iot_log`
--
ALTER TABLE `iot_log`
    ADD PRIMARY KEY (`id`) USING BTREE,
  ADD UNIQUE KEY `log_id` (`log_id`) USING BTREE;

--
-- 表的索引 `iot_rule`
--
ALTER TABLE `iot_rule`
    ADD PRIMARY KEY (`id`) USING BTREE,
  ADD UNIQUE KEY `rule_id` (`rule_id`) USING BTREE;

--
-- 表的索引 `iot_rule_action`
--
ALTER TABLE `iot_rule_action`
    ADD PRIMARY KEY (`id`) USING BTREE,
  ADD KEY `idx_rule_id` (`rule_id`) USING BTREE,
  ADD KEY `idx_target_device` (`target_device_id`) USING BTREE;

--
-- 表的索引 `iot_thing_model`
--
ALTER TABLE `iot_thing_model`
    ADD PRIMARY KEY (`id`) USING BTREE;

--
-- 表的索引 `sys_dict_data`
--
ALTER TABLE `sys_dict_data`
    ADD PRIMARY KEY (`id`) USING BTREE,
  ADD KEY `idx_dict_data_type` (`dict_type`,`sort_order`) USING BTREE;

--
-- 表的索引 `sys_dict_type`
--
ALTER TABLE `sys_dict_type`
    ADD PRIMARY KEY (`id`) USING BTREE,
  ADD UNIQUE KEY `dict_type` (`dict_type`) USING BTREE;

--
-- 表的索引 `sys_menu`
--
ALTER TABLE `sys_menu`
    ADD PRIMARY KEY (`id`) USING BTREE,
  ADD KEY `idx_menu_parent` (`parent_id`) USING BTREE;

--
-- 表的索引 `sys_permission`
--
ALTER TABLE `sys_permission`
    ADD PRIMARY KEY (`id`) USING BTREE,
  ADD UNIQUE KEY `perm_key` (`perm_key`) USING BTREE;

--
-- 表的索引 `sys_role`
--
ALTER TABLE `sys_role`
    ADD PRIMARY KEY (`id`) USING BTREE,
  ADD UNIQUE KEY `role_key` (`role_key`) USING BTREE;

--
-- 表的索引 `sys_role_menu`
--
ALTER TABLE `sys_role_menu`
    ADD PRIMARY KEY (`id`) USING BTREE,
  ADD UNIQUE KEY `uk_role_menu` (`role_id`,`menu_id`) USING BTREE,
  ADD KEY `idx_role_menu_role` (`role_id`) USING BTREE,
  ADD KEY `idx_role_menu_menu` (`menu_id`) USING BTREE;

--
-- 表的索引 `sys_role_permission`
--
ALTER TABLE `sys_role_permission`
    ADD PRIMARY KEY (`id`) USING BTREE,
  ADD UNIQUE KEY `uk_role_perm` (`role_id`,`permission_id`) USING BTREE,
  ADD KEY `idx_role_perm_role` (`role_id`) USING BTREE,
  ADD KEY `idx_role_perm_perm` (`permission_id`) USING BTREE;

--
-- 表的索引 `sys_user`
--
ALTER TABLE `sys_user`
    ADD PRIMARY KEY (`id`) USING BTREE,
  ADD UNIQUE KEY `username` (`username`) USING BTREE;

--
-- 表的索引 `sys_user_role`
--
ALTER TABLE `sys_user_role`
    ADD PRIMARY KEY (`id`) USING BTREE,
  ADD UNIQUE KEY `uk_user_role` (`user_id`,`role_id`) USING BTREE,
  ADD KEY `idx_user_role_user` (`user_id`) USING BTREE,
  ADD KEY `idx_user_role_role` (`role_id`) USING BTREE;

--
-- 在导出的表使用AUTO_INCREMENT
--

-- 系统表
ALTER TABLE `sys_user`           MODIFY `id` bigint(20) NOT NULL AUTO_INCREMENT COMMENT '自增主键ID', AUTO_INCREMENT=3;
ALTER TABLE `sys_role`           MODIFY `id` bigint(20) NOT NULL AUTO_INCREMENT COMMENT '自增角色ID', AUTO_INCREMENT=4;
ALTER TABLE `sys_user_role`      MODIFY `id` bigint(20) NOT NULL AUTO_INCREMENT COMMENT '自增主键ID', AUTO_INCREMENT=3;
ALTER TABLE `sys_role_menu`      MODIFY `id` bigint(20) NOT NULL AUTO_INCREMENT COMMENT '自增主键ID', AUTO_INCREMENT=22;
ALTER TABLE `sys_role_permission` MODIFY `id` bigint(20) NOT NULL AUTO_INCREMENT COMMENT '自增主键ID', AUTO_INCREMENT=68;
ALTER TABLE `sys_menu`           MODIFY `id` bigint(20) NOT NULL AUTO_INCREMENT COMMENT '自增菜单ID', AUTO_INCREMENT=14;
ALTER TABLE `sys_permission`     MODIFY `id` bigint(20) NOT NULL AUTO_INCREMENT COMMENT '自增权限ID', AUTO_INCREMENT=118;
ALTER TABLE `sys_dict_type`      MODIFY `id` bigint(20) NOT NULL AUTO_INCREMENT COMMENT '自增字典类型ID', AUTO_INCREMENT=5;
ALTER TABLE `sys_dict_data`      MODIFY `id` bigint(20) NOT NULL AUTO_INCREMENT COMMENT '自增字典数据ID', AUTO_INCREMENT=15;

--
-- 使用表AUTO_INCREMENT `iot_alarm`
--
ALTER TABLE `iot_alarm`
    MODIFY `id` bigint(20) NOT NULL AUTO_INCREMENT COMMENT '自增主键ID', AUTO_INCREMENT=3297;

--
-- 使用表AUTO_INCREMENT `iot_device`
--
ALTER TABLE `iot_device`
    MODIFY `id` bigint(20) NOT NULL AUTO_INCREMENT COMMENT '自增主键ID', AUTO_INCREMENT=6;

--
-- 使用表AUTO_INCREMENT `iot_device_point_mapping`
--
ALTER TABLE `iot_device_point_mapping`
    MODIFY `id` bigint(20) NOT NULL AUTO_INCREMENT COMMENT '自增主键ID', AUTO_INCREMENT=16;

--
-- 使用表AUTO_INCREMENT `iot_driver_info`
--
ALTER TABLE `iot_driver_info`
    MODIFY `id` bigint(20) NOT NULL AUTO_INCREMENT COMMENT '自增主键ID', AUTO_INCREMENT=7;

--
-- 使用表AUTO_INCREMENT `iot_log`
--
ALTER TABLE `iot_log`
    MODIFY `id` bigint(20) NOT NULL AUTO_INCREMENT COMMENT '自增主键ID', AUTO_INCREMENT=3610;

--
-- 使用表AUTO_INCREMENT `iot_rule`
--
ALTER TABLE `iot_rule`
    MODIFY `id` bigint(20) NOT NULL AUTO_INCREMENT COMMENT '自增主键ID', AUTO_INCREMENT=5;

--
-- 使用表AUTO_INCREMENT `iot_rule_action`
--
ALTER TABLE `iot_rule_action`
    MODIFY `id` bigint(20) NOT NULL AUTO_INCREMENT COMMENT '自增主键ID', AUTO_INCREMENT=10;

--
-- 使用表AUTO_INCREMENT `iot_thing_model`
--
ALTER TABLE `iot_thing_model`
    MODIFY `id` bigint(20) NOT NULL AUTO_INCREMENT COMMENT '自增主键ID', AUTO_INCREMENT=118;

--
-- 使用表AUTO_INCREMENT `sys_dict_data`
--
ALTER TABLE `sys_dict_data`
    MODIFY `id` bigint(20) NOT NULL AUTO_INCREMENT COMMENT '自增字典数据ID', AUTO_INCREMENT=15;

--
-- 使用表AUTO_INCREMENT `sys_dict_type`
--
ALTER TABLE `sys_dict_type`
    MODIFY `id` bigint(20) NOT NULL AUTO_INCREMENT COMMENT '自增字典类型ID', AUTO_INCREMENT=5;

--
-- 使用表AUTO_INCREMENT `sys_menu`
--
ALTER TABLE `sys_menu`
    MODIFY `id` bigint(20) NOT NULL AUTO_INCREMENT COMMENT '自增菜单ID', AUTO_INCREMENT=14;

--
-- 使用表AUTO_INCREMENT `sys_permission`
--
ALTER TABLE `sys_permission`
    MODIFY `id` bigint(20) NOT NULL AUTO_INCREMENT COMMENT '自增权限ID', AUTO_INCREMENT=118;

--
-- 使用表AUTO_INCREMENT `sys_role`
--
ALTER TABLE `sys_role`
    MODIFY `id` bigint(20) NOT NULL AUTO_INCREMENT COMMENT '自增角色ID', AUTO_INCREMENT=4;

--
-- 使用表AUTO_INCREMENT `sys_role_menu`
--
ALTER TABLE `sys_role_menu`
    MODIFY `id` bigint(20) NOT NULL AUTO_INCREMENT COMMENT '自增主键ID', AUTO_INCREMENT=22;

--
-- 使用表AUTO_INCREMENT `sys_role_permission`
--
ALTER TABLE `sys_role_permission`
    MODIFY `id` bigint(20) NOT NULL AUTO_INCREMENT COMMENT '自增主键ID', AUTO_INCREMENT=68;

--
-- 使用表AUTO_INCREMENT `sys_user`
--
ALTER TABLE `sys_user`
    MODIFY `id` bigint(20) NOT NULL AUTO_INCREMENT COMMENT '自增主键ID', AUTO_INCREMENT=3;

--
-- 使用表AUTO_INCREMENT `sys_user_role`
--
ALTER TABLE `sys_user_role`
    MODIFY `id` bigint(20) NOT NULL AUTO_INCREMENT COMMENT '自增主键ID', AUTO_INCREMENT=3;
COMMIT;

/*!40101 SET CHARACTER_SET_CLIENT=@OLD_CHARACTER_SET_CLIENT */;
/*!40101 SET CHARACTER_SET_RESULTS=@OLD_CHARACTER_SET_RESULTS */;
/*!40101 SET COLLATION_CONNECTION=@OLD_COLLATION_CONNECTION */;
