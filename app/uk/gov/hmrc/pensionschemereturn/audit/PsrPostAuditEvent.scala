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

case class PsrPostAuditEvent(
  pstr: String,
  credentialRole: String,
  psaPspId: String,
  userName: String,
  schemeName: String,
  payload: JsValue,
  status: Option[Int],
  response: Option[JsValue],
  errorMessage: Option[String]
) extends ExtendedAuditEvent {

  override def auditType: String = "PSRPost"

  override def details: JsObject = {

    val optStatus = status.fold[JsObject](Json.obj())(s => Json.obj("HttpStatus" -> s))
    val optResponse = response.fold[JsObject](Json.obj())(s => Json.obj("Response" -> s))
    val optErrorMessage = errorMessage.fold[JsObject](Json.obj())(s => Json.obj("ErrorMessage" -> s))

    val apiDetails =
      Json.obj(
        "PensionSchemeTaxReference" -> pstr,
        "SchemeName" -> schemeName,
        "Payload" -> payload
      )
    psaOrPspIdDetails(credentialRole, psaPspId, userName) ++ apiDetails ++ optStatus ++ optResponse ++ optErrorMessage
  }
}
