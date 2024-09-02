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

package uk.gov.hmrc.pensionschemereturn.audit

import uk.gov.hmrc.pensionschemereturn.models.Event
import play.api.libs.json.{JsObject, Json}

case class EmailAuditEvent(
  psaPspId: String,
  pstr: String,
  submittedBy: String,
  emailAddress: String,
  event: Event,
  requestId: String,
  reportVersion: String,
  schemeName: String,
  taxYear: String,
  userName: String
) extends ExtendedAuditEvent {
  override def auditType: String = "PensionSchemeReturnEmailEvent"

  override def details: JsObject = {
    val emailDetails =
      Json.obj(
        fields = "emailInitiationRequestId" -> requestId,
        "emailAddress" -> emailAddress,
        "event" -> event.toString,
        "submittedBy" -> submittedBy,
        "reportVersion" -> reportVersion,
        "pensionSchemeTaxReference" -> pstr,
        "schemeName" -> schemeName,
        "taxYear" -> taxYear
      )
    psaOrPspIdDetails(submittedBy, psaPspId, userName) ++ emailDetails
  }
}
