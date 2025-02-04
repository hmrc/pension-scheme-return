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

package uk.gov.hmrc.pensionschemereturn.models.etmp.nonsipp.assets

import uk.gov.hmrc.pensionschemereturn.models.etmp.YesNo
import play.api.libs.json.{Json, OFormat}

import java.time.LocalDate

case class EtmpBonds(
  recordVersion: Option[String],
  bondsWereAdded: Option[String],
  bondsWereDisposed: Option[String],
  noOfTransactions: Option[Int],
  bondTransactions: Option[Seq[EtmpBondTransactions]]
)

case class EtmpBondTransactions(
  prePopulated: Option[YesNo],
  nameOfBonds: String,
  methodOfHolding: String,
  dateOfAcqOrContrib: Option[LocalDate],
  costOfBonds: Double,
  connectedPartyStatus: Option[String],
  bondsUnregulated: String,
  totalIncomeOrReceipts: Option[Double],
  bondsDisposed: Option[Seq[EtmpBondsDisposed]]
)

case class EtmpBondsDisposed(
  methodOfDisposal: String,
  otherMethod: Option[String],
  dateSold: Option[LocalDate],
  amountReceived: Option[Double],
  bondsPurchaserName: Option[String],
  connectedPartyStatus: Option[String],
  totalNowHeld: Int
)

object EtmpBonds {
  private implicit val formatsEtmpBondsDisposed: OFormat[EtmpBondsDisposed] =
    Json.format[EtmpBondsDisposed]
  private implicit val formatsEtmpBondTransactions: OFormat[EtmpBondTransactions] =
    Json.format[EtmpBondTransactions]
  implicit val formats: OFormat[EtmpBonds] =
    Json.format[EtmpBonds]
}
