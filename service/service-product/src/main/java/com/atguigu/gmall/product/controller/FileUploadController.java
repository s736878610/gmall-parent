package com.atguigu.gmall.product.controller;

import com.atguigu.gmall.product.service.FileUploadService;
import com.atguigu.gmall.product.test.TestFastFDS;
import com.atguigu.gmall.result.Result;
import org.csource.fastdfs.ClientGlobal;
import org.csource.fastdfs.StorageClient;
import org.csource.fastdfs.TrackerClient;
import org.csource.fastdfs.TrackerServer;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.util.StringUtils;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;

@RestController
@RequestMapping("admin/product/")
@CrossOrigin
public class FileUploadController {

    @Autowired
    private FileUploadService fileUploadService;

    /**
     * 文件上传
     * @param file
     * @return
     */
    @PostMapping("fileUpload")
    public Result fileUpload(@RequestParam("file") MultipartFile file) {
        //获取tracker.conf文件的绝对路径
        String path = FileUploadController.class.getClassLoader().getResource("tracker.conf").getPath();
        //String url = "http://42.192.250.48:80";
        String url = "http://192.168.200.128:8080";
        try {
            // 初始化
            ClientGlobal.init(path);

            // 获得tracker连接
            TrackerClient trackerClient = new TrackerClient();
            TrackerServer connection = trackerClient.getConnection();

            // 通过tracker获得storage
            StorageClient storageClient = new StorageClient(connection, null);

            // 上传文件
            String originalFilename = file.getOriginalFilename();
            //upload_file(byte[] file_buff 文件路径,
            //            String file_ext_name 文件后缀,
            //            NameValuePair[] meta_list 元数据)
            String[] jpgs = storageClient.upload_file(file.getBytes(), StringUtils.getFilenameExtension(originalFilename), null);
            // 返回url
            for (String jpg : jpgs) {
                url = url + "/" + jpg;
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
        System.out.println(url);
        return Result.ok(url);
    }
}
