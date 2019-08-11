package bridge.controller;


import bridge.config.DnslogConfig;
import bridge.model.DnsRecordA;
import bridge.model.DnsRecordRebind;
import bridge.service.DnsRecordAService;
import bridge.service.DnsRecordRebindService;
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

import java.sql.Timestamp;
import java.util.*;

@Controller
public class DnsSettingController {


    @Autowired
    private UserService userService;

    @Autowired
    private DnsRecordAService dnsRecordAService;

    @Autowired
    private DnsRecordRebindService dnsRecordRebindService;

    @GetMapping("/dns_setting")
    public ModelAndView getDNSSettingList() {
        List<Map<String, String>> dnsRecordASettingList = new ArrayList<Map<String, String>>();
        List<Map<String, String>> dnsRecordRebindSettingList = new ArrayList<Map<String, String>>();
        SecurityContext securityContext = SecurityContextHolder.getContext();
        String username = securityContext.getAuthentication().getName();
        Integer userLogID = userService.getLogIdByName(username);
        String userDomain = String.valueOf(userLogID) + '.' + DnslogConfig.dnslogDomain;
        for (Object x : dnsRecordAService.getAllDnsRecordA(userDomain)) {
            DnsRecordA a = (DnsRecordA) x;
            HashMap<String, String> tmpMap = new HashMap<String, String>();
            tmpMap.put("id", a.getId());
            tmpMap.put("host", a.getSubdomain());
            tmpMap.put("subdomain", a.getSubdomain().replace('.' + String.valueOf(userLogID) + '.' + DnslogConfig.dnslogDomain, ""));
            tmpMap.put("ip", a.getIp());
            String timeString = a.getTime().toString();
            tmpMap.put("time", timeString.substring(0, timeString.length() - 2));
            dnsRecordASettingList.add(tmpMap);
        }
        for (Object x : dnsRecordRebindService.getAllDnsRecordRebind(userDomain)) {
            DnsRecordRebind a = (DnsRecordRebind) x;
            HashMap<String, String> tmpMap = new HashMap<String, String>();
            tmpMap.put("id", a.getId());
            tmpMap.put("host", a.getSubdomain());
            tmpMap.put("subdomain", a.getSubdomain().replace('.' + String.valueOf(userLogID) + '.' + DnslogConfig.dnslogDomain, ""));
            tmpMap.put("ip1", a.getIp1());
            tmpMap.put("ip2", a.getIp2());
            String timeString = a.getTime().toString();
            tmpMap.put("time", timeString.substring(0, timeString.length() - 2));
            dnsRecordRebindSettingList.add(tmpMap);
        }
        ModelMap model = new ModelMap();
        model.addAttribute("dnsRecordASettingList", dnsRecordASettingList);
        model.addAttribute("dnsRecordRebindSettingList", dnsRecordRebindSettingList);
        model.addAttribute("username", username);

        return new ModelAndView("dnssetting", model);
    }

    @ResponseBody
    @PostMapping(value = "/dns_setting/add/a", produces = "text/plain;charset=utf-8")
    public String addRecordA(@RequestParam Map<String, String> args) throws JsonProcessingException {
        String subDomainA = args.get("subDomainA");
        String destIP = args.get("destIP");
        ObjectMapper mapper = new ObjectMapper();
        HashMap<String, Object> map = new HashMap<String, Object>();
        String result;
        DnsRecordA dnsRecordA = new DnsRecordA();
        SecurityContext securityContext = SecurityContextHolder.getContext();
        String username = securityContext.getAuthentication().getName();
        int userLogID = userService.getLogIdByName(username);

        subDomainA = subDomainA + '.' + String.valueOf(userLogID) + '.' + DnslogConfig.dnslogDomain;
        if (dnsRecordAService.getDnsRecordABySubdomain(subDomainA) == null && dnsRecordRebindService.getDnsRecordRebindBySubdomain(subDomainA) == null) {
            dnsRecordA.setSubdomain(subDomainA);
            dnsRecordA.setIp(destIP);
            dnsRecordA.setTime(new Timestamp(System.currentTimeMillis()));
            dnsRecordA.setId(UUID.randomUUID().toString());
            dnsRecordA.setLogid(userLogID);

            dnsRecordAService.addDnsRecordA(dnsRecordA);
            map.put("status", true);
            result = mapper.writeValueAsString(map);
            return result;
        } else {
            map.put("status", false);
            map.put("message", "subdomain is already existed");
            result = mapper.writeValueAsString(map);
            return result;
        }
    }

    @ResponseBody
    @PostMapping(value = "/dns_setting/add/rebind", produces = "text/plain;charset=utf-8")
    public String addRecordRebind(@RequestParam Map<String, String> args) throws JsonProcessingException {
        String subDomainRebind = args.get("subDomainRebind");
        String destIPRebind1 = args.get("destIPRebind1");
        String destIPRebind2 = args.get("destIPRebind2");
        ObjectMapper mapper = new ObjectMapper();
        HashMap<String, Object> map = new HashMap<String, Object>();
        String result;
        DnsRecordRebind dnsRecordRebind = new DnsRecordRebind();
        SecurityContext securityContext = SecurityContextHolder.getContext();
        String username = securityContext.getAuthentication().getName();
        int userLogID = userService.getLogIdByName(username);

        subDomainRebind = subDomainRebind + '.' + String.valueOf(userLogID) + '.' + DnslogConfig.dnslogDomain;
        if (dnsRecordAService.getDnsRecordABySubdomain(subDomainRebind) == null && dnsRecordRebindService.getDnsRecordRebindBySubdomain(subDomainRebind) == null) {
            dnsRecordRebind.setSubdomain(subDomainRebind);
            dnsRecordRebind.setIp1(destIPRebind1);
            dnsRecordRebind.setIp2(destIPRebind2);
            dnsRecordRebind.setTime(new Timestamp(System.currentTimeMillis()));
            dnsRecordRebind.setId(UUID.randomUUID().toString());
            dnsRecordRebind.setLogid(userLogID);

            dnsRecordRebindService.addDnsRecordRebind(dnsRecordRebind);
            map.put("status", true);
            result = mapper.writeValueAsString(map);
            return result;
        } else {
            map.put("status", false);
            map.put("message", "subdomain is already existed");
            result = mapper.writeValueAsString(map);
            return result;
        }
    }

    @ResponseBody
    @PostMapping(value = "/dns_setting/delete/a", produces = "text/plain;charset=utf-8")
    public String deleteOneDnsRecordASetting(@RequestParam Map<String, String> args) throws JsonProcessingException {
        ObjectMapper mapper = new ObjectMapper();
        HashMap<String, Boolean> map = new HashMap<String, Boolean>();
        String result;
        DnsRecordA dnsRecordA = dnsRecordAService.getDnsRecordAByID(args.get("id"));
        SecurityContext securityContext = SecurityContextHolder.getContext();
        String username = securityContext.getAuthentication().getName();
        int userLogID = userService.getLogIdByName(username);
        if (Objects.equals(dnsRecordA.getLogid(), userLogID)) {
            dnsRecordAService.delOneDnsRecordA(args.get("id"));
            map.put("status", true);
            result = mapper.writeValueAsString(map);
        } else {
            map.put("status", false);
            result = mapper.writeValueAsString(map);
        }
        return result;
    }

    @ResponseBody
    @PostMapping(value = "/dns_setting/delete/rebind", produces = "text/plain;charset=utf-8")
    public String deleteOneDnsRecordRebindSetting(@RequestParam Map<String, String> args) throws JsonProcessingException {
        ObjectMapper mapper = new ObjectMapper();
        HashMap<String, Boolean> map = new HashMap<String, Boolean>();
        String result;
        DnsRecordRebind dnsRecordRebind = dnsRecordRebindService.getDnsRecordRebindByID(args.get("id"));
        SecurityContext securityContext = SecurityContextHolder.getContext();
        String username = securityContext.getAuthentication().getName();
        int userLogID = userService.getLogIdByName(username);
        if (Objects.equals(dnsRecordRebind.getLogid(), userLogID)) {
            dnsRecordRebindService.delOneDnsRecordRebind(args.get("id"));
            map.put("status", true);
            result = mapper.writeValueAsString(map);
        } else {
            map.put("status", false);
            result = mapper.writeValueAsString(map);
        }
        return result;
    }

    @ResponseBody
    @GetMapping(value = "/dns_setting/delete_all/a", produces = "text/plain;charset=utf-8")
    public String deleteAllDnsRecordA() throws JsonProcessingException {
        ObjectMapper mapper = new ObjectMapper();
        HashMap<String, Boolean> map = new HashMap<String, Boolean>();
        String result;
        SecurityContext securityContext = SecurityContextHolder.getContext();
        String username = securityContext.getAuthentication().getName();
        int userLogID = userService.getLogIdByName(username);
        try {
            dnsRecordAService.delAllDnsRecordA(userLogID);
            map.put("status", true);
        } catch (RuntimeException r) {
            map.put("status", false);
        }
        result = mapper.writeValueAsString(map);
        return result;
    }

    @ResponseBody
    @GetMapping(value = "/dns_setting/delete_all/rebind", produces = "text/plain;charset=utf-8")
    public String deleteAllDnsRecordRebind() throws JsonProcessingException {
        ObjectMapper mapper = new ObjectMapper();
        HashMap<String, Boolean> map = new HashMap<String, Boolean>();
        String result;
        SecurityContext securityContext = SecurityContextHolder.getContext();
        String username = securityContext.getAuthentication().getName();
        int userLogID = userService.getLogIdByName(username);
        try {
            dnsRecordRebindService.delAllDnsRecordRebind(userLogID);
            map.put("status", true);
        } catch (RuntimeException r) {
            map.put("status", false);
        }
        result = mapper.writeValueAsString(map);
        return result;
    }

    @ResponseBody
    @PostMapping(value = "/dns_setting/edit/a", produces = "text/plain;charset=utf-8")
    public String editRecordA(@RequestParam Map<String, String> args) throws JsonProcessingException {
        String subDomainA = args.get("subDomainA");
        String destIP = args.get("destIP");
        String id = args.get("id");
        ObjectMapper mapper = new ObjectMapper();
        HashMap<String, Object> map = new HashMap<String, Object>();
        String result;
        DnsRecordA dnsRecordA = new DnsRecordA();
        SecurityContext securityContext = SecurityContextHolder.getContext();
        String username = securityContext.getAuthentication().getName();
        int userLogID = userService.getLogIdByName(username);
        DnsRecordA dnsRecordAByID = dnsRecordAService.getDnsRecordAByID(id);
        subDomainA = subDomainA + '.' + String.valueOf(userLogID) + '.' + DnslogConfig.dnslogDomain;
        if (dnsRecordAService.getDnsRecordABySubdomain(subDomainA) == null && dnsRecordRebindService.getDnsRecordRebindBySubdomain(subDomainA) == null || dnsRecordAByID.getSubdomain().equals(subDomainA)) {
            if (dnsRecordAByID != null && dnsRecordAByID.getLogid() == userLogID) {
                dnsRecordA.setSubdomain(subDomainA);
                dnsRecordA.setIp(destIP);
                dnsRecordA.setTime(new Timestamp(System.currentTimeMillis()));
                dnsRecordA.setId(id);
                dnsRecordAService.updateDnsRecordAByID(dnsRecordA);
                map.put("status", true);
                result = mapper.writeValueAsString(map);
                return result;
            } else {
                map.put("status", false);
                map.put("message", "记录ID不存在或没有更新权限，更新失败");
                result = mapper.writeValueAsString(map);
                return result;
            }
        } else {
            map.put("status", false);
            map.put("message", "新更新的子域名与现有子域名设置重复，更新失败");
            result = mapper.writeValueAsString(map);
            return result;
        }
    }

    @ResponseBody
    @PostMapping(value = "/dns_setting/edit/rebind", produces = "text/plain;charset=utf-8")
    public String editRecordRebind(@RequestParam Map<String, String> args) throws JsonProcessingException {
        String subDomainRebind = args.get("subDomainRebind");
        String destIPRebind1 = args.get("destIPRebind1");
        String destIPRebind2 = args.get("destIPRebind2");
        String id = args.get("id");
        ObjectMapper mapper = new ObjectMapper();
        HashMap<String, Object> map = new HashMap<String, Object>();
        String result;
        DnsRecordRebind dnsRecordRebind = new DnsRecordRebind();
        SecurityContext securityContext = SecurityContextHolder.getContext();
        String username = securityContext.getAuthentication().getName();
        int userLogID = userService.getLogIdByName(username);
        DnsRecordRebind dnsRecordRebindByID = dnsRecordRebindService.getDnsRecordRebindByID(id);
        subDomainRebind = subDomainRebind + '.' + String.valueOf(userLogID) + '.' + DnslogConfig.dnslogDomain;
        if (dnsRecordAService.getDnsRecordABySubdomain(subDomainRebind) == null && dnsRecordRebindService.getDnsRecordRebindBySubdomain(subDomainRebind) == null || dnsRecordRebindByID.getSubdomain().equals(subDomainRebind) ) {
            if (dnsRecordRebindByID != null && dnsRecordRebindByID.getLogid() == userLogID) {
                dnsRecordRebind.setSubdomain(subDomainRebind);
                dnsRecordRebind.setIp1(destIPRebind1);
                dnsRecordRebind.setIp2(destIPRebind2);
                dnsRecordRebind.setTime(new Timestamp(System.currentTimeMillis()));
                dnsRecordRebind.setId(id);
                dnsRecordRebindService.updateDnsRecordRebindByID(dnsRecordRebind);
                map.put("status", true);
                result = mapper.writeValueAsString(map);
                return result;
            } else {
                map.put("status", false);
                map.put("message", "记录ID不存在或没有更新权限，更新失败");
                result = mapper.writeValueAsString(map);
                return result;
            }
        } else {
            map.put("status", false);
            map.put("message", "新更新的子域名与现有子域名设置重复，更新失败");
            result = mapper.writeValueAsString(map);
            return result;
        }
    }
}
