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
class LoansToEtmp @Inject()() extends Transformer {

  def transform(loans: Loans): LoansRequest =
    LoansRequest(
      recordVersion = "001", // TODO hardcoded for now
      schemeHadLoans = toYesNo(loans.schemeHadLoans),
      noOfLoans = loans.loanTransactions.size,
      loanTransactions = loans.loanTransactions.map { loanTransaction =>
        LoanTransactionsRequest(
          dateOfLoan = loanTransaction.datePeriodLoanDetails.dateOfLoan,
          loanRecipientName = loanTransaction.loanRecipientName,
          recipientIdentityType = buildRecipientIdentityTypeRequest(loanTransaction.recipientIdentityType),
          recipientSponsoringEmployer = toYesNo(loanTransaction.optRecipientSponsoringEmployer.contains("sponsoring")),
          connectedPartyStatus = if (loanTransaction.optConnectedPartyStatus.getOrElse(false)) "01" else "02",
          loanAmount = loanTransaction.loanAmountDetails.loanAmount,
          loanInterestAmount = loanTransaction.loanInterestDetails.loanInterestAmount,
          loanTotalSchemeAssets = loanTransaction.datePeriodLoanDetails.loanTotalSchemeAssets,
          loanPeriodInMonths = loanTransaction.datePeriodLoanDetails.loanPeriodInMonths,
          equalInstallments = toYesNo(loanTransaction.equalInstallments),
          loanInterestRate = loanTransaction.loanInterestDetails.loanInterestRate,
          securityGiven = optToYesNo(loanTransaction.optSecurityGivenDetails),
          securityDetails = loanTransaction.optSecurityGivenDetails,
          capRepaymentCY = loanTransaction.loanAmountDetails.capRepaymentCY,
          intReceivedCY = loanTransaction.loanInterestDetails.intReceivedCY,
          arrearsPrevYears = optToYesNo(loanTransaction.optOutstandingArrearsOnLoan),
          amountOfArrears = loanTransaction.optOutstandingArrearsOnLoan,
          amountOutstanding = loanTransaction.loanAmountDetails.amountOutstanding
        )
      }
    )

  private def buildRecipientIdentityTypeRequest(
    recipientIdentityType: RecipientIdentityType
  ): RecipientIdentityTypeRequest =
    RecipientIdentityTypeRequest(
      indivOrOrgType = getIdentityTypeAsString(recipientIdentityType.identityType),
      idNumber = recipientIdentityType.idNumber,
      reasonNoIdNumber = recipientIdentityType.reasonNoIdNumber,
      otherDescription = recipientIdentityType.otherDescription
    )
}
