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

package uk.gov.hmrc.pensionschemereturn.models.nonsipp.assets

import play.api.libs.json._
import uk.gov.hmrc.pensionschemereturn.utils.WithName

sealed trait HowDisposed {
  val name: String
}

object HowDisposed {

  case object Sold extends WithName("Sold") with HowDisposed

  case object Transferred extends WithName("Transferred") with HowDisposed

  case object Other extends WithName("Other") with HowDisposed

  val values: List[HowDisposed] = List(Sold, Transferred, Other)

  private val mappings: Map[String, HowDisposed] = values.map(v => (v.toString, v)).toMap

  implicit val reads: Reads[HowDisposed] =
    JsPath.read[String].flatMap {
      case aop if mappings.keySet.contains(aop) => Reads(_ => JsSuccess(mappings.apply(aop)))
      case invalidValue => Reads(_ => JsError(s"Invalid HowDisposed type: $invalidValue"))
    }

  implicit val writes: Writes[HowDisposed] = it => JsString(it.name)

  def howDisposedToString(howDisposed: HowDisposed): String =
    howDisposed match {
      case Sold => "01"
      case Transferred => "02"
      case Other => "03"
    }

  def stringToHowDisposed(string: String): HowDisposed =
    string match {
      case "01" => Sold
      case "02" => Transferred
      case "03" => Other
    }

}
