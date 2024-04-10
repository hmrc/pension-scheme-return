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
import uk.gov.hmrc.pensionschemereturn.models.nonsipp.memberpayments.PensionSurrender
import uk.gov.hmrc.pensionschemereturn.models.etmp.nonsipp.memberpayments.EtmpPensionSurrender
import uk.gov.hmrc.pensionschemereturn.transformations.{ETMPTransformer, TransformerError}

@Singleton()
class PensionSurrenderTransformer @Inject() extends ETMPTransformer[PensionSurrender, EtmpPensionSurrender] {
  override def toEtmp(in: PensionSurrender): EtmpPensionSurrender =
    EtmpPensionSurrender(
      totalSurrendered = in.totalSurrendered,
      dateOfSurrender = in.dateOfSurrender,
      surrenderReason = in.surrenderReason
    )

  override def fromEtmp(out: EtmpPensionSurrender): Either[TransformerError, PensionSurrender] =
    Right(
      PensionSurrender(
        totalSurrendered = out.totalSurrendered,
        dateOfSurrender = out.dateOfSurrender,
        surrenderReason = out.surrenderReason
      )
    )
}
