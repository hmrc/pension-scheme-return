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

package uk.gov.hmrc.pensionschemereturn.models.etmp.nonsipp.memberpayments

import play.api.libs.json.{Format, Json}
import uk.gov.hmrc.pensionschemereturn.models.etmp.{SectionStatus, YesNo}

import java.time.LocalDate

case class EtmpMemberPayments(
  recordVersion: Option[String],
  employerContributionMade: YesNo,
  unallocatedContribsMade: YesNo,
  unallocatedContribAmount: Option[Double],
  memberContributionMade: YesNo,
  schemeReceivedTransferIn: YesNo,
  schemeMadeTransferOut: YesNo,
  lumpSumReceived: YesNo,
  pensionReceived: YesNo,
  surrenderMade: YesNo,
  memberDetails: List[EtmpMemberDetails]
)

// Using Option[List[?]] as the data items are optional in ETMP,
// this means that when we read them as a List[?] and the field doesn't exist in the response, it will blow up
case class EtmpMemberDetails(
  memberStatus: SectionStatus,
  memberPSRVersion: String,
  noOfContributions: Option[Int],
  totalContributions: Option[Double],
  noOfTransfersIn: Option[Int],
  noOfTransfersOut: Option[Int],
  pensionAmountReceived: Option[Double],
  personalDetails: EtmpMemberPersonalDetails,
  memberEmpContribution: Option[List[EtmpEmployerContributions]],
  memberTransfersIn: Option[List[EtmpTransfersIn]],
  memberLumpSumReceived: Option[List[EtmpMemberLumpSumReceived]],
  memberTransfersOut: Option[List[EtmpTransfersOut]],
  memberPensionSurrender: Option[List[EtmpPensionSurrender]]
)

case class EtmpMemberPersonalDetails(
  foreName: String,
  middleName: Option[String],
  lastName: String,
  nino: Option[String],
  reasonNoNINO: Option[String],
  dateOfBirth: LocalDate
)

case class EtmpMemberLumpSumReceived(
  lumpSumAmount: Double,
  designatedPensionAmount: Double
)

object EtmpMemberPayments {
  private implicit val formatEtmpMemberLumpSumReceived: Format[EtmpMemberLumpSumReceived] =
    Json.format[EtmpMemberLumpSumReceived]
  private implicit val formatEtmpMemberPersonalDetails: Format[EtmpMemberPersonalDetails] =
    Json.format[EtmpMemberPersonalDetails]
  private implicit val formatEtmpMemberDetails: Format[EtmpMemberDetails] = Json.format[EtmpMemberDetails]
  implicit val writes: Format[EtmpMemberPayments] = Json.format[EtmpMemberPayments]
}
