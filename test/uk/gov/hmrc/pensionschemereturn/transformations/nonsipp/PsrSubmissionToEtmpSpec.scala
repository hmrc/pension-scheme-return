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

import org.mockito.Mockito._
import uk.gov.hmrc.pensionschemereturn.models.nonsipp.assets.Assets
import uk.gov.hmrc.pensionschemereturn.models.nonsipp.shares.Shares
import uk.gov.hmrc.pensionschemereturn.base.EtmpTransformerSpec
import org.mockito.ArgumentMatchers.any
import uk.gov.hmrc.pensionschemereturn.models.nonsipp.memberpayments.MemberPayments
import uk.gov.hmrc.pensionschemereturn.models.nonsipp._
import com.softwaremill.diffx.generic.auto.indicator
import uk.gov.hmrc.pensionschemereturn.models.requests.PsrSubmissionEtmpRequest

class PsrSubmissionToEtmpSpec extends EtmpTransformerSpec {

  override protected def beforeEach(): Unit = {
    reset(mockMinimalRequiredDetailsToEtmp)
    reset(mockLoansToEtmp)
    reset(mockAssetsToEtmp)
    reset(mockMemberPaymentsTransformer)
    reset(mockSharesToEtmp)
    reset(mockPsrDeclarationToEtmp)
    super.beforeEach()
  }

  private val mockMinimalRequiredDetailsToEtmp: MinimalRequiredDetailsToEtmp = mock[MinimalRequiredDetailsToEtmp]
  private val mockLoansToEtmp: LoansToEtmp = mock[LoansToEtmp]
  private val mockAssetsToEtmp: AssetsToEtmp = mock[AssetsToEtmp]
  private val mockMemberPaymentsTransformer: MemberPaymentsTransformer = mock[MemberPaymentsTransformer]
  private val mockSharesToEtmp: SharesToEtmp = mock[SharesToEtmp]
  private val mockPsrDeclarationToEtmp: PsrDeclarationToEtmp = mock[PsrDeclarationToEtmp]

  private val transformation: PsrSubmissionToEtmp =
    new PsrSubmissionToEtmp(
      mockMinimalRequiredDetailsToEtmp,
      mockLoansToEtmp,
      mockAssetsToEtmp,
      mockMemberPaymentsTransformer,
      mockSharesToEtmp,
      mockPsrDeclarationToEtmp
    )

  "PsrSubmissionToEtmp" should {
    "PSR submission should successfully transform to etmp format with only MinimalRequiredDetails" in {

      when(mockMinimalRequiredDetailsToEtmp.transform(any(), any())).thenReturn(sampleEtmpMinimalRequiredSubmission)

      val psrSubmission: PsrSubmission = PsrSubmission(
        minimalRequiredSubmission = mock[MinimalRequiredSubmission],
        checkReturnDates = false,
        loans = None,
        assets = None,
        membersPayments = None,
        shares = None,
        psrDeclaration = None
      )

      val expected = PsrSubmissionEtmpRequest(
        sampleEtmpMinimalRequiredSubmission.reportDetails,
        sampleEtmpMinimalRequiredSubmission.accountingPeriodDetails,
        sampleEtmpMinimalRequiredSubmission.schemeDesignatory,
        loans = None,
        assets = None,
        membersPayments = None,
        shares = None,
        psrDeclaration = None
      )

      transformation.transform(psrSubmission) shouldMatchTo expected
      verify(mockMinimalRequiredDetailsToEtmp, times(1)).transform(any(), any())
      verify(mockLoansToEtmp, never).transform(any())
      verify(mockAssetsToEtmp, never).transform(any())
      verify(mockAssetsToEtmp, never).transform(any())
      verify(mockMemberPaymentsTransformer, never).toEtmp(any())
      verify(mockSharesToEtmp, never).transform(any())
      verify(mockPsrDeclarationToEtmp, never).transform(any())
    }

    "PSR submission should not set set etmpShares if shares are 'empty'" in {

      when(mockMinimalRequiredDetailsToEtmp.transform(any(), any())).thenReturn(sampleEtmpMinimalRequiredSubmission)

      val psrSubmission: PsrSubmission = PsrSubmission(
        minimalRequiredSubmission = mock[MinimalRequiredSubmission],
        checkReturnDates = false,
        loans = None,
        assets = None,
        membersPayments = None,
        shares = Some(Shares(None, None, None, None)),
        psrDeclaration = None
      )

      val expected = PsrSubmissionEtmpRequest(
        sampleEtmpMinimalRequiredSubmission.reportDetails,
        sampleEtmpMinimalRequiredSubmission.accountingPeriodDetails,
        sampleEtmpMinimalRequiredSubmission.schemeDesignatory,
        loans = None,
        assets = None,
        membersPayments = None,
        shares = None,
        psrDeclaration = None
      )

      transformation.transform(psrSubmission) shouldMatchTo expected
      verify(mockMinimalRequiredDetailsToEtmp, times(1)).transform(any(), any())
      verify(mockLoansToEtmp, never).transform(any())
      verify(mockAssetsToEtmp, never).transform(any())
      verify(mockAssetsToEtmp, never).transform(any())
      verify(mockMemberPaymentsTransformer, never).toEtmp(any())
      verify(mockSharesToEtmp, never).transform(any())
      verify(mockPsrDeclarationToEtmp, never).transform(any())
    }

    "PSR submission should successfully transform to etmp format" in {

      when(mockMinimalRequiredDetailsToEtmp.transform(any(), any())).thenReturn(sampleEtmpMinimalRequiredSubmission)
      when(mockLoansToEtmp.transform(any())).thenReturn(sampleEtmpLoans)
      when(mockAssetsToEtmp.transform(any())).thenReturn(sampleEtmpAssets)
      when(mockMemberPaymentsTransformer.toEtmp(any())).thenReturn(sampleEtmpMemberPayments)
      when(mockSharesToEtmp.transform(any())).thenReturn(sampleEtmpShares)
      when(mockPsrDeclarationToEtmp.transform(any())).thenReturn(sampleEtmpPsrDeclaration)

      val psrSubmission: PsrSubmission = PsrSubmission(
        minimalRequiredSubmission = mock[MinimalRequiredSubmission],
        checkReturnDates = false,
        loans = Some(mock[Loans]),
        assets = Some(mock[Assets]),
        membersPayments = Some(mock[MemberPayments]),
        shares = Some(mock[Shares]),
        psrDeclaration = Some(mock[PsrDeclaration])
      )

      val expected = PsrSubmissionEtmpRequest(
        reportDetails = sampleEtmpMinimalRequiredSubmission.reportDetails,
        accountingPeriodDetails = sampleEtmpMinimalRequiredSubmission.accountingPeriodDetails,
        schemeDesignatory = sampleEtmpMinimalRequiredSubmission.schemeDesignatory,
        loans = Some(sampleEtmpLoans),
        assets = Some(sampleEtmpAssets),
        membersPayments = Some(sampleEtmpMemberPayments),
        shares = Some(sampleEtmpShares),
        psrDeclaration = Some(sampleEtmpPsrDeclaration)
      )

      transformation.transform(psrSubmission) shouldMatchTo expected
      verify(mockMinimalRequiredDetailsToEtmp, times(1)).transform(any(), any())
      verify(mockLoansToEtmp, times(1)).transform(any())
      verify(mockAssetsToEtmp, times(1)).transform(any())
      verify(mockMemberPaymentsTransformer, times(1)).toEtmp(any())
      verify(mockSharesToEtmp, times(1)).transform(any())
      verify(mockPsrDeclarationToEtmp, times(1)).transform(any())
      verify(mockPsrDeclarationToEtmp, times(1)).transform(any())
    }
  }
}
