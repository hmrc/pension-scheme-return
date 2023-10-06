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

package uk.gov.hmrc.pensionschemereturn.models.nonsipp

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

  val values: List[IdentityType] = List(Individual, UKCompany, UKPartnership, Other)

  private val mappings: Map[String, IdentityType] = values.map(v => (v.toString, v)).toMap

  implicit val reads: Reads[IdentityType] =
    JsPath.read[String].flatMap {
      case aop if mappings.keySet.contains(aop) => Reads(_ => JsSuccess(mappings.apply(aop)))
      case invalidValue => Reads(_ => JsError(s"Invalid IdentityType type: $invalidValue"))
    }

  implicit val writes: Writes[IdentityType] = it => JsString(it.name)

  def identityTypeToString(identityType: IdentityType): String =
    identityType match {
      case Individual => "01"
      case UKCompany => "02"
      case UKPartnership => "03"
      case Other => "04"
    }

  def stringToIdentityType(value: String): IdentityType =
    value match {
      case "01" => Individual
      case "02" => UKCompany
      case "03" => UKPartnership
      case "04" => Other
    }
}
