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

import org.mockito.MockitoSugar
import org.scalatestplus.play.PlaySpec
import uk.gov.hmrc.pensionschemereturn.models._
import uk.gov.hmrc.pensionschemereturn.models.requests.etmp._

import java.time.LocalDate

class MinimalRequiredDetailsToEtmpSpec extends PlaySpec with MockitoSugar with Transformer {
  private val transformation: MinimalRequiredDetailsToEtmp = new MinimalRequiredDetailsToEtmp()
  val today: LocalDate = LocalDate.now

  "LoansToEtmp" should {
    "PSR minimalRequiredDetails should successfully transform to etmp format" in {

      val minimalRequiredSubmission = MinimalRequiredSubmission(
        reportDetails = ReportDetails("testPstr", today, today),
        accountingPeriods = List(today -> today),
        schemeDesignatory = SchemeDesignatory(
          reasonForNoBankAccount = Some("reasonForNoBankAccount"),
          openBankAccount = false,
          activeMembers = 1,
          deferredMembers = 2,
          pensionerMembers = 3,
          totalAssetValueStart = Some(Double.MaxValue),
          totalAssetValueEnd = None,
          totalCashStart = Some(Double.MaxValue),
          totalCashEnd = None,
          totalPayments = Some(Double.MaxValue)
        )
      )

      val expected = MinimalRequiredSubmissionRequest(
        ReportDetailsRequest(
          pstr = "testPstr",
          psrStatus = Compiled,
          periodStart = today,
          periodEnd = today
        ),
        AccountingPeriodDetailsRequest(
          recordVersion = "001",
          accountingPeriods = List(
            AccountingPeriodRequest(
              accPeriodStart = today,
              accPeriodEnd = today
            )
          )
        ),
        SchemeDesignatoryRequest(
          recordVersion = "001",
          openBankAccount = No,
          reasonNoOpenAccount = Some("reasonForNoBankAccount"),
          noOfActiveMembers = 1,
          noOfDeferredMembers = 2,
          noOfPensionerMembers = 3,
          totalAssetValueStart = Some(Double.MaxValue),
          totalAssetValueEnd = None,
          totalCashStart = Some(Double.MaxValue),
          totalCashEnd = None,
          totalPayments = Some(Double.MaxValue)
        )
      )

      transformation.transform(minimalRequiredSubmission) mustEqual expected
    }

  }
}
