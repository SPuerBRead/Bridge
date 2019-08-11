package bridge.controller;


import bridge.config.DnslogConfig;
import bridge.model.WebLog;
import bridge.service.UserService;
import bridge.service.WeblogService;
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
import java.util.*;

@Controller
public class WeblogController {

    @Autowired
    private WeblogService weblogService;


    @Autowired
    private UserService userService;


    @GetMapping("/weblog")
    public ModelAndView getWeblogList() throws IOException {
        List<Map<String, Object>> weblogList = new ArrayList<Map<String, Object>>();
        SecurityContext securityContext = SecurityContextHolder.getContext();
        String username = securityContext.getAuthentication().getName();
        Integer userLogID = userService.getLogIdByName(username);
        String userDomain = String.valueOf(userLogID) + '.' + DnslogConfig.dnslogDomain;
        for (Object x : weblogService.getAllWeblog(userDomain)) {
            WebLog a = (WebLog) x;
            HashMap<String, Object> tmpMap = new HashMap<String, Object>();
            ObjectMapper objectMapper = new ObjectMapper();
            Map headerMap = objectMapper.readValue(a.getHeader(), Map.class);
            tmpMap.put("id", a.getId());
            tmpMap.put("host", a.getHost());
            tmpMap.put("ip", a.getIp());
            tmpMap.put("method", a.getMethod());
            tmpMap.put("path", a.getPath());
            tmpMap.put("header", headerMap);
            tmpMap.put("version", a.getVersion());
            if (a.getParams() == null) {
                tmpMap.put("query", "");
            } else {
                tmpMap.put("query", "?" + a.getParams());
            }
            tmpMap.put("userAgent", headerMap.get("user-agent").toString());
            String timeString = a.getTime().toString();
            tmpMap.put("time", timeString.substring(0, timeString.length() - 2));
            if (a.getData() != null) {
                tmpMap.put("data", a.getData());
            } else {
                tmpMap.put("data", "");
            }
            weblogList.add(tmpMap);
        }
        ModelMap model = new ModelMap();
        model.addAttribute("weblogList", weblogList);
        model.addAttribute("username", username);

        return new ModelAndView("weblog", model);
    }

    @ResponseBody
    @PostMapping(value = "/weblog/delete", produces = "text/plain;charset=utf-8")
    public String deleteOneWeblog(@RequestParam Map<String, String> args) throws JsonProcessingException {
        ObjectMapper mapper = new ObjectMapper();
        HashMap<String, Boolean> map = new HashMap<String, Boolean>();
        String result;
        WebLog webLog = weblogService.getWeblogByID(args.get("id"));
        SecurityContext securityContext = SecurityContextHolder.getContext();
        String username = securityContext.getAuthentication().getName();
        int userLogID = userService.getLogIdByName(username);
        if (Objects.equals(webLog.getLogid(), userLogID)) {
            weblogService.delOneWeblog(args.get("id"));
            map.put("status", true);
            result = mapper.writeValueAsString(map);
        } else {
            map.put("status", false);
            result = mapper.writeValueAsString(map);
        }
        return result;
    }

    @ResponseBody
    @GetMapping(value = "/weblog/delete_all", produces = "text/plain;charset=utf-8")
    public String deleteAllWeblog() throws JsonProcessingException {
        ObjectMapper mapper = new ObjectMapper();
        HashMap<String, Boolean> map = new HashMap<String, Boolean>();
        String result;
        SecurityContext securityContext = SecurityContextHolder.getContext();
        String username = securityContext.getAuthentication().getName();
        int userLogID = userService.getLogIdByName(username);
        try {
            weblogService.delAllWeblog(userLogID);
            map.put("status", true);
        } catch (RuntimeException r) {
            map.put("status", false);
        }
        result = mapper.writeValueAsString(map);
        return result;
    }

}
