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

package uk.gov.hmrc.pensionschemereturn.utils

import play.api.Logger
import play.api.http.Status._
import uk.gov.hmrc.http._

trait HttpResponseHelper extends HttpErrorFunctions {

  implicit val httpResponseReads: HttpReads[HttpResponse] = (method: String, url: String, response: HttpResponse) =>
    response

  private val logger = Logger(classOf[HttpResponseHelper])

  def handleErrorResponse(httpMethod: String, url: String)(response: HttpResponse): Nothing =
    response.status match {
      case BAD_REQUEST =>
        if (response.body.contains("INVALID_PAYLOAD"))
          logger.warn(s"INVALID_PAYLOAD returned from: $url")
        throw new BadRequestException(
          badRequestMessage(httpMethod, url, response.body)
        )
      case NOT_FOUND =>
        throw new NotFoundException(
          notFoundMessage(httpMethod, url, response.body)
        )
      case status if is4xx(status) =>
        throw UpstreamErrorResponse(
          upstreamResponseMessage(httpMethod, url, status, response.body),
          status,
          status,
          response.headers
        )
      case status if is5xx(status) =>
        throw UpstreamErrorResponse(
          upstreamResponseMessage(httpMethod, url, status, response.body),
          status,
          status
        )
      case _ =>
        throw new UnrecognisedHttpResponseException(httpMethod, url, response)
    }
}

class UnrecognisedHttpResponseException(method: String, url: String, response: HttpResponse)
    extends Exception(s"$method to $url failed with status ${response.status}. Response body: '${response.body}'")
