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
import uk.gov.hmrc.auth.core.retrieve.~
import uk.gov.hmrc.http.{BadRequestException, HeaderCarrier, UnauthorizedException}
import uk.gov.hmrc.pensionschemereturn.connectors.SchemeDetailsConnector
import org.mockito.ArgumentMatchers.{any, eq => eqTo}
import play.api.test.Helpers._
import uk.gov.hmrc.pensionschemereturn.config.AppConfig
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
  private val mockConfig: AppConfig = mock[AppConfig]
  private val mockSchemeDetailsConnector: SchemeDetailsConnector = mock[SchemeDetailsConnector]

  override def beforeEach(): Unit = {
    reset(mockAuthConnector)
    reset(mockPsrOverviewService)
    reset(mockSchemeDetailsConnector)
    reset(mockConfig)
  }

  val modules: Seq[GuiceableModule] =
    Seq(
      bind[PsrOverviewService].toInstance(mockPsrOverviewService),
      bind[AuthConnector].toInstance(mockAuthConnector),
      bind[SchemeDetailsConnector].toInstance(mockSchemeDetailsConnector),
      bind[AppConfig].toInstance(mockConfig)
    )

  val application: Application = new GuiceApplicationBuilder()
    .configure(conf = "auditing.enabled" -> false, "metrics.enabled" -> false, "metrics.jvm" -> false)
    .overrides(modules*)
    .build()

  private val controller = application.injector.instanceOf[PsrOverviewController]

  "Get Overview" must {

    "return 400 - Bad Request with missing parameter: srn" in {
      when(mockConfig.earliestPsrPeriodStartDate).thenReturn("2022-04-06")
      val thrown = intercept[BadRequestException] {
        await(controller.getOverview("testPstr", "2020-04-06", "2024-04-05")(fakeRequest))
      }

      thrown.message.trim mustBe "Bad Request with missing parameters: srn missing"

      verify(mockPsrOverviewService, never).getOverview(any(), any(), any())(any(), any())
      verify(mockAuthConnector, never).authorise(any(), any())(any(), any())
      verify(mockSchemeDetailsConnector, never).checkAssociation(any(), any(), any())(any(), any())
    }

    "return 400 - Bad Request with Invalid scheme reference number" in {
      when(mockConfig.earliestPsrPeriodStartDate).thenReturn("2022-04-06")

      val result =
        controller.getOverview("testPstr", "2020-04-06", "2024-04-05")(fakeRequest.withHeaders("srn" -> "INVALID_SRN"))
      status(result) mustBe Status.BAD_REQUEST
      contentAsString(result) mustBe "Invalid scheme reference number"

      verify(mockPsrOverviewService, never).getOverview(any(), any(), any())(any(), any())
      verify(mockAuthConnector, never).authorise(any(), any())(any(), any())
      verify(mockSchemeDetailsConnector, never).checkAssociation(any(), any(), any())(any(), any())
    }

    "return 401 - Bearer token expired" in {
      when(mockConfig.earliestPsrPeriodStartDate).thenReturn("2022-04-06")

      when(mockAuthConnector.authorise[Option[String] ~ Enrolments](any(), any())(any(), any()))
        .thenReturn(
          Future.failed(new BearerTokenExpired)
        )

      val thrown = intercept[AuthorisationException] {
        await(controller.getOverview("testPstr", "2020-04-06", "2024-04-05")(fakeRequest.withHeaders("srn" -> srn)))
      }

      thrown.reason mustBe "Bearer token expired"

      verify(mockPsrOverviewService, never).getOverview(any(), any(), any())(any(), any())
      verify(mockAuthConnector, times(1)).authorise(any(), any())(any(), any())
      verify(mockSchemeDetailsConnector, never).checkAssociation(any(), any(), any())(any(), any())
    }

    "return 401 - Bearer token not supplied" in {
      when(mockConfig.earliestPsrPeriodStartDate).thenReturn("2022-04-06")

      when(mockAuthConnector.authorise[Option[String] ~ Enrolments](any(), any())(any(), any()))
        .thenReturn(
          Future.failed(new MissingBearerToken)
        )

      val thrown = intercept[AuthorisationException] {
        await(controller.getOverview("testPstr", "2020-04-06", "2024-04-05")(fakeRequest.withHeaders("srn" -> srn)))
      }

      thrown.reason mustBe "Bearer token not supplied"
      verify(mockPsrOverviewService, never).getOverview(any(), any(), any())(any(), any())
      verify(mockAuthConnector, times(1)).authorise(any(), any())(any(), any())
      verify(mockSchemeDetailsConnector, never).checkAssociation(any(), any(), any())(any(), any())
    }

    "return 401 - Scheme is not associated with the user" in {
      when(mockConfig.earliestPsrPeriodStartDate).thenReturn("2022-04-06")
      when(mockAuthConnector.authorise[Option[String] ~ Enrolments](any(), any())(any(), any()))
        .thenReturn(
          Future.successful(new ~(Some(externalId), enrolments))
        )
      when(mockSchemeDetailsConnector.checkAssociation(any(), any(), any())(any(), any()))
        .thenReturn(Future.successful(false))

      val thrown = intercept[UnauthorizedException] {
        await(controller.getOverview("testPstr", "2020-04-06", "2024-04-05")(fakeRequest.withHeaders("srn" -> srn)))
      }

      thrown.message mustBe "Not Authorised - scheme is not associated with the user"
      verify(mockPsrOverviewService, never).getOverview(any(), any(), any())(any(), any())
      verify(mockAuthConnector, times(1)).authorise(any(), any())(any(), any())
      verify(mockSchemeDetailsConnector, times(1)).checkAssociation(any(), any(), any())(any(), any())
    }

    "return success" in {
      val responseJson: JsObject = Json.obj("mock" -> "pass")
      when(mockConfig.earliestPsrPeriodStartDate).thenReturn("2022-04-06")
      when(mockAuthConnector.authorise[Option[String] ~ Enrolments](any(), any())(any(), any()))
        .thenReturn(
          Future.successful(new ~(Some(externalId), enrolments))
        )
      when(mockPsrOverviewService.getOverview(any(), any(), any())(any(), any()))
        .thenReturn(Future.successful(responseJson))
      when(mockSchemeDetailsConnector.checkAssociation(any(), any(), any())(any(), any()))
        .thenReturn(Future.successful(true))

      val result = controller.getOverview("testPstr", "2020-04-06", "2024-04-05")(fakeRequest.withHeaders("srn" -> srn))
      status(result) mustBe Status.OK
      verify(mockPsrOverviewService, times(1)).getOverview(any(), any(), any())(any(), any())
      verify(mockAuthConnector, times(1)).authorise(any(), any())(any(), any())
      verify(mockSchemeDetailsConnector, times(1)).checkAssociation(any(), any(), any())(any(), any())
    }

    "return success when empty" in {
      when(mockConfig.earliestPsrPeriodStartDate).thenReturn("2021-04-06")
      when(mockAuthConnector.authorise[Option[String] ~ Enrolments](any(), any())(any(), any()))
        .thenReturn(
          Future.successful(new ~(Some(externalId), enrolments))
        )
      when(mockPsrOverviewService.getOverview(any(), any(), any())(any(), any()))
        .thenReturn(Future.successful(Json.arr()))
      when(mockSchemeDetailsConnector.checkAssociation(any(), any(), any())(any(), any()))
        .thenReturn(Future.successful(true))

      val result = controller.getOverview("testPstr", "2022-04-06", "2024-04-05")(fakeRequest.withHeaders("srn" -> srn))
      status(result) mustBe Status.OK
      verify(mockPsrOverviewService, times(1)).getOverview(any(), eqTo("2022-04-06"), eqTo("2024-04-05"))(any(), any())
      verify(mockAuthConnector, times(1)).authorise(any(), any())(any(), any())
      verify(mockSchemeDetailsConnector, times(1)).checkAssociation(any(), any(), any())(any(), any())
    }

    "limit calls to service by configured minimum date" in {
      when(mockConfig.earliestPsrPeriodStartDate).thenReturn("2021-04-06")
      when(mockAuthConnector.authorise[Option[String] ~ Enrolments](any(), any())(any(), any()))
        .thenReturn(
          Future.successful(new~(Some(externalId), enrolments))
        )
      when(mockPsrOverviewService.getOverview(any(), any(), any())(any(), any()))
        .thenReturn(Future.successful(Json.arr()))
      when(mockSchemeDetailsConnector.checkAssociation(any(), any(), any())(any(), any()))
        .thenReturn(Future.successful(true))

      val result = controller.getOverview("testPstr", "2018-04-06", "2024-04-05")(fakeRequest.withHeaders("srn" -> srn))
      status(result) mustBe Status.OK
      verify(mockPsrOverviewService, times(1)).getOverview(any(), eqTo("2021-04-06"), eqTo("2024-04-05"))(any(), any())
      verify(mockAuthConnector, times(1)).authorise(any(), any())(any(), any())
      verify(mockSchemeDetailsConnector, times(1)).checkAssociation(any(), any(), any())(any(), any())
    }
  }
}
