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

package com.unihh.iot.gateway.controller;

import com.unihh.iot.common.grpc.GrpcClient;
import com.unihh.iot.common.grpc.GrpcModels;
import com.unihh.iot.common.model.R;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.*;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.client.RestTemplate;

import java.net.URI;
import javax.servlet.http.HttpServletRequest;

/**
 * 统一 API 网关路由请求转发控制器 (优先使用内部 gRPC 通信, HTTP Rest 备用)
 */
@CrossOrigin(origins = "*", allowedHeaders = "*")
@RestController
@RequestMapping("/api/v1")
public class GatewayProxyController {
    private static final Logger log = LoggerFactory.getLogger(GatewayProxyController.class);
    private final RestTemplate restTemplate = new RestTemplate();

    @Value("${unihh.services.admin-service.host:127.0.0.1}")
    private String adminHost;
    @Value("${unihh.services.admin-service.port:28001}")
    private int adminPort;
    @Value("${unihh.services.admin-service.grpc-port:29001}")
    private int adminGrpcPort;

    @Value("${unihh.services.data-service.host:127.0.0.1}")
    private String dataHost;
    @Value("${unihh.services.data-service.port:28002}")
    private int dataPort;
    @Value("${unihh.services.data-service.grpc-port:29002}")
    private int dataGrpcPort;

    @Value("${unihh.services.protocol-service.host:127.0.0.1}")
    private String protocolHost;
    @Value("${unihh.services.protocol-service.port:28003}")
    private int protocolPort;
    @Value("${unihh.services.protocol-service.grpc-port:29003}")
    private int protocolGrpcPort;

    /** SSE 实时数据流长连接透传代理 */
    @CrossOrigin(origins = "*")
    @GetMapping(value = "/data/sse/{deviceId}", produces = MediaType.TEXT_EVENT_STREAM_VALUE)
    public org.springframework.web.servlet.mvc.method.annotation.SseEmitter proxySseData(@PathVariable("deviceId") String deviceId) {
        org.springframework.web.servlet.mvc.method.annotation.SseEmitter emitter =
                new org.springframework.web.servlet.mvc.method.annotation.SseEmitter(30 * 60 * 1000L);
        String targetUrl = String.format("http://%s:%d/api/v1/data/sse/%s", dataHost, dataPort, deviceId);

        Thread streamThread = new Thread(() -> {
            java.net.HttpURLConnection conn = null;
            try {
                java.net.URL url = new java.net.URL(targetUrl);
                conn = (java.net.HttpURLConnection) url.openConnection();
                conn.setRequestProperty("Accept", "text/event-stream");
                conn.setReadTimeout(0);
                conn.setConnectTimeout(5000);

                int respCode = conn.getResponseCode();
                if (respCode == 200) {
                    try (java.io.BufferedReader reader = new java.io.BufferedReader(
                            new java.io.InputStreamReader(conn.getInputStream(), java.nio.charset.StandardCharsets.UTF_8))) {
                        String line;
                        String eventName = "message";
                        StringBuilder dataBuf = new StringBuilder();
                        while (!Thread.currentThread().isInterrupted() && (line = reader.readLine()) != null) {
                            if (line.startsWith("event:")) {
                                eventName = line.substring(6).trim();
                            } else if (line.startsWith("data:")) {
                                if (dataBuf.length() > 0) dataBuf.append("\n");
                                dataBuf.append(line.substring(5).trim());
                            } else if (line.isEmpty() && dataBuf.length() > 0) {
                                emitter.send(org.springframework.web.servlet.mvc.method.annotation.SseEmitter.event()
                                        .name(eventName).data(dataBuf.toString()));
                                dataBuf.setLength(0);
                                eventName = "message";
                            }
                        }
                    }
                } else {
                    log.warn("下游 data-service 未返回 200 流 (HTTP {}), 需确认 unihh-data 已重启", respCode);
                    emitter.send(org.springframework.web.servlet.mvc.method.annotation.SseEmitter.event()
                            .name("status").data("{\"status\":\"waiting\",\"code\":" + respCode + "}"));
                }
            } catch (Exception e) {
                log.debug("SSE 转发连接结束: deviceId={}, reason={}", deviceId, e.getMessage());
            } finally {
                if (conn != null) {
                    conn.disconnect();
                }
                emitter.complete();
            }
        }, "sse-proxy-" + deviceId);

        emitter.onCompletion(streamThread::interrupt);
        emitter.onTimeout(streamThread::interrupt);
        emitter.onError(t -> streamThread.interrupt());

        streamThread.start();
        return emitter;
    }

    /** 统一转发 REST 请求至下游对应微服务 (优先 gRPC 内部调用) */
    @RequestMapping(value = "/{module}/**")
    public ResponseEntity<?> proxyRequest(@PathVariable("module") String module,
                                          HttpMethod method,
                                          HttpServletRequest request,
                                          @RequestBody(required = false) byte[] body) {
        String uri = request.getRequestURI();
        String queryString = request.getQueryString();
        String targetHost;
        int targetPort;
        int targetGrpcPort;
        String serviceName;

        if ("devices".equals(module) || "thingmodels".equals(module) || "drivers".equals(module) || "rules".equals(module) || "alarms".equals(module) || "logs".equals(module) || "admin".equals(module)
                || "auth".equals(module) || "system".equals(module) || "users".equals(module) || "roles".equals(module) || "menus".equals(module) || "dicts".equals(module) || "dashboard".equals(module)) {
            targetHost = adminHost;
            targetPort = adminPort;
            targetGrpcPort = adminGrpcPort;
            serviceName = "AdminGrpcService";
        } else if ("data".equals(module)) {
            targetHost = dataHost;
            targetPort = dataPort;
            targetGrpcPort = dataGrpcPort;
            serviceName = "DataGrpcService";
        } else if ("protocol".equals(module)) {
            targetHost = protocolHost;
            targetPort = protocolPort;
            targetGrpcPort = protocolGrpcPort;
            serviceName = "ProtocolGrpcService";
        } else {
            return ResponseEntity.status(HttpStatus.NOT_FOUND).body(R.fail(404, "网关未识别的路由模块: " + module));
        }

        log.info("[API Gateway 转发] {} {} -> 微服务 {} (gRPC端口: {}, HTTP端口: {})", method, uri, targetHost, targetGrpcPort, targetPort);

        // 尝试优先走内部 gRPC 调取
        if (HttpMethod.GET.equals(method) && queryString == null) {
            String rpcMethod = null;
            if (uri.endsWith("/rules")) rpcMethod = "getRules";
            else if (uri.endsWith("/alarms")) rpcMethod = "getAlarms";
            else if (uri.endsWith("/logs")) rpcMethod = "getLogs";

            if (rpcMethod != null) {
                GrpcModels.RpcResponse rpcResp = GrpcClient.call(targetHost, targetGrpcPort, serviceName, rpcMethod, "");
                if (rpcResp != null && rpcResp.getCode() == 200) {
                    log.info("[API Gateway gRPC 通信成功] method={} 返回数据长度: {} 字节", rpcMethod, rpcResp.getDataJson().length());
                    return ResponseEntity.ok(JSONResponseRaw(rpcResp.getDataJson()));
                }
            }
        }

        // HTTP RestTemplate 请求转发 (带自动降级重试)
        String targetUrl = String.format("http://%s:%d%s%s", targetHost, targetPort, uri,
                (queryString != null ? "?" + queryString : ""));

        HttpHeaders headers = new HttpHeaders();
        headers.setContentType(MediaType.APPLICATION_JSON);
        String authHeader = request.getHeader("Authorization");
        if (authHeader != null && !authHeader.trim().isEmpty()) {
            headers.set("Authorization", authHeader.trim());
        }
        String operatorHeader = request.getHeader("X-Operator");
        if (operatorHeader != null && !operatorHeader.trim().isEmpty()) {
            headers.set("X-Operator", operatorHeader.trim());
        }
        String userHeader = request.getHeader("X-User");
        if (userHeader != null && !userHeader.trim().isEmpty()) {
            headers.set("X-User", userHeader.trim());
        }
        String clientIp = request.getHeader("X-Forwarded-For");
        if (clientIp == null || clientIp.isEmpty() || "unknown".equalsIgnoreCase(clientIp)) {
            clientIp = request.getRemoteAddr();
        }
        headers.set("X-Forwarded-For", clientIp);
        HttpEntity<byte[]> entity = new HttpEntity<>(body, headers);

        try {
            return restTemplate.exchange(URI.create(targetUrl), method, entity, byte[].class);
        } catch (org.springframework.web.client.HttpStatusCodeException e) {
            // 下游微服务返回业务 HTTP 状态码响应 (如 400 参数异常, 401 未授权, 403 权限不足, 404 资源不存在, 500 等)
            // 网关将下游原始 HTTP 状态码与响应体无损透传给前端客户端，禁止包装成 502
            log.warn("[API Gateway 业务拦截透传] targetUrl={}, status={}, response={}",
                    targetUrl, e.getRawStatusCode(), e.getResponseBodyAsString());
            HttpHeaders respHeaders = new HttpHeaders();
            if (e.getResponseHeaders() != null) {
                e.getResponseHeaders().forEach((name, values) -> {
                    if (!HttpHeaders.TRANSFER_ENCODING.equalsIgnoreCase(name)
                            && !HttpHeaders.CONTENT_LENGTH.equalsIgnoreCase(name)) {
                        respHeaders.put(name, values);
                    }
                });
            }
            if (respHeaders.getContentType() == null) {
                respHeaders.setContentType(MediaType.APPLICATION_JSON);
            }
            return ResponseEntity.status(e.getRawStatusCode())
                    .headers(respHeaders)
                    .body(e.getResponseBodyAsByteArray());
        } catch (Exception e) {
            // 如果目标 HTTP 服务不可用，尝试走 gRPC 降级回退
            if (targetPort != adminPort && (uri.contains("drivers") || uri.contains("protocol"))) {
                String fallbackUrl = String.format("http://%s:%d%s%s", adminHost, adminPort, uri,
                        (queryString != null ? "?" + queryString : ""));
                log.warn("[API Gateway 自动降级] 目标服务 {}:{} 不可用，尝试降级转发至 admin-service: {}", targetHost, targetPort, fallbackUrl);
                try {
                    return restTemplate.exchange(URI.create(fallbackUrl), method, entity, byte[].class);
                } catch (Exception ex) {
                    log.error("[API Gateway 降级转发失败] fallbackUrl={}", fallbackUrl, ex);
                }
            }

            log.error("[API Gateway 转发异常] targetUrl={}", targetUrl, e);
            return ResponseEntity.status(HttpStatus.BAD_GATEWAY)
                    .body(R.fail(502, "网关转发下游目标服务不可用 (端口 " + targetPort + " / gRPC " + targetGrpcPort + " 连接被拒绝，请确认下游服务 `java -jar` 已启动): " + e.getMessage()));
        }
    }

    private byte[] JSONResponseRaw(String jsonStr) {
        String wrapped = "{\"code\":200,\"msg\":\"success\",\"data\":" + (jsonStr != null && !jsonStr.isEmpty() ? jsonStr : "[]") + "}";
        return wrapped.getBytes(java.nio.charset.StandardCharsets.UTF_8);
    }
}
