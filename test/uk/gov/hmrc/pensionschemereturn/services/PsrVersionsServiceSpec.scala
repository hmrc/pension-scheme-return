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

import play.api.test.FakeRequest
import utils.{BaseSpec, TestValues}
import play.api.mvc.AnyContentAsEmpty
import play.api.libs.json.{JsValue, Json}
import uk.gov.hmrc.http.HeaderCarrier
import uk.gov.hmrc.pensionschemereturn.connectors.PsrConnector
import org.mockito.MockitoSugar
import org.mockito.ArgumentMatchers.any

import scala.concurrent.Future
import scala.concurrent.ExecutionContext.Implicits.global

class PsrVersionsServiceSpec extends BaseSpec with MockitoSugar with TestValues {

  override def beforeEach(): Unit =
    reset(mockPsrConnector)

  private val mockPsrConnector = mock[PsrConnector]

  private val service: PsrVersionsService = new PsrVersionsService(
    mockPsrConnector
  )

  private implicit val hc: HeaderCarrier = HeaderCarrier()
  private implicit val rq: FakeRequest[AnyContentAsEmpty.type] = FakeRequest()

  "getVersions" should {
    "return 200 without data when connector returns successfully" in {

      when(mockPsrConnector.getVersions(any(), any())(any(), any()))
        .thenReturn(Future.successful(Seq.empty))

      whenReady(service.getVersions("testPstr", "2020-04-06")) { result: JsValue =>
        verify(mockPsrConnector, times(1)).getVersions(any(), any())(any(), any())

        result mustBe Json.arr()
      }
    }
    "return 200 with data when connector returns successfully" in {

      when(mockPsrConnector.getVersions(any(), any())(any(), any()))
        .thenReturn(Future.successful(sampleVersionsResponse))

      whenReady(service.getVersions("testPstr", "2020-04-06")) { result: JsValue =>
        verify(mockPsrConnector, times(1)).getVersions(any(), any())(any(), any())

        result mustBe Json.toJson(sampleVersionsResponse)
      }
    }
  }
}
