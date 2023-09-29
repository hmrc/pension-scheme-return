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

import org.mockito.ArgumentMatchers.any
import org.mockito.MockitoSugar
import org.scalatest.BeforeAndAfterEach
import org.scalatestplus.play.PlaySpec
import uk.gov.hmrc.pensionschemereturn.models._
import uk.gov.hmrc.pensionschemereturn.models.requests.etmp._
import uk.gov.hmrc.pensionschemereturn.transformations.PsrSubmissionToEtmpSpec.{
  sampleLoansRequest,
  sampleMinimalRequiredSubmissionRequest
}

import java.time.LocalDate

class PsrSubmissionToEtmpSpec extends PlaySpec with MockitoSugar with Transformer with BeforeAndAfterEach {

  override protected def beforeEach(): Unit = {
    reset(mockMinimalRequiredDetailsToEtmp)
    reset(mockLoansToEtmp)
    super.beforeEach()
  }

  private val mockMinimalRequiredDetailsToEtmp: MinimalRequiredDetailsToEtmp = mock[MinimalRequiredDetailsToEtmp]
  private val mockLoansToEtmp: LoansToEtmp = mock[LoansToEtmp]
  private val transformation: PsrSubmissionToEtmp =
    new PsrSubmissionToEtmp(mockMinimalRequiredDetailsToEtmp, mockLoansToEtmp)

  "PsrSubmissionToEtmp" should {
    "PSR submission should successfully transform to etmp format with only MinimalRequiredDetails" in {

      when(mockMinimalRequiredDetailsToEtmp.transform(any())).thenReturn(sampleMinimalRequiredSubmissionRequest)

      val psrSubmission: PsrSubmission = PsrSubmission(
        minimalRequiredSubmission = mock[MinimalRequiredSubmission],
        checkReturnDates = false,
        loans = None
      )

      val expected = PsrSubmissionRequest(
        sampleMinimalRequiredSubmissionRequest.reportDetails,
        sampleMinimalRequiredSubmissionRequest.accountingPeriodDetails,
        sampleMinimalRequiredSubmissionRequest.schemeDesignatory,
        None
      )

      transformation.transform(psrSubmission) mustEqual expected
      verify(mockMinimalRequiredDetailsToEtmp, times(1)).transform(any())
      verify(mockLoansToEtmp, never).transform(any())
    }

    "PSR submission should successfully transform to etmp format" in {

      when(mockMinimalRequiredDetailsToEtmp.transform(any())).thenReturn(sampleMinimalRequiredSubmissionRequest)
      when(mockLoansToEtmp.transform(any())).thenReturn(sampleLoansRequest)

      val psrSubmission: PsrSubmission = PsrSubmission(
        minimalRequiredSubmission = mock[MinimalRequiredSubmission],
        checkReturnDates = false,
        loans = Some(mock[Loans])
      )

      val expected = PsrSubmissionRequest(
        sampleMinimalRequiredSubmissionRequest.reportDetails,
        sampleMinimalRequiredSubmissionRequest.accountingPeriodDetails,
        sampleMinimalRequiredSubmissionRequest.schemeDesignatory,
        Some(sampleLoansRequest)
      )

      transformation.transform(psrSubmission) mustEqual expected
      verify(mockMinimalRequiredDetailsToEtmp, times(1)).transform(any())
      verify(mockLoansToEtmp, times(1)).transform(any())
    }

  }
}

object PsrSubmissionToEtmpSpec extends Transformer {

  val today: LocalDate = LocalDate.now
  val sampleMinimalRequiredSubmissionRequest: MinimalRequiredSubmissionRequest = MinimalRequiredSubmissionRequest(
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

  val sampleLoansRequest: LoansRequest = LoansRequest(
    recordVersion = "001",
    schemeHadLoans = Yes,
    noOfLoans = 1,
    loanTransactions = List(
      LoanTransactionsRequest(
        dateOfLoan = today,
        loanRecipientName = "UKPartnershipName",
        recipientIdentityType = RecipientIdentityTypeRequest(
          indivOrOrgType = "03",
          idNumber = Some("1234567890"),
          reasonNoIdNumber = None,
          otherDescription = None
        ),
        recipientSponsoringEmployer = Yes,
        connectedPartyStatus = "02",
        loanAmount = Double.MaxValue,
        loanInterestAmount = Double.MaxValue,
        loanTotalSchemeAssets = Double.MaxValue,
        loanPeriodInMonths = Int.MaxValue,
        equalInstallments = No,
        loanInterestRate = Double.MaxValue,
        securityGiven = Yes,
        securityDetails = Some("SecurityGivenDetails"),
        capRepaymentCY = Double.MaxValue,
        intReceivedCY = Double.MaxValue,
        arrearsPrevYears = No,
        amountOfArrears = None,
        amountOutstanding = Double.MaxValue
      )
    )
  )
}
