package com.loans.service.impl;


import com.loans.constants.LoanConstants;
import com.loans.dto.LoanDto;
import com.loans.entity.Loan;
import com.loans.exception.LoanAlreadyExistsException;
import com.loans.exception.ResourceNotFoundException;
import com.loans.mapper.LoanMapper;
import com.loans.repository.LoanRepository;
import com.loans.service.LoanService;
import lombok.AllArgsConstructor;
import org.springframework.stereotype.Service;


import java.util.Optional;
import java.util.Random;

@Service
@AllArgsConstructor
public class LoanServiceImpl implements LoanService {

   private LoanRepository loanRepository;

    @Override
    public void createLoan(String mobileNumber) {

        //LoanAlreadyExistsException
        Optional<Loan> loanExistsByNumber = loanRepository.findByMobileNumber(mobileNumber);
        if(loanExistsByNumber.isPresent()){
            throw new LoanAlreadyExistsException("Loan Already registered with given mobile number "+mobileNumber);
        }

        loanRepository.save(createNewLoan(mobileNumber));
    }

    private Loan createNewLoan(String mobileNumber) {
        Loan newLoan = new Loan();
        long randomLoanNumber = 100000000000L + new Random().nextInt(900000000);
        newLoan.setLoanNumber(Long.toString(randomLoanNumber));
        newLoan.setMobileNumber(mobileNumber);
        newLoan.setLoanType(LoanConstants.HOME_LOAN);
        newLoan.setTotalLoan(LoanConstants.NEW_LOAN_LIMIT);
        newLoan.setAmountPaid(0);
        newLoan.setOutstandingAmount(LoanConstants.NEW_LOAN_LIMIT);
        return newLoan;
    }


    @Override
    public LoanDto fetchLoan(String mobileNumber) {
        Loan loanFound = loanRepository.findByMobileNumber(mobileNumber)
                .orElseThrow(() -> new ResourceNotFoundException("Loan", "mobileNumber", mobileNumber));

        LoanDto loanDto = LoanMapper.mapToLoansDto(loanFound, new LoanDto());
        return loanDto;

    }

    @Override
    public boolean updateLoan(LoanDto loansDto) {
        String mobileNumber = loansDto.getMobileNumber();
        Loan loanFound = loanRepository.findByMobileNumber(mobileNumber)
                .orElseThrow(() -> new ResourceNotFoundException("Loan", "mobileNumber", mobileNumber));
            Loan updatedLoan = LoanMapper.mapToLoans(loansDto,loanFound);
            loanRepository.save(updatedLoan);
        return true;
    }



    @Override
    public boolean deleteLoan(String mobileNumber) {
        Loan loanFound = loanRepository.findByMobileNumber(mobileNumber)
                .orElseThrow(() -> new ResourceNotFoundException("Loan", "mobileNumber", mobileNumber));

         loanRepository.deleteById(loanFound.getLoanId());
        return true;
    }
}
