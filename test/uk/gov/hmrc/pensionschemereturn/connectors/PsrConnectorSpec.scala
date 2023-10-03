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

package uk.gov.hmrc.pensionschemereturn.connectors

import com.github.tomakehurst.wiremock.client.WireMock
import com.github.tomakehurst.wiremock.client.WireMock._
import play.api.http.Status.{BAD_REQUEST, CREATED}
import play.api.inject.guice.GuiceApplicationBuilder
import play.api.libs.json.{JsObject, Json}
import play.api.mvc.RequestHeader
import play.api.test.FakeRequest
import play.api.test.Helpers.{await, defaultAwaitTimeout}
import uk.gov.hmrc.http.{BadRequestException, HeaderCarrier, HttpResponse}

import scala.concurrent.ExecutionContext.Implicits.global

class PsrConnectorSpec extends BaseConnectorSpec {

  implicit val hc: HeaderCarrier = HeaderCarrier()
  private implicit lazy val rh: RequestHeader = FakeRequest("", "")

  override lazy val applicationBuilder: GuiceApplicationBuilder =
    super.applicationBuilder.configure("microservice.services.if-hod.port" -> wireMockPort)

  private def createJsonObject(msg: String = "Sample Response"): JsObject =
    Json.obj(
      "msg" -> msg
    )

  private lazy val connector: PsrConnector = applicationBuilder.injector().instanceOf[PsrConnector]

  "submitStandardPsr" should {
    "return a standard PSR value with only fbNumber" in {
      stubPost(
        "/pension-online/psr/standard",
        Json.stringify(createJsonObject()),
        created()
      )

      whenReady(connector.submitStandardPsr(createJsonObject())) { result: HttpResponse =>
        WireMock.verify(
          postRequestedFor(urlEqualTo("/pension-online/psr/standard"))
        )

        result.status mustBe CREATED
      }
    }
  }

  "getStandardPsr" should {

    "return a standard PSR value with only fbNumber" in {

      stubGet(
        "/pension-online/psr/standard/testPstr?fbNumber=testFbNumber",
        ok(Json.stringify(createJsonObject()))
      )

      whenReady(connector.getStandardPsr("testPstr", Some("testFbNumber"), None, None)) { result: Option[JsObject] =>
        WireMock.verify(
          getRequestedFor(urlEqualTo("/pension-online/psr/standard/testPstr?fbNumber=testFbNumber"))
        )

        result mustBe Some(createJsonObject())
      }
    }

    "return a standard PSR value with periodStartDate and psrVersion" in {

      stubGet(
        "/pension-online/psr/standard/testPstr?periodStartDate=testPeriodStartDate&psrVersion=testPsrVersion",
        ok(Json.stringify(createJsonObject()))
      )

      whenReady(connector.getStandardPsr("testPstr", None, Some("testPeriodStartDate"), Some("testPsrVersion"))) {
        result: Option[JsObject] =>
          WireMock.verify(
            getRequestedFor(
              urlEqualTo(
                "/pension-online/psr/standard/testPstr?periodStartDate=testPeriodStartDate&psrVersion=testPsrVersion"
              )
            )
          )
          result mustBe Some(createJsonObject())
      }
    }

    "return 404 NotFound when pstr not found in etmp" in {

      stubGet(
        "/pension-online/psr/standard/notFoundTestPstr?periodStartDate=testPeriodStartDate&psrVersion=testPsrVersion",
        notFound()
      )

      whenReady(connector.getStandardPsr("notFoundTestPstr", None, Some("testPeriodStartDate"), Some("testPsrVersion"))) {
        result: Option[JsObject] =>
          WireMock.verify(
            getRequestedFor(
              urlEqualTo(
                "/pension-online/psr/standard/notFoundTestPstr?periodStartDate=testPeriodStartDate&psrVersion=testPsrVersion"
              )
            )
          )
          result mustBe None
      }
    }

    "return 400 BadRequest when etmp returns badRequest" in {

      stubGet(
        "/pension-online/psr/standard/invalidTestPstr?periodStartDate=testPeriodStartDate&psrVersion=testPsrVersion",
        badRequest().withBody("INVALID_PAYLOAD")
      )

      val thrown = intercept[BadRequestException] {
        await(connector.getStandardPsr("invalidTestPstr", None, Some("testPeriodStartDate"), Some("testPsrVersion")))
      }
      WireMock.verify(
        getRequestedFor(
          urlEqualTo(
            "/pension-online/psr/standard/invalidTestPstr?periodStartDate=testPeriodStartDate&psrVersion=testPsrVersion"
          )
        )
      )
      thrown.responseCode mustBe BAD_REQUEST
      thrown.message must include(s"Response body 'INVALID_PAYLOAD'")

    }

    "return 400 BadRequest when missing parameters" in {

      val thrown = intercept[BadRequestException] {
        await(connector.getStandardPsr("testPstr", None, None, None))
      }

      thrown.responseCode mustBe BAD_REQUEST
      thrown.message mustEqual "Missing url parameters"
    }
  }

}
