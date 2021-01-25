package com.atguigu.gmall.product.test;

import org.csource.common.MyException;
import org.csource.fastdfs.ClientGlobal;
import org.csource.fastdfs.StorageClient;
import org.csource.fastdfs.TrackerClient;
import org.csource.fastdfs.TrackerServer;

import java.io.IOException;

public class TestFastFDS {
    public static void main(String[] args) {
        //获取tracker.conf文件的绝对路径
        String path = TestFastFDS.class.getClassLoader().getResource("tracker.conf").getPath();
        System.out.println(path);
        String url = "http://42.192.250.48:80";
        try {
            // 初始化
            ClientGlobal.init(path);

            // 获得tracker连接
            TrackerClient trackerClient = new TrackerClient();
            TrackerServer connection = trackerClient.getConnection();

            // 通过tracker获得storage
            StorageClient storageClient = new StorageClient(connection, null);

            // 上传文件
            String[] jpgs = storageClient.upload_file("C:\\Users\\Administrator\\Desktop\\c.jpg", "jpg", null);

            // 返回url
            for (String jpg : jpgs) {
                System.out.println(jpg);
                url = url + "/" + jpg;
            }
            System.out.println(url);
        } catch (Exception e) {
            e.printStackTrace();
        }
    }
}
