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

import uk.gov.hmrc.pensionschemereturn.models.nonsipp.memberpayments.{MemberPayments, PensionSurrender, SectionDetails}
import uk.gov.hmrc.pensionschemereturn.base.EtmpTransformerSpec
import uk.gov.hmrc.pensionschemereturn.models.etmp.nonsipp.memberpayments.{EtmpMemberPayments, EtmpPensionSurrender}

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
      surrenderMade = surrenderMade,
      memberDetails = sampleEtmpMemberPayments.memberDetails.map(_.copy(memberPensionSurrender = surrenderedBenefits))
    )
}
