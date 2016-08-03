/*
 * Copyright (c) 2016, fangstar.com
 *
 * All rights reserved.
 */
package net.fangstar.wifi.portal.controller;

import java.io.PrintWriter;
import java.util.Enumeration;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import javax.servlet.http.HttpSession;
import net.fangstar.wifi.portal.Server;
import org.apache.commons.lang.StringUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;

/**
 * WiFiPortal 控制器.
 *
 * @author <a href="http://88250.b3log.org">Liang Ding</a>
 * @version 1.0.0.0, Aug 1, 2016
 * @since 1.0.0
 */
@Controller
@RequestMapping("/wifidog")
public class WiFiPortalController {

    /**
     * Logger.
     */
    private static final Logger LOGGER = LoggerFactory.getLogger(WiFiPortalController.class);

    /**
     * WiFiDog 网关地址.
     */
    private static final String GATEWAY_ADDR = Server.CONF.getString("gateway.addr");

    /**
     * Test token.
     */
    private static final String TOKEN = "22";

    @RequestMapping(value = "/login", method = RequestMethod.GET)
    public void showLogin(final HttpServletRequest request, final HttpServletResponse response) {
        logReq(request);

        try (final PrintWriter writer = response.getWriter()) {
            final HttpSession session = request.getSession();
            String visitURL = (String) session.getAttribute("url");
            if (StringUtils.isBlank(visitURL)) {
                visitURL = request.getParameter("url");
            }
            session.setAttribute("url", visitURL);

            writer.write("<html>    \n"
                    + "    <head>\n"
                    + "        <meta charset=\"UTF-8\">\n"
                    + "        <title>登录 - Portal</title>\n"
                    + "    </head>\n"
                    + "    <body>\n"
                    + "        <form action=\"http://192.168.1.109:8910/wifidog/login\" method=\"POST\">\n"
                    + "            <input type=\"text\" id=\"username\" name=\"username\" placeholder=\"Username\">\n"
                    + "            <input type=\"password\" id=\"password\" name=\"password\" placeholder=\"Password\">\n"
                    + "            <button type=\"submit\">登录</button>\n"
                    + "        </form>\n"
                    + "    </body>\n"
                    + "</html>");

            writer.flush();
        } catch (final Exception e) {
            LOGGER.error("Write response failed", e);
        }
    }

    @RequestMapping(value = "/login", method = RequestMethod.POST)
    public void login(final HttpServletRequest request, final HttpServletResponse response) {
        logReq(request);

        try {
            response.sendRedirect(GATEWAY_ADDR + "/wifidog/auth?token=" + TOKEN);
        } catch (final Exception e) {
            LOGGER.error("Write response failed", e);
        }
    }

    @RequestMapping(value = "/auth", method = {RequestMethod.POST, RequestMethod.GET})
    public void auth(final HttpServletRequest request, final HttpServletResponse response) {
        logReq(request);

        try (final PrintWriter writer = response.getWriter()) {
            final String token = request.getParameter("token");
            if (TOKEN.equals(token)) {
                writer.write("Auth: 1");
            } else {
                writer.write("Auth: 0");
            }

            writer.flush();
        } catch (final Exception e) {
            LOGGER.error("Write response failed", e);
        }
    }

    @RequestMapping(value = "portal", method = {RequestMethod.POST, RequestMethod.GET})
    public void portal(final HttpServletRequest request, final HttpServletResponse response) {
        logReq(request);

        try {
            final String visitURL = (String) request.getSession().getAttribute("url");

            response.sendRedirect(visitURL);
        } catch (final Exception e) {
            LOGGER.error("Write response failed", e);
        }
    }

    @RequestMapping(value = "ping", method = {RequestMethod.POST, RequestMethod.GET})
    public void ping(final HttpServletRequest request, final HttpServletResponse response) {
        logReq(request);

        try (final PrintWriter writer = response.getWriter()) {
            writer.write("Pong");
            writer.flush();
        } catch (final Exception e) {
            LOGGER.error("Write response failed", e);
        }
    }

    private void logReq(final HttpServletRequest request) {
        final StringBuilder reqBuilder = new StringBuilder("\nrequest [\n  URI=").append(request.getRequestURI())
                .append("\n  method=").append(request.getMethod())
                .append("\n  remoteAddr=").append(request.getRemoteAddr());

        final String queryStr = request.getQueryString();
        if (StringUtils.isNotBlank(queryStr)) {
            reqBuilder.append("\n  queryStr=").append(queryStr);
        }

        final StringBuilder headerBuilder = new StringBuilder();
        final Enumeration<String> headerNames = request.getHeaderNames();
        while (headerNames.hasMoreElements()) {
            final String headerName = headerNames.nextElement();
            final String headerValue = request.getHeader(headerName);

            headerBuilder.append("    ").append(headerName).append("=").append(headerValue).append("\n");
        }
        headerBuilder.append("  ]");
        reqBuilder.append("\n  headers=[\n").append(headerBuilder.toString()).append("\n]");

        LOGGER.debug(reqBuilder.toString());
    }
}
