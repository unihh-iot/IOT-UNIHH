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

import java.util.ArrayList;
import java.util.Date;
import java.util.List;

/**
 * 系统菜单实体对象 (支持动态路由与侧边栏渲染)
 */
public class SysMenu {
    private Long id;
    private Long parentId;
    private String menuName;
    private String path;         // 路由路径 (如 /dashboard, /devices)
    private String component;    // 组件路径 (如 views/dashboard/OverviewDashboard.vue)
    private String icon;         // 图标标识
    private Integer sortOrder;
    private String menuType;     // M: 目录, C: 菜单, F: 按钮权限
    private String perms;        // 权限标识 (如 system:user:view)
    private Integer visible;     // 1: 显示, 0: 隐藏
    private Integer status;      // 1: 启用, 0: 停用
    private String remark;
    private Date createTime;
    private Date updateTime;

    /** 子菜单列表 (用于树状结构) */
    private List<SysMenu> children = new ArrayList<>();

    public SysMenu() {}

    public Long getId() { return id; }
    public void setId(Long id) { this.id = id; }

    public Long getParentId() { return parentId; }
    public void setParentId(Long parentId) { this.parentId = parentId; }

    public String getMenuName() { return menuName; }
    public void setMenuName(String menuName) { this.menuName = menuName; }

    public String getPath() { return path; }
    public void setPath(String path) { this.path = path; }

    public String getComponent() { return component; }
    public void setComponent(String component) { this.component = component; }

    public String getIcon() { return icon; }
    public void setIcon(String icon) { this.icon = icon; }

    public Integer getSortOrder() { return sortOrder; }
    public void setSortOrder(Integer sortOrder) { this.sortOrder = sortOrder; }

    public String getMenuType() { return menuType; }
    public void setMenuType(String menuType) { this.menuType = menuType; }

    public String getPerms() { return perms; }
    public void setPerms(String perms) { this.perms = perms; }

    public Integer getVisible() { return visible; }
    public void setVisible(Integer visible) { this.visible = visible; }

    public Integer getStatus() { return status; }
    public void setStatus(Integer status) { this.status = status; }

    public String getRemark() { return remark; }
    public void setRemark(String remark) { this.remark = remark; }

    public Date getCreateTime() { return createTime; }
    public void setCreateTime(Date createTime) { this.createTime = createTime; }

    public Date getUpdateTime() { return updateTime; }
    public void setUpdateTime(Date updateTime) { this.updateTime = updateTime; }

    public List<SysMenu> getChildren() { return children; }
    public void setChildren(List<SysMenu> children) { this.children = children; }
}
