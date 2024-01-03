/*
 * Copyright 2024 HM Revenue & Customs
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

package uk.gov.hmrc.pensionschemereturn.models.etmp.nonsipp

import play.api.libs.json.{Format, Json}

case class TransferSchemeType(
  schemeType: String,
  refNumber: Option[String],
  otherDescription: Option[String]
)

object TransferSchemeType {

  def registeredScheme(ref: String): TransferSchemeType = TransferSchemeType(
    schemeType = "01",
    refNumber = Some(ref),
    otherDescription = None
  )

  def qrops(ref: String): TransferSchemeType = TransferSchemeType(
    schemeType = "02",
    refNumber = Some(ref),
    otherDescription = None
  )

  def other(description: String): TransferSchemeType = TransferSchemeType(
    schemeType = "03",
    refNumber = None,
    otherDescription = Some(description)
  )

  implicit val format: Format[TransferSchemeType] = Json.format[TransferSchemeType]
}
