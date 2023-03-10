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

package uk.gov.hmrc.pensionschemereturn.models

import play.api.libs.json._

case class PensionSchemeReturn(
  name: DataEntry[String]
)

case class DataEntry[A](
  value: A,
  rule: DataEntryRule,
  changed: DataEntryChanged[A]
)

case class DataEntryChanged[A](
  version: String,
  previousValue: A
)

sealed trait DataEntryRule

object DataEntryRule {
  case object Updated extends DataEntryRule

  case object Fixed extends DataEntryRule

  case object None extends DataEntryRule
}

object PensionSchemeReturn {
  implicit val dataEntryRuleWrites: Writes[DataEntryRule] = {
    case DataEntryRule.Updated => JsString("updated")
    case DataEntryRule.Fixed => JsString("fixed")
    case DataEntryRule.None => JsString("none")
  }

  implicit def dataEntryChangedWrites[A: Writes]: Writes[DataEntryChanged[A]] = Json.writes[DataEntryChanged[A]]

  implicit def dataEntryWrites[A: Writes]: Writes[DataEntry[A]] = Json.writes[DataEntry[A]]

  implicit val writes: Writes[PensionSchemeReturn] = Json.writes[PensionSchemeReturn]
}
