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

package uk.gov.hmrc.pensionschemereturn.models.etmp.nonsipp.memberpayments

import play.api.libs.json._

case class OrganisationIdentity(
  orgType: EmployerContributionsOrgType,
  idNumber: Option[String] = None,
  reasonNoIdNumber: Option[String] = None,
  otherDescription: Option[String] = None
)

sealed trait EmployerContributionsOrgType {
  val value: String
}

object EmployerContributionsOrgType {
  case object UKCompany extends EmployerContributionsOrgType {
    val value = "01"
  }

  case object UKPartnership extends EmployerContributionsOrgType {
    val value = "02"
  }

  case object Other extends EmployerContributionsOrgType {
    val value = "03"
  }

  implicit val writes: Writes[EmployerContributionsOrgType] = Writes(orgType => JsString(orgType.value))
  implicit val reads: Reads[EmployerContributionsOrgType] = Reads {
    case JsString(UKCompany.value) => JsSuccess(UKCompany)
    case JsString(UKPartnership.value) => JsSuccess(UKPartnership)
    case JsString(Other.value) => JsSuccess(Other)
    case unknown => JsError(s"Failed to read EmployeeContributionsOrgType with unknown json $unknown")
  }
}

object OrganisationIdentity {
  implicit val formatOrganisationIdentity: Format[OrganisationIdentity] = Json.format[OrganisationIdentity]
}
