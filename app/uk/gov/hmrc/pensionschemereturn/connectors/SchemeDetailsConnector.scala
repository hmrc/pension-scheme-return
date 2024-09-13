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

import uk.gov.hmrc.pensionschemereturn.config.AppConfig
import uk.gov.hmrc.pensionschemereturn.models.Srn
import play.api.Logging
import uk.gov.hmrc.pensionschemereturn.utils.HttpResponseHelper
import uk.gov.hmrc.http.client.HttpClientV2
import play.api.http.Status.OK
import uk.gov.hmrc.http.{HeaderCarrier, StringContextOps}

import scala.concurrent.{ExecutionContext, Future}

import javax.inject.{Inject, Singleton}

@Singleton
class SchemeDetailsConnector @Inject() (appConfig: AppConfig, http: HttpClientV2)
    extends HttpResponseHelper
    with Logging {

  def checkAssociation(idValue: String, idType: String, srn: Srn)(implicit
    hc: HeaderCarrier,
    ec: ExecutionContext
  ): Future[Boolean] = {
    val url = appConfig.isPsaAssociatedUrl
    http
      .get(url"$url")
      .transform(
        _.addHttpHeaders(
          idType -> idValue,
          "schemeReferenceNumber" -> srn.value,
          "Content-Type" -> "application/json"
        )
      )
      .execute
      .map { response =>
        response.status match {
          case OK =>
            response.json.as[Boolean]
          case _ => handleErrorResponse("GET", url)(response)
        }
      }
  }
}
