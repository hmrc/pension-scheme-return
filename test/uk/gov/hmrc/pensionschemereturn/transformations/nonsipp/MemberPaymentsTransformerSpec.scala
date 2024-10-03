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

import uk.gov.hmrc.pensionschemereturn.models.etmp.{SectionStatus, YesNo}
import uk.gov.hmrc.pensionschemereturn.models.nonsipp.memberpayments._
import uk.gov.hmrc.pensionschemereturn.base.EtmpTransformerSpec
import uk.gov.hmrc.pensionschemereturn.models.etmp.nonsipp.memberpayments.{
  EtmpEmployerContributions,
  EtmpMemberPayments,
  EtmpPensionSurrender
}

import java.time.LocalDate

class MemberPaymentsTransformerSpec extends EtmpTransformerSpec {

  private val pensionSurrender = PensionSurrender(12.34, LocalDate.of(2022, 12, 12), "some reason")
  private val etmpPensionSurrender = EtmpPensionSurrender(12.34, LocalDate.of(2022, 12, 12), "some reason")
  private val pensionAmountReceived = 12.34

  "MemberPaymentsTransformer" should {
    "successfully transform to ETMP format" in {
      val result = memberPaymentsTransformer.toEtmp(sampleMemberPayments)

      result shouldMatchTo sampleEtmpMemberPayments
    }

    "successfully transform from ETMP format" in {
      val result = memberPaymentsTransformer.fromEtmp(sampleEtmpMemberPayments)

      result shouldMatchTo Right(sampleMemberPayments)
    }

    "successfully transform Pension Payments section to ETMP format" when {

      "pensionReceived.made is TRUE, pensionReceived.completed is TRUE and pensionAmountReceived is provided" in {

        val memberPayments = transformPensionPaymentsToEtmp(
          made = true,
          completed = true,
          pensionAmountReceived = Some(pensionAmountReceived)
        )

        val etmpMemberPayments = transformPensionPaymentsFromEtmp(
          pensionReceived = Some(true),
          pensionAmountReceived = Some(pensionAmountReceived)
        )

        val result = memberPaymentsTransformer.toEtmp(memberPayments)

        result shouldMatchTo etmpMemberPayments
      }

      "pensionReceived.made is TRUE, pensionReceived.completed is FALSE and pensionAmountReceived is provided" in {

        val memberPayments = transformPensionPaymentsToEtmp(
          made = true,
          completed = false,
          pensionAmountReceived = Some(pensionAmountReceived)
        )

        val etmpMemberPayments = transformPensionPaymentsFromEtmp(
          pensionReceived = Some(true),
          pensionAmountReceived = Some(pensionAmountReceived)
        )

        val result = memberPaymentsTransformer.toEtmp(memberPayments)

        result shouldMatchTo etmpMemberPayments
      }

      "pensionReceived.made is TRUE, pensionReceived.completed is TRUE and pensionAmountReceived is NOT provided" in {

        val memberPayments = transformPensionPaymentsToEtmp(
          made = true,
          completed = true,
          pensionAmountReceived = None
        )

        val etmpMemberPayments = transformPensionPaymentsFromEtmp(
          pensionReceived = Some(true),
          pensionAmountReceived = None
        )

        val result = memberPaymentsTransformer.toEtmp(memberPayments)

        result shouldMatchTo etmpMemberPayments
      }

      "pensionReceived.made is FALSE, pensionReceived.completed is TRUE and pensionAmountReceived is NOT provided" in {

        val memberPayments = transformPensionPaymentsToEtmp(
          made = false,
          completed = true,
          pensionAmountReceived = None
        )

        val etmpMemberPayments = transformPensionPaymentsFromEtmp(
          pensionReceived = Some(false),
          pensionAmountReceived = None
        )

        val result = memberPaymentsTransformer.toEtmp(memberPayments)

        result shouldMatchTo etmpMemberPayments
      }

      "pensionReceived.made is FALSE, pensionReceived.completed is FALSE indicating Not Started" in {

        val memberPayments = transformPensionPaymentsToEtmp(
          made = false,
          completed = false,
          pensionAmountReceived = None
        )

        val etmpMemberPayments = transformPensionPaymentsFromEtmp(
          pensionReceived = None,
          pensionAmountReceived = None
        )

        val result = memberPaymentsTransformer.toEtmp(memberPayments)

        result shouldMatchTo etmpMemberPayments
      }
    }

    "successfully transform Pension Payments section from ETMP format" when {

      "pensionReceived.made is TRUE, pensionAmountReceived is provided" in {

        val etmpMemberPayments = transformPensionPaymentsFromEtmp(
          pensionReceived = Some(true),
          pensionAmountReceived = Some(pensionAmountReceived)
        )

        val memberPayments = transformPensionPaymentsToEtmp(
          made = true,
          completed = true,
          pensionAmountReceived = Some(pensionAmountReceived)
        )

        val result = memberPaymentsTransformer.fromEtmp(etmpMemberPayments)

        result shouldMatchTo Right(memberPayments)
      }

      "pensionReceived.made is TRUE, pensionAmountReceived is NOT provided" in {

        val etmpMemberPayments = transformPensionPaymentsFromEtmp(
          pensionReceived = Some(true),
          pensionAmountReceived = None
        )

        val memberPayments = transformPensionPaymentsToEtmp(
          made = true,
          completed = false,
          pensionAmountReceived = None
        )

        val result = memberPaymentsTransformer.fromEtmp(etmpMemberPayments)

        result shouldMatchTo Right(memberPayments)
      }

      "pensionReceived.made is FALSE and pensionAmountReceived is provided" in {

        val etmpMemberPayments = transformPensionPaymentsFromEtmp(
          pensionReceived = Some(false),
          pensionAmountReceived = Some(pensionAmountReceived)
        )

        val memberPayments = transformPensionPaymentsToEtmp(
          made = false,
          completed = true,
          pensionAmountReceived = Some(pensionAmountReceived)
        )

        val result = memberPaymentsTransformer.fromEtmp(etmpMemberPayments)

        result shouldMatchTo Right(memberPayments)
      }
    }

    "successfully transform surrenderedBenefits section to ETMP format" when {

      "surrenders made is TRUE, completed is TRUE and BOTH surrendered benefits are provided" in {

        val memberPayments = transformSurrenderedBenefits(
          made = true,
          completed = true,
          surrenderedBenefits = Some(pensionSurrender)
        )

        val etmpMemberPayments = transformEtmpSurrenderedBenefits(
          surrenderMade = Some(true),
          surrenderedBenefits = Some(List(etmpPensionSurrender))
        )

        val result = memberPaymentsTransformer.toEtmp(memberPayments)

        result shouldMatchTo etmpMemberPayments
      }

      "surrenders made is TRUE, completed is FALSE and BOTH surrendered benefits are provided" in {

        val memberPayments = transformSurrenderedBenefits(
          made = true,
          completed = false,
          surrenderedBenefits = Some(pensionSurrender)
        )

        val etmpMemberPayments = transformEtmpSurrenderedBenefits(
          surrenderMade = Some(false),
          surrenderedBenefits = Some(List(etmpPensionSurrender))
        )

        val result = memberPaymentsTransformer.toEtmp(memberPayments)

        result shouldMatchTo etmpMemberPayments
      }

      "surrenders made is TRUE, completed is TRUE and NO surrendered benefits are provided" in {

        val memberPayments = transformSurrenderedBenefits(
          made = true,
          completed = true,
          surrenderedBenefits = None
        )

        val etmpMemberPayments = transformEtmpSurrenderedBenefits(
          surrenderMade = Some(true),
          surrenderedBenefits = Some(Nil)
        )

        val result = memberPaymentsTransformer.toEtmp(memberPayments)

        result shouldMatchTo etmpMemberPayments
      }

      "surrenders made is FALSE, completed is TRUE and NO surrendered benefits are provided" in {

        val memberPayments = transformSurrenderedBenefits(
          made = false,
          completed = true,
          surrenderedBenefits = None
        )

        val etmpMemberPayments = transformEtmpSurrenderedBenefits(
          surrenderMade = Some(false),
          surrenderedBenefits = None
        )

        val result = memberPaymentsTransformer.toEtmp(memberPayments)

        result shouldMatchTo etmpMemberPayments
      }

      "surrenders made is FALSE, completed is FALSE indicating Not Started" in {

        val memberPayments = transformSurrenderedBenefits(
          made = false,
          completed = false,
          surrenderedBenefits = None
        )

        val etmpMemberPayments = transformEtmpSurrenderedBenefits(
          surrenderMade = None,
          surrenderedBenefits = None
        )

        val result = memberPaymentsTransformer.toEtmp(memberPayments)

        result shouldMatchTo etmpMemberPayments
      }
    }

    "successfully transform surrenderedBenefits section from ETMP format" when {
      "surrenders made is TRUE, BOTH surrendered benefits are provided" in {

        val etmpMemberPayments = transformEtmpSurrenderedBenefits(
          surrenderMade = Some(true),
          surrenderedBenefits = Some(List(etmpPensionSurrender))
        )

        val memberPayments = transformSurrenderedBenefits(
          made = true,
          completed = true,
          surrenderedBenefits = Some(pensionSurrender)
        )

        val result = memberPaymentsTransformer.fromEtmp(etmpMemberPayments)

        result shouldMatchTo Right(memberPayments)
      }

      "surrenders made is TRUE, NO surrendered benefits are provided" in {

        val etmpMemberPayments = transformEtmpSurrenderedBenefits(
          surrenderMade = Some(true),
          surrenderedBenefits = Some(Nil)
        )

        val memberPayments = transformSurrenderedBenefits(
          made = true,
          completed = true,
          surrenderedBenefits = None
        )

        val result = memberPaymentsTransformer.fromEtmp(etmpMemberPayments)

        result shouldMatchTo Right(memberPayments)
      }

      "surrenders made is FALSE and BOTH surrendered benefits are provided" in {

        val etmpMemberPayments = transformEtmpSurrenderedBenefits(
          surrenderMade = Some(false),
          surrenderedBenefits = Some(List(etmpPensionSurrender))
        )

        val memberPayments = transformSurrenderedBenefits(
          made = false,
          completed = true,
          surrenderedBenefits = Some(pensionSurrender)
        )

        val result = memberPaymentsTransformer.fromEtmp(etmpMemberPayments)

        result shouldMatchTo Right(memberPayments)
      }
    }

    "successfully transform Employer Contributions section to ETMP format" when {

      "Completed with 0 contributions: case (true, 0, true)" in {

        val memberPayments: MemberPayments = transformEmployerContributionsToEtmp(
          made = true,
          completed = true,
          employerContributions = List.empty
        )

        val etmpMemberPayments: EtmpMemberPayments = transformEmployerContributionsFromEtmp(
          employerContributionMade = Some(false), // transformed from true in this case
          noOfContributions = None,
          employerContributions = None
        )

        val result = memberPaymentsTransformer.toEtmp(memberPayments)

        result shouldMatchTo etmpMemberPayments
      }

      "Completed with 1+ contributions: case (true, _, true)" in {

        val memberPayments: MemberPayments = transformEmployerContributionsToEtmp(
          made = true,
          completed = true,
          employerContributions = List(sampleEmployerContribution1)
        )

        val etmpMemberPayments: EtmpMemberPayments = transformEmployerContributionsFromEtmp(
          employerContributionMade = Some(true),
          noOfContributions = Some(1),
          employerContributions = Some(List(sampleEtmpEmployerContribution1))
        )

        val result = memberPaymentsTransformer.toEtmp(memberPayments)

        result shouldMatchTo etmpMemberPayments
      }

      "In Progress with 0 contributions: case (true, 0, false)" in {

        val memberPayments: MemberPayments = transformEmployerContributionsToEtmp(
          made = true,
          completed = false,
          employerContributions = List.empty
        )

        val etmpMemberPayments: EtmpMemberPayments = transformEmployerContributionsFromEtmp(
          employerContributionMade = Some(true),
          noOfContributions = None,
          employerContributions = None
        )

        val result = memberPaymentsTransformer.toEtmp(memberPayments)

        result shouldMatchTo etmpMemberPayments
      }

      "In Progress with 1+ contributions: case (true, _, false)" in {

        val memberPayments: MemberPayments = transformEmployerContributionsToEtmp(
          made = true,
          completed = false,
          employerContributions = List(sampleEmployerContribution2, sampleEmployerContribution3)
        )

        val etmpMemberPayments: EtmpMemberPayments = transformEmployerContributionsFromEtmp(
          employerContributionMade = Some(false), // transformed from true in this case
          noOfContributions = Some(2),
          employerContributions = Some(List(sampleEtmpEmployerContribution2, sampleEtmpEmployerContribution3))
        )

        val result = memberPaymentsTransformer.toEtmp(memberPayments)

        result shouldMatchTo etmpMemberPayments
      }

      "Not Started: case (false, 0, false)" in {

        val memberPayments: MemberPayments = transformEmployerContributionsToEtmp(
          made = false,
          completed = false,
          employerContributions = List.empty
        )

        val etmpMemberPayments: EtmpMemberPayments = transformEmployerContributionsFromEtmp(
          employerContributionMade = None,
          noOfContributions = None,
          employerContributions = None
        )

        val result = memberPaymentsTransformer.toEtmp(memberPayments)

        result shouldMatchTo etmpMemberPayments
      }

      "Not valid state: case (false, 0, true)" in {

        val memberPayments: MemberPayments = transformEmployerContributionsToEtmp(
          made = false,
          completed = true,
          employerContributions = List.empty
        )

        val etmpMemberPayments: EtmpMemberPayments = transformEmployerContributionsFromEtmp(
          employerContributionMade = Some(false),
          noOfContributions = None,
          employerContributions = None
        )

        val result = memberPaymentsTransformer.toEtmp(memberPayments)

        result shouldMatchTo etmpMemberPayments
      }
    }

    "successfully transform Employer Contributions section from ETMP format" when {

      "Answer is \"No\" and 0 contributions made: case (Some(false), 0)" in {

        val etmpMemberPayments: EtmpMemberPayments = transformEmployerContributionsFromEtmp(
          employerContributionMade = Some(false),
          noOfContributions = None,
          employerContributions = None
        )

        val memberPayments: MemberPayments = transformEmployerContributionsToEtmp(
          made = false,
          completed = true,
          employerContributions = List.empty
        )

        val result = memberPaymentsTransformer.fromEtmp(etmpMemberPayments)

        result shouldMatchTo Right(memberPayments)
      }

      "Answer is \"No\" (transformed from \"Yes\") and 1+ contributions made: case (Some(false), _)" in {

        val etmpMemberPayments: EtmpMemberPayments = transformEmployerContributionsFromEtmp(
          employerContributionMade = Some(false),
          noOfContributions = Some(1),
          employerContributions = Some(List(sampleEtmpEmployerContribution4))
        )

        val memberPayments: MemberPayments = transformEmployerContributionsToEtmp(
          made = false,
          completed = false,
          employerContributions = List(sampleEmployerContribution4)
        )

        val result = memberPaymentsTransformer.fromEtmp(etmpMemberPayments)

        result shouldMatchTo Right(memberPayments)
      }

      "Answer is \"Yes\" and 0 contributions made: case (Some(true), 0)" in {

        val etmpMemberPayments: EtmpMemberPayments = transformEmployerContributionsFromEtmp(
          employerContributionMade = Some(true),
          noOfContributions = None,
          employerContributions = None
        )

        val memberPayments: MemberPayments = transformEmployerContributionsToEtmp(
          made = true,
          completed = false,
          employerContributions = List.empty
        )

        val result = memberPaymentsTransformer.fromEtmp(etmpMemberPayments)

        result shouldMatchTo Right(memberPayments)
      }

      "Answer is \"Yes\" and 1+ contributions made: case (Some(true), _)" in {

        val etmpMemberPayments: EtmpMemberPayments = transformEmployerContributionsFromEtmp(
          employerContributionMade = Some(true),
          noOfContributions = Some(2),
          employerContributions = Some(List(sampleEtmpEmployerContribution1, sampleEtmpEmployerContribution2))
        )

        val memberPayments: MemberPayments = transformEmployerContributionsToEtmp(
          made = true,
          completed = true,
          employerContributions = List(sampleEmployerContribution1, sampleEmployerContribution2)
        )

        val result = memberPaymentsTransformer.fromEtmp(etmpMemberPayments)

        result shouldMatchTo Right(memberPayments)
      }

      "Answer not provided: case (None, 0)" in {

        val etmpMemberPayments: EtmpMemberPayments = transformEmployerContributionsFromEtmp(
          employerContributionMade = None,
          noOfContributions = None,
          employerContributions = None
        )

        val memberPayments: MemberPayments = transformEmployerContributionsToEtmp(
          made = false,
          completed = false,
          employerContributions = List.empty
        )

        val result = memberPaymentsTransformer.fromEtmp(etmpMemberPayments)

        result shouldMatchTo Right(memberPayments)
      }

      "Not valid state: case (None, 1)" in {

        val memberPayments: MemberPayments = transformEmployerContributionsToEtmp(
          made = false,
          completed = true,
          employerContributions = List.empty
        )

        val etmpMemberPayments: EtmpMemberPayments = transformEmployerContributionsFromEtmp(
          employerContributionMade = Some(false),
          noOfContributions = None,
          employerContributions = None
        )

        val result = memberPaymentsTransformer.fromEtmp(etmpMemberPayments)

        result shouldMatchTo Right(memberPayments)
      }
    }

    "successfully transform Member Details section to ETMP format" when {
      "member state is Changed" in {
        val memberDetails = sampleMemberDetails1.copy(state = MemberState.Changed)
        val memberPayments = sampleMemberPayments.copy(memberDetails = List(memberDetails))

        val etmpMemberDetails = sampleEtmpMemberDetail1.copy(memberStatus = SectionStatus.Changed)
        val etmpMemberPayments = sampleEtmpMemberPayments.copy(memberDetails = List(etmpMemberDetails))

        val result = memberPaymentsTransformer.toEtmp(memberPayments)

        result shouldMatchTo etmpMemberPayments
      }

      "member state is Deleted" in {
        val memberDetails = sampleMemberDetails1.copy(state = MemberState.Deleted)
        val memberPayments = sampleMemberPayments.copy(memberDetails = List(memberDetails))

        val etmpMemberDetails = sampleEtmpMemberDetail1.copy(memberStatus = SectionStatus.Deleted)
        val etmpMemberPayments = sampleEtmpMemberPayments.copy(memberDetails = List(etmpMemberDetails))

        val result = memberPaymentsTransformer.toEtmp(memberPayments)

        result shouldMatchTo etmpMemberPayments
      }

      "there are no transfers in" in {
        val memberDetails = sampleMemberDetails1.copy(transfersIn = Nil)
        val memberPayments = sampleMemberPayments.copy(memberDetails = List(memberDetails))

        val etmpMemberDetails = sampleEtmpMemberDetail1.copy(memberTransfersIn = None, noOfTransfersIn = Some(0))
        val etmpMemberPayments = sampleEtmpMemberPayments.copy(memberDetails = List(etmpMemberDetails))

        val result = memberPaymentsTransformer.toEtmp(memberPayments)

        result shouldMatchTo etmpMemberPayments
      }

      "there are no transfers out" in {
        val memberDetails = sampleMemberDetails1.copy(transfersOut = Nil)
        val memberPayments = sampleMemberPayments.copy(memberDetails = List(memberDetails))

        val etmpMemberDetails = sampleEtmpMemberDetail1.copy(memberTransfersOut = None, noOfTransfersOut = Some(0))
        val etmpMemberPayments = sampleEtmpMemberPayments.copy(memberDetails = List(etmpMemberDetails))

        val result = memberPaymentsTransformer.toEtmp(memberPayments)

        result shouldMatchTo etmpMemberPayments
      }
    }

    "successfully transform Member Details section from ETMP format" when {
      "member state is Changed" in {
        val memberDetails = sampleMemberDetails1.copy(state = MemberState.Changed)
        val memberPayments = sampleMemberPayments.copy(memberDetails = List(memberDetails))

        val etmpMemberDetails = sampleEtmpMemberDetail1.copy(memberStatus = SectionStatus.Changed)
        val etmpMemberPayments = sampleEtmpMemberPayments.copy(memberDetails = List(etmpMemberDetails))

        val result = memberPaymentsTransformer.fromEtmp(etmpMemberPayments)

        result shouldMatchTo Right(memberPayments)
      }

      "member state is Deleted" in {
        val memberDetails = sampleMemberDetails1.copy(state = MemberState.Deleted)
        val memberPayments = sampleMemberPayments.copy(memberDetails = List(memberDetails))

        val etmpMemberDetails = sampleEtmpMemberDetail1.copy(memberStatus = SectionStatus.Deleted)
        val etmpMemberPayments = sampleEtmpMemberPayments.copy(memberDetails = List(etmpMemberDetails))

        val result = memberPaymentsTransformer.fromEtmp(etmpMemberPayments)

        result shouldMatchTo Right(memberPayments)
      }

      "there are no transfers in" in {
        val memberDetails = sampleMemberDetails1.copy(transfersIn = Nil)
        val memberPayments = sampleMemberPayments.copy(memberDetails = List(memberDetails))

        val etmpMemberDetails = sampleEtmpMemberDetail1.copy(memberTransfersIn = None)
        val etmpMemberPayments = sampleEtmpMemberPayments.copy(memberDetails = List(etmpMemberDetails))

        val result = memberPaymentsTransformer.fromEtmp(etmpMemberPayments)

        result shouldMatchTo Right(memberPayments)
      }

      "there are no transfers out" in {
        val memberDetails = sampleMemberDetails1.copy(transfersOut = Nil)
        val memberPayments = sampleMemberPayments.copy(memberDetails = List(memberDetails))

        val etmpMemberDetails = sampleEtmpMemberDetail1.copy(memberTransfersOut = None)
        val etmpMemberPayments = sampleEtmpMemberPayments.copy(memberDetails = List(etmpMemberDetails))

        val result = memberPaymentsTransformer.fromEtmp(etmpMemberPayments)

        result shouldMatchTo Right(memberPayments)
      }
    }
  }

  private def transformPensionPaymentsToEtmp(
    made: Boolean,
    completed: Boolean,
    pensionAmountReceived: Option[Double]
  ): MemberPayments =
    sampleMemberPayments.copy(
      pensionReceived = SectionDetails(
        made = made,
        completed = completed
      ),
      memberDetails = sampleMemberPayments.memberDetails.map(_.copy(pensionAmountReceived = pensionAmountReceived))
    )

  private def transformPensionPaymentsFromEtmp(
    pensionReceived: Option[Boolean],
    pensionAmountReceived: Option[Double]
  ): EtmpMemberPayments =
    sampleEtmpMemberPayments.copy(
      pensionReceived = pensionReceived,
      memberDetails = sampleEtmpMemberPayments.memberDetails.map(_.copy(pensionAmountReceived = pensionAmountReceived))
    )

  private def transformSurrenderedBenefits(
    made: Boolean,
    completed: Boolean,
    surrenderedBenefits: Option[PensionSurrender]
  ): MemberPayments =
    sampleMemberPayments.copy(
      benefitsSurrenderedDetails = SectionDetails(
        made = made,
        completed = completed
      ),
      memberDetails = sampleMemberPayments.memberDetails.map(_.copy(benefitsSurrendered = surrenderedBenefits))
    )

  private def transformEtmpSurrenderedBenefits(
    surrenderMade: Option[Boolean],
    surrenderedBenefits: Option[List[EtmpPensionSurrender]]
  ): EtmpMemberPayments =
    sampleEtmpMemberPayments.copy(
      surrenderMade = surrenderMade,
      memberDetails = sampleEtmpMemberPayments.memberDetails.map(_.copy(memberPensionSurrender = surrenderedBenefits))
    )

  private def transformEmployerContributionsToEtmp(
    made: Boolean,
    completed: Boolean,
    employerContributions: List[EmployerContributions]
  ): MemberPayments =
    sampleMemberPayments.copy(
      employerContributionsDetails = SectionDetails(
        made = made,
        completed = completed
      ),
      memberDetails = sampleMemberPayments.memberDetails.map(_.copy(employerContributions = employerContributions))
    )

  private def transformEmployerContributionsFromEtmp(
    employerContributionMade: Option[YesNo],
    noOfContributions: Option[Int],
    employerContributions: Option[List[EtmpEmployerContributions]]
  ): EtmpMemberPayments =
    sampleEtmpMemberPayments.copy(
      employerContributionMade = employerContributionMade,
      memberDetails = sampleEtmpMemberPayments.memberDetails.map(
        _.copy(
          noOfContributions = if (noOfContributions.isEmpty) Some(0) else noOfContributions,
          memberEmpContribution = employerContributions
        )
      )
    )
}
