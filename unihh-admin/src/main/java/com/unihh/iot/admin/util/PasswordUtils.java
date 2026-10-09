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

package com.unihh.iot.admin.util;

import java.nio.charset.StandardCharsets;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;
import java.security.SecureRandom;

/**
 * 密码加盐与加密安全工具类
 * 采用 SHA-256 结合 16 字节 (32 字符 Hex) 独立随机盐 (Salt) 进行哈希加密
 */
public class PasswordUtils {

    private static final SecureRandom SECURE_RANDOM = new SecureRandom();

    /**
     * 生成 16 字节 (32 字符 Hex) 随机盐
     */
    public static String generateSalt() {
        byte[] saltBytes = new byte[16];
        SECURE_RANDOM.nextBytes(saltBytes);
        StringBuilder sb = new StringBuilder(32);
        for (byte b : saltBytes) {
            sb.append(String.format("%02x", b));
        }
        return sb.toString();
    }

    /**
     * 根据明文密码与盐值计算 SHA-256 哈希
     * 算法公式: Hex(SHA-256(salt + rawPassword))
     *
     * @param rawPassword 用户输入的明文密码
     * @param salt        随机盐值
     * @return 64 字符十六进制密文
     */
    public static String encrypt(String rawPassword, String salt) {
        if (rawPassword == null || salt == null) {
            throw new IllegalArgumentException("密码或盐值不能为空");
        }
        try {
            MessageDigest md = MessageDigest.getInstance("SHA-256");
            md.update(salt.getBytes(StandardCharsets.UTF_8));
            byte[] digest = md.digest(rawPassword.getBytes(StandardCharsets.UTF_8));
            StringBuilder sb = new StringBuilder(64);
            for (byte b : digest) {
                sb.append(String.format("%02x", b));
            }
            return sb.toString();
        } catch (NoSuchAlgorithmException e) {
            throw new RuntimeException("系统缺少 SHA-256 算法支持", e);
        }
    }

    /**
     * 校验明文密码与加盐密文是否匹配
     *
     * @param rawPassword       用户输入的明文密码
     * @param salt              数据库存储的盐值
     * @param encryptedPassword 数据库存储的加盐密文
     * @return true 匹配成功，false 密码错误
     */
    public static boolean matches(String rawPassword, String salt, String encryptedPassword) {
        if (rawPassword == null || salt == null || encryptedPassword == null) {
            return false;
        }
        String calculated = encrypt(rawPassword, salt);
        return calculated.equalsIgnoreCase(encryptedPassword);
    }
}
