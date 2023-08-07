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
import play.api.libs.json.JsValue
import play.api.mvc.RequestHeader
import uk.gov.hmrc.http._
import uk.gov.hmrc.pensionschemereturn.config.AppConfig
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
    val StandardPsrUrl = config.submitStandardPsrUrl
    logger.debug("Submit standard PSR called URL:" + StandardPsrUrl)

    http.POST[JsValue, HttpResponse](StandardPsrUrl, data)(implicitly, implicitly, headerCarrier, implicitly).map {
      response =>
        response.status match {
          case CREATED => response
          case UNPROCESSABLE_ENTITY => response
        }
    }
  }
}
