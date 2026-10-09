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

package com.unihh.iot.driver.opcua;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;

/**
 * OPC UA 协议驱动微服务启动类 (:28013)
 * 支持在 IDE 中直接单独启动并进行 OPC UA 节点通信调试
 */
@SpringBootApplication(scanBasePackages = "com.unihh.iot")
public class OpcUaDriverApplication {
    public static void main(String[] args) {
        System.setProperty("server.port", "28013");
        SpringApplication.run(OpcUaDriverApplication.class, args);
    }
}
