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

import com.softwaremill.diffx.generic.auto.diffForCaseClass
import com.softwaremill.diffx.scalatest.DiffShouldMatcher
import generators.ModelGenerators
import org.mockito.MockitoSugar
import org.scalatestplus.play.PlaySpec
import uk.gov.hmrc.pensionschemereturn.models.nonsipp.{MinimalRequiredSubmission, ReportDetails, SchemeDesignatory}
import utils.TestValues

import java.time.LocalDate

class MinimalRequiredSubmissionFromEtmpSpec
    extends PlaySpec
    with MockitoSugar
    with ModelGenerators
    with TestValues
    with DiffShouldMatcher {

  private val transformation = new MinimalRequiredSubmissionFromEtmp()

  "MinimalRequiredSubmissionFromEtmp" should {
    "transform successfully" in {

      val expected = MinimalRequiredSubmission(
        ReportDetails(
          "12345678AA",
          periodStart = LocalDate.parse("2023-04-06"),
          periodEnd = LocalDate.parse("2024-04-05")
        ),
        List(
          LocalDate.parse("2022-04-06") -> LocalDate.parse("2022-12-31"),
          LocalDate.parse("2023-01-01") -> LocalDate.parse("2023-04-05")
        ),
        SchemeDesignatory(
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
