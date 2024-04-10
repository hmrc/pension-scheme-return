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

package uk.gov.hmrc.pensionschemereturn.transformations.nonsipp

import uk.gov.hmrc.pensionschemereturn.models.response.PsrSubmissionEtmpResponse
import com.google.inject.{Inject, Singleton}
import uk.gov.hmrc.pensionschemereturn.models.nonsipp.{MinimalRequiredSubmission, ReportDetails, SchemeDesignatory}
import uk.gov.hmrc.pensionschemereturn.transformations.Transformer

@Singleton()
class MinimalRequiredSubmissionFromEtmp @Inject()() extends Transformer {

  def transform(psrSubmissionResponse: PsrSubmissionEtmpResponse): MinimalRequiredSubmission =
    MinimalRequiredSubmission(
      reportDetails = ReportDetails(
        pstr = psrSubmissionResponse.schemeDetails.pstr,
        periodStart = psrSubmissionResponse.psrDetails.periodStart,
        periodEnd = psrSubmissionResponse.psrDetails.periodEnd
      ),
      accountingPeriods = psrSubmissionResponse.accountingPeriodDetails.accountingPeriods
        .map(accPeriod => (accPeriod.accPeriodStart, accPeriod.accPeriodEnd)),
      schemeDesignatory = SchemeDesignatory(
        reasonForNoBankAccount = psrSubmissionResponse.schemeDesignatory.reasonNoOpenAccount,
        openBankAccount = fromYesNo(psrSubmissionResponse.schemeDesignatory.openBankAccount),
        activeMembers = psrSubmissionResponse.schemeDesignatory.noOfActiveMembers,
        deferredMembers = psrSubmissionResponse.schemeDesignatory.noOfDeferredMembers,
        pensionerMembers = psrSubmissionResponse.schemeDesignatory.noOfPensionerMembers,
        totalAssetValueStart = psrSubmissionResponse.schemeDesignatory.totalAssetValueStart,
        totalAssetValueEnd = psrSubmissionResponse.schemeDesignatory.totalAssetValueEnd,
        totalCashStart = psrSubmissionResponse.schemeDesignatory.totalCashStart,
        totalCashEnd = psrSubmissionResponse.schemeDesignatory.totalCashEnd,
        totalPayments = psrSubmissionResponse.schemeDesignatory.totalPayments
      )
    )
}
