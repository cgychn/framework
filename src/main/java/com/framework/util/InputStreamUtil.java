package com.framework.util;

import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.InputStream;

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

}
