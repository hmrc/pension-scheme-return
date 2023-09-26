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
import uk.gov.hmrc.pensionschemereturn.models.requests.etmp.{
  AccountingPeriodDetailsRequest,
  AccountingPeriodRequest,
  MinimalRequiredSubmissionRequest,
  ReportDetailsRequest,
  SchemeDesignatoryRequest,
  Submitted
}

import java.time.LocalDate

class MinimalRequiredDetailsToPsrSpec extends PlaySpec with MockitoSugar {

  private val transformation =
    new MinimalRequiredDetailsToPsr()

  "MinimalRequiredDetailsToPsr" should {
    "ETMP submission should successfully transform to PSR format" in {

      val etmpSubmissions = MinimalRequiredSubmissionRequest(
        ReportDetailsRequest(
          "17836742CF",
          Submitted,
          periodStart = LocalDate.of(2020, 4, 6),
          periodEnd = LocalDate.of(2021, 4, 5)
        ),
        AccountingPeriodDetailsRequest(
          recordVersion = "001",
          accountingPeriods = List(
            AccountingPeriodRequest(
              accPeriodStart = LocalDate.of(2020, 6, 1),
              accPeriodEnd = LocalDate.of(2020, 6, 10)
            )
          )
        ),
        SchemeDesignatoryRequest(
          recordVersion = "001",
          openBankAccount = "Yes",
          reasonNoOpenAccount = None,
          noOfActiveMembers = 1,
          noOfDeferredMembers = 2,
          noOfPensionerMembers = 3,
          totalAssetValueStart = None,
          totalAssetValueEnd = None,
          totalCashStart = None,
          totalCashEnd = None,
          totalPayments = None
        )
      )

      val expected = MinimalRequiredSubmission(
        ReportDetails(
          "17836742CF",
          periodStart = LocalDate.of(2020, 4, 6),
          periodEnd = LocalDate.of(2021, 4, 5)
        ),
        List(LocalDate.of(2020, 6, 1) -> LocalDate.of(2020, 6, 10)),
        SchemeDesignatory(
          reasonForNoBankAccount = None,
          openBankAccount = true,
          activeMembers = 1,
          deferredMembers = 2,
          pensionerMembers = 3,
          totalAssetValueStart = None,
          totalAssetValueEnd = None,
          totalCashStart = None,
          totalCashEnd = None,
          totalPayments = None
        )
      )

      transformation.transform(etmpSubmissions) mustEqual expected
    }
  }
}
