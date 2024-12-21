package com.charity_hub.application;

import com.charity_hub.domain.contracts.IAccountRepo;
import com.charity_hub.domain.contracts.IAuthProvider;
import com.charity_hub.domain.contracts.IInvitationRepo;
import com.charity_hub.domain.contracts.IJWTGenerator;
import com.charity_hub.domain.models.account.Account;
import com.charity_hub.domain.models.account.Tokens;
import com.charity_hub.domain.exceptions.AppException;
import com.charity_hub.domain.services.AccountDomainService;  // <-- Import domain service
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Service;

import java.util.concurrent.CompletableFuture;

@Service
public class AuthenticateHandler {
    private final IAccountRepo accountRepo;
    private final IInvitationRepo invitationRepo;
    private final IAuthProvider authProvider;
    private final IJWTGenerator jwtGenerator;

    private final Logger logger = LoggerFactory.getLogger(this.getClass().getSimpleName());

    // Domain service instance
    private final AccountDomainService accountDomainService;

    public AuthenticateHandler(
            IAccountRepo accountRepo,
            IInvitationRepo invitationRepo,
            IAuthProvider authProvider,
            IJWTGenerator jwtGenerator
    ) {
        this.accountRepo = accountRepo;
        this.invitationRepo = invitationRepo;
        this.authProvider = authProvider;
        this.jwtGenerator = jwtGenerator;

        // Domain service has no external dependencies,
        // so we can instantiate it directly
        this.accountDomainService = new AccountDomainService();
    }

    public CompletableFuture<AuthenticateResponse> handle(Authenticate command) {
        return CompletableFuture.supplyAsync(() -> {
            logger.info("Handling authentication for idToken: {}", command.idToken());

            String mobileNumber = authProvider.getVerifiedMobileNumber(command.idToken()).join();
            logger.info("Verified mobile number: {}", mobileNumber);

            Account account = existingAccountOrNewAccount(mobileNumber, command);

            Tokens tokens = account.authenticate(jwtGenerator, command.deviceId(), command.deviceType());

            accountRepo.save(account);
            logger.info("Authentication successful for account: {}", account.getMobileNumber());

            return new AuthenticateResponse(tokens.accessToken(), tokens.refreshToken());
        });
    }

    private Account existingAccountOrNewAccount(String mobileNumber, Authenticate request) {
        logger.info("Attempting to find account by mobile number: {}", mobileNumber);

        var account = accountRepo.getByMobileNumber(mobileNumber).join();
        if (accountExists(account)) {
            return account;
        }

        return createNewAccount(mobileNumber, request.deviceType(), request.deviceId());
    }

    private static boolean accountExists(Account existingAccount) {
        return existingAccount != null;
    }

    private Account createNewAccount(String mobileNumber, String deviceType, String deviceId) {
        boolean isAdmin = accountRepo.isAdmin(mobileNumber).join();
        boolean hasInvitation = invitationRepo.hasInvitation(mobileNumber).join();

        return accountDomainService.createNewAccount(
            mobileNumber, 
            isAdmin, 
            hasInvitation, 
            deviceType, 
            deviceId
        );
    }
}
