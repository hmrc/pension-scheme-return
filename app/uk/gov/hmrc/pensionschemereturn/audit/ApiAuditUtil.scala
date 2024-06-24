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

package uk.gov.hmrc.pensionschemereturn.audit

import uk.gov.hmrc.pensionschemereturn.models.response.PsrSubmissionEtmpResponse
import uk.gov.hmrc.pensionschemereturn.services.AuditService
import play.api.mvc.RequestHeader
import com.google.inject.Inject
import play.api.http.Status
import uk.gov.hmrc.http.{HttpException, HttpResponse, UpstreamErrorResponse}
import play.api.Logging
import play.api.libs.json._

import scala.concurrent.ExecutionContext
import scala.util.{Failure, Success, Try}

class ApiAuditUtil @Inject()(auditService: AuditService) extends Logging {

  def firePsrPostAuditEvent(
    pstr: String,
    data: JsValue
  )(implicit ec: ExecutionContext, request: RequestHeader): PartialFunction[Try[HttpResponse], Unit] = {
    case Success(httpResponse) =>
      logger.info(s"PsrPostAuditEvent ->> Status: ${Status.OK}, Payload: ${Json.prettyPrint(data)}")
      auditService.sendEvent(
        PsrPostAuditEvent(
          pstr = pstr,
          payload = data,
          status = Some(Status.OK),
          response = Some(httpResponse.json),
          errorMessage = None
        )
      )
    case Failure(error: UpstreamErrorResponse) =>
      logger.info(s"PsrPostAuditEvent ->> Status: ${error.statusCode}, ErrorMessage: ${Json.toJson(error.message)}")
      auditService.sendEvent(
        PsrPostAuditEvent(
          pstr = pstr,
          payload = data,
          status = Some(error.statusCode),
          response = None,
          errorMessage = Some(error.message)
        )
      )
    case Failure(error: HttpException) =>
      logger.info(s"PsrPostAuditEvent ->> Status: ${error.responseCode}, ErrorMessage: ${Json.toJson(error.message)}")
      auditService.sendEvent(
        PsrPostAuditEvent(
          pstr = pstr,
          payload = data,
          status = Some(error.responseCode),
          response = None,
          errorMessage = Some(error.message)
        )
      )
    case Failure(error: Throwable) =>
      logger.info(s"PsrPostAuditEvent ->> ErrorMessage: ${Json.toJson(error.getMessage)}")
      auditService.sendEvent(
        PsrPostAuditEvent(
          pstr = pstr,
          payload = data,
          status = None,
          response = None,
          errorMessage = Some(error.getMessage)
        )
      )
  }

  def firePsrGetAuditEvent(
    pstr: String,
    optFbNumber: Option[String],
    optPeriodStartDate: Option[String],
    optPsrVersion: Option[String]
  )(
    implicit ec: ExecutionContext,
    request: RequestHeader
  ): PartialFunction[Try[Option[PsrSubmissionEtmpResponse]], Unit] = {
    case Success(optPsrSubmissionEtmpResponse) =>
      logger.info(s"PsrGetAuditEvent ->> Status: ${Status.OK}, Response: ${Json.toJson(optPsrSubmissionEtmpResponse)}")
      auditService.sendEvent(
        PsrGetAuditEvent(
          pstr = pstr,
          fbNumber = optFbNumber,
          periodStartDate = optPeriodStartDate,
          psrVersion = optPsrVersion,
          status = Some(Status.OK),
          response = optPsrSubmissionEtmpResponse.map(Json.toJson(_)),
          errorMessage = None
        )
      )
    case Failure(error: UpstreamErrorResponse) =>
      logger.info(s"PsrGetAuditEvent ->> Status: ${error.statusCode}, ErrorMessage: ${Json.toJson(error.message)}")
      auditService.sendEvent(
        PsrGetAuditEvent(
          pstr = pstr,
          fbNumber = optFbNumber,
          periodStartDate = optPeriodStartDate,
          psrVersion = optPsrVersion,
          status = Some(error.statusCode),
          response = None,
          errorMessage = Some(error.message)
        )
      )
    case Failure(error: HttpException) =>
      logger.info(s"PsrGetAuditEvent ->> Status: ${error.responseCode}, ErrorMessage: ${Json.toJson(error.message)}")
      auditService.sendEvent(
        PsrGetAuditEvent(
          pstr = pstr,
          fbNumber = optFbNumber,
          periodStartDate = optPeriodStartDate,
          psrVersion = optPsrVersion,
          status = Some(error.responseCode),
          response = None,
          errorMessage = Some(error.message)
        )
      )
    case Failure(error: Throwable) =>
      logger.info(s"PsrGetAuditEvent ->> ErrorMessage: ${Json.toJson(error.getMessage)}")
      auditService.sendEvent(
        PsrGetAuditEvent(
          pstr = pstr,
          fbNumber = optFbNumber,
          periodStartDate = optPeriodStartDate,
          psrVersion = optPsrVersion,
          status = None,
          response = None,
          errorMessage = Some(error.getMessage)
        )
      )
  }
}
