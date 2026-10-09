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

import com.alibaba.fastjson2.JSON;
import io.grpc.MethodDescriptor;

import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.InputStream;

/**
 * 高性能零编解码 gRPC JSON/Binary 序列化 Marshaller 工具类
 */
public class GrpcUtils {

    public static <T> MethodDescriptor.Marshaller<T> jsonMarshaller(final Class<T> clazz) {
        return new MethodDescriptor.Marshaller<T>() {
            @Override
            public InputStream stream(T value) {
                byte[] bytes = JSON.toJSONBytes(value);
                return new ByteArrayInputStream(bytes);
            }

            @Override
            public T parse(InputStream stream) {
                try {
                    ByteArrayOutputStream buffer = new ByteArrayOutputStream();
                    int nRead;
                    byte[] data = new byte[1024];
                    while ((nRead = stream.read(data, 0, data.length)) != -1) {
                        buffer.write(data, 0, nRead);
                    }
                    buffer.flush();
                    byte[] bytes = buffer.toByteArray();
                    return JSON.parseObject(bytes, clazz);
                } catch (Exception e) {
                    throw new RuntimeException("gRPC 反序列化失败: " + e.getMessage(), e);
                }
            }
        };
    }

    public static <ReqT, RespT> MethodDescriptor<ReqT, RespT> createMethodDescriptor(String serviceName, String methodName, Class<ReqT> requestClass, Class<RespT> responseClass) {
        return MethodDescriptor.<ReqT, RespT>newBuilder()
                .setType(MethodDescriptor.MethodType.UNARY)
                .setFullMethodName(MethodDescriptor.generateFullMethodName(serviceName, methodName))
                .setRequestMarshaller(jsonMarshaller(requestClass))
                .setResponseMarshaller(jsonMarshaller(responseClass))
                .build();
    }
}
