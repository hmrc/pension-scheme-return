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

package uk.gov.hmrc.pensionschemereturn.models.response

import play.api.libs.functional.syntax.toFunctionalBuilderOps
import play.api.libs.json.{JsPath, Json, Reads}
import uk.gov.hmrc.pensionschemereturn.models.etmp.nonsipp.{EtmpAccountingPeriod, EtmpAccountingPeriodDetails}
import uk.gov.hmrc.pensionschemereturn.models.etmp.sipp.EtmpSippReportDetails

case class SippPsrSubmissionEtmpResponse(
  reportDetails: EtmpSippReportDetails,
  accountingPeriodDetails: EtmpAccountingPeriodDetails
)

object SippPsrSubmissionEtmpResponse {

  private implicit val accountingPeriodReads: Reads[EtmpAccountingPeriod] = Json.reads[EtmpAccountingPeriod]

  implicit val accountingPeriodDetailsReads: Reads[EtmpAccountingPeriodDetails] = {
    (JsPath \ "version")
      .readNullable[String]
      .and((JsPath \ "accountingPeriods").read[List[EtmpAccountingPeriod]])(EtmpAccountingPeriodDetails.apply _)
  }

  implicit val reads: Reads[SippPsrSubmissionEtmpResponse] = Json.reads[SippPsrSubmissionEtmpResponse]
}
