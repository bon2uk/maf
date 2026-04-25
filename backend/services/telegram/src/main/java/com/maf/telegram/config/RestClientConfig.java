package com.maf.telegram.config;

import com.maf.telegram.security.ServiceJwtIssuer;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.http.HttpHeaders;
import org.springframework.http.MediaType;
import org.springframework.http.client.SimpleClientHttpRequestFactory;
import org.springframework.web.client.RestClient;

@Configuration
public class RestClientConfig {

    @Bean
    public RestClient parserRestClient(ParserProperties props, ServiceJwtIssuer jwtIssuer) {
        SimpleClientHttpRequestFactory factory = new SimpleClientHttpRequestFactory();
        factory.setConnectTimeout(props.connectTimeout());
        factory.setReadTimeout(props.readTimeout());

        return RestClient.builder()
                .baseUrl(props.baseUrl())
                .requestFactory(factory)
                .defaultHeader(HttpHeaders.CONTENT_TYPE, MediaType.APPLICATION_JSON_VALUE)
                // Per-request: re-fetch the cached service JWT so we don't
                // bake an expired token into the client. The issuer handles
                // refresh transparently.
                .defaultHeaders(headers ->
                        headers.setBearerAuth(jwtIssuer.currentToken()))
                .build();
    }
}
