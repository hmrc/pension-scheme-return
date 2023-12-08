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

import play.api.libs.json.{Format, JsError, JsString, JsSuccess}

sealed trait SectionStatus {
  val value: String
}

object SectionStatus {
  case object New extends SectionStatus {
    val value = "New"
  }
  case object Changed extends SectionStatus {
    val value = "Changed"
  }
  case object Deleted extends SectionStatus {
    val value = "Deleted"
  }

  implicit val format: Format[SectionStatus] = Format(
    {
      case JsString(New.value) => JsSuccess(New)
      case JsString(Changed.value) => JsSuccess(Changed)
      case JsString(Deleted.value) => JsSuccess(Deleted)
      case unknown => JsError(s"Unknown value for SectionStatus ${unknown}")
    },
    status => JsString(status.value)
  )
}
