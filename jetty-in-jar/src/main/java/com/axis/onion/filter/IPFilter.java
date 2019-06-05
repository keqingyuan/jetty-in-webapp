package com.axis.onion.filter;

import com.axis.onion.exception.BusinessException;
import com.axis.onion.filter.annotation.IPFilterConfigure;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import javax.servlet.http.HttpServletRequest;
import javax.ws.rs.container.ContainerRequestContext;
import javax.ws.rs.container.ContainerRequestFilter;
import javax.ws.rs.container.ResourceInfo;
import javax.ws.rs.core.Context;
import javax.ws.rs.core.Response;
import javax.ws.rs.ext.Provider;
import java.lang.reflect.Method;

/**
 * Created by Ke Qingyuan on 2019-06-05.
 */
@Provider
@IPFilterConfigure
public class IPFilter implements ContainerRequestFilter {

    private static final Logger LOG = LoggerFactory.getLogger(IPFilter.class);

    @Context
    HttpServletRequest httpServletRequest;

    @Context
    ResourceInfo resourceInfo;

    @Override
    public void filter(ContainerRequestContext containerRequestContext) {
        LOG.info("In the IP interceptor");
        // 拿到客户端IP
        String clientIp = this.getIPAddress(httpServletRequest);
        LOG.info("client ip is {}", clientIp);
        Class<?> clazz = resourceInfo.getResourceClass();
        IPFilterConfigure annotationClass = clazz.getAnnotation(IPFilterConfigure.class);
        if (annotationClass != null) {
            LOG.info("ClassAnnotation={}", annotationClass);
            String[] ips = annotationClass.value();
            for (String ip : ips) {
                if (clientIp.startsWith(ip)) {
                    return;
                }
            }
        }
        Method method = resourceInfo.getResourceMethod();
        IPFilterConfigure annotationMethod = method.getAnnotation(IPFilterConfigure.class);
        if (annotationMethod != null) {
            LOG.info("MethodAnnotation={}", annotationMethod);
            String[] ips = annotationMethod.value();
            for (String ip : ips) {
                if (clientIp.startsWith(ip)) {
                    return;
                }
            }
        }
        throw new BusinessException(Response.Status.NOT_ACCEPTABLE, "0001",clientIp);
    }

    private String getIPAddress(HttpServletRequest request) {
        String ip = request.getHeader("x-forwarded-for");
        if (ip == null || ip.length() == 0 || "unknown".equalsIgnoreCase(ip)) {
            ip = request.getHeader("Proxy-Client-IP");
        }
        if (ip == null || ip.length() == 0 || "unknown".equalsIgnoreCase(ip)) {
            ip = request.getHeader("WL-Proxy-Client-IP");
        }
        if (ip == null || ip.length() == 0 || "unknown".equalsIgnoreCase(ip)) {
            ip = request.getHeader("HTTP_CLIENT_IP");
        }
        if (ip == null || ip.length() == 0 || "unknown".equalsIgnoreCase(ip)) {
            ip = request.getHeader("HTTP_X_FORWARDED_FOR");
        }
        if (ip == null || ip.length() == 0 || "unknown".equalsIgnoreCase(ip)) {
            ip = request.getRemoteAddr();
        }
        return ip;
    }
}
