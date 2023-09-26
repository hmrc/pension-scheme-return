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

package uk.gov.hmrc.pensionschemereturn.models.requests.etmp

import play.api.libs.json.{Json, OWrites}

import java.time.LocalDate

case class LoansRequest(
  recordVersion: String,
  schemeHadLoans: String,
  noOfLoans: Int,
  loanTransactions: Seq[LoanTransactionsRequest]
)

case class LoanTransactionsRequest(
  dateOfLoan: LocalDate,
  loanRecipientName: String,
  recipientIdentityType: RecipientIdentityTypeRequest,
  recipientSponsoringEmployer: String,
  connectedPartyStatus: String,
  loanAmount: Double,
  loanInterestAmount: Double,
  loanTotalSchemeAssets: Double,
  loanPeriodInMonths: Int,
  equalInstallments: String,
  loanInterestRate: Double,
  securityGiven: String,
  securityDetails: Option[String],
  capRepaymentCY: Double,
  intReceivedCY: Double,
  arrearsPrevYears: String,
  amountOfArrears: Option[Double],
  amountOutstanding: Double
)

case class RecipientIdentityTypeRequest(
  indivOrOrgType: String,
  idNumber: Option[String],
  reasonNoIdNumber: Option[String],
  otherDescription: Option[String]
)

object LoansRequest {
  private implicit val writesRecipientIdentityTypeRequest: OWrites[RecipientIdentityTypeRequest] =
    Json.writes[RecipientIdentityTypeRequest]
  private implicit val writesLoanTransactionsRequest: OWrites[LoanTransactionsRequest] =
    Json.writes[LoanTransactionsRequest]
  implicit val writes: OWrites[LoansRequest] = Json.writes[LoansRequest]
}
