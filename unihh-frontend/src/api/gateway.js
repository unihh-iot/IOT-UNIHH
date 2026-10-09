import request from './request';

export function getGatewayStatus() {
  return request({
    url: '/gateway/status',
    method: 'get'
  });
}
