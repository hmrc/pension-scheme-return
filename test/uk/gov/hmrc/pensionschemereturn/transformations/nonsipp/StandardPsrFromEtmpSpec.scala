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
import uk.gov.hmrc.pensionschemereturn.models.etmp.nonsipp.{
  EtmpAccountingPeriodDetails,
  EtmpLoans,
  EtmpSchemeDesignatory
}
import uk.gov.hmrc.pensionschemereturn.models.response.{EtmpPsrDetails, EtmpSchemeDetails, PsrSubmissionEtmpResponse}
import uk.gov.hmrc.pensionschemereturn.transformations.Transformer
import utils.TestValues

class StandardPsrFromEtmpSpec
    extends PlaySpec
    with MockitoSugar
    with Transformer
    with BeforeAndAfterEach
    with TestValues {

  override protected def beforeEach(): Unit = {
    reset(mockMinimalRequiredSubmissionFromEtmp)
    reset(mockLoansFromEtmp)
    super.beforeEach()
  }

  val mockMinimalRequiredSubmissionFromEtmp: MinimalRequiredSubmissionFromEtmp = mock[MinimalRequiredSubmissionFromEtmp]
  val mockLoansFromEtmp: LoansFromEtmp = mock[LoansFromEtmp]

  private val transformation = new StandardPsrFromEtmp(mockMinimalRequiredSubmissionFromEtmp, mockLoansFromEtmp)

  "PSR submission should successfully transform to etmp format with only MinimalRequiredDetails when checkReturnDates is true" in {

    when(mockMinimalRequiredSubmissionFromEtmp.transform(any())).thenReturn(sampleMinimalRequiredSubmission)

    val psrSubmissionResponse = PsrSubmissionEtmpResponse(
      schemeDetails = mock[EtmpSchemeDetails],
      psrDetails = mock[EtmpPsrDetails],
      accountingPeriodDetails = mock[EtmpAccountingPeriodDetails],
      schemeDesignatory = mock[EtmpSchemeDesignatory],
      loans = None
    )

    transformation.transform(psrSubmissionResponse) mustEqual samplePsrSubmission
    verify(mockMinimalRequiredSubmissionFromEtmp, times(1)).transform(any())
    verify(mockLoansFromEtmp, never).transform(any())
  }

  "PSR submission should successfully transform to etmp format with only MinimalRequiredDetails checkReturnDates is false" in {

    when(mockMinimalRequiredSubmissionFromEtmp.transform(any())).thenReturn(
      sampleMinimalRequiredSubmission
        .copy(reportDetails = sampleMinimalRequiredSubmission.reportDetails.copy(periodStart = today.plusDays(1)))
    )

    val psrSubmissionResponse = PsrSubmissionEtmpResponse(
      schemeDetails = mock[EtmpSchemeDetails],
      psrDetails = mock[EtmpPsrDetails],
      accountingPeriodDetails = mock[EtmpAccountingPeriodDetails],
      schemeDesignatory = mock[EtmpSchemeDesignatory],
      loans = None
    )

    val updatedReportDetails =
      samplePsrSubmission.minimalRequiredSubmission.reportDetails.copy(periodStart = today.plusDays(1))
    transformation.transform(psrSubmissionResponse) mustEqual samplePsrSubmission.copy(
      minimalRequiredSubmission =
        samplePsrSubmission.minimalRequiredSubmission.copy(reportDetails = updatedReportDetails),
      checkReturnDates = false
    )
    verify(mockMinimalRequiredSubmissionFromEtmp, times(1)).transform(any())
    verify(mockLoansFromEtmp, never).transform(any())
  }

  "PSR submission should successfully transform to etmp format with Loans" in {

    when(mockMinimalRequiredSubmissionFromEtmp.transform(any())).thenReturn(sampleMinimalRequiredSubmission)
    when(mockLoansFromEtmp.transform(any())).thenReturn(sampleLoans)

    val psrSubmissionResponse = PsrSubmissionEtmpResponse(
      schemeDetails = mock[EtmpSchemeDetails],
      psrDetails = mock[EtmpPsrDetails],
      accountingPeriodDetails = mock[EtmpAccountingPeriodDetails],
      schemeDesignatory = mock[EtmpSchemeDesignatory],
      loans = Some(mock[EtmpLoans])
    )

    transformation.transform(psrSubmissionResponse) mustEqual samplePsrSubmission.copy(loans = Some(sampleLoans))
    verify(mockMinimalRequiredSubmissionFromEtmp, times(1)).transform(any())
    verify(mockLoansFromEtmp, times(1)).transform(any())
  }
}
