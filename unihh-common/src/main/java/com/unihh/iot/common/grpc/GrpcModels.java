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

import java.io.Serializable;

/**
 * 统一 gRPC 内部服务通信 DTO 封装模型
 */
public class GrpcModels {

    public static class RpcRequest implements Serializable {
        private static final long serialVersionUID = 1L;

        private String method;
        private String paramJson;

        public RpcRequest() {}

        public RpcRequest(String method, Object paramObj) {
            this.method = method;
            this.paramJson = paramObj != null ? JSON.toJSONString(paramObj) : "";
        }

        public String getMethod() { return method; }
        public void setMethod(String method) { this.method = method; }
        public String getParamJson() { return paramJson; }
        public void setParamJson(String paramJson) { this.paramJson = paramJson; }

        public <T> T parseParam(Class<T> clazz) {
            if (paramJson == null || paramJson.isEmpty()) return null;
            return JSON.parseObject(paramJson, clazz);
        }
    }

    public static class RpcResponse implements Serializable {
        private static final long serialVersionUID = 1L;

        private int code; // 200: SUCCESS, 500: ERROR
        private String msg;
        private String dataJson;

        public RpcResponse() {}

        public static RpcResponse ok(Object data) {
            RpcResponse resp = new RpcResponse();
            resp.setCode(200);
            resp.setMsg("OK");
            resp.setDataJson(data != null ? JSON.toJSONString(data) : "");
            return resp;
        }

        public static RpcResponse fail(String msg) {
            RpcResponse resp = new RpcResponse();
            resp.setCode(500);
            resp.setMsg(msg);
            resp.setDataJson("");
            return resp;
        }

        public int getCode() { return code; }
        public void setCode(int code) { this.code = code; }
        public boolean isSuccess() { return code == 200; }
        public String getMsg() { return msg; }
        public String getMessage() { return msg; }
        public void setMsg(String msg) { this.msg = msg; }
        public String getDataJson() { return dataJson; }
        public void setDataJson(String dataJson) { this.dataJson = dataJson; }

        public <T> T parseData(Class<T> clazz) {
            if (dataJson == null || dataJson.isEmpty()) return null;
            return JSON.parseObject(dataJson, clazz);
        }
    }
}
