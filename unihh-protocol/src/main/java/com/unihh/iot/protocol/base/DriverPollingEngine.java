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

package com.unihh.iot.protocol.base;

import com.unihh.iot.common.model.DeviceConfig;
import com.unihh.iot.common.model.PointData;
import com.unihh.iot.common.model.ThingModel;
import com.unihh.iot.protocol.model.ParsedPayload;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Lazy;
import org.springframework.stereotype.Component;

import javax.annotation.PreDestroy;
import java.util.*;
import java.util.concurrent.*;
import java.util.concurrent.atomic.AtomicInteger;
import java.util.concurrent.locks.ReentrantLock;

/**
 * 通用南向工业协议自主周期采集与调度引擎 (Autonomous Polling Engine)
 * <p>
 * 负责多设备并发定时采样、健康度探测，以及将采集结果自动打包交由 MQTT 管理器推送至数据中心。
 */
@Component
public class DriverPollingEngine {
    private static final Logger log = LoggerFactory.getLogger(DriverPollingEngine.class);

    private final ScheduledExecutorService scheduler = Executors.newScheduledThreadPool(
            Math.max(4, Runtime.getRuntime().availableProcessors() * 2),
            new ThreadFactory() {
                private int counter = 0;
                @Override
                public Thread newThread(Runnable r) {
                    Thread t = new Thread(r, "driver-polling-pool-" + (++counter));
                    t.setDaemon(true);
                    return t;
                }
            }
    );

    private final Map<String, DeviceTaskContext> activeTasks = new ConcurrentHashMap<>();

    @Autowired(required = false)
    @Lazy
    private AbstractDriverPlugin driverPlugin;

    @Autowired(required = false)
    @Lazy
    private BaseDriverMqttManager mqttManager;

    /**
     * 注册或热更新设备采集任务
     */
    public synchronized void addOrUpdateDevice(DeviceConfig config, List<ThingModel.PropertySpec> points) {
        if (config == null || config.getDeviceId() == null) return;
        String deviceId = config.getDeviceId();

        // 如果采集状态为 0 (停止采集)，则移除/停止调度
        if (config.getAcqStatus() != null && config.getAcqStatus() == 0) {
            removeDevice(deviceId);
            return;
        }

        DeviceTaskContext existing = activeTasks.get(deviceId);
        if (existing != null) {
            existing.config = config;
            existing.points = (points != null) ? new ArrayList<>(points) : Collections.emptyList();
            log.info("[DriverPollingEngine] Updated device context [{}] with {} points", deviceId, existing.points.size());
            return;
        }

        // 尝试打开物理连接
        if (driverPlugin != null) {
            try {
                driverPlugin.openConnection(config);
            } catch (Exception e) {
                log.warn("[DriverPollingEngine] Initial connect warning for device [{}]: {}", deviceId, e.getMessage());
            }
        }

        DeviceTaskContext task = new DeviceTaskContext(config, points);
        long intervalMs = (config.getBaseIntervalMs() != null && config.getBaseIntervalMs() >= 100)
                ? config.getBaseIntervalMs() : 1000L;

        ScheduledFuture<?> future = scheduler.scheduleWithFixedDelay(
                () -> pollDevice(deviceId),
                500, intervalMs, TimeUnit.MILLISECONDS
        );
        task.scheduledFuture = future;
        activeTasks.put(deviceId, task);

        log.info("[DriverPollingEngine] Started autonomous polling for device [{}] (period: {}ms, points: {})",
                deviceId, intervalMs, task.points.size());
    }

    /**
     * 停止并移除设备采集任务
     */
    public synchronized void removeDevice(String deviceId) {
        if (deviceId == null) return;
        DeviceTaskContext task = activeTasks.remove(deviceId);
        if (task != null) {
            if (task.scheduledFuture != null) {
                task.scheduledFuture.cancel(true);
            }
            if (task.getCurrentStatus() != 0) {
                task.setCurrentStatus(0);
                if (mqttManager != null) {
                    mqttManager.publishDeviceStatus(deviceId, 0, "Acquisition stopped");
                }
            }
            if (driverPlugin != null) {
                try {
                    driverPlugin.closeConnection(deviceId);
                } catch (Exception ignored) {}
            }
            log.info("[DriverPollingEngine] Stopped polling and released device [{}]", deviceId);
        }
    }

    /**
     * 执行单次设备点位采样 (受目标设备通道级排队公平互斥锁保护，防止与反控写冲突)
     */
    private void pollDevice(String deviceId) {
        DeviceTaskContext ctx = activeTasks.get(deviceId);
        if (ctx == null || ctx.config == null || driverPlugin == null) return;

        ReentrantLock lock = driverPlugin.getChannelLock(ctx.config);
        if (lock != null) {
            lock.lock();
        }
        try {
            boolean commSuccess = false;
            String failureReason = "Communication failed";

            if (ctx.points == null || ctx.points.isEmpty()) {
                // 无测点配置时，通过底层链路连通性探测 (如 Socket 握手) 判定通道健康度
                try {
                    boolean ok = driverPlugin.openConnection(ctx.config);
                    commSuccess = ok;
                    if (!ok) failureReason = "Physical link connection failed";
                } catch (Exception e) {
                    commSuccess = false;
                    failureReason = e.getMessage();
                }
            } else {
                ParsedPayload payload = driverPlugin.readPoints(ctx.config, ctx.points);
                if (payload != null && payload.getPoints() != null && !payload.getPoints().isEmpty()) {
                    if (mqttManager != null) {
                        mqttManager.publishDataPayload(ctx.config.getDeviceId(), payload);
                    }
                    boolean anyGood = false;
                    for (PointData pd : payload.getPoints()) {
                        if (pd != null && pd.getQuality() == 1) {
                            anyGood = true;
                            break;
                        }
                        if (pd != null && pd.getRawValue() != null && pd.getRawValue().toString().startsWith("COMM_FAILURE")) {
                            failureReason = pd.getRawValue().toString();
                        }
                    }
                    commSuccess = anyGood;
                }
            }

            handleDeviceHealthCheck(ctx, commSuccess, failureReason);
        } catch (Exception e) {
            log.warn("[DriverPollingEngine] Polling device [{}] error: {}", deviceId, e.getMessage());
            handleDeviceHealthCheck(ctx, false, e.getMessage());
        } finally {
            if (lock != null) {
                lock.unlock();
            }
        }
    }

    /**
     * 设备物理通信健康度诊断与防抖状态上报
     * 连续失败 >= 2 次才触发离线，避免单次丢包闪烁；一旦恢复正常则立即自愈切回在线。
     */
    private void handleDeviceHealthCheck(DeviceTaskContext ctx, boolean commSuccess, String failureReason) {
        if (ctx == null || ctx.config == null) return;
        String deviceId = ctx.config.getDeviceId();
        if (deviceId == null || deviceId.trim().isEmpty()) return;

        if (commSuccess) {
            ctx.resetFailures();
            if (ctx.getCurrentStatus() != 1) {
                ctx.setCurrentStatus(1);
                if (mqttManager != null) {
                    mqttManager.publishDeviceStatus(deviceId, 1, "Communication recovered");
                }
                log.info("[DriverPollingEngine] Device [{}] communication restored to ONLINE", deviceId);
            }
        } else {
            int failures = ctx.incrementFailures();
            if (failures >= 2 && ctx.getCurrentStatus() != 0) {
                ctx.setCurrentStatus(0);
                if (mqttManager != null) {
                    mqttManager.publishDeviceStatus(deviceId, 0, failureReason != null ? failureReason : "Communication failure/timeout");
                }
                log.warn("[DriverPollingEngine] Device [{}] communication failure confirmed (consecutive failures: {}), reporting OFFLINE: {}",
                        deviceId, failures, failureReason);
            }
        }
    }

    public int getActiveDeviceCount() {
        return activeTasks.size();
    }

    public Map<String, Object> getDeviceStatus(String deviceId) {
        DeviceTaskContext ctx = activeTasks.get(deviceId);
        if (ctx == null) return null;
        Map<String, Object> map = new HashMap<>();
        map.put("deviceId", deviceId);
        map.put("modelKey", ctx.config.getModelKey());
        map.put("host", ctx.config.getHost());
        map.put("port", ctx.config.getPort());
        map.put("pointCount", ctx.points.size());
        map.put("intervalMs", ctx.config.getBaseIntervalMs());
        return map;
    }

    public List<Map<String, Object>> getAllDeviceStatuses() {
        List<Map<String, Object>> list = new ArrayList<>();
        for (String devId : activeTasks.keySet()) {
            Map<String, Object> st = getDeviceStatus(devId);
            if (st != null) list.add(st);
        }
        return list;
    }

    public DeviceConfig getDeviceConfig(String deviceId) {
        DeviceTaskContext ctx = activeTasks.get(deviceId);
        return ctx != null ? ctx.config : null;
    }

    public List<ThingModel.PropertySpec> getDevicePoints(String deviceId) {
        DeviceTaskContext ctx = activeTasks.get(deviceId);
        return ctx != null && ctx.points != null ? ctx.points : Collections.emptyList();
    }

    public ThingModel.PropertySpec findPoint(String deviceId, String pointCode) {
        if (deviceId == null || pointCode == null) return null;
        DeviceTaskContext ctx = activeTasks.get(deviceId);
        if (ctx == null || ctx.points == null) return null;
        for (ThingModel.PropertySpec p : ctx.points) {
            if (pointCode.equalsIgnoreCase(p.getPointCode())) {
                return p;
            }
        }
        // 模糊匹配 (例如传入 "speed", 物模型点位编码为 "targetSpeed")
        for (ThingModel.PropertySpec p : ctx.points) {
            if (p.getPointCode() != null && p.getPointCode().toLowerCase().contains(pointCode.toLowerCase())) {
                return p;
            }
        }
        return null;
    }

    @PreDestroy
    public void shutdown() {
        scheduler.shutdownNow();
        if (mqttManager != null) {
            for (Map.Entry<String, DeviceTaskContext> entry : activeTasks.entrySet()) {
                try {
                    mqttManager.publishDeviceStatus(entry.getKey(), 0, "Driver shutdown");
                } catch (Exception ignored) {}
            }
        }
        activeTasks.clear();
        log.info("[DriverPollingEngine] Shutdown completed.");
    }

    private static class DeviceTaskContext {
        volatile DeviceConfig config;
        volatile List<ThingModel.PropertySpec> points;
        ScheduledFuture<?> scheduledFuture;
        final AtomicInteger consecutiveFailures = new AtomicInteger(0);
        volatile int currentStatus = -1;

        DeviceTaskContext(DeviceConfig config, List<ThingModel.PropertySpec> points) {
            this.config = config;
            this.points = points != null ? new ArrayList<>(points) : new ArrayList<>();
            this.currentStatus = -1;
        }

        public int getConsecutiveFailures() { return consecutiveFailures.get(); }
        public int incrementFailures() { return consecutiveFailures.incrementAndGet(); }
        public void resetFailures() { consecutiveFailures.set(0); }
        public int getCurrentStatus() { return currentStatus; }
        public void setCurrentStatus(int currentStatus) { this.currentStatus = currentStatus; }
    }
}
