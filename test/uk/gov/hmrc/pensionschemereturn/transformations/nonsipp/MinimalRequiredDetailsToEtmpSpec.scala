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

import com.softwaremill.diffx.scalatest.DiffShouldMatcher
import uk.gov.hmrc.pensionschemereturn.models.etmp._
import uk.gov.hmrc.pensionschemereturn.models.nonsipp.{MinimalRequiredSubmission, ReportDetails, SchemeDesignatory}
import uk.gov.hmrc.pensionschemereturn.models.etmp.nonsipp._
import uk.gov.hmrc.pensionschemereturn.transformations.Transformer
import com.softwaremill.diffx.generic.auto.diffForCaseClass
import org.scalatestplus.play.PlaySpec
import org.scalatestplus.mockito.MockitoSugar

import java.time.LocalDate

class MinimalRequiredDetailsToEtmpSpec extends PlaySpec with MockitoSugar with Transformer with DiffShouldMatcher {
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

      val expected = EtmpMinimalRequiredSubmission(
        EtmpReportDetails(
          pstr = None,
          psrStatus = Compiled,
          periodStart = today,
          periodEnd = today
        ),
        EtmpAccountingPeriodDetails(
          recordVersion = None,
          accountingPeriods = List(
            EtmpAccountingPeriod(
              accPeriodStart = today,
              accPeriodEnd = today
            )
          )
        ),
        EtmpSchemeDesignatory(
          recordVersion = None,
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

      transformation.transform(minimalRequiredSubmission) shouldMatchTo expected
    }

  }
}
