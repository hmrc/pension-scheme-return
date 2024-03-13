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

package uk.gov.hmrc.pensionschemereturn.models.nonsipp.assets

import play.api.libs.json.{Json, OFormat}

import java.time.LocalDate

case class Bonds(
  bondsWereAdded: Boolean,
  bondsWereDisposed: Boolean,
  bondTransactions: Seq[BondTransactions]
)

case class BondTransactions(
  nameOfBonds: String,
  methodOfHolding: SchemeHoldBond,
  optDateOfAcqOrContrib: Option[LocalDate],
  costOfBonds: Double,
  optConnectedPartyStatus: Option[Boolean],
  bondsUnregulated: Boolean,
  totalIncomeOrReceipts: Double,
  optBondsDisposed: Option[Seq[BondDisposed]]
)

case class BondDisposed(
  methodOfDisposal: HowDisposed,
  optOtherMethod: Option[String],
  optDateSold: Option[LocalDate],
  optAmountReceived: Option[Double],
  optBondsPurchaserName: Option[String],
  optConnectedPartyStatus: Option[Boolean],
  totalNowHeld: Int
)

object Bonds {
  private implicit val formatBondDisposed: OFormat[BondDisposed] =
    Json.format[BondDisposed]
  private implicit val formatBondTransactions: OFormat[BondTransactions] =
    Json.format[BondTransactions]
  implicit val format: OFormat[Bonds] = Json.format[Bonds]
}
