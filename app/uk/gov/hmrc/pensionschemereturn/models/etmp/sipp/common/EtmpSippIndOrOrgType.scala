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

package uk.gov.hmrc.pensionschemereturn.models.etmp.sipp.common

import play.api.libs.json.{JsError, JsString, JsSuccess, Reads, Writes}

// 01 = Individual, 02 = Company, 03 = Partnership, 04 = Other
sealed trait EtmpSippIndOrOrgType {
  val value: String
  val definition: String
}

object EtmpSippIndOrOrgType {
  case object Individual extends EtmpSippIndOrOrgType {
    val value = "01"
    val definition = "Individual"
  }
  case object Company extends EtmpSippIndOrOrgType {
    val value = "02"
    val definition = "Company"
  }

  case object Partnership extends EtmpSippIndOrOrgType {
    val value = "03"
    val definition = "Partnership"
  }

  case object Other extends EtmpSippIndOrOrgType {
    val value = "04"
    val definition = "Other"
  }

  def apply(definition: String): EtmpSippIndOrOrgType = definition match {
    case Individual.definition => Individual
    case Company.definition => Company
    case Partnership.definition => Partnership
    case Other.definition => Other
    case _ => throw new RuntimeException("Couldn't match the type for EtmpSippIndOrOrgType!")
  }

  implicit val writes: Writes[EtmpSippIndOrOrgType] = invOrOrgType => JsString(invOrOrgType.value)
  implicit val reads: Reads[EtmpSippIndOrOrgType] = Reads {
    case JsString(Individual.value) => JsSuccess(Individual)
    case JsString(Company.value) => JsSuccess(Company)
    case JsString(Partnership.value) => JsSuccess(Partnership)
    case JsString(Other.value) => JsSuccess(Other)
    case unknown => JsError(s"Unknown value for YesNo: $unknown")
  }
}
