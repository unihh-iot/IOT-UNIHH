-- ===================================================================
-- UNIHH-IOT 物联网平台 TDengine 3.x 时序数据库初始化脚本
-- Author: deversun (unihh.com)
-- ===================================================================

CREATE DATABASE IF NOT EXISTS unihh_iot_ts KEEP 3650 DAYS 10 BLOCKS 6;
USE unihh_iot_ts;

-- 创建点位数据超级表 (Super Table)
CREATE STABLE IF NOT EXISTS device_meters (
    ts TIMESTAMP,
    val DOUBLE,
    quality INT
) TAGS (
    model_key VARCHAR(64),
    device_id VARCHAR(64),
    point_code VARCHAR(64)
);
