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

import uk.gov.hmrc.pensionschemereturn.models.etmp.SubmitterType
import play.api.libs.json.{Format, Json}

case class EtmpPsrDeclaration(
  submittedBy: SubmitterType,
  submitterId: String,
  psaId: Option[String],
  psaDeclaration: Option[EtmpPsaDeclaration],
  pspDeclaration: Option[EtmpPspDeclaration]
)

case class EtmpPsaDeclaration(
  psaDeclaration1: Boolean,
  psaDeclaration2: Boolean
)

case class EtmpPspDeclaration(
  pspDeclaration1: Boolean,
  pspDeclaration2: Boolean
)

object EtmpPsrDeclaration {
  private implicit val formatEtmpPspDeclaration: Format[EtmpPspDeclaration] =
    Json.format[EtmpPspDeclaration]
  private implicit val formatEtmpPsaDeclaration: Format[EtmpPsaDeclaration] = Json.format[EtmpPsaDeclaration]
  implicit val format: Format[EtmpPsrDeclaration] = Json.format[EtmpPsrDeclaration]
}
