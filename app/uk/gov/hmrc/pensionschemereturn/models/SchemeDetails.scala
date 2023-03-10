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

import play.api.libs.json.{JsError, JsPath, JsString, JsSuccess, Json, Reads}
import uk.gov.hmrc.pensionschemereturn.utils.Named

case class SchemeDetails(schemeName: String, pstr: String, schemeStatus: SchemeStatus, authorisingPSAID: Option[String])

object SchemeDetails {

  implicit val reads: Reads[SchemeDetails] =
    Json.reads[SchemeDetails].flatMap { details =>
      (JsPath \ "pspDetails" \ "authorisingPSAID")
        .readNullable[String]
        .map(value => details.copy(authorisingPSAID = value))
    }

}

sealed trait SchemeStatus

object SchemeStatus {

  case object Pending extends Named("Pending") with SchemeStatus
  case object PendingInfoRequired extends Named("Pending Info Required") with SchemeStatus
  case object PendingInfoReceived extends Named("Pending Info Received") with SchemeStatus
  case object Rejected extends Named("Rejected") with SchemeStatus
  case object Open extends Named("Open") with SchemeStatus
  case object Deregistered extends Named("Deregistered") with SchemeStatus
  case object WoundUp extends Named("Wound-up") with SchemeStatus
  case object RejectedUnderAppeal extends Named("Rejected Under Appeal") with SchemeStatus

  implicit val reads: Reads[SchemeStatus] = {
    case JsString(Pending.name) => JsSuccess(Pending)
    case JsString(PendingInfoRequired.name) => JsSuccess(PendingInfoRequired)
    case JsString(PendingInfoReceived.name) => JsSuccess(PendingInfoReceived)
    case JsString(Rejected.name) => JsSuccess(Rejected)
    case JsString(Open.name) => JsSuccess(Open)
    case JsString(Deregistered.name) => JsSuccess(Deregistered)
    case JsString(WoundUp.name) => JsSuccess(WoundUp)
    case JsString(RejectedUnderAppeal.name) => JsSuccess(RejectedUnderAppeal)
    case _ => JsError("Unrecognized scheme status")
  }
}
