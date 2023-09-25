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

package uk.gov.hmrc.pensionschemereturn.models

import play.api.libs.json._
import uk.gov.hmrc.pensionschemereturn.utils.WithName

sealed trait IdentityType {
  val name: String
}

object IdentityType {

  case object Individual extends WithName("individual") with IdentityType

  case object UKCompany extends WithName("ukCompany") with IdentityType

  case object UKPartnership extends WithName("ukPartnership") with IdentityType

  case object Other extends WithName("other") with IdentityType

  implicit val reads: Reads[IdentityType] = {
    case JsString(Individual.name) => JsSuccess(Individual)
    case JsString(UKCompany.name) => JsSuccess(UKCompany)
    case JsString(UKPartnership.name) => JsSuccess(UKPartnership)
    case JsString(Other.name) => JsSuccess(Other)
    case invalidType => JsError(s"Expected identity type but got $invalidType")
  }
}
