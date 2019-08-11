package bridge.controller;

import bridge.config.DnslogConfig;
import bridge.model.DnsLog;
import bridge.service.DnsLogService;
import bridge.service.UserService;
import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.core.context.SecurityContext;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Controller;
import org.springframework.ui.ModelMap;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.servlet.ModelAndView;

import java.util.*;

@Controller
public class DnslogController {


    @Autowired
    private UserService userService;

    @Autowired
    private DnsLogService dnsLogService;


    @GetMapping("/dnslog")
    public ModelAndView getDnslogList() {
        List<Map<String, String>> dnslogList = new ArrayList<Map<String, String>>();
        SecurityContext securityContext = SecurityContextHolder.getContext();
        String username = securityContext.getAuthentication().getName();
        Integer userLogID = userService.getLogIdByName(username);
        String userDomain = String.valueOf(userLogID) + '.' + DnslogConfig.dnslogDomain;
        for (Object x : dnsLogService.getAllDnslog(userDomain)) {
            DnsLog a = (DnsLog) x;
            HashMap<String, String> tmpMap = new HashMap<String, String>();
            tmpMap.put("id", a.getId());
            tmpMap.put("host", a.getHost());
            tmpMap.put("ip", a.getIp());
            tmpMap.put("type", a.getType());
            String timeString = a.getTime().toString();
            tmpMap.put("time", timeString.substring(0, timeString.length() - 2));
            dnslogList.add(tmpMap);
        }
        ModelMap model = new ModelMap();
        model.addAttribute("dnslogList", dnslogList);
        model.addAttribute("username", username);

        return new ModelAndView("dnslog", model);
    }

    @ResponseBody
    @PostMapping(value = "/dnslog/delete", produces = "text/plain;charset=utf-8")
    public String deleteOneDnslog(@RequestParam Map<String, String> args) throws JsonProcessingException {
        ObjectMapper mapper = new ObjectMapper();
        HashMap<String, Boolean> map = new HashMap<String, Boolean>();
        String result;
        DnsLog dnslog = dnsLogService.getDnslogByID(args.get("id"));
        SecurityContext securityContext = SecurityContextHolder.getContext();
        String username = securityContext.getAuthentication().getName();
        int userLogID = userService.getLogIdByName(username);
        if (Objects.equals(dnslog.getLogid(), userLogID)) {
            dnsLogService.delOneDnslog(args.get("id"));
            map.put("status", true);
            result = mapper.writeValueAsString(map);
        } else {
            map.put("status", false);
            result = mapper.writeValueAsString(map);
        }
        return result;
    }

    @ResponseBody
    @GetMapping(value = "/dnslog/delete_all", produces = "text/plain;charset=utf-8")
    public String deleteAllDnslog() throws JsonProcessingException {
        ObjectMapper mapper = new ObjectMapper();
        HashMap<String, Boolean> map = new HashMap<String, Boolean>();
        String result;
        SecurityContext securityContext = SecurityContextHolder.getContext();
        String username = securityContext.getAuthentication().getName();
        int userLogID = userService.getLogIdByName(username);
        try {
            dnsLogService.delAllDnslog(userLogID);
            map.put("status", true);
        } catch (RuntimeException r) {
            map.put("status", false);
        }
        result = mapper.writeValueAsString(map);
        return result;
    }
}
