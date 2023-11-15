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

package uk.gov.hmrc.pensionschemereturn.controllers

import org.mockito.ArgumentMatchers.any
import org.mockito.MockitoSugar
import org.scalatest.BeforeAndAfter
import play.api.Application
import play.api.http.Status
import play.api.inject.bind
import play.api.inject.guice.{GuiceApplicationBuilder, GuiceableModule}
import play.api.libs.json.{JsObject, Json}
import play.api.test.FakeRequest
import play.api.test.Helpers._
import uk.gov.hmrc.http.HeaderCarrier
import uk.gov.hmrc.pensionschemereturn.base.SpecBase
import uk.gov.hmrc.pensionschemereturn.services.PsrOverviewService
import utils.TestValues

import scala.concurrent.Future

class PsrOverviewControllerSpec extends SpecBase with MockitoSugar with BeforeAndAfter with TestValues {

  implicit val hc: HeaderCarrier = HeaderCarrier()
  private val fakeRequest = FakeRequest("GET", "/")
  private val mockPsrOverviewService = mock[PsrOverviewService]
  val modules: Seq[GuiceableModule] =
    Seq(
      bind[PsrOverviewService].toInstance(mockPsrOverviewService)
    )

  val application: Application = new GuiceApplicationBuilder()
    .configure(conf = "auditing.enabled" -> false, "metrics.enabled" -> false, "metrics.jvm" -> false)
    .overrides(modules: _*)
    .build()

  private val controller = application.injector.instanceOf[PsrOverviewController]

  "Get Overview" must {
    "return success" in {
      val responseJson: JsObject = Json.obj("mock" -> "pass")

      when(mockPsrOverviewService.getOverview(any(), any(), any())(any(), any()))
        .thenReturn(Future.successful(responseJson))

      val result = controller.getOverview("testPstr", "2020-04-06", "2024-04-05")(fakeRequest)
      status(result) mustBe Status.OK
    }

    "return success when empty" in {
      when(mockPsrOverviewService.getOverview(any(), any(), any())(any(), any()))
        .thenReturn(Future.successful(Json.arr()))

      val result = controller.getOverview("testPstr", "2020-04-06", "2024-04-05")(fakeRequest)
      status(result) mustBe Status.OK
    }
  }
}
