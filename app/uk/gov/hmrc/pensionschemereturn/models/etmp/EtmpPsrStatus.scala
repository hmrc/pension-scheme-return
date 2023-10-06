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

package uk.gov.hmrc.pensionschemereturn.models.etmp

import play.api.libs.json._

sealed trait EtmpPsrStatus {
  val name: String
}

case object Compiled extends EtmpPsrStatus {
  val name = "Compiled"
}

case object Submitted extends EtmpPsrStatus {
  val name = "Submitted"
}

object EtmpPsrStatus {

  private val values: List[EtmpPsrStatus] = List(Compiled, Submitted)

  implicit val formats: Format[EtmpPsrStatus] = new Format[EtmpPsrStatus] {
    override def writes(o: EtmpPsrStatus): JsValue = JsString(o.name)

    override def reads(json: JsValue): JsResult[EtmpPsrStatus] = {
      val jsonAsString = json.as[String]
      values.find(_.toString == jsonAsString) match {
        case Some(status) => JsSuccess(status)
        case None => JsError(s"Unknown psr status: $jsonAsString")
      }
    }
  }
}
