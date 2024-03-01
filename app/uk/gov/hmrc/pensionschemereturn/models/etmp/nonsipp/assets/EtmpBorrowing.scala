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

import play.api.libs.json.{Json, OFormat}

import java.time.LocalDate

case class EtmpBorrowing(
  recordVersion: Option[String],
  moneyWasBorrowed: String,
  noOfBorrows: Option[Int],
  moneyBorrowed: Option[Seq[EtmpMoneyBorrowed]]
)

case class EtmpMoneyBorrowed(
  dateOfBorrow: LocalDate,
  schemeAssetsValue: Double,
  amountBorrowed: Double,
  interestRate: Double,
  borrowingFromName: String,
  connectedPartyStatus: String,
  reasonForBorrow: String
)

object EtmpBorrowing {
  private implicit val formatsEtmpMoneyBorrowed: OFormat[EtmpMoneyBorrowed] =
    Json.format[EtmpMoneyBorrowed]
  implicit val formats: OFormat[EtmpBorrowing] =
    Json.format[EtmpBorrowing]
}
