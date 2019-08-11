package bridge.controller;

import bridge.config.DnslogConfig;
import bridge.model.User;
import bridge.service.UserService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.core.context.SecurityContext;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestParam;

import java.util.Map;

@Controller
public class AuthController {

    @Autowired
    private UserService userService;

    @GetMapping("/login")
    public String login() {
        return "login";
    }

    @GetMapping("/")
    public String index() {
        SecurityContext securityContext = SecurityContextHolder.getContext();
        String username = securityContext.getAuthentication().getName();
        if (username.equals("anonymousUser")) {
            return "login";
        } else {
            return "redirect:dnslog";
        }
    }


    @GetMapping("/register")
    public String register() {
        return "register";
    }


    @PostMapping("/register")
    public String register(@RequestParam Map<String, String> args) {
        if(DnslogConfig.signal.equals(args.get("signal"))){
            if (args.get("password1").equals(args.get("password2"))) {
                User user = new User();
                user.setPassword(args.get("password1"));
                user.setUsername(args.get("username"));
                try {
                    userService.insertNewUser(user);
                } catch (RuntimeException runtimeException) {
                    return "redirect:register?error1";
                }
            } else {
                return "redirect:register?error2";
            }
        }else{
            return "redirect:register?error3";
        }
        return "redirect:login";
    }

}
