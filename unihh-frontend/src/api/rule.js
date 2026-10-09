import request from './request';

export function getRuleList(params) {
  return request({
    url: '/rules',
    method: 'get',
    params
  });
}

export function getRuleDetail(ruleId) {
  return request({
    url: `/rules/${ruleId}`,
    method: 'get'
  });
}

export function addRule(data) {
  return request({
    url: '/rules',
    method: 'post',
    data
  });
}

export function updateRule(ruleId, data) {
  return request({
    url: `/rules/${ruleId}`,
    method: 'put',
    data
  });
}

export function toggleRule(ruleId, enabled) {
  return request({
    url: `/rules/${ruleId}/toggle`,
    method: 'put',
    data: { enabled }
  });
}

export function deleteRule(ruleId) {
  return request({
    url: `/rules/${ruleId}`,
    method: 'delete'
  });
}
