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

package uk.gov.hmrc.pensionschemereturn.connectors

import com.google.inject.Inject
import play.api.Logging
import play.api.http.Status._
import play.api.libs.json.Format.GenericFormat
import play.api.libs.json.{JsValue, Json}
import play.api.mvc.RequestHeader
import uk.gov.hmrc.http._
import uk.gov.hmrc.pensionschemereturn.config.AppConfig
import uk.gov.hmrc.pensionschemereturn.models.response.{PsrSubmissionEtmpResponse, SippPsrSubmissionEtmpResponse}
import uk.gov.hmrc.pensionschemereturn.utils.HttpResponseHelper

import scala.concurrent.{ExecutionContext, Future}

class PsrConnector @Inject()(
  config: AppConfig,
  http: HttpClient
) extends HttpErrorFunctions
    with HttpResponseHelper
    with Logging {

  def submitStandardPsr(
    data: JsValue
  )(implicit headerCarrier: HeaderCarrier, ec: ExecutionContext, request: RequestHeader): Future[HttpResponse] = {
    val url = config.submitStandardPsrUrl
    logger.info("Submit standard PSR called URL: " + url + s" with payload: ${Json.stringify(data)}")

    http
      .POST[JsValue, HttpResponse](url, data)(implicitly, implicitly, headerCarrier, implicitly)
      .map { response =>
        response.status match {
          case CREATED => response
          case _ => handleErrorResponse("POST", url)(response)
        }
      }
  }

  def getStandardPsr(
    pstr: String,
    optFbNumber: Option[String],
    optPeriodStartDate: Option[String],
    optPsrVersion: Option[String]
  )(implicit headerCarrier: HeaderCarrier, ec: ExecutionContext): Future[Option[PsrSubmissionEtmpResponse]] = {

    val params = buildParams(pstr, optFbNumber, optPeriodStartDate, optPsrVersion)
    val url: String = config.getStandardPsrUrl.format(params)
    val logMessage = "Get standard PSR called URL: " + url + s" with pstr: $pstr"

    logger.info(logMessage)

    http.GET[HttpResponse](url)(implicitly, headerCarrier, implicitly).map { response =>
      response.status match {
        case OK =>
          Some(response.json.as[PsrSubmissionEtmpResponse])
        case NOT_FOUND =>
          logger.warn(s"$logMessage and returned ${response.status}")
          None
        case _ => handleErrorResponse("GET", url)(response)
      }
    }
  }

  def submitSippPsr(
    data: JsValue
  )(implicit headerCarrier: HeaderCarrier, ec: ExecutionContext, request: RequestHeader): Future[HttpResponse] = {
    val url = config.submitSippPsrUrl
    logger.info("Submit SIPP PSR called URL: " + url + s" with payload: ${Json.stringify(data)}")

    http
      .POST[JsValue, HttpResponse](url, data)(implicitly, implicitly, headerCarrier, implicitly)
      .map { response =>
        response.status match {
          case CREATED => response
          case _ => handleErrorResponse("POST", url)(response)
        }
      }
  }

  def getSippPsr(
    pstr: String,
    optFbNumber: Option[String],
    optPeriodStartDate: Option[String],
    optPsrVersion: Option[String]
  )(implicit headerCarrier: HeaderCarrier, ec: ExecutionContext): Future[Option[SippPsrSubmissionEtmpResponse]] = {

    val params = buildParams(pstr, optFbNumber, optPeriodStartDate, optPsrVersion)
    val url: String = config.getSippPsrUrl.format(params)
    val logMessage = "Get SIPP PSR called URL: " + url + s" with pstr: $pstr"

    logger.info(logMessage)

    http.GET[HttpResponse](url)(implicitly, headerCarrier, implicitly).map { response =>
      response.status match {
        case OK =>
          Some(response.json.as[SippPsrSubmissionEtmpResponse])
        case NOT_FOUND =>
          logger.warn(s"$logMessage and returned ${response.status}")
          None
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
      case (Some(fbNumber), _, _) => s"$pstr?fbNumber=$fbNumber"
      case (None, Some(periodStartDate), Some(psrVersion)) =>
        s"$pstr?periodStartDate=$periodStartDate&psrVersion=$psrVersion"
      case _ => throw new BadRequestException("Missing url parameters")
    }

}
