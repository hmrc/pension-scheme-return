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

package uk.gov.hmrc.pensionschemereturn.connectors

import com.github.tomakehurst.wiremock.client.{ResponseDefinitionBuilder, WireMock}
import com.github.tomakehurst.wiremock.client.WireMock._
import com.github.tomakehurst.wiremock.stubbing.StubMapping
import play.api.Application
import play.api.inject.bind
import play.api.inject.guice.{GuiceApplicationBuilder, GuiceableModule}
import play.api.libs.json.Json
import play.api.mvc.RequestHeader
import play.api.test.FakeRequest
import play.api.test.Helpers.await
import uk.gov.hmrc.http.{BadRequestException, HeaderCarrier}
import uk.gov.hmrc.pensionschemereturn.BaseConnectorSpec
import uk.gov.hmrc.pensionschemereturn.config.Constants
import uk.gov.hmrc.pensionschemereturn.models.Srn
import utils.TestValues
import play.api.test.Helpers.defaultAwaitTimeout

import scala.concurrent.ExecutionContext.Implicits.global

class SchemeDetailsConnectorSpec extends BaseConnectorSpec with TestValues {

  private implicit lazy val hc: HeaderCarrier = HeaderCarrier()

  val app: Application = new GuiceApplicationBuilder()
    .configure("microservice.services.pensionsScheme.port" -> wireMockPort)
    .build()

  object CheckAssociationHelper {
    val url = "/pensions-scheme/is-psa-associated"

    def stubGet(idValue: String, idType: String, srn: String, response: ResponseDefinitionBuilder): StubMapping =
      wireMockServer
        .stubFor(
          get(urlEqualTo(url))
            .withHeader(idType, equalTo(idValue))
            .withHeader("schemeReferenceNumber", equalTo(srn))
            .withHeader("Content-Type", equalTo("application/json"))
            .willReturn(response)
        )
  }

  private lazy val connector: SchemeDetailsConnector = app.injector.instanceOf[SchemeDetailsConnector]

  "checkAssociation" should {
    "return true if psa is associated" in {
      CheckAssociationHelper.stubGet(psaId, Constants.psaId, srn, ok("true"))

      whenReady(connector.checkAssociation(psaId, Constants.psaId, Srn(srn).get)) { result: Boolean =>
        WireMock.verify(
          getRequestedFor(urlEqualTo("/pensions-scheme/is-psa-associated"))
            .withHeader(Constants.psaId, equalTo(psaId))
            .withHeader("schemeReferenceNumber", equalTo(srn))
            .withHeader("Content-Type", equalTo("application/json"))
        )
        result shouldMatchTo true
      }
    }

    "return false if psa is not associated" in {
      CheckAssociationHelper.stubGet(psaId, Constants.psaId, srn, ok("false"))

      whenReady(connector.checkAssociation(psaId, Constants.psaId, Srn(srn).get)) { result: Boolean =>
        WireMock.verify(
          getRequestedFor(urlEqualTo("/pensions-scheme/is-psa-associated"))
            .withHeader(Constants.psaId, equalTo(psaId))
            .withHeader("schemeReferenceNumber", equalTo(srn))
            .withHeader("Content-Type", equalTo("application/json"))
        )
        result shouldMatchTo false
      }
    }

    "return true if psp is associated" in {
      CheckAssociationHelper.stubGet(pspId, Constants.pspId, srn, ok("true"))

      whenReady(connector.checkAssociation(pspId, Constants.pspId, Srn(srn).get)) { result: Boolean =>
        WireMock.verify(
          getRequestedFor(urlEqualTo("/pensions-scheme/is-psa-associated"))
            .withHeader(Constants.pspId, equalTo(pspId))
            .withHeader("schemeReferenceNumber", equalTo(srn))
            .withHeader("Content-Type", equalTo("application/json"))
        )
        result shouldMatchTo true
      }
    }

    "return false if psp is not associated" in {
      CheckAssociationHelper.stubGet(pspId, Constants.pspId, srn, ok("false"))

      whenReady(connector.checkAssociation(pspId, Constants.pspId, Srn(srn).get)) { result: Boolean =>
        WireMock.verify(
          getRequestedFor(urlEqualTo("/pensions-scheme/is-psa-associated"))
            .withHeader(Constants.pspId, equalTo(pspId))
            .withHeader("schemeReferenceNumber", equalTo(srn))
            .withHeader("Content-Type", equalTo("application/json"))
        )
        result shouldMatchTo false
      }
    }

    "throw error" in {
      CheckAssociationHelper.stubGet(pspId, Constants.pspId, srn, badRequest)
      val thrown = intercept[BadRequestException] {
        await(connector.checkAssociation(pspId, Constants.pspId, Srn(srn).get))
      }
      WireMock.verify(
        getRequestedFor(urlEqualTo("/pensions-scheme/is-psa-associated"))
      )
      thrown.responseCode shouldMatchTo 400
    }
  }
}
