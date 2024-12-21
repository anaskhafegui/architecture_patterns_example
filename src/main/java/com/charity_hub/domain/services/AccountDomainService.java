package com.charity_hub.domain.services;

import com.charity_hub.domain.exceptions.AppException;
import com.charity_hub.domain.models.account.Account;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * A domain service for account-related domain logic 
 * that doesn't belong strictly inside the Account entity alone.
 */
public class AccountDomainService {
    private final Logger logger = LoggerFactory.getLogger(AccountDomainService.class);
    public Account createNewAccount(
            String mobileNumber, 
            boolean isAdmin, 
            boolean hasInvitation, 
            String deviceType, 
            String deviceId
    ) {
        logger.info("Entering AccountDomainService#createNewAccount for {}", mobileNumber);

        if (!isAdmin && !hasInvitation) {
            logger.warn("Account not invited: {}", mobileNumber);
            throw new AppException.RequirementException("Account not invited to use the App");
        }

        return Account.newAccount(mobileNumber, isAdmin, deviceType, deviceId);
    }
}
