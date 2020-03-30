/*
 * File: IfoodAdminApplication
 * Author: DorSey Q F TANG
 * Created: 2020/3/30
 * CopyRight: All rights reserved
 */
package com.xbg.ifood.admin;

import org.mybatis.spring.annotation.MapperScan;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;

@MapperScan(basePackages = {"com.xbg.ifood.admin.repository"})
@SpringBootApplication
public class IFoodAdminApplication {
    public static void main(String[] args) {
        SpringApplication.run(IFoodAdminApplication.class, args);
    }
}
