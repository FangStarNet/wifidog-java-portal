/*
 * Copyright (c) 2016, fangstar.com
 *
 * All rights reserved.
 */
package net.fangstar.wifi.portal.util;

import javax.servlet.http.HttpServletRequest;
import org.apache.commons.lang.StringUtils;

/**
 * HTTP 请求相关工具.
 *
 * @author <a href="http://88250.b3log.org">Liang Ding</a>
 * @version 1.0.0.0, Jun 27, 2016
 * @since 1.0.0
 */
public class Requests {

    /**
     * Gets the Internet Protocol (IP) address of the end-client that sent the specified request.
     *
     * <p>
     * It will try to get HTTP head "X-forwarded-for" or "X-Real-IP" from the last proxy to get the request first, if
     * not found, try to get it directly by {@link HttpServletRequest#getRemoteAddr()}.
     * </p>
     *
     * @param request the specified request
     * @return the IP address of the end-client sent the specified request
     */
    public static String getRemoteAddr(final HttpServletRequest request) {
        String ret = request.getHeader("X-forwarded-for");

        if (StringUtils.isBlank(ret)) {
            ret = request.getHeader("X-Real-IP");
        }

        if (StringUtils.isBlank(ret)) {
            return request.getRemoteAddr();
        }

        return ret.split(",")[0];
    }
}
