import request from './request';

export function getLogList(params) {
  return request({
    url: '/logs',
    method: 'get',
    params
  });
}

export function recordLog(data) {
  return request({
    url: '/logs',
    method: 'post',
    data
  });
}

export function clearLogs() {
  return request({
    url: '/logs',
    method: 'delete'
  });
}
