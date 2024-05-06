package searchengine.config;
import org.springframework.boot.jdbc.DataSourceBuilder;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.ComponentScan;
import org.springframework.context.annotation.Configuration;




@Configuration
@ComponentScan("searchengine")
public class SpringConfig {

    @Bean
    public static Site site() {
        return new Site();
    }




}



