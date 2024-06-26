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

package uk.gov.hmrc.pensionschemereturn.models.nonsipp.memberpayments

import play.api.libs.json.{Format, Json}

/**
 * Contains a representation of the important yes / no questions for a section
 * (made = false && completed = false) = section not started
 * (made = false && completed = true)  = initial section question is no
 * (made = true && completed = true)   = initial section question is yes and the user has said they don't want to add anymore
 * (made = true && completed = false)  = initial section question is yes and the user has changed or removed something in a section
 *                                       this will be changed to (made = true && completed = true) when the user says they don't want to add anymore
 */
case class SectionDetails(
  made: Boolean,
  completed: Boolean
) {
  val started: Boolean = !(!made && !completed)
}

object SectionDetails {
  val notStarted: SectionDetails = SectionDetails(made = false, completed = false)
}

case class MemberPayments(
  recordVersion: Option[String],
  memberDetails: List[MemberDetails],
  employerContributionsDetails: SectionDetails,
  transfersInMade: Option[Boolean],
  transfersOutMade: Option[Boolean],
  unallocatedContribsMade: Option[Boolean],
  unallocatedContribAmount: Option[Double],
  memberContributionMade: Option[Boolean],
  lumpSumReceived: Option[Boolean],
  pensionReceived: SectionDetails,
  benefitsSurrenderedDetails: SectionDetails
)

case class MemberDetails(
  state: MemberState,
  memberPSRVersion: Option[String],
  personalDetails: MemberPersonalDetails,
  employerContributions: List[EmployerContributions],
  totalContributions: Option[Double],
  transfersIn: List[TransfersIn],
  memberLumpSumReceived: Option[MemberLumpSumReceived],
  transfersOut: List[TransfersOut],
  benefitsSurrendered: Option[PensionSurrender],
  pensionAmountReceived: Option[Double]
)

case class MemberLumpSumReceived(
  lumpSumAmount: Double,
  designatedPensionAmount: Double
)

object MemberPayments {
  private implicit val formatSectionDetails: Format[SectionDetails] = Json.format[SectionDetails]
  private implicit val formatMemberLumpSumReceived: Format[MemberLumpSumReceived] = Json.format[MemberLumpSumReceived]
  private implicit val formatMemberDetails: Format[MemberDetails] = Json.format[MemberDetails]
  implicit val format: Format[MemberPayments] = Json.format[MemberPayments]
}
