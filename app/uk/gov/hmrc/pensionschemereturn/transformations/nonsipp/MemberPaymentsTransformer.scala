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

import cats.syntax.traverse._
import com.google.inject.{Inject, Singleton}
import uk.gov.hmrc.pensionschemereturn.models.etmp.SectionStatus
import uk.gov.hmrc.pensionschemereturn.models.etmp.YesNo.unapply
import uk.gov.hmrc.pensionschemereturn.models.etmp.nonsipp._
import uk.gov.hmrc.pensionschemereturn.models.nonsipp._
import uk.gov.hmrc.pensionschemereturn.transformations.{ETMPTransformer, TransformerError}

@Singleton()
class MemberPaymentsTransformer @Inject()(
  employerContributionsTransformer: EmployerContributionsTransformer,
  memberPersonalDetailsTransformer: MemberPersonalDetailsTransformer,
  transferInTransformer: TransferInTransformer
) extends ETMPTransformer[MemberPayments, EtmpMemberPayments] {

  override def toEtmp(memberPayments: MemberPayments): EtmpMemberPayments =
    EtmpMemberPayments(
      recordVersion = None,
      employerContributionMade = memberPayments.employerContributionsCompleted,
      unallocatedContribsMade = memberPayments.unallocatedContribsMade,
      unallocatedContribAmount =
        if (memberPayments.unallocatedContribsMade) memberPayments.unallocatedContribAmount else None,
      memberContributionMade = memberPayments.memberContributionMade,
      schemeReceivedTransferIn = memberPayments.memberDetails.exists(_.transfersIn.nonEmpty),
      schemeMadeTransferOut = false,
      lumpSumReceived = memberPayments.lumpSumReceived,
      pensionReceived = false,
      surrenderMade = false,
      memberDetails = memberPayments.memberDetails.map { memberDetails =>
        EtmpMemberDetails(
          memberStatus = SectionStatus.New,
          memberPSRVersion = "0",
          noOfContributions =
            if (memberPayments.employerContributionsCompleted) Some(memberDetails.employerContributions.size) else None,
          totalContributions = memberDetails.totalContributions,
          noOfTransfersIn = if (memberPayments.transfersInCompleted) Some(memberDetails.transfersIn.size) else None,
          noOfTransfersOut = 0,
          pensionAmountReceived = None,
          personalDetails = memberPersonalDetailsTransformer.toEtmp(memberDetails.personalDetails),
          memberEmpContribution = memberDetails.employerContributions.map(employerContributionsTransformer.toEtmp),
          memberTransfersIn = memberDetails.transfersIn.map(transferInTransformer.toEtmp),
          memberLumpSumReceived = memberDetails.memberLumpSumReceived.map(
            x => List(EtmpMemberLumpSumReceived(x.lumpSumAmount, x.designatedPensionAmount))
          )
        )
      }
    )

  override def fromEtmp(out: EtmpMemberPayments): Either[TransformerError, MemberPayments] = {

    val memberDetails: Either[TransformerError, List[MemberDetails]] = out.memberDetails.traverse { member =>
      for {
        memberPersonalDetails <- memberPersonalDetailsTransformer.fromEtmp(member.personalDetails)
        employerContributions <- member.memberEmpContribution.traverse(employerContributionsTransformer.fromEtmp)
        transfersIn <- member.memberTransfersIn.traverse(transferInTransformer.fromEtmp)
      } yield MemberDetails(
        personalDetails = memberPersonalDetails,
        employerContributions = employerContributions,
        totalContributions = member.totalContributions,
        transfersIn = transfersIn,
        memberLumpSumReceived = member.memberLumpSumReceived.map(t => {
          val head = t.head
          MemberLumpSumReceived(head.lumpSumAmount, head.designatedPensionAmount)
        })
      )
    }
    memberDetails.map(
      details =>
        MemberPayments(
          memberDetails = details,
          employerContributionsCompleted = out.memberDetails.forall(_.noOfContributions.nonEmpty),
          transfersInCompleted = out.memberDetails.forall(_.noOfTransfersIn.nonEmpty),
          unallocatedContribsMade = unapply(out.unallocatedContribsMade),
          unallocatedContribAmount = out.unallocatedContribAmount,
          memberContributionMade = unapply(out.memberContributionMade),
          lumpSumReceived = unapply(out.lumpSumReceived)
        )
    )
  }
}