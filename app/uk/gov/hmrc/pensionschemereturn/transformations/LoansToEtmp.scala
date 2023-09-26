/*
 * Copyright 2023 HM Revenue & Customs
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *     http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package uk.gov.hmrc.pensionschemereturn.transformations

import com.google.inject.{Inject, Singleton}
import uk.gov.hmrc.pensionschemereturn.models.IdentityType.getIdentityTypeAsString
import uk.gov.hmrc.pensionschemereturn.models._
import uk.gov.hmrc.pensionschemereturn.models.requests.etmp._

@Singleton()
class LoansToEtmp @Inject()() {

  def transform(loans: Loans): LoansRequest =
    LoansRequest(
      recordVersion = "001", // TODO hardcoded for now
      schemeHadLoans = if (loans.schemeHadLoans) "Yes" else "No",
      noOfLoans = loans.loanTransactions.size,
      loanTransactions = loans.loanTransactions.map { loanTransaction =>
        LoanTransactionsRequest(
          dateOfLoan = loanTransaction.datePeriodLoanDetails.dateOfLoan,
          loanRecipientName = loanTransaction.loanRecipientName,
          recipientIdentityType = buildRecipientIdentityTypeRequest(loanTransaction.recipientIdentityType),
          recipientSponsoringEmployer =
            if (loanTransaction.optRecipientSponsoringEmployer.contains("sponsoring")) "Yes" else "No",
          connectedPartyStatus = if (loanTransaction.optConnectedPartyStatus.getOrElse(false)) "01" else "02",
          loanAmount = loanTransaction.loanAmountDetails.loanAmount,
          loanInterestAmount = loanTransaction.loanInterestDetails.loanInterestAmount,
          loanTotalSchemeAssets = loanTransaction.datePeriodLoanDetails.loanTotalSchemeAssets,
          loanPeriodInMonths = loanTransaction.datePeriodLoanDetails.loanPeriodInMonths,
          equalInstallments = if (loanTransaction.equalInstallments) "Yes" else "No",
          loanInterestRate = loanTransaction.loanInterestDetails.loanInterestRate,
          securityGiven = loanTransaction.optSecurityGivenDetails.map(_ => "Yes").getOrElse("No"),
          securityDetails = loanTransaction.optSecurityGivenDetails,
          capRepaymentCY = loanTransaction.loanAmountDetails.capRepaymentCY,
          intReceivedCY = loanTransaction.loanInterestDetails.intReceivedCY,
          arrearsPrevYears = loanTransaction.optOutstandingArrearsOnLoan.map(_ => "Yes").getOrElse("No"),
          amountOfArrears = loanTransaction.optOutstandingArrearsOnLoan,
          amountOutstanding = loanTransaction.loanAmountDetails.amountOutstanding
        )
      }
    )

  private def buildRecipientIdentityTypeRequest(recipientIdentityType: RecipientIdentityType): RecipientIdentityTypeRequest =
    RecipientIdentityTypeRequest(
      indivOrOrgType = getIdentityTypeAsString(recipientIdentityType.identityType),
      idNumber = recipientIdentityType.idNumber,
      reasonNoIdNumber = recipientIdentityType.reasonNoIdNumber,
      otherDescription = recipientIdentityType.otherDescription
    )
}
