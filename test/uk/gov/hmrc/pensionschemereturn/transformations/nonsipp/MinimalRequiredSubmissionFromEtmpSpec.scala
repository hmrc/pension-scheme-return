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

import utils.TestValues
import com.softwaremill.diffx.scalatest.DiffShouldMatcher
import uk.gov.hmrc.pensionschemereturn.models.etmp.Compiled
import uk.gov.hmrc.pensionschemereturn.models.nonsipp._
import com.softwaremill.diffx.generic.auto.indicator
import org.scalatestplus.play.PlaySpec
import org.scalatestplus.mockito.MockitoSugar

import java.time.{LocalDate, LocalDateTime}
import java.time.format.DateTimeFormatter

class MinimalRequiredSubmissionFromEtmpSpec extends PlaySpec with MockitoSugar with TestValues with DiffShouldMatcher {

  private val transformation = new MinimalRequiredSubmissionFromEtmp()

  "MinimalRequiredSubmissionFromEtmp" should {
    "transform successfully" in {

      val expected = MinimalRequiredSubmission(
        reportDetails = ReportDetails(
          fbVersion = Some("001"),
          fbstatus = Some(Compiled),
          pstr = "12345678AA",
          periodStart = LocalDate.parse("2023-04-06"),
          periodEnd = LocalDate.parse("2024-04-05"),
          compilationOrSubmissionDate =
            Some(LocalDateTime.parse("2023-12-17T09:30:47Z", DateTimeFormatter.ISO_DATE_TIME))
        ),
        accountingPeriodDetails = AccountingPeriodDetails(
          recordVersion = Some("002"),
          accountingPeriods = List(
            LocalDate.parse("2022-04-06") -> LocalDate.parse("2022-12-31"),
            LocalDate.parse("2023-01-01") -> LocalDate.parse("2023-04-05")
          )
        ),
        schemeDesignatory = SchemeDesignatory(
          recordVersion = Some("002"),
          reasonForNoBankAccount = None,
          openBankAccount = true,
          activeMembers = 5,
          deferredMembers = 2,
          pensionerMembers = 10,
          totalAssetValueStart = Some(10000000),
          totalAssetValueEnd = Some(11000000),
          totalCashStart = Some(2500000),
          totalCashEnd = Some(2800000),
          totalPayments = Some(2000000)
        )
      )

      transformation.transform(samplePsrSubmissionEtmpResponse) shouldMatchTo expected
    }
  }
}
