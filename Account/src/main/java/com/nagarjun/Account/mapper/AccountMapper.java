package com.nagarjun.Account.mapper;

import com.nagarjun.Account.dto.AccountsDto;
import com.nagarjun.Account.entity.Account;

public class AccountMapper {


//    public static Account mapToAccount ( AccountsDto accountsDto ) {
//        Account account = new Account ( );
//        account.setAccountNumber ( accountsDto.getAccountNumber ( ) );
//        account.setAccountType ( accountsDto.getAccountType ( ) );
//        account.setBranchAddress ( accountsDto.getBranchAddress ( ) );
//        return account;
//    }
//
//    public static AccountsDto mapToAccountsDto ( Account account ) {
//        AccountsDto accountsDto = new AccountsDto ( );
//        accountsDto.setAccountNumber ( account.getAccountNumber ( ) );
//        accountsDto.setAccountType ( account.getAccountType ( ) );
//        accountsDto.setBranchAddress ( account.getBranchAddress ( ) );
//        return accountsDto;
//    }


    public static AccountsDto mapToAccountsDto ( Account accounts, AccountsDto accountsDto ) {
        accountsDto.setAccountNumber ( accounts.getAccountNumber ( ) );
        accountsDto.setAccountType ( accounts.getAccountType ( ) );
        accountsDto.setBranchAddress ( accounts.getBranchAddress ( ) );
        return accountsDto;
    }

    public static Account mapToAccounts ( AccountsDto accountsDto, Account accounts ) {
        accounts.setAccountNumber ( accountsDto.getAccountNumber ( ) );
        accounts.setAccountType ( accountsDto.getAccountType ( ) );
        accounts.setBranchAddress ( accountsDto.getBranchAddress ( ) );
        return accounts;
    }

}
