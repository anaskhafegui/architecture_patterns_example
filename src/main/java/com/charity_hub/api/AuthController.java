package com.charity_hub.api;

import com.charity_hub.api.common.DeferredResults;
import com.charity_hub.application.Authenticate;
import com.charity_hub.application.AuthenticateHandler;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.context.request.async.DeferredResult;

@RestController
public class AuthController {
    private final AuthenticateHandler authenticateHandler;
    private final Logger log = LoggerFactory.getLogger(this.getClass().getSimpleName());

    public AuthController(AuthenticateHandler authenticateHandler) {
        this.authenticateHandler = authenticateHandler;
    }

    @PostMapping("/v1/accounts/authenticate")
    public DeferredResult<ResponseEntity<?>> login(@RequestBody Authenticate authenticate) {
        log.info("Processing authentication request");
        return DeferredResults.from(
                authenticateHandler.handle(authenticate)
                        .thenApply(ResponseEntity::ok)
        );
    }
}

