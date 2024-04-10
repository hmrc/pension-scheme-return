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

package uk.gov.hmrc.pensionschemereturn.models.nonsipp.shares

import play.api.libs.json._
import uk.gov.hmrc.pensionschemereturn.utils.WithName

sealed trait TypeOfShares {
  val name: String
}

object TypeOfShares {

  case object SponsoringEmployer extends WithName("01") with TypeOfShares

  case object Unquoted extends WithName("02") with TypeOfShares

  case object ConnectedParty extends WithName("03") with TypeOfShares

  val values: List[TypeOfShares] = List(SponsoringEmployer, Unquoted, ConnectedParty)

  private val mappings: Map[String, TypeOfShares] = values.map(v => (v.toString, v)).toMap

  implicit val reads: Reads[TypeOfShares] =
    JsPath.read[String].flatMap {
      case aop if mappings.keySet.contains(aop) => Reads(_ => JsSuccess(mappings.apply(aop)))
      case invalidValue => Reads(_ => JsError(s"Invalid TypeOfShares type: $invalidValue"))
    }

  implicit val writes: Writes[TypeOfShares] = it => JsString(it.name)

  def stringToTypeOfShares(string: String): TypeOfShares =
    string match {
      case "01" => SponsoringEmployer
      case "02" => Unquoted
      case "03" => ConnectedParty
    }
}
