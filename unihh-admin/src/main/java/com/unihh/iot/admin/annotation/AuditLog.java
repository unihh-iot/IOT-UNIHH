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

package com.unihh.iot.admin.annotation;

import java.lang.annotation.*;

/**
 * 系统操作审计日志自定义注解
 */
@Target(ElementType.METHOD)
@Retention(RetentionPolicy.RUNTIME)
@Documented
public @interface AuditLog {

    /**
     * 业务操作模块 (如: 设备管理, 物模型管理, 规则管理, 驱动管理, 告警管理)
     */
    String module() default "系统管理";

    /**
     * 操作行为说明 (如: 新增物理设备, 修改物模型定义, 删除告警规则, 切换数采状态)
     */
    String action() default "";

    /**
     * 是否记录请求入参 JSON
     */
    boolean saveParams() default true;
}
