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
import play.api.Application
import play.api.http.Status
import play.api.inject.bind
import play.api.inject.guice.{GuiceApplicationBuilder, GuiceableModule}
import play.api.libs.json.{JsObject, JsValue, Json}
import play.api.test.FakeRequest
import play.api.test.Helpers._
import uk.gov.hmrc.auth.core._
import uk.gov.hmrc.auth.core.retrieve.{~, Name}
import uk.gov.hmrc.http.{HeaderCarrier, HttpResponse}
import uk.gov.hmrc.pensionschemereturn.services.SippPsrSubmissionService
import utils.{BaseSpec, TestValues}

import scala.concurrent.Future

class SippPsrSubmitControllerSpec extends BaseSpec with TestValues {

  implicit val hc: HeaderCarrier = HeaderCarrier()
  private val fakeRequest = FakeRequest("POST", "/")
  private val mockSippPsrSubmissionService = mock[SippPsrSubmissionService]
  private val mockAuthConnector: AuthConnector = mock[AuthConnector]

  override def beforeEach(): Unit = {
    reset(mockAuthConnector)
    reset(mockSippPsrSubmissionService)
  }

  val modules: Seq[GuiceableModule] =
    Seq(
      bind[SippPsrSubmissionService].toInstance(mockSippPsrSubmissionService),
      bind[AuthConnector].toInstance(mockAuthConnector)
    )

  val application: Application = new GuiceApplicationBuilder()
    .configure(conf = "auditing.enabled" -> false, "metrics.enabled" -> false, "metrics.jvm" -> false)
    .overrides(modules: _*)
    .build()

  private val controller = application.injector.instanceOf[SippPsrSubmitController]

  "POST SIPP PSR" must {

    "return 401 - Bearer token expired" in {

      when(mockAuthConnector.authorise[Option[String] ~ Enrolments ~ Option[Name]](any(), any())(any(), any()))
        .thenReturn(
          Future.failed(new BearerTokenExpired)
        )

      val thrown = intercept[AuthorisationException] {
        await(controller.submitSippPsr(fakeRequest))
      }

      thrown.reason mustBe "Bearer token expired"

      verify(mockSippPsrSubmissionService, never).submitSippPsr(any())(any(), any(), any())
      verify(mockAuthConnector, times(1)).authorise(any(), any())(any(), any())
    }

    "return 401 - Bearer token not supplied" in {

      when(mockAuthConnector.authorise[Option[String] ~ Enrolments ~ Option[Name]](any(), any())(any(), any()))
        .thenReturn(
          Future.failed(new MissingBearerToken)
        )

      val thrown = intercept[AuthorisationException] {
        await(controller.submitSippPsr(fakeRequest))
      }

      thrown.reason mustBe "Bearer token not supplied"
      verify(mockSippPsrSubmissionService, never).submitSippPsr(any())(any(), any(), any())
      verify(mockAuthConnector, times(1)).authorise(any(), any())(any(), any())
    }

    "return 204" in {
      val responseJson: JsObject = Json.obj("mock" -> "pass")

      when(mockAuthConnector.authorise[Option[String] ~ Enrolments ~ Option[Name]](any(), any())(any(), any()))
        .thenReturn(
          Future.successful(new ~(new ~(Some(externalId), enrolments), Some(Name(Some("FirstName"), Some("lastName")))))
        )
      when(mockSippPsrSubmissionService.submitSippPsr(any())(any(), any(), any()))
        .thenReturn(Future.successful(HttpResponse(OK, responseJson.toString)))

      val requestJson: JsValue = Json.parse(
        """
          |{
          |  "reportDetails" : {
          |    "pstr" : "17836742CF",
          |    "psrStatus" : "Compiled",
          |    "periodStart" : "2022-04-06",
          |    "periodEnd" : "2023-04-05",
          |    "memberTransactions": "Yes"
          |  }
          |}
          |""".stripMargin
      )
      val postRequest = fakeRequest.withJsonBody(requestJson)
      val result = controller.submitSippPsr(postRequest)
      status(result) mustBe Status.NO_CONTENT
    }
  }

  "GET SIPP PSR" must {
    "return 200" in {

      when(mockAuthConnector.authorise[Option[String] ~ Enrolments ~ Option[Name]](any(), any())(any(), any()))
        .thenReturn(
          Future.successful(new ~(new ~(Some(externalId), enrolments), Some(Name(Some("FirstName"), Some("lastName")))))
        )
      when(mockSippPsrSubmissionService.getSippPsr(any(), any(), any(), any())(any(), any(), any()))
        .thenReturn(Future.successful(Some(sampleSippPsrSubmission)))

      val result = controller.getSippPsr("testPstr", Some("fbNumber"), None, None)(fakeRequest)
      status(result) mustBe Status.OK
    }

    "return 404" in {

      when(mockAuthConnector.authorise[Option[String] ~ Enrolments ~ Option[Name]](any(), any())(any(), any()))
        .thenReturn(
          Future.successful(new ~(new ~(Some(externalId), enrolments), Some(Name(Some("FirstName"), Some("lastName")))))
        )
      when(mockSippPsrSubmissionService.getSippPsr(any(), any(), any(), any())(any(), any(), any()))
        .thenReturn(Future.successful(None))

      val result = controller.getSippPsr("testPstr", None, Some("periodStartDate"), Some("psrVersion"))(fakeRequest)
      status(result) mustBe Status.NOT_FOUND
    }
  }
}
