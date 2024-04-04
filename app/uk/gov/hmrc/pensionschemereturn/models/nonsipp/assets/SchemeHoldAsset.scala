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

sealed trait SchemeHoldAsset {
  val name: String
}

object SchemeHoldAsset {

  case object Acquisition extends WithName("01") with SchemeHoldAsset

  case object Contribution extends WithName("02") with SchemeHoldAsset

  case object Transfer extends WithName("03") with SchemeHoldAsset

  val values: List[SchemeHoldAsset] = List(Acquisition, Contribution, Transfer)

  private val mappings: Map[String, SchemeHoldAsset] = values.map(v => (v.toString, v)).toMap

  implicit val reads: Reads[SchemeHoldAsset] =
    JsPath.read[String].flatMap {
      case aop if mappings.keySet.contains(aop) => Reads(_ => JsSuccess(mappings.apply(aop)))
      case invalidValue => Reads(_ => JsError(s"Invalid SchemeHoldAsset type: $invalidValue"))
    }

  implicit val writes: Writes[SchemeHoldAsset] = it => JsString(it.name)

  def schemeHoldAssetToString(schemeHoldAsset: SchemeHoldAsset): String =
    schemeHoldAsset match {
      case Acquisition => "01"
      case Contribution => "02"
      case Transfer => "03"
    }

  def stringToSchemeHoldAsset(string: String): SchemeHoldAsset =
    string match {
      case "01" => Acquisition
      case "02" => Contribution
      case "03" => Transfer
    }

}
