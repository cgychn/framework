package com.framework.util;

import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.nio.ByteBuffer;

public class InputStreamUtil {


    public static InputStream cloneInputStream (byte[] bs, long totalLen) throws IOException {
        ByteArrayOutputStream bas = new ByteArrayOutputStream();
        bas.write(bs, 0, bs.length);
        bas.flush();
        return new ByteArrayInputStream(bas.toByteArray());
    }

    public static InputStream cloneInputStream (InputStream inputStream, long totalLen) throws IOException {
        ByteArrayOutputStream bas = new ByteArrayOutputStream();
        byte[] buffer = new byte[4096];
        int len;
        long currentLen = 0l;
        while ((len = inputStream.read(buffer)) != -1 || currentLen < totalLen) {
            bas.write(buffer, 0, len);
            currentLen += len;
            System.out.println(currentLen);
        }
        bas.flush();
        return new ByteArrayInputStream(bas.toByteArray());
    }

    public static long getStreamLen (InputStream inputStream) throws IOException {
        byte[] lenBytes = new byte[8];
        // 一定会大于8个字节，应为前64个字节表示数据包的长度
        long packLen = 0l;
        // 阻塞直到读到8位
        while (inputStream.available() < 8) {}
        if (inputStream.read(lenBytes) != -1) {
            // 获取到数据包的长度
            ByteBuffer buffer = ByteBuffer.allocate(8);
            buffer.put(lenBytes, 0, 8);
            buffer.flip();
            packLen = buffer.getLong();
        }
        return packLen;
    }

}
