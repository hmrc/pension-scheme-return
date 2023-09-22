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

package uk.gov.hmrc.pensionschemereturn.transformations.toetmp

import play.api.libs.functional.syntax.toFunctionalBuilderOps
import play.api.libs.json.Reads.JsObjectReducer
import play.api.libs.json._
import uk.gov.hmrc.pensionschemereturn.transformations.Transformer

import javax.inject.Inject

class EPID1444 @Inject() extends Transformer {

  private val pathToReportDetails: JsPath = __ \ Symbol("reportDetails")
  private val pathToAccountingPeriodDetails: JsPath = __ \ Symbol("accountingPeriodDetails")
  private val pathToSchemeDesignatory: JsPath = __ \ Symbol("schemeDesignatory")
  private val pathToLoans: JsPath = __ \ Symbol("loans")

  val transformToETMPData: Reads[JsObject] = {
    (
      readsReportDetails("Compiled") // TODO Hardcoded for now
        and
        (pathToAccountingPeriodDetails \ Symbol("accountingPeriods")).json.copyFrom(readsAccountingPeriods)
      ).reduce


  }

  private def readsReportDetails(psrStatus: String): Reads[JsObject] = {
    (
      (pathToReportDetails \ Symbol("pstr")).json.copyFrom((pathToReportDetails \ Symbol("pstr")).json.pick) and
        (pathToReportDetails \ Symbol("psrStatus")).json.put(JsString(psrStatus)) and
        (pathToReportDetails \ Symbol("periodStart")).json.copyFrom((pathToReportDetails \ Symbol("from")).json.pick) and
        (pathToReportDetails \ Symbol("periodEnd")).json.copyFrom((pathToReportDetails \ Symbol("to")).json.pick)
      ).reduce
  }

  private def readsAccountingPeriods: Reads[JsArray] = {
    JsArray(
      (__ \ Symbol("checkReturnDates")).json.pick.flatMap {
        case JsTrue => (__ \ Symbol("accountingPeriods")).json.pick
        case JsFalse => fail[JsArray]
      }
    )
  }

}
