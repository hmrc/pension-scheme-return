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

import play.api.libs.json.{Format, Json}
import uk.gov.hmrc.pensionschemereturn.models.etmp.SectionStatus

import java.time.LocalDate

case class EtmpMemberPayments(
  recordVersion: Option[String],
  employerContributionMade: Boolean,
  unallocatedContribsMade: Boolean,
  unallocatedContribAmount: Option[Double],
  memberContributionMade: Boolean,
  schemeReceivedTransferIn: Boolean,
  schemeMadeTransferOut: Boolean,
  lumpSumReceived: Boolean,
  pensionReceived: Boolean,
  surrenderMade: Boolean,
  memberDetails: List[EtmpMemberDetails]
)

case class EtmpMemberDetails(
  memberStatus: SectionStatus,
  memberPSRVersion: String,
  noOfContributions: Option[Int],
  totalContributions: Double,
  noOfTransfersIn: Int,
  noOfTransfersOut: Int,
  pensionAmountReceived: Double,
  personalDetails: EtmpMemberPersonalDetails,
  memberEmpContribution: List[EtmpEmployerContributions]
)

case class EtmpMemberPersonalDetails(
  foreName: String,
  middleName: Option[String],
  lastName: String,
  nino: Option[String],
  reasonNoNino: Option[String],
  dateOfBirth: LocalDate
)

object EtmpMemberPayments {
  private implicit val formatEtmpMemberPersonalDetails: Format[EtmpMemberPersonalDetails] =
    Json.format[EtmpMemberPersonalDetails]
  private implicit val formatEtmpMemberDetails: Format[EtmpMemberDetails] = Json.format[EtmpMemberDetails]
  implicit val writes: Format[EtmpMemberPayments] = Json.format[EtmpMemberPayments]
}
