package bridge.controller;


import bridge.config.DnslogConfig;
import bridge.model.DnsLog;
import bridge.model.User;
import bridge.model.WebLog;
import bridge.service.DnsLogService;
import bridge.service.UserService;
import bridge.service.WeblogService;
import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.ResponseBody;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

@Controller
public class APIController {

    @Autowired
    private UserService userService;

    @Autowired
    private DnsLogService dnsLogService;

    @Autowired
    private WeblogService weblogService;

    @ResponseBody
    @GetMapping(value = "/api/dnslog/search", produces = "text/plain;charset=utf-8")
    public String dnslogSearchAPI(@RequestParam Map<String, String> args) throws JsonProcessingException {
        ObjectMapper mapper = new ObjectMapper();
        List<Map<String,Object>> logList = new ArrayList<>();
        String token = args.get("token");
        User user = userService.getUserByApiKey(token);
        if(user != null){
            String keyword = args.get("keyword");
            int logID = user.getLogid();
            List dnslogList = dnsLogService.getDnsLogByHost(keyword+'.'+String.valueOf(logID)+'.'+DnslogConfig.dnslogDomain);
            for(Object x:dnslogList){
                HashMap<String, Object> map = new HashMap<String, Object>();
                map.put("host", ((DnsLog)x).getHost());
                map.put("type", ((DnsLog)x).getType());
                map.put("ip", ((DnsLog)x).getIp());
                map.put("time", ((DnsLog)x).getTime().toString());
                logList.add(map);
            }
            return mapper.writeValueAsString(logList);
        }else{
            return mapper.writeValueAsString("[]");
        }
    }

    @ResponseBody
    @GetMapping(value = "/api/weblog/search", produces = "text/plain;charset=utf-8")
    public String weblogSearchAPI(@RequestParam Map<String, String> args) throws JsonProcessingException{
        ObjectMapper mapper = new ObjectMapper();
        List<Map<String,Object>> logList = new ArrayList<>();
        String token = args.get("token");
        User user = userService.getUserByApiKey(token);
        if(user != null){
            String keyword = args.get("keyword");
            int logID = user.getLogid();
            List weblogList = weblogService.getWeblogByHost(keyword+'.'+String.valueOf(logID)+'.'+DnslogConfig.dnslogDomain);
            System.out.println(weblogList);
            for(Object x:weblogList){
                WebLog a = (WebLog) x;
                HashMap<String, Object> map = new HashMap<String, Object>();
                map.put("host", a.getHost());
                map.put("method", a.getMethod());
                map.put("ip", a.getIp());
                map.put("version", a.getVersion());
                map.put("path", a.getPath());
                map.put("header", a.getHeader());
                map.put("params", a.getParams());
                map.put("data", a.getData());
                map.put("time", a.getTime().toString());
                logList.add(map);
            }
            return mapper.writeValueAsString(logList);
        }else{
            return mapper.writeValueAsString("[]");
        }
    }
}
