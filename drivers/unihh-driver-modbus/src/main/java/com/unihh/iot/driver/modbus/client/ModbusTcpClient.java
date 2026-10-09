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

package com.unihh.iot.driver.modbus.client;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.InputStream;
import java.io.OutputStream;
import java.net.InetSocketAddress;
import java.net.Socket;
import java.util.Arrays;

/**
 * Modbus TCP 原生 Socket 网络客户端
 * 负责建立网络连接、发送二进制请求帧并读取返回数据
 */
public class ModbusTcpClient {
    private static final Logger log = LoggerFactory.getLogger(ModbusTcpClient.class);

    private final String host;
    private final int port;
    private final int connectTimeoutMs;
    private final int readTimeoutMs;

    public ModbusTcpClient(String host, int port) {
        this(host, port, 500, 1000);
    }

    public ModbusTcpClient(String host, int port, int connectTimeoutMs, int readTimeoutMs) {
        this.host = (host != null && !host.trim().isEmpty()) ? host : "127.0.0.1";
        this.port = (port > 0) ? port : 502;
        this.connectTimeoutMs = connectTimeoutMs;
        this.readTimeoutMs = readTimeoutMs;
    }

    private static final java.util.concurrent.ConcurrentHashMap<String, java.util.concurrent.locks.ReentrantLock> DEVICE_LOCKS = new java.util.concurrent.ConcurrentHashMap<>();

    /**
     * 发送 Modbus 二进制请求帧并接收响应字节数组 (带设备级通信互斥锁)
     */
    public byte[] executeFrame(byte[] requestBytes) throws Exception {
        String deviceKey = host + ":" + port;
        java.util.concurrent.locks.ReentrantLock lock = DEVICE_LOCKS.computeIfAbsent(deviceKey, k -> new java.util.concurrent.locks.ReentrantLock(true));

        if (!lock.tryLock(4, java.util.concurrent.TimeUnit.SECONDS)) {
            throw new java.io.IOException("设备通信通道繁忙, 获取通信锁超时 [" + deviceKey + "]");
        }

        try {
            try (Socket socket = new Socket()) {
                socket.setTcpNoDelay(true);
                socket.setReuseAddress(true);
                socket.connect(new InetSocketAddress(host, port), connectTimeoutMs);
                socket.setSoTimeout(readTimeoutMs);

                OutputStream out = socket.getOutputStream();
                InputStream in = socket.getInputStream();

                out.write(requestBytes);
                out.flush();

                byte[] buffer = new byte[512];
                int bytesRead = in.read(buffer);
                if (bytesRead > 0) {
                    return Arrays.copyOf(buffer, bytesRead);
                } else {
                    throw new java.io.IOException("Socket 读返回 0 字节, 目标设备未响应");
                }
            } finally {
                // 通信帧间隔微休眠 (20ms)，保障 Modbus 从站微控制器状态机完全复位
                try {
                    Thread.sleep(20);
                } catch (InterruptedException ignored) {}
            }
        } finally {
            lock.unlock();
        }
    }

    public String getHost() { return host; }
    public int getPort() { return port; }
}
