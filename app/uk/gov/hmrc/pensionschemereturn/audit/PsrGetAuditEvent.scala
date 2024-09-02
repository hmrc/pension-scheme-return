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

import play.api.libs.json.{JsObject, JsValue, Json}

case class PsrGetAuditEvent(
  pstr: String,
  credentialRole: String,
  psaPspId: String,
  userName: String,
  schemeName: String,
  fbNumber: Option[String],
  periodStartDate: Option[String],
  psrVersion: Option[String],
  status: Option[Int],
  response: Option[JsValue],
  errorMessage: Option[String]
) extends ExtendedAuditEvent {

  override def auditType: String = "PSRGet"

  override def details: JsObject = {

    val optFbNumber = fbNumber.fold[JsObject](Json.obj())(s => Json.obj("fbNumber" -> s))
    val optPeriodStartDate = periodStartDate.fold[JsObject](Json.obj())(s => Json.obj("periodStartDate" -> s))
    val optPsrVersion = psrVersion.fold[JsObject](Json.obj())(s => Json.obj("psrVersion" -> s))

    val optStatus = status.fold[JsObject](Json.obj())(s => Json.obj("httpStatus" -> s))
    val optResponse = response.fold[JsObject](Json.obj())(s => Json.obj("response" -> s))
    val optErrorMessage = errorMessage.fold[JsObject](Json.obj())(s => Json.obj("errorMessage" -> s))

    val apiDetails =
      Json.obj(
        "pensionSchemeTaxReference" -> pstr
      )
    psaOrPspIdDetails(credentialRole, psaPspId, userName) ++ apiDetails ++ optFbNumber ++ optPeriodStartDate ++ optPsrVersion ++ optStatus ++ optResponse ++ optErrorMessage
  }
}
