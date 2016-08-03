/*
 * Copyright (c) 2016, fangstar.com
 *
 * All rights reserved.
 */
package net.fangstar.wifi.portal.util;

import com.alibaba.druid.pool.DruidDataSourceFactory;
import java.sql.Connection;
import java.sql.SQLException;
import java.util.Properties;
import javax.sql.DataSource;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * 数据库连接工具类.
 *
 * @author <a href="http://88250.b3log.org">Liang Ding</a>
 * @version 1.0.0.0, Nov 2, 2015
 * @since 1.0.0
 */
public final class Connections {

    /**
     * Logger.
     */
    private static final Logger LOGGER = LoggerFactory.getLogger(Connections.class);

    /**
     * 数据源.
     */
    private static DataSource dataSource;

    static {
        final ClassLoader classLoader = Connections.class.getClassLoader();

        try {
            final Properties props = new Properties();
            props.load(classLoader.getResourceAsStream("db.properties"));

            dataSource = DruidDataSourceFactory.createDataSource(props);
        } catch (final Exception e) {
            LOGGER.error("Reading DB configurations failed, exit Portal", e);

            System.exit(-1);
        }
    }

    /**
     * 获取一个数据库连接.
     *
     * @return 数据源连接
     * @throws SQLException 如果获取异常
     */
    public static Connection getConnection() throws SQLException {
        return dataSource.getConnection();
    }

    /**
     * 私有的构造器.
     */
    private Connections() {
    }
}
