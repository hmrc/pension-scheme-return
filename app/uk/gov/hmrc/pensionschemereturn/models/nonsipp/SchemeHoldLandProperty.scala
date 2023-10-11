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
import play.api.mvc.JavascriptLiteral
import uk.gov.hmrc.pensionschemereturn.utils.WithName

sealed trait SchemeHoldLandProperty {
  val name: String
}

object SchemeHoldLandProperty {

  case object Acquisition extends WithName("Acquisition") with SchemeHoldLandProperty

  case object Contribution extends WithName("Contribution") with SchemeHoldLandProperty

  case object Transfer extends WithName("Transfer") with SchemeHoldLandProperty

  val values: List[SchemeHoldLandProperty] = List(Acquisition, Contribution, Transfer)

  implicit val jsLiteral: JavascriptLiteral[SchemeHoldLandProperty] = (value: SchemeHoldLandProperty) => value.name

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

}
