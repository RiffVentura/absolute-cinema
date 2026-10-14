package com.rventura.abscinema.user.adapters.in;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.ApplicationArguments;
import org.springframework.boot.ApplicationRunner;
import org.springframework.stereotype.Component;
import com.rventura.abscinema.user.application.DefaultAdminUserInitialzer;

@Component
public class InitDefaultAdminAppRunner implements ApplicationRunner {

    @Value("${app.default.admin}")
    private String adminUsername;
    @Value("${app.default.admin-password}")
    private String adminPassword;

    private final DefaultAdminUserInitialzer defaultAdminUserInitialzer;

    public InitDefaultAdminAppRunner(DefaultAdminUserInitialzer defaultAdminUserInitialzer) {
        this.defaultAdminUserInitialzer = defaultAdminUserInitialzer;
    }

    @Override
    public void run(ApplicationArguments args) {
        this.defaultAdminUserInitialzer.init(this.adminUsername, this.adminPassword);
    }
}
