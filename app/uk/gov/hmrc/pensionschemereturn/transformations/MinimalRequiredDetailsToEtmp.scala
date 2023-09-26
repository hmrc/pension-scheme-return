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

@Singleton()
class MinimalRequiredDetailsToEtmp @Inject()() {

  def transform(minimalRequiredSubmission: MinimalRequiredSubmission): ETMPMinimalRequiredDetails =
    ETMPMinimalRequiredDetails(
      ETMPReportDetails(
        pstr = minimalRequiredSubmission.reportDetails.pstr,
        psrStatus = Compiled,
        periodStart = minimalRequiredSubmission.reportDetails.periodStart,
        periodEnd = minimalRequiredSubmission.reportDetails.periodEnd
      ),
      ETMPAccountingPeriodDetails(
        recordVersion = "001", // TODO hardcoded for now
        accountingPeriods = minimalRequiredSubmission.accountingPeriods.map {
          case (start, end) =>
            ETMPAccountingPeriod(
              accPeriodStart = start,
              accPeriodEnd = end
            )
        }
      ),
      ETMPSchemeDesignatory(
        recordVersion = "001", // TODO hardcoded for now
        openBankAccount = if (minimalRequiredSubmission.schemeDesignatory.openBankAccount) "Yes" else "No",
        reasonNoOpenAccount = minimalRequiredSubmission.schemeDesignatory.reasonForNoBankAccount,
        noOfActiveMembers = minimalRequiredSubmission.schemeDesignatory.activeMembers,
        noOfDeferredMembers = minimalRequiredSubmission.schemeDesignatory.deferredMembers,
        noOfPensionerMembers = minimalRequiredSubmission.schemeDesignatory.pensionerMembers,
        totalPayments = minimalRequiredSubmission.schemeDesignatory.totalPayments
      )
    )

}
