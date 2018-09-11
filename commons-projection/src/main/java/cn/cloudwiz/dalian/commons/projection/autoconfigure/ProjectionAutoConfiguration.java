package cn.cloudwiz.dalian.commons.projection.autoconfigure;

import cn.cloudwiz.dalian.commons.projection.ProxyProjectionFactoryBean;
import org.springframework.boot.autoconfigure.condition.ConditionalOnMissingBean;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.ComponentScan;
import org.springframework.context.annotation.Configuration;
import org.springframework.data.projection.ProjectionFactory;

@Configuration
@ComponentScan("cn.cloudwiz.dalian.commons.projection")
public class ProjectionAutoConfiguration {

    @Bean
    @ConditionalOnMissingBean(ProjectionFactory.class)
    public ProxyProjectionFactoryBean createProjectionFactory() throws Exception {
        return new ProxyProjectionFactoryBean();
    }

}
