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

package uk.gov.hmrc.pensionschemereturn.models.nonsipp.shares

import play.api.libs.json._
import uk.gov.hmrc.pensionschemereturn.utils.WithName

sealed trait HowSharesDisposed {
  val name: String
}

object HowSharesDisposed {

  case object Sold extends WithName("Sold") with HowSharesDisposed
  case object Redeemed extends WithName("Redeemed") with HowSharesDisposed
  case object Transferred extends WithName("Transferred") with HowSharesDisposed
  case object Other extends WithName("Other") with HowSharesDisposed

  val values: List[HowSharesDisposed] = List(Sold, Redeemed, Transferred, Other)

  private val mappings: Map[String, HowSharesDisposed] = values.map(v => (v.toString, v)).toMap

  implicit val reads: Reads[HowSharesDisposed] =
    JsPath.read[String].flatMap {
      case aop if mappings.keySet.contains(aop) => Reads(_ => JsSuccess(mappings.apply(aop)))
      case invalidValue => Reads(_ => JsError(s"Invalid HowSharesDisposed type: $invalidValue"))
    }

  implicit val writes: Writes[HowSharesDisposed] = it => JsString(it.name)

  implicit val format: Format[HowSharesDisposed] = Format(reads, writes)

  def howSharesDisposedToString(howSharesDisposed: HowSharesDisposed): String =
    howSharesDisposed match {
      case Sold => "01"
      case Redeemed => "02"
      case Transferred => "03"
      case Other => "04"
    }

  def stringToHowSharesDisposed(string: String): HowSharesDisposed =
    string match {
      case "01" => Sold
      case "02" => Redeemed
      case "03" => Transferred
      case "04" => Other
    }
}
