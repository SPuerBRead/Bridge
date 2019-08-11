package bridge.controller;


import bridge.config.DnslogConfig;
import bridge.model.Response;
import bridge.service.ResponseService;
import bridge.service.UserService;
import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.core.context.SecurityContext;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Controller;
import org.springframework.ui.ModelMap;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.ResponseBody;
import org.springframework.web.servlet.ModelAndView;

import java.io.IOException;
import java.sql.Timestamp;
import java.util.*;

@Controller
public class ResponseSettingController {

    @Autowired
    private UserService userService;

    @Autowired
    private ResponseService responseService;


    @GetMapping("/response_setting")
    public ModelAndView getResponseSetting() throws JsonProcessingException, IOException {
        ObjectMapper mapper = new ObjectMapper();
        List<Map<String, Object>> responseList = new ArrayList<Map<String, Object>>();
        SecurityContext securityContext = SecurityContextHolder.getContext();
        String username = securityContext.getAuthentication().getName();
        Integer userLogID = userService.getLogIdByName(username);
        String userDomain = String.valueOf(userLogID) + '.' + DnslogConfig.dnslogDomain;
        for (Object x : responseService.getAllResponse(userDomain)) {
            Response a = (Response) x;
            HashMap<String, Object> tmpMap = new HashMap<String, Object>();
            tmpMap.put("id", a.getId());
            tmpMap.put("host", a.getSubDomain());
            tmpMap.put("subDomain", a.getSubDomain().replace('.'+String.valueOf(userLogID) + '.' + DnslogConfig.dnslogDomain,""));
            tmpMap.put("responseType", a.getResponseType());
            tmpMap.put("statusCode", String.valueOf(a.getStatusCode()));
            if(a.getResponseBody() == null || a.getResponseBody().equals("")){
                tmpMap.put("responseBody","");
            }else{
                tmpMap.put("responseBody",a.getResponseBody());
            }
            if(a.getHeaders() == null){
                tmpMap.put("headers","[]");
            }else{
                ArrayList headerList = mapper.readValue(a.getHeaders(),ArrayList.class);
                tmpMap.put("headers",headerList);
            }if(a.getRedirectURL() == null){
                tmpMap.put("redirectURL","");
            }else{
                tmpMap.put("redirectURL",a.getRedirectURL());
            }
            String timeString = a.getTime().toString();
            tmpMap.put("time", timeString.substring(0, timeString.length() - 2));
            responseList.add(tmpMap);
        }
        ModelMap model = new ModelMap();
        model.addAttribute("responseList", responseList);
        model.addAttribute("username", username);
        return new ModelAndView("responsesetting", model);
    }

    @ResponseBody
    @PostMapping(value = "/response_setting/add", produces = "text/plain;charset=utf-8")
    public String addResponseSetting(@RequestParam Map<String, String> args) throws JsonProcessingException {
        String subDomain = args.get("subDomain");
        String responseType = args.get("responseType");
        String statusCode = args.get("statusCode");
        String responseBody = args.get("responseBody");
        String redirectURL = args.get("redirectURL");
        String headers = args.get("headers");
        ObjectMapper mapper = new ObjectMapper();
        HashMap<String, Object> map = new HashMap<String, Object>();
        String result;

        Response response = new Response();
        SecurityContext securityContext = SecurityContextHolder.getContext();
        String username = securityContext.getAuthentication().getName();
        int userLogID = userService.getLogIdByName(username);
        subDomain = subDomain + '.' + String.valueOf(userLogID) + '.' + DnslogConfig.dnslogDomain;

        if (responseService.getResponseBySubdomain(subDomain) == null){
            response.setSubDomain(subDomain);
            response.setResponseType(responseType);

            SetResponse(responseType, statusCode, responseBody, redirectURL, response);

            response.setHeaders(headers);
            response.setTime(new Timestamp(System.currentTimeMillis()));
            response.setId(UUID.randomUUID().toString());
            response.setLogid(userLogID);

            responseService.addResponse(response);
            map.put("status", true);
            result = mapper.writeValueAsString(map);
            return result;
        }else{
            map.put("status", false);
            map.put("message", "subdomain is already existed");
            result = mapper.writeValueAsString(map);
            return result;
        }
    }


    @ResponseBody
    @PostMapping(value = "/response_setting/edit", produces = "text/plain;charset=utf-8")
    public String editResponseSetting(@RequestParam Map<String, String> args) throws JsonProcessingException {
        String subDomain = args.get("subDomain");
        String responseType = args.get("responseType");
        String statusCode = args.get("statusCode");
        String responseBody = args.get("responseBody");
        String redirectURL = args.get("redirectURL");
        String headers = args.get("headers");
        String id = args.get("id");
        ObjectMapper mapper = new ObjectMapper();
        HashMap<String, Object> map = new HashMap<String, Object>();
        String result;


        Response response = new Response();
        SecurityContext securityContext = SecurityContextHolder.getContext();
        String username = securityContext.getAuthentication().getName();
        int userLogID = userService.getLogIdByName(username);
        subDomain = subDomain + '.' + String.valueOf(userLogID) + '.' + DnslogConfig.dnslogDomain;

        Response selectResponse = responseService.getResponseByID(id);
        if(selectResponse!=null && selectResponse.getLogid() == userLogID){
            if (responseService.getResponseBySubdomain(subDomain) == null || selectResponse.getSubDomain().equals(subDomain)){
                response.setSubDomain(subDomain);
                response.setResponseType(responseType);

                SetResponse(responseType, statusCode, responseBody, redirectURL, response);

                response.setHeaders(headers);
                response.setTime(new Timestamp(System.currentTimeMillis()));
                response.setId(id);


                responseService.updateResponseAByID(response);
                map.put("status", true);
                result = mapper.writeValueAsString(map);
                return result;
            }else {
                    map.put("status", false);
                    map.put("message", "新更新的子域名与现有子域名设置重复，更新失败");
                    result = mapper.writeValueAsString(map);
                    return result;
            }
        }else{
            map.put("status", false);
            map.put("message", "记录ID不存在或没有更新权限，更新失败");
            result = mapper.writeValueAsString(map);
            return result;
        }


    }

    @ResponseBody
    @PostMapping(value = "/response_setting/delete", produces = "text/plain;charset=utf-8")
    public String deleteOneDResponseSetting(@RequestParam Map<String, String> args) throws JsonProcessingException {
        ObjectMapper mapper = new ObjectMapper();
        HashMap<String, Boolean> map = new HashMap<String, Boolean>();
        String result;
        Response response = responseService.getResponseByID(args.get("id"));
        SecurityContext securityContext = SecurityContextHolder.getContext();
        String username = securityContext.getAuthentication().getName();
        int userLogID = userService.getLogIdByName(username);
        if (Objects.equals(response.getLogid(), userLogID)) {
            responseService.delOneResponse(args.get("id"));
            map.put("status", true);
            result = mapper.writeValueAsString(map);
        } else {
            map.put("status", false);
            result = mapper.writeValueAsString(map);
        }
        return result;
    }

    @ResponseBody
    @GetMapping(value = "/response_setting/delete_all", produces = "text/plain;charset=utf-8")
    public String deleteAllResponse() throws JsonProcessingException {
        ObjectMapper mapper = new ObjectMapper();
        HashMap<String, Boolean> map = new HashMap<String, Boolean>();
        String result;
        SecurityContext securityContext = SecurityContextHolder.getContext();
        String username = securityContext.getAuthentication().getName();
        int userLogID = userService.getLogIdByName(username);
        try {
            responseService.delAllResponse(userLogID);
            map.put("status", true);
        } catch (RuntimeException r) {
            map.put("status", false);
        }
        result = mapper.writeValueAsString(map);
        return result;
    }

    private void SetResponse(String responseType, String statusCode, String responseBody, String redirectURL, Response response) {
        if(responseType.equals("Data")){
            response.setStatusCode(Integer.parseInt(statusCode));
            response.setResponseBody(responseBody);
            response.setRedirectURL("");
        }else if(responseType.equals("Error")){
            response.setStatusCode(Integer.parseInt(statusCode));
            response.setResponseBody("");
            response.setRedirectURL("");
        }else if(responseType.equals("Redirect")){
            response.setRedirectURL(redirectURL);
            response.setStatusCode(302);
            response.setResponseBody("");
        }
    }
}