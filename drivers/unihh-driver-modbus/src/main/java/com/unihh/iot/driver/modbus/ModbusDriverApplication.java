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

package com.unihh.iot.driver.modbus;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;

/**
 * Modbus TCP 协议驱动微服务启动类 (:28011)
 * 支持 IDE 中直接右键运行该 App，提供直观的 REST/Socket 边开发边调试接口
 */
@SpringBootApplication(scanBasePackages = "com.unihh.iot")
public class ModbusDriverApplication {
    public static void main(String[] args) {
        System.setProperty("server.port", "28011");
        SpringApplication.run(ModbusDriverApplication.class, args);
    }
}
