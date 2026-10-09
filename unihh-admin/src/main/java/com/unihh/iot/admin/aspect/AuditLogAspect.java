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

package com.unihh.iot.admin.aspect;

import com.alibaba.fastjson2.JSON;
import com.alibaba.fastjson2.JSONObject;
import com.unihh.iot.admin.annotation.AuditLog;
import com.unihh.iot.admin.service.LogService;
import com.unihh.iot.common.model.IotLog;
import com.unihh.iot.common.model.R;
import org.aspectj.lang.ProceedingJoinPoint;
import org.aspectj.lang.annotation.Around;
import org.aspectj.lang.annotation.Aspect;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;
import org.springframework.web.context.request.RequestContextHolder;
import org.springframework.web.context.request.ServletRequestAttributes;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import java.io.InputStream;
import java.io.OutputStream;
import java.text.SimpleDateFormat;
import java.util.*;

/**
 * 系统敏感操作审计日志切面
 */
@Aspect
@Component
public class AuditLogAspect {

    private static final Logger log = LoggerFactory.getLogger(AuditLogAspect.class);

    @Autowired(required = false)
    private LogService logService;

    @Around("@annotation(auditLog)")
    public Object around(ProceedingJoinPoint point, AuditLog auditLog) throws Throwable {
        long startMs = System.currentTimeMillis();
        boolean isSuccess = true;
        String errorMsg = null;
        Object result = null;

        // 提取请求上下文信息 (在调用线程中获取)
        String operator = "admin";
        String clientIp = "127.0.0.1";
        String deviceId = null;
        String modelKey = null;

        try {
            ServletRequestAttributes attributes = (ServletRequestAttributes) RequestContextHolder.getRequestAttributes();
            if (attributes != null) {
                HttpServletRequest request = attributes.getRequest();
                clientIp = getClientIp(request);
                operator = getOperator(request);
            }
        } catch (Exception ignored) {}

        // 提取方法入参摘要与关键标识
        String paramsSnapshot = "";
        try {
            Object[] args = point.getArgs();
            org.aspectj.lang.reflect.MethodSignature signature = (point.getSignature() instanceof org.aspectj.lang.reflect.MethodSignature)
                    ? (org.aspectj.lang.reflect.MethodSignature) point.getSignature() : null;
            String[] paramNames = signature != null ? signature.getParameterNames() : null;

            if (args != null && args.length > 0) {
                Map<String, Object> paramMap = new LinkedHashMap<>();
                for (int i = 0; i < args.length; i++) {
                    Object arg = args[i];
                    if (arg instanceof HttpServletRequest || arg instanceof HttpServletResponse
                            || arg instanceof InputStream || arg instanceof OutputStream) {
                        continue;
                    }
                    String name = (paramNames != null && i < paramNames.length) ? paramNames[i] : ("arg" + i);
                    paramMap.put(name, arg);

                    // 动态探测关联的 deviceId 或 modelKey
                    if (arg != null) {
                        String s = arg.toString();
                        if (deviceId == null && s.startsWith("DEV_")) {
                            deviceId = s;
                        }
                        if (modelKey == null && (s.startsWith("PK_") || s.startsWith("pk_"))) {
                            modelKey = s;
                        }
                    }
                }
                if (auditLog.saveParams() && !paramMap.isEmpty()) {
                    paramsSnapshot = JSON.toJSONString(paramMap);
                    if (paramsSnapshot.length() > 800) {
                        paramsSnapshot = paramsSnapshot.substring(0, 800) + "...(截断)";
                    }
                }
            }
        } catch (Exception ignored) {}

        try {
            result = point.proceed();
            if (result instanceof R) {
                R<?> r = (R<?>) result;
                if (r.getCode() != 200) {
                    isSuccess = false;
                    errorMsg = r.getMsg();
                }
            }
            return result;
        } catch (Throwable t) {
            isSuccess = false;
            errorMsg = t.getMessage();
            throw t;
        } finally {
            long costMs = System.currentTimeMillis() - startMs;
            saveAuditLog(auditLog, operator, clientIp, deviceId, modelKey, paramsSnapshot, isSuccess, errorMsg, costMs);
        }
    }

    private void saveAuditLog(AuditLog auditLog, String operator, String clientIp, String deviceId, String modelKey,
                              String paramsSnapshot, boolean isSuccess, String errorMsg, long costMs) {
        if (logService == null) return;
        try {
            String dateStr = new SimpleDateFormat("yyyyMMdd_HHmmss").format(new Date());
            String logId = "LOG_" + dateStr + "_" + UUID.randomUUID().toString().substring(0, 4).toUpperCase();

            JSONObject detailObj = new JSONObject();
            detailObj.put("module", auditLog.module());
            detailObj.put("action", auditLog.action());
            detailObj.put("params", paramsSnapshot != null ? paramsSnapshot : "");
            detailObj.put("ip", clientIp);
            detailObj.put("costMs", costMs);
            detailObj.put("status", isSuccess ? "SUCCESS" : "FAILED");
            if (errorMsg != null) {
                detailObj.put("error", errorMsg);
            }

            IotLog logEntry = new IotLog();
            logEntry.setLogId(logId);
            logEntry.setLogType("OPERATION");
            logEntry.setLevel(isSuccess ? "INFO" : "ERROR");
            logEntry.setOperator(operator);
            logEntry.setDeviceId(deviceId);
            logEntry.setModelKey(modelKey);
            logEntry.setContent(detailObj.toJSONString());
            logEntry.setCreateTime(new Date());

            logService.recordLog(logEntry);
        } catch (Exception e) {
            log.warn("Record audit log failed: {}", e.getMessage());
        }
    }

    private String getOperator(HttpServletRequest request) {
        String contextUser = com.unihh.iot.admin.security.SecurityContextHolder.getUsername();
        if (contextUser != null && !"SYSTEM".equals(contextUser) && !contextUser.trim().isEmpty()) {
            return contextUser;
        }
        String op = request.getHeader("X-Operator");
        if (op == null || op.trim().isEmpty()) {
            op = request.getHeader("X-User");
        }
        if (op == null || op.trim().isEmpty()) {
            String auth = request.getHeader("Authorization");
            if (auth != null && !auth.trim().isEmpty()) {
                op = "admin";
            }
        }
        return (op != null && !op.trim().isEmpty()) ? op.trim() : "admin";
    }

    private String getClientIp(HttpServletRequest request) {
        String ip = request.getHeader("X-Forwarded-For");
        if (ip == null || ip.isEmpty() || "unknown".equalsIgnoreCase(ip)) {
            ip = request.getHeader("X-Real-IP");
        }
        if (ip == null || ip.isEmpty() || "unknown".equalsIgnoreCase(ip)) {
            ip = request.getHeader("Proxy-Client-IP");
        }
        if (ip == null || ip.isEmpty() || "unknown".equalsIgnoreCase(ip)) {
            ip = request.getHeader("WL-Proxy-Client-IP");
        }
        if (ip == null || ip.isEmpty() || "unknown".equalsIgnoreCase(ip)) {
            ip = request.getRemoteAddr();
        }
        if ("0:0:0:0:0:0:0:1".equals(ip) || "localhost".equalsIgnoreCase(ip)) {
            ip = "127.0.0.1";
        }
        if (ip != null && ip.contains(",")) {
            ip = ip.split(",")[0].trim();
        }
        return ip != null ? ip : "127.0.0.1";
    }
}
