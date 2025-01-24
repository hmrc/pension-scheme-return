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
import uk.gov.hmrc.pensionschemereturn.services.PsrVersionsService
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

class PsrVersionsControllerSpec extends BaseSpec with TestValues {

  implicit val hc: HeaderCarrier = HeaderCarrier()
  private val fakeRequest = FakeRequest("GET", "/")
  private val mockPsrVersionsService = mock[PsrVersionsService]
  private val mockAuthConnector: AuthConnector = mock[AuthConnector]
  private val mockSchemeDetailsConnector: SchemeDetailsConnector = mock[SchemeDetailsConnector]
  private val mockConfig: AppConfig = mock[AppConfig]

  override def beforeEach(): Unit = {
    reset(mockAuthConnector)
    reset(mockPsrVersionsService)
    reset(mockSchemeDetailsConnector)
    reset(mockConfig)
  }

  val modules: Seq[GuiceableModule] =
    Seq(
      bind[PsrVersionsService].toInstance(mockPsrVersionsService),
      bind[AuthConnector].toInstance(mockAuthConnector),
      bind[SchemeDetailsConnector].toInstance(mockSchemeDetailsConnector),
      bind[AppConfig].toInstance(mockConfig)
    )

  val application: Application = new GuiceApplicationBuilder()
    .configure(conf = "auditing.enabled" -> false, "metrics.enabled" -> false, "metrics.jvm" -> false)
    .overrides(modules*)
    .build()

  private val controller = application.injector.instanceOf[PsrVersionsController]

  "Get Reporting Versions" must {

    "return 400 - Bad Request with missing parameter: srn" in {

      val thrown = intercept[BadRequestException] {
        await(controller.getVersions("testPstr", "2020-04-06")(fakeRequest))
      }

      thrown.message.trim mustBe "Bad Request with missing parameters: srn missing"

      verify(mockPsrVersionsService, never).getVersions(any(), any())(any(), any())
      verify(mockAuthConnector, never).authorise(any(), any())(any(), any())
      verify(mockSchemeDetailsConnector, never).checkAssociation(any(), any(), any())(any(), any())
    }

    "return 400 - Bad Request with Invalid scheme reference number" in {

      val result = controller.getVersions("testPstr", "2020-04-06")(fakeRequest.withHeaders("srn" -> "INVALID_SRN"))
      status(result) mustBe Status.BAD_REQUEST
      contentAsString(result) mustBe "Invalid scheme reference number"

      verify(mockPsrVersionsService, never).getVersions(any(), any())(any(), any())
      verify(mockAuthConnector, never).authorise(any(), any())(any(), any())
      verify(mockSchemeDetailsConnector, never).checkAssociation(any(), any(), any())(any(), any())
    }

    "return 401 - Bearer token expired" in {

      when(mockAuthConnector.authorise[Option[String] ~ Enrolments](any(), any())(any(), any()))
        .thenReturn(
          Future.failed(new BearerTokenExpired)
        )

      val thrown = intercept[AuthorisationException] {
        await(controller.getVersions("testPstr", "2020-04-06")(fakeRequest.withHeaders("srn" -> srn)))
      }

      thrown.reason mustBe "Bearer token expired"

      verify(mockPsrVersionsService, never).getVersions(any(), any())(any(), any())
      verify(mockAuthConnector, times(1)).authorise(any(), any())(any(), any())
      verify(mockSchemeDetailsConnector, never).checkAssociation(any(), any(), any())(any(), any())
    }

    "return 401 - Bearer token not supplied" in {

      when(mockAuthConnector.authorise[Option[String] ~ Enrolments](any(), any())(any(), any()))
        .thenReturn(
          Future.failed(new MissingBearerToken)
        )

      val thrown = intercept[AuthorisationException] {
        await(controller.getVersions("testPstr", "2020-04-06")(fakeRequest.withHeaders("srn" -> srn)))
      }

      thrown.reason mustBe "Bearer token not supplied"
      verify(mockPsrVersionsService, never).getVersions(any(), any())(any(), any())
      verify(mockAuthConnector, times(1)).authorise(any(), any())(any(), any())
      verify(mockSchemeDetailsConnector, never).checkAssociation(any(), any(), any())(any(), any())
    }

    "return 401 - Scheme is not associated with the user" in {
      when(mockAuthConnector.authorise[Option[String] ~ Enrolments](any(), any())(any(), any()))
        .thenReturn(
          Future.successful(new ~(Some(externalId), enrolments))
        )
      when(mockSchemeDetailsConnector.checkAssociation(any(), any(), any())(any(), any()))
        .thenReturn(Future.successful(false))

      val thrown = intercept[UnauthorizedException] {
        await(controller.getVersions("testPstr", "2020-04-06")(fakeRequest.withHeaders("srn" -> srn)))
      }

      thrown.message mustBe "Not Authorised - scheme is not associated with the user"
      verify(mockPsrVersionsService, never).getVersions(any(), any())(any(), any())
      verify(mockAuthConnector, times(1)).authorise(any(), any())(any(), any())
      verify(mockSchemeDetailsConnector, times(1)).checkAssociation(any(), any(), any())(any(), any())
    }

    "return success" in {
      val responseJson: JsObject = Json.obj("mock" -> "pass")

      when(mockAuthConnector.authorise[Option[String] ~ Enrolments](any(), any())(any(), any()))
        .thenReturn(
          Future.successful(new ~(Some(externalId), enrolments))
        )
      when(mockPsrVersionsService.getVersions(any(), any())(any(), any()))
        .thenReturn(Future.successful(responseJson))
      when(mockSchemeDetailsConnector.checkAssociation(any(), any(), any())(any(), any()))
        .thenReturn(Future.successful(true))

      val result = controller.getVersions("testPstr", "2020-04-06")(fakeRequest.withHeaders("srn" -> srn))
      status(result) mustBe Status.OK
      verify(mockPsrVersionsService, times(1)).getVersions(any(), any())(any(), any())
      verify(mockAuthConnector, times(1)).authorise(any(), any())(any(), any())
      verify(mockSchemeDetailsConnector, times(1)).checkAssociation(any(), any(), any())(any(), any())
    }

    "return success when empty" in {
      when(mockAuthConnector.authorise[Option[String] ~ Enrolments](any(), any())(any(), any()))
        .thenReturn(
          Future.successful(new ~(Some(externalId), enrolments))
        )
      when(mockPsrVersionsService.getVersions(any(), any())(any(), any()))
        .thenReturn(Future.successful(Json.arr()))
      when(mockSchemeDetailsConnector.checkAssociation(any(), any(), any())(any(), any()))
        .thenReturn(Future.successful(true))

      val result = controller.getVersions("testPstr", "2020-04-06")(fakeRequest.withHeaders("srn" -> srn))
      status(result) mustBe Status.OK
      verify(mockPsrVersionsService, times(1)).getVersions(any(), any())(any(), any())
      verify(mockAuthConnector, times(1)).authorise(any(), any())(any(), any())
      verify(mockSchemeDetailsConnector, times(1)).checkAssociation(any(), any(), any())(any(), any())
    }
  }

  "getVersionsForYears" must {
    "return success" in {
      val responseJson: JsObject = Json.obj("mock" -> "pass")
      when(mockConfig.earliestPsrPeriodStartDate).thenReturn("2021-04-06")

      when(mockAuthConnector.authorise[Option[String] ~ Enrolments](any(), any())(any(), any()))
        .thenReturn(Future.successful(new ~(Some(externalId), enrolments)))
      when(mockPsrVersionsService.getVersions(any(), any())(any(), any()))
        .thenReturn(Future.successful(responseJson))
      when(mockSchemeDetailsConnector.checkAssociation(any(), any(), any())(any(), any()))
        .thenReturn(Future.successful(true))

      val result = controller.getVersionsForYears("testPstr", List("2022-04-06", "2023-04-06"))(
        fakeRequest.withHeaders("srn" -> srn)
      )
      status(result) mustBe Status.OK
      verify(mockPsrVersionsService, times(1)).getVersions(any(), eqTo("2022-04-06"))(any(), any())
      verify(mockPsrVersionsService, times(1)).getVersions(any(), eqTo("2023-04-06"))(any(), any())
      verify(mockAuthConnector, times(1)).authorise(any(), any())(any(), any())
      verify(mockSchemeDetailsConnector, times(1)).checkAssociation(any(), any(), any())(any(), any())
    }

    "filter out dates before the earliest date in config" in {
      val responseJson: JsObject = Json.obj("mock" -> "pass")

      when(mockConfig.earliestPsrPeriodStartDate).thenReturn("2021-04-06")
      when(mockAuthConnector.authorise[Option[String] ~ Enrolments](any(), any())(any(), any()))
        .thenReturn(Future.successful(new ~(Some(externalId), enrolments)))
      when(mockPsrVersionsService.getVersions(any(), any())(any(), any()))
        .thenReturn(Future.successful(responseJson))
      when(mockSchemeDetailsConnector.checkAssociation(any(), any(), any())(any(), any()))
        .thenReturn(Future.successful(true))

      val result =
        controller.getVersionsForYears("testPstr", List("2019-04-06", "2020-04-06", "2021-04-06", "2022-04-06"))(
          fakeRequest.withHeaders("srn" -> srn)
        )
      status(result) mustBe Status.OK
      verify(mockPsrVersionsService, never).getVersions(any(), eqTo("2019-04-06"))(any(), any())
      verify(mockPsrVersionsService, never).getVersions(any(), eqTo("2020-04-06"))(any(), any())
      verify(mockPsrVersionsService, times(1)).getVersions(any(), eqTo("2021-04-06"))(any(), any())
      verify(mockPsrVersionsService, times(1)).getVersions(any(), eqTo("2022-04-06"))(any(), any())
      verify(mockAuthConnector, times(1)).authorise(any(), any())(any(), any())
      verify(mockSchemeDetailsConnector, times(1)).checkAssociation(any(), any(), any())(any(), any())
    }
  }
}
