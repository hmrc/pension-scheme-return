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

import uk.gov.hmrc.pensionschemereturn.models.etmp.YesNo
import uk.gov.hmrc.pensionschemereturn.models.etmp.nonsipp.common.EtmpIdentityType
import uk.gov.hmrc.pensionschemereturn.models.etmp.nonsipp.memberpayments.TransferSchemeType
import uk.gov.hmrc.pensionschemereturn.models.nonsipp.IdentityType
import uk.gov.hmrc.pensionschemereturn.models.nonsipp.IdentityType.identityTypeToString
import uk.gov.hmrc.pensionschemereturn.models.nonsipp.memberpayments.PensionSchemeType
import uk.gov.hmrc.pensionschemereturn.transformations.TransformerError.UnknownError

import scala.language.implicitConversions

trait Transformer {

  protected val Sponsoring: String = "sponsoring"
  protected val ConnectedParty: String = "connectedParty"
  protected val Neither: String = "neither"

  protected val Yes: String = "Yes"
  protected val No: String = "No"

  protected val Connected: String = "01"
  protected val Unconnected: String = "02"

  protected def toYesNo(condition: Boolean): String = if (condition) Yes else No

  protected def optToYesNo(optValue: Option[_]): String = optValue.map(_ => Yes).getOrElse(No)

  protected def fromYesNo(value: String): Boolean = value == Yes

  protected def toEtmpIdentityType(
    identityType: IdentityType,
    optIdNumber: Option[String],
    optReasonNoIdNumber: Option[String],
    optOtherDescription: Option[String]
  ): EtmpIdentityType =
    EtmpIdentityType(
      indivOrOrgType = identityTypeToString(identityType),
      idNumber = optIdNumber,
      reasonNoIdNumber = optReasonNoIdNumber,
      otherDescription = optOtherDescription
    )

  protected def toPensionSchemeType(o: TransferSchemeType): Either[TransformerError, PensionSchemeType] = o match {
    case TransferSchemeType("01", Some(ref), None) => Right(PensionSchemeType.RegisteredPS(ref))
    case TransferSchemeType("02", Some(ref), None) => Right(PensionSchemeType.QualifyingRecognisedOverseasPS(ref))
    case TransferSchemeType("03", None, Some(description)) => Right(PensionSchemeType.Other(description))
    case unknown => Left(UnknownError(s"toPensionSchemeType unknown pattern $unknown"))
  }

  protected def transformToEtmpConnectedPartyStatus(condition: Boolean): String =
    if (condition) Connected else Unconnected

  protected def toTransferSchemeType(o: PensionSchemeType): TransferSchemeType = o match {
    case PensionSchemeType.RegisteredPS(ref) => TransferSchemeType.registeredScheme(ref)
    case PensionSchemeType.QualifyingRecognisedOverseasPS(ref) => TransferSchemeType.qrops(ref)
    case PensionSchemeType.Other(description) => TransferSchemeType.other(description)
  }

  // Used to easily convert optional ETMP data where 1 is required to an Either
  // e.g. Nino or Reason for No Nino
  // returns None if both are empty
  implicit class Tuple2OptionOps[A, B](tup: (Option[A], Option[B])) {
    def toEither(error: TransformerError): Either[TransformerError, Either[A, B]] = tup match {
      case (Some(a), _) => Right(Left(a))
      case (_, Some(b)) => Right(Right(b))
      case _ => Left(error)
    }
  }

  implicit def implicitToYesNo(bool: Boolean): YesNo = YesNo(bool)
}
