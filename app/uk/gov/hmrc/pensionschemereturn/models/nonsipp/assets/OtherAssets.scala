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

package uk.gov.hmrc.pensionschemereturn.models.nonsipp.assets

import uk.gov.hmrc.pensionschemereturn.models.nonsipp.PropertyAcquiredFrom
import play.api.libs.json.{Json, OFormat}

import java.time.LocalDate

case class OtherAssets(
  otherAssetsWereHeld: Boolean,
  otherAssetsWereDisposed: Boolean,
  otherAssetTransactions: Seq[OtherAssetTransaction]
)

case class OtherAssetTransaction(
  assetDescription: String,
  methodOfHolding: SchemeHoldAsset,
  optDateOfAcqOrContrib: Option[LocalDate],
  costOfAsset: Double,
  optPropertyAcquiredFromName: Option[String],
  optPropertyAcquiredFrom: Option[PropertyAcquiredFrom],
  optConnectedStatus: Option[Boolean],
  optIndepValuationSupport: Option[Boolean],
  movableSchedule29A: Boolean,
  totalIncomeOrReceipts: Double
)

object OtherAssets {

  private implicit val formatOtherAssetTransaction: OFormat[OtherAssetTransaction] =
    Json.format[OtherAssetTransaction]
  implicit val format: OFormat[OtherAssets] = Json.format[OtherAssets]
}
