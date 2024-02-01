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

// Cost Value = Cost, Market Value = Market
sealed trait EtmpSippCostOrMarketType {
  val value: String
  val definition: String
}

object EtmpSippCostOrMarketType {
  case object Market extends EtmpSippCostOrMarketType {
    val value = "Market Value"
    val definition = "Market"
  }
  case object Cost extends EtmpSippCostOrMarketType {
    val value = "Cost Value"
    val definition = "Cost"
  }

  def apply(definition: String): EtmpSippCostOrMarketType = definition match {
    case Market.definition => Market
    case Cost.definition => Cost
    case _ => throw new RuntimeException("Couldn't match the type for EtmpSippCostOrMarketType!")
  }

  implicit val writes: Writes[EtmpSippCostOrMarketType] = invOrOrgType => JsString(invOrOrgType.value)
  implicit val reads: Reads[EtmpSippCostOrMarketType] = Reads {
    case JsString(Market.value) => JsSuccess(Market)
    case JsString(Cost.value) => JsSuccess(Cost)
    case unknown => JsError(s"Unknown value for YesNo: $unknown")
  }
}
