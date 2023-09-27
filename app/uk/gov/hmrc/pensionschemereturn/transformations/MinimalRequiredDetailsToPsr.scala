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
import uk.gov.hmrc.pensionschemereturn.models.requests.etmp.MinimalRequiredSubmissionRequest

@Singleton()
class MinimalRequiredDetailsToPsr @Inject()() {

  def transform(etmpMinimalRequiredDetails: MinimalRequiredSubmissionRequest): MinimalRequiredSubmission =
    MinimalRequiredSubmission(
      ReportDetails(
        pstr = etmpMinimalRequiredDetails.reportDetails.pstr,
        periodStart = etmpMinimalRequiredDetails.reportDetails.periodStart,
        periodEnd = etmpMinimalRequiredDetails.reportDetails.periodEnd
      ),
      accountingPeriods = etmpMinimalRequiredDetails.accountingPeriodDetails.accountingPeriods
        .map(accountingPeriod => (accountingPeriod.accPeriodStart, accountingPeriod.accPeriodEnd)),
      SchemeDesignatory(
        openBankAccount = etmpMinimalRequiredDetails.schemeDesignatory.openBankAccount.toLowerCase == "yes",
        reasonForNoBankAccount = etmpMinimalRequiredDetails.schemeDesignatory.reasonNoOpenAccount,
        activeMembers = etmpMinimalRequiredDetails.schemeDesignatory.noOfActiveMembers,
        deferredMembers = etmpMinimalRequiredDetails.schemeDesignatory.noOfDeferredMembers,
        pensionerMembers = etmpMinimalRequiredDetails.schemeDesignatory.noOfPensionerMembers,
        totalAssetValueStart = etmpMinimalRequiredDetails.schemeDesignatory.totalAssetValueStart,
        totalAssetValueEnd = etmpMinimalRequiredDetails.schemeDesignatory.totalAssetValueEnd,
        totalCashStart = etmpMinimalRequiredDetails.schemeDesignatory.totalCashStart,
        totalCashEnd = etmpMinimalRequiredDetails.schemeDesignatory.totalCashEnd,
        totalPayments = etmpMinimalRequiredDetails.schemeDesignatory.totalPayments
      )
    )
}
