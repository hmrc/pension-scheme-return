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

package uk.gov.hmrc.pensionschemereturn.services

import org.mockito.ArgumentMatchers.any
import org.mockito.MockitoSugar
import org.scalatestplus.play.PlaySpec
import play.api.libs.json.Json
import play.api.test.FakeRequest
import uk.gov.hmrc.http.{HeaderCarrier, HttpResponse}
import uk.gov.hmrc.pensionschemereturn.connectors.PsrConnector
import uk.gov.hmrc.pensionschemereturn.models.{MinimalRequiredSubmission, ReportDetails, SchemeDesignatory}
import uk.gov.hmrc.pensionschemereturn.transformations.MinimalRequiredDetailsToEtmp
import uk.gov.hmrc.pensionschemereturn.validators.JSONSchemaValidator

import java.time.LocalDate
import scala.concurrent.ExecutionContext.Implicits.global
import scala.concurrent.Future

class PsrSubmissionServiceSpec extends PlaySpec with MockitoSugar {

  private val mockConnector = mock[PsrConnector]
  private val service =
    new PsrSubmissionService(mockConnector, new JSONSchemaValidator, new MinimalRequiredDetailsToEtmp)
  private implicit val hc = HeaderCarrier()
  private implicit val rq = FakeRequest()

  "PsrSubmissionService" should {
    "successfully proxy minimal required submission details" in {
      val expectedResponse = HttpResponse(200, Json.obj(), Map.empty)
      when(mockConnector.submitStandardPsr(any())(any(), any(), any())).thenReturn(Future.successful(expectedResponse))

      val details = MinimalRequiredSubmission(
        ReportDetails(
          "17836742CF",
          periodStart = LocalDate.of(2020, 12, 12),
          periodEnd = LocalDate.of(2021, 12, 12)
        ),
        List(LocalDate.of(2020, 12, 12) -> LocalDate.of(2021, 12, 12)),
        SchemeDesignatory(
          reasonForNoBankAccount = None,
          openBankAccount = true,
          activeMembers = 1,
          deferredMembers = 2,
          pensionerMembers = 3,
          totalPayments = 6
        )
      )

      service.submitMinimalRequiredDetails(details).map { result =>
        result mustEqual expectedResponse
      }

    }
  }
}
