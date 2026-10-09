import request from './request';

export function getLoadedDrivers(params) {
  return request({
    url: '/drivers',
    method: 'get',
    params
  });
}

export function openDeviceChannel(data) {
  return request({
    url: '/protocol/channels/connect',
    method: 'post',
    data
  });
}

export function sendControlCommand(data) {
  return request({
    url: '/protocol/command/send',
    method: 'post',
    data
  });
}

export function pingDriver(driverCode) {
  return request({
    url: `/drivers/${driverCode}/ping`,
    method: 'post'
  });
}
