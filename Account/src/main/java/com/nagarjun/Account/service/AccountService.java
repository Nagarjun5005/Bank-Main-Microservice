package com.nagarjun.Account.service;

import com.nagarjun.Account.dto.CustomerDto;

public interface AccountService {

     void createAccount(CustomerDto customerDto);

    CustomerDto fetchAccount(String mobileNumber);

    boolean updateAccount(CustomerDto customerDto);

    boolean deleteAccountByMobileNumber(String mobileNumber );
}
