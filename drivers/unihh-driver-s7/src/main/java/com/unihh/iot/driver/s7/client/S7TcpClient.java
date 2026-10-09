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

package com.unihh.iot.driver.s7.client;

import com.unihh.iot.driver.s7.protocol.codec.S7RequestEncoder;
import com.unihh.iot.driver.s7.protocol.codec.S7ResponseDecoder;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.DataInputStream;
import java.io.IOException;
import java.io.OutputStream;
import java.net.InetSocketAddress;
import java.net.Socket;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.locks.ReentrantLock;

/**
 * 西门子 S7 工业原生 TCP 客户端
 * <p>
 * 自动化托管:
 * 1. TCP 物理链路建连;
 * 2. COTP Connection Request (CR / CC) 握手;
 * 3. S7 Setup Communication 参数协商;
 * 4. TPKT 长度自适应精准定界读写;
 * 5. 通道级线程安全互斥锁与硬件消振。
 */
public class S7TcpClient {
    private static final Logger log = LoggerFactory.getLogger(S7TcpClient.class);

    private final String host;
    private final int port;
    private final int rack;
    private final int slot;
    private final int pduLength;
    private final int connectTimeoutMs;
    private final int readTimeoutMs;

    private static final ConcurrentHashMap<String, ReentrantLock> CHANNEL_LOCKS = new ConcurrentHashMap<>();

    public S7TcpClient(String host, int port, int rack, int slot) {
        this(host, port, rack, slot, 480, 1000, 2000);
    }

    public S7TcpClient(String host, int port, int rack, int slot, int pduLength, int connectTimeoutMs, int readTimeoutMs) {
        this.host = (host != null && !host.trim().isEmpty()) ? host.trim() : "127.0.0.1";
        this.port = (port > 0) ? port : 102;
        this.rack = Math.max(0, rack);
        this.slot = slot > 0 ? slot : 1;
        this.pduLength = pduLength > 0 ? pduLength : 480;
        this.connectTimeoutMs = connectTimeoutMs > 0 ? connectTimeoutMs : 1000;
        this.readTimeoutMs = readTimeoutMs > 0 ? readTimeoutMs : 2000;
    }

    /**
     * 测试西门子 S7 PLC 物理连通性及握手协商全流程
     */
    public boolean testConnection() {
        String channelKey = host + ":" + port;
        ReentrantLock lock = CHANNEL_LOCKS.computeIfAbsent(channelKey, k -> new ReentrantLock(true));
        try {
            if (!lock.tryLock(3, TimeUnit.SECONDS)) {
                return false;
            }
        } catch (InterruptedException e) {
            Thread.currentThread().interrupt();
            return false;
        }

        try (Socket socket = new Socket()) {
            initHandshakeSocket(socket);
            return true;
        } catch (Exception e) {
            log.warn("[S7TcpClient] S7 PLC 握手连接失败 [{}:{}]: {}", host, port, e.getMessage());
            return false;
        } finally {
            lock.unlock();
        }
    }

    /**
     * 执行单次 S7 Job 报文交互 (包含建连、COTP握手、Setup协商、业务交互)
     */
    public byte[] executeJob(byte[] s7JobFrame) throws Exception {
        String channelKey = host + ":" + port;
        ReentrantLock lock = CHANNEL_LOCKS.computeIfAbsent(channelKey, k -> new ReentrantLock(true));

        if (!lock.tryLock(4, TimeUnit.SECONDS)) {
            throw new IOException("S7 PLC 通道繁忙，获取锁超时 [" + channelKey + "]");
        }

        try {
            try (Socket socket = new Socket()) {
                initHandshakeSocket(socket);

                OutputStream out = socket.getOutputStream();
                DataInputStream in = new DataInputStream(socket.getInputStream());

                // 发送业务 S7 报文 (如 Read Var / Write Var)
                out.write(s7JobFrame);
                out.flush();

                // 读取响应帧
                return readTpktPacket(in);
            } finally {
                // 工业设备微延迟消振 (15ms)，让 PLC 状态机就绪
                try {
                    Thread.sleep(15);
                } catch (InterruptedException ignored) {}
            }
        } finally {
            lock.unlock();
        }
    }

    /**
     * 建立 TCP 连接并完成 COTP 与 S7 Setup 协商
     */
    private void initHandshakeSocket(Socket socket) throws Exception {
        socket.setTcpNoDelay(true);
        socket.setReuseAddress(true);
        socket.connect(new InetSocketAddress(host, port), connectTimeoutMs);
        socket.setSoTimeout(readTimeoutMs);

        OutputStream out = socket.getOutputStream();
        DataInputStream in = new DataInputStream(socket.getInputStream());

        // 1. 发送 COTP Connection Request (CR) 握手
        byte[] cotpCr = S7RequestEncoder.encodeCotpConnectRequest(rack, slot);
        out.write(cotpCr);
        out.flush();

        byte[] cotpCc = readTpktPacket(in);
        if (!S7ResponseDecoder.isCotpConnectionConfirmed(cotpCc)) {
            throw new IOException("COTP 连接握手被 PLC 拒绝或超时, 响应: " + (cotpCc != null ? cotpCc.length : 0) + " bytes");
        }

        // 2. 发送 S7 Setup Communication 参数协商
        byte[] setupComm = S7RequestEncoder.encodeSetupCommunication(pduLength);
        out.write(setupComm);
        out.flush();

        byte[] setupAck = readTpktPacket(in);
        if (!S7ResponseDecoder.isSetupCommunicationAck(setupAck)) {
            throw new IOException("S7 Setup Communication 参数协商失败");
        }
    }

    /**
     * 基于 RFC 1006 TPKT 协议自定界定长读取完整报文 (彻底防止半包与粘包)
     */
    private byte[] readTpktPacket(DataInputStream in) throws IOException {
        byte[] tpktHeader = new byte[4];
        in.readFully(tpktHeader);

        if ((tpktHeader[0] & 0xFF) != 0x03) {
            throw new IOException("非标准的 TPKT 报文头: 0x" + Integer.toHexString(tpktHeader[0] & 0xFF));
        }

        int totalLength = ((tpktHeader[2] & 0xFF) << 8) | (tpktHeader[3] & 0xFF);
        if (totalLength < 4 || totalLength > 4096) {
            throw new IOException("异常的 TPKT 报文总长度: " + totalLength);
        }

        byte[] fullPacket = new byte[totalLength];
        System.arraycopy(tpktHeader, 0, fullPacket, 0, 4);

        int remaining = totalLength - 4;
        if (remaining > 0) {
            in.readFully(fullPacket, 4, remaining);
        }

        return fullPacket;
    }

    public String getHost() { return host; }
    public int getPort() { return port; }
    public int getRack() { return rack; }
    public int getSlot() { return slot; }
}
