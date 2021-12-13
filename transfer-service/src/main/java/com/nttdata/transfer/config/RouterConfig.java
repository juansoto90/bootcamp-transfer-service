package com.nttdata.transfer.config;

import com.nttdata.transfer.handler.TransferHandler;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import static org.springframework.web.reactive.function.server.RequestPredicates.*;
import org.springframework.web.reactive.function.server.RouterFunction;
import static org.springframework.web.reactive.function.server.RouterFunctions.route;
import org.springframework.web.reactive.function.server.ServerResponse;

@Configuration
public class RouterConfig {
    @Bean
    public RouterFunction<ServerResponse> routes(TransferHandler handler){
        return route(GET("/transfer/{id}"), handler::findById)
                .andRoute(POST("/transfer"), handler::accountsBetweenTransfer);
    }
}
