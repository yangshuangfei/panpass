package com.panpass.gateway.auth;

import cn.hutool.core.util.ObjectUtil;
import com.alibaba.fastjson.JSON;
import com.panpass.commons.biz.CurrentUser;
import com.panpass.commons.template.RedisRepository;
import com.panpass.commons.util.CurrentUserUtil;
import com.sp.common.model.LoginUser;
import com.yami.shop.security.service.YamiUser;
import lombok.extern.slf4j.Slf4j;
import org.springframework.security.authentication.AuthenticationServiceException;
import org.springframework.security.authentication.BadCredentialsException;
import org.springframework.security.authentication.ReactiveAuthenticationManager;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.AuthenticationException;
import org.springframework.security.oauth2.common.OAuth2AccessToken;
import org.springframework.security.oauth2.common.exceptions.OAuth2Exception;
/*import org.springframework.security.oauth2.jwt.BadJwtException;*/
import org.springframework.security.oauth2.jwt.JwtException;
import org.springframework.security.oauth2.jwt.JwtValidationException;
import org.springframework.security.oauth2.provider.OAuth2Authentication;
import org.springframework.security.oauth2.provider.token.TokenStore;
import org.springframework.security.oauth2.server.resource.BearerTokenAuthenticationToken;
/*import org.springframework.security.oauth2.server.resource.InvalidBearerTokenException;*/
import reactor.core.publisher.Mono;

import javax.annotation.Resource;

/**
 * @Description: TODO
 * @ClassName JwtAuthenticationManager
 * @Author So_Ea
 * @Date 2021/2/5
 * @ModDate 2021/2/5
 * @ModUser So_Ea
 * @Version V1.0
 **/
@Slf4j
public class JwtAuthenticationManager implements ReactiveAuthenticationManager{
    /*@Resource
    private RedisRepository redisRepository;*/

    private TokenStore tokenStore;

    public JwtAuthenticationManager(TokenStore tokenStore) {
        this.tokenStore = tokenStore;
    }
    @Override
    public Mono<Authentication> authenticate(Authentication authentication) {
        return Mono.justOrEmpty(authentication)
                .filter(authenticationToken -> authenticationToken instanceof BearerTokenAuthenticationToken)
                .cast(BearerTokenAuthenticationToken.class)
                .map(BearerTokenAuthenticationToken::getToken)
                .flatMap(this::chekToken).onErrorMap(JwtException.class,this::onError);
    }

    private AuthenticationException onError(JwtException e) {
        if (e instanceof JwtValidationException) {
            return new BadCredentialsException(e.getMessage(), e);
        } else {
            return new AuthenticationServiceException(e.getMessage(), e);
        }
    }

    private Mono<Authentication> chekToken(String accessToken) throws JwtException{
        /*log.info("accessToken is :{}",accessToken);*/
        OAuth2Authentication oAuth2Authentication = this.tokenStore.readAuthentication(accessToken);
        if (ObjectUtil.isNull(oAuth2Authentication)) {
            return Mono.error(OAuth2Exception.create(OAuth2Exception.INVALID_TOKEN,"当前登录信息失效,请退出后重新登录"));
        }
        if(ObjectUtil.isNotNull(oAuth2Authentication)){
            LoginUser loginUser = null;
            Object oauthUser = oAuth2Authentication.getUserAuthentication().getPrincipal();
            if(oauthUser instanceof LoginUser){
                loginUser = (LoginUser) oAuth2Authentication.getUserAuthentication().getPrincipal();
            }else{
                YamiUser yamiUser = (YamiUser) oAuth2Authentication.getUserAuthentication().getPrincipal();
                loginUser = new LoginUser();
                loginUser.setPId(Long.parseLong(yamiUser.getUserId())).setUserName(yamiUser.getOpenId());
            }
            if(ObjectUtil.isNull(loginUser)){
                return Mono.error(OAuth2Exception.create(OAuth2Exception.INVALID_TOKEN,"当前登录信息失效,请退出后重新登录"));
            }
            if(ObjectUtil.isNull(loginUser.getPId())){
                return Mono.error(OAuth2Exception.create(OAuth2Exception.INVALID_TOKEN,"当前登录信息失效,请退出后重新登录"));
            }

            /*log.info("当前登录用户信息：{}", JSON.toJSONString(loginUser));*/

            /*OAuth2AccessToken oAuth2AccessToken = this.tokenStore.getAccessToken(oAuth2Authentication);*/
            /** 每次操作，更新redis中token过期时间  */
            /*this.tokenStore.storeAccessToken(oAuth2AccessToken,oAuth2Authentication);*/
            CurrentUser currentUser=new CurrentUser().setId(loginUser.getPId()).setUsername(ObjectUtil.isNull(loginUser.getUsername())? loginUser.getUserName() : loginUser.getUsername());
            if(ObjectUtil.isNotNull(loginUser.getUserType()) && loginUser.getUserType() == 1){  //1 代表经销商
                currentUser.setAgentId(loginUser.getJxId().toString());
            }
            CurrentUserUtil.setUserContext(currentUser);
        }
        return Mono.just(oAuth2Authentication);
    }
}
