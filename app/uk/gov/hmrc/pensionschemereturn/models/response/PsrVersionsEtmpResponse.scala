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

import uk.gov.hmrc.pensionschemereturn.models.etmp.ReportStatus
import play.api.libs.json._

import java.time.LocalDateTime

case class PsrVersionsEtmpResponse(
  reportFormBundleNumber: String,
  reportVersion: Int,
  reportStatus: ReportStatus,
  compilationOrSubmissionDate: LocalDateTime,
  reportSubmitterDetails: Option[ReportSubmitterDetails],
  psaDetails: Option[PsaDetails]
)

case class ReportSubmitterDetails(
  reportSubmittedBy: String,
  organisationOrPartnershipDetails: Option[OrganisationOrPartnershipDetails],
  individualDetails: Option[IndividualDetails]
)

case class OrganisationOrPartnershipDetails(
  organisationOrPartnershipName: String
)

case class IndividualDetails(
  firstName: String,
  middleName: Option[String],
  lastName: String
)

case class PsaDetails(
  psaOrganisationOrPartnershipDetails: Option[PsaOrganisationOrPartnershipDetails],
  psaIndividualDetails: Option[PsaIndividualDetails]
)

case class PsaOrganisationOrPartnershipDetails(
  organisationOrPartnershipName: String
)

case class PsaIndividualDetails(
  firstName: String,
  middleName: Option[String],
  lastName: String
)

object PsrVersionsEtmpResponse {
  private given formatOrganisationOrPartnershipDetails: Format[OrganisationOrPartnershipDetails] =
    Json.format[OrganisationOrPartnershipDetails]
  private given formatIndividualDetails: Format[IndividualDetails] =
    Json.format[IndividualDetails]
  private given formatReportSubmitterDetails: Format[ReportSubmitterDetails] =
    Json.format[ReportSubmitterDetails]

  private given formatPsaOrganisationOrPartnershipDetails: Format[PsaOrganisationOrPartnershipDetails] =
    Json.format[PsaOrganisationOrPartnershipDetails]
  private given formatPsaIndividualDetails: Format[PsaIndividualDetails] =
    Json.format[PsaIndividualDetails]
  private given formatPsaDetails: Format[PsaDetails] =
    Json.format[PsaDetails]

  given formats: Format[PsrVersionsEtmpResponse] = Json.format[PsrVersionsEtmpResponse]
}
