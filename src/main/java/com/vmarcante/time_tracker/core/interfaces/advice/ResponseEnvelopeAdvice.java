package com.vmarcante.time_tracker.core.interfaces.advice;

import org.springframework.core.MethodParameter;
import org.springframework.core.io.Resource;
import org.springframework.http.MediaType;
import org.springframework.http.converter.HttpMessageConverter;
import org.springframework.http.server.ServerHttpRequest;
import org.springframework.http.server.ServerHttpResponse;
import org.springframework.http.server.ServletServerHttpResponse;
import org.springframework.web.bind.annotation.RestControllerAdvice;
import org.springframework.web.servlet.mvc.method.annotation.ResponseBodyAdvice;

import com.vmarcante.time_tracker.base.domain.response.ApiResponseDTO;

@RestControllerAdvice(basePackages = "com.vmarcante.time_tracker.core.interfaces")
public class ResponseEnvelopeAdvice implements ResponseBodyAdvice<Object> {

    @Override
    public boolean supports(MethodParameter returnType, Class<? extends HttpMessageConverter<?>> converterType) {
        String declaringClassName = returnType.getDeclaringClass().getName();
        if (declaringClassName.contains("springdoc") || declaringClassName.contains("swagger")) {
            return false;
        }
        
        return !returnType.getParameterType().equals(byte[].class)
                && !Resource.class.isAssignableFrom(returnType.getParameterType());
    }

    @Override
    public Object beforeBodyWrite(Object body, MethodParameter returnType, MediaType selectedContentType,
            Class<? extends HttpMessageConverter<?>> selectedConverterType,
            ServerHttpRequest request, ServerHttpResponse response) {

        if (body instanceof ApiResponseDTO<?>) {
            return body;
        }

        int status = extractStatus(response);

        return ApiResponseDTO.ok(status, body);
    }

    private int extractStatus(ServerHttpResponse response) {
        if (response instanceof ServletServerHttpResponse servletResponse) {
            return servletResponse.getServletResponse().getStatus();
        }

        return 200;
    }
}
