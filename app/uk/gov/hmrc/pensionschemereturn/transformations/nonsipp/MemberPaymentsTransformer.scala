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
import uk.gov.hmrc.pensionschemereturn.models.etmp.YesNo.unapply
import com.google.inject.{Inject, Singleton}
import uk.gov.hmrc.pensionschemereturn.models.etmp.SectionStatus
import uk.gov.hmrc.pensionschemereturn.models.nonsipp.memberpayments._
import uk.gov.hmrc.pensionschemereturn.models.etmp.nonsipp.memberpayments.{
  EtmpMemberDetails,
  EtmpMemberLumpSumReceived,
  EtmpMemberPayments
}
import uk.gov.hmrc.pensionschemereturn.transformations.{ETMPTransformer, TransformerError}

@Singleton()
class MemberPaymentsTransformer @Inject()(
  employerContributionsTransformer: EmployerContributionsTransformer,
  memberPersonalDetailsTransformer: MemberPersonalDetailsTransformer,
  transferInTransformer: TransferInTransformer,
  transferOutTransformer: TransferOutTransformer,
  pensionSurrenderTransformer: PensionSurrenderTransformer
) extends ETMPTransformer[MemberPayments, EtmpMemberPayments] {

  override def toEtmp(memberPayments: MemberPayments): EtmpMemberPayments =
    EtmpMemberPayments(
      recordVersion = None,
      employerContributionMade = (
        memberPayments.employerContributionsDetails.made,
        memberPayments.memberDetails.flatMap(_.employerContributions).size,
        memberPayments.employerContributionsDetails.completed
      ) match {
        // Complete with 0 contributions: transform to false, as both cases are logically equivalent
        case (true, 0, true) => false
        // In Progress with 1+ contributions: transform to false, to differentiate from Completed with 1+ contributions
        case (true, _, false) => false
        // All other cases: no transformation required
        case (answer, _, _) => answer
      },
      unallocatedContribsMade = memberPayments.unallocatedContribsMade,
      unallocatedContribAmount =
        if (memberPayments.unallocatedContribsMade) memberPayments.unallocatedContribAmount else None,
      memberContributionMade = memberPayments.memberContributionMade,
      schemeReceivedTransferIn = memberPayments.memberDetails.exists(_.transfersIn.nonEmpty),
      schemeMadeTransferOut = memberPayments.memberDetails.exists(_.transfersOut.nonEmpty),
      lumpSumReceived = memberPayments.lumpSumReceived,
      pensionReceived = memberPayments.pensionReceived,
      surrenderMade = memberPayments.benefitsSurrenderedDetails match {
        case SectionDetails(made @ true, completed @ true) => true
        case SectionDetails(made @ true, completed @ false) => false
        case SectionDetails(made @ false, completed @ true) => false
        case SectionDetails(made @ false, completed @ false) => false // shouldn't happen but adding just in case
      },
      memberDetails = memberPayments.memberDetails.map { memberDetails =>
        EtmpMemberDetails(
          memberStatus = memberDetails.state match {
            case MemberState.Active => SectionStatus.New
            case MemberState.Deleted => SectionStatus.Deleted
          },
          memberPSRVersion = "001",
          noOfContributions = Some(memberDetails.employerContributions.size),
          totalContributions = memberDetails.totalContributions,
          noOfTransfersIn = if (memberPayments.transfersInCompleted) Some(memberDetails.transfersIn.size) else None,
          noOfTransfersOut = if (memberPayments.transfersOutCompleted) Some(memberDetails.transfersOut.size) else None,
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
          memberLumpSumReceived = memberDetails.memberLumpSumReceived.map(
            x => List(EtmpMemberLumpSumReceived(x.lumpSumAmount, x.designatedPensionAmount))
          ),
          memberTransfersOut = memberDetails.transfersOut.map(transferOutTransformer.toEtmp) match {
            case Nil => None
            case list => Some(list)
          },
          memberPensionSurrender = Option.when(memberPayments.benefitsSurrenderedDetails.made)(
            memberDetails.benefitsSurrendered.map(pensionSurrenderTransformer.toEtmp) match {
              case Some(surrender) => List(surrender)
              case None => Nil
            }
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
        state = member.memberStatus match {
          case SectionStatus.New => MemberState.Active
          case SectionStatus.Changed => MemberState.Active
          case SectionStatus.Deleted => MemberState.Deleted
        },
        personalDetails = memberPersonalDetails,
        employerContributions = employerContributions,
        totalContributions = member.totalContributions,
        transfersIn = transfersIn,
        memberLumpSumReceived = member.memberLumpSumReceived.map(t => {
          val head = t.head
          MemberLumpSumReceived(head.lumpSumAmount, head.designatedPensionAmount)
        }),
        transfersOut = transfersOut,
        benefitsSurrendered = pensionSurrenders.headOption,
        pensionAmountReceived = member.pensionAmountReceived
      )
    }
    memberDetails.map(
      details =>
        MemberPayments(
          memberDetails = details,
          employerContributionsDetails = SectionDetails(
            made = out.employerContributionMade.boolean,
            completed = (out.employerContributionMade.boolean, details.flatMap(_.employerContributions).size) match {
              case (false, 0) => true // Completed with no Emp Conts made
              case (false, _) => false // In Progress with "Yes" answer (set as "No" to differentiate from cases below)
              case (true, 0) => false // In Progress with "Yes" answer but no Emp Conts made yet
              case (true, _) => true // Completed with some Emp Conts made
            }
          ),
          transfersInCompleted = out.memberDetails.forall(_.noOfTransfersIn.nonEmpty),
          transfersOutCompleted = out.memberDetails.forall(_.noOfTransfersOut.nonEmpty),
          unallocatedContribsMade = unapply(out.unallocatedContribsMade),
          unallocatedContribAmount = out.unallocatedContribAmount,
          memberContributionMade = unapply(out.memberContributionMade),
          lumpSumReceived = unapply(out.lumpSumReceived),
          pensionReceived = unapply(out.pensionReceived),
          benefitsSurrenderedDetails =
            (out.surrenderMade.boolean, out.memberDetails.map(_.memberPensionSurrender)) match {
              case (false, List(Some(Nil))) => SectionDetails(made = true, completed = false)
              case (true, List(Some(Nil))) => SectionDetails(made = true, completed = true)
              case (false, List(None)) => SectionDetails(made = false, completed = true)
              case (true, List(None)) => SectionDetails(made = true, completed = true) // this case shouldn't happen
              case (true, _) => SectionDetails(made = true, completed = true)
              case (false, _) => SectionDetails(made = false, completed = true)
            }
        )
    )
  }
}
