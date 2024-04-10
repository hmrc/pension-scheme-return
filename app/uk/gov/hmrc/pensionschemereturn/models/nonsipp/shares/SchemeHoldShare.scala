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

sealed trait SchemeHoldShare {
  val name: String
}

object SchemeHoldShare {

  case object Acquisition extends WithName("01") with SchemeHoldShare

  case object Contribution extends WithName("02") with SchemeHoldShare

  case object Transfer extends WithName("03") with SchemeHoldShare

  val values: List[SchemeHoldShare] = List(Acquisition, Contribution, Transfer)

  private val mappings: Map[String, SchemeHoldShare] = values.map(v => (v.toString, v)).toMap

  implicit val reads: Reads[SchemeHoldShare] =
    JsPath.read[String].flatMap {
      case aop if mappings.keySet.contains(aop) => Reads(_ => JsSuccess(mappings.apply(aop)))
      case invalidValue => Reads(_ => JsError(s"Invalid SchemeHoldShare type: $invalidValue"))
    }

  implicit val writes: Writes[SchemeHoldShare] = it => JsString(it.name)

  def stringToSchemeHoldShare(string: String): SchemeHoldShare =
    string match {
      case "01" => Acquisition
      case "02" => Contribution
      case "03" => Transfer
    }
}
