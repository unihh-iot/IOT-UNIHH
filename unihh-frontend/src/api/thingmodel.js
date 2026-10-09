import request from './request';

export function getAllThingModels() {
  return request({
    url: '/thingmodels',
    method: 'get'
  });
}

export function getThingModel(modelKey) {
  return request({
    url: `/thingmodels/${modelKey}`,
    method: 'get'
  });
}

export function addThingModel(data) {
  return request({
    url: '/thingmodels',
    method: 'post',
    data
  });
}

export function updateThingModel(modelKey, data) {
  return request({
    url: `/thingmodels/${modelKey}`,
    method: 'put',
    data
  });
}

export function deleteThingModel(modelKey) {
  return request({
    url: `/thingmodels/${modelKey}`,
    method: 'delete'
  });
}

export function addProperty(modelKey, data) {
  return request({
    url: `/thingmodels/${modelKey}/properties`,
    method: 'post',
    data
  });
}

export function updateProperty(modelKey, pointCode, data) {
  return request({
    url: `/thingmodels/${modelKey}/properties/${pointCode}`,
    method: 'put',
    data
  });
}

export function deleteProperty(modelKey, pointCode) {
  return request({
    url: `/thingmodels/${modelKey}/properties/${pointCode}`,
    method: 'delete'
  });
}

export function addService(modelKey, data) {
  return request({
    url: `/thingmodels/${modelKey}/services`,
    method: 'post',
    data
  });
}

export function updateService(modelKey, serviceCode, data) {
  return request({
    url: `/thingmodels/${modelKey}/services/${serviceCode}`,
    method: 'put',
    data
  });
}

export function deleteService(modelKey, serviceCode) {
  return request({
    url: `/thingmodels/${modelKey}/services/${serviceCode}`,
    method: 'delete'
  });
}

export function addEvent(modelKey, data) {
  return request({
    url: `/thingmodels/${modelKey}/events`,
    method: 'post',
    data
  });
}

export function updateEvent(modelKey, eventCode, data) {
  return request({
    url: `/thingmodels/${modelKey}/events/${eventCode}`,
    method: 'put',
    data
  });
}

export function deleteEvent(modelKey, eventCode) {
  return request({
    url: `/thingmodels/${modelKey}/events/${eventCode}`,
    method: 'delete'
  });
}
