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
import uk.gov.hmrc.pensionschemereturn.base.EtmpTransformerSpec
import uk.gov.hmrc.pensionschemereturn.models.nonsipp._
import uk.gov.hmrc.pensionschemereturn.models.requests.etmp.PsrSubmissionEtmpRequest

class PsrSubmissionToEtmpSpec extends EtmpTransformerSpec {

  override protected def beforeEach(): Unit = {
    reset(mockMinimalRequiredDetailsToEtmp)
    reset(mockLoansToEtmp)
    reset(mockAssetsToEtmp)
    reset(mockMemberPaymentsTransformer)
    super.beforeEach()
  }

  private val mockMinimalRequiredDetailsToEtmp: MinimalRequiredDetailsToEtmp = mock[MinimalRequiredDetailsToEtmp]
  private val mockLoansToEtmp: LoansToEtmp = mock[LoansToEtmp]
  private val mockAssetsToEtmp: AssetsToEtmp = mock[AssetsToEtmp]
  private val mockMemberPaymentsTransformer = mock[MemberPaymentsTransformer]

  private val transformation: PsrSubmissionToEtmp =
    new PsrSubmissionToEtmp(
      mockMinimalRequiredDetailsToEtmp,
      mockLoansToEtmp,
      mockAssetsToEtmp,
      mockMemberPaymentsTransformer
    )

  "PsrSubmissionToEtmp" should {
    "PSR submission should successfully transform to etmp format with only MinimalRequiredDetails" in {

      when(mockMinimalRequiredDetailsToEtmp.transform(any())).thenReturn(sampleEtmpMinimalRequiredSubmission)

      val psrSubmission: PsrSubmission = PsrSubmission(
        minimalRequiredSubmission = mock[MinimalRequiredSubmission],
        checkReturnDates = false,
        loans = None,
        assets = None,
        membersPayments = None
      )

      val expected = PsrSubmissionEtmpRequest(
        sampleEtmpMinimalRequiredSubmission.reportDetails,
        sampleEtmpMinimalRequiredSubmission.accountingPeriodDetails,
        sampleEtmpMinimalRequiredSubmission.schemeDesignatory,
        loans = None,
        assets = None,
        membersPayments = None
      )

      transformation.transform(psrSubmission) shouldMatchTo expected
      verify(mockMinimalRequiredDetailsToEtmp, times(1)).transform(any())
      verify(mockLoansToEtmp, never).transform(any())
      verify(mockAssetsToEtmp, never).transform(any())
      verify(mockAssetsToEtmp, never).transform(any())
      verify(mockMemberPaymentsTransformer, never).toEtmp(any())
    }

    "PSR submission should successfully transform to etmp format" in {

      when(mockMinimalRequiredDetailsToEtmp.transform(any())).thenReturn(sampleEtmpMinimalRequiredSubmission)
      when(mockLoansToEtmp.transform(any())).thenReturn(sampleEtmpLoans)
      when(mockAssetsToEtmp.transform(any())).thenReturn(sampleEtmpAssets)
      when(mockMemberPaymentsTransformer.toEtmp(any())).thenReturn(sampleEtmpMemberPayments)

      val psrSubmission: PsrSubmission = PsrSubmission(
        minimalRequiredSubmission = mock[MinimalRequiredSubmission],
        checkReturnDates = false,
        loans = Some(mock[Loans]),
        assets = Some(mock[Assets]),
        membersPayments = Some(mock[MemberPayments])
      )

      val expected = PsrSubmissionEtmpRequest(
        reportDetails = sampleEtmpMinimalRequiredSubmission.reportDetails,
        accountingPeriodDetails = sampleEtmpMinimalRequiredSubmission.accountingPeriodDetails,
        schemeDesignatory = sampleEtmpMinimalRequiredSubmission.schemeDesignatory,
        loans = Some(sampleEtmpLoans),
        assets = Some(sampleEtmpAssets),
        membersPayments = Some(sampleEtmpMemberPayments)
      )

      transformation.transform(psrSubmission) shouldMatchTo expected
      verify(mockMinimalRequiredDetailsToEtmp, times(1)).transform(any())
      verify(mockLoansToEtmp, times(1)).transform(any())
      verify(mockAssetsToEtmp, times(1)).transform(any())
      verify(mockMemberPaymentsTransformer, times(1)).toEtmp(any())
    }
  }
}
