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

import uk.gov.hmrc.pensionschemereturn.models.etmp.SubmitterType
import uk.gov.hmrc.pensionschemereturn.models.nonsipp.PsrDeclaration
import uk.gov.hmrc.pensionschemereturn.base.EtmpTransformerSpec
import uk.gov.hmrc.pensionschemereturn.models.etmp.nonsipp.{EtmpPsaDeclaration, EtmpPspDeclaration, EtmpPsrDeclaration}
import com.softwaremill.diffx.generic.auto.indicator

class PsrDeclarationToEtmpSpec extends EtmpTransformerSpec {

  private val transformation: PsrDeclarationToEtmp = new PsrDeclarationToEtmp()

  "PsrDeclarationToEtmp" should {
    "transform correctly for a PSA" in {

      val psrSubmission: PsrDeclaration = PsrDeclaration(
        submittedBy = SubmitterType.PSA,
        submitterId = "submitterId",
        optAuthorisingPSAID = Some("psaId"),
        declaration1 = true,
        declaration2 = false
      )

      val expected = EtmpPsrDeclaration(
        submittedBy = SubmitterType.PSA,
        submitterId = "submitterId",
        psaId = Some("psaId"),
        psaDeclaration = Some(EtmpPsaDeclaration(psaDeclaration1 = true, psaDeclaration2 = false)),
        pspDeclaration = None
      )

      transformation.transform(psrSubmission) shouldMatchTo expected
    }

    "transform correctly for a PSP" in {

      val psrSubmission: PsrDeclaration = PsrDeclaration(
        submittedBy = SubmitterType.PSP,
        submitterId = "submitterId",
        optAuthorisingPSAID = Some("psaId"),
        declaration1 = true,
        declaration2 = false
      )

      val expected = EtmpPsrDeclaration(
        submittedBy = SubmitterType.PSP,
        submitterId = "submitterId",
        psaId = Some("psaId"),
        psaDeclaration = None,
        pspDeclaration = Some(EtmpPspDeclaration(pspDeclaration1 = true, pspDeclaration2 = false))
      )

      transformation.transform(psrSubmission) shouldMatchTo expected
    }
  }
}
