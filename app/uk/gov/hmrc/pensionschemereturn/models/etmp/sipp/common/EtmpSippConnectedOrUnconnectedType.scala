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

// 01 = Connected, 02 = Unconnected
sealed trait EtmpSippConnectedOrUnconnectedType {
  val value: String
  val definition: String
}

object EtmpSippConnectedOrUnconnectedType {
  case object Connected extends EtmpSippConnectedOrUnconnectedType {
    val value = "01"
    val definition = "Connected"
  }
  case object Unconnected extends EtmpSippConnectedOrUnconnectedType {
    val value = "02"
    val definition = "Unconnected"
  }

  def apply(definition: String): EtmpSippConnectedOrUnconnectedType = definition match {
    case Connected.definition => Connected
    case Unconnected.definition => Unconnected
    case _ => throw new RuntimeException("Couldn't match the type for EtmpSippConnectedOrUnconnectedType!")
  }

  implicit val writes: Writes[EtmpSippConnectedOrUnconnectedType] = invOrOrgType => JsString(invOrOrgType.value)
  implicit val reads: Reads[EtmpSippConnectedOrUnconnectedType] = Reads {
    case JsString(Connected.value) => JsSuccess(Connected)
    case JsString(Unconnected.value) => JsSuccess(Unconnected)
    case unknown => JsError(s"Unknown value for YesNo: $unknown")
  }
}
