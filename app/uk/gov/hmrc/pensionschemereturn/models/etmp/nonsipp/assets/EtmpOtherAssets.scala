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

package uk.gov.hmrc.pensionschemereturn.models.etmp.nonsipp.assets

import uk.gov.hmrc.pensionschemereturn.models.etmp.nonsipp.common.EtmpIdentityType
import uk.gov.hmrc.pensionschemereturn.models.etmp.YesNo
import play.api.libs.json.{Json, OFormat}

import java.time.LocalDate

case class EtmpOtherAssets(
  recordVersion: Option[String],
  otherAssetsWereHeld: Option[String],
  otherAssetsWereDisposed: Option[String],
  noOfTransactions: Option[Int],
  otherAssetTransactions: Option[Seq[EtmpOtherAssetTransaction]]
)

case class EtmpOtherAssetTransaction(
  prePopulated: Option[YesNo],
  assetDescription: String,
  methodOfHolding: String,
  dateOfAcqOrContrib: Option[LocalDate],
  costOfAsset: Double,
  acquiredFromName: Option[String],
  acquiredFromType: Option[EtmpIdentityType],
  connectedStatus: Option[String],
  supportedByIndepValuation: Option[String],
  movableSchedule29A: Option[String],
  totalIncomeOrReceipts: Option[Double],
  assetsDisposed: Option[Seq[EtmpAssetsDisposed]]
)

case class EtmpAssetsDisposed(
  methodOfDisposal: String,
  otherMethod: Option[String],
  dateSold: Option[LocalDate],
  purchaserName: Option[String],
  purchaserType: Option[EtmpIdentityType],
  totalAmountReceived: Option[Double],
  connectedStatus: Option[String],
  supportedByIndepValuation: Option[String],
  fullyDisposedOf: String
)

object EtmpOtherAssets {

  private implicit val formatsEtmpAssetsDisposed: OFormat[EtmpAssetsDisposed] =
    Json.format[EtmpAssetsDisposed]
  private implicit val formatsEtmpOtherAssetTransaction: OFormat[EtmpOtherAssetTransaction] =
    Json.format[EtmpOtherAssetTransaction]
  implicit val formats: OFormat[EtmpOtherAssets] =
    Json.format[EtmpOtherAssets]
}
