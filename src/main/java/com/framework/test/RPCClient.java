package com.framework.test;

import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.ObjectOutputStream;
import java.io.OutputStream;
import java.net.Socket;
import java.nio.ByteBuffer;

public class RPCClient {


    public static void main(String[] args) {
        try {
            Socket socket = new Socket("localhost", 9098);
            OutputStream outputStream = socket.getOutputStream();
            User user = new User();
            user.setUserName("ppppp");
            user.setLoginName("dddd");
            user.setPassword("asdwqqsdawd");
            System.out.println(user);

            byte[] bs = ObjectToByte(user);
            long size = bs.length;
            System.out.println();
            System.out.println(size + 8);
            outputStream.write(longToBytes(size + 8));
            outputStream.write(bs);
            outputStream.flush();
            outputStream.close();
            socket.close();
        } catch (IOException e) {
            e.printStackTrace();
        }
    }


    public static byte[] ObjectToByte(Object obj) {
        byte[] bytes = null;
        try {
            // object to bytearray
            ByteArrayOutputStream bo = new ByteArrayOutputStream();
            ObjectOutputStream oo = new ObjectOutputStream(bo);
            oo.writeObject(obj);
            bytes = bo.toByteArray();
            bo.close();
            oo.close();
        } catch (Exception e) {
            System.out.println("translation" + e.getMessage());
            e.printStackTrace();
        }
        return bytes;
    }

    public static byte[] longToBytes(long l) {
        byte[] result = new byte[8];
        for (int i = 7; i >= 0; i--) {
            result[i] = (byte)(l & 0xFF);
            l >>= 8;
        }
        return result;}public static long bytesToLong(byte[] b) {
        long result = 0;
        for (int i = 0; i < 8; i++) {
            result <<= 8;
            result |= (b[i] & 0xFF);
        }
        return result;
    }
}
