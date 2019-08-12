package bridge.controller;

import bridge.config.DnslogConfig;
import bridge.service.UserService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.core.context.SecurityContext;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Controller;
import org.springframework.ui.ModelMap;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.servlet.ModelAndView;

import java.util.HashMap;
import java.util.Map;

@Controller
public class APIInfoController {

    @Autowired
    private UserService userService;


    @GetMapping("/api_info")
    public ModelAndView api() {
        SecurityContext securityContext = SecurityContextHolder.getContext();
        String username = securityContext.getAuthentication().getName();
        String apiKey = userService.getapiKeyByName(username);
        int logID = userService.getLogIdByName(username);
        Map<String, String> map = new HashMap<>();
        map.put("apiKey", apiKey);
        map.put("logAddress", String.valueOf(logID) + '.' + DnslogConfig.dnslogDomain);
        ModelMap model = new ModelMap();
        model.addAttribute("apimap", map);
        model.addAttribute("username", username);
        return new ModelAndView("apiinfo", model);
    }

}
