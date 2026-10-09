/*
 * Copyright (c) 2026 unihh.com All Rights Reserved.
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *     http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 *
 * Website: https://unihh.com
 * Author: deversun
 */

package com.unihh.iot.admin.model;

import java.util.Date;

/**
 * 系统角色实体对象
 */
public class SysRole {
    private Long id;
    private String roleKey;   // 角色权限标识 (如 admin, operator, viewer)
    private String roleName;  // 角色名称 (如 超级管理员, 操作员)
    private Integer sortOrder;
    private Integer status;   // 1: 启用, 0: 停用
    private String remark;
    private Date createTime;
    private Date updateTime;
    private Integer permsCount;
    private java.util.List<Long> permissionIds;
    private java.util.List<Long> menuIds;

    public SysRole() {}

    public Long getId() { return id; }
    public void setId(Long id) { this.id = id; }

    public String getRoleKey() { return roleKey; }
    public void setRoleKey(String roleKey) { this.roleKey = roleKey; }

    public String getRoleName() { return roleName; }
    public void setRoleName(String roleName) { this.roleName = roleName; }

    public Integer getSortOrder() { return sortOrder; }
    public void setSortOrder(Integer sortOrder) { this.sortOrder = sortOrder; }

    public Integer getStatus() { return status; }
    public void setStatus(Integer status) { this.status = status; }

    public String getRemark() { return remark; }
    public void setRemark(String remark) { this.remark = remark; }

    public Date getCreateTime() { return createTime; }
    public void setCreateTime(Date createTime) { this.createTime = createTime; }

    public Date getUpdateTime() { return updateTime; }
    public void setUpdateTime(Date updateTime) { this.updateTime = updateTime; }

    public Integer getPermsCount() { return permsCount; }
    public void setPermsCount(Integer permsCount) { this.permsCount = permsCount; }

    public java.util.List<Long> getPermissionIds() { return permissionIds; }
    public void setPermissionIds(java.util.List<Long> permissionIds) { this.permissionIds = permissionIds; }

    public java.util.List<Long> getMenuIds() { return menuIds; }
    public void setMenuIds(java.util.List<Long> menuIds) { this.menuIds = menuIds; }
}
