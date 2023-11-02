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
import uk.gov.hmrc.pensionschemereturn.models.etmp._
import uk.gov.hmrc.pensionschemereturn.models.etmp.nonsipp._
import uk.gov.hmrc.pensionschemereturn.models.nonsipp.MinimalRequiredSubmission
import uk.gov.hmrc.pensionschemereturn.transformations.Transformer

@Singleton()
class MinimalRequiredDetailsToEtmp @Inject()() extends Transformer {

  def transform(minimalRequiredSubmission: MinimalRequiredSubmission): EtmpMinimalRequiredSubmission =
    EtmpMinimalRequiredSubmission(
      EtmpReportDetails(
        pstr = minimalRequiredSubmission.reportDetails.pstr,
        psrStatus = Compiled,
        periodStart = minimalRequiredSubmission.reportDetails.periodStart,
        periodEnd = minimalRequiredSubmission.reportDetails.periodEnd
      ),
      EtmpAccountingPeriodDetails(
        recordVersion = None,
        accountingPeriods = minimalRequiredSubmission.accountingPeriods.map {
          case (start, end) =>
            EtmpAccountingPeriod(
              accPeriodStart = start,
              accPeriodEnd = end
            )
        }
      ),
      EtmpSchemeDesignatory(
        recordVersion = None,
        openBankAccount = toYesNo(minimalRequiredSubmission.schemeDesignatory.openBankAccount),
        reasonNoOpenAccount = minimalRequiredSubmission.schemeDesignatory.reasonForNoBankAccount,
        noOfActiveMembers = minimalRequiredSubmission.schemeDesignatory.activeMembers,
        noOfDeferredMembers = minimalRequiredSubmission.schemeDesignatory.deferredMembers,
        noOfPensionerMembers = minimalRequiredSubmission.schemeDesignatory.pensionerMembers,
        totalAssetValueStart = minimalRequiredSubmission.schemeDesignatory.totalAssetValueStart,
        totalAssetValueEnd = minimalRequiredSubmission.schemeDesignatory.totalAssetValueEnd,
        totalCashStart = minimalRequiredSubmission.schemeDesignatory.totalCashStart,
        totalCashEnd = minimalRequiredSubmission.schemeDesignatory.totalCashEnd,
        totalPayments = minimalRequiredSubmission.schemeDesignatory.totalPayments
      )
    )

}
