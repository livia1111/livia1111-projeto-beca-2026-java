package com.nttdata.users;

import com.nttdata.users.configuration.PasswordConfiguration;
import com.nttdata.users.configuration.WebConfiguration;
import com.nttdata.users.configuration.UserConfiguration;
import com.nttdata.users.configuration.SecurityConfiguration;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.SpringBootConfiguration;
import org.springframework.boot.autoconfigure.EnableAutoConfiguration;
import org.springframework.context.annotation.Import;

@SpringBootConfiguration
@EnableAutoConfiguration
@Import({WebConfiguration.class, PasswordConfiguration.class, UserConfiguration.class, SecurityConfiguration.class})
public class Ms1Application {

    public static void main(String[] args) {
        SpringApplication.run(Ms1Application.class, args);
    }
}
