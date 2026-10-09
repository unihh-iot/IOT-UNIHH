import request from './request';

export function getAlarmList(params) {
  return request({
    url: '/alarms',
    method: 'get',
    params
  });
}

export function confirmAlarm(alarmId, data) {
  return request({
    url: `/alarms/${alarmId}/confirm`,
    method: 'post',
    data
  });
}

export function resolveAlarm(alarmId, data) {
  return request({
    url: `/alarms/${alarmId}/resolve`,
    method: 'post',
    data
  });
}

export function deleteAlarm(alarmId) {
  return request({
    url: `/alarms/${alarmId}`,
    method: 'delete'
  });
}
