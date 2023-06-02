package com.code.maker.job;

import com.code.maker.utils.FtpUtils;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;

@Component
public class FtpJob {


    /**
     * 0 0 11 * * ?   每天上午11.00执行一次
     * 0 0/1 * * * ?  每分钟执行一次
     */
    @Scheduled(cron = "0 0 11 * * ?")
    public void execute() {
        // 业务逻辑
        //ftp连接信息
        String hostname = "120.224.39.232";
        int port = 21;
        String username = "oklink";
        String password = "oklink";
        String workingPath = "/home/oklink";
        //对ftp里指定目录下所有文件进行加后缀和mv操作 实测OK
        FtpUtils.renameAndMvFTP(hostname, port, username, password);
    }


}
