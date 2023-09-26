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

package uk.gov.hmrc.pensionschemereturn.models.requests.etmp

import play.api.libs.json.{JsString, Json, OWrites, Writes}

import java.time.LocalDate

sealed trait PSRStatus {
  val name: String
}

case object Compiled extends PSRStatus {
  val name = "Compiled"
}

case object Submitted extends PSRStatus {
  val name = "Submitted"
}

case class ReportDetailsRequest(
  pstr: String,
  psrStatus: PSRStatus,
  periodStart: LocalDate,
  periodEnd: LocalDate
)

object ReportDetailsRequest {
  private implicit val psrStatusWrites: Writes[PSRStatus] = status => JsString(status.name)
  implicit val writes: OWrites[ReportDetailsRequest] = Json.writes[ReportDetailsRequest]
}
