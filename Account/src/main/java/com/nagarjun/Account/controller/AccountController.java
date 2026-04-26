package com.nagarjun.Account.controller;


import com.nagarjun.Account.constants.AccountsConstants;
import com.nagarjun.Account.dto.CustomerDto;
import com.nagarjun.Account.dto.ResponseDto;
import com.nagarjun.Account.service.AccountService;
import jakarta.validation.Valid;
import jakarta.validation.constraints.Pattern;
import lombok.AllArgsConstructor;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("api")
@AllArgsConstructor
@Validated
public class AccountController {


    private final AccountService accountService;

       //create account
    @PostMapping("/create")
    public ResponseEntity<ResponseDto>createAccount(@Valid @RequestBody CustomerDto customerDto){
         accountService.createAccount(customerDto);
        return ResponseEntity.status(HttpStatus.CREATED).body(new ResponseDto(AccountsConstants.STATUS_201,AccountsConstants.MESSAGE_201));
    }


    @GetMapping("/fetch")
    public ResponseEntity<CustomerDto>fetchAccountDetails(@RequestParam
                                                              @Pattern(regexp = "(^$|[0-9]{10})", message = "Mobile number must be 10 digits")
                                                              String mobileNumber){

       CustomerDto customerDto= accountService.fetchAccount(mobileNumber);
        return ResponseEntity.status(HttpStatus.FOUND).body(customerDto);

    }


    @PutMapping("/update")
    public ResponseEntity<ResponseDto>updateAccountDetails(@Valid @RequestBody CustomerDto customerDto){

        boolean updateAccount = accountService.updateAccount(customerDto);
        if(updateAccount){
            return ResponseEntity.status(HttpStatus.OK).body(new ResponseDto(AccountsConstants.STATUS_200,AccountsConstants.MESSAGE_200));
        }else{
            return  ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body(new ResponseDto(AccountsConstants.STATUS_417,AccountsConstants.MESSAGE_417_UPDATE));
        }
    }


    @DeleteMapping("/delete")
    public ResponseEntity<ResponseDto>deleteAccount(@RequestParam
                                                        @Pattern(regexp = "(^$|[0-9]{10})", message = "Mobile number must be 10 digits")
                                                        String mobileNumber){


        boolean deleted = accountService.deleteAccountByMobileNumber(mobileNumber);
        if (deleted){
            return ResponseEntity
                    .status(HttpStatus.OK)
                    .body(new ResponseDto(AccountsConstants.STATUS_200,AccountsConstants.MESSAGE_200));
        }else {
            return  ResponseEntity
                    .status(HttpStatus.INTERNAL_SERVER_ERROR)
                    .body(new ResponseDto(AccountsConstants.STATUS_417,AccountsConstants.MESSAGE_417_DELETE));
        }
    }


}
