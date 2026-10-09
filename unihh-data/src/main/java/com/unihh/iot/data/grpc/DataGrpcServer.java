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

package com.unihh.iot.data.grpc;

import com.unihh.iot.common.grpc.GrpcModels;
import com.unihh.iot.common.grpc.GrpcServerRunner;
import com.unihh.iot.common.model.HistoryQuery;
import com.unihh.iot.common.model.PointData;
import com.unihh.iot.data.service.DataIngestionService;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.CommandLineRunner;
import org.springframework.stereotype.Component;

import java.util.ArrayList;
import java.util.List;

/**
 * unihh-data 内部微服务 gRPC 服务端监听组件 (端口 :29002)
 */
@Component
public class DataGrpcServer implements CommandLineRunner {
    private static final Logger log = LoggerFactory.getLogger(DataGrpcServer.class);

    @Value("${unihh.grpc.port:29002}")
    private int grpcPort;

    @Autowired
    private DataIngestionService ingestionService;

    @Override
    public void run(String... args) throws Exception {
        log.info("[Data gRPC] 正在启动数据管理微服务内部 gRPC 服务端 (端口: {})...", grpcPort);

        GrpcServerRunner.start(grpcPort, "DataGrpcService", request -> {
            String method = request.getMethod();
            log.info("[Data gRPC 服务端接收 RPC 调取] method={}", method);

            try {
                switch (method) {
                    case "ingestPoint":
                        PointData data = request.parseParam(PointData.class);
                        ingestionService.processPointData(data);
                        return GrpcModels.RpcResponse.ok("点位数据经 gRPC 接入清洗完成");
                    case "queryHistory":
                        HistoryQuery query = request.parseParam(HistoryQuery.class);
                        List<PointData> list = ingestionService.queryHistory(query);
                        return GrpcModels.RpcResponse.ok(list);
                    case "getLatestSnapshot":
                        String devId = request.getParamJson();
                        if (devId != null && devId.startsWith("\"") && devId.endsWith("\"")) {
                            devId = devId.substring(1, devId.length() - 1);
                        }
                        return GrpcModels.RpcResponse.ok(ingestionService.getLatestSnapshot(devId));
                    default:
                        return GrpcModels.RpcResponse.fail("未知 Data gRPC 方法: " + method);
                }
            } catch (Exception e) {
                log.error("[Data gRPC 执行异常] method={}", method, e);
                return GrpcModels.RpcResponse.fail("Data gRPC 执行失败: " + e.getMessage());
            }
        });
    }
}
