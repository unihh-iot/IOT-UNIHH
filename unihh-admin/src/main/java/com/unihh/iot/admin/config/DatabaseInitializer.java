/*
 * Copyright (c) 2026 unihh.com All Rights Reserved.
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *     http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 *
 * Website: https://unihh.com
 * Author: deversun
 */

package com.unihh.iot.admin.config;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.CommandLineRunner;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.stereotype.Component;

/**
 * 数据库自动初始化与表结构校验组件
 * 首次启动时自动建立 MySQL 数据库表（产品、设备、物模型、驱动、规则引擎、告警中心、日志审计）并注入初始示例数据
 */
@Component
public class DatabaseInitializer implements CommandLineRunner {
    private static final Logger log = LoggerFactory.getLogger(DatabaseInitializer.class);

    private static final String MODBUS_YAML = "driverCode: modbus-tcp\n"
            + "driverName: Modbus TCP 工业协议驱动\n"
            + "version: 2.0.0\n"
            + "params:\n"
            + "  - key: host\n"
            + "    name: IP地址\n"
            + "    dataType: STRING\n"
            + "    required: true\n"
            + "    defaultValue: '192.168.1.100'\n"
            + "  - key: port\n"
            + "    name: 通信端口\n"
            + "    dataType: INT\n"
            + "    required: true\n"
            + "    defaultValue: '502'\n"
            + "  - key: slaveId\n"
            + "    name: 从站 ID\n"
            + "    dataType: INT\n"
            + "    required: true\n"
            + "    defaultValue: '1'\n"
            + "  - key: byteOrder\n"
            + "    name: 寄存器字节序\n"
            + "    dataType: SELECT\n"
            + "    required: false\n"
            + "    defaultValue: 'CDAB'\n"
            + "    description: '32位/64位整型与浮点数在寄存器中的存储排列顺序 (推荐CDAB)'\n"
            + "    options:\n"
            + "      - label: 'CDAB (工业推荐 / 32位小端字大端字节)'\n"
            + "        value: 'CDAB'\n"
            + "      - label: 'ABCD (标准大端模式 / Big-Endian)'\n"
            + "        value: 'ABCD'\n"
            + "      - label: 'DCBA (完全小端模式 / Little-Endian)'\n"
            + "        value: 'DCBA'\n"
            + "      - label: 'BADC (字节交换模式 / Byte Swap)'\n"
            + "        value: 'BADC'\n"
            + "      - label: 'AB (16位标准大端)'\n"
            + "        value: 'AB'\n"
            + "      - label: 'BA (16位字节交换)'\n"
            + "        value: 'BA'\n"
            + "      - label: 'ABCDEFGH (64位标准大端)'\n"
            + "        value: 'ABCDEFGH'\n"
            + "      - label: 'GHEFCDAB (64位双字交换)'\n"
            + "        value: 'GHEFCDAB'\n"
            + "      - label: 'HGFEDCBA (64位完全小端)'\n"
            + "        value: 'HGFEDCBA'\n"
            + "      - label: 'BADCFEHG (64位字节交换)'\n"
            + "        value: 'BADCFEHG'\n"
            + "addressRule:\n"
            + "  ruleType: 'MODBUS'\n"
            + "  placeholder: '例: 40001 (保持寄存器) 或 00001 (线圈)'\n"
            + "  startOffset: 40001\n"
            + "  coilStartOffset: 1\n"
            + "  typeSteps:\n"
            + "    INT: 1\n"
            + "    ENUM: 1\n"
            + "    INT32: 2\n"
            + "    FLOAT: 2\n"
            + "    DOUBLE: 4\n"
            + "    INT64: 4\n"
            + "    BOOL: 1\n";

    private static final String S7_YAML = "driverCode: s7-driver\n"
            + "driverName: 西门子 S7 工业 PLC 协议驱动\n"
            + "version: 2.0.0\n"
            + "params:\n"
            + "  - key: host\n"
            + "    name: PLC IP地址\n"
            + "    dataType: STRING\n"
            + "    required: true\n"
            + "    defaultValue: '192.168.1.200'\n"
            + "  - key: port\n"
            + "    name: 端口号\n"
            + "    dataType: INT\n"
            + "    required: true\n"
            + "    defaultValue: '102'\n"
            + "  - key: rack\n"
            + "    name: 机架号 (Rack)\n"
            + "    dataType: INT\n"
            + "    required: true\n"
            + "    defaultValue: '0'\n"
            + "  - key: slot\n"
            + "    name: 插槽号 (Slot)\n"
            + "    dataType: INT\n"
            + "    required: true\n"
            + "    defaultValue: '1'\n"
            + "  - key: plcType\n"
            + "    name: PLC 型号\n"
            + "    dataType: SELECT\n"
            + "    required: false\n"
            + "    defaultValue: 'S7-1200'\n"
            + "    description: '西门子 S7 系列控制器型号 (自动适配机架与插槽)'\n"
            + "    options:\n"
            + "      - label: 'S7-1200 (默认 Rack 0, Slot 1)'\n"
            + "        value: 'S7-1200'\n"
            + "      - label: 'S7-1500 (默认 Rack 0, Slot 1)'\n"
            + "        value: 'S7-1500'\n"
            + "      - label: 'S7-300 (默认 Rack 0, Slot 2)'\n"
            + "        value: 'S7-300'\n"
            + "      - label: 'S7-400 (默认 Rack 0, Slot 2)'\n"
            + "        value: 'S7-400'\n"
            + "      - label: 'S7-200 SMART (默认 Rack 0, Slot 1)'\n"
            + "        value: 'S7-200-SMART'\n"
            + "  - key: byteOrder\n"
            + "    name: 寄存器字节序\n"
            + "    dataType: SELECT\n"
            + "    required: false\n"
            + "    defaultValue: 'ABCD'\n"
            + "    description: '32位/64位整型与浮点数在S7数据块中的存储排列顺序 (推荐ABCD)'\n"
            + "    options:\n"
            + "      - label: 'ABCD (西门子原生标准大端模式 / Big-Endian)'\n"
            + "        value: 'ABCD'\n"
            + "      - label: 'CDAB (32位小端字大端字节)'\n"
            + "        value: 'CDAB'\n"
            + "      - label: 'DCBA (完全小端模式 / Little-Endian)'\n"
            + "        value: 'DCBA'\n"
            + "      - label: 'BADC (字节交换模式 / Byte Swap)'\n"
            + "        value: 'BADC'\n"
            + "      - label: 'AB (16位标准大端)'\n"
            + "        value: 'AB'\n"
            + "      - label: 'BA (16位字节交换)'\n"
            + "        value: 'BA'\n"
            + "      - label: 'ABCDEFGH (64位标准大端)'\n"
            + "        value: 'ABCDEFGH'\n"
            + "      - label: 'GHEFCDAB (64位双字交换)'\n"
            + "        value: 'GHEFCDAB'\n"
            + "      - label: 'HGFEDCBA (64位完全小端)'\n"
            + "        value: 'HGFEDCBA'\n"
            + "      - label: 'BADCFEHG (64位字节交换)'\n"
            + "        value: 'BADCFEHG'\n"
            + "addressRule:\n"
            + "  ruleType: 'S7'\n"
            + "  placeholder: '例: DB1.DBD0 (Float/DInt) 或 DB1.DBW2 (Int)'\n"
            + "  defaultArea: 'DB1'\n"
            + "  startOffset: 0\n"
            + "  typeSteps:\n"
            + "    BOOL: 1\n"
            + "    INT: 2\n"
            + "    INT32: 4\n"
            + "    FLOAT: 4\n"
            + "    DOUBLE: 8\n"
            + "    INT64: 8\n";

    private static final String OPCUA_YAML = "driverCode: opc-ua\n"
            + "driverName: OPC UA 规范协议驱动\n"
            + "version: 2.0.0\n"
            + "params:\n"
            + "  - key: endpointUrl\n"
            + "    name: Endpoint URL\n"
            + "    dataType: STRING\n"
            + "    required: true\n"
            + "    defaultValue: 'opc.tcp://192.168.1.50:4840'\n"
            + "  - key: securityPolicy\n"
            + "    name: 安全策略\n"
            + "    dataType: SELECT\n"
            + "    required: false\n"
            + "    defaultValue: 'None'\n"
            + "    options:\n"
            + "      - label: 'None (无加密认证)'\n"
            + "        value: 'None'\n"
            + "      - label: 'Basic256'\n"
            + "        value: 'Basic256'\n"
            + "      - label: 'Basic256Sha256'\n"
            + "        value: 'Basic256Sha256'\n"
            + "addressRule:\n"
            + "  ruleType: 'TEMPLATE'\n"
            + "  placeholder: '例: ns=2;s=Device1.Temperature'\n"
            + "  defaultTemplate: 'ns=2;s={deviceId}.{pointCode}'\n";

    @Autowired(required = false)
    private JdbcTemplate jdbcTemplate;

    @Override
    public void run(String... args) throws Exception {
        if (jdbcTemplate == null) {
            log.warn("[DatabaseInitializer] JdbcTemplate 未配置，跳过数据库自动初始化");
            return;
        }

        try {
            log.info("[DatabaseInitializer] 正在校验并初始化 MySQL 后台管理数据库表结构...");

            // 1. 删除废弃的产品表 iot_product (若存在)
            jdbcTemplate.execute("DROP TABLE IF EXISTS `iot_product`;");

            // 2. 设备表 iot_device
            jdbcTemplate.execute("CREATE TABLE IF NOT EXISTS `iot_device` ("
                    + "`id` BIGINT NOT NULL AUTO_INCREMENT PRIMARY KEY COMMENT '自增主键ID',"
                    + "`device_id` VARCHAR(64) NOT NULL UNIQUE COMMENT '物理设备唯一标识ID (如 dev_001)',"
                    + "`device_name` VARCHAR(128) NOT NULL COMMENT '物理设备显示名称',"
                    + "`model_key` VARCHAR(64) NOT NULL COMMENT '关联物模型Key',"
                    + "`driver_code` VARCHAR(64) NOT NULL COMMENT '绑定协议驱动Code (如 modbus-tcp)',"
                    + "`host` VARCHAR(64) DEFAULT '192.168.1.100' COMMENT '设备通信IP地址/串口号',"
                    + "`port` INT DEFAULT 502 COMMENT '设备通信端口号',"
                    + "`connection_params` TEXT DEFAULT NULL COMMENT '驱动拓展连接参数 (JSON格式)',"
                    + "`status` TINYINT NOT NULL DEFAULT 1 COMMENT '物理网络通道在线状态 (1:在线 Online, 0:离线 Offline, 2:通信异常 Error)',"
                    + "`acq_status` TINYINT NOT NULL DEFAULT 1 COMMENT '数据采集使能状态 (1:采集运行中 Active, 0:手动暂停采集 Paused)',"
                    + "`address_mappings_json` TEXT DEFAULT NULL COMMENT '物理点位与协议地址映射配置 (JSON格式)',"
                    + "`last_online_time` DATETIME DEFAULT NULL COMMENT '最后在线通信时间',"
                    + "`create_time` DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP COMMENT '设备创建时间',"
                    + "`update_time` DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP COMMENT '设备更新时间'"
                    + ") ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COMMENT='物理设备明细表';");

            // 3. 物模型定义表 iot_thing_model
            jdbcTemplate.execute("CREATE TABLE IF NOT EXISTS `iot_thing_model` ("
                    + "`id` BIGINT NOT NULL AUTO_INCREMENT PRIMARY KEY COMMENT '自增主键ID',"
                    + "`model_key` VARCHAR(64) NOT NULL COMMENT '关联物模型Key',"
                    + "`identifier` VARCHAR(64) NOT NULL COMMENT '点位/服务标识符 (如 temperature)',"
                    + "`name` VARCHAR(128) NOT NULL COMMENT '显示名称',"
                    + "`model_type` VARCHAR(32) NOT NULL DEFAULT 'PROPERTY' COMMENT '物模型类型 (PROPERTY:属性, SERVICE:服务, EVENT:事件)',"
                    + "`data_type` VARCHAR(32) NOT NULL DEFAULT 'DOUBLE' COMMENT '数据类型 (INT, DOUBLE, STRING, BOOL, ENUM)',"
                    + "`access_mode` VARCHAR(16) NOT NULL DEFAULT 'R' COMMENT '读写模式 (R:只读, RW:读写)',"
                    + "`unit` VARCHAR(32) DEFAULT NULL COMMENT '物理测量单位',"
                    + "`schema_json` TEXT DEFAULT NULL COMMENT '属性/服务扩展Schema定义 (JSON格式)',"
                    + "`create_time` DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间'"
                    + ") ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COMMENT='物模型元数据定义表';");

            // 4. 驱动注册表 iot_driver_info
            jdbcTemplate.execute("CREATE TABLE IF NOT EXISTS `iot_driver_info` ("
                    + "`id` BIGINT NOT NULL AUTO_INCREMENT PRIMARY KEY COMMENT '自增主键ID',"
                    + "`driver_code` VARCHAR(64) NOT NULL UNIQUE COMMENT '驱动唯一Code (如 modbus-tcp)',"
                    + "`driver_name` VARCHAR(128) NOT NULL COMMENT '驱动显示名称',"
                    + "`version` VARCHAR(32) NOT NULL DEFAULT '1.0.0' COMMENT '驱动版本号',"
                    + "`jar_file_name` VARCHAR(256) DEFAULT NULL COMMENT '驱动Jar包文件名',"
                    + "`status` TINYINT NOT NULL DEFAULT 1 COMMENT '驱动激活状态 (1:启用, 0:停用)',"
                    + "`description` VARCHAR(512) DEFAULT NULL COMMENT '驱动功能与协议详细描述',"
                    + "`manifest_yaml` TEXT DEFAULT NULL COMMENT '驱动Manifest YAML定义原文',"
                    + "`service_url` VARCHAR(256) DEFAULT NULL COMMENT '驱动微服务运行URL地址 (如 http://localhost:28011)',"
                    + "`create_time` DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP COMMENT '注册时间'"
                    + ") ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COMMENT='协议驱动注册信息表';");

            // 5. 规则引擎定义表 iot_rule
            jdbcTemplate.execute("CREATE TABLE IF NOT EXISTS `iot_rule` ("
                    + "`id` BIGINT NOT NULL AUTO_INCREMENT PRIMARY KEY COMMENT '自增主键ID',"
                    + "`rule_id` VARCHAR(64) NOT NULL UNIQUE COMMENT '规则唯一ID (如 RULE-101)',"
                    + "`rule_name` VARCHAR(128) NOT NULL COMMENT '规则名称',"
                    + "`model_key` VARCHAR(64) DEFAULT NULL COMMENT '关联物模型Key',"
                    + "`source_device_id` VARCHAR(64) NOT NULL COMMENT '触发源设备ID',"
                    + "`source_point` VARCHAR(64) NOT NULL COMMENT '触发源点位Code',"
                    + "`condition_mode` VARCHAR(32) NOT NULL DEFAULT 'SINGLE' COMMENT '条件类型 (SINGLE:单条件阈值, RANGE_OUTSIDE:区间越界)',"
                    + "`operator` VARCHAR(16) DEFAULT '>' COMMENT '判断操作符 (>, >=, <, <=, ==, !=)',"
                    + "`threshold_value` DOUBLE DEFAULT 0 COMMENT '比较阈值',"
                    + "`min_val` DOUBLE DEFAULT 0 COMMENT '区间最小值',"
                    + "`max_val` DOUBLE DEFAULT 0 COMMENT '区间最大值',"
                    + "`enabled` TINYINT NOT NULL DEFAULT 1 COMMENT '规则开关 (1:启用, 0:禁用)',"
                    + "`description` VARCHAR(512) DEFAULT NULL COMMENT '规则描述',"
                    + "`create_time` DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',"
                    + "`update_time` DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP COMMENT '更新时间'"
                    + ") ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COMMENT='规则引擎定义表';");

            // 5.1 规则联动响应动作关联明细表 iot_rule_action
            jdbcTemplate.execute("CREATE TABLE IF NOT EXISTS `iot_rule_action` ("
                    + "`id` BIGINT NOT NULL AUTO_INCREMENT PRIMARY KEY COMMENT '自增主键ID',"
                    + "`rule_id` VARCHAR(64) NOT NULL COMMENT '所属规则ID',"
                    + "`action_id` VARCHAR(64) NOT NULL COMMENT '动作唯一ID',"
                    + "`action_type` VARCHAR(32) NOT NULL DEFAULT 'SERVICE' COMMENT '动作类型 (SERVICE, ALARM, LOG)',"
                    + "`action_name` VARCHAR(128) NOT NULL COMMENT '动作名称',"
                    + "`target_device_id` VARCHAR(64) DEFAULT NULL COMMENT '目标反控设备ID',"
                    + "`target_service_code` VARCHAR(64) DEFAULT NULL COMMENT '目标服务指令Code',"
                    + "`service_input_values` TEXT DEFAULT NULL COMMENT '物模型入参JSON',"
                    + "`alarm_level` VARCHAR(32) DEFAULT NULL COMMENT '告警级别',"
                    + "`alarm_content` VARCHAR(512) DEFAULT NULL COMMENT '告警文本',"
                    + "`log_level` VARCHAR(32) DEFAULT NULL COMMENT '日志级别',"
                    + "`log_content` VARCHAR(512) DEFAULT NULL COMMENT '日志文本',"
                    + "`enabled` TINYINT NOT NULL DEFAULT 1 COMMENT '独立开关 (1:启用, 0:禁用)',"
                    + "`sort_order` INT NOT NULL DEFAULT 0 COMMENT '执行次序',"
                    + "`create_time` DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',"
                    + "INDEX `idx_rule_id` (`rule_id`),"
                    + "INDEX `idx_target_device` (`target_device_id`)"
                    + ") ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COMMENT='规则联动响应动作关联明细表';");

            // 6. 告警中心表 iot_alarm
            jdbcTemplate.execute("CREATE TABLE IF NOT EXISTS `iot_alarm` ("
                    + "`id` BIGINT NOT NULL AUTO_INCREMENT PRIMARY KEY COMMENT '自增主键ID',"
                    + "`alarm_id` VARCHAR(64) NOT NULL UNIQUE COMMENT '告警事件唯一ID',"
                    + "`rule_id` VARCHAR(64) DEFAULT NULL COMMENT '关联规则ID',"
                    + "`device_id` VARCHAR(64) NOT NULL COMMENT '告警源设备ID',"
                    + "`device_name` VARCHAR(128) DEFAULT NULL COMMENT '设备名称',"
                    + "`model_key` VARCHAR(64) DEFAULT NULL COMMENT '关联物模型Key',"
                    + "`metric` VARCHAR(64) DEFAULT NULL COMMENT '告警触发点位',"
                    + "`alarm_level` VARCHAR(16) NOT NULL DEFAULT 'WARN' COMMENT '告警级别 (INFO, WARN, HIGH, CRITICAL)',"
                    + "`alarm_title` VARCHAR(256) NOT NULL COMMENT '告警标题',"
                    + "`trigger_value` VARCHAR(128) DEFAULT NULL COMMENT '触发时实际测得数值',"
                    + "`threshold_desc` VARCHAR(128) DEFAULT NULL COMMENT '告警判定阈值描述',"
                    + "`status` TINYINT NOT NULL DEFAULT 0 COMMENT '告警处理状态 (0:未处理, 1:已确认, 2:已恢复结单)',"
                    + "`alarm_time` DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP COMMENT '告警发生时间',"
                    + "`handle_time` DATETIME DEFAULT NULL COMMENT '最后处理时间',"
                    + "`handle_user` VARCHAR(64) DEFAULT NULL COMMENT '处理人账号',"
                    + "`remark` VARCHAR(512) DEFAULT NULL COMMENT '处理备注记录'"
                    + ") ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COMMENT='告警事件明细表';");

            // 7. 日志管理表 iot_log
            jdbcTemplate.execute("CREATE TABLE IF NOT EXISTS `iot_log` ("
                    + "`id` BIGINT NOT NULL AUTO_INCREMENT PRIMARY KEY COMMENT '自增主键ID',"
                    + "`log_id` VARCHAR(64) NOT NULL UNIQUE COMMENT '日志记录唯一ID',"
                    + "`log_type` VARCHAR(32) NOT NULL DEFAULT 'DEVICE' COMMENT '日志类型 (DEVICE:设备日志, COMMAND:指令反控, SYSTEM:系统日志)',"
                    + "`device_id` VARCHAR(64) DEFAULT NULL COMMENT '关联设备ID',"
                    + "`model_key` VARCHAR(64) DEFAULT NULL COMMENT '关联物模型Key',"
                    + "`level` VARCHAR(16) NOT NULL DEFAULT 'INFO' COMMENT '日志级别 (DEBUG, INFO, WARN, ERROR)',"
                    + "`content` TEXT NOT NULL COMMENT '日志详细正文内容',"
                    + "`operator` VARCHAR(64) DEFAULT 'SYSTEM' COMMENT '操作人/触发源',"
                    + "`create_time` DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP COMMENT '日志产生时间'"
                    + ") ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COMMENT='系统运行与审计日志表';");

            // 8. 设备物理点位/地址映射表 iot_device_point_mapping
            jdbcTemplate.execute("CREATE TABLE IF NOT EXISTS `iot_device_point_mapping` ("
                    + "`id` BIGINT NOT NULL AUTO_INCREMENT PRIMARY KEY COMMENT '自增主键ID',"
                    + "`device_id` VARCHAR(64) NOT NULL COMMENT '关联设备ID',"
                    + "`model_key` VARCHAR(64) NOT NULL COMMENT '关联物模型Key',"
                    + "`point_code` VARCHAR(64) NOT NULL COMMENT '物模型点位编码',"
                    + "`point_name` VARCHAR(128) DEFAULT NULL COMMENT '点位中文名称',"
                    + "`physical_address` VARCHAR(255) DEFAULT NULL COMMENT '底层物理协议地址',"
                    + "`data_type` VARCHAR(32) DEFAULT 'DOUBLE' COMMENT '数据类型',"
                    + "`access_mode` VARCHAR(16) DEFAULT 'R' COMMENT '读写模式',"
                    + "`scale` DOUBLE DEFAULT 1.0 COMMENT '换算缩放系数 Scale',"
                    + "`offset` DOUBLE DEFAULT 0.0 COMMENT '换算零点偏置 Offset',"
                    + "`remark` VARCHAR(255) DEFAULT NULL COMMENT '备注信息',"
                    + "`create_time` DATETIME DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',"
                    + "`update_time` DATETIME DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP COMMENT '更新时间',"
                    + "UNIQUE KEY `uk_device_point` (`device_id`, `point_code`),"
                    + "KEY `idx_device_id` (`device_id`),"
                    + "KEY `idx_model_key` (`model_key`)"
                    + ") ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COMMENT='设备物模型点位物理地址映射表';");

            // 9. 系统用户表 sys_user (带加盐加密密码)
            jdbcTemplate.execute("CREATE TABLE IF NOT EXISTS `sys_user` ("
                    + "`id` BIGINT NOT NULL AUTO_INCREMENT PRIMARY KEY COMMENT '自增主键ID',"
                    + "`username` VARCHAR(64) NOT NULL UNIQUE COMMENT '登录用户名',"
                    + "`nickname` VARCHAR(64) NOT NULL COMMENT '用户昵称',"
                    + "`password` VARCHAR(128) NOT NULL COMMENT '加盐加密后的密码Hash (SHA-256)',"
                    + "`salt` VARCHAR(64) NOT NULL COMMENT '密码独立随机盐 (Salt)',"
                    + "`email` VARCHAR(128) DEFAULT NULL COMMENT '电子邮箱',"
                    + "`phone` VARCHAR(32) DEFAULT NULL COMMENT '联系手机号',"
                    + "`avatar` VARCHAR(255) DEFAULT NULL COMMENT '用户头像地址',"
                    + "`status` TINYINT NOT NULL DEFAULT 1 COMMENT '账号状态 (1:启用, 0:停用/冻结)',"
                    + "`last_login_ip` VARCHAR(64) DEFAULT NULL COMMENT '最后登录IP',"
                    + "`last_login_time` DATETIME DEFAULT NULL COMMENT '最后登录时间',"
                    + "`remark` VARCHAR(500) DEFAULT NULL COMMENT '备注信息',"
                    + "`create_time` DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',"
                    + "`update_time` DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP COMMENT '更新时间'"
                    + ") ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COMMENT='系统用户表';");

            // 10. 系统角色表 sys_role
            jdbcTemplate.execute("CREATE TABLE IF NOT EXISTS `sys_role` ("
                    + "`id` BIGINT NOT NULL AUTO_INCREMENT PRIMARY KEY COMMENT '自增角色ID',"
                    + "`role_key` VARCHAR(64) NOT NULL UNIQUE COMMENT '角色权限字符 (如 admin, operator, viewer)',"
                    + "`role_name` VARCHAR(64) NOT NULL COMMENT '角色名称',"
                    + "`sort_order` INT NOT NULL DEFAULT 0 COMMENT '显示排序',"
                    + "`status` TINYINT NOT NULL DEFAULT 1 COMMENT '角色状态 (1:启用, 0:停用)',"
                    + "`remark` VARCHAR(500) DEFAULT NULL COMMENT '备注',"
                    + "`create_time` DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',"
                    + "`update_time` DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP COMMENT '更新时间'"
                    + ") ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COMMENT='系统角色表';");

            // 11. 用户角色关联表 sys_user_role
            jdbcTemplate.execute("CREATE TABLE IF NOT EXISTS `sys_user_role` ("
                    + "`id` BIGINT NOT NULL AUTO_INCREMENT PRIMARY KEY COMMENT '自增主键ID',"
                    + "`user_id` BIGINT NOT NULL COMMENT '用户ID',"
                    + "`role_id` BIGINT NOT NULL COMMENT '角色ID',"
                    + "UNIQUE KEY `uk_user_role` (`user_id`, `role_id`),"
                    + "INDEX `idx_user_role_user` (`user_id`),"
                    + "INDEX `idx_user_role_role` (`role_id`)"
                    + ") ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COMMENT='用户角色关联表';");

            // 12. 系统权限表 sys_permission
            jdbcTemplate.execute("CREATE TABLE IF NOT EXISTS `sys_permission` ("
                    + "`id` BIGINT NOT NULL AUTO_INCREMENT PRIMARY KEY COMMENT '自增权限ID',"
                    + "`perm_key` VARCHAR(128) NOT NULL UNIQUE COMMENT '权限标识编码 (如 device:control)',"
                    + "`perm_name` VARCHAR(128) NOT NULL COMMENT '权限名称',"
                    + "`module` VARCHAR(64) DEFAULT NULL COMMENT '所属业务模块',"
                    + "`remark` VARCHAR(500) DEFAULT NULL COMMENT '备注',"
                    + "`create_time` DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间'"
                    + ") ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COMMENT='系统功能权限表';");

            // 13. 角色权限关联表 sys_role_permission
            jdbcTemplate.execute("CREATE TABLE IF NOT EXISTS `sys_role_permission` ("
                    + "`id` BIGINT NOT NULL AUTO_INCREMENT PRIMARY KEY COMMENT '自增主键ID',"
                    + "`role_id` BIGINT NOT NULL COMMENT '角色ID',"
                    + "`permission_id` BIGINT NOT NULL COMMENT '权限ID',"
                    + "UNIQUE KEY `uk_role_perm` (`role_id`, `permission_id`),"
                    + "INDEX `idx_role_perm_role` (`role_id`),"
                    + "INDEX `idx_role_perm_perm` (`permission_id`)"
                    + ") ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COMMENT='角色权限关联表';");

            // 14. 菜单与动态路由表 sys_menu
            jdbcTemplate.execute("CREATE TABLE IF NOT EXISTS `sys_menu` ("
                    + "`id` BIGINT NOT NULL AUTO_INCREMENT PRIMARY KEY COMMENT '自增菜单ID',"
                    + "`parent_id` BIGINT NOT NULL DEFAULT 0 COMMENT '父菜单ID (0为顶级目录)',"
                    + "`menu_name` VARCHAR(64) NOT NULL COMMENT '菜单名称',"
                    + "`path` VARCHAR(128) NOT NULL COMMENT '路由访问路径',"
                    + "`component` VARCHAR(128) DEFAULT NULL COMMENT '前端组件路径',"
                    + "`icon` VARCHAR(64) DEFAULT NULL COMMENT '菜单图标',"
                    + "`sort_order` INT NOT NULL DEFAULT 0 COMMENT '显示顺序',"
                    + "`menu_type` CHAR(1) NOT NULL DEFAULT 'C' COMMENT '菜单类型 (M:目录, C:菜单, F:按钮)',"
                    + "`perms` VARCHAR(128) DEFAULT NULL COMMENT '权限标识 (如 system:user:view)',"
                    + "`visible` TINYINT NOT NULL DEFAULT 1 COMMENT '是否显示 (1:显示, 0:隐藏)',"
                    + "`status` TINYINT NOT NULL DEFAULT 1 COMMENT '状态 (1:启用, 0:停用)',"
                    + "`remark` VARCHAR(500) DEFAULT NULL COMMENT '备注',"
                    + "`create_time` DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',"
                    + "`update_time` DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP COMMENT '更新时间',"
                    + "INDEX `idx_menu_parent` (`parent_id`)"
                    + ") ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COMMENT='系统菜单与动态路由配置表';");

            // 15. 角色菜单关联表 sys_role_menu
            jdbcTemplate.execute("CREATE TABLE IF NOT EXISTS `sys_role_menu` ("
                    + "`id` BIGINT NOT NULL AUTO_INCREMENT PRIMARY KEY COMMENT '自增主键ID',"
                    + "`role_id` BIGINT NOT NULL COMMENT '角色ID',"
                    + "`menu_id` BIGINT NOT NULL COMMENT '菜单ID',"
                    + "UNIQUE KEY `uk_role_menu` (`role_id`, `menu_id`),"
                    + "INDEX `idx_role_menu_role` (`role_id`),"
                    + "INDEX `idx_role_menu_menu` (`menu_id`)"
                    + ") ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COMMENT='角色菜单关联表';");

            // 16. 字典类型表 sys_dict_type
            jdbcTemplate.execute("CREATE TABLE IF NOT EXISTS `sys_dict_type` ("
                    + "`id` BIGINT NOT NULL AUTO_INCREMENT PRIMARY KEY COMMENT '自增字典类型ID',"
                    + "`dict_name` VARCHAR(100) NOT NULL COMMENT '字典名称',"
                    + "`dict_type` VARCHAR(100) NOT NULL UNIQUE COMMENT '字典类型唯一标识',"
                    + "`status` TINYINT NOT NULL DEFAULT 1 COMMENT '状态 (1:正常, 0:停用)',"
                    + "`remark` VARCHAR(500) DEFAULT NULL COMMENT '备注',"
                    + "`create_time` DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',"
                    + "`update_time` DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP COMMENT '更新时间'"
                    + ") ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COMMENT='系统字典类型表';");

            // 17. 字典数据表 sys_dict_data
            jdbcTemplate.execute("CREATE TABLE IF NOT EXISTS `sys_dict_data` ("
                    + "`id` BIGINT NOT NULL AUTO_INCREMENT PRIMARY KEY COMMENT '自增字典数据ID',"
                    + "`dict_type` VARCHAR(100) NOT NULL COMMENT '字典类型 (关联 sys_dict_type.dict_type)',"
                    + "`dict_label` VARCHAR(100) NOT NULL COMMENT '字典标签名称',"
                    + "`dict_value` VARCHAR(100) NOT NULL COMMENT '字典键值',"
                    + "`sort_order` INT NOT NULL DEFAULT 0 COMMENT '字典排序',"
                    + "`css_class` VARCHAR(100) DEFAULT NULL COMMENT '样式属性',"
                    + "`list_class` VARCHAR(100) DEFAULT NULL COMMENT '表格回显样式',"
                    + "`is_default` TINYINT NOT NULL DEFAULT 0 COMMENT '是否默认 (1:是, 0:否)',"
                    + "`status` TINYINT NOT NULL DEFAULT 1 COMMENT '状态 (1:正常, 0:停用)',"
                    + "`remark` VARCHAR(500) DEFAULT NULL COMMENT '备注',"
                    + "`create_time` DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',"
                    + "`update_time` DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP COMMENT '更新时间',"
                    + "INDEX `idx_dict_data_type` (`dict_type`, `sort_order`)"
                    + ") ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COMMENT='系统字典键值数据表';");

            migrateExistingTables();
            initSeedData();
            log.info("[DatabaseInitializer] MySQL 后台管理数据库表与种子数据初始化成功！");
        } catch (Exception e) {
            log.warn("[DatabaseInitializer] 数据库自动建表或连接出现提示（可能 MySQL 未启动或未创建数据库）: {}", e.getMessage());
        }
    }

    private void migrateExistingTables() {
        addColumnIfNotExists("iot_driver_info", "description", "VARCHAR(512) DEFAULT NULL COMMENT '驱动功能与协议详细描述'");
        addColumnIfNotExists("iot_driver_info", "service_url", "VARCHAR(256) DEFAULT NULL COMMENT '驱动微服务运行URL地址'");
        addColumnIfNotExists("iot_rule", "description", "VARCHAR(512) DEFAULT NULL COMMENT '规则描述'");

        dropColumnIfExists("iot_device", "address_mappings_json");

        renameColumnIfExists("iot_device", "product_key", "model_key", "VARCHAR(64) NOT NULL COMMENT '关联物模型Key'");
        renameColumnIfExists("iot_thing_model", "product_key", "model_key", "VARCHAR(64) NOT NULL COMMENT '物模型Key'");
        renameColumnIfExists("iot_rule", "product_key", "model_key", "VARCHAR(64) DEFAULT NULL COMMENT '关联物模型Key'");
        renameColumnIfExists("iot_alarm", "product_key", "model_key", "VARCHAR(64) DEFAULT NULL COMMENT '关联物模型Key'");
        renameColumnIfExists("iot_log", "product_key", "model_key", "VARCHAR(64) DEFAULT NULL COMMENT '关联物模型Key'");

        try {
            jdbcTemplate.update("UPDATE `iot_driver_info` SET `description` = '支持标准 Modbus TCP 保持寄存器与输入寄存器读写 (FC01-FC06/FC15/FC16 功能码)。', `manifest_yaml` = ? WHERE `driver_code` = 'modbus-tcp' AND (`manifest_yaml` IS NULL OR `manifest_yaml` = '' OR `manifest_yaml` NOT LIKE '%addressRule%')", MODBUS_YAML);
            jdbcTemplate.update("UPDATE `iot_driver_info` SET `description` = '支持西门子 S7-200 SMART / S7-300 / S7-1200 / S7-1500 PLC DB 块、M 区、I/Q 区数据读写。', `manifest_yaml` = ? WHERE `driver_code` = 's7-driver' AND (`manifest_yaml` IS NULL OR `manifest_yaml` = '' OR `manifest_yaml` NOT LIKE '%addressRule%')", S7_YAML);
            jdbcTemplate.update("UPDATE `iot_driver_info` SET `description` = '支持 OPC UA Client 节点订阅、Read 属性与 Write 节点控制。', `manifest_yaml` = ? WHERE `driver_code` = 'opc-ua' AND (`manifest_yaml` IS NULL OR `manifest_yaml` = '' OR `manifest_yaml` NOT LIKE '%addressRule%')", OPCUA_YAML);
        } catch (Exception e) {
            log.warn("[DatabaseInitializer] 刷新已有驱动描述提示: {}", e.getMessage());
        }
    }

    private void renameColumnIfExists(String tableName, String oldCol, String newCol, String colDef) {
        try {
            Integer count = jdbcTemplate.queryForObject(
                "SELECT COUNT(*) FROM information_schema.columns WHERE table_schema = DATABASE() AND table_name = ? AND column_name = ?",
                Integer.class, tableName, oldCol);
            if (count != null && count > 0) {
                jdbcTemplate.execute("ALTER TABLE `" + tableName + "` CHANGE COLUMN `" + oldCol + "` `" + newCol + "` " + colDef);
                log.info("[DatabaseInitializer] 已自动将表 `{}` 字段 `{}` 重命名为 `{}`", tableName, oldCol, newCol);
            }
        } catch (Exception e) {
            log.warn("[DatabaseInitializer] 重命名字段 `{}`.`{}` 时跳过: {}", tableName, oldCol, e.getMessage());
        }
    }

    private void addColumnIfNotExists(String tableName, String columnName, String columnDef) {
        try {
            Integer count = jdbcTemplate.queryForObject(
                "SELECT COUNT(*) FROM information_schema.columns WHERE table_schema = DATABASE() AND table_name = ? AND column_name = ?",
                Integer.class, tableName, columnName);
            if (count == null || count == 0) {
                jdbcTemplate.execute("ALTER TABLE `" + tableName + "` ADD COLUMN `" + columnName + "` " + columnDef);
                log.info("[DatabaseInitializer] 已自动为表 `{}` 补充缺失字段 `{}`", tableName, columnName);
            }
        } catch (Exception e) {
            log.warn("[DatabaseInitializer] 检查/添加字段 `{}`.`{}` 时跳过: {}", tableName, columnName, e.getMessage());
        }
    }

    private void dropColumnIfExists(String tableName, String columnName) {
        try {
            Integer count = jdbcTemplate.queryForObject(
                "SELECT COUNT(*) FROM information_schema.columns WHERE table_schema = DATABASE() AND table_name = ? AND column_name = ?",
                Integer.class, tableName, columnName);
            if (count != null && count > 0) {
                jdbcTemplate.execute("ALTER TABLE `" + tableName + "` DROP COLUMN `" + columnName + "`");
                log.info("[DatabaseInitializer] 已自动从表 `{}` 移除过时字段 `{}`", tableName, columnName);
            }
        } catch (Exception e) {
            log.warn("[DatabaseInitializer] 检查/移除字段 `{}`.`{}` 时跳过: {}", tableName, columnName, e.getMessage());
        }
    }

    private void initSeedData() {
        // 1. Seed Drivers
        Integer driverCount = jdbcTemplate.queryForObject("SELECT COUNT(*) FROM iot_driver_info", Integer.class);
        if (driverCount == null || driverCount == 0) {
            jdbcTemplate.update("INSERT INTO `iot_driver_info` (`driver_code`, `driver_name`, `version`, `jar_file_name`, `description`, `manifest_yaml`) VALUES "
                    + "('modbus-tcp', 'Modbus TCP 工业协议驱动', '2.0.0', 'unihh-driver-modbus.jar', '支持标准 Modbus TCP 保持寄存器与输入寄存器读写 (FC01-FC06/FC15/FC16 功能码)。', ?),"
                    + "('s7-driver', '西门子 S7 工业 PLC 协议驱动', '2.0.0', 'unihh-driver-s7.jar', '支持西门子 S7-200 SMART / S7-300 / S7-1200 / S7-1500 PLC DB 块、M 区、I/Q 区数据读写。', ?),"
                    + "('opc-ua', 'OPC UA 规范协议驱动', '2.0.0', 'unihh-driver-opcua.jar', '支持 OPC UA Client 节点订阅、Read 属性与 Write 节点控制。', ?)",
                    MODBUS_YAML, S7_YAML, OPCUA_YAML);
        }

        // 3. Seed Devices
        Integer deviceCount = jdbcTemplate.queryForObject("SELECT COUNT(*) FROM iot_device", Integer.class);
        if (deviceCount == null || deviceCount == 0) {
            jdbcTemplate.update("INSERT INTO `iot_device` (`device_id`, `device_name`, `model_key`, `driver_code`, `host`, `port`, `connection_params`, `status`, `acq_status`) VALUES "
                    + "('DEV_001', '1号高压车间电动机传感器', 'pk_demo', 'modbus-tcp', '192.168.1.100', 502, '{\"host\":\"192.168.1.100\",\"port\":502,\"slaveId\":1}', 1, 1),"
                    + "('DEV_002', '2号储罐压力PLC', 'pk_plc_s7', 's7-driver', '192.168.1.200', 102, '{\"host\":\"192.168.1.200\",\"port\":102,\"rack\":0,\"slot\":1}', 1, 1),"
                    + "('DEV_003', '3号流量计OPC Server', 'pk_opcua_server', 'opc-ua', '192.168.1.50', 4840, '{\"endpointUrl\":\"opc.tcp://192.168.1.50:4840\",\"securityPolicy\":\"None\"}', 1, 0)");
        }

        // 4. Seed Thing Models
        Integer modelCount = jdbcTemplate.queryForObject("SELECT COUNT(*) FROM iot_thing_model", Integer.class);
        if (modelCount == null || modelCount == 0) {
            jdbcTemplate.update("INSERT INTO `iot_thing_model` (`model_key`, `identifier`, `name`, `model_type`, `data_type`, `access_mode`, `unit`, `schema_json`) VALUES "
                    + "('pk_demo', 'temperature', '主轴承实时温度', 'PROPERTY', 'DOUBLE', 'R', '℃', '{\"description\":\"电机主轴承温度传感器只读数据\"}'),"
                    + "('pk_demo', 'humidity', '环境相对湿度', 'PROPERTY', 'DOUBLE', 'R', '%RH', '{\"description\":\"电机安装车间湿度\"}'),"
                    + "('pk_demo', 'pressure', '进气口设定压力', 'PROPERTY', 'DOUBLE', 'RW', 'kPa', '{\"description\":\"控制风机目标进气口压力\"}'),"
                    + "('pk_demo', 'targetSpeed', '变频目标转速', 'PROPERTY', 'INT', 'RW', 'RPM', '{\"description\":\"额定转速设定值\"}'),"
                    + "('pk_demo', 'autoCruiseMode', '全自动巡航开关', 'PROPERTY', 'BOOL', 'RW', NULL, '{\"description\":\"全自动巡航启停标志\"}'),"
                    + "('pk_demo', 'workMode', '工况模式选择', 'PROPERTY', 'ENUM', 'RW', NULL, '{\"description\":\"电机运行工况模式\",\"enumOptions\":[{\"value\":0,\"label\":\"待机模式\"},{\"value\":1,\"label\":\"自动运行\"},{\"value\":2,\"label\":\"手动维护\"},{\"value\":3,\"label\":\"节能巡检\"}]}'),"
                    + "('pk_demo', 'startCoolingFan', '开启/调节冷却风扇', 'SERVICE', 'VOID', 'W', NULL, '{\"description\":\"下发风扇编号及目标风速指令\",\"inputParams\":[{\"pointCode\":\"fanId\",\"pointName\":\"风扇编号\",\"dataType\":\"INT\"},{\"pointCode\":\"speed\",\"pointName\":\"目标转速\",\"dataType\":\"INT\"}]}'),"
                    + "('pk_demo', 'setRelay', '继电器开关控制', 'SERVICE', 'VOID', 'W', NULL, '{\"description\":\"控制输出通道闭合/断开\",\"inputParams\":[{\"pointCode\":\"channel\",\"pointName\":\"通道号\",\"dataType\":\"INT\"},{\"pointCode\":\"state\",\"pointName\":\"开关状态\",\"dataType\":\"BOOL\"}]}'),"
                    + "('pk_demo', 'OVER_TEMP_WARN', '电机轴承超温自保护', 'EVENT', 'VOID', 'R', NULL, '{\"eventType\":\"ALERT\",\"eventLevel\":\"WARN\",\"description\":\"硬件微控制器检测到轴承温度异常过热主动关断并上报\"}'),"
                    + "('pk_plc_s7', 'plcRunStatus', 'PLC 运行状态', 'PROPERTY', 'BOOL', 'R', '', '{\"description\":\"PLC 当前 RUN/STOP 标志\"}'),"
                    + "('pk_plc_s7', 'errorCode', '系统故障代码', 'PROPERTY', 'INT', 'R', '', '{\"description\":\"故障报警码\"}'),"
                    + "('pk_plc_s7', 'targetPressure', '储罐加压目标设定值', 'PROPERTY', 'DOUBLE', 'RW', 'kPa', '{\"description\":\"西门子 PLC 储罐远控加压设定值 (DB1.DBD0)\",\"min\":0.0,\"max\":200.0}'),"
                    + "('pk_plc_s7', 'remoteControlCmd', '远程启停遥控开关', 'PROPERTY', 'BOOL', 'RW', '', '{\"description\":\"S7 PLC 远程运行/停止逻辑反写控制 (DB1.DBX4.0)\"}'),"
                    + "('pk_plc_s7', 'resetFault', '告警故障一键复位', 'SERVICE', 'VOID', 'W', NULL, '{\"description\":\"清除 PLC 故障状态与历史锁死标志\",\"inputParams\":[{\"pointCode\":\"clearAll\",\"pointName\":\"清除告警\",\"dataType\":\"BOOL\"}]}'),"
                    + "('pk_plc_s7', 'OVER_PRESSURE_ALARM', '储罐超压紧急防护报警', 'EVENT', 'VOID', 'R', NULL, '{\"eventType\":\"ALERT\",\"eventLevel\":\"CRITICAL\",\"description\":\"S7 PLC 监测到储罐压力越界触发高压切断系统并上报\"}')");
        }

        seedMissingThingModels();

        // 5. Seed Rules
        Integer ruleCount = jdbcTemplate.queryForObject("SELECT COUNT(*) FROM iot_rule", Integer.class);
        if (ruleCount == null || ruleCount == 0) {
            jdbcTemplate.update("INSERT INTO `iot_rule` (`rule_id`, `rule_name`, `model_key`, `source_device_id`, `source_point`, `condition_mode`, `operator`, `threshold_value`, `min_val`, `max_val`, `enabled`, `description`) VALUES "
                    + "('RULE_101', '1号主轴承过热防护与自动降速规则', 'pk_demo', 'DEV_001', 'temperature', 'SINGLE', '>', 75.0, 0, 0, 1, '当1号轴承温度超过75℃时触发自动开启风扇及急停预警'),"
                    + "('RULE_102', '2号储罐压力上限反控开阀规则', 'pk_plc_s7', 'DEV_002', 'pressure', 'RANGE_OUTSIDE', '>', 0, 10.0, 100.0, 1, '储罐压力不在10~100kPa安全范围自动报警'),"
                    + "('RULE_103', '车间环境湿度过高排风规则', 'pk_demo', 'DEV_001', 'humidity', 'SINGLE', '>', 85.0, 0, 0, 0, '湿度超过85%RH时自动发送预警提示')");
        }

        // 5.1 Seed Rule Actions Detail Table
        Integer actionCount = jdbcTemplate.queryForObject("SELECT COUNT(*) FROM iot_rule_action", Integer.class);
        if (actionCount == null || actionCount == 0) {
            jdbcTemplate.update("INSERT INTO `iot_rule_action` (`rule_id`, `action_id`, `action_type`, `action_name`, `target_device_id`, `target_service_code`, `service_input_values`, `alarm_level`, `alarm_content`, `log_level`, `log_content`, `enabled`, `sort_order`) VALUES "
                    + "('RULE_101', 'ACT_101', 'ALARM', '触发电机主轴承急停告警', 'DEV_001', NULL, NULL, 'CRITICAL', '当1号轴承温度超过75℃时触发自动开启风扇及急停预警', NULL, NULL, 1, 0),"
                    + "('RULE_101', 'ACT_102', 'SERVICE', '开启辅助冷却风扇系统', 'DEV_001', 'startCoolingFan', '{\"speed\":3000,\"mode\":\"AUTO\"}', NULL, NULL, NULL, NULL, 1, 1),"
                    + "('RULE_102', 'ACT_201', 'ALARM', '储罐高压越界告警', 'DEV_002', NULL, NULL, 'HIGH', '储罐压力不在10~100kPa安全范围自动报警', NULL, NULL, 1, 0),"
                    + "('RULE_103', 'ACT_301', 'LOG', '发布湿度偏高审计日志', 'DEV_001', NULL, NULL, NULL, NULL, 'INFO', '[规则引擎] 检测到1号电机车间湿度偏高', 1, 0)");
        }

        // 6. Seed Alarms
        Integer alarmCount = jdbcTemplate.queryForObject("SELECT COUNT(*) FROM iot_alarm", Integer.class);
        if (alarmCount == null || alarmCount == 0) {
            jdbcTemplate.update("INSERT INTO `iot_alarm` (`alarm_id`, `rule_id`, `device_id`, `device_name`, `model_key`, `metric`, `alarm_level`, `alarm_title`, `trigger_value`, `threshold_desc`, `status`, `alarm_time`, `handle_time`, `handle_user`, `remark`) VALUES "
                    + "('ALM_20260909_001', 'RULE_101', 'DEV_001', '1号高压车间电动机传感器', 'pk_demo', 'temperature', 'CRITICAL', '1号电机主轴承温度过高', '82.4 ℃', '> 75.0 ℃', 0, NOW(), NULL, NULL, NULL),"
                    + "('ALM_20260909_002', 'RULE_102', 'DEV_002', '2号储罐压力PLC', 'pk_plc_s7', 'pressure', 'HIGH', '2号储罐压力超界报警', '105.2 kPa', 'Out of 10~100 kPa', 1, DATE_SUB(NOW(), INTERVAL 1 HOUR), NOW(), 'admin', '现场值班员已确认，降压阀正常排气'),"
                    + "('ALM_20260909_003', 'RULE_103', 'DEV_001', '1号高压车间电动机传感器', 'pk_demo', 'humidity', 'WARN', '1号车间环境湿度偏高', '87.1 %RH', '> 85.0 %RH', 2, DATE_SUB(NOW(), INTERVAL 2 HOUR), DATE_SUB(NOW(), INTERVAL 1 HOUR), 'system', '湿度自然回落至安全区间，系统自动关单')");
        }

        // 7. Seed Logs
        Integer logCount = jdbcTemplate.queryForObject("SELECT COUNT(*) FROM iot_log", Integer.class);
        if (logCount == null || logCount == 0) {
            jdbcTemplate.update("INSERT INTO `iot_log` (`log_id`, `log_type`, `device_id`, `model_key`, `level`, `content`, `operator`) VALUES "
                    + "('LOG_20260909_0001', 'COMMAND', 'DEV_001', 'pk_demo', 'INFO', '规则引擎 [RULE_101] 触发反控: 下发服务 startCoolingFan, 参数: {\"fanId\":1,\"speed\":3000}', 'RULE_ENGINE'),"
                    + "('LOG_20260909_0002', 'COMMAND', 'DEV_002', 'pk_plc_s7', 'INFO', '强降温联动 - 开启高功率风机: 下发服务 switchPower, 参数: {\"powerState\":\"ON\",\"speedLevel\":3}', 'RULE_ENGINE'),"
                    + "('LOG_20260909_0003', 'COMMAND', 'DEV_003', 'pk_opcua_server', 'ERROR', '自动关阀 - 管道压力异常紧急响应: 下发服务 closeValve 失败 [超时 5000ms]', 'RULE_ENGINE'),"
                    + "('LOG_20260909_0004', 'COMMAND', 'DEV_001', 'pk_demo', 'INFO', '控制指令下发: 设置变频目标转速 targetSpeed=1500', 'admin'),"
                    + "('LOG_20260909_0005', 'COMMAND', 'DEV_002', 'pk_plc_s7', 'INFO', '控制指令下发: 储罐加压复位 resetFault, 参数: {\"clearAll\":true}', 'operator01'),"
                    + "('LOG_20260909_0006', 'SYSTEM', 'DEV_001', 'pk_demo', 'INFO', '用户 admin 成功登录系统管理控制台 [IP: 192.168.1.120, 客户端: Chrome/Win11]', 'admin'),"
                    + "('LOG_20260909_0007', 'SYSTEM', 'DEV_002', 'pk_plc_s7', 'INFO', '用户 operator01 成功登录系统控制台 [IP: 192.168.1.145, 客户端: Edge/Win10]', 'operator01'),"
                    + "('LOG_20260909_0008', 'SYSTEM', 'DEV_003', 'pk_opcua_server', 'WARN', '用户 test_user 尝试登录系统失败 [密码错误三次, IP: 222.128.10.88]', 'test_user'),"
                    + "('LOG_20260909_0009', 'OPERATION', 'DEV_001', 'pk_demo', 'INFO', '规则管理: 修改告警规则 [RULE_101] 联动动作参数, 将临界阈值从 70.0℃ 调整为 75.0℃', 'admin'),"
                    + "('LOG_20260909_0010', 'OPERATION', 'DEV_003', 'pk_opcua_server', 'INFO', '设备管理: 新增设备节点 [DEV_003], 注册 OPC UA 工业网关', 'admin'),"
                    + "('LOG_20260909_0011', 'OPERATION', 'DEV_002', 'pk_plc_s7', 'INFO', '物模型管理: 更新物模型 [pk_plc_s7] 储罐压力点位物理映射', 'operator01'),"
                    + "('LOG_20260909_0012', 'DEVICE', 'DEV_001', 'pk_demo', 'INFO', '1号高压车间电动机传感器 Modbus TCP 通讯握手正常', 'modbus-tcp-driver'),"
                    + "('LOG_20260909_0013', 'DEVICE', 'DEV_002', 'pk_plc_s7', 'INFO', '2号储罐压力PLC 西门子 S7 协议轮询采集数据正常', 's7-driver'),"
                    + "('LOG_20260909_0014', 'DEVICE', 'DEV_003', 'pk_opcua_server', 'WARN', 'OPC UA 驱动网络闪断重新建立长连接', 'opc-ua-driver'),"
                    + "('LOG_20260909_0015', 'DEVICE', 'DEV_001', 'pk_demo', 'ERROR', '1号电机温度点位采集超时，触发硬件通道重试', 'modbus-tcp-driver')");
        }

        // 8. Seed Point Address Mappings
        Integer mappingCount = jdbcTemplate.queryForObject("SELECT COUNT(*) FROM iot_device_point_mapping", Integer.class);
        if (mappingCount == null || mappingCount == 0) {
            jdbcTemplate.update("INSERT INTO `iot_device_point_mapping` (`device_id`, `model_key`, `point_code`, `point_name`, `physical_address`, `data_type`, `access_mode`, `scale`, `offset`) VALUES "
                    + "('DEV_001', 'pk_demo', 'temperature', '主轴承实时温度', '40001', 'DOUBLE', 'R', 1.0, 0.0),"
                    + "('DEV_001', 'pk_demo', 'humidity', '环境相对湿度', '40003', 'DOUBLE', 'R', 1.0, 0.0),"
                    + "('DEV_001', 'pk_demo', 'pressure', '进气口设定压力', '40005', 'DOUBLE', 'RW', 1.0, 0.0),"
                    + "('DEV_001', 'pk_demo', 'targetSpeed', '变频目标转速', '40007', 'INT', 'RW', 1.0, 0.0),"
                    + "('DEV_001', 'pk_demo', 'autoCruiseMode', '全自动巡航开关', '00001', 'BOOL', 'RW', 1.0, 0.0),"
                    + "('DEV_001', 'pk_demo', 'workMode', '工况模式选择', '40008', 'ENUM', 'RW', 1.0, 0.0)");
        }

        // 9. Seed RBAC Users, Roles, Permissions, Menus, Dicts
        seedRbacAndSystemData();
    }

    private void seedRbacAndSystemData() {
        try {
            // 9.1 Roles
            Integer roleCount = jdbcTemplate.queryForObject("SELECT COUNT(*) FROM sys_role", Integer.class);
            if (roleCount == null || roleCount == 0) {
                jdbcTemplate.update("INSERT INTO `sys_role` (`id`, `role_key`, `role_name`, `sort_order`, `status`, `remark`) VALUES "
                        + "(1, 'admin', '超级管理员', 1, 1, '系统内置最高权限管理员'),"
                        + "(2, 'operator', '工控运维员', 2, 1, '负责现场设备调试、规则与告警处置'),"
                        + "(3, 'viewer', '访客观察员', 3, 1, '仅具有平台数据只读监控权限')");
            }

            // 9.2 Users (加盐加密密码)
            Integer userCount = jdbcTemplate.queryForObject("SELECT COUNT(*) FROM sys_user", Integer.class);
            if (userCount == null || userCount == 0) {
                // admin / admin123  (salt: 9f2b8c4d1e0a7f5b3a6c9e2d8f1b4a7c, hash: 78065aa34e6590a9135df5d07046e9064bc84a00d3e314c8e71e7414e341e2fa)
                // operator / operator123 (salt: 4d1e0a7f5b3a6c9e2d8f1b4a7c9f2b8c, hash: fb79c7a14084b53980735dda477fee1f3d8b3e6e75b516f0c05c5c6e4f37eb22)
                jdbcTemplate.update("INSERT INTO `sys_user` (`id`, `username`, `nickname`, `password`, `salt`, `email`, `phone`, `status`, `remark`) VALUES "
                        + "(1, 'admin', '超级管理员', '78065aa34e6590a9135df5d07046e9064bc84a00d3e314c8e71e7414e341e2fa', '9f2b8c4d1e0a7f5b3a6c9e2d8f1b4a7c', 'admin@unihh.com', '13800138000', 1, '系统预设超级管理员 (密码: admin123)'),"
                        + "(2, 'operator', '工控运维专员', 'fb79c7a14084b53980735dda477fee1f3d8b3e6e75b516f0c05c5c6e4f37eb22', '4d1e0a7f5b3a6c9e2d8f1b4a7c9f2b8c', 'operator@unihh.com', '13900139000', 1, '现场工控与设备采集运维人员 (密码: operator123)')");
            }

            // 9.3 User-Role Mapping
            Integer urCount = jdbcTemplate.queryForObject("SELECT COUNT(*) FROM sys_user_role", Integer.class);
            if (urCount == null || urCount == 0) {
                jdbcTemplate.update("INSERT INTO `sys_user_role` (`user_id`, `role_id`) VALUES (1, 1), (2, 2)");
            }

            // 9.4 Permissions (全面细粒度系统功能权限初始化与同步)
            jdbcTemplate.execute("INSERT INTO `sys_permission` (`perm_key`, `perm_name`, `module`, `remark`) VALUES "
                    + "('system:user:list', '用户查询', '系统管理', '查看用户列表与账号详情'),"
                    + "('system:user:add', '用户新增', '系统管理', '创建系统操作人员账号'),"
                    + "('system:user:edit', '用户编辑', '系统管理', '修改信息、重置密码与启停状态'),"
                    + "('system:user:delete', '用户删除', '系统管理', '删除操作人员账号'),"
                    + "('system:role:list', '角色查询', '系统管理', '查看系统角色列表'),"
                    + "('system:role:add', '角色新增', '系统管理', '新建平台角色身份'),"
                    + "('system:role:edit', '角色编辑', '系统管理', '修改角色、授权功能权限与分配菜单'),"
                    + "('system:role:delete', '角色删除', '系统管理', '删除平台角色'),"
                    + "('system:menu:list', '菜单查询', '系统管理', '查看动态路由与菜单树'),"
                    + "('system:menu:add', '菜单新增', '系统管理', '新增顶级菜单或子项'),"
                    + "('system:menu:edit', '菜单编辑', '系统管理', '修改菜单信息与权限标识'),"
                    + "('system:menu:delete', '菜单删除', '系统管理', '删除系统菜单项'),"
                    + "('system:dict:list', '字典查询', '系统管理', '查看数据字典类型与键值'),"
                    + "('system:dict:add', '字典新增', '系统管理', '新增字典类型与字典键值'),"
                    + "('system:dict:edit', '字典修改', '系统管理', '修改字典类型与字典键值'),"
                    + "('system:dict:delete', '字典删除', '系统管理', '删除字典类型与字典键值'),"
                    + "('device:list', '设备查看', '设备管理', '查看物理设备列表与状态'),"
                    + "('device:add', '设备新增', '设备管理', '注册接入物理硬件设备'),"
                    + "('device:edit', '设备编辑', '设备管理', '编辑修改物理设备配置与点位映射'),"
                    + "('device:control', '设备反控', '设备管理', '下发设备物模型服务指令与控制调试'),"
                    + "('device:delete', '设备删除', '设备管理', '移除边缘网关或终端设备'),"
                    + "('driver:list', '驱动查看', '驱动管理', '查看协议驱动列表与状态'),"
                    + "('driver:edit', '驱动管理', '驱动管理', '注册、更新驱动配置与启停驱动'),"
                    + "('thingmodel:list', '物模型查看', '物模型管理', '查看物模型属性与服务'),"
                    + "('thingmodel:add', '物模型新增', '物模型管理', '新建物模型基础模板'),"
                    + "('thingmodel:edit', '物模型编辑', '物模型管理', '编辑物模型模板名称、编码与分类'),"
                    + "('thingmodel:design', '设计物模型', '物模型管理', '设计物模型 Schema，定义与导入点位属性、控制服务与告警事件'),"
                    + "('thingmodel:delete', '物模型删除', '物模型管理', '删除未被引用的物模型模板'),"
                    + "('data:monitor', '数据实时监控', '数据中心', '查看点位实时趋势'),"
                    + "('rule:list', '规则查看', '规则引擎', '查看阈值与告警联动规则'),"
                    + "('rule:add', '规则新增', '规则引擎', '新建联动告警与自动化反控规则'),"
                    + "('rule:edit', '规则维护', '规则引擎', '配置与修改联动规则判定与反控动作'),"
                    + "('rule:delete', '规则删除', '规则引擎', '删除联动规则'),"
                    + "('alarm:list', '告警查看', '告警中心', '查看系统告警事件记录'),"
                    + "('alarm:handle', '告警处理', '告警中心', '确认与闭环恢复告警'),"
                    + "('alarm:delete', '告警删除', '告警中心', '物理删除与清理历史告警记录'),"
                    + "('log:list', '日志查看', '日志管理', '查看系统运行与操作审计日志'),"
                    + "('log:clear', '日志清理', '日志管理', '清空与归档审计历史日志'),"
                    + "('dashboard:view', '大屏监控', '监控大屏', '查看全景数据吞吐与设备拓扑大屏') "
                    + "ON DUPLICATE KEY UPDATE `perm_name` = VALUES(`perm_name`), `module` = VALUES(`module`), `remark` = VALUES(`remark`);");

            // 9.5 Role-Permission Mapping (admin 拥有所有权限, operator 拥有主要业务及物模型设计权限)
            jdbcTemplate.execute("INSERT IGNORE INTO `sys_role_permission` (`role_id`, `permission_id`) "
                    + "SELECT 1, `id` FROM `sys_permission`;");
            jdbcTemplate.execute("INSERT IGNORE INTO `sys_role_permission` (`role_id`, `permission_id`) "
                    + "SELECT 2, `id` FROM `sys_permission` WHERE `perm_key` IN ("
                    + "'device:list', 'device:add', 'device:edit', 'device:control', 'driver:list', "
                    + "'thingmodel:list', 'thingmodel:add', 'thingmodel:edit', 'thingmodel:design', "
                    + "'data:monitor', 'rule:list', 'rule:add', 'rule:edit', 'alarm:list', 'alarm:handle', 'log:list', 'dashboard:view');");

            // 9.6 Menus (动态路由菜单结构)
            Integer menuCount = jdbcTemplate.queryForObject("SELECT COUNT(*) FROM sys_menu", Integer.class);
            if (menuCount == null || menuCount == 0) {
                jdbcTemplate.update("INSERT INTO `sys_menu` (`id`, `parent_id`, `menu_name`, `path`, `component`, `icon`, `sort_order`, `menu_type`, `perms`, `visible`, `status`, `remark`) VALUES "
                        + "(1, 0, '系统仪表盘', '/dashboard', 'views/dashboard/OverviewDashboard.vue', 'activity', 1, 'C', 'dashboard:view', 1, 1, '全景数据监控与概览'),"
                        + "(2, 0, '驱动管理', '/drivers', 'views/protocol/ProtocolDrivers.vue', 'server', 2, 'C', 'driver:list', 1, 1, '工业协议驱动管理'),"
                        + "(3, 0, '设备管理', '/devices', 'views/devices/DeviceList.vue', 'cpu', 3, 'C', 'device:list', 1, 1, '边缘硬件网关与设备管理'),"
                        + "(4, 0, '数据管理', '/data', 'views/data/PointDataMonitor.vue', 'database', 4, 'C', 'data:monitor', 1, 1, '点位实时与历史数据'),"
                        + "(5, 0, '物模型管理', '/thingmodels', 'views/thingmodels/ThingModelManager.vue', 'database', 5, 'C', 'thingmodel:list', 1, 1, '物模型Schema元数据定义'),"
                        + "(6, 0, '规则管理', '/rules', 'views/rules/RuleManager.vue', 'refresh', 6, 'C', 'rule:list', 1, 1, '轻量级联动规则引擎'),"
                        + "(7, 0, '告警中心', '/alarms', 'views/alarms/AlarmEvents.vue', 'bell', 7, 'C', 'alarm:list', 1, 1, '异常状态与事件报警'),"
                        + "(8, 0, '日志管理', '/logs', 'views/logs/LogManager.vue', 'file-text', 8, 'C', 'log:list', 1, 1, '运行与审计日志中心'),"
                        + "(9, 0, '系统管理', '/system', 'Layout', 'shield', 9, 'M', 'system:manage', 1, 1, '系统与用户权限管理目录'),"
                        + "(10, 9, '用户管理', '/system/users', 'views/system/UserList.vue', 'user', 1, 'C', 'system:user:list', 1, 1, '系统账号管理'),"
                        + "(11, 9, '角色权限', '/system/roles', 'views/system/RoleList.vue', 'shield', 2, 'C', 'system:role:list', 1, 1, '角色与权限分配'),"
                        + "(12, 9, '菜单路由', '/system/menus', 'views/system/MenuList.vue', 'menu', 3, 'C', 'system:menu:list', 1, 1, '动态路由菜单维护'),"
                        + "(13, 9, '字典管理', '/system/dicts', 'views/system/DictList.vue', 'file-text', 4, 'C', 'system:dict:list', 1, 1, '数据字典与常量配置')");
            }

            // 9.7 Role-Menu Mapping
            Integer rmCount = jdbcTemplate.queryForObject("SELECT COUNT(*) FROM sys_role_menu", Integer.class);
            if (rmCount == null || rmCount == 0) {
                // admin (role 1) 拥有全部 1~13 菜单
                StringBuilder adminMenuSql = new StringBuilder("INSERT INTO `sys_role_menu` (`role_id`, `menu_id`) VALUES ");
                for (int i = 1; i <= 13; i++) {
                    if (i > 1) adminMenuSql.append(",");
                    adminMenuSql.append("(1, ").append(i).append(")");
                }
                jdbcTemplate.update(adminMenuSql.toString());

                // operator (role 2) 拥有前 8 个业务菜单
                jdbcTemplate.update("INSERT INTO `sys_role_menu` (`role_id`, `menu_id`) VALUES "
                        + "(2, 1), (2, 2), (2, 3), (2, 4), (2, 5), (2, 6), (2, 7), (2, 8)");
            }

            // 9.8 Dict Type
            Integer dtCount = jdbcTemplate.queryForObject("SELECT COUNT(*) FROM sys_dict_type", Integer.class);
            if (dtCount == null || dtCount == 0) {
                jdbcTemplate.update("INSERT INTO `sys_dict_type` (`id`, `dict_name`, `dict_type`, `status`, `remark`) VALUES "
                        + "(1, '系统用户状态', 'sys_user_status', 1, '用户账号可用状态'),"
                        + "(2, '设备通信在线状态', 'sys_device_status', 1, '物理设备通道网络状态'),"
                        + "(3, '告警事件紧急等级', 'iot_alarm_level', 1, '系统告警级别'),"
                        + "(4, '物模型数据类型', 'iot_data_type', 1, '物模型点位与参数数据类型')");
            }

            // 9.9 Dict Data
            Integer ddCount = jdbcTemplate.queryForObject("SELECT COUNT(*) FROM sys_dict_data", Integer.class);
            if (ddCount == null || ddCount == 0) {
                jdbcTemplate.update("INSERT INTO `sys_dict_data` (`dict_type`, `dict_label`, `dict_value`, `sort_order`, `css_class`, `list_class`, `is_default`, `status`, `remark`) VALUES "
                        + "('sys_user_status', '正常启用', '1', 1, 'el-tag--success', 'success', 1, 1, '用户账号正常使用'),"
                        + "('sys_user_status', '停用冻结', '0', 2, 'el-tag--danger', 'danger', 0, 1, '账号已被管理员停用'),"
                        + "('sys_device_status', '在线运行', '1', 1, 'el-tag--success', 'success', 1, 1, '通道网络心跳正常'),"
                        + "('sys_device_status', '未连接/离线', '0', 2, 'el-tag--warning', 'warning', 0, 1, 'TCP/串口未建立连接'),"
                        + "('sys_device_status', '连接异常', '2', 3, 'el-tag--danger', 'danger', 0, 1, '通信超时或数据校验失败'),"
                        + "('iot_alarm_level', '普通提示', 'INFO', 1, 'el-tag--info', 'info', 0, 1, '常规参数越界提示'),"
                        + "('iot_alarm_level', '一般告警', 'WARN', 2, 'el-tag--warning', 'warning', 1, 1, '非致命异常'),"
                        + "('iot_alarm_level', '严重超限', 'HIGH', 3, 'el-tag--danger', 'danger', 0, 1, '工艺参数严重超标'),"
                        + "('iot_alarm_level', '紧急险情', 'CRITICAL', 4, 'el-tag--danger', 'danger', 0, 1, '触发设备急停保护'),"
                        + "('iot_data_type', '双精度浮点 (Double)', 'DOUBLE', 1, '', 'primary', 1, 1, '64位 IEEE 754 浮点'),"
                        + "('iot_data_type', '整型 (Integer)', 'INT', 2, '', 'primary', 0, 1, '16位/32位整型'),"
                        + "('iot_data_type', '布尔开关 (Boolean)', 'BOOL', 3, '', 'success', 0, 1, '0 或 1 逻辑开关'),"
                        + "('iot_data_type', '文本字符串 (String)', 'STRING', 4, '', 'info', 0, 1, '字符串文本'),"
                        + "('iot_data_type', '枚举状态 (Enum)', 'ENUM', 5, '', 'warning', 0, 1, '离散枚举代码')");
            }
        } catch (Exception e) {
            log.warn("[DatabaseInitializer] 初始化用户/角色/权限/菜单/字典数据提示: {}", e.getMessage());
        }
    }

    private void seedMissingThingModels() {
        try {
            // Ensure pk_plc_s7 properties, services and events exist
            insertThingModelIfMissing("pk_plc_s7", "plcRunStatus", "PLC 运行状态", "PROPERTY", "BOOL", "R", "", "{\"description\":\"PLC 当前 RUN/STOP 标志\"}");
            insertThingModelIfMissing("pk_plc_s7", "errorCode", "系统故障代码", "PROPERTY", "INT", "R", "", "{\"description\":\"故障报警码\"}");
            insertThingModelIfMissing("pk_plc_s7", "targetPressure", "储罐加压目标设定值", "PROPERTY", "DOUBLE", "RW", "kPa", "{\"description\":\"西门子 PLC 储罐远控加压设定值 (DB1.DBD0)\",\"min\":0.0,\"max\":200.0}");
            insertThingModelIfMissing("pk_plc_s7", "remoteControlCmd", "远程启停遥控开关", "PROPERTY", "BOOL", "RW", "", "{\"description\":\"S7 PLC 远程运行/停止逻辑反写控制 (DB1.DBX4.0)\"}");
            insertThingModelIfMissing("pk_plc_s7", "resetFault", "告警故障一键复位", "SERVICE", "VOID", "W", null, "{\"description\":\"清除 PLC 故障状态与历史锁死标志\",\"inputParams\":[{\"pointCode\":\"clearAll\",\"pointName\":\"清除告警\",\"dataType\":\"BOOL\"}]}");
            insertThingModelIfMissing("pk_plc_s7", "OVER_PRESSURE_ALARM", "储罐超压紧急防护报警", "EVENT", "VOID", "R", null, "{\"eventType\":\"ALERT\",\"eventLevel\":\"CRITICAL\",\"description\":\"S7 PLC 监测到储罐压力越界触发高压切断系统并上报\"}");

            // Ensure pk_opcua_server properties, services and events exist
            insertThingModelIfMissing("pk_opcua_server", "flowRate", "管道实时流量", "PROPERTY", "DOUBLE", "R", "m³/h", "{\"description\":\"OPC UA 流量计实时只读测点\"}");
            insertThingModelIfMissing("pk_opcua_server", "valvePosition", "调节阀目标开度", "PROPERTY", "DOUBLE", "RW", "%", "{\"description\":\"OPC UA 节点 Valve.Pos 远程反写阀门开度 (0 ~ 100%)\",\"min\":0.0,\"max\":100.0}");
            insertThingModelIfMissing("pk_opcua_server", "pumpSwitch", "循环泵远程开关", "PROPERTY", "BOOL", "RW", "", "{\"description\":\"OPC UA 节点 Pump.Switch 逻辑控制开关\"}");
            insertThingModelIfMissing("pk_opcua_server", "calibrateSensor", "传感器零点校准", "SERVICE", "VOID", "W", null, "{\"description\":\"下发 OPC UA 传感器校准数值\",\"inputParams\":[{\"pointCode\":\"offset\",\"pointName\":\"零点偏移量\",\"dataType\":\"DOUBLE\"}]}");
            insertThingModelIfMissing("pk_opcua_server", "SENSOR_FAULT_ALARM", "节点传感器通讯异常", "EVENT", "VOID", "R", null, "{\"eventType\":\"FAULT\",\"eventLevel\":\"HIGH\",\"description\":\"OPC UA 节点通讯握手超时报警\"}");
        } catch (Exception e) {
            log.warn("[DatabaseInitializer] 补全物模型种子数据提示: {}", e.getMessage());
        }
    }

    private void insertThingModelIfMissing(String modelKey, String identifier, String name, String modelType, String dataType, String accessMode, String unit, String schemaJson) {
        try {
            Integer count = jdbcTemplate.queryForObject(
                "SELECT COUNT(*) FROM iot_thing_model WHERE model_key = ? AND identifier = ? AND model_type = ?",
                Integer.class, modelKey, identifier, modelType);
            if (count == null || count == 0) {
                jdbcTemplate.update("INSERT INTO iot_thing_model (model_key, identifier, name, model_type, data_type, access_mode, unit, schema_json) VALUES (?, ?, ?, ?, ?, ?, ?, ?)",
                    modelKey, identifier, name, modelType, dataType, accessMode, unit, schemaJson);
            }
        } catch (Exception ignored) {}
    }
}
