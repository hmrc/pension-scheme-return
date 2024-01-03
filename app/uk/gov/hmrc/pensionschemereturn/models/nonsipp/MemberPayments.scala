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

import play.api.libs.json.{Format, Json}

case class MemberPayments(
  memberDetails: List[MemberDetails],
  employerContributionsCompleted: Boolean,
  transfersInCompleted: Boolean,
  transfersOutCompleted: Boolean,
  unallocatedContribsMade: Boolean,
  unallocatedContribAmount: Option[Double],
  memberContributionMade: Boolean,
  lumpSumReceived: Boolean
)

case class MemberDetails(
  personalDetails: MemberPersonalDetails,
  employerContributions: List[EmployerContributions],
  totalContributions: Option[Double],
  transfersIn: List[TransfersIn],
  memberLumpSumReceived: Option[MemberLumpSumReceived],
  transfersOut: List[TransfersOut]
)

case class MemberLumpSumReceived(
  lumpSumAmount: Double,
  designatedPensionAmount: Double
)

object MemberPayments {
  private implicit val formatMemberLumpSumReceived: Format[MemberLumpSumReceived] = Json.format[MemberLumpSumReceived]
  private implicit val formatMemberDetails: Format[MemberDetails] = Json.format[MemberDetails]
  implicit val format: Format[MemberPayments] = Json.format[MemberPayments]
}
