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

package com.unihh.iot.common.grpc;

import io.grpc.*;
import io.grpc.stub.ClientCalls;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.TimeUnit;

/**
 * 客户端微服务间 gRPC 通信 RPC 客户端组件 (HTTP/2 + Protobuf/JSON 序列化)
 */
public class GrpcClient {
    private static final Logger log = LoggerFactory.getLogger(GrpcClient.class);

    private static final Map<String, ManagedChannel> channelMap = new ConcurrentHashMap<>();

    private static ManagedChannel getChannel(String host, int grpcPort) {
        String key = host + ":" + grpcPort;
        return channelMap.computeIfAbsent(key, k -> {
            log.info("[gRPC Client] 建立 Netty HTTP/2 Channel 物理连接: {}", key);
            return ManagedChannelBuilder.forAddress(host, grpcPort)
                    .usePlaintext()
                    .keepAliveTime(30, TimeUnit.SECONDS)
                    .keepAliveTimeout(10, TimeUnit.SECONDS)
                    .build();
        });
    }

    public static GrpcModels.RpcResponse call(String host, int grpcPort, String serviceName, String methodName, Object paramObj) {
        try {
            ManagedChannel channel = getChannel(host, grpcPort);
            MethodDescriptor<GrpcModels.RpcRequest, GrpcModels.RpcResponse> methodDesc =
                    GrpcUtils.createMethodDescriptor(serviceName, "invoke", GrpcModels.RpcRequest.class, GrpcModels.RpcResponse.class);

            GrpcModels.RpcRequest request = new GrpcModels.RpcRequest(methodName, paramObj);
            log.info("[gRPC Client 发起 RPC 调取] {}:{} -> {}.{}()", host, grpcPort, serviceName, methodName);

            return ClientCalls.blockingUnaryCall(channel, methodDesc, CallOptions.DEFAULT, request);
        } catch (Exception e) {
            log.error("[gRPC Client 调用异常] host={}, port={}, service={}, method={}", host, grpcPort, serviceName, methodName, e);
            return GrpcModels.RpcResponse.fail("gRPC 内部服务调用异常: " + e.getMessage());
        }
    }

    public static void shutdownAll() {
        for (Map.Entry<String, ManagedChannel> entry : channelMap.entrySet()) {
            try {
                entry.getValue().shutdown().awaitTermination(5, TimeUnit.SECONDS);
            } catch (Exception ignored) {}
        }
        channelMap.clear();
    }
}
