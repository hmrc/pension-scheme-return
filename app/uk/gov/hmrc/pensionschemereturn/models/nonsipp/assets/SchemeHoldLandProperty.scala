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

package uk.gov.hmrc.pensionschemereturn.models.nonsipp.assets

import play.api.libs.json._
import uk.gov.hmrc.pensionschemereturn.utils.WithName

sealed trait SchemeHoldLandProperty {
  val name: String
}

object SchemeHoldLandProperty {

  case object Acquisition extends WithName("Acquisition") with SchemeHoldLandProperty

  case object Contribution extends WithName("Contribution") with SchemeHoldLandProperty

  case object Transfer extends WithName("Transfer") with SchemeHoldLandProperty

  val values: List[SchemeHoldLandProperty] = List(Acquisition, Contribution, Transfer)

  private val mappings: Map[String, SchemeHoldLandProperty] = values.map(v => (v.toString, v)).toMap

  implicit val reads: Reads[SchemeHoldLandProperty] =
    JsPath.read[String].flatMap {
      case aop if mappings.keySet.contains(aop) => Reads(_ => JsSuccess(mappings.apply(aop)))
      case invalidValue => Reads(_ => JsError(s"Invalid SchemeHoldLandProperty type: $invalidValue"))
    }

  implicit val writes: Writes[SchemeHoldLandProperty] = it => JsString(it.name)

  def schemeHoldLandPropertyToString(schemeHoldLandProperty: SchemeHoldLandProperty): String =
    schemeHoldLandProperty match {
      case Acquisition => "01"
      case Contribution => "02"
      case Transfer => "03"
    }

  def stringToSchemeHoldLandProperty(string: String): SchemeHoldLandProperty =
    string match {
      case "01" => Acquisition
      case "02" => Contribution
      case "03" => Transfer
    }

}
