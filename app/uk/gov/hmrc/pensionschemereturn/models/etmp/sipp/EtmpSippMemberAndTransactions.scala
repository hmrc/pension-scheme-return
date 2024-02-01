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

package uk.gov.hmrc.pensionschemereturn.models.etmp.sipp

import play.api.libs.json.{Json, OFormat}
import uk.gov.hmrc.pensionschemereturn.models.etmp.sipp.EtmpSippMemberDetails.PersonalDetails
import uk.gov.hmrc.pensionschemereturn.models.etmp.sipp.common._
import uk.gov.hmrc.pensionschemereturn.models.etmp.{SectionStatus, YesNo}

import java.time.LocalDate

case class EtmpSippMemberAndTransactions(
  status: SectionStatus,
  version: Option[String],
  memberDetails: EtmpSippMemberDetails,
  landConnectedParty: Option[EtmpSippLandConnectedParty],
  otherAssetsConnectedParty: Option[EtmpSippOtherAssetsConnectedParty],
  landArmsLength: Option[EtmpSippLandArmsLength],
  tangibleProperty: Option[EtmpSippTangibleProperty],
  loanOutstanding: Option[EtmpSippLoanOutstanding],
  unquotedShares: Option[EtmpSippUnquotedShares]
)

case class EtmpSippMemberDetails(
  personalDetails: PersonalDetails,
  isUKAddress: YesNo,
  addressDetails: EtmpSippAddress
)

object EtmpSippMemberDetails {
  case class PersonalDetails(
    firstName: String,
    middleName: Option[String],
    lastName: String,
    nino: Option[String],
    reasonNoNINO: Option[String],
    dateOfBirth: LocalDate
  )

  implicit val formatPersonalDetails: OFormat[PersonalDetails] = Json.format[PersonalDetails]
}

case class EtmpSippLandConnectedParty(
  noOfTransactions: Int,
  transactionDetails: Option[List[EtmpSippLandConnectedParty.TransactionDetail]]
)

object EtmpSippLandConnectedParty {

  case class TransactionDetail(
    acquisitionDate: LocalDate,
    landOrPropertyinUK: YesNo,
    addressDetails: EtmpSippAddress,
    registryDetails: EtmpSippRegistryDetails,
    acquiredFromName: String,
    acquiredFromType: EtmpIndividualOrOrgIdentityDetails,
    totalCost: Double,
    independentValution: YesNo,
    jointlyHeld: YesNo,
    noOfPersons: Option[Int],
    jointPropertyPersonDetails: Option[List[EtmpSippJointPropertyDetail]],
    residentialSchedule29A: YesNo,
    isLeased: YesNo,
    lesseeDetails: Option[List[EtmpSippLesseeDetail]],
    totalIncomeOrReceipts: Double,
    isPropertyDisposed: YesNo,
    disposalDetails: Option[EtmpSippDispossalDetail]
  )

  implicit val formatTransactionDetails: OFormat[TransactionDetail] = Json.format[TransactionDetail]
}

case class EtmpSippOtherAssetsConnectedParty(
  noOfTransactions: Int,
  transactionDetails: Option[List[EtmpSippOtherAssetsConnectedParty.TransactionDetail]]
)

object EtmpSippOtherAssetsConnectedParty {
  case class TransactionDetail(
    acquisitionDate: LocalDate,
    assetDescription: String,
    acquisitionOfShares: YesNo,
    sharesCompanyDetails: Option[EtmpSippSharesCompanyDetail],
    acquiredFromName: String,
    acquiredFromType: EtmpIndividualOrOrgIdentityDetails,
    totalCost: Double,
    independentValution: YesNo,
    tangibleSchedule29A: YesNo,
    totalIncomeOrReceipts: Double,
    isPropertyDisposed: YesNo,
    disposalDetails: Option[EtmpSippDispossalDetail],
    disposalOfShares: YesNo,
    noOfSharesHeld: Option[Int]
  )

  implicit val formatTransactionDetails: OFormat[TransactionDetail] = Json.format[TransactionDetail]
}

case class EtmpSippLandArmsLength(
  noOfTransactions: Int,
  transactionDetails: Option[List[EtmpSippLandArmsLength.TransactionDetail]]
)

object EtmpSippLandArmsLength {
  case class TransactionDetail(
    acquisitionDate: LocalDate,
    landOrPropertyinUK: YesNo,
    addressDetails: EtmpSippAddress,
    registryDetails: EtmpSippRegistryDetails,
    acquiredFromName: String,
    acquiredFromType: EtmpIndividualOrOrgIdentityDetails,
    totalCost: Double,
    independentValution: YesNo,
    jointlyHeld: YesNo,
    noOfPersons: Option[Int],
    jointPropertyPersonDetails: Option[List[EtmpSippJointPropertyDetail]],
    residentialSchedule29A: YesNo,
    isLeased: YesNo,
    lesseeDetails: Option[List[EtmpSippLesseeDetail]],
    totalIncomeOrReceipts: Double,
    isPropertyDisposed: YesNo,
    disposalDetails: Option[EtmpSippDispossalDetail]
  )

  implicit val formatTransactionDetails: OFormat[TransactionDetail] = Json.format[TransactionDetail]
}

case class EtmpSippTangibleProperty(
  noOfTransactions: Int,
  transactionDetails: Option[List[EtmpSippTangibleProperty.TransactionDetail]]
)

object EtmpSippTangibleProperty {
  case class TransactionDetail(
    assetDescription: String,
    acquisitionDate: LocalDate,
    totalCost: Double,
    acquiredFromName: String,
    acquiredFromType: EtmpIndividualOrOrgIdentityDetails,
    independentValution: YesNo,
    totalIncomeOrReceipts: Double,
    costOrMarket: EtmpSippCostOrMarketType,
    costMarketValue: Double,
    isPropertyDisposed: YesNo,
    disposalDetails: Option[EtmpSippDispossalDetail]
  )

  implicit val formatTransactionDetails: OFormat[TransactionDetail] = Json.format[TransactionDetail]
}

case class EtmpSippLoanOutstanding(
  noOfTransactions: Int,
  transactionDetails: Option[List[EtmpSippLoanOutstanding.TransactionDetail]]
)

object EtmpSippLoanOutstanding {
  case class TransactionDetail(
    loanRecipientName: String,
    indivOrOrgIdentityDetails: EtmpIndividualOrOrgIdentityDetails,
    dateOfLoan: LocalDate,
    amountOfLoan: Double,
    loanConnectedParty: EtmpSippConnectedOrUnconnectedType,
    repayDate: LocalDate,
    interestRate: Double,
    loanSecurity: YesNo,
    capitalRepayments: Double,
    interestPayments: Double,
    arrearsOutstandingPrYears: YesNo,
    arrearsOutstandingPrYearsAmt: Option[Double],
    outstandingYearEndAmount: Double
  )

  implicit val formatTransactionDetails: OFormat[TransactionDetail] = Json.format[TransactionDetail]
}

case class EtmpSippUnquotedShares(
  noOfTransactions: Int,
  transactionDetails: Option[List[EtmpSippUnquotedShares.TransactionDetail]]
)

object EtmpSippUnquotedShares {
  case class TransactionDetail(
    sharesCompanyDetails: EtmpSippSharesCompanyDetail,
    acquiredFromName: String,
    acquiredFromType: EtmpIndividualOrOrgIdentityDetails,
    totalCost: Double,
    independentValution: YesNo,
    noOfSharesSold: Option[Int],
    totalDividendsIncome: Double,
    sharesDisposed: YesNo,
    sharesDisposalDetails: EtmpSippSharesDisposalDetails,
    noOfSharesHeld: Option[Int]
  )

  implicit val formatTransactionDetails: OFormat[TransactionDetail] = Json.format[TransactionDetail]
}

object EtmpSippMemberAndTransactions {
  implicit val formatEtmpSippMemberDetails: OFormat[EtmpSippMemberDetails] = Json.format[EtmpSippMemberDetails]
  implicit val formatEtmpSippLandConnectedParty: OFormat[EtmpSippLandConnectedParty] =
    Json.format[EtmpSippLandConnectedParty]
  implicit val formatEtmpSippOtherAssetsConnectedParty: OFormat[EtmpSippOtherAssetsConnectedParty] =
    Json.format[EtmpSippOtherAssetsConnectedParty]
  implicit val formatEtmpSippLandArmsLength: OFormat[EtmpSippLandArmsLength] = Json.format[EtmpSippLandArmsLength]
  implicit val formatEtmpSippTangibleProperty: OFormat[EtmpSippTangibleProperty] = Json.format[EtmpSippTangibleProperty]
  implicit val formatEtmpSippLoanOutstanding: OFormat[EtmpSippLoanOutstanding] = Json.format[EtmpSippLoanOutstanding]
  implicit val formatEtmpSippUnquotedShares: OFormat[EtmpSippUnquotedShares] = Json.format[EtmpSippUnquotedShares]
  implicit val format: OFormat[EtmpSippMemberAndTransactions] = Json.format[EtmpSippMemberAndTransactions]
}
