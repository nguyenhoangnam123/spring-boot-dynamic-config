package com.ptfmobile.vn.authservice.domain.exception;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.ptfmobile.vn.common.BaseResponse;
import com.ptfmobile.vn.common.ErrorCodeDefs;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.boot.web.reactive.error.ErrorWebExceptionHandler;
import org.springframework.core.annotation.Order;
import org.springframework.core.io.buffer.DataBufferFactory;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.http.server.reactive.ServerHttpResponse;
import org.springframework.stereotype.Component;
import org.springframework.web.server.ResponseStatusException;
import org.springframework.web.server.ServerWebExchange;
import reactor.core.publisher.Mono;


@Component
@Slf4j
@Order(-1)
@RequiredArgsConstructor

public class GlobalExceptionHandler implements ErrorWebExceptionHandler {

//  @Autowired
//  private DataBufferWriter bufferWriter;
//
//  @Override
//  public Mono<Void> handle(ServerWebExchange exchange, Throwable ex) {
//    HttpStatus status = HttpStatus.OK;
//    BaseResponse response = new BaseResponse();
//    if (ex instanceof UserNotHaveRoleException) {
//      response.setFailed(ErrorCodeDefs.ERR_PERMISSION_INVALID, ex.getMessage());
//    }
//
//    if (exchange.getResponse().isCommitted()) {
//      return Mono.error(ex);
//    }
//
//    exchange.getResponse().setStatusCode(status);
//    return bufferWriter.write(exchange.getResponse(), response);
//
//
//
//  }

    private final ObjectMapper objectMapper;

    @Override
    public Mono<Void> handle(ServerWebExchange exchange, Throwable ex) {
        ServerHttpResponse response = exchange.getResponse();
        log.error("Exception = {}", ex);

        if (response.isCommitted()) {
            return Mono.error(ex);
        }

        // header set
        response.getHeaders().setContentType(MediaType.APPLICATION_JSON);
        if (ex instanceof ResponseStatusException) {
            response.setStatusCode(HttpStatus.OK);
        }
        int statusCode = ((ResponseStatusException) ex).getStatus().value();

        return response.writeWith(Mono.fromSupplier(() -> {
            DataBufferFactory bufferFactory = response.bufferFactory();
            try {
                BaseResponse baseResponse = new BaseResponse();
                baseResponse.setFailed(statusCode, ErrorCodeDefs.getMessage(statusCode));

                return bufferFactory.wrap(objectMapper.writeValueAsBytes(baseResponse));
            } catch (JsonProcessingException e) {
                log.error("Error writing response", ex);
                return bufferFactory.wrap(new byte[0]);
            }
        }));
    }


}
