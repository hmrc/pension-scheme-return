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

import uk.gov.hmrc.pensionschemereturn.validators.SchemaPaths.API_1997
import uk.gov.hmrc.pensionschemereturn.transformations.sipp.{SippPsrFromEtmp, SippPsrSubmissionToEtmp}
import play.api.mvc.RequestHeader
import com.google.inject.{Inject, Singleton}
import uk.gov.hmrc.pensionschemereturn.models._
import uk.gov.hmrc.http._
import uk.gov.hmrc.pensionschemereturn.validators.JSONSchemaValidator
import uk.gov.hmrc.pensionschemereturn.connectors.PsrConnector
import uk.gov.hmrc.pensionschemereturn.models.sipp.SippPsrSubmission
import play.api.Logging
import play.api.libs.json._

import scala.concurrent.{ExecutionContext, Future}

@Singleton()
class SippPsrSubmissionService @Inject()(
  psrConnector: PsrConnector,
  jsonPayloadSchemaValidator: JSONSchemaValidator,
  sippPsrSubmissionToEtmp: SippPsrSubmissionToEtmp,
  sippPsrFromEtmp: SippPsrFromEtmp
) extends Logging {

  def submitSippPsr(
    sippPsrSubmission: SippPsrSubmission
  )(implicit headerCarrier: HeaderCarrier, ec: ExecutionContext, request: RequestHeader): Future[HttpResponse] = {
    val payloadAsJson = Json.toJson(sippPsrSubmissionToEtmp.transform(sippPsrSubmission))
    val validationResult = jsonPayloadSchemaValidator.validatePayload(API_1997, payloadAsJson)
    if (validationResult.hasErrors) {
      throw PensionSchemeReturnValidationFailureException(
        s"Invalid payload when submitSippPsr :-\n${validationResult.toString}"
      )
    } else {
      psrConnector.submitSippPsr(sippPsrSubmission.reportDetails.pstr, payloadAsJson).recover {
        case _: BadRequestException =>
          throw new ExpectationFailedException("Nothing to submit")
      }
    }
  }

  def getSippPsr(
    pstr: String,
    optFbNumber: Option[String],
    optPeriodStartDate: Option[String],
    optPsrVersion: Option[String]
  )(
    implicit headerCarrier: HeaderCarrier,
    ec: ExecutionContext,
    request: RequestHeader
  ): Future[Option[SippPsrSubmission]] =
    psrConnector
      .getSippPsr(pstr, optFbNumber, optPeriodStartDate, optPsrVersion)
      .map(_.map(sippPsrFromEtmp.transform))

}
