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

package uk.gov.hmrc.pensionschemereturn.service

import com.google.inject.{Inject, Singleton}
import play.api.Logging
import play.api.libs.json._
import play.api.mvc.RequestHeader
import uk.gov.hmrc.http.{HeaderCarrier, HttpResponse}
import uk.gov.hmrc.pensionschemereturn.connectors.PsrConnector

import scala.concurrent.{ExecutionContext, Future}

@Singleton()
class PsrSubmissionService @Inject()(psrConnector: PsrConnector) extends Logging {

  def submitStandardPsr(
    userAnswersJson: JsValue
  )(implicit headerCarrier: HeaderCarrier, ec: ExecutionContext, request: RequestHeader): Future[HttpResponse] =
    // TODO transform from userDetails into ETMP format
    psrConnector.submitStandardPsr(userAnswersJson)
}
