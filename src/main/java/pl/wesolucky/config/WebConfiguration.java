package pl.wesolucky.config;

import org.springframework.context.annotation.Bean;
import org.springframework.stereotype.Component;
import org.springframework.web.servlet.config.annotation.CorsRegistry;
import org.springframework.web.servlet.config.annotation.WebMvcConfigurer;
import org.springframework.web.servlet.config.annotation.WebMvcConfigurerAdapter;

@Component
public class WebConfiguration 
{
	
    @Bean
    public WebMvcConfigurer corsConfigurer() 
    {
        return new WebMvcConfigurerAdapter() 
        {
        	// See more settings >> https://spring.io/blog/2015/06/08/cors-support-in-spring-framework
            @Override
            public void addCorsMappings(CorsRegistry registry) 
            {
                registry.addMapping("/**")
                		.allowedMethods("GET", "POST", "PUT", "DELETE");
            }
        };
    }
}
