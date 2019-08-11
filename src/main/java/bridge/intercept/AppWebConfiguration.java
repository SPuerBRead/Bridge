package bridge.intercept;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;
import org.springframework.web.servlet.config.annotation.InterceptorRegistry;
import org.springframework.web.servlet.config.annotation.WebMvcConfigurationSupport;

@Component
public class AppWebConfiguration extends WebMvcConfigurationSupport {
    @Autowired
    WebLogIntercept webLogIntercept;

    @Override
    public void addInterceptors(InterceptorRegistry registry) {
        registry.addInterceptor(webLogIntercept).addPathPatterns("/**");
        super.addInterceptors(registry);
    }
}
