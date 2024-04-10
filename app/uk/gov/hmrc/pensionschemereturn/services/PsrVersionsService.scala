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

import com.google.inject.{Inject, Singleton}
import uk.gov.hmrc.http.HeaderCarrier
import uk.gov.hmrc.pensionschemereturn.connectors.PsrConnector
import play.api.Logging
import play.api.libs.json._

import scala.concurrent.{ExecutionContext, Future}

@Singleton()
class PsrVersionsService @Inject()(
  psrConnector: PsrConnector
) extends Logging {

  def getVersions(pstr: String, startDate: String)(
    implicit headerCarrier: HeaderCarrier,
    ec: ExecutionContext
  ): Future[JsValue] =
    psrConnector
      .getVersions(pstr, startDate)
      .map(data => Json.toJson(data))
}
