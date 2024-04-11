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

package uk.gov.hmrc.pensionschemereturn.controllers

import play.api.test.FakeRequest
import uk.gov.hmrc.pensionschemereturn.services.PsrOverviewService
import play.api.http.Status
import play.api.inject.bind
import uk.gov.hmrc.auth.core._
import uk.gov.hmrc.auth.core.retrieve.{~, Name}
import uk.gov.hmrc.http.HeaderCarrier
import org.mockito.ArgumentMatchers.any
import play.api.test.Helpers._
import org.mockito.Mockito._
import utils.{BaseSpec, TestValues}
import play.api.inject.guice.{GuiceApplicationBuilder, GuiceableModule}
import play.api.Application
import play.api.libs.json.{JsObject, Json}

import scala.concurrent.Future

class PsrOverviewControllerSpec extends BaseSpec with TestValues {

  implicit val hc: HeaderCarrier = HeaderCarrier()
  private val fakeRequest = FakeRequest("GET", "/")

  private val mockPsrOverviewService = mock[PsrOverviewService]
  private val mockAuthConnector: AuthConnector = mock[AuthConnector]

  override def beforeEach(): Unit = {
    reset(mockAuthConnector)
    reset(mockPsrOverviewService)
  }

  val modules: Seq[GuiceableModule] =
    Seq(
      bind[PsrOverviewService].toInstance(mockPsrOverviewService),
      bind[AuthConnector].toInstance(mockAuthConnector)
    )

  val application: Application = new GuiceApplicationBuilder()
    .configure(conf = "auditing.enabled" -> false, "metrics.enabled" -> false, "metrics.jvm" -> false)
    .overrides(modules: _*)
    .build()

  private val controller = application.injector.instanceOf[PsrOverviewController]

  "Get Overview" must {

    "return 401 - Bearer token expired" in {

      when(mockAuthConnector.authorise[Option[String] ~ Enrolments ~ Option[Name]](any(), any())(any(), any()))
        .thenReturn(
          Future.failed(new BearerTokenExpired)
        )

      val thrown = intercept[AuthorisationException] {
        await(controller.getOverview("testPstr", "2020-04-06", "2024-04-05")(fakeRequest))
      }

      thrown.reason mustBe "Bearer token expired"

      verify(mockPsrOverviewService, never).getOverview(any(), any(), any())(any(), any())
      verify(mockAuthConnector, times(1)).authorise(any(), any())(any(), any())
    }

    "return 401 - Bearer token not supplied" in {

      when(mockAuthConnector.authorise[Option[String] ~ Enrolments ~ Option[Name]](any(), any())(any(), any()))
        .thenReturn(
          Future.failed(new MissingBearerToken)
        )

      val thrown = intercept[AuthorisationException] {
        await(controller.getOverview("testPstr", "2020-04-06", "2024-04-05")(fakeRequest))
      }

      thrown.reason mustBe "Bearer token not supplied"
      verify(mockPsrOverviewService, never).getOverview(any(), any(), any())(any(), any())
      verify(mockAuthConnector, times(1)).authorise(any(), any())(any(), any())
    }

    "return success" in {
      val responseJson: JsObject = Json.obj("mock" -> "pass")
      when(mockAuthConnector.authorise[Option[String] ~ Enrolments ~ Option[Name]](any(), any())(any(), any()))
        .thenReturn(
          Future.successful(new ~(new ~(Some(externalId), enrolments), Some(Name(Some("FirstName"), Some("lastName")))))
        )
      when(mockPsrOverviewService.getOverview(any(), any(), any())(any(), any()))
        .thenReturn(Future.successful(responseJson))

      val result = controller.getOverview("testPstr", "2020-04-06", "2024-04-05")(fakeRequest)
      status(result) mustBe Status.OK
      verify(mockPsrOverviewService, times(1)).getOverview(any(), any(), any())(any(), any())
      verify(mockAuthConnector, times(1)).authorise(any(), any())(any(), any())
    }

    "return success when empty" in {
      when(mockAuthConnector.authorise[Option[String] ~ Enrolments ~ Option[Name]](any(), any())(any(), any()))
        .thenReturn(
          Future.successful(new ~(new ~(Some(externalId), enrolments), Some(Name(Some("FirstName"), Some("lastName")))))
        )
      when(mockPsrOverviewService.getOverview(any(), any(), any())(any(), any()))
        .thenReturn(Future.successful(Json.arr()))

      val result = controller.getOverview("testPstr", "2020-04-06", "2024-04-05")(fakeRequest)
      status(result) mustBe Status.OK
      verify(mockPsrOverviewService, times(1)).getOverview(any(), any(), any())(any(), any())
      verify(mockAuthConnector, times(1)).authorise(any(), any())(any(), any())
    }
  }
}
