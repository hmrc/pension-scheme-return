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

import com.google.inject.{Inject, Singleton}
import uk.gov.hmrc.pensionschemereturn.models.etmp.PSA
import uk.gov.hmrc.pensionschemereturn.models.nonsipp.PsrDeclaration
import uk.gov.hmrc.pensionschemereturn.models.etmp.nonsipp.EtmpPsrDeclaration
import uk.gov.hmrc.pensionschemereturn.transformations.Transformer

@Singleton()
class PsrDeclarationFromEtmp @Inject() extends Transformer {

  def transform(psrDeclaration: EtmpPsrDeclaration): PsrDeclaration = {
    val submittedByPsa = PSA == psrDeclaration.submittedBy
    PsrDeclaration(
      submittedBy = psrDeclaration.submittedBy,
      submitterId = psrDeclaration.submitterId,
      optAuthorisingPSAID = psrDeclaration.psaId,
      declaration1 =
        if (submittedByPsa) psrDeclaration.psaDeclaration.get.psaDeclaration1
        else psrDeclaration.pspDeclaration.get.pspDeclaration1,
      declaration2 =
        if (submittedByPsa) psrDeclaration.psaDeclaration.get.psaDeclaration2
        else psrDeclaration.pspDeclaration.get.pspDeclaration2
    )
  }
}
