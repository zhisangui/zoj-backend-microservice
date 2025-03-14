package com.zhisangui.zojbackendgateway.config;

import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.web.cors.CorsConfiguration;
import org.springframework.web.cors.reactive.CorsWebFilter;
import org.springframework.web.cors.reactive.UrlBasedCorsConfigurationSource;
import org.springframework.web.util.pattern.PathPatternParser;

import java.util.Arrays;
import java.util.List;

/**
 * @author zsg
 */ // 处理跨域
@Configuration
public class CorsConfig {

    @Bean
    public CorsWebFilter corsFilter() {
        CorsConfiguration config = new CorsConfiguration();
        config.addAllowedMethod("*");
        config.setAllowCredentials(true);
        // ★ 上线必须把 localhost 修改为实际ip,由于后来加了ssl证书，前端改为了https，因此进行补充
//        List<String> allowedOrigins = Arrays.asList(
//                "http://103.73.66.197", "https://103.73.66.197",
//                "http://47.106.179.250", "http://47.106.179.250",
//                "http://oj.nmsl.us", "https://oj.nmsl.us");
        List<String> allowedOrigins = Arrays.asList("*");
        config.setAllowedOriginPatterns(allowedOrigins);
        config.addAllowedHeader("*");
        UrlBasedCorsConfigurationSource source = new UrlBasedCorsConfigurationSource(new PathPatternParser());
        source.registerCorsConfiguration("/**", config);
        return new CorsWebFilter(source);
    }
}