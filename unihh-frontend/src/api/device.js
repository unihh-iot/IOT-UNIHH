import request from './request';

export function getDeviceList(params) {
  return request({
    url: '/devices',
    method: 'get',
    params
  });
}

export function getDeviceDetail(deviceId) {
  return request({
    url: `/devices/${deviceId}`,
    method: 'get'
  });
}

export function addDevice(data) {
  return request({
    url: '/devices',
    method: 'post',
    data
  });
}

export function updateDevice(deviceId, data) {
  return request({
    url: `/devices/${deviceId}`,
    method: 'put',
    data
  });
}

export function deleteDevice(deviceId) {
  return request({
    url: `/devices/${deviceId}`,
    method: 'delete'
  });
}

export function updateAcqStatus(deviceId, acqStatus) {
  return request({
    url: `/devices/${deviceId}/acq-status`,
    method: 'put',
    params: { acqStatus }
  });
}

export function getPointAddressMappings(deviceId) {
  return request({
    url: `/devices/${deviceId}/points`,
    method: 'get'
  });
}

export function suggestPointAddressMappings(deviceId) {
  return request({
    url: `/devices/${deviceId}/points/suggested`,
    method: 'get'
  });
}

export function savePointAddressMappings(deviceId, data) {
  return request({
    url: `/devices/${deviceId}/points`,
    method: 'put',
    data
  });
}

export function controlDevicePoint(deviceId, pointCode, value, dataType) {
  return request({
    url: `/devices/${deviceId}/control`,
    method: 'post',
    data: { pointCode, value, dataType }
  });
}

export function invokeDeviceService(deviceId, serviceCode, params) {
  return request({
    url: `/devices/${deviceId}/services/${serviceCode}`,
    method: 'post',
    data: params || {}
  });
}
