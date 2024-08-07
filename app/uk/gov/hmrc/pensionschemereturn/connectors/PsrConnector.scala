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

package uk.gov.hmrc.pensionschemereturn.connectors

import uk.gov.hmrc.pensionschemereturn.models.response._
import play.api.libs.json.Format.GenericFormat
import play.api.mvc.RequestHeader
import com.google.inject.Inject
import play.api.libs.ws.WSRequest
import uk.gov.hmrc.pensionschemereturn.utils.HttpResponseHelper
import uk.gov.hmrc.http.client.HttpClientV2
import uk.gov.hmrc.pensionschemereturn.config.AppConfig
import uk.gov.hmrc.pensionschemereturn.audit.ApiAuditUtil
import play.api.Logging
import play.api.libs.json.{JsValue, Json}
import play.api.http.Status._
import uk.gov.hmrc.http._

import scala.concurrent.{ExecutionContext, Future}

import java.util.UUID.randomUUID

class PsrConnector @Inject()(config: AppConfig, http: HttpClientV2, apiAuditUtil: ApiAuditUtil)
    extends HttpErrorFunctions
    with HttpResponseHelper
    with Logging {

  private val maxLengthCorrelationId = 36

  def submitStandardPsr(
    pstr: String,
    data: JsValue,
    schemeName: String,
    psaPspId: String,
    credentialRole: String,
    userName: String
  )(implicit headerCarrier: HeaderCarrier, ec: ExecutionContext, request: RequestHeader): Future[HttpResponse] = {

    val url: String = config.submitStandardPsrUrl.format(pstr)
    // TODO even when this is at info level and it is very useful for development, we'd need to take the body out before go-live:
    logger.info(s"Submit standard PSR called URL: $url with payload: ${Json.prettyPrint(data)}")

    http
      .post(url"$url")
      .withBody(Json.toJson(data))
      .transform(integrationFrameworkHeader)
      .execute[HttpResponse]
      .map { response =>
        response.status match {
          case OK =>
            // TODO even when this is at debug level and it is very useful for development, we'd need to take the body out before go-live:
            logger.debug(s"Submit standard PSR ----<<RESPONSE>>----: ${Json.prettyPrint(response.json)}")
            response
          case _ => handleErrorResponse("POST", url)(response)
        }
      }
      .andThen(
        apiAuditUtil.firePsrPostAuditEvent(pstr, data, schemeName, credentialRole, psaPspId, userName)
      )
  }

  def getStandardPsr(
    pstr: String,
    optFbNumber: Option[String],
    optPeriodStartDate: Option[String],
    optPsrVersion: Option[String],
    schemeName: String,
    psaPspId: String,
    credentialRole: String,
    userName: String
  )(
    implicit headerCarrier: HeaderCarrier,
    ec: ExecutionContext,
    request: RequestHeader
  ): Future[Option[PsrSubmissionEtmpResponse]] = {

    val params = buildParams(pstr, optFbNumber, optPeriodStartDate, optPsrVersion)
    val url: String = config.getStandardPsrUrl.format(params)
    val logMessage = s"Get standard PSR called URL: $url with pstr: $pstr"
    logger.info(logMessage)

    http
      .get(url"$url")
      .transform(integrationFrameworkHeader)
      .execute
      .map { response =>
        response.status match {
          case OK =>
            logger.debug(s"This is ETMP Response -->> Status : ${response.status}, Data : ${response.json}")
            Some(response.json.as[PsrSubmissionEtmpResponse])
          case UNPROCESSABLE_ENTITY if response.body.contains("PSR_NOT_FOUND") =>
            logger.info(s"$logMessage and returned PSR_NOT_FOUND with status: ${response.status}")
            None
          case _ => handleErrorResponse("GET", url)(response)
        }
      }
      .andThen(
        apiAuditUtil.firePsrGetAuditEvent(
          pstr,
          optFbNumber,
          optPeriodStartDate,
          optPsrVersion,
          credentialRole,
          psaPspId,
          userName,
          schemeName
        )
      )
  }

  def getOverview(pstr: String, fromDate: String, toDate: String)(
    implicit headerCarrier: HeaderCarrier,
    ec: ExecutionContext
  ): Future[Seq[PsrOverviewEtmpResponse]] = {

    val url: String = config.getOverviewUrl.format(pstr, fromDate, toDate)
    val logMessage = s"Get overview called, URL: $url"
    logger.info(logMessage)

    http
      .get(url"$url")
      .transform(integrationFrameworkHeader)
      .execute
      .map { response =>
        response.status match {
          case OK =>
            response.json.as[Seq[PsrOverviewEtmpResponse]]
          case NOT_FOUND =>
            logger.info(s"$logMessage and returned ${response.status}, ${response.json}")
            Seq.empty[PsrOverviewEtmpResponse]
          case _ => handleErrorResponse("GET", url)(response)
        }
      }
  }

  def getVersions(pstr: String, startDate: String)(
    implicit headerCarrier: HeaderCarrier,
    ec: ExecutionContext
  ): Future[Seq[PsrVersionsEtmpResponse]] = {

    val url: String = config.getVersionsUrl.format(pstr, startDate)
    val logMessage = s"Get report versions called, URL: $url"
    logger.info(logMessage)

    http
      .get(url"$url")
      .transform(integrationFrameworkHeader)
      .execute
      .map { response =>
        response.status match {
          case OK =>
            response.json.as[Seq[PsrVersionsEtmpResponse]]
          case SERVICE_UNAVAILABLE =>
            // TODO even when this is at info level and it is very useful for development, we'd need to take the body out before go-live:
            // TODO - must be a temporary solution to check QA env issues
            logger.info(s"$logMessage and returned ${response.status}, ${response.json} - returning empty response")
            Seq.empty[PsrVersionsEtmpResponse]
          case NOT_FOUND =>
            logger.info(s"$logMessage and returned ${response.status}, ${response.json}")
            Seq.empty[PsrVersionsEtmpResponse]
          case _ => handleErrorResponse("GET", url)(response)
        }
      }
  }

  private def buildParams(
    pstr: String,
    optFbNumber: Option[String],
    optPeriodStartDate: Option[String],
    optPsrVersion: Option[String]
  ): String =
    (optFbNumber, optPeriodStartDate, optPsrVersion) match {
      case (Some(fbNumber), _, _) => s"$pstr?psrFormBundleNumber=$fbNumber"
      case (None, Some(periodStartDate), Some(psrVersion)) =>
        s"$pstr?periodStartDate=$periodStartDate&psrVersion=$psrVersion"
      case _ => throw new BadRequestException("Missing url parameters")
    }
  private def getCorrelationId: String = randomUUID.toString.slice(0, maxLengthCorrelationId)

  private def integrationFrameworkHeader(wsRequest: WSRequest): WSRequest =
    wsRequest.addHttpHeaders(
      "Environment" -> config.integrationFrameworkEnvironment,
      "Authorization" -> config.integrationFrameworkAuthorization,
      "Content-Type" -> "application/json",
      "CorrelationId" -> getCorrelationId
    )
}
