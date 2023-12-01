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

package uk.gov.hmrc.pensionschemereturn.transformations.sipp

import com.google.inject.{Inject, Singleton}
import uk.gov.hmrc.pensionschemereturn.models.response.SippPsrSubmissionEtmpResponse
import uk.gov.hmrc.pensionschemereturn.models.sipp.{SippPsrSubmission, SippReportDetailsSubmission}
import uk.gov.hmrc.pensionschemereturn.transformations.Transformer

@Singleton()
class SippPsrFromEtmp @Inject()() extends Transformer {

  def transform(sippPsrSubmissionEtmpResponse: SippPsrSubmissionEtmpResponse): SippPsrSubmission = {
    val reportDetails = sippPsrSubmissionEtmpResponse.reportDetails
    SippPsrSubmission(
      reportDetails = SippReportDetailsSubmission(
        pstr = reportDetails.pstr,
        periodStart = reportDetails.periodStart,
        periodEnd = reportDetails.periodEnd,
        memberTransactions = reportDetails.memberTransactions
      )
    )

  }
}