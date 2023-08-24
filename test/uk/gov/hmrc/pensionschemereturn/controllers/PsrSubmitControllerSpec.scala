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
import play.api.libs.json.{JsObject, JsValue, Json}
import play.api.mvc.RequestHeader
import play.api.test.FakeRequest
import play.api.test.Helpers._
import uk.gov.hmrc.http.{HeaderCarrier, HttpResponse}
import uk.gov.hmrc.pensionschemereturn.base.SpecBase
import uk.gov.hmrc.pensionschemereturn.service.PsrSubmissionService

import scala.concurrent.Future

class PsrSubmitControllerSpec extends SpecBase with MockitoSugar with BeforeAndAfter {

  implicit val hc: HeaderCarrier = HeaderCarrier()
  private implicit lazy val rh: RequestHeader = FakeRequest("", "")
  private val fakeRequest = FakeRequest("POST", "/")
  private val mockPsrSubmissionService = mock[PsrSubmissionService]
  val modules: Seq[GuiceableModule] =
    Seq(
      bind[PsrSubmissionService].toInstance(mockPsrSubmissionService)
    )

  val application: Application = new GuiceApplicationBuilder()
    .configure(conf = "auditing.enabled" -> false, "metrics.enabled" -> false, "metrics.jvm" -> false)
    .overrides(modules: _*)
    .build()

  private val controller = application.injector.instanceOf[PsrSubmitController]

  "POST minimal required details" must {
    "return 204" in {
      val responseJson: JsObject = Json.obj("mock" -> "pass")

      when(mockPsrSubmissionService.submitMinimalRequiredDetails(any())(any(), any(), any()))
        .thenReturn(Future.successful(HttpResponse(OK, responseJson.toString)))

      val requestJson: JsValue = Json.parse(
        """{
          |
          |  "reportDetails" : {
          |    "pstr": "test-pstr",
          |    "periodStart": "2022-04-06",
          |    "periodEnd": "2023-04-05"
          |  },
          |  "accountingPeriods": [
          |    ["2022-04-06", "2023-04-05"]
          |  ],
          |  "schemeDesignatory": {
          |    "openBankAccount": true,
          |    "activeMembers": 1,
          |    "deferredMembers": 2,
          |    "pensionerMembers": 3,
          |    "totalPayments": 6
          |  }
          |}""".stripMargin
      )
      val postRequest = fakeRequest.withJsonBody(requestJson)
      val result = controller.submitMinimalRequiredDetails(postRequest)
      status(result) mustBe Status.NO_CONTENT
    }
  }

  "POST standard PSR" must {
    "return 204" in {
      val responseJson: JsObject = Json.obj("mock" -> "pass")

      when(mockPsrSubmissionService.submitStandardPsr(any())(any(), any(), any()))
        .thenReturn(Future.successful(HttpResponse(OK, responseJson.toString)))

      val requestJson: JsValue = Json.parse(
        """{
          |
          |  "reportDetails" : {
          |    "start" : "2022-04-06",
          |    "end" : "2023-04-05"
          |  }
          |}""".stripMargin
      )
      val postRequest = fakeRequest.withJsonBody(requestJson)
      val result = controller.submitStandardPsr(postRequest)
      status(result) mustBe Status.NO_CONTENT
    }
  }
}
