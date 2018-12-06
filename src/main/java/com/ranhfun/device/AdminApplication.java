package com.ranhfun.device;

import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;

import javax.persistence.EntityManager;

import org.apache.shiro.realm.Realm;
import org.apache.shiro.session.mgt.eis.SessionDAO;
import org.apache.shiro.spring.web.config.DefaultShiroFilterChainDefinition;
import org.apache.shiro.spring.web.config.ShiroFilterChainDefinition;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.aop.framework.autoproxy.DefaultAdvisorAutoProxyCreator;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.propertyeditors.CustomDateEditor;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.data.redis.connection.RedisConnectionFactory;
import org.springframework.data.redis.core.RedisKeyValueTemplate;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.data.redis.core.convert.MappingRedisConverter;
import org.springframework.data.redis.core.convert.RedisCustomConversions;
import org.springframework.data.redis.core.convert.ReferenceResolver;
import org.springframework.data.redis.core.mapping.RedisMappingContext;
import org.springframework.data.redis.serializer.RedisSerializer;
import org.springframework.data.redis.serializer.StringRedisSerializer;
import org.springframework.web.bind.WebDataBinder;
import org.springframework.web.bind.annotation.InitBinder;
import org.springframework.web.method.support.HandlerMethodArgumentResolver;
import org.springframework.web.servlet.config.annotation.WebMvcConfigurer;

import com.ranhfun.device.annotation.JPAMethodArgumentResolver;
import com.ranhfun.device.cache.RedisCacheSessionDAO;
import com.ranhfun.device.service.UserRepository;
import com.ranhfun.device.service.UtilManager;

@Configuration
@SpringBootApplication
public class AdminApplication implements WebMvcConfigurer {

	private static Logger log = LoggerFactory.getLogger(AdminApplication.class);
	
	@Autowired
	private EntityManager em;
	
	public static void main(String[] args) throws Exception {
		SpringApplication.run(AdminApplication.class, args);
	}

    @Bean
    public Realm realm(UserRepository userRepository, UtilManager utilManager) {
        return new AdminRealm(userRepository, utilManager);
    }

    @Bean
    public ShiroFilterChainDefinition shiroFilterChainDefinition() {
        DefaultShiroFilterChainDefinition chainDefinition = new DefaultShiroFilterChainDefinition();
        chainDefinition.addPathDefinition("/login", "anon");
        chainDefinition.addPathDefinition("/register", "anon");
        chainDefinition.addPathDefinition("/api", "authcBasic, rest[api]");
        chainDefinition.addPathDefinition("/*", "authc"); 
        chainDefinition.addPathDefinition("/logout", "logout");
        return chainDefinition;
    }
    
    @InitBinder
    public void initBinder(WebDataBinder binder) {
        SimpleDateFormat dateFormat = new SimpleDateFormat("yyyy-MM-dd");
        dateFormat.setLenient(false);
        binder.registerCustomEditor(Date.class, new CustomDateEditor(dateFormat, false));
    }

	@Override
	public void addArgumentResolvers(List<HandlerMethodArgumentResolver> resolvers) {
		resolvers.add(new JPAMethodArgumentResolver(em));
	}
    
    @Bean
    public static DefaultAdvisorAutoProxyCreator getDefaultAdvisorAutoProxyCreator() {
        DefaultAdvisorAutoProxyCreator defaultAdvisorAutoProxyCreator = new DefaultAdvisorAutoProxyCreator();
        defaultAdvisorAutoProxyCreator.setUsePrefix(true);
        return defaultAdvisorAutoProxyCreator;
    }
    
//    @Bean
//    public MappingRedisConverter redisConverter(RedisMappingContext mappingContext, ReferenceResolver referenceResolver) {
//
//      MappingRedisConverter mappingRedisConverter = new MappingRedisConverter(mappingContext, null, referenceResolver);
//
//      List<Object> converters = new ArrayList<>();
////      converters.add(new ObjectToBytesConverter());
////      converters.add(new BytesToObjectConverter());
////      converters.add(new MapToBytesConverter());
////      converters.add(new BytesToMapConverter());
////      converters.add(new SavedRequestToBytesConverter());
////      converters.add(new BytesToSavedRequestConverter());
////      converters.add(new SimpleSessionToBytesConverter());
////      converters.add(new BytesToSimpleSessionConverter());
//      
//      mappingRedisConverter.setCustomConversions(new RedisCustomConversions(converters));
//
//      return mappingRedisConverter;
//    }
    
    @Bean
    public SessionDAO sessionDAO(/*RedisTemplate<String, byte[]> template*/RedisConnectionFactory connectionFactory) {
    	RedisTemplate<String, byte[]> template = new RedisTemplate<String, byte[]>();
    	RedisSerializer<String> stringSerializer = new StringRedisSerializer();
    	template.setKeySerializer(stringSerializer);
    	template.setValueSerializer(stringSerializer);
    	template.setHashKeySerializer(stringSerializer);
//    	template.setHashValueSerializer(stringSerializer);
    	template.setConnectionFactory(connectionFactory);
    	template.afterPropertiesSet();
        return new RedisCacheSessionDAO(template);
    }
}