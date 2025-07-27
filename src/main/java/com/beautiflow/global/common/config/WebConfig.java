package com.beautiflow.global.common.config;


import com.beautiflow.global.common.OctetStreamReadMsgConverter;
import java.util.List;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Configuration;
import org.springframework.http.converter.HttpMessageConverter;
import org.springframework.web.servlet.config.annotation.CorsRegistry;
import org.springframework.web.servlet.config.annotation.WebMvcConfigurer;

@Configuration
public class WebConfig implements WebMvcConfigurer {

  private OctetStreamReadMsgConverter octetStreamReadMsgConverter;

  @Autowired
  public WebConfig(OctetStreamReadMsgConverter octetStreamReadMsgConverter) {
    this.octetStreamReadMsgConverter = octetStreamReadMsgConverter;
  }

  @Override
  public void configureMessageConverters(List<HttpMessageConverter<?>> converters) {
    converters.add(octetStreamReadMsgConverter);
  }

  @Override
  public void addCorsMappings(CorsRegistry registry) {
    registry.addMapping("/**") // 모든 경로에 대해
        .allowedOrigins("*") // 모든 오리진 허용
        .allowedMethods("GET", "POST", "PUT", "DELETE", "OPTIONS") // 허용할 HTTP 메서드
        .allowedHeaders("Authorization", "Content-Type") // 허용할 헤더
        .maxAge(3600); // pre-flight 요청의 캐시 시간 (초)
  }
}