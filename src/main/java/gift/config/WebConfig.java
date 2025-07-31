package gift.config;

import org.springframework.boot.web.servlet.FilterRegistrationBean;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.http.client.SimpleClientHttpRequestFactory;
import org.springframework.web.client.RestClient;
import org.springframework.web.method.support.HandlerMethodArgumentResolver;
import org.springframework.web.servlet.config.annotation.WebMvcConfigurer;

import java.time.Duration;
import java.util.List;

@Configuration
public class WebConfig implements WebMvcConfigurer {

    private final JwtAuthFilter jwtAuthFilter;
    private final LoginMemberArgumentResolver loginMemberArgumentResolver;

    public WebConfig(JwtAuthFilter jwtAuthFilter, LoginMemberArgumentResolver loginMemberArgumentResolver) {
        this.jwtAuthFilter = jwtAuthFilter;
        this.loginMemberArgumentResolver = loginMemberArgumentResolver;
    }

    @Override
    public void addArgumentResolvers(List<HandlerMethodArgumentResolver> resolvers) {
        resolvers.add(loginMemberArgumentResolver);
    }

    @Bean
    public FilterRegistrationBean<JwtAuthFilter> jwtFilter() {
        FilterRegistrationBean<JwtAuthFilter> registration = new FilterRegistrationBean<>();
        registration.setFilter(jwtAuthFilter);
        registration.addUrlPatterns("/api/products/*", "/admin/*", "/wishlist/*", "/api/orders/*");
        return registration;
    }

    @Bean
    public RestClient restClient() {
        SimpleClientHttpRequestFactory factory = new SimpleClientHttpRequestFactory();
        factory.setConnectTimeout(3000); // 3초 연결 타임아웃
        factory.setReadTimeout(5000);   // 5초 읽기 타임아웃

        return RestClient.builder()
                .requestFactory(factory)
                .build();
    }
}
