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

package uk.gov.hmrc.pensionschemereturn.services

import com.google.inject.{Inject, Singleton}
import play.api.Logging
import play.api.libs.json._
import play.api.mvc.RequestHeader
import uk.gov.hmrc.http.{BadRequestException, ExpectationFailedException, HeaderCarrier, HttpResponse}
import uk.gov.hmrc.pensionschemereturn.connectors.PsrConnector
import uk.gov.hmrc.pensionschemereturn.models._
import uk.gov.hmrc.pensionschemereturn.models.nonsipp.PsrSubmission
import uk.gov.hmrc.pensionschemereturn.models.sipp.SippPsrSubmission
import uk.gov.hmrc.pensionschemereturn.transformations.nonsipp.{PsrSubmissionToEtmp, StandardPsrFromEtmp}
import uk.gov.hmrc.pensionschemereturn.transformations.sipp.SippPsrSubmissionToEtmp
import uk.gov.hmrc.pensionschemereturn.validators.JSONSchemaValidator
import uk.gov.hmrc.pensionschemereturn.validators.SchemaPaths.{EPID_1444, EPID_1446}

import scala.concurrent.{ExecutionContext, Future}

@Singleton()
class PsrSubmissionService @Inject()(
  psrConnector: PsrConnector,
  jsonPayloadSchemaValidator: JSONSchemaValidator,
  psrSubmissionToEtmp: PsrSubmissionToEtmp,
  sippPsrSubmissionToEtmp: SippPsrSubmissionToEtmp,
  standardPsrFromEtmp: StandardPsrFromEtmp
) extends Logging {

  def submitStandardPsr(
    psrSubmission: PsrSubmission
  )(implicit headerCarrier: HeaderCarrier, ec: ExecutionContext, request: RequestHeader): Future[HttpResponse] = {
    val payloadAsJson = Json.toJson(psrSubmissionToEtmp.transform(psrSubmission))
    val validationResult = jsonPayloadSchemaValidator.validatePayload(EPID_1444, payloadAsJson)
    if (validationResult.hasErrors) {
      throw PensionSchemeReturnValidationFailureException(
        s"Invalid payload when submitStandardPsr :-\n${validationResult.toString}"
      )
    } else {
      psrConnector.submitStandardPsr(payloadAsJson).recover {
        case _: BadRequestException =>
          throw new ExpectationFailedException("Nothing to submit")
      }
    }
  }

  def getStandardPsr(
    pstr: String,
    optFbNumber: Option[String],
    optPeriodStartDate: Option[String],
    optPsrVersion: Option[String]
  )(
    implicit headerCarrier: HeaderCarrier,
    ec: ExecutionContext,
    request: RequestHeader
  ): Future[Option[PsrSubmission]] =
    psrConnector
      .getStandardPsr(pstr, optFbNumber, optPeriodStartDate, optPsrVersion)
      .map(_.map(standardPsrFromEtmp.transform))

  def submitSippPsr(
    sippPsrSubmission: SippPsrSubmission
  )(implicit headerCarrier: HeaderCarrier, ec: ExecutionContext, request: RequestHeader): Future[HttpResponse] = {
    val payloadAsJson = Json.toJson(sippPsrSubmissionToEtmp.transform(sippPsrSubmission))
    val validationResult = jsonPayloadSchemaValidator.validatePayload(EPID_1446, payloadAsJson)
    if (validationResult.hasErrors) {
      throw PensionSchemeReturnValidationFailureException(
        s"Invalid payload when submitSippPsr :-\n${validationResult.toString}"
      )
    } else {
      psrConnector.submitSippPsr(payloadAsJson).recover {
        case _: BadRequestException =>
          throw new ExpectationFailedException("Nothing to submit")
      }
    }
  }

}
