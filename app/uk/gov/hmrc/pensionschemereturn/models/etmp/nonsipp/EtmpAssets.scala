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

package uk.gov.hmrc.pensionschemereturn.models.etmp.nonsipp

import play.api.libs.json.{Json, OFormat}

import java.time.LocalDate

case class EtmpAssets(
  landOrProperty: EtmpLandOrProperty,
  borrowing: EtmpBorrowing,
  bonds: EtmpBonds,
  otherAssets: EtmpOtherAssets
)

case class EtmpLandOrProperty(
  recordVersion: Option[String],
  heldAnyLandOrProperty: String,
  disposeAnyLandOrProperty: String,
  noOfTransactions: Int,
  landOrPropertyTransactions: Seq[EtmpLandOrPropertyTransactions]
)

case class EtmpBorrowing(
  recordVersion: Option[String],
  moneyWasBorrowed: String,
  noOfBorrows: Option[Int],
  moneyBorrowed: Option[Seq[EtmpMoneyBorrowed]]
)

case class EtmpMoneyBorrowed(
  dateOfBorrow: LocalDate,
  schemeAssetsValue: Double,
  amountBorrowed: Double,
  interestRate: Double,
  borrowingFromName: String,
  connectedPartyStatus: String,
  reasonForBorrow: String
)
case class EtmpBonds(
  bondsWereAdded: String,
  bondsWereDisposed: String
)

case class EtmpOtherAssets(
  otherAssetsWereHeld: String,
  otherAssetsWereDisposed: String
)

case class EtmpLandOrPropertyTransactions(
  propertyDetails: EtmpPropertyDetails,
  heldPropertyTransaction: EtmpHeldPropertyTransaction,
  disposedPropertyTransaction: Option[Seq[EtmpDisposedPropertyTransaction]]
)

case class EtmpPropertyDetails(
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
  residentialSchedule29A: String,
  landOrPropertyLeased: String,
  leaseDetails: Option[EtmpLeaseDetails],
  totalIncomeOrReceipts: Double
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
  lesseeName: String,
  connectedPartyStatus: String,
  leaseGrantDate: LocalDate,
  annualLeaseAmount: Double
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

object EtmpAssets {

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
  private implicit val formatsEtmpLandOrProperty: OFormat[EtmpLandOrProperty] =
    Json.format[EtmpLandOrProperty]

  private implicit val formatsEtmpOtherAssets: OFormat[EtmpOtherAssets] =
    Json.format[EtmpOtherAssets]
  private implicit val formatsEtmpBonds: OFormat[EtmpBonds] =
    Json.format[EtmpBonds]

  private implicit val formatsEtmpMoneyBorrowed: OFormat[EtmpMoneyBorrowed] =
    Json.format[EtmpMoneyBorrowed]
  private implicit val formatsEtmpBorrowing: OFormat[EtmpBorrowing] =
    Json.format[EtmpBorrowing]

  implicit val formats: OFormat[EtmpAssets] = Json.format[EtmpAssets]
}
