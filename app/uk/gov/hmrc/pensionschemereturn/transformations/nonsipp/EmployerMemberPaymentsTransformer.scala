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

package uk.gov.hmrc.pensionschemereturn.transformations.nonsipp

import com.google.inject.{Inject, Singleton}
import uk.gov.hmrc.pensionschemereturn.models.etmp.SectionStatus
import uk.gov.hmrc.pensionschemereturn.models.etmp.nonsipp._
import uk.gov.hmrc.pensionschemereturn.models.nonsipp._
import uk.gov.hmrc.pensionschemereturn.transformations.{ETMPTransformer, TransformerError}
import cats.syntax.traverse._

@Singleton()
class EmployerMemberPaymentsTransformer @Inject()(
  employerContributionsTransformer: EmployerContributionsTransformer,
  memberPersonalDetailsTransformer: MemberPersonalDetailsTransformer
) extends ETMPTransformer[MemberPayments, EtmpMemberPayments] {

  override def toEtmp(memberPayments: MemberPayments): EtmpMemberPayments =
    EtmpMemberPayments(
      recordVersion = None,
      employerContributionMade = memberPayments.memberDetails.exists(_.employerContributions.nonEmpty),
      unallocatedContribsMade = false,
      unallocatedContribAmount = None,
      memberContributionMade = false,
      schemeReceivedTransferIn = false,
      schemeMadeTransferOut = false,
      lumpSumReceived = false,
      pensionReceived = false,
      surrenderMade = false,
      memberDetails = memberPayments.memberDetails.map { memberDetails =>
        EtmpMemberDetails(
          memberStatus = SectionStatus.New,
          memberPSRVersion = "0",
          noOfContributions = None,
          totalContributions = 0,
          noOfTransfersIn = 0,
          noOfTransfersOut = 0,
          pensionAmountReceived = None,
          personalDetails = memberPersonalDetailsTransformer.toEtmp(memberDetails.personalDetails),
          memberEmpContribution = memberDetails.employerContributions.map(employerContributionsTransformer.toEtmp)
        )
      }
    )

  override def fromEtmp(out: EtmpMemberPayments): Either[TransformerError, MemberPayments] = {

    val memberDetails: Either[TransformerError, List[MemberDetails]] = out.memberDetails.traverse { member =>
      for {
        memberPersonalDetails <- memberPersonalDetailsTransformer.fromEtmp(member.personalDetails)
        employerContributions <- member.memberEmpContribution.traverse(employerContributionsTransformer.fromEtmp)
      } yield MemberDetails(
        personalDetails = memberPersonalDetails,
        employerContributions = employerContributions
      )
    }
    memberDetails.map(MemberPayments(_))
  }
}
