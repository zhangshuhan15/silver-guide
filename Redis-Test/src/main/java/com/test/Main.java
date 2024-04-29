package com.test;

import java.io.*;
import java.net.Socket;
import java.nio.charset.StandardCharsets;
import java.util.ArrayList;
import java.util.List;

/**
 * ClassName: Main
 * Package: com.test
 * Description:
 *
 * @Author zhangdalu
 * @Create 2024/4/28 16:57
 * @Version 1.0
 */
public class Main {
    static Socket s;
    static PrintWriter writer;
    static BufferedReader reader;

    public static void main(String[] args) {
        try {
            //1.建立连接
            String host = "127.0.0.1";
            int port = 6379;
            s = new Socket(host, port);
            //2.获取输出流输入流
            writer = new PrintWriter(new OutputStreamWriter(s.getOutputStream(), StandardCharsets.UTF_8));
            reader = new BufferedReader(new InputStreamReader(s.getInputStream(), StandardCharsets.UTF_8));

            //3.发出请求
            //3.1获取授权 auth 123321
            //sendResquest("auth", "123321");
            //Object obj = handerResponse();
            //System.out.println("obj=" + obj);
            //3.2发出请求 set name 胡歌
            /*sendResquest("set", "name", "胡歌");
            //4.解析响应
            Object obj = handerResponse();
            System.out.println("obj=" + obj);*/

            sendResquest("get", "chen");
            Object obj = handerResponse();
            System.out.println("obj=" + obj);


        } catch (IOException e) {
            throw new RuntimeException(e);
        } finally {
            //5.释放连接
            if (reader != null) {
                try {
                    reader.close();
                    if (writer != null) writer.close();
                    if (s != null) s.close();
                } catch (IOException e) {
                    e.printStackTrace();
                }
            }

        }


    }

    private static Object handerResponse() throws IOException {
        //读取首字节，判断数据类型的标识
        int prefix = reader.read();
        //判断五种类型标识
        switch (prefix) {
            case '+'://单行字符串，直接读一行
                return reader.readLine();
            case '-'://异常，也读一行
                throw new RuntimeException(reader.readLine());
            case ':'://数字
                return Long.parseLong(reader.readLine());
            case '$'://多行字符串
                //先读长度
                int len = Integer.parseInt(reader.readLine());
                if (len == -1) {
                    return null;
                }
                if (len == 0) {
                    return "";
                }
                //再读数据,度len个字节，我们假设没有特殊字符，所以读一行（简化）
                return reader.readLine();
            case '*'://数组
                return readBulkString();
            default:
                throw new RuntimeException("错误的数据格式");

        }

    }

    private static Object readBulkString() throws IOException {
        //获取数组大小
        int len = Integer.parseInt(reader.readLine());
        if (len <= 0) {
            return null;
        }
        //定义集合 接受多个元素
        List<Object> list = new ArrayList<>(len);
        //遍历 依次读取每个元素
        for (int i = 0; i < len; i++) {
            list.add(handerResponse());
        }
        return list;
    }

    //set name 胡歌
    private static void sendResquest(String... args) {
        writer.println("*" + args.length);
        for (String arg : args) {
            writer.println("$" + arg.getBytes(StandardCharsets.UTF_8).length);
            writer.println(arg);
        }
        writer.flush();

    }
}
