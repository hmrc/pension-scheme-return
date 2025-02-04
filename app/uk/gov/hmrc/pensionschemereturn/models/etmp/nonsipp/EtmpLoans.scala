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

package uk.gov.hmrc.pensionschemereturn.models.etmp.nonsipp

import uk.gov.hmrc.pensionschemereturn.models.etmp.nonsipp.common.EtmpIdentityType
import uk.gov.hmrc.pensionschemereturn.models.etmp.YesNo
import play.api.libs.json.{Json, OFormat}

import java.time.LocalDate

case class EtmpLoans(
  recordVersion: Option[String],
  schemeHadLoans: Option[String],
  noOfLoans: Option[Int],
  loanTransactions: Option[Seq[EtmpLoanTransactions]]
)

case class EtmpLoanTransactions(
  prePopulated: Option[YesNo],
  dateOfLoan: LocalDate,
  loanRecipientName: String,
  recipientIdentityType: EtmpIdentityType,
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
  capRepaymentCY: Option[Double],
  intReceivedCY: Option[Double],
  arrearsPrevYears: Option[String],
  amountOfArrears: Option[Double],
  amountOutstanding: Option[Double]
)

object EtmpLoans {
  private implicit val formatsLoanTransactions: OFormat[EtmpLoanTransactions] =
    Json.format[EtmpLoanTransactions]
  implicit val formats: OFormat[EtmpLoans] = Json.format[EtmpLoans]
}
