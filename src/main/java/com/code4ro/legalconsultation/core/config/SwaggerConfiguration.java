package com.code4ro.legalconsultation.core.config;

import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import springfox.documentation.builders.ApiInfoBuilder;
import springfox.documentation.builders.PathSelectors;
import springfox.documentation.builders.RequestHandlerSelectors;
import springfox.documentation.service.ApiInfo;
import springfox.documentation.service.Contact;
import springfox.documentation.spi.DocumentationType;
import springfox.documentation.spring.web.plugins.Docket;
import springfox.documentation.swagger2.annotations.EnableSwagger2;

@Configuration
@EnableSwagger2
public class SwaggerConfiguration {

    @Bean
    public Docket swaggerConfig(){
        return new Docket(DocumentationType.SWAGGER_2)
                .select()
                .apis(RequestHandlerSelectors.basePackage("com.code4ro.legalconsultation"))
                .paths(PathSelectors.any())
                .build()
                .apiInfo(apiInfo());
    }

    private ApiInfo apiInfo() {
        return new ApiInfoBuilder()
                .title("Legal Consultation API")
                .description("API documentation for Legal Consultation project - https://github.com/code4romania/legal-consultation-api")
                .version("1.0")
                .license("Mozilla Public License 2.0")
                .licenseUrl("https://www.mozilla.org/en-US/MPL/2.0/")
                .contact(new Contact("Code 4 Romania", "https://code4.ro/en/","contact@code4.ro"))
                .build();
    }
}
