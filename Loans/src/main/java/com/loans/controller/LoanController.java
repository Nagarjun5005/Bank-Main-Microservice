package com.loans.controller;


import com.loans.constants.LoanConstants;
import com.loans.dto.LoanDto;
import com.loans.dto.ResponseDto;
import com.loans.service.LoanService;
import jakarta.validation.Valid;
import jakarta.validation.constraints.Pattern;
import lombok.AllArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Controller;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.*;

@Controller
@RequestMapping(path="/api",produces = {
        MediaType.APPLICATION_JSON_VALUE
})
@AllArgsConstructor
@Validated
public class LoanController {


    private LoanService loanService;

    @PostMapping("/create")
    public ResponseEntity<ResponseDto> createLoan(@RequestParam
                                                      @Pattern(regexp="(^$|[0-9]{10})",message = "Mobile number must be 10 digits")
                                                  String mobileNumber) {
        loanService.createLoan(mobileNumber);
        return ResponseEntity
                .status(HttpStatus.CREATED)
                .body(new ResponseDto(LoanConstants.STATUS_201, LoanConstants.MESSAGE_201));
    }


    @GetMapping("/fetch")
    public ResponseEntity<LoanDto>fetchLoanDetails(@RequestParam
                                                       @Pattern(regexp="(^$|[0-9]{10})",message = "Mobile number must be 10 digits")
                                                       String mobileNumber){
        LoanDto loanDto = loanService.fetchLoan(mobileNumber);
        return new ResponseEntity<>(loanDto,HttpStatus.OK);
    }


    @PutMapping("/update")
    public ResponseEntity<ResponseDto>updateLoanDetails(@Valid @RequestBody LoanDto loanDto){
        boolean updated = loanService.updateLoan(loanDto);
        if (updated){
            return ResponseEntity.status(HttpStatus.OK)
                    .body(new ResponseDto(LoanConstants.STATUS_200,LoanConstants.MESSAGE_200));
        }else{
            return ResponseEntity.status(HttpStatus.EXPECTATION_FAILED)
                    .body(new ResponseDto(LoanConstants.STATUS_417,LoanConstants.MESSAGE_417_UPDATE));
        }

    }

    @DeleteMapping("/delete")
    public ResponseEntity<ResponseDto>deleteLoanByMobileNumber(@RequestParam
                                                                   @Pattern(regexp="(^$|[0-9]{10})",message = "Mobile number must be 10 digits")
                                                                   String mobileNumber){
        boolean deleted = loanService.deleteLoan(mobileNumber);
        if (deleted){
            return ResponseEntity.status(HttpStatus.OK)
                    .body(new ResponseDto(LoanConstants.STATUS_200,LoanConstants.MESSAGE_200));
        }else {
            return ResponseEntity.status(HttpStatus.EXPECTATION_FAILED)
                    .body(new ResponseDto(LoanConstants.STATUS_417,LoanConstants.MESSAGE_417_DELETE));
        }
    }


}
