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

import play.api.libs.json.{Format, Json}
import uk.gov.hmrc.pensionschemereturn.models.etmp.YesNo
import uk.gov.hmrc.pensionschemereturn.models.etmp.nonsipp.common.EtmpIdentityType

import java.time.LocalDate

case class EtmpShares(
  recordVersion: Option[String],
  sponsorEmployerSharesWereHeld: YesNo,
  noOfSponsEmplyrShareTransactions: Option[Int],
  unquotedSharesWereHeld: YesNo,
  noOfUnquotedShareTransactions: Option[Int],
  connectedPartySharesWereHeld: YesNo,
  noOfConnPartyTransactions: Option[Int],
  sponsorEmployerSharesWereDisposed: YesNo,
  unquotedSharesWereDisposed: YesNo,
  connectedPartySharesWereDisposed: YesNo,
  shareTransactions: Option[List[EtmpShareTransaction]],
  totalValueQuotedShares: Double
)

case class EtmpShareTransaction(
  typeOfSharesHeld: String,
  shareIdentification: EtmpShareIdentification,
  heldSharesTransaction: EtmpHeldSharesTransaction,
  disposedSharesTransaction: Option[List[EtmpDisposedSharesTransaction]]
)

case class EtmpShareIdentification(
  nameOfSharesCompany: String,
  crnNumber: Option[String],
  reasonNoCRN: Option[String],
  classOfShares: Option[String]
)

case class EtmpHeldSharesTransaction(
  methodOfHolding: String,
  dateOfAcqOrContrib: Option[LocalDate],
  totalShares: Int,
  acquiredFromName: String,
  acquiredFromType: EtmpIdentityType,
  connectedPartyStatus: Option[String],
  costOfShares: Double,
  supportedByIndepValuation: YesNo,
  totalAssetValue: Option[Double],
  totalDividendsOrReceipts: Double
)

case class EtmpDisposedSharesTransaction(
  methodOfDisposal: String,
  otherMethod: Option[String],
  salesQuestions: Option[EtmpSalesQuestions],
  redemptionQuestions: Option[EtmpRedemptionQuestions],
  totalSharesNowHeld: Int
)

case class EtmpSalesQuestions(
  dateOfSale: LocalDate,
  noOfSharesSold: Int,
  amountReceived: Double,
  nameOfPurchaser: String,
  purchaserType: EtmpIdentityType,
  connectedPartyStatus: String,
  supportedByIndepValuation: YesNo
)

case class EtmpRedemptionQuestions(
  dateOfRedemption: LocalDate,
  noOfSharesRedeemed: Int,
  amountReceived: Double
)

object EtmpShares {
  private implicit val formatEtmpRedemptionQuestions: Format[EtmpRedemptionQuestions] =
    Json.format[EtmpRedemptionQuestions]
  private implicit val formatEtmpSalesQuestions: Format[EtmpSalesQuestions] =
    Json.format[EtmpSalesQuestions]
  private implicit val formatEtmpDisposedSharesTransaction: Format[EtmpDisposedSharesTransaction] =
    Json.format[EtmpDisposedSharesTransaction]
  private implicit val formatEtmpHeldSharesTransaction: Format[EtmpHeldSharesTransaction] =
    Json.format[EtmpHeldSharesTransaction]
  private implicit val formatEtmpShareIdentification: Format[EtmpShareIdentification] =
    Json.format[EtmpShareIdentification]
  private implicit val formatEtmpShareTransaction: Format[EtmpShareTransaction] = Json.format[EtmpShareTransaction]
  implicit val format: Format[EtmpShares] = Json.format[EtmpShares]
}
