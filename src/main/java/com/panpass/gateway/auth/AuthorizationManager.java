package com.panpass.gateway.auth;

import cn.hutool.core.collection.CollectionUtil;
import cn.hutool.core.convert.Convert;
import cn.hutool.core.util.ObjectUtil;
import com.panpass.commons.template.RedisRepository;
import com.panpass.gateway.constant.RedisConstant;
import lombok.AllArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.security.authorization.AuthorizationDecision;
import org.springframework.security.authorization.ReactiveAuthorizationManager;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.GrantedAuthority;
import org.springframework.security.oauth2.provider.OAuth2Authentication;
import org.springframework.security.web.server.authorization.AuthorizationContext;
import org.springframework.stereotype.Component;
import reactor.core.publisher.Mono;

import javax.annotation.Resource;
import java.net.URI;
import java.util.Collection;
import java.util.List;
import java.util.Objects;
import java.util.Set;
import java.util.stream.Collectors;

/**
 * @Description: TODO
 * @ClassName AuthorizationManager
 * @Author So_Ea
 * @Date 2021/2/2
 * @ModDate 2021/2/2
 * @ModUser So_Ea
 * @Version V1.0
 **/
@Component
@Slf4j
@AllArgsConstructor
public class AuthorizationManager implements ReactiveAuthorizationManager<AuthorizationContext> {
    @Resource
    private RedisRepository redisRepository;
    @Override
    public Mono<AuthorizationDecision> check(Mono<Authentication> mono, AuthorizationContext authorizationContext) {
       /* log.info("进去权限校验");*/
        URI uri = authorizationContext.getExchange().getRequest().getURI();
        /*log.info("请求参数：{}",authorizationContext.getExchange().getRequest().getHeaders());*/
        Object obj = redisRepository.getHashValues(RedisConstant.RESOURCE_ROLES_MAP, uri.getPath());
        List<String> authoritieRoles = Convert.toList(String.class,obj);
        return mono.map(auth -> new AuthorizationDecision(checkAuthorities(auth,authoritieRoles)))
                .defaultIfEmpty(new AuthorizationDecision(false));
    }

    private boolean checkAuthorities(Authentication auth, List<String> authoritieRoles) {
        /*if (auth instanceof OAuth2Authentication) {
            OAuth2Authentication oAuth2Authentication = (OAuth2Authentication) auth;
            Collection<GrantedAuthority> authorities = oAuth2Authentication.getAuthorities();
            Set<String> authorityList = authorities.stream().map(authoritie -> authoritieRoles.stream()
                    .filter(authoritieRole -> ObjectUtil.equal(authoritie.getAuthority(),authoritieRole))
                    .findFirst().orElse(null)).filter(Objects::nonNull).collect(Collectors.toSet());
            return CollectionUtil.isNotEmpty(authorityList)?true:false;
        }
        return false;*/
        return true;
    }

}
