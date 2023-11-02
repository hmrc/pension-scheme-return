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

import org.mockito.ArgumentMatchers.any
import org.mockito.MockitoSugar
import org.scalatest.BeforeAndAfterEach
import org.scalatestplus.play.PlaySpec
import uk.gov.hmrc.pensionschemereturn.models.etmp._
import uk.gov.hmrc.pensionschemereturn.models.etmp.nonsipp._
import uk.gov.hmrc.pensionschemereturn.models.nonsipp.{Assets, Loans, MinimalRequiredSubmission, PsrSubmission}
import uk.gov.hmrc.pensionschemereturn.models.requests.etmp.PsrSubmissionEtmpRequest
import uk.gov.hmrc.pensionschemereturn.transformations.Transformer
import uk.gov.hmrc.pensionschemereturn.transformations.nonsipp.PsrSubmissionToEtmpSpec.{
  sampleEtmpAssets,
  sampleEtmpLoans,
  sampleEtmpMinimalRequiredSubmission
}

import java.time.LocalDate

class PsrSubmissionToEtmpSpec extends PlaySpec with MockitoSugar with Transformer with BeforeAndAfterEach {

  override protected def beforeEach(): Unit = {
    reset(mockMinimalRequiredDetailsToEtmp)
    reset(mockLoansToEtmp)
    reset(mockAssetsToEtmp)
    super.beforeEach()
  }

  private val mockMinimalRequiredDetailsToEtmp: MinimalRequiredDetailsToEtmp = mock[MinimalRequiredDetailsToEtmp]
  private val mockLoansToEtmp: LoansToEtmp = mock[LoansToEtmp]
  private val mockAssetsToEtmp: AssetsToEtmp = mock[AssetsToEtmp]
  private val transformation: PsrSubmissionToEtmp =
    new PsrSubmissionToEtmp(mockMinimalRequiredDetailsToEtmp, mockLoansToEtmp, mockAssetsToEtmp)

  "PsrSubmissionToEtmp" should {
    "PSR submission should successfully transform to etmp format with only MinimalRequiredDetails" in {

      when(mockMinimalRequiredDetailsToEtmp.transform(any())).thenReturn(sampleEtmpMinimalRequiredSubmission)

      val psrSubmission: PsrSubmission = PsrSubmission(
        minimalRequiredSubmission = mock[MinimalRequiredSubmission],
        checkReturnDates = false,
        loans = None,
        assets = None
      )

      val expected = PsrSubmissionEtmpRequest(
        sampleEtmpMinimalRequiredSubmission.reportDetails,
        sampleEtmpMinimalRequiredSubmission.accountingPeriodDetails,
        sampleEtmpMinimalRequiredSubmission.schemeDesignatory,
        None,
        None
      )

      transformation.transform(psrSubmission) mustEqual expected
      verify(mockMinimalRequiredDetailsToEtmp, times(1)).transform(any())
      verify(mockLoansToEtmp, never).transform(any())
      verify(mockAssetsToEtmp, never).transform(any())
    }

    "PSR submission should successfully transform to etmp format" in {

      when(mockMinimalRequiredDetailsToEtmp.transform(any())).thenReturn(sampleEtmpMinimalRequiredSubmission)
      when(mockLoansToEtmp.transform(any())).thenReturn(sampleEtmpLoans)
      when(mockAssetsToEtmp.transform(any())).thenReturn(sampleEtmpAssets)

      val psrSubmission: PsrSubmission = PsrSubmission(
        minimalRequiredSubmission = mock[MinimalRequiredSubmission],
        checkReturnDates = false,
        loans = Some(mock[Loans]),
        assets = Some(mock[Assets])
      )

      val expected = PsrSubmissionEtmpRequest(
        sampleEtmpMinimalRequiredSubmission.reportDetails,
        sampleEtmpMinimalRequiredSubmission.accountingPeriodDetails,
        sampleEtmpMinimalRequiredSubmission.schemeDesignatory,
        Some(sampleEtmpLoans),
        Some(sampleEtmpAssets)
      )

      transformation.transform(psrSubmission) mustEqual expected
      verify(mockMinimalRequiredDetailsToEtmp, times(1)).transform(any())
      verify(mockLoansToEtmp, times(1)).transform(any())
      verify(mockAssetsToEtmp, times(1)).transform(any())
    }

  }
}

object PsrSubmissionToEtmpSpec extends Transformer {

  val today: LocalDate = LocalDate.now
  val sampleEtmpMinimalRequiredSubmission: EtmpMinimalRequiredSubmission = EtmpMinimalRequiredSubmission(
    EtmpReportDetails(
      pstr = "testPstr",
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
      recordVersion = Some("001"),
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

  val sampleEtmpLoans: EtmpLoans = EtmpLoans(
    recordVersion = Some("001"),
    schemeHadLoans = Yes,
    noOfLoans = 1,
    loanTransactions = List(
      EtmpLoanTransactions(
        dateOfLoan = today,
        loanRecipientName = "UKPartnershipName",
        recipientIdentityType = EtmpIdentityType(
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

  val sampleEtmpAssets: EtmpAssets = EtmpAssets(
    landOrProperty = EtmpLandOrProperty(
      recordVersion = None,
      heldAnyLandOrProperty = No,
      disposeAnyLandOrProperty = No,
      noOfTransactions = 0,
      landOrPropertyTransactions = Seq.empty
    ),
    borrowing = EtmpBorrowing(
      recordVersion = None,
      moneyWasBorrowed = "moneyWasBorrowed",
      noOfBorrows = 0,
      moneyBorrowed = Seq.empty
    ),
    bonds = EtmpBonds(bondsWereAdded = "bondsWereAdded", bondsWereDisposed = "bondsWereDisposed"),
    otherAssets =
      EtmpOtherAssets(otherAssetsWereHeld = "otherAssetsWereHeld", otherAssetsWereDisposed = "otherAssetsWereDisposed")
  )
}
