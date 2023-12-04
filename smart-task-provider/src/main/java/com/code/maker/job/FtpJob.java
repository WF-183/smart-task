package com.code.maker.job;

import com.code.maker.utils.FtpUtils;
import lombok.extern.slf4j.Slf4j;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;

import java.math.BigDecimal;
import java.util.regex.Pattern;

@Component
@Slf4j
public class FtpJob {


    /**
     * 0 0 11 * * ?   每天上午11.00执行一次
     * 0 0/1 * * * ?  每分钟执行一次
     */
    @Scheduled(cron = "0 0 14 * * ?")
    public void execute() {
        // 业务逻辑
        //ftp连接信息
        String hostname = "120.224.39.232";
        int port = 21;
        String username = "oklink";
        String password = "oklink";
        String workingPath = "/home/oklink";
        //对ftp里指定目录下所有文件进行加后缀和mv操作 实测OK
        log.info("FtpJob execute ！！！");
        FtpUtils.renameAndMvFTP(hostname, port, username, password);
    }


    public static void main(String[] args) {
//        String valueStr = "1088888888888889000000000000000";
//        Double valueD = Double.valueOf(valueStr);
//        double precent = new BigDecimal(valueD).divide(new BigDecimal(valueD), 10, BigDecimal.ROUND_HALF_UP).doubleValue();
//        System.out.println(111);

//        Pattern addressPattern = Pattern.compile("[A-Za-z0-9]{34}");
//        boolean b = addressPattern.matcher("12EkhtFAPK2tzavrrRSHXd7FveJ6wcpfR1").matches();
//        System.out.println(b);

//        Long blocktime = 1662122937000000L;
        Long blocktime = 1662122937L;
        if (String.valueOf(blocktime).length() < 13) {
            blocktime = blocktime * 1000;
        }else if (String.valueOf(blocktime).length() > 13) {
            blocktime = blocktime / 1000;
        }
        System.out.println(blocktime);

    }

}
