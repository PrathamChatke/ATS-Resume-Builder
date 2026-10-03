package com.atsresume.config;

import org.springframework.context.annotation.Configuration;
import org.springframework.web.servlet.config.annotation.ViewControllerRegistry;
import org.springframework.web.servlet.config.annotation.WebMvcConfigurer;

/**
 * The React app is served as static resources from the same Spring Boot jar
 * and owns client-side routing. A hard refresh on a deep link (e.g.
 * /resumes/5) must still resolve to index.html rather than 404 - everything
 * that isn't an API/actuator call or a static file (has a file extension)
 * is forwarded there.
 */
@Configuration
public class SpaWebConfig implements WebMvcConfigurer {

    @Override
    public void addViewControllers(ViewControllerRegistry registry) {
        registry.addViewController("/{path:^(?!api|actuator)[^.]*$}").setViewName("forward:/index.html");
        registry.addViewController("/{path:^(?!api|actuator)[^.]*$}/**").setViewName("forward:/index.html");
    }
}
