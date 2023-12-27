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
import play.api.libs.functional.syntax._

sealed trait PensionSchemeType {
  val name: String
}

object PensionSchemeType {
  case class RegisteredPS(description: String) extends PensionSchemeType {
    val name = "registeredPS"
  }

  case class QualifyingRecognisedOverseasPS(description: String) extends PensionSchemeType {
    val name = "qualifyingRecognisedOverseasPS"
  }

  case class Other(description: String) extends PensionSchemeType {
    val name = "other"
  }

  implicit val writes: Writes[PensionSchemeType] = {
    case schemeType @ RegisteredPS(description) => Json.obj("key" -> schemeType.name, "value" -> description)
    case schemeType @ QualifyingRecognisedOverseasPS(description) =>
      Json.obj("key" -> schemeType.name, "value" -> description)
    case schemeType @ Other(description) => Json.obj("key" -> schemeType.name, "value" -> description)
  }

  implicit val reads: Reads[PensionSchemeType] =
    (__ \ "key")
      .read[String]
      .and((__ \ "value").read[String])
      .tupled
      .flatMap {
        case ("registeredPS", value) => Reads.pure(RegisteredPS(value))
        case ("qualifyingRecognisedOverseasPS", value) => Reads.pure(QualifyingRecognisedOverseasPS(value))
        case ("other", value) => Reads.pure(Other(value))
        case unknown => Reads.failed(s"Failed to read EmployerType with unknown pattern $unknown")
      }
}
