package com.code.maker.utils;


import lombok.extern.slf4j.Slf4j;
import org.apache.commons.net.ftp.FTP;
import org.apache.commons.net.ftp.FTPClient;
import org.apache.commons.net.ftp.FTPFile;

import java.io.*;
import java.util.List;


/**
 * NOTE:
 * 经Ftputils上传后的es bulk文件字节数会发生变化（会略增加），但实测不影响es写入效果，check diff时注意逻辑兼容
 */
@Slf4j
public class FtpUtils {


    public static void main(String[] args) throws FileNotFoundException {
        //ftp连接信息
        String hostname = "120.224.39.232";
        int port = 21;
        String username = "oklink";
        String password = "oklink";
        String workingPath = "/home/oklink";

        //单个文件上传 实测OK
        //InputStream fileInputStream = new FileInputStream(new File("/Users/wangfei/Downloads/data2/tag/__3acfe155b34a47d6a07a7874d12937ae"));
        //String saveName = "__3acfe155b34a47d6a07a7874d12937ae";
        //FtpUtils.upload( hostname, port, username, password, workingPath, fileInputStream, saveName);

        //指定文件夹下指定List<String>文件名批量上传 实测OK
        //String dir = "/Users/wangfei/Downloads/data2/token_tx";
        //List<String> fileNameList = new ArrayList<>();
        //fileNameList.add("__5ce36b54610e4ff09c69c1d0797302e7_1");
        //fileNameList.add("__8c32f9ac97344aedb0f600bac0065001_1");
        //fileNameList.add("__3581d3cd1f1c422f86dcc18705f1150c_1");
        //FtpUtils.uploadFTPByListFiles( hostname, port, username, password, workingPath, dir, fileNameList);


        //对ftp里指定目录下所有文件进行加后缀和mv操作 实测OK
        FtpUtils.renameAndMvFTP( hostname, port, username, password);

    }


    /**
     * java ftp重命名&mv操作
     * 对ftp服务器指定目录下的所有文件进行重命名加后缀和mv移动文件夹操作
     * @param hostname ftp服务器ip或域名地址
     * @param port  ftp服务器端口
     * @param username ftp服务器用户名
     * @param password ftp服务器密码
     * @return
     */
    public static void renameAndMvFTP(String hostname, int port, String username, String password) {
        //效果：对/home/oklink/data_inc下所有文件添加.txt后缀并移动到/home/oklink/下
        //ftp服务器下的基准工作目录
        String workingPath = "/home/oklink/data_inc";

        FTPClient ftpClient = new FTPClient();
        try {
            //PART1 设置ftp初始化参数
            ftpClient.connect(hostname, port);
            //设置被动模式
            ftpClient.enterLocalPassiveMode();
            //设置流上传方式
            ftpClient.setFileTransferMode(FTP.STREAM_TRANSFER_MODE );
            //必须使用二进制格式 使得在ftp传输过程中不对文件内容进行任何处理
            ftpClient.setFileType(FTP.BINARY_FILE_TYPE);
            ftpClient.setControlEncoding("UTF-8");
            //超时时间
            ftpClient.setDefaultTimeout(600*1000);
            ftpClient.setDataTimeout(600*1000);
            //PART2 登陆
            if (ftpClient.login(username, password)) {
                log.info("FTP登陆连接成功");
                //PART3 切换工作目录 要求必须已存在
                if (ftpClient.changeWorkingDirectory(workingPath)) {
                    //PART4 执行操作
                    //重命名ftp服务器指定目录下文件+mv到指定目录
                    FTPFile[] ftpFiles = ftpClient.listFiles();
                    for (FTPFile ftpFile : ftpFiles) {
                        String name = ftpFile.getName();
                        //实测 必须使用相对路径的方式才能进行重命名+移动同时操作得效果，该方法有返回值，操作成功返回true
                        if (ftpClient.rename(name,"../"+name+".txt")) {
                            log.info("FTP文件renameAndMv操作成功 {}",name);
                        }
                    }
                }
            }
        }catch (Exception ex){
            ex.printStackTrace();
        }finally {
            //PART5 关流
            if (ftpClient.isConnected()) {
                try {
                    ftpClient.disconnect();
                    log.info("FTP退出关流成功");
                } catch (IOException e) {
                    throw new RuntimeException(e);
                }
            }
        }
    }



    /**
     * java ftp上传文件
     * 指定文件夹下指定List<String>文件名批量上传
     * @param hostname ftp服务器ip或域名地址
     * @param port  ftp服务器端口
     * @param username ftp服务器用户名
     * @param password ftp服务器密码
     * @param workingPath ftp服务器下的基准工作目录
     * @param dir  要上传文件所在本地目录的绝对路径 最后不带/
     * @param fileNameList  dir下要上传的文件名
     * @return
     */
    public static void uploadFTPByListFiles(String hostname, int port, String username, String password, String workingPath, String dir , List<String> fileNameList) {
        FTPClient ftpClient = new FTPClient();
        try {
            //PART1 设置ftp初始化参数
            ftpClient.connect(hostname, port);
            // 设置被动模式
            ftpClient.enterLocalPassiveMode();
            // 设置流上传方式
            ftpClient.setFileTransferMode(FTP.STREAM_TRANSFER_MODE );
            //必须使用二进制格式 使得在ftp传输过程中不对文件内容进行任何处理
            ftpClient.setFileType(FTP.BINARY_FILE_TYPE);
            ftpClient.setControlEncoding("UTF-8");
            //超时时间
            ftpClient.setDefaultTimeout(600*1000);
            ftpClient.setDataTimeout(600*1000);
            //PART2 登陆
            if (ftpClient.login(username, password)) {
                log.info("FTP登陆连接成功");
                //PART3 切换工作目录 要求必须已存在
                if (ftpClient.changeWorkingDirectory(workingPath)) {
                    //PART4 执行上传
                    for (String filename : fileNameList) {
                        InputStream fileInputStream = new FileInputStream(new File(dir + "/" + filename));
                        if (ftpClient.storeFile(filename, fileInputStream)) {
                            log.info("文件上传FTP成功 {}",filename);
                        }
                    }
                }
            }
        }catch (Exception ex){
            ex.printStackTrace();
        }finally {
            //PART5 关流
            if (ftpClient.isConnected()) {
                try {
                    ftpClient.disconnect();
                    log.info("FTP退出关流成功");
                } catch (IOException e) {
                    throw new RuntimeException(e);
                }
            }
        }
    }




    /**
     * java ftp上传文件
     * 单个文件上传
     * @param hostname ip或域名地址
     * @param port  端口
     * @param username 用户名
     * @param password 密码
     * @param workingPath 服务器的工作目
     * @param inputStream 要上传文件的输入流
     * @param saveName    设置上传之后的文件名
     * @return
     */
    public static boolean upload(String hostname, int port, String username, String password, String workingPath, InputStream inputStream, String saveName) {
        boolean flag = false;
        FTPClient ftpClient = new FTPClient();
        //1 测试连接
        if (connect(ftpClient, hostname, port, username, password)) {
            try {
                //2 检查工作目录是否存在
                if (ftpClient.changeWorkingDirectory(workingPath)) {
                    // 3 检查是否上传成功
                    if (storeFile(ftpClient, saveName, inputStream)) {
                        flag = true;
                        disconnect(ftpClient);
                    }
                }
            } catch (IOException e) {
                log.error("工作目录不存在");
                e.printStackTrace();
                disconnect(ftpClient);
            }
        }
        return flag;
    }

    /**
     * 断开连接
     *
     * @param ftpClient
     * @throws Exception
     */
    public static void disconnect(FTPClient ftpClient) {
        if (ftpClient.isConnected()) {
            try {
                ftpClient.disconnect();
                log.error("已关闭连接");
            } catch (IOException e) {
                log.error("没有关闭连接");
                e.printStackTrace();
            }
        }
    }

    /**
     * 测试是否能连接
     *
     * @param ftpClient
     * @param hostname  ip或域名地址
     * @param port      端口
     * @param username  用户名
     * @param password  密码
     * @return 返回真则能连接
     */
    public static boolean connect(FTPClient ftpClient, String hostname, int port, String username, String password) {
        boolean flag = false;
        try {
            //ftp初始化的一些参数
            ftpClient.connect(hostname, port);
            // 设置被动模式
            ftpClient.enterLocalPassiveMode();
            // 设置流上传方式
            ftpClient.setFileTransferMode(FTP.STREAM_TRANSFER_MODE );
            //必须使用二进制格式 使得在ftp传输过程中不对文件内容进行任何处理
            ftpClient.setFileType(FTP.BINARY_FILE_TYPE);
            ftpClient.setControlEncoding("UTF-8");
            //超时时间
            ftpClient.setDefaultTimeout(600*1000);
            ftpClient.setDataTimeout(600*1000);
            if (ftpClient.login(username, password)) {
                log.info("连接ftp成功");
                flag = true;
            } else {
                log.error("连接ftp失败，可能用户名或密码错误");
                try {
                    disconnect(ftpClient);
                } catch (Exception e) {
                    e.printStackTrace();
                }
            }
        } catch (IOException e) {
            log.error("连接失败，可能ip或端口错误");
            e.printStackTrace();
        }
        return flag;
    }

    /**
     * 上传文件
     *
     * @param ftpClient
     * @param saveName        全路径。如/home/public/a.txt
     * @param fileInputStream 要上传的文件流
     * @return
     */
    public static boolean storeFile(FTPClient ftpClient, String saveName, InputStream fileInputStream) {
        boolean flag = false;
        try {
            if (ftpClient.storeFile(saveName, fileInputStream)) {
                flag = true;
                log.error("上传成功");
                disconnect(ftpClient);
            }
        } catch (IOException e) {
            log.error("上传失败");
            disconnect(ftpClient);
            e.printStackTrace();
        }
        return flag;
    }

    /**
     *
     *
     *
     *
     *
     */


}
