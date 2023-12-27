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

package uk.gov.hmrc.pensionschemereturn.transformations

import play.api.libs.json.{Json, Writes}

sealed trait TransformerError {
  val value: String
}

object TransformerError {
  case object NoIdOrReason extends TransformerError {
    val value = "both idNumber and reasonNoIdNumber are empty for UKCompany"
  }

  case class UnknownError(msg: String) extends TransformerError {
    val value = msg
  }

  case object OtherNoDescription extends TransformerError {
    val value = "Identity is Other but description is missing"
  }

  implicit val writes: Writes[TransformerError] = err => Json.obj("error" -> err.value)
}
