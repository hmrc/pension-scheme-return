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

import uk.gov.hmrc.pensionschemereturn.models.nonsipp.PropertyAcquiredFrom
import play.api.libs.json.{Json, OFormat}

import java.time.LocalDate

case class LandOrProperty(
  recordVersion: Option[String],
  landOrPropertyHeld: Boolean,
  disposeAnyLandOrProperty: Boolean,
  landOrPropertyTransactions: Seq[LandOrPropertyTransactions]
)

case class LandOrPropertyTransactions(
  propertyDetails: PropertyDetails,
  heldPropertyTransaction: HeldPropertyTransaction,
  optDisposedPropertyTransaction: Option[Seq[DisposedPropertyTransaction]]
)

case class PropertyDetails(
  landOrPropertyInUK: Boolean,
  addressDetails: Address,
  landRegistryTitleNumberKey: Boolean,
  landRegistryTitleNumberValue: String
)

case class HeldPropertyTransaction(
  methodOfHolding: SchemeHoldLandProperty,
  dateOfAcquisitionOrContribution: Option[LocalDate],
  optPropertyAcquiredFromName: Option[String],
  optPropertyAcquiredFrom: Option[PropertyAcquiredFrom],
  optConnectedPartyStatus: Option[Boolean],
  totalCostOfLandOrProperty: Double,
  optIndepValuationSupport: Option[Boolean],
  isLandOrPropertyResidential: Boolean,
  optLeaseDetails: Option[LeaseDetails],
  landOrPropertyLeased: Boolean,
  totalIncomeOrReceipts: Double
)

case class DisposedPropertyTransaction(
  methodOfDisposal: HowDisposed,
  optOtherMethod: Option[String],
  optDateOfSale: Option[LocalDate],
  optNameOfPurchaser: Option[String],
  optPropertyAcquiredFrom: Option[PropertyAcquiredFrom],
  optSaleProceeds: Option[Double],
  optConnectedPartyStatus: Option[Boolean],
  optIndepValuationSupport: Option[Boolean],
  portionStillHeld: Boolean
)

case class LeaseDetails(
  lesseeName: String,
  leaseGrantDate: LocalDate,
  annualLeaseAmount: Double,
  connectedPartyStatus: Boolean
)

object LandOrProperty {
  private implicit val formatLeaseDetails: OFormat[LeaseDetails] = Json.format[LeaseDetails]
  private implicit val formatHeldPropertyTransaction: OFormat[HeldPropertyTransaction] =
    Json.format[HeldPropertyTransaction]
  private implicit val formatDisposedPropertyTransaction: OFormat[DisposedPropertyTransaction] =
    Json.format[DisposedPropertyTransaction]
  private implicit val formatPropertyDetails: OFormat[PropertyDetails] = Json.format[PropertyDetails]
  private implicit val formatLandOrPropertyTransactions: OFormat[LandOrPropertyTransactions] =
    Json.format[LandOrPropertyTransactions]
  implicit val format: OFormat[LandOrProperty] = Json.format[LandOrProperty]
}
