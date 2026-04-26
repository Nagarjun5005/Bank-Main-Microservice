package com.nagarjun.Account.service.impl;

import com.nagarjun.Account.constants.AccountsConstants;
import com.nagarjun.Account.dto.AccountsDto;
import com.nagarjun.Account.dto.CustomerDto;
import com.nagarjun.Account.entity.Account;
import com.nagarjun.Account.entity.Customer;
import com.nagarjun.Account.exception.CustomerAlreadyExistsException;
import com.nagarjun.Account.exception.ResourceNotFoundException;
import com.nagarjun.Account.mapper.AccountMapper;
import com.nagarjun.Account.mapper.CustomerMapper;
import com.nagarjun.Account.repository.AccountsRepository;
import com.nagarjun.Account.repository.CustomerRepository;
import com.nagarjun.Account.service.AccountService;
import lombok.AllArgsConstructor;
import org.springframework.stereotype.Service;
import java.time.LocalDateTime;
import java.util.Optional;


@Service
@AllArgsConstructor
public class AccountServiceImpl implements AccountService {

    private AccountsRepository accountsRepository;
    private CustomerRepository customerRepository;

    @Override
    public void createAccount(CustomerDto customerDto) {


        //when u create a account u need to create customer and account
        //in repo u have customer
        Customer customer= CustomerMapper.mapToCustomer(customerDto,new Customer());

        //check if this customer already exists with the same mobile number
       Optional<Customer> mobileNumber = customerRepository.findByMobileNumber(customerDto.getMobileNumber());
        if(mobileNumber.isPresent()){
            throw new CustomerAlreadyExistsException("customer already exists with the registered "+customer.getMobileNumber()+" mobile number");
        }
        Customer savedCustomer = customerRepository.save(customer);
        accountsRepository.save(createNewAccount(savedCustomer));
    }

    @Override
    public CustomerDto fetchAccount(String mobileNumber) {
        //find the customer using findByMobileNumber
        Customer customer = customerRepository.findByMobileNumber(mobileNumber)
                .orElseThrow(() -> new ResourceNotFoundException("Customer", "mobileNumber", mobileNumber));

        // find the account using findByCustomerId
        Account account = accountsRepository.findByCustomerId(customer.getCustomerId())
                .orElseThrow(() -> new ResourceNotFoundException("Account", "customerId", customer.getCustomerId().toString()));

        CustomerDto customerDto = CustomerMapper.mapToCustomerDto(customer, new CustomerDto());
        customerDto.setAccountsDto(AccountMapper.mapToAccountsDto(account, new AccountsDto()));
        return customerDto;
    }

    @Override
    public boolean updateAccount(CustomerDto customerDto) {

        boolean updated =false;

        AccountsDto accountsDto = customerDto.getAccountsDto();
        if(accountsDto!=null){
            Account account1 = accountsRepository.findById(accountsDto.getAccountNumber())
                    .orElseThrow(() -> new ResourceNotFoundException("Account", "AccountNumber", accountsDto.getAccountNumber().toString()));

            Account account = AccountMapper.mapToAccounts(accountsDto,account1);
            Account savedAccount = accountsRepository.save(account);

            Long customerId=savedAccount.getCustomerId();
            Customer customer1 = customerRepository.findById(customerId)
                    .orElseThrow(() -> new ResourceNotFoundException("Customer", "CustomerId", customerId.toString()));

            Customer customer = CustomerMapper.mapToCustomer(customerDto,customer1);
            customerRepository.save(customer);
            updated=true;

        }

        //have to update the customer ----->findByid
        return updated;
    }

    @Override
    public boolean deleteAccountByMobileNumber(String mobileNumber) {


        Customer customer = customerRepository.findByMobileNumber(mobileNumber)
                .orElseThrow(() -> new ResourceNotFoundException("Customer", "mobileNumber", mobileNumber));
        accountsRepository.deleteByCustomerId(customer.getCustomerId());
        customerRepository.deleteById(customer.getCustomerId());
        return true;
    }

    private Account createNewAccount(Customer customer) {
        Account account = new Account();
        account.setCustomerId(customer.getCustomerId());
        account.setAccountNumber(generateAccountNumber());
        account.setAccountType(AccountsConstants.SAVINGS);
        account.setBranchAddress(AccountsConstants.ADDRESS);
        return account;
    }

    private long generateAccountNumber() {
        int year = LocalDateTime.now().getYear(); // 2026
        long sequence = accountsRepository.count() + 1; // 1, 2, 3...
        return Long.parseLong(year + String.format("%08d", sequence));
        // → 202600000001, 202600000002 ...
    }
}
