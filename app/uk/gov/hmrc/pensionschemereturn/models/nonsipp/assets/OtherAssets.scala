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
  recordVersion: Option[String],
  optOtherAssetsWereHeld: Option[Boolean],
  optOtherAssetsWereDisposed: Option[Boolean],
  otherAssetTransactions: Seq[OtherAssetTransaction]
)

case class OtherAssetTransaction(
  prePopulated: Option[Boolean],
  assetDescription: String,
  methodOfHolding: SchemeHoldAsset,
  optDateOfAcqOrContrib: Option[LocalDate],
  costOfAsset: Double,
  optPropertyAcquiredFromName: Option[String],
  optPropertyAcquiredFrom: Option[PropertyAcquiredFrom],
  optConnectedStatus: Option[Boolean],
  optIndepValuationSupport: Option[Boolean],
  optMovableSchedule29A: Option[Boolean],
  optTotalIncomeOrReceipts: Option[Double],
  optOtherAssetDisposed: Option[Seq[OtherAssetDisposed]]
)

case class OtherAssetDisposed(
  methodOfDisposal: HowDisposed,
  optOtherMethod: Option[String],
  optDateSold: Option[LocalDate],
  optPurchaserName: Option[String],
  optPropertyAcquiredFrom: Option[PropertyAcquiredFrom],
  optTotalAmountReceived: Option[Double],
  optConnectedStatus: Option[Boolean],
  optSupportedByIndepValuation: Option[Boolean],
  anyPartAssetStillHeld: Boolean
)

object OtherAssets {

  private implicit val formatOtherAssetDisposed: OFormat[OtherAssetDisposed] =
    Json.format[OtherAssetDisposed]
  private implicit val formatOtherAssetTransaction: OFormat[OtherAssetTransaction] =
    Json.format[OtherAssetTransaction]
  implicit val format: OFormat[OtherAssets] = Json.format[OtherAssets]
}
