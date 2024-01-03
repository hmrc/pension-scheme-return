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

package uk.gov.hmrc.pensionschemereturn.transformations

import org.mockito.MockitoSugar
import org.scalatestplus.play.PlaySpec
import uk.gov.hmrc.pensionschemereturn.models.etmp.nonsipp.EtmpIdentityType
import uk.gov.hmrc.pensionschemereturn.models.nonsipp.IdentityType

class TransformerSpec extends PlaySpec with MockitoSugar with Transformer {
  "Transformer" should {
    "successfully transform boolean to Yes or No" in {
      toYesNo(true) mustEqual "Yes"
      toYesNo(false) mustEqual "No"
    }

    "successfully transform optional boolean to Yes or No" in {
      optToYesNo(Some("")) mustEqual "Yes"
      optToYesNo(None) mustEqual "No"
    }

    "successfully transform Yes or No to boolean" in {
      fromYesNo(Yes) mustBe true
      fromYesNo(No) mustBe false
    }

    "successfully transform boolean to EtmpConnectedPartyStatus" in {
      transformToEtmpConnectedPartyStatus(true) mustBe "01"
      transformToEtmpConnectedPartyStatus(false) mustBe "02"
    }

    "successfully transform to EtmpIdentityType - Individual" in {
      toEtmpIdentityType(IdentityType.Individual, Some("IdNumber"), None, None) mustEqual EtmpIdentityType(
        "01",
        Some("IdNumber"),
        None,
        None
      )
      toEtmpIdentityType(IdentityType.Individual, None, Some("ReasonNoIdNumber"), None) mustEqual EtmpIdentityType(
        "01",
        None,
        Some("ReasonNoIdNumber"),
        None
      )
    }

    "successfully transform to EtmpIdentityType - Company" in {
      toEtmpIdentityType(IdentityType.UKCompany, Some("IdNumber"), None, None) mustEqual EtmpIdentityType(
        "02",
        Some("IdNumber"),
        None,
        None
      )
      toEtmpIdentityType(IdentityType.UKCompany, None, Some("ReasonNoIdNumber"), None) mustEqual EtmpIdentityType(
        "02",
        None,
        Some("ReasonNoIdNumber"),
        None
      )
    }

    "successfully transform to EtmpIdentityType - Partnership" in {
      toEtmpIdentityType(IdentityType.UKPartnership, Some("IdNumber"), None, None) mustEqual EtmpIdentityType(
        "03",
        Some("IdNumber"),
        None,
        None
      )
      toEtmpIdentityType(IdentityType.UKPartnership, None, Some("ReasonNoIdNumber"), None) mustEqual EtmpIdentityType(
        "03",
        None,
        Some("ReasonNoIdNumber"),
        None
      )
    }

    "successfully transform to EtmpIdentityType - Other" in {
      toEtmpIdentityType(IdentityType.Other, None, None, Some("OtherDescription")) mustEqual EtmpIdentityType(
        "04",
        None,
        None,
        Some("OtherDescription")
      )
    }
  }
}
