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

package uk.gov.hmrc.pensionschemereturn.services

import uk.gov.hmrc.pensionschemereturn.validators.SchemaPaths.{API_1999, API_1999_optional, API_1999_v115}
import uk.gov.hmrc.pensionschemereturn.config.AppConfig
import play.api.mvc.RequestHeader
import com.google.inject.{Inject, Singleton}
import uk.gov.hmrc.pensionschemereturn.auth.PsrAuthContext
import uk.gov.hmrc.pensionschemereturn.models.etmp.{Compiled, Submitted}
import uk.gov.hmrc.pensionschemereturn.transformations.nonsipp.{PsrSubmissionToEtmp, StandardPsrFromEtmp}
import uk.gov.hmrc.pensionschemereturn.models.nonsipp.PsrSubmission
import uk.gov.hmrc.http._
import uk.gov.hmrc.pensionschemereturn.connectors.PsrConnector
import uk.gov.hmrc.pensionschemereturn.models.enumeration.CipPsrStatus
import uk.gov.hmrc.pensionschemereturn.models.PensionSchemeReturnValidationFailureException
import play.api.Logging
import play.api.libs.json._
import uk.gov.hmrc.pensionschemereturn.validators.JSONSchemaValidator
import uk.gov.hmrc.pensionschemereturn.transformations.TransformerError

import scala.concurrent.{ExecutionContext, Future}

@Singleton()
class PsrSubmissionService @Inject() (
  psrConnector: PsrConnector,
  jsonPayloadSchemaValidator: JSONSchemaValidator,
  psrSubmissionToEtmp: PsrSubmissionToEtmp,
  standardPsrFromEtmp: StandardPsrFromEtmp,
  config: AppConfig
) extends Logging {

  def submitStandardPsr(
    psrSubmission: PsrSubmission,
    psrAuth: PsrAuthContext[Any],
    userName: String,
    schemeName: String
  )(implicit headerCarrier: HeaderCarrier, ec: ExecutionContext, request: RequestHeader): Future[HttpResponse] = {
    val payloadAsJson = Json.toJson(psrSubmissionToEtmp.transform(psrSubmission))
    val schema = if (config.submitPsrSchemaVersionV120) API_1999 else API_1999_v115
    logger.info(s"Using schema version $schema for PSR Submission")
    val validationResult = jsonPayloadSchemaValidator.validatePayload(schema, payloadAsJson)
    if (validationResult.hasErrors) {
      throw PensionSchemeReturnValidationFailureException(
        s"Invalid payload when submitStandardPsr :-\n${validationResult.toString}"
      )
    } else {
      psrConnector
        .submitStandardPsr(
          psrSubmission.minimalRequiredSubmission.reportDetails.pstr,
          payloadAsJson,
          schemeName,
          psrAuth.psaPspId,
          psrAuth.credentialRole,
          userName,
          getCipPsrStatus(psrSubmission)
        )
        .recover { case badReq: BadRequestException =>
          throw new ExpectationFailedException(s"${badReq.message}")
        }
    }
  }

  def submitPrePopulatedPsr(
    psrSubmission: PsrSubmission,
    psrAuth: PsrAuthContext[Any],
    userName: String,
    schemeName: String
  )(implicit headerCarrier: HeaderCarrier, ec: ExecutionContext, request: RequestHeader): Future[HttpResponse] = {
    val payloadAsJson = Json.toJson(psrSubmissionToEtmp.transform(psrSubmission))
    val validationResult = jsonPayloadSchemaValidator.validatePayload(API_1999_optional, payloadAsJson)
    if (validationResult.hasErrors) {
      throw PensionSchemeReturnValidationFailureException(
        s"Invalid payload when submitPrePopulatedPsr :-\n${validationResult.toString}"
      )
    } else {
      psrConnector
        .submitStandardPsr(
          psrSubmission.minimalRequiredSubmission.reportDetails.pstr,
          payloadAsJson,
          schemeName,
          psrAuth.psaPspId,
          psrAuth.credentialRole,
          userName,
          getCipPsrStatus(psrSubmission)
        )
        .recover { case badReq: BadRequestException =>
          throw new ExpectationFailedException(s"${badReq.message}")
        }
    }
  }
  def getCipPsrStatus(psrSubmission: PsrSubmission): Option[String] =
    (
      psrSubmission.minimalRequiredSubmission.reportDetails.fbVersion,
      psrSubmission.minimalRequiredSubmission.reportDetails.fbstatus
    ) match {
      case (Some(version), Some(Compiled)) if version.toInt > 1 => Some(CipPsrStatus.CHANGED_COMPILED.toString)
      case (Some(version), Some(Submitted)) if version.toInt > 1 => Some(CipPsrStatus.CHANGED_SUBMITTED.toString)
      case (None, Some(Compiled)) => Some(CipPsrStatus.CHANGED_COMPILED.toString)
      case (None, Some(Submitted)) => Some(CipPsrStatus.CHANGED_SUBMITTED.toString)
      case _ =>
        None // requirement is: If this is the Compiled and submitted return, we don't need this field to be populated.
    }

  def getStandardPsr(
    pstr: String,
    optFbNumber: Option[String],
    optPeriodStartDate: Option[String],
    optPsrVersion: Option[String],
    psrAuth: PsrAuthContext[Any],
    userName: String,
    schemeName: String
  )(implicit
    headerCarrier: HeaderCarrier,
    ec: ExecutionContext,
    request: RequestHeader
  ): Future[Option[Either[TransformerError, PsrSubmission]]] =
    psrConnector
      .getStandardPsr(
        pstr,
        optFbNumber,
        optPeriodStartDate,
        optPsrVersion,
        schemeName,
        psrAuth.psaPspId,
        psrAuth.credentialRole,
        userName
      )
      .map(_.map(standardPsrFromEtmp.transform))
}
