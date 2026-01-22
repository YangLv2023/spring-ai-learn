package com.learn.springailearn.config;

import okhttp3.OkHttpClient;
import org.springframework.boot.web.client.RestClientCustomizer;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.http.client.BufferingClientHttpRequestFactory;
import org.springframework.http.client.OkHttp3ClientHttpRequestFactory;

import java.util.concurrent.TimeUnit;

/**
 * HTTP客户端配置，主要用于设置AI服务调用的超时参数
 * @author ken
 * @date 2024-01-21
 */
@Configuration
public class HttpClientConfig {

    /**
     * 自定义RestClient的请求工厂，设置连接和读取超时
     */
    @Bean
    public RestClientCustomizer restClientCustomizer() {
        return restClientBuilder -> {
            // 创建OkHttpClient，配置各种超时时间
            OkHttpClient okHttpClient = new OkHttpClient.Builder()
                    .connectTimeout(60, TimeUnit.SECONDS)      // 连接超时
                    .readTimeout(120, TimeUnit.SECONDS)        // 读取超时
                    .writeTimeout(60, TimeUnit.SECONDS)        // 写入超时
                    .callTimeout(180, TimeUnit.SECONDS)        // 调用总超时
                    .retryOnConnectionFailure(true)            // 连接失败时重试
                    .build();

            // 使用OkHttp3ClientHttpRequestFactory
            OkHttp3ClientHttpRequestFactory requestFactory = new OkHttp3ClientHttpRequestFactory(okHttpClient);
            
            // 设置缓冲以便能够多次读取响应体
            BufferingClientHttpRequestFactory bufferingFactory = new BufferingClientHttpRequestFactory(requestFactory);
            
            restClientBuilder.requestFactory(bufferingFactory);
        };
    }
}