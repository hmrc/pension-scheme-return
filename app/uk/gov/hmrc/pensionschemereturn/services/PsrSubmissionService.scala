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
import uk.gov.hmrc.pensionschemereturn.services.PsrSubmissionService._
import uk.gov.hmrc.pensionschemereturn.transformations.MinimalRequiredDetailsToEtmp
import uk.gov.hmrc.pensionschemereturn.validators.JSONSchemaValidator
import uk.gov.hmrc.pensionschemereturn.validators.SchemaPaths.EPID_1444

import scala.concurrent.{ExecutionContext, Future}

@Singleton()
class PsrSubmissionService @Inject()(
  psrConnector: PsrConnector,
  jsonPayloadSchemaValidator: JSONSchemaValidator,
  minimalRequiredDetailsToEtmp: MinimalRequiredDetailsToEtmp
) extends Logging {

  def submitMinimalRequiredDetails(
    minimalRequiredSubmission: MinimalRequiredSubmission
  )(implicit headerCarrier: HeaderCarrier, ec: ExecutionContext, request: RequestHeader): Future[HttpResponse] = {
    val payloadAsJson = Json.toJson(minimalRequiredDetailsToEtmp.transform(minimalRequiredSubmission))
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

  def submitStandardPsr(
    psrSubmission: PsrSubmission
  )(implicit headerCarrier: HeaderCarrier, ec: ExecutionContext, request: RequestHeader): Future[HttpResponse] = {
    val payloadAsJson = Json.toJson("to be implemented")
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
}

object PsrSubmissionService {
  private implicit val psrStatusWrites: Writes[PSRStatus] = status => JsString(status.name)
  private implicit val reportDetailsWrites: OWrites[ETMPReportDetails] = Json.writes[ETMPReportDetails]
  private implicit val accountingPeriodWrites: OWrites[ETMPAccountingPeriod] = Json.writes[ETMPAccountingPeriod]
  private implicit val accountingPeriodDetailsWrites: OWrites[ETMPAccountingPeriodDetails] =
    Json.writes[ETMPAccountingPeriodDetails]
  private implicit val schemeDesignatoryWrites: OWrites[ETMPSchemeDesignatory] = Json.writes[ETMPSchemeDesignatory]
  implicit val reads: OWrites[ETMPMinimalRequiredDetails] = Json.writes[ETMPMinimalRequiredDetails]
}
