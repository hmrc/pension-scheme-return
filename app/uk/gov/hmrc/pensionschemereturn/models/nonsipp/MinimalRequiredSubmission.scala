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

package uk.gov.hmrc.pensionschemereturn.models.nonsipp

import play.api.libs.json.{Json, OFormat}

import java.time.LocalDate

case class MinimalRequiredSubmission(
  reportDetails: ReportDetails,
  accountingPeriods: List[(LocalDate, LocalDate)],
  schemeDesignatory: SchemeDesignatory
)

case class ReportDetails(
  pstr: String,
  periodStart: LocalDate,
  periodEnd: LocalDate
)

case class SchemeDesignatory(
  reasonForNoBankAccount: Option[String],
  openBankAccount: Boolean,
  activeMembers: Int,
  deferredMembers: Int,
  pensionerMembers: Int,
  totalAssetValueStart: Option[Double],
  totalAssetValueEnd: Option[Double],
  totalCashStart: Option[Double],
  totalCashEnd: Option[Double],
  totalPayments: Option[Double]
)

object MinimalRequiredSubmission {
  private implicit val formatDetails: OFormat[ReportDetails] = Json.format[ReportDetails]
  private implicit val formatSchemeDesignatory: OFormat[SchemeDesignatory] = Json.format[SchemeDesignatory]
  implicit val formats: OFormat[MinimalRequiredSubmission] = Json.format[MinimalRequiredSubmission]
}
