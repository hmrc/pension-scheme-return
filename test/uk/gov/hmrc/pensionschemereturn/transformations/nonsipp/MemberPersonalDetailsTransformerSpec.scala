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

import uk.gov.hmrc.pensionschemereturn.models.nonsipp.memberpayments.MemberPersonalDetails
import uk.gov.hmrc.pensionschemereturn.base.EtmpTransformerSpec
import uk.gov.hmrc.pensionschemereturn.models.etmp.nonsipp.memberpayments.EtmpMemberPersonalDetails
import com.softwaremill.diffx.generic.auto.indicator

class MemberPersonalDetailsTransformerSpec extends EtmpTransformerSpec {

  private val memberPersonalDetails = MemberPersonalDetails(
    firstName = "test first one",
    lastName = "test last one",
    nino = Some("nino"),
    reasonNoNINO = None,
    dateOfBirth = sampleToday
  )

  private val etmpMemberPersonalDetails = EtmpMemberPersonalDetails(
    foreName = "test first one",
    middleName = None,
    lastName = "test last one",
    nino = Some("nino"),
    reasonNoNINO = None,
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
  }
}
