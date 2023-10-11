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

package uk.gov.hmrc.pensionschemereturn.transformations.nonsipp

import com.google.inject.{Inject, Singleton}
import uk.gov.hmrc.pensionschemereturn.models.etmp.nonsipp.{EtmpIdentityType, EtmpLoanTransactions, EtmpLoans}
import uk.gov.hmrc.pensionschemereturn.models.nonsipp.IdentityType.identityTypeToString
import uk.gov.hmrc.pensionschemereturn.models.nonsipp.{Loans, RecipientIdentityType}
import uk.gov.hmrc.pensionschemereturn.transformations.Transformer

@Singleton()
class LoansToEtmp @Inject()() extends Transformer {

  def transform(loans: Loans): EtmpLoans =
    EtmpLoans(
      recordVersion = "001", // TODO hardcoded for now
      schemeHadLoans = toYesNo(loans.schemeHadLoans),
      noOfLoans = loans.loanTransactions.size,
      loanTransactions = loans.loanTransactions.map { loanTransaction =>
        EtmpLoanTransactions(
          dateOfLoan = loanTransaction.datePeriodLoanDetails.dateOfLoan,
          loanRecipientName = loanTransaction.loanRecipientName,
          recipientIdentityType = buildEtmpIdentityType(loanTransaction.recipientIdentityType),
          recipientSponsoringEmployer = toYesNo(loanTransaction.optRecipientSponsoringEmployer.contains(Sponsoring)),
          connectedPartyStatus = if (loanTransaction.connectedPartyStatus) "01" else "02",
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

  private def buildEtmpIdentityType(
    recipientIdentityType: RecipientIdentityType
  ): EtmpIdentityType =
    EtmpIdentityType(
      indivOrOrgType = identityTypeToString(recipientIdentityType.identityType),
      idNumber = recipientIdentityType.idNumber,
      reasonNoIdNumber = recipientIdentityType.reasonNoIdNumber,
      otherDescription = recipientIdentityType.otherDescription
    )
}
