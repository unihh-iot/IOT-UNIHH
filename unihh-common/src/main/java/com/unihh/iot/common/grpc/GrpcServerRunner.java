/*
 * Copyright (c) 2026 unihh.com All Rights Reserved.
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file me except in compliance with the License.
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

import io.grpc.MethodDescriptor;
import io.grpc.Server;
import io.grpc.ServerBuilder;
import io.grpc.ServerServiceDefinition;
import io.grpc.stub.ServerCalls;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.function.Function;

/**
 * 统一微服务 gRPC 服务端监听启动器
 */
public class GrpcServerRunner {
    private static final Logger log = LoggerFactory.getLogger(GrpcServerRunner.class);

    private Server server;

    public static GrpcServerRunner start(int port, String serviceName, Function<GrpcModels.RpcRequest, GrpcModels.RpcResponse> handler) {
        GrpcServerRunner runner = new GrpcServerRunner();
        try {
            MethodDescriptor<GrpcModels.RpcRequest, GrpcModels.RpcResponse> methodDesc =
                    GrpcUtils.createMethodDescriptor(serviceName, "invoke", GrpcModels.RpcRequest.class, GrpcModels.RpcResponse.class);

            ServerServiceDefinition serviceDefinition = ServerServiceDefinition.builder(serviceName)
                    .addMethod(methodDesc, ServerCalls.asyncUnaryCall((request, responseObserver) -> {
                        try {
                            GrpcModels.RpcResponse response = handler.apply(request);
                            responseObserver.onNext(response);
                            responseObserver.onCompleted();
                        } catch (Exception e) {
                            log.error("[gRPC Server 处理异常] service={}, method={}", serviceName, request.getMethod(), e);
                            responseObserver.onNext(GrpcModels.RpcResponse.fail("RPC 异常: " + e.getMessage()));
                            responseObserver.onCompleted();
                        }
                    }))
                    .build();

            runner.server = ServerBuilder.forPort(port)
                    .addService(serviceDefinition)
                    .build()
                    .start();

            log.info("[gRPC Microservice Server] 已成功启动 Netty HTTP/2 gRPC 监听端口: {} (服务名: {})", port, serviceName);

            Runtime.getRuntime().addShutdownHook(new Thread(() -> {
                log.info("[gRPC Server] 正在关闭 gRPC 监听端口: {}", port);
                runner.stop();
            }));

        } catch (Exception e) {
            log.error("[gRPC Server 启动失败] port={}, service={}", port, serviceName, e);
        }
        return runner;
    }

    public void stop() {
        if (server != null) {
            server.shutdown();
        }
    }
}
