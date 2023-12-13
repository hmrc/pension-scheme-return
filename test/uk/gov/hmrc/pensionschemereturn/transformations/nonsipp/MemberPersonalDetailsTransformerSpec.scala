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

import uk.gov.hmrc.pensionschemereturn.base.EtmpTransformerSpec
import uk.gov.hmrc.pensionschemereturn.models.etmp.nonsipp.EtmpMemberPersonalDetails
import uk.gov.hmrc.pensionschemereturn.models.nonsipp.MemberPersonalDetails
import uk.gov.hmrc.pensionschemereturn.transformations.TransformerError

class MemberPersonalDetailsTransformerSpec extends EtmpTransformerSpec {

  private val memberPersonalDetails = MemberPersonalDetails(
    firstName = "test first one",
    lastName = "test last one",
    ninoOrReason = Right("nino"),
    dateOfBirth = sampleToday
  )

  private val etmpMemberPersonalDetails = EtmpMemberPersonalDetails(
    foreName = "test first one",
    middleName = None,
    lastName = "test last one",
    nino = Some("nino"),
    reasonNoNino = None,
    dateOfBirth = sampleToday
  )

  "MemberPersonalDetailsTransformer" should {
    "successfully transform to ETMP format" in {
      val result = memberPersonalDetailsTransformer.toEtmp(memberPersonalDetails)

      result shouldMatchTo etmpMemberPersonalDetails
    }

    "successfully transform from ETMP format" in {
      val result = memberPersonalDetailsTransformer.fromEtmp(etmpMemberPersonalDetails)

      result shouldMatchTo Right(memberPersonalDetails)
    }

    "fail to transform from ETMP format when both nino and no nino reason are missing" in {
      val incorrectEtmpMemberPersonalDetails = EtmpMemberPersonalDetails(
        foreName = "test first one",
        middleName = None,
        lastName = "test last one",
        nino = None,
        reasonNoNino = None,
        dateOfBirth = sampleToday
      )

      val result = memberPersonalDetailsTransformer.fromEtmp(incorrectEtmpMemberPersonalDetails)

      result shouldMatchTo Left(TransformerError.NoNinoOrReason)
    }
  }
}
