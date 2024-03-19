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

package uk.gov.hmrc.pensionschemereturn.models.etmp.sipp

import uk.gov.hmrc.pensionschemereturn.models.etmp.EtmpPsrStatus
import play.api.libs.json.{Json, OFormat}

import java.time.LocalDate

case class EtmpSippReportDetails(
  pstr: Option[String],
  status: EtmpPsrStatus,
  periodStart: LocalDate,
  periodEnd: LocalDate,
  memberTransactions: String,
  schemeName: Option[String],
  psrVersion: Option[String]
)

object EtmpSippReportDetails {
  implicit val format: OFormat[EtmpSippReportDetails] = Json.format[EtmpSippReportDetails]
}
