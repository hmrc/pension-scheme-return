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

package uk.gov.hmrc.pensionschemereturn.models.nonsipp

import play.api.libs.json.{JsString, Json, OFormat, Writes}

import java.time.LocalDate

case class Assets(landOrProperty: LandOrProperty)
case class LandOrProperty(
  landOrPropertyHeld: Boolean,
  //disposeAnyLandOrProperty: Boolean,
  landOrPropertyTransactions: Seq[LandOrPropertyTransactions]
)

case class LandOrPropertyTransactions(
  propertyDetails: PropertyDetails,
  heldPropertyTransaction: HeldPropertyTransaction
)

case class PropertyDetails(
  landOrPropertyInUK: Boolean,
  addressDetails: Address,
  landRegistryTitleNumberKey: String,
  landRegistryTitleNumberValue: String
)

case class HeldPropertyTransaction(
  methodOfHolding: SchemeHoldLandProperty,
  dateOfAcquisitionOrContribution: Option[LocalDate],
  propertyAcquiredFromName: Option[String],
  propertyAcquiredFrom: Option[PropertyAcquiredFrom],
  connectedPartyStatus: Boolean,
  totalCostOfLandOrProperty: Double,
  indepValuationSupport: Option[Boolean],
  isLandOrPropertyResidential: Boolean,
  leaseDetails: LeaseDetails,
  landOrPropertyLeased: Boolean,
  totalIncomeOrReceipts: Double
)

case class PropertyAcquiredFrom(
  identityType: IdentityType,
  idNumber: Option[String],
  reasonNoIdNumber: Option[String],
  otherDescription: Option[String],
  connectedPartyStatus: Boolean
)

case class LeaseDetails(
  lesseeName: String,
  leaseGrantDate: Double,
  annualLeaseAmount: String
)

object Assets {

  private implicit val formatLeaseDetails: OFormat[LeaseDetails] = Json.format[LeaseDetails]
  private implicit val writesSchemeHoldLandProperty: Writes[SchemeHoldLandProperty] =
    Writes(value => JsString(value.toString)) // TODO
  private implicit val formatPropertyAcquiredFrom: OFormat[PropertyAcquiredFrom] = Json.format[PropertyAcquiredFrom]
  private implicit val formatHeldPropertyTransaction: OFormat[HeldPropertyTransaction] =
    Json.format[HeldPropertyTransaction]
  private implicit val formatPropertyDetails: OFormat[PropertyDetails] = Json.format[PropertyDetails]
  private implicit val formatLandOrPropertyTransactions: OFormat[LandOrPropertyTransactions] =
    Json.format[LandOrPropertyTransactions]
  private implicit val formatLandOrProperty: OFormat[LandOrProperty] = Json.format[LandOrProperty]
  implicit val formats: OFormat[Assets] = Json.format[Assets]
}
