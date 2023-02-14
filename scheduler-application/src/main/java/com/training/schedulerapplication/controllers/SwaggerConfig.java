//package com.training.schedulerapplication.controllers;
//
//import org.springframework.context.annotation.Bean;
//import org.springframework.context.annotation.Configuration;
//
//import com.google.common.base.Predicate;
//
//import org.springframework.hateoas.client.LinkDiscoverer;
//import org.springframework.hateoas.client.LinkDiscoverers;
//import org.springframework.hateoas.mediatype.collectionjson.CollectionJsonLinkDiscoverer;
//import org.springframework.plugin.core.SimplePluginRegistry;
//import springfox.documentation.builders.ApiInfoBuilder;
//import springfox.documentation.service.ApiInfo;
//import springfox.documentation.spi.DocumentationType;
//import springfox.documentation.spring.web.plugins.Docket;
//import springfox.documentation.swagger2.annotations.EnableSwagger2;
//
//import java.util.ArrayList;
//import java.util.List;
//
//import static springfox.documentation.builders.PathSelectors.regex;
//import static com.google.common.base.Predicates.or;
//
//@Configuration
//@EnableSwagger2
//public class SwaggerConfig {
//
//    @Bean
//    public Docket postsApi() {
//        return new Docket(DocumentationType.SWAGGER_2).groupName("public-api")
//                .apiInfo(apiInfo()).select().paths(postPaths()).build();
//    }
//
//    private Predicate<String> postPaths() {
//        return or(regex("/api/.*"), regex("/api/.*"));
//    }
//
//    private ApiInfo apiInfo() {
//        return new ApiInfoBuilder().title("JavaInUse API")
//                .description("JavaInUse API reference for developers")
//                .termsOfServiceUrl("http://javainuse.com")
//                .contact("javainuse@gmail.com").license("JavaInUse License")
//                .licenseUrl("javainuse@gmail.com").version("1.0").build();
//    }
//
//    @Bean
//    public LinkDiscoverers discoverers() {
//        List<LinkDiscoverer> plugins = new ArrayList<>();
//        plugins.add(new CollectionJsonLinkDiscoverer());
//        return new LinkDiscoverers(SimplePluginRegistry.create(plugins));
//
//    }
//
//}