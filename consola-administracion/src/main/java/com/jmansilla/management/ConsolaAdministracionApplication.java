package com.jmansilla.management;

import java.nio.charset.StandardCharsets;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.AutoConfigureBefore;
import org.springframework.boot.autoconfigure.EnableAutoConfiguration;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.boot.autoconfigure.condition.ConditionalOnBean;
import org.springframework.boot.autoconfigure.condition.ConditionalOnMissingBean;
import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.cloud.client.discovery.EnableDiscoveryClient;
import org.springframework.context.ApplicationContext;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.mail.MailSender;
import org.springframework.mail.javamail.JavaMailSender;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.config.annotation.web.configuration.WebSecurityConfigurerAdapter;
import org.springframework.security.web.authentication.SavedRequestAwareAuthenticationSuccessHandler;
import org.springframework.security.web.csrf.CookieCsrfTokenRepository;
import org.thymeleaf.TemplateEngine;
import org.thymeleaf.spring5.SpringTemplateEngine;
import org.thymeleaf.spring5.templateresolver.SpringResourceTemplateResolver;
import org.thymeleaf.templatemode.TemplateMode;

import de.codecentric.boot.admin.server.config.AdminServerProperties;
import de.codecentric.boot.admin.server.config.EnableAdminServer;
import de.codecentric.boot.admin.server.config.AdminServerNotifierAutoConfiguration.CompositeNotifierConfiguration;
import de.codecentric.boot.admin.server.config.AdminServerNotifierAutoConfiguration.MailNotifierConfiguration;
import de.codecentric.boot.admin.server.config.AdminServerNotifierAutoConfiguration.NotifierTriggerConfiguration;
import de.codecentric.boot.admin.server.domain.entities.InstanceRepository;
import de.codecentric.boot.admin.server.notify.MailNotifier;

@SpringBootApplication
@EnableAutoConfiguration
@EnableDiscoveryClient
@EnableAdminServer
public class ConsolaAdministracionApplication {

	@Autowired
	private MailNotifierConfiguration template;
	
	public static void main(String[] args) {
		SpringApplication.run(ConsolaAdministracionApplication.class, args);
	}

    @Bean
    @ConfigurationProperties("spring.boot.admin.notify.mail")
    public MailNotifier mailNotifier(JavaMailSender mailSender, InstanceRepository repository) {
        return new MailNotifierCustom(mailSender, repository, template.mailNotifierTemplateEngine());
    }
        
	
	@Configuration
	public static class SecuritySecureConfig extends WebSecurityConfigurerAdapter {
	    private final String adminContextPath;

	    public SecuritySecureConfig(AdminServerProperties adminServerProperties) {
	        this.adminContextPath = adminServerProperties.getContextPath();
	    }

	    @Override
	    protected void configure(HttpSecurity http) throws Exception {
	        // @formatter:off
	        SavedRequestAwareAuthenticationSuccessHandler successHandler = new SavedRequestAwareAuthenticationSuccessHandler();
	        successHandler.setTargetUrlParameter("redirectTo");
	        successHandler.setDefaultTargetUrl(adminContextPath + "/");

	        http.authorizeRequests()
	            .antMatchers(adminContextPath + "/assets/**").permitAll() 
	            .antMatchers(adminContextPath + "/login").permitAll()
	            .anyRequest().authenticated() 
	            .and()
	        .formLogin().loginPage(adminContextPath + "/login").successHandler(successHandler).and() 
	        .logout().logoutUrl(adminContextPath + "/logout").and()
	        .httpBasic().and() 
	        .csrf()
	            .csrfTokenRepository(CookieCsrfTokenRepository.withHttpOnlyFalse())  
	            .ignoringAntMatchers(
	                adminContextPath + "/instances",   
	                adminContextPath + "/actuator/**"  
	            );
	        // @formatter:on
	    }
	}
}

