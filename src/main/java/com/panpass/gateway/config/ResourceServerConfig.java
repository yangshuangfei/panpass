package com.panpass.gateway.config;


import com.panpass.commons.oauth.store.RedisTokenStore;
import com.panpass.commons.template.RedisRepository;
import com.panpass.gateway.auth.AuthorizationManager;
import com.panpass.gateway.auth.JwtAuthenticationManager;
import com.panpass.gateway.component.RestAuthenticationEntryPoint;
import com.panpass.gateway.component.RestfulAccessDeniedHandler;
import lombok.AllArgsConstructor;
import org.springframework.boot.context.properties.EnableConfigurationProperties;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.security.authentication.ReactiveAuthenticationManager;
import org.springframework.security.config.annotation.method.configuration.EnableReactiveMethodSecurity;
import org.springframework.security.config.annotation.web.reactive.EnableWebFluxSecurity;
import org.springframework.security.config.web.server.ServerHttpSecurity;
import org.springframework.security.oauth2.provider.token.TokenStore;
import org.springframework.security.web.server.SecurityWebFilterChain;
import com.panpass.commons.oauth.properties.SecurityProperties;

import javax.annotation.Resource;

/**
 * @Description: TODO
 * @ClassName ResourceServerConfig
 * @Author So_Ea
 * @Date 2021/2/2
 * @ModDate 2021/2/2
 * @ModUser So_Ea
 * @Version V1.0
 **/
@AllArgsConstructor
@EnableConfigurationProperties(SecurityProperties.class)
@EnableWebFluxSecurity
@EnableReactiveMethodSecurity
public class ResourceServerConfig {
    private final AuthorizationManager authorizationManager;
    private final RestfulAccessDeniedHandler restfulAccessDeniedHandler;
    private final RestAuthenticationEntryPoint restAuthenticationEntryPoint;
    /*@Resource
    private RedisIgnoreUrl redisIgnoreUrl;*/
    @Resource
    private RedisRepository redisRepository;
    @Resource
    private SecurityProperties securityProperties;

    @Bean
    public SecurityWebFilterChain springSecurityFilterChain(ServerHttpSecurity http) {
        http.oauth2ResourceServer().jwt()
                .authenticationManager(oauth2TokenAuthenticationManager());
        http.authorizeExchange()
                .pathMatchers(securityProperties.getIgnore().getUrls()).permitAll()/**白名单配置*/
                .anyExchange().access(authorizationManager)/**鉴权管理器配置*/
                .and().exceptionHandling()
                .accessDeniedHandler(restfulAccessDeniedHandler)//处理未授权
                .authenticationEntryPoint(restAuthenticationEntryPoint)//处理未认证
                .and().cors().and().csrf().disable();
        return http.build();
    }
    /*@Bean
    public Converter<Jwt, ? extends Mono<? extends AbstractAuthenticationToken>> jwtAuthenticationConverter() {
        JwtGrantedAuthoritiesConverter jwtGrantedAuthoritiesConverter = new JwtGrantedAuthoritiesConverter();
        jwtGrantedAuthoritiesConverter.setAuthorityPrefix(AuthConstant.AUTHORITY_PREFIX);
        jwtGrantedAuthoritiesConverter.setAuthoritiesClaimName(AuthConstant.AUTHORITY_CLAIM_NAME);
        JwtAuthenticationConverter jwtAuthenticationConverter = new JwtAuthenticationConverter();
        jwtAuthenticationConverter.setJwtGrantedAuthoritiesConverter(jwtGrantedAuthoritiesConverter);
        return new ReactiveJwtAuthenticationConverterAdapter(jwtAuthenticationConverter);
    }*/
    @Bean
    public ReactiveAuthenticationManager oauth2TokenAuthenticationManager(){
        return new JwtAuthenticationManager(tokenStore());
    }

    @Bean
    public TokenStore tokenStore() {
        RedisTokenStore tokenStore = new RedisTokenStore();
        return tokenStore;
    }


}
