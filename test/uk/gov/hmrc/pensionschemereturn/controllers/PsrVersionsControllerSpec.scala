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

import play.api.test.FakeRequest
import uk.gov.hmrc.pensionschemereturn.services.PsrVersionsService
import play.api.http.Status
import play.api.inject.bind
import uk.gov.hmrc.auth.core._
import uk.gov.hmrc.auth.core.retrieve.{~, Name}
import uk.gov.hmrc.http.HeaderCarrier
import org.mockito.ArgumentMatchers.any
import play.api.test.Helpers._
import utils.{BaseSpec, TestValues}
import play.api.inject.guice.{GuiceApplicationBuilder, GuiceableModule}
import play.api.Application
import play.api.libs.json.{JsObject, Json}

import scala.concurrent.Future

class PsrVersionsControllerSpec extends BaseSpec with TestValues {

  implicit val hc: HeaderCarrier = HeaderCarrier()
  private val fakeRequest = FakeRequest("GET", "/")
  private val mockPsrVersionsService = mock[PsrVersionsService]
  private val mockAuthConnector: AuthConnector = mock[AuthConnector]

  override def beforeEach(): Unit = {
    reset(mockAuthConnector)
    reset(mockPsrVersionsService)
  }

  val modules: Seq[GuiceableModule] =
    Seq(
      bind[PsrVersionsService].toInstance(mockPsrVersionsService),
      bind[AuthConnector].toInstance(mockAuthConnector)
    )

  val application: Application = new GuiceApplicationBuilder()
    .configure(conf = "auditing.enabled" -> false, "metrics.enabled" -> false, "metrics.jvm" -> false)
    .overrides(modules: _*)
    .build()

  private val controller = application.injector.instanceOf[PsrVersionsController]

  "Get Reporting Versions" must {

    "return 401 - Bearer token expired" in {

      when(mockAuthConnector.authorise[Option[String] ~ Enrolments ~ Option[Name]](any(), any())(any(), any()))
        .thenReturn(
          Future.failed(new BearerTokenExpired)
        )

      val thrown = intercept[AuthorisationException] {
        await(controller.getVersions("testPstr", "2020-04-06")(fakeRequest))
      }

      thrown.reason mustBe "Bearer token expired"

      verify(mockPsrVersionsService, never).getVersions(any(), any())(any(), any())
      verify(mockAuthConnector, times(1)).authorise(any(), any())(any(), any())
    }

    "return 401 - Bearer token not supplied" in {

      when(mockAuthConnector.authorise[Option[String] ~ Enrolments ~ Option[Name]](any(), any())(any(), any()))
        .thenReturn(
          Future.failed(new MissingBearerToken)
        )

      val thrown = intercept[AuthorisationException] {
        await(controller.getVersions("testPstr", "2020-04-06")(fakeRequest))
      }

      thrown.reason mustBe "Bearer token not supplied"
      verify(mockPsrVersionsService, never).getVersions(any(), any())(any(), any())
      verify(mockAuthConnector, times(1)).authorise(any(), any())(any(), any())
    }

    "return success" in {
      val responseJson: JsObject = Json.obj("mock" -> "pass")

      when(mockAuthConnector.authorise[Option[String] ~ Enrolments ~ Option[Name]](any(), any())(any(), any()))
        .thenReturn(
          Future.successful(new ~(new ~(Some(externalId), enrolments), Some(Name(Some("FirstName"), Some("lastName")))))
        )
      when(mockPsrVersionsService.getVersions(any(), any())(any(), any()))
        .thenReturn(Future.successful(responseJson))

      val result = controller.getVersions("testPstr", "2020-04-06")(fakeRequest)
      status(result) mustBe Status.OK
    }

    "return success when empty" in {
      when(mockAuthConnector.authorise[Option[String] ~ Enrolments ~ Option[Name]](any(), any())(any(), any()))
        .thenReturn(
          Future.successful(new ~(new ~(Some(externalId), enrolments), Some(Name(Some("FirstName"), Some("lastName")))))
        )
      when(mockPsrVersionsService.getVersions(any(), any())(any(), any()))
        .thenReturn(Future.successful(Json.arr()))

      val result = controller.getVersions("testPstr", "2020-04-06")(fakeRequest)
      status(result) mustBe Status.OK
    }
  }
}
