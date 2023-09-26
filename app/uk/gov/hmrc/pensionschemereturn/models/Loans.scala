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

package uk.gov.hmrc.pensionschemereturn.models

import play.api.libs.json.{Json, Reads}

import java.time.LocalDate

case class LoanPeriod(dateOfLoan: LocalDate, loanTotalSchemeAssets: Double, loanPeriodInMonths: Int)

case class LoanAmountDetails(loanAmount: Double, capRepaymentCY: Double, amountOutstanding: Double)

case class LoanInterestDetails(loanInterestAmount: Double, loanInterestRate: Double, intReceivedCY: Double)

case class RecipientIdentityType(
  identityType: IdentityType,
  idNumber: Option[String],
  reasonNoIdNumber: Option[String],
  otherDescription: Option[String]
)

case class LoanTransactions(
  recipientIdentityType: RecipientIdentityType,
  loanRecipientName: String,
  optConnectedPartyStatus: Option[Boolean],
  optRecipientSponsoringEmployer: Option[String],
  datePeriodLoanDetails: LoanPeriod,
  loanAmountDetails: LoanAmountDetails,
  equalInstallments: Boolean,
  loanInterestDetails: LoanInterestDetails,
  optSecurityGivenDetails: Option[String],
  optOutstandingArrearsOnLoan: Option[Double]
)

case class Loans(schemeHadLoans: Boolean, loanTransactions: Seq[LoanTransactions])

object Loans {
  private implicit val readsLoanInterestDetails: Reads[LoanInterestDetails] = Json.reads[LoanInterestDetails]
  private implicit val readsLoanAmountDetails: Reads[LoanAmountDetails] = Json.reads[LoanAmountDetails]
  private implicit val readsLoanPeriod: Reads[LoanPeriod] = Json.reads[LoanPeriod]
  private implicit val readsRecipientIdentityType: Reads[RecipientIdentityType] = Json.reads[RecipientIdentityType]
  private implicit val readsLoanTransactions: Reads[LoanTransactions] = Json.reads[LoanTransactions]
  implicit val reads: Reads[Loans] = Json.reads[Loans]
}
