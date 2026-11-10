package com.nttdata.users.configuration;

import com.nttdata.users.adapter.in.web.controller.StatusController;
import com.nttdata.users.adapter.in.web.controller.AuthController;
import com.nttdata.users.adapter.in.web.error.GlobalExceptionHandler;
import com.nttdata.users.adapter.in.web.controller.UserController;
import com.nttdata.users.adapter.in.web.mapper.UserWebMapper;
import com.nttdata.users.application.port.in.CreateUserUseCase;
import com.nttdata.users.application.port.in.ManageUserUseCase;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

@Configuration(proxyBeanMethods = false)
public class WebConfiguration {

    @Bean
    public com.nttdata.users.application.port.out.UserCsvReader userCsvReader(jakarta.validation.Validator validator) {
        return new com.nttdata.users.adapter.out.csv.UserCsvReaderAdapter(validator);
    }

    @Bean
    public com.nttdata.users.application.port.in.ImportUsersUseCase importUsersUseCase(
            com.nttdata.users.application.port.out.UserCsvReader reader, CreateUserUseCase cadastro) {
        return new com.nttdata.users.application.service.ImportUsersService(reader, cadastro);
    }

    @Bean
    public io.swagger.v3.oas.models.OpenAPI openAPI() {
        var basic = new io.swagger.v3.oas.models.security.SecurityScheme()
                .type(io.swagger.v3.oas.models.security.SecurityScheme.Type.HTTP)
                .scheme("basic");
        return new io.swagger.v3.oas.models.OpenAPI()
                .components(new io.swagger.v3.oas.models.Components().addSecuritySchemes("basicAuth", basic))
                .addSecurityItem(new io.swagger.v3.oas.models.security.SecurityRequirement().addList("basicAuth"))
                .info(new io.swagger.v3.oas.models.info.Info()
                .title("MS1 — API de usuários").version("v1")
                .description("CRUD e importação de usuários. Autenticação HTTP Basic."));
    }

    @Bean
    public GlobalExceptionHandler globalExceptionHandler() {
        return new GlobalExceptionHandler();
    }

    @Bean
    public StatusController statusController() {
        return new StatusController();
    }

    @Bean
    public AuthController authController() { return new AuthController(); }

    @Bean
    public UserWebMapper userWebMapper() {
        return new UserWebMapper();
    }

    @Bean
    public UserController userController(CreateUserUseCase useCase, ManageUserUseCase manage, UserWebMapper mapper,
                                         com.nttdata.users.application.port.in.ImportUsersUseCase importacao) {
        return new UserController(useCase, manage, mapper, importacao);
    }
}
