/**
 * 前端系统全局常量配置
 */
export const API_CONFIG = {
  BASE_URL: 'https://demoapi.unihh.com/api/v1',
  TIMEOUT: 10000
};

export const DEVICE_STATUS = {
  ONLINE: 1,
  OFFLINE: 0,
  ERROR: 2
};

export const DEVICE_STATUS_MAP = {
  1: { label: '在线通道', class: 'iot-badge-success' },
  0: { label: '未连接', class: 'iot-badge-warn' },
  2: { label: '连接异常', class: 'iot-badge-error' }
};

export const STORAGE_ENGINES = [
  { value: 'memory', label: 'Memory (内存缓冲区)' },
  { value: 'mysql', label: 'MySQL 8.0 关系库' },
  { value: 'tdengine', label: 'TDengine 3.x 时序库' }
];
