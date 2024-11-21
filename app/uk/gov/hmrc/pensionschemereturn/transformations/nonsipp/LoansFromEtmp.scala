/*
 * Copyright 2024 HM Revenue & Customs
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

import uk.gov.hmrc.pensionschemereturn.models.nonsipp.IdentityType.{stringToIdentityType, Individual}
import com.google.inject.{Inject, Singleton}
import uk.gov.hmrc.pensionschemereturn.models.nonsipp._
import uk.gov.hmrc.pensionschemereturn.models.etmp.nonsipp.EtmpLoans
import uk.gov.hmrc.pensionschemereturn.transformations.Transformer

@Singleton()
class LoansFromEtmp @Inject() extends Transformer {

  def transform(loans: EtmpLoans): Loans =
    Loans(
      recordVersion = loans.recordVersion,
      schemeHadLoans = fromYesNo(loans.schemeHadLoans),
      loanTransactions = loans.loanTransactions
        .getOrElse(Seq.empty)
        .map { loanTransaction =>
          val connectedPartyStatus = loanTransaction.connectedPartyStatus == Connected
          val identityType = stringToIdentityType(loanTransaction.recipientIdentityType.indivOrOrgType)
          LoanTransactions(
            recipientIdentityType = RecipientIdentityType(
              identityType = identityType,
              idNumber = loanTransaction.recipientIdentityType.idNumber,
              reasonNoIdNumber = loanTransaction.recipientIdentityType.reasonNoIdNumber,
              otherDescription = loanTransaction.recipientIdentityType.otherDescription
            ),
            loanRecipientName = loanTransaction.loanRecipientName,
            connectedPartyStatus = connectedPartyStatus,
            optRecipientSponsoringEmployer = Option.when(identityType != Individual)(
              if (loanTransaction.recipientSponsoringEmployer == Yes) Sponsoring
              else {
                if (connectedPartyStatus) ConnectedParty else Neither
              }
            ),
            datePeriodLoanDetails = LoanPeriod(
              dateOfLoan = loanTransaction.dateOfLoan,
              loanTotalSchemeAssets = loanTransaction.loanTotalSchemeAssets,
              loanPeriodInMonths = loanTransaction.loanPeriodInMonths
            ),
            loanAmountDetails = LoanAmountDetails(
              loanAmount = loanTransaction.loanAmount,
              capRepaymentCY = loanTransaction.capRepaymentCY,
              amountOutstanding = loanTransaction.amountOutstanding
            ),
            equalInstallments = fromYesNo(loanTransaction.equalInstallments),
            loanInterestDetails = LoanInterestDetails(
              loanInterestAmount = loanTransaction.loanInterestAmount,
              loanInterestRate = loanTransaction.loanInterestRate,
              intReceivedCY = loanTransaction.intReceivedCY
            ),
            optSecurityGivenDetails = loanTransaction.securityDetails,
            optOutstandingArrearsOnLoan = loanTransaction.amountOfArrears
          )
        }
    )
}
