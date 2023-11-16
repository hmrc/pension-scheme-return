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
import play.api.libs.json.{JsValue, Json}
import play.api.mvc.AnyContentAsEmpty
import play.api.test.FakeRequest
import uk.gov.hmrc.http.HeaderCarrier
import uk.gov.hmrc.pensionschemereturn.connectors.PsrConnector
import utils.{BaseSpec, TestValues}

import scala.concurrent.ExecutionContext.Implicits.global
import scala.concurrent.Future

class PsrOverviewServiceSpec extends BaseSpec with MockitoSugar with TestValues {

  override def beforeEach(): Unit =
    reset(mockPsrConnector)

  private val mockPsrConnector = mock[PsrConnector]

  private val service: PsrOverviewService = new PsrOverviewService(
    mockPsrConnector
  )

  private implicit val hc: HeaderCarrier = HeaderCarrier()
  private implicit val rq: FakeRequest[AnyContentAsEmpty.type] = FakeRequest()

  "getOverview" should {
    "return 200 without data when connector returns successfully" in {

      when(mockPsrConnector.getOverview(any(), any(), any())(any(), any()))
        .thenReturn(Future.successful(Seq.empty))

      whenReady(service.getOverview("testPstr", "2020-04-06", "2024-04-05")) { result: JsValue =>
        verify(mockPsrConnector, times(1)).getOverview(any(), any(), any())(any(), any())

        result mustBe Json.arr()
      }
    }
    "return 200 with data when connector returns successfully" in {

      when(mockPsrConnector.getOverview(any(), any(), any())(any(), any()))
        .thenReturn(Future.successful(sampleOverviewResponse))

      whenReady(service.getOverview("testPstr", "2020-04-06", "2024-04-05")) { result: JsValue =>
        verify(mockPsrConnector, times(1)).getOverview(any(), any(), any())(any(), any())

        result mustBe Json.toJson(sampleOverviewResponse)
      }
    }
  }
}
