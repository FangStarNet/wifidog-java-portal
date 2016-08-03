/*
 * Copyright (c) 2016, fangstar.com
 *
 * All rights reserved.
 */
package net.fangstar.wifi.portal;

import java.util.ResourceBundle;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;

/**
 * WiFiPortal 服务.
 *
 * @author <a href="http://88250.b3log.org">Liang Ding</a>
 * @version 1.0.0.0, Aug 1, 2016
 * @since 1.0.0
 */
@SpringBootApplication
public class Server {

    /**
     * Logger.
     */
    private static final Logger LOGGER = LoggerFactory.getLogger(Server.class);

    /**
     * 配置.
     */
    public static final ResourceBundle CONF = ResourceBundle.getBundle("portal");

    /**
     * 程序入口.
     *
     * @param args 指定的命令行参数，不需要传
     * @throws Exception
     */
    public static void main(final String[] args) throws Exception {
        SpringApplication.run(Server.class);
    }
}
