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
import uk.gov.hmrc.pensionschemereturn.models.nonsipp.PsrSubmission
import uk.gov.hmrc.pensionschemereturn.models.requests.etmp.PsrSubmissionEtmpRequest

@Singleton()
class PsrSubmissionToEtmp @Inject()(
  minimalRequiredDetailsToEtmp: MinimalRequiredDetailsToEtmp,
  loansToEtmp: LoansToEtmp,
  assetsToEtmp: AssetsToEtmp,
  memberPaymentsTransformer: MemberPaymentsTransformer,
  sharesToEtmp: SharesToEtmp
) {

  def transform(psrSubmission: PsrSubmission): PsrSubmissionEtmpRequest = {
    val etmpMinimalRequiredSubmission =
      minimalRequiredDetailsToEtmp.transform(psrSubmission.minimalRequiredSubmission)
    PsrSubmissionEtmpRequest(
      reportDetails = etmpMinimalRequiredSubmission.reportDetails,
      accountingPeriodDetails = etmpMinimalRequiredSubmission.accountingPeriodDetails,
      schemeDesignatory = etmpMinimalRequiredSubmission.schemeDesignatory,
      loans = psrSubmission.loans.map(loansToEtmp.transform),
      assets = psrSubmission.assets.map(assetsToEtmp.transform),
      membersPayments = psrSubmission.membersPayments.map(memberPaymentsTransformer.toEtmp),
      shares = psrSubmission.shares.map(sharesToEtmp.transform)
    )
  }

}
