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

package uk.gov.hmrc.pensionschemereturn.transformations.nonsipp

import com.google.inject.{Inject, Singleton}
import uk.gov.hmrc.pensionschemereturn.models.nonsipp._
import uk.gov.hmrc.pensionschemereturn.models.response.PsrSubmissionEtmpResponse
import uk.gov.hmrc.pensionschemereturn.transformations.Transformer

import java.time.LocalDate

@Singleton()
class StandardPsrFromEtmp @Inject()(
  minimalRequiredSubmissionFromEtmp: MinimalRequiredSubmissionFromEtmp,
  loansFromEtmp: LoansFromEtmp,
  assetsFromEtmp: AssetsFromEtmp
) extends Transformer {

  def transform(psrSubmissionResponse: PsrSubmissionEtmpResponse): PsrSubmission = {
    val minimalRequiredSubmission: MinimalRequiredSubmission =
      minimalRequiredSubmissionFromEtmp.transform(psrSubmissionResponse)

    PsrSubmission(
      minimalRequiredSubmission = minimalRequiredSubmission,
      checkReturnDates =
        isCheckReturnDates(minimalRequiredSubmission.reportDetails, minimalRequiredSubmission.accountingPeriods.head),
      loans = psrSubmissionResponse.loans.map(loansFromEtmp.transform),
      assets = psrSubmissionResponse.assets.map(assetsFromEtmp.transform)
    )
  }

  private def isCheckReturnDates(reportDetails: ReportDetails, accountingPeriods: (LocalDate, LocalDate)): Boolean =
    reportDetails.periodStart == accountingPeriods._1 && reportDetails.periodEnd == accountingPeriods._2

}
