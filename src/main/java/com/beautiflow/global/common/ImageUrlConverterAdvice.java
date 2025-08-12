package com.beautiflow.global.common;

import java.lang.reflect.Field;
import java.util.Collection;
import java.util.Collections;
import java.util.IdentityHashMap;
import java.util.Set;
import lombok.SneakyThrows;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.core.MethodParameter;
import org.springframework.http.MediaType;
import org.springframework.http.converter.HttpMessageConverter;
import org.springframework.http.server.ServerHttpRequest;
import org.springframework.http.server.ServerHttpResponse;
import org.springframework.web.bind.annotation.RestControllerAdvice;
import org.springframework.web.servlet.mvc.method.annotation.ResponseBodyAdvice;

@RestControllerAdvice(basePackages = "com.beautiflow")
public class ImageUrlConverterAdvice implements ResponseBodyAdvice<Object> {

  @Value("${spring.cloud.aws.cloudfront.domain}")
  private String cloudfrontDomain;

  @Override
  public boolean supports(MethodParameter returnType, Class<? extends HttpMessageConverter<?>> converterType) {
    return true;
  }

  @Override
  public Object beforeBodyWrite(Object body, MethodParameter returnType, MediaType selectedContentType,
      Class<? extends HttpMessageConverter<?>> selectedConverterType,
      ServerHttpRequest request, ServerHttpResponse response) {

    if (body != null) {
      // [수정] 방문한 객체를 추적하기 위한 Set을 생성합니다.
      // IdentityHashMap은 객체의 주소값(==)으로 비교하여 순환 참조를 정확히 감지합니다.
      Set<Object> visited = Collections.newSetFromMap(new IdentityHashMap<>());
      convertFields(body, visited);
    }
    return body;
  }

  @SneakyThrows
  private void convertFields(Object target, Set<Object> visited) {
    // target이 null이거나, 이미 방문한 객체이면 무한 루프 방지를 위해 즉시 종료합니다.
    if (target == null || !visited.add(target)) {
      return;
    }

    if (target instanceof Collection<?> collection) {
      for (Object item : collection) {
        convertFields(item, visited); // [수정] visited Set을 재귀 호출에 전달
      }
      return;
    }
    if (target instanceof java.util.Map<?, ?> map) {
      for (Object item : map.values()) {
        convertFields(item, visited); // [수정] visited Set을 재귀 호출에 전달
      }
      return;
    }

    if (target.getClass().isPrimitive() || target.getClass().getName().startsWith("java.")) {
      return;
    }

    for (Field field : target.getClass().getDeclaredFields()) {
      field.setAccessible(true);
      Object value = field.get(target);

      if (value instanceof String url) {
        if (isS3Key(url)) {
          field.set(target, cloudfrontDomain + "/images/" + url);
        }
      } else {
        convertFields(value, visited); // [수정] visited Set을 재귀 호출에 전달
      }
    }
  }

  private boolean isS3Key(String value) {
    // [수정] 더 많은 경로 패턴을 추가하여 변환 대상을 명확히 합니다.
    return value != null && (value.startsWith("shops/") || value.startsWith("treatments/") || value.startsWith("members/"));
  }
}