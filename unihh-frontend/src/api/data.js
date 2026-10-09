import request from './request';

export function ingestPointData(data) {
  return request({
    url: '/data/ingest',
    method: 'post',
    data
  });
}

export function ingestBatchData(data) {
  return request({
    url: '/data/ingest/batch',
    method: 'post',
    data
  });
}

export function queryHistoryData(data) {
  return request({
    url: '/data/history',
    method: 'post',
    data
  });
}

export function getLatestPointData(deviceId) {
  return request({
    url: `/data/latest/${deviceId}`,
    method: 'get'
  });
}

export function getStorageEngineInfo() {
  return request({
    url: '/data/engine',
    method: 'get'
  });
}

export function switchStorageEngine(engineType) {
  return request({
    url: '/data/engine',
    method: 'put',
    params: { engineType }
  });
}

export function getAlarmEvents() {
  return request({
    url: '/data/alarms',
    method: 'get'
  });
}

export function confirmAlarmEvent(alarmId) {
  return request({
    url: `/data/alarms/${alarmId}/confirm`,
    method: 'post'
  });
}

