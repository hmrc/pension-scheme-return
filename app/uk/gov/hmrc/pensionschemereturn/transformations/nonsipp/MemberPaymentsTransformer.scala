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

package uk.gov.hmrc.pensionschemereturn.transformations.nonsipp

import cats.syntax.traverse._
import com.google.inject.{Inject, Singleton}
import uk.gov.hmrc.pensionschemereturn.models.etmp.SectionStatus
import uk.gov.hmrc.pensionschemereturn.models.nonsipp.memberpayments._
import uk.gov.hmrc.pensionschemereturn.models.etmp.nonsipp.memberpayments._
import uk.gov.hmrc.pensionschemereturn.transformations.{ETMPTransformer, TransformerError}

@Singleton()
class MemberPaymentsTransformer @Inject() (
  employerContributionsTransformer: EmployerContributionsTransformer,
  memberPersonalDetailsTransformer: MemberPersonalDetailsTransformer,
  transferInTransformer: TransferInTransformer,
  transferOutTransformer: TransferOutTransformer,
  pensionSurrenderTransformer: PensionSurrenderTransformer
) extends ETMPTransformer[MemberPayments, EtmpMemberPayments] {

  override def toEtmp(memberPayments: MemberPayments): EtmpMemberPayments =
    EtmpMemberPayments(
      checked = memberPayments.checked,
      recordVersion = memberPayments.recordVersion,
      employerContributionMade = memberPayments.employerContributionMade,
      unallocatedContribsMade = memberPayments.unallocatedContribsMade,
      unallocatedContribAmount = memberPayments.unallocatedContribAmount,
      memberContributionMade = memberPayments.memberContributionMade,
      schemeReceivedTransferIn = memberPayments.transfersInMade,
      schemeMadeTransferOut = memberPayments.transfersOutMade,
      lumpSumReceived = memberPayments.lumpSumReceived,
      pensionReceived = memberPayments.pensionReceived,
      surrenderMade = memberPayments.surrenderMade,
      memberDetails = memberPayments.memberDetails.map { memberDetails =>
        EtmpMemberDetails(
          prePopulated = memberDetails.prePopulated,
          memberStatus = memberDetails.state match {
            case MemberState.New => SectionStatus.New
            case MemberState.Changed => SectionStatus.Changed
            case MemberState.Deleted => SectionStatus.Deleted
          },
          memberPSRVersion = memberDetails.memberPSRVersion,
          noOfContributions = Some(memberDetails.employerContributions.size),
          totalContributions = memberDetails.totalContributions,
          noOfTransfersIn = Some(memberDetails.transfersIn.size),
          noOfTransfersOut = Some(memberDetails.transfersOut.size),
          pensionAmountReceived = memberDetails.pensionAmountReceived,
          personalDetails = memberPersonalDetailsTransformer.toEtmp(memberDetails.personalDetails),
          memberEmpContribution =
            memberDetails.employerContributions.map(employerContributionsTransformer.toEtmp) match {
              case Nil => None
              case list => Some(list)
            },
          memberTransfersIn = memberDetails.transfersIn.map(transferInTransformer.toEtmp) match {
            case Nil => None
            case list => Some(list)
          },
          memberLumpSumReceived = memberDetails.memberLumpSumReceived.map(x =>
            List(EtmpMemberLumpSumReceived(x.lumpSumAmount, x.designatedPensionAmount))
          ),
          memberTransfersOut = memberDetails.transfersOut.map(transferOutTransformer.toEtmp) match {
            case Nil => None
            case list => Some(list)
          },
          memberPensionSurrender = memberDetails.benefitsSurrendered.map(x =>
            List(EtmpPensionSurrender(x.totalSurrendered, x.dateOfSurrender, x.surrenderReason))
          )
        )
      }
    )

  override def fromEtmp(out: EtmpMemberPayments): Either[TransformerError, MemberPayments] = {

    val memberDetails: Either[TransformerError, List[MemberDetails]] = out.memberDetails.traverse { member =>
      for {
        memberPersonalDetails <- memberPersonalDetailsTransformer.fromEtmp(member.personalDetails)
        employerContributions <- member.memberEmpContribution.toList.flatten
          .traverse(employerContributionsTransformer.fromEtmp)
        transfersIn <- member.memberTransfersIn.toList.flatten.traverse(transferInTransformer.fromEtmp)
        transfersOut <- member.memberTransfersOut.toList.flatten.traverse(transferOutTransformer.fromEtmp)
        pensionSurrenders <- member.memberPensionSurrender.toList.flatten.traverse(pensionSurrenderTransformer.fromEtmp)
      } yield MemberDetails(
        prePopulated = member.prePopulated,
        state = member.memberStatus match {
          case SectionStatus.New => MemberState.New
          case SectionStatus.Changed => MemberState.Changed
          case SectionStatus.Deleted => MemberState.Deleted
        },
        memberPSRVersion = member.memberPSRVersion,
        personalDetails = memberPersonalDetails,
        employerContributions = employerContributions,
        totalContributions = member.totalContributions,
        transfersIn = transfersIn,
        memberLumpSumReceived = member.memberLumpSumReceived.map { t =>
          val head = t.head
          MemberLumpSumReceived(head.lumpSumAmount, head.designatedPensionAmount)
        },
        transfersOut = transfersOut,
        benefitsSurrendered = pensionSurrenders.headOption,
        pensionAmountReceived = member.pensionAmountReceived
      )
    }
    memberDetails.map(details =>
      MemberPayments(
        checked = out.checked,
        recordVersion = out.recordVersion,
        memberDetails = details,
        employerContributionMade = out.employerContributionMade,
        transfersInMade = out.schemeReceivedTransferIn,
        transfersOutMade = out.schemeMadeTransferOut,
        unallocatedContribsMade = out.unallocatedContribsMade.map(_.boolean),
        unallocatedContribAmount = out.unallocatedContribAmount,
        memberContributionMade = out.memberContributionMade,
        lumpSumReceived = out.lumpSumReceived,
        pensionReceived = out.pensionReceived,
        surrenderMade = out.surrenderMade
      )
    )
  }
}
