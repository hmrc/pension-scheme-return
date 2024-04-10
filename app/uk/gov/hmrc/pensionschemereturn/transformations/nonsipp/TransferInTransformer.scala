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
import uk.gov.hmrc.pensionschemereturn.models.nonsipp.memberpayments.TransfersIn
import uk.gov.hmrc.pensionschemereturn.models.etmp.nonsipp.memberpayments.EtmpTransfersIn
import uk.gov.hmrc.pensionschemereturn.transformations.{ETMPTransformer, TransformerError}

@Singleton()
class TransferInTransformer @Inject() extends ETMPTransformer[TransfersIn, EtmpTransfersIn] {

  override def toEtmp(in: TransfersIn): EtmpTransfersIn =
    EtmpTransfersIn(
      schemeName = in.schemeName,
      dateOfTransfer = in.dateOfTransfer,
      transferSchemeType = toTransferSchemeType(in.transferSchemeType),
      transferValue = in.transferValue,
      transferIncludedAsset = in.transferIncludedAsset
    )

  override def fromEtmp(out: EtmpTransfersIn): Either[TransformerError, TransfersIn] =
    for {
      schemeType <- toPensionSchemeType(out.transferSchemeType)
    } yield TransfersIn(
      schemeName = out.schemeName,
      dateOfTransfer = out.dateOfTransfer,
      transferSchemeType = schemeType,
      transferValue = out.transferValue,
      transferIncludedAsset = out.transferIncludedAsset.boolean
    )
}
