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

package models

import play.api.libs.json.{JsObject, JsString, JsValue, Json, Writes}
import uk.gov.hmrc.pensionschemereturn.models.cache.PensionSchemeUser
import uk.gov.hmrc.pensionschemereturn.models.{IndividualDetails, MinimalDetails, SchemeDetails, SchemeStatus}

trait ModelSerializers {

  implicit val writesIndividualDetails: Writes[IndividualDetails] = Json.writes[IndividualDetails]
  implicit val writesMinimalDetails: Writes[MinimalDetails] = Json.writes[MinimalDetails]
  implicit val writesPensionSchemeUser: Writes[PensionSchemeUser] = s => JsString(s.toString)
  implicit val writesSchemeStatus: Writes[SchemeStatus] = s => JsString(s.toString)

  implicit val writeSchemeDetails: Writes[SchemeDetails] = { details =>
    val props: List[(String, JsValue)] = List(
      Some("schemeName" -> JsString(details.schemeName)),
      Some("pstr" -> JsString(details.pstr)),
      Some("schemeStatus" -> Json.toJson(details.schemeStatus)),
      details.authorisingPSAID.map(psa => "pspDetails" -> Json.obj("authorisingPSAID" -> psa))
    ).flatten

    JsObject(props)
  }
}
