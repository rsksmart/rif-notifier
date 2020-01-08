package org.rif.notifier.boot.configuration;

import org.rif.notifier.constants.ControllerConstants;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import springfox.documentation.builders.ApiInfoBuilder;
import springfox.documentation.builders.PathSelectors;
import springfox.documentation.builders.RequestHandlerSelectors;
import springfox.documentation.service.ApiInfo;
import springfox.documentation.service.Tag;
import springfox.documentation.spi.DocumentationType;
import springfox.documentation.spring.web.plugins.Docket;
import springfox.documentation.swagger2.annotations.EnableSwagger2;

import java.util.HashSet;
import java.util.Set;

@Configuration
@EnableSwagger2
public class SwaggerConfig {

    //Docket configuration

    private final String BASE_PACKAGE_REQUEST_SELECTORS_PATH = "org.rif.notifier.controllers";

    // Tags information for resources



    //Api endpoints general information

    private final String API_ENDPOINTS_TITLE = "Spring Boot REST API";
    private final String API_ENDPOINTS_DESCRIPTION = "RIF Notifier Management REST API";
    private final String API_ENDPOINTS_LICENSE = "Apache 2.0";
    private final String API_ENDPOINTS_LICENSE_URL = "http://www.apache.org/licenses/LICENSE-2.0.html";
    private final String API_ENDPOINTS_VERSION = "1.0.0";

    @Bean
    public Docket api() {

        Set<String> consumes = new HashSet<>();
        consumes.add(ControllerConstants.CONTENT_TYPE_APPLICATION_JSON);

        Set<String> produces = new HashSet<>();
        consumes.add(ControllerConstants.CONTENT_TYPE_APPLICATION_JSON);

        return new Docket(DocumentationType.SWAGGER_2)
                .produces(produces)
                .consumes(consumes)
                .select()
                .apis(RequestHandlerSelectors.basePackage(BASE_PACKAGE_REQUEST_SELECTORS_PATH))
                .paths(PathSelectors.any())
                .build().apiInfo(apiEndPointsInfo()).tags(new Tag("Test", "Test"));
    }

    private ApiInfo apiEndPointsInfo() {
        return new ApiInfoBuilder().title(API_ENDPOINTS_TITLE)
                .description(API_ENDPOINTS_DESCRIPTION)
                .license(API_ENDPOINTS_LICENSE)
                .licenseUrl(API_ENDPOINTS_LICENSE_URL)
                .version(API_ENDPOINTS_VERSION)
                .build();
    }

}
