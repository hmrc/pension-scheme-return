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

import uk.gov.hmrc.pensionschemereturn.models.etmp.YesNo
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

  "MemberPaymentsTransformer" should {
    "successfully transform to ETMP format" in {
      val result = memberPaymentsTransformer.toEtmp(sampleMemberPayments)

      result shouldMatchTo sampleEtmpMemberPayments
    }

    "successfully transform from ETMP format" in {
      val result = memberPaymentsTransformer.fromEtmp(sampleEtmpMemberPayments)

      result shouldMatchTo Right(sampleMemberPayments)
    }

    "successfully transform surrenderedBenefits section to ETMP format" when {

      "surrenders made is TRUE, completed is TRUE and BOTH surrendered benefits are provided" in {

        val memberPayments = transformSurrenderedBenefits(
          made = true,
          completed = true,
          surrenderedBenefits = Some(pensionSurrender)
        )

        val etmpMemberPayments = transformEtmpSurrenderedBenefits(
          surrenderMade = true,
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
          surrenderMade = false,
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
          surrenderMade = true,
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
          surrenderMade = false,
          surrenderedBenefits = None
        )

        val result = memberPaymentsTransformer.toEtmp(memberPayments)

        result shouldMatchTo etmpMemberPayments
      }
    }

    "successfully transform surrenderedBenefits section from ETMP format" when {
      "surrenders made is TRUE, BOTH surrendered benefits are provided" in {

        val etmpMemberPayments = transformEtmpSurrenderedBenefits(
          surrenderMade = true,
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
          surrenderMade = true,
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
          surrenderMade = false,
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
    }
  }

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
    surrenderMade: Boolean,
    surrenderedBenefits: Option[List[EtmpPensionSurrender]]
  ): EtmpMemberPayments =
    sampleEtmpMemberPayments.copy(
      surrenderMade = Some(surrenderMade),
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
