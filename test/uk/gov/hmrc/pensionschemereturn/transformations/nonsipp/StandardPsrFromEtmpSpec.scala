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

import uk.gov.hmrc.pensionschemereturn.models.response.{EtmpPsrDetails, EtmpSchemeDetails, PsrSubmissionEtmpResponse}
import com.softwaremill.diffx.scalatest.DiffShouldMatcher
import uk.gov.hmrc.pensionschemereturn.models.etmp.nonsipp._
import org.scalatest.BeforeAndAfterEach
import uk.gov.hmrc.pensionschemereturn.transformations.Transformer
import com.softwaremill.diffx.generic.AutoDerivation
import org.mockito.ArgumentMatchers.any
import utils.TestValues
import org.mockito.Mockito._
import uk.gov.hmrc.pensionschemereturn.models.etmp.nonsipp.assets.EtmpAssets
import uk.gov.hmrc.pensionschemereturn.models.etmp.nonsipp.memberpayments.EtmpMemberPayments
import org.scalatestplus.play.PlaySpec
import org.scalatestplus.mockito.MockitoSugar

class StandardPsrFromEtmpSpec
    extends PlaySpec
    with MockitoSugar
    with Transformer
    with BeforeAndAfterEach
    with DiffShouldMatcher
    with AutoDerivation
    with TestValues {

  override protected def beforeEach(): Unit = {
    reset(mockMinimalRequiredSubmissionFromEtmp)
    reset(mockLoansFromEtmp)
    reset(mockAssetsFromEtmp)
    reset(mockMemberPayments)
    reset(mockSharesFromEtmp)
    super.beforeEach()
  }

  val mockMinimalRequiredSubmissionFromEtmp: MinimalRequiredSubmissionFromEtmp = mock[MinimalRequiredSubmissionFromEtmp]
  val mockLoansFromEtmp: LoansFromEtmp = mock[LoansFromEtmp]
  val mockAssetsFromEtmp: AssetsFromEtmp = mock[AssetsFromEtmp]
  val mockMemberPayments: MemberPaymentsTransformer = mock[MemberPaymentsTransformer]
  val mockSharesFromEtmp: SharesFromEtmp = mock[SharesFromEtmp]

  private val transformation =
    new StandardPsrFromEtmp(
      mockMinimalRequiredSubmissionFromEtmp,
      mockLoansFromEtmp,
      mockAssetsFromEtmp,
      mockMemberPayments,
      mockSharesFromEtmp
    )

  "PSR submission should successfully transform to etmp format with only MinimalRequiredDetails when checkReturnDates is true" in {

    when(mockMinimalRequiredSubmissionFromEtmp.transform(any())).thenReturn(sampleMinimalRequiredSubmission)

    val psrSubmissionResponse = PsrSubmissionEtmpResponse(
      schemeDetails = mock[EtmpSchemeDetails],
      psrDetails = mock[EtmpPsrDetails],
      accountingPeriodDetails = mock[EtmpAccountingPeriodDetails],
      schemeDesignatory = mock[EtmpSchemeDesignatory],
      loans = None,
      assets = None,
      membersPayments = None,
      shares = None
    )

    transformation.transform(psrSubmissionResponse) shouldMatchTo Right(samplePsrSubmission)
    verify(mockMinimalRequiredSubmissionFromEtmp, times(1)).transform(any())
    verify(mockLoansFromEtmp, never).transform(any())
    verify(mockAssetsFromEtmp, never).transform(any())
    verify(mockMemberPayments, never).fromEtmp(any())
    verify(mockSharesFromEtmp, never).transform(any())
  }

  "PSR submission should successfully transform to etmp format with only MinimalRequiredDetails checkReturnDates is false" in {

    when(mockMinimalRequiredSubmissionFromEtmp.transform(any())).thenReturn(
      sampleMinimalRequiredSubmission
        .copy(reportDetails = sampleMinimalRequiredSubmission.reportDetails.copy(periodStart = sampleToday.plusDays(1)))
    )

    val psrSubmissionResponse = PsrSubmissionEtmpResponse(
      schemeDetails = mock[EtmpSchemeDetails],
      psrDetails = mock[EtmpPsrDetails],
      accountingPeriodDetails = mock[EtmpAccountingPeriodDetails],
      schemeDesignatory = mock[EtmpSchemeDesignatory],
      loans = None,
      assets = None,
      membersPayments = None,
      shares = None
    )

    val updatedReportDetails =
      samplePsrSubmission.minimalRequiredSubmission.reportDetails.copy(periodStart = sampleToday.plusDays(1))
    transformation.transform(psrSubmissionResponse) shouldMatchTo Right(
      samplePsrSubmission.copy(
        minimalRequiredSubmission =
          samplePsrSubmission.minimalRequiredSubmission.copy(reportDetails = updatedReportDetails),
        checkReturnDates = false
      )
    )
    verify(mockMinimalRequiredSubmissionFromEtmp, times(1)).transform(any())
    verify(mockLoansFromEtmp, never).transform(any())
    verify(mockAssetsFromEtmp, never).transform(any())
    verify(mockMemberPayments, never).fromEtmp(any())
    verify(mockSharesFromEtmp, never).transform(any())
  }

  "PSR submission should successfully transform to etmp format with Loans" in {

    when(mockMinimalRequiredSubmissionFromEtmp.transform(any())).thenReturn(sampleMinimalRequiredSubmission)
    when(mockLoansFromEtmp.transform(any())).thenReturn(sampleLoans)

    val psrSubmissionResponse = PsrSubmissionEtmpResponse(
      schemeDetails = mock[EtmpSchemeDetails],
      psrDetails = mock[EtmpPsrDetails],
      accountingPeriodDetails = mock[EtmpAccountingPeriodDetails],
      schemeDesignatory = mock[EtmpSchemeDesignatory],
      loans = Some(mock[EtmpLoans]),
      assets = None,
      membersPayments = None,
      shares = None
    )

    transformation.transform(psrSubmissionResponse) shouldMatchTo Right(
      samplePsrSubmission.copy(loans = Some(sampleLoans))
    )
    verify(mockMinimalRequiredSubmissionFromEtmp, times(1)).transform(any())
    verify(mockLoansFromEtmp, times(1)).transform(any())
    verify(mockAssetsFromEtmp, never).transform(any())
    verify(mockMemberPayments, never).fromEtmp(any())
    verify(mockSharesFromEtmp, never).transform(any())
  }

  "PSR submission should successfully transform to etmp format with Assets" in {

    when(mockMinimalRequiredSubmissionFromEtmp.transform(any())).thenReturn(sampleMinimalRequiredSubmission)
    when(mockAssetsFromEtmp.transform(any())).thenReturn(sampleAssets)

    val psrSubmissionResponse = PsrSubmissionEtmpResponse(
      schemeDetails = mock[EtmpSchemeDetails],
      psrDetails = mock[EtmpPsrDetails],
      accountingPeriodDetails = mock[EtmpAccountingPeriodDetails],
      schemeDesignatory = mock[EtmpSchemeDesignatory],
      loans = None,
      assets = Some(mock[EtmpAssets]),
      membersPayments = None,
      shares = None
    )

    transformation.transform(psrSubmissionResponse) shouldMatchTo Right(
      samplePsrSubmission.copy(assets = Some(sampleAssets))
    )
    verify(mockMinimalRequiredSubmissionFromEtmp, times(1)).transform(any())
    verify(mockLoansFromEtmp, never).transform(any())
    verify(mockAssetsFromEtmp, times(1)).transform(any())
    verify(mockMemberPayments, never).fromEtmp(any())
    verify(mockSharesFromEtmp, never).transform(any())
  }

  "PSR submission should successfully transform to etmp format with Member Payments" in {

    when(mockMinimalRequiredSubmissionFromEtmp.transform(any())).thenReturn(sampleMinimalRequiredSubmission)
    when(mockMemberPayments.fromEtmp(any())).thenReturn(Right(sampleMemberPayments))

    val psrSubmissionResponse = PsrSubmissionEtmpResponse(
      schemeDetails = mock[EtmpSchemeDetails],
      psrDetails = mock[EtmpPsrDetails],
      accountingPeriodDetails = mock[EtmpAccountingPeriodDetails],
      schemeDesignatory = mock[EtmpSchemeDesignatory],
      loans = None,
      assets = None,
      membersPayments = Some(mock[EtmpMemberPayments]),
      shares = None
    )

    transformation.transform(psrSubmissionResponse) shouldMatchTo Right(
      samplePsrSubmission.copy(membersPayments = Some(sampleMemberPayments))
    )

    verify(mockMinimalRequiredSubmissionFromEtmp, times(1)).transform(any())
    verify(mockLoansFromEtmp, never).transform(any())
    verify(mockAssetsFromEtmp, never).transform(any())
    verify(mockMemberPayments, times(1)).fromEtmp(any())
    verify(mockSharesFromEtmp, never).transform(any())
  }

  "PSR submission should successfully transform to etmp format with Shares" in {

    when(mockMinimalRequiredSubmissionFromEtmp.transform(any())).thenReturn(sampleMinimalRequiredSubmission)
    when(mockSharesFromEtmp.transform(any())).thenReturn(sampleShares)

    val psrSubmissionResponse = PsrSubmissionEtmpResponse(
      schemeDetails = mock[EtmpSchemeDetails],
      psrDetails = mock[EtmpPsrDetails],
      accountingPeriodDetails = mock[EtmpAccountingPeriodDetails],
      schemeDesignatory = mock[EtmpSchemeDesignatory],
      loans = None,
      assets = None,
      membersPayments = None,
      shares = Some(mock[EtmpShares])
    )

    transformation.transform(psrSubmissionResponse) shouldMatchTo Right(
      samplePsrSubmission.copy(shares = Some(sampleShares))
    )

    verify(mockMinimalRequiredSubmissionFromEtmp, times(1)).transform(any())
    verify(mockLoansFromEtmp, never).transform(any())
    verify(mockAssetsFromEtmp, never).transform(any())
    verify(mockMemberPayments, never).fromEtmp(any())
    verify(mockSharesFromEtmp, times(1)).transform(any())
  }
}
