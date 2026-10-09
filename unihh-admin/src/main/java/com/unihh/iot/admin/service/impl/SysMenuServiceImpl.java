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

package com.unihh.iot.admin.service.impl;

import com.unihh.iot.admin.dao.SysMenuDao;
import com.unihh.iot.admin.model.SysMenu;
import com.unihh.iot.admin.security.LoginUser;
import com.unihh.iot.admin.security.SecurityContextHolder;
import com.unihh.iot.admin.service.SysMenuService;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.*;

@Service
public class SysMenuServiceImpl implements SysMenuService {

    private static final Logger log = LoggerFactory.getLogger(SysMenuServiceImpl.class);

    @Autowired
    private SysMenuDao sysMenuDao;

    @Override
    public List<SysMenu> getAllMenus() {
        return sysMenuDao.findAll();
    }

    @Override
    public List<SysMenu> getMenuList(String keyword, Integer status) {
        return sysMenuDao.findAll(keyword, status);
    }

    @Override
    public SysMenu getMenuById(Long id) {
        return sysMenuDao.findById(id);
    }

    @Override
    public List<SysMenu> getMenuTree() {
        List<SysMenu> flatMenus = sysMenuDao.findAll();
        return buildTree(flatMenus);
    }

    @Override
    public List<SysMenu> getRoutesByUserId(Long userId) {
        List<SysMenu> flatMenus = (userId == null) ? sysMenuDao.findAll() : sysMenuDao.findMenusByUserId(userId);
        return buildTree(flatMenus);
    }

    @Override
    public List<SysMenu> getCurrentUserRoutes() {
        LoginUser loginUser = SecurityContextHolder.get();
        if (loginUser == null || loginUser.isAdmin()) {
            return getRoutesByUserId(null);
        }
        return getRoutesByUserId(loginUser.getUserId());
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public boolean createMenu(SysMenu menu) {
        if (menu == null || menu.getMenuName() == null || menu.getMenuName().trim().isEmpty()) {
            throw new IllegalArgumentException("菜单名称不能为空");
        }
        if (menu.getPath() == null || menu.getPath().trim().isEmpty()) {
            throw new IllegalArgumentException("路由路径不能为空");
        }

        if (menu.getParentId() == null) {
            menu.setParentId(0L);
        }
        if (menu.getSortOrder() == null) {
            menu.setSortOrder(0);
        }
        if (menu.getMenuType() == null || menu.getMenuType().trim().isEmpty()) {
            menu.setMenuType("C");
        }
        if (menu.getVisible() == null) {
            menu.setVisible(1);
        }
        if (menu.getStatus() == null) {
            menu.setStatus(1);
        }

        Long menuId = sysMenuDao.insert(menu);
        log.info("[菜单路由] 成功新增菜单: menuName={}, path={}, id={}", menu.getMenuName(), menu.getPath(), menuId);
        return menuId != null;
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public boolean updateMenu(SysMenu menu) {
        if (menu == null || menu.getId() == null) {
            throw new IllegalArgumentException("菜单 ID 不能为空");
        }
        SysMenu exist = sysMenuDao.findById(menu.getId());
        if (exist == null) {
            throw new IllegalArgumentException("菜单不存在");
        }
        if (menu.getId().equals(menu.getParentId())) {
            throw new IllegalArgumentException("上级菜单不能选择自身");
        }

        int rows = sysMenuDao.update(menu);
        log.info("[菜单路由] 成功更新菜单信息: id={}, menuName={}", menu.getId(), menu.getMenuName());
        return rows > 0;
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public boolean deleteMenu(Long id) {
        if (id == null) return false;
        int childCount = sysMenuDao.countChildren(id);
        if (childCount > 0) {
            throw new IllegalArgumentException("该菜单下存在 " + childCount + " 个子菜单或按钮，请先删除或转移子项");
        }

        int rows = sysMenuDao.deleteById(id);
        log.info("[菜单路由] 删除菜单: id={}", id);
        return rows > 0;
    }

    private List<SysMenu> buildTree(List<SysMenu> menus) {
        if (menus == null || menus.isEmpty()) {
            return Collections.emptyList();
        }

        List<SysMenu> rootList = new ArrayList<>();
        Map<Long, SysMenu> map = new LinkedHashMap<>();
        for (SysMenu menu : menus) {
            menu.setChildren(new ArrayList<>());
            map.put(menu.getId(), menu);
        }

        for (SysMenu menu : menus) {
            Long parentId = menu.getParentId();
            if (parentId == null || parentId == 0L || !map.containsKey(parentId)) {
                rootList.add(menu);
            } else {
                SysMenu parent = map.get(parentId);
                parent.getChildren().add(menu);
            }
        }
        return rootList;
    }
}
