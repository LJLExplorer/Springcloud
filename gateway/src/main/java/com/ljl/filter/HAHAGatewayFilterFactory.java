package com.ljl.filter;

import lombok.*;
import org.springframework.cloud.gateway.filter.GatewayFilter;
import org.springframework.cloud.gateway.filter.GatewayFilterChain;
import org.springframework.cloud.gateway.filter.factory.AbstractGatewayFilterFactory;
import org.springframework.http.server.reactive.ServerHttpRequest;
import org.springframework.stereotype.Component;
import org.springframework.web.server.ServerWebExchange;
import reactor.core.publisher.Mono;

import java.util.List;

@Component
public class HAHAGatewayFilterFactory extends AbstractGatewayFilterFactory<HAHAGatewayFilterFactory.Config> {

    static final String PARAM_NAME = "param";

    public HAHAGatewayFilterFactory() {
        super(Config.class);
    }

    public List<String> shortcutFieldOrder() {

        return List.of(PARAM_NAME);
    }

    //    过滤器（MyParamGatewayFilterFactory）中将http://localhost:10010/api/user/8?name=yemage
    //    中的参数name的值获取到并输出到控制台；并且参数名是可变的，也就是不一定每次都是name；需要可以通过配置过滤器的时候做到配置参数名。
    @Override
    public GatewayFilter apply(Config config) {
        // 非lambda写法
        return new GatewayFilter() {
            @Override
            public Mono<Void> filter(ServerWebExchange exchange, GatewayFilterChain chain) {
                // 拉取请求
                ServerHttpRequest request = exchange.getRequest();
                // 检查参数中是否包含 config.name
                if (request.getQueryParams().containsKey(config.param)) {
                    // 遍历参数的值并打印
                    request.getQueryParams().get(config.param).forEach(value -> {
                        System.out.printf("局部过滤器: 参数 %s = %s%n", config.param, value);
                    });
                }
                // 将请求传递给下一个过滤器
                return chain.filter(exchange);
            }
        };

        // lambda写法
//        return ((exchange, chain) -> {
//            // http://localhost:10010/api/user/8?name=yemage config.param ==> name
//            //获取请求参数中param对应的参数名 的参数值
//            ServerHttpRequest request = exchange.getRequest();
//            if (request.getQueryParams().containsKey(config.param)) {
//                // 遍历参数的值并打印
//                request.getQueryParams().get(config.param).forEach(value -> {
//                    System.out.printf("局部过滤器: 参数 %s = %s%n", config.param, value);
//                });
//            }
//            return chain.filter(exchange);
//        });

    }

    @Data
    @NoArgsConstructor
    @AllArgsConstructor
    public static class Config {
        //对应在配置过滤器的时候指定的参数名
        private String param;
    }
}
