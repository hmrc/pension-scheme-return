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

import com.google.inject.{Inject, Singleton}
import org.slf4j.LoggerFactory
import uk.gov.hmrc.pensionschemereturn.models.etmp.nonsipp.{EtmpTransfersIn, TransferSchemeType}
import uk.gov.hmrc.pensionschemereturn.models.nonsipp.{PensionSchemeType, TransfersIn}
import uk.gov.hmrc.pensionschemereturn.transformations.TransformerError.UnknownError
import uk.gov.hmrc.pensionschemereturn.transformations.{ETMPTransformer, TransformerError}

@Singleton()
class TransferInTransformer @Inject() extends ETMPTransformer[TransfersIn, EtmpTransfersIn] {

  private val logger = LoggerFactory.getLogger(getClass)

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

  private def toTransferSchemeType(o: PensionSchemeType): TransferSchemeType = o match {
    case PensionSchemeType.RegisteredPS(ref) => TransferSchemeType.registeredScheme(ref)
    case PensionSchemeType.QualifyingRecognisedOverseasPS(ref) => TransferSchemeType.qrops(ref)
    case PensionSchemeType.Other(description) => TransferSchemeType.other(description)
  }

  private def toPensionSchemeType(o: TransferSchemeType): Either[TransformerError, PensionSchemeType] = o match {
    case TransferSchemeType("01", Some(ref), None) => Right(PensionSchemeType.RegisteredPS(ref))
    case TransferSchemeType("02", Some(ref), None) => Right(PensionSchemeType.QualifyingRecognisedOverseasPS(ref))
    case TransferSchemeType("03", None, Some(description)) => Right(PensionSchemeType.Other(description))
    case unknown =>
      logger.error(s"toPensionSchemeType unknown pattern $unknown")
      Left(UnknownError(s"toPensionSchemeType unknown pattern $unknown"))
  }
}
