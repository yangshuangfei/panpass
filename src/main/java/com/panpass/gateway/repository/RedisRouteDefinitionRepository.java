package com.panpass.gateway.repository;

import com.alibaba.fastjson.JSON;
import com.panpass.commons.template.RedisRepository;
import com.panpass.gateway.biz.dto.GateWayConfigDTO;
import org.springframework.cloud.gateway.filter.FilterDefinition;
import org.springframework.cloud.gateway.handler.predicate.PredicateDefinition;
import org.springframework.cloud.gateway.route.RouteDefinition;
import org.springframework.cloud.gateway.route.RouteDefinitionRepository;
import org.springframework.cloud.gateway.support.NotFoundException;
import org.springframework.stereotype.Component;
import org.springframework.web.util.UriComponentsBuilder;
import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;

import javax.annotation.Resource;
import java.net.URI;
import java.util.*;

/**
 * @Description: TODO
 * @ClassName RedisRouteDefinitionRepository
 * @Author So_Ea
 * @Date 2020/6/14
 * @ModDate 2020/6/14
 * @ModUser So_Ea
 * @Version V1.0
 **/
/*@Component*/
public class RedisRouteDefinitionRepository implements RouteDefinitionRepository {

    public static final String  GATEWAY_ROUTES = "GATEWAY:ROUTES";
    @Resource
    private RedisRepository redisRepository;
    /**
     * @Description 从redis中加载路由信息
     * @MethodName getRouteDefinitions
     * @Author So_Ea
     * @Date 19:29 2020/6/14
     * @Param []
     * @ModDate 19:29 2020/6/14
     * @ModUser So_Ea
     * @return reactor.core.publisher.Flux<org.springframework.cloud.gateway.route.RouteDefinition>
     **/
    @Override
    public Flux<RouteDefinition> getRouteDefinitions() {
        List<RouteDefinition> routeDefinitions = new ArrayList<>();
        redisRepository.opsForHash().values(GATEWAY_ROUTES).stream().forEach(definition -> {
            GateWayConfigDTO gateWayConfigDTO = JSON.parseObject(definition.toString(), GateWayConfigDTO.class);
            routeDefinitions.add(this.handleData(gateWayConfigDTO));
        });
        return Flux.fromIterable(routeDefinitions);
    }

    /**
     * @Description 新增路由
     * @MethodName save
     * @Author So_Ea
     * @Date 20:01 2020/6/14
     * @Param [route]
     * @ModDate 20:01 2020/6/14
     * @ModUser So_Ea
     * @return reactor.core.publisher.Mono<java.lang.Void>
     **/
    @Override
    public Mono<Void> save(Mono<RouteDefinition> route) {
        return route.flatMap(routeDefinition -> {
            redisRepository.opsForHash().put(GATEWAY_ROUTES, routeDefinition.getId(),
                    JSON.toJSONString(routeDefinition));
            return Mono.empty();
        });
    }

    /**
     * @Description 删除路由
     * @MethodName delete
     * @Author So_Ea
     * @Date 20:01 2020/6/14
     * @Param [routeId]
     * @ModDate 20:01 2020/6/14
     * @ModUser So_Ea
     * @return reactor.core.publisher.Mono<java.lang.Void>
     **/
    @Override
    public Mono<Void> delete(Mono<String> routeId) {
        return routeId.flatMap(id -> {
            if (redisRepository.opsForHash().hasKey(GATEWAY_ROUTES, id)) {
                redisRepository.opsForHash().delete(GATEWAY_ROUTES, id);
                return Mono.empty();
            }
            return Mono.defer(() -> Mono.error(new NotFoundException("路由文件没有找到: " + routeId)));
        });
    }

    /**
     * 路由数据转换公共方法
     * @param gatewayRoute
     * @return
     */
    public RouteDefinition handleData(GateWayConfigDTO gatewayRoute){
        RouteDefinition definition = new RouteDefinition();
        Map<String, String> predicateParams = new HashMap<>(8);
        PredicateDefinition predicate = new PredicateDefinition();
        FilterDefinition filterDefinition = new FilterDefinition();
        Map<String, String> filterParams = new HashMap<>(8);

        URI uri = UriComponentsBuilder.fromUriString(gatewayRoute.getUri()).build().toUri();
        /*if(gatewayRoute.getUri().startsWith("http")){
            //http地址
            uri = UriComponentsBuilder.fromHttpUrl(gatewayRoute.getUri()).build().toUri();
        }else{
            //注册中心
            uri = UriComponentsBuilder.fromUriString(gatewayRoute.getUri()).build().toUri();
        }*/


        definition.setId(gatewayRoute.getServiceId());
        // 名称是固定的，spring gateway会根据名称找对应的PredicateFactory
        predicate.setName("Path");
        predicateParams.put("pattern",gatewayRoute.getPredicates());
        predicate.setArgs(predicateParams);

        // 名称是固定的, 路径去前缀
        filterDefinition.setName("StripPrefix");
        filterParams.put("_genkey_0", gatewayRoute.getFilters().toString());
        filterDefinition.setArgs(filterParams);

        definition.setPredicates(Arrays.asList(predicate));
        definition.setFilters(Arrays.asList(filterDefinition));
        definition.setUri(uri);
        definition.setOrder(Integer.parseInt(gatewayRoute.getOrderNo()));

        return definition;
    }
}
