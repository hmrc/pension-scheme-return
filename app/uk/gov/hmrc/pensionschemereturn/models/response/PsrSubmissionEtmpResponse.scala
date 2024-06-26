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

package uk.gov.hmrc.pensionschemereturn.models.response

import uk.gov.hmrc.pensionschemereturn.models.etmp.EtmpPsrStatus
import uk.gov.hmrc.pensionschemereturn.models.etmp.nonsipp._
import play.api.libs.json.{Json, OFormat}
import uk.gov.hmrc.pensionschemereturn.models.etmp.nonsipp.assets.EtmpAssets
import uk.gov.hmrc.pensionschemereturn.models.etmp.nonsipp.memberpayments.EtmpMemberPayments

import java.time.{LocalDate, LocalDateTime}

case class PsrSubmissionEtmpResponse(
  schemeDetails: EtmpSchemeDetails,
  psrDetails: EtmpPsrDetails,
  accountingPeriodDetails: EtmpAccountingPeriodDetails,
  schemeDesignatory: EtmpSchemeDesignatory,
  loans: Option[EtmpLoans],
  assets: Option[EtmpAssets],
  membersPayments: Option[EtmpMemberPayments],
  shares: Option[EtmpShares],
  psrDeclaration: Option[EtmpPsrDeclaration]
)

case class EtmpSchemeDetails(
  pstr: String,
  schemeName: String
)

case class EtmpPsrDetails(
  fbVersion: String,
  fbstatus: EtmpPsrStatus,
  periodStart: LocalDate,
  periodEnd: LocalDate,
  compilationOrSubmissionDate: LocalDateTime
)

object PsrSubmissionEtmpResponse {
  private implicit val formatsEtmpSchemeDetails: OFormat[EtmpSchemeDetails] = Json.format[EtmpSchemeDetails]
  private implicit val formatsEtmpPsrDetails: OFormat[EtmpPsrDetails] = Json.format[EtmpPsrDetails]
  implicit val formats: OFormat[PsrSubmissionEtmpResponse] = Json.format[PsrSubmissionEtmpResponse]
}
