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

package uk.gov.hmrc.pensionschemereturn.transformations

import com.google.inject.{Inject, Singleton}
import uk.gov.hmrc.pensionschemereturn.models._
import uk.gov.hmrc.pensionschemereturn.models.requests.etmp._

@Singleton()
class PsrSubmissionToEtmp @Inject()(
  minimalRequiredDetailsToEtmp: MinimalRequiredDetailsToEtmp,
  loansToEtmp: LoansToEtmp
) {

  def transform(psrSubmission: PsrSubmission): PsrSubmissionRequest = {
    val minimalRequiredSubmissionRequest =
      minimalRequiredDetailsToEtmp.transform(psrSubmission.minimalRequiredSubmission)
    PsrSubmissionRequest(
      minimalRequiredSubmissionRequest.reportDetails,
      minimalRequiredSubmissionRequest.accountingPeriodDetails,
      minimalRequiredSubmissionRequest.schemeDesignatory,
      loans = psrSubmission.loans.map(loansToEtmp.transform)
    )
  }

}