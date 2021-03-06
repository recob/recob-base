package com.recob.starter;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.Setter;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.context.event.ApplicationStartedEvent;
import org.springframework.context.ApplicationListener;
import org.springframework.core.env.Environment;
import org.springframework.web.reactive.function.BodyInserters;
import org.springframework.web.reactive.function.client.WebClient;

import java.util.Objects;

@Slf4j
@Setter(onMethod = @__(@Autowired))
public class RegisterApplicationListener implements ApplicationListener<ApplicationStartedEvent> {

    private final String PROTOCOL = "http://";
    private final String ENDPOINT = "/register";

    private Environment environment;

    @Override
    public void onApplicationEvent(ApplicationStartedEvent applicationStartedEvent) {
        String managerHost = environment.getProperty("MANAGER_SERVICE_HOST");
        Long port = Long.valueOf(Objects.requireNonNull(environment.getProperty("local.server.port")));
        String applicationUUID = environment.getProperty("RECOB_UUID");

        registerApplication(managerHost, port, applicationUUID);
    }

    private void registerApplication(String managerHost, Long port, String applicationUUID) {
        System.out.println("manager host " + managerHost);
        System.out.println("port " + port);

        RegisterApplicationBody body = new RegisterApplicationBody(port, applicationUUID);

        System.out.println("sending start application on {} with port {} and appUuid {}");
        WebClient.builder()
                .baseUrl(buildManagerURI(managerHost))
                .build()
                .post()
                .body(BodyInserters.fromObject(body))
                .exchange()
                .subscribe(r -> {
                    log.info(r.statusCode().toString());
                    if (!r.statusCode().is2xxSuccessful()) {
                        System.out.println("[registerApplication] can't register application");
//                        System.exit(0);
                    }

                    r.bodyToMono(String.class)
                            .subscribe(System.out::println);
                });
    }

    private String buildManagerURI(String managerHost) {
        return PROTOCOL + managerHost + ENDPOINT;
    }

    @Data
    @AllArgsConstructor
    private class RegisterApplicationBody {
        private Long port;
        private String uuid;
    }
}
