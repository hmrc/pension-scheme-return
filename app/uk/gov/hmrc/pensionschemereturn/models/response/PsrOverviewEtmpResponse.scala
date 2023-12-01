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

package uk.gov.hmrc.pensionschemereturn.models.response

import play.api.libs.json.{Format, Json}
import uk.gov.hmrc.pensionschemereturn.models.etmp.PsrReportType

import java.time.LocalDate

case class PsrOverviewEtmpResponse(
  periodStartDate: LocalDate,
  periodEndDate: LocalDate,
  numberOfVersions: Int,
  submittedVersionAvailable: String,
  compiledVersionAvailable: String,
  ntfDateOfIssue: LocalDate,
  psrDueDate: LocalDate,
  psrReportType: PsrReportType
)

object PsrOverviewEtmpResponse {

  implicit val formats: Format[PsrOverviewEtmpResponse] = Json.format[PsrOverviewEtmpResponse]
}
