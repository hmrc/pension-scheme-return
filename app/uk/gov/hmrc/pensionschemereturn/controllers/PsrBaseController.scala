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

package uk.gov.hmrc.pensionschemereturn.controllers

import play.api.mvc.{AnyContent, Request}
import play.api.libs.json.JsValue
import uk.gov.hmrc.http.BadRequestException

trait PsrBaseController {
  def requiredBody(implicit request: Request[AnyContent]): JsValue =
    request.body.asJson.getOrElse(throw new BadRequestException("Request does not contain Json body"))

  protected def requiredHeaders(headers: String*)(implicit request: Request[AnyContent]): Seq[String] = {
    val headerData: Seq[Option[String]] = headers.map(request.headers.get)
    val allHeadersDefined = headerData.forall(_.isDefined)
    if (allHeadersDefined) {
      headerData.collect { case Some(value) => value }
    } else {
      val missingHeaders: Seq[(String, Option[String])] = headers.zip(headerData)
      val errorString = missingHeaders
        .map {
          case (headerName, data) =>
            prettyMissingParamError(data, headerName + " missing")
        }
        .mkString(" ")
      throw new BadRequestException("Bad Request with missing parameters: " + errorString)
    }
  }

  private def prettyMissingParamError(param: Option[String], error: String): String =
    if (param.isEmpty) s"$error " else ""

}
