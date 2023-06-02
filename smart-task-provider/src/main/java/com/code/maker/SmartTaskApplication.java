package com.code.maker;

import lombok.extern.slf4j.Slf4j;
import org.apache.ibatis.annotations.Mapper;
import org.mybatis.spring.annotation.MapperScan;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.boot.autoconfigure.jdbc.DataSourceAutoConfiguration;
import org.springframework.boot.autoconfigure.orm.jpa.HibernateJpaAutoConfiguration;
import org.springframework.cloud.openfeign.EnableFeignClients;
import org.springframework.context.annotation.EnableAspectJAutoProxy;
import org.springframework.scheduling.annotation.EnableScheduling;
import org.springframework.transaction.annotation.EnableTransactionManagement;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.bind.annotation.RestController;

/**
 * @ClassName ExecutorApplication
 * @Description
 **/
//暂时关闭Eureka服务发现
//@EnableCircuitBreaker
//@EnableDiscoveryClient
@Slf4j
@EnableScheduling
@EnableAspectJAutoProxy(proxyTargetClass = true, exposeProxy = true)
@EnableFeignClients(basePackages = {"com.code.maker"})
@EnableTransactionManagement
@MapperScan(value = {"com.code.maker.mapper"}, annotationClass = Mapper.class)
@SpringBootApplication(scanBasePackages = {"com.code.maker"}, exclude = {DataSourceAutoConfiguration.class, HibernateJpaAutoConfiguration.class})
@RestController
public class SmartTaskApplication {

    public static void main(final String[] args) {
        try {
            SpringApplication application = new SpringApplication(SmartTaskApplication.class);
            //application.setAllowBeanDefinitionOverriding(true);
            //application.setAllowCircularReferences(true);
            application.run(args);
        } catch (Exception e) {
            System.out.println(e.getMessage());
        }
    }

    @RequestMapping(path = {"/", "health"}, method = RequestMethod.GET)
    public void health() {
    }

}


