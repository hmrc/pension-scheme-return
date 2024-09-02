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
import uk.gov.hmrc.pensionschemereturn.models.etmp.SubmitterType
import uk.gov.hmrc.pensionschemereturn.models.nonsipp.PsrDeclaration
import uk.gov.hmrc.pensionschemereturn.models.etmp.nonsipp._
import uk.gov.hmrc.pensionschemereturn.transformations.Transformer

@Singleton()
class PsrDeclarationToEtmp @Inject() extends Transformer {

  def transform(psrDeclaration: PsrDeclaration): EtmpPsrDeclaration =
    EtmpPsrDeclaration(
      submittedBy = psrDeclaration.submittedBy,
      submitterId = psrDeclaration.submitterId,
      psaId = psrDeclaration.optAuthorisingPSAID,
      psaDeclaration = Option.when(SubmitterType.PSA == psrDeclaration.submittedBy)(
        EtmpPsaDeclaration(psaDeclaration1 = psrDeclaration.declaration1, psaDeclaration2 = psrDeclaration.declaration2)
      ),
      pspDeclaration = Option.when(SubmitterType.PSP == psrDeclaration.submittedBy)(
        EtmpPspDeclaration(pspDeclaration1 = psrDeclaration.declaration1, pspDeclaration2 = psrDeclaration.declaration2)
      )
    )
}
