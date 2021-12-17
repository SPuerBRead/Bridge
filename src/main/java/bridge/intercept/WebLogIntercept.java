package bridge.intercept;

import bridge.config.DnslogConfig;
import bridge.model.Response;
import bridge.model.WebLog;
import bridge.service.ResponseService;
import bridge.service.WeblogService;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.apache.tomcat.util.buf.MessageBytes;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;
import org.springframework.web.servlet.HandlerInterceptor;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import java.io.BufferedReader;
import java.io.IOException;
import java.io.PrintWriter;
import java.lang.reflect.Field;
import java.sql.Timestamp;
import java.util.*;

@Component
public class WebLogIntercept implements HandlerInterceptor {

    @Autowired
    private WebLog webLog;


    @Autowired
    private WeblogService weblogService;

    @Autowired
    private ResponseService responseService;

    @Override
    public boolean preHandle(HttpServletRequest request, HttpServletResponse response, Object handler) throws Exception {
        String host = request.getHeader("host").replaceAll(":(.*)", "").trim();
        Integer logID;
        if (host.equals(DnslogConfig.managerDomain)) {
            return true;
        } else {
            ObjectMapper mapper = new ObjectMapper();
            PrintWriter writer = response.getWriter();
            response.setCharacterEncoding("utf-8");
            response.setContentType("application/json; charset=utf-8");
            HashMap<String, String> map = new HashMap<String, String>();
            String[] hd = host.replace('.' + DnslogConfig.dnslogDomain, "").split("\\.");
            try {
                logID = Integer.parseInt(hd[hd.length - 1]);
            } catch (NumberFormatException n) {
                map.put("status", "error");
                map.put("message", "request host no logid found");
                writer.write(mapper.writeValueAsString(map));
                return false;
            }
            if (!host.endsWith(DnslogConfig.dnslogDomain)) {
                map.put("status", "error");
                map.put("message", "request host no logid found");
                writer.write(mapper.writeValueAsString(map));
                return false;
            }
            webLog.setLogid(logID);
            webLog.setHost(host);
            webLog.setTime(new Timestamp(System.currentTimeMillis()));
            webLog.setId(UUID.randomUUID().toString());
            webLog.setMethod(request.getMethod());
            webLog.setParams(request.getQueryString());

            Object a = findCoyoteRequest(request);
            Field coyoteRequest = a.getClass().getDeclaredField("coyoteRequest");
            coyoteRequest.setAccessible(true);
            Object b = coyoteRequest.get(a);

            Field uriMB = b.getClass().getDeclaredField("uriMB");
            uriMB.setAccessible(true);
            MessageBytes c = (MessageBytes) uriMB.get(b);
            webLog.setPath(c.getString());


            HashMap<String, String> headersMap = new HashMap<String, String>();
            Enumeration<String> e = request.getHeaderNames();
            while (e.hasMoreElements()) {
                String headerName = e.nextElement();
                Enumeration<String> headerValues = request.getHeaders(headerName);
                while (headerValues.hasMoreElements()) {
                    headersMap.put(headerName, headerValues.nextElement());
                }
            }
            webLog.setHeader(mapper.writeValueAsString(headersMap));
            webLog.setData(getBodyData(request));
            webLog.setIp(getIPAddress(request));
            webLog.setVersion(request.getProtocol());
            weblogService.addWeblog(webLog);
            if(responseService.getResponseBySubdomain(host) != null){
                Response responseData = responseService.getResponseBySubdomain(host);
                if(responseData.getResponseType().equals("Data")){
                    String responseBody = responseData.getResponseBody();
                    int statusCode = responseData.getStatusCode();
                    ArrayList headerList = mapper.readValue(responseData.getHeaders(),ArrayList.class);
                    writer.write(responseBody);
                    response.setStatus(statusCode);
                    if(headerList.size() > 0){
                        for(Object x:headerList){
                            String[] keyValue = ((String) x).split(":");
                            response.setHeader(keyValue[0],keyValue[1]);
                        }
                    }
                }else if(responseData.getResponseType().equals("Redirect")){
                    String redirectURL = responseData.getRedirectURL();
                    response.setStatus(responseData.getStatusCode());
                    response.setHeader("Location", redirectURL);
                    ArrayList headerList = mapper.readValue(responseData.getHeaders(),ArrayList.class);
                    if(headerList.size() > 0){
                        for(Object x:headerList){
                            String[] keyValue = ((String) x).split(":");
                            response.setHeader(keyValue[0],keyValue[1]);
                        }
                    }
                }else if(responseData.getResponseType().equals("Error")){
                    ArrayList headerList = mapper.readValue(responseData.getHeaders(),ArrayList.class);
                    if(headerList.size() > 0){
                        for(Object x:headerList){
                            String[] keyValue = ((String) x).split(":");
                            response.setHeader(keyValue[0],keyValue[1]);
                        }
                    }
                    response.sendError(responseData.getStatusCode());
                }else{
                    map.put("status", "success");
                    map.put("message", "response type in database error");
                    writer.write(mapper.writeValueAsString(map));
                }
            }else{
                map.put("status", "success");
                map.put("message", "ok");
                writer.write(mapper.writeValueAsString(map));
            }
            return false;
        }
    }

    private String getBodyData(HttpServletRequest request) {
        StringBuffer data = new StringBuffer();
        String line = null;
        BufferedReader reader = null;
        try {
            reader = request.getReader();
            while (null != (line = reader.readLine()))
                data.append(line);
        } catch (IOException e) {
        } finally {
        }
        return data.toString();
    }

    private String getIPAddress(HttpServletRequest request) {
        String ipAddress = request.getHeader("X-FORWARDED-FOR");
        if (ipAddress == null) {
            ipAddress = request.getRemoteAddr();
        }
        return ipAddress;
    }


    private Class getClassByName(Class classObject, String name) {
        Map<Class, List<Field>> fieldMap = new HashMap<>();
        Class returnClass = null;
        Class tempClass = classObject;
        while (tempClass != null) {
            fieldMap.put(tempClass, Arrays.asList(tempClass.getDeclaredFields()));
            tempClass = tempClass.getSuperclass();
        }

        for (Map.Entry<Class, List<Field>> entry : fieldMap.entrySet()) {
            for (Field f : entry.getValue()) {
                if (f.getName().equals(name)) {
                    returnClass = entry.getKey();
                    break;
                }
            }
        }
        return returnClass;
    }

    private Object findCoyoteRequest(Object request) throws Exception {
        Class a = getClassByName(request.getClass(), "request");
        Field request1 = a.getDeclaredField("request");
        request1.setAccessible(true);
        Object b = request1.get(request);
        if (getClassByName(b.getClass(), "coyoteRequest") == null) {
            return findCoyoteRequest(b);
        } else {
            return b;
        }
    }
}
