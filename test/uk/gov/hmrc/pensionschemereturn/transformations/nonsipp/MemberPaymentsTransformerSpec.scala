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

import uk.gov.hmrc.pensionschemereturn.models.etmp.SectionStatus
import uk.gov.hmrc.pensionschemereturn.models.nonsipp.memberpayments._
import uk.gov.hmrc.pensionschemereturn.base.EtmpTransformerSpec

class MemberPaymentsTransformerSpec extends EtmpTransformerSpec {

  "MemberPaymentsTransformer" should {
    "successfully transform to ETMP format" in {
      val result = memberPaymentsTransformer.toEtmp(sampleMemberPayments)

      result shouldMatchTo sampleEtmpMemberPayments
    }

    "successfully transform from ETMP format" in {
      val result = memberPaymentsTransformer.fromEtmp(sampleEtmpMemberPayments)

      result shouldMatchTo Right(sampleMemberPayments)
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
}
