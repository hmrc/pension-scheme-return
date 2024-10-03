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
      recordVersion = memberPayments.recordVersion,
      employerContributionMade = (
        memberPayments.employerContributionsDetails.made,
        memberPayments.memberDetails.flatMap(_.employerContributions).size,
        memberPayments.employerContributionsDetails.completed
      ) match {
        // Completed with 0 contributions: transform to false, as both cases are logically equivalent
        case (true, 0, true) => Some(false)
        // Completed with 1+ contributions: no transformation required
        case (true, _, true) => Some(true)
        // In Progress with 0 contributions: no transformation required
        case (true, 0, false) => Some(true)
        // In Progress with 1+ contributions: transform to false, to differentiate from Completed with 1+ contributions
        case (true, _, false) => Some(false)
        // Not Started: transform to None
        case (false, 0, false) => None
        // (Not a valid state, but included for completeness)
        case (answer, _, _) => Some(answer)
      },
      unallocatedContribsMade = memberPayments.unallocatedContribsMade,
      unallocatedContribAmount = memberPayments.unallocatedContribAmount,
      memberContributionMade = memberPayments.memberContributionMade,
      schemeReceivedTransferIn = memberPayments.transfersInMade,
      schemeMadeTransferOut = memberPayments.transfersOutMade,
      lumpSumReceived = memberPayments.lumpSumReceived,
      pensionReceived = memberPayments.pensionReceived match {
        case SectionDetails(made @ true, completed @ true) => Some(true)
        case SectionDetails(made @ true, completed @ false) => Some(true)
        case SectionDetails(made @ false, completed @ true) => Some(false)
        case SectionDetails(made @ false, completed @ false) => None // not started
      },
      surrenderMade = memberPayments.benefitsSurrenderedDetails match {
        case SectionDetails(made @ true, completed @ true) => Some(true)
        case SectionDetails(made @ true, completed @ false) => Some(false)
        case SectionDetails(made @ false, completed @ true) => Some(false)
        case SectionDetails(made @ false, completed @ false) => None // not started
      },
      memberDetails = memberPayments.memberDetails.map { memberDetails =>
        EtmpMemberDetails(
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
          case SectionStatus.New => MemberState.New
          case SectionStatus.Changed => MemberState.Changed
          case SectionStatus.Deleted => MemberState.Deleted
        },
        memberPSRVersion = member.memberPSRVersion,
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
          recordVersion = out.recordVersion,
          memberDetails = details,
          employerContributionsDetails = SectionDetails(
            made = out.employerContributionMade.exists(_.boolean),
            completed =
              (out.employerContributionMade.map(_.boolean), details.flatMap(_.employerContributions).size) match {
                // "No" answer provided for contributions made, and 0 contributions: Completed
                case (Some(false), 0) => true
                // "No" answer (transformed from "Yes") for contributions made, and 1+ contributions: In Progress
                case (Some(false), _) => false
                // "Yes" answer provided for contributions made, and 0 contributions: In Progress
                case (Some(true), 0) => false
                // "Yes" answer provided for contributions made, and 1+ contributions: Completed
                case (Some(true), _) => true
                // Answer not provided for contributions made: Not Started
                case (None, 0) => false
                // (Not a valid state, but included for completeness)
                case (None, _) => false
              }
          ),
          transfersInMade = out.schemeReceivedTransferIn,
          transfersOutMade = out.schemeMadeTransferOut,
          unallocatedContribsMade = out.unallocatedContribsMade.map(_.boolean),
          unallocatedContribAmount = out.unallocatedContribAmount,
          memberContributionMade = out.memberContributionMade,
          lumpSumReceived = out.lumpSumReceived,
          pensionReceived = (out.pensionReceived.map(_.boolean), out.memberDetails.map(_.pensionAmountReceived)) match {
            case (None, _) => SectionDetails.notStarted
            case (Some(false), _) => SectionDetails(made = false, completed = true)
            case (Some(true), s) if s.forall(_.nonEmpty) || s.exists(_.exists(_ == 0)) =>
              SectionDetails(made = true, completed = true)
            case (Some(true), _) => SectionDetails(made = true, completed = false)
          },
          benefitsSurrenderedDetails = {
            (out.surrenderMade, out.memberDetails.map(_.memberPensionSurrender)) match {
              case (None, _) => SectionDetails.notStarted
              case (Some(made), list) if list.nonEmpty => SectionDetails(made = made, completed = true)
              case (Some(made), Nil) => SectionDetails(made = made, completed = false)
            }
          }
        )
    )
  }
}
