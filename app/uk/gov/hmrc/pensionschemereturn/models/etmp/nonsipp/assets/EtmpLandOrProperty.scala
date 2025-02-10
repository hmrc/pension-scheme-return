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

import uk.gov.hmrc.pensionschemereturn.models.etmp.nonsipp.common.EtmpIdentityType
import uk.gov.hmrc.pensionschemereturn.models.etmp.YesNo
import play.api.libs.json.{Json, OFormat}

import java.time.LocalDate

case class EtmpLandOrProperty(
  recordVersion: Option[String],
  heldAnyLandOrProperty: Option[String],
  disposeAnyLandOrProperty: Option[String],
  noOfTransactions: Option[Int],
  landOrPropertyTransactions: Option[Seq[EtmpLandOrPropertyTransactions]]
)

case class EtmpLandOrPropertyTransactions(
  propertyDetails: EtmpPropertyDetails,
  heldPropertyTransaction: EtmpHeldPropertyTransaction,
  disposedPropertyTransaction: Option[Seq[EtmpDisposedPropertyTransaction]]
)

case class EtmpPropertyDetails(
  prePopulated: Option[YesNo],
  landOrPropertyInUK: String,
  addressDetails: EtmpAddress,
  landRegistryDetails: EtmpLandRegistryDetails
)

case class EtmpHeldPropertyTransaction(
  methodOfHolding: String,
  dateOfAcquisitionOrContribution: Option[LocalDate],
  propertyAcquiredFromName: Option[String],
  propertyAcquiredFrom: Option[EtmpIdentityType],
  connectedPartyStatus: Option[String],
  totalCostOfLandOrProperty: Double,
  indepValuationSupport: Option[String],
  residentialSchedule29A: Option[String],
  landOrPropertyLeased: Option[String],
  leaseDetails: Option[EtmpLeaseDetails],
  totalIncomeOrReceipts: Option[Double]
)

case class EtmpDisposedPropertyTransaction(
  methodOfDisposal: String,
  otherMethod: Option[String],
  dateOfSale: Option[LocalDate],
  nameOfPurchaser: Option[String],
  purchaseOrgDetails: Option[EtmpIdentityType],
  saleProceeds: Option[Double],
  connectedPartyStatus: Option[String],
  indepValuationSupport: Option[String],
  portionStillHeld: String
)

case class EtmpLeaseDetails(
  lesseeName: Option[String],
  connectedPartyStatus: Option[String],
  leaseGrantDate: Option[LocalDate],
  annualLeaseAmount: Option[Double]
)

case class EtmpAddress(
  addressLine1: String,
  addressLine2: String,
  addressLine3: Option[String],
  addressLine4: Option[String],
  addressLine5: Option[String],
  ukPostCode: Option[String],
  countryCode: String
)

case class EtmpLandRegistryDetails(
  landRegistryReferenceExists: String,
  landRegistryReference: Option[String],
  reasonNoReference: Option[String]
)

object EtmpLandOrProperty {
  private implicit val formatsEtmpLeaseDetails: OFormat[EtmpLeaseDetails] =
    Json.format[EtmpLeaseDetails]
  private implicit val formatsEtmpLandRegistryDetails: OFormat[EtmpLandRegistryDetails] =
    Json.format[EtmpLandRegistryDetails]
  private implicit val formatsEtmpAddress: OFormat[EtmpAddress] =
    Json.format[EtmpAddress]
  private implicit val formatsEtmpHeldPropertyTransaction: OFormat[EtmpHeldPropertyTransaction] =
    Json.format[EtmpHeldPropertyTransaction]
  private implicit val formatsEtmpDisposedPropertyTransaction: OFormat[EtmpDisposedPropertyTransaction] =
    Json.format[EtmpDisposedPropertyTransaction]
  private implicit val formatsEtmpPropertyDetails: OFormat[EtmpPropertyDetails] =
    Json.format[EtmpPropertyDetails]
  private implicit val formatsEtmpLandOrPropertyTransactions: OFormat[EtmpLandOrPropertyTransactions] =
    Json.format[EtmpLandOrPropertyTransactions]
  implicit val formats: OFormat[EtmpLandOrProperty] =
    Json.format[EtmpLandOrProperty]
}
