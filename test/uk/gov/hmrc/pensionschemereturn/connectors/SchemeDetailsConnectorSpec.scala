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

import com.github.tomakehurst.wiremock.client.ResponseDefinitionBuilder
import com.github.tomakehurst.wiremock.client.WireMock._
import com.github.tomakehurst.wiremock.stubbing.StubMapping
import org.scalatest.RecoverMethods.recoverToExceptionIf
import play.api.inject.guice.GuiceApplicationBuilder
import play.api.libs.json.Json
import play.api.test.Helpers.running
import uk.gov.hmrc.http.HeaderCarrier
import uk.gov.hmrc.pensionschemereturn.models.PensionSchemeId.{PsaId, PspId}
import uk.gov.hmrc.pensionschemereturn.models.SchemeId.Srn
import uk.gov.hmrc.pensionschemereturn.models.{PensionSchemeId, SchemeId}

import scala.concurrent.ExecutionContext.Implicits.global

class SchemeDetailsConnectorSpec extends BaseConnectorSpec {

  implicit val hc: HeaderCarrier = HeaderCarrier()

  override lazy val applicationBuilder: GuiceApplicationBuilder =
    super.applicationBuilder.configure("microservice.services.pensionsScheme.port" -> wireMockPort)

  object PsaSchemeDetailsHelper {
    val url = "/pensions-scheme/scheme"

    def stubGet(psaId: PsaId, schemeId: SchemeId, response: ResponseDefinitionBuilder): StubMapping =
      wireMockServer
        .stubFor(
          get(urlEqualTo(url))
            .withHeader("psaId", equalTo(psaId.value))
            .withHeader("idNumber", equalTo(schemeId.value))
            .withHeader("schemeIdType", equalTo(schemeId.idType))
            .willReturn(response)
        )
  }

  object PspSchemeDetailsHelper {
    val url = "/pensions-scheme/psp-scheme"

    def stubGet(pspId: PspId, srn: Srn, response: ResponseDefinitionBuilder): StubMapping =
      wireMockServer
        .stubFor(
          get(urlEqualTo(url))
            .withHeader("pspId", equalTo(pspId.value))
            .withHeader("srn", equalTo(srn.value))
            .willReturn(response)
        )
  }

  object CheckAssociationHelper {
    val url = "/pensions-scheme/is-psa-associated"

    def stubGet(pensionSchemeId: PensionSchemeId, srn: Srn, response: ResponseDefinitionBuilder): StubMapping = {

      val (idType, idValue) = extractHeader(pensionSchemeId)

      wireMockServer
        .stubFor(
          get(urlEqualTo(url))
            .withHeader(idType, equalTo(idValue))
            .withHeader("schemeReferenceNumber", equalTo(srn.value))
            .withHeader("Content-Type", equalTo("application/json"))
            .willReturn(response)
        )
    }

    private def extractHeader(pensionSchemeId: PensionSchemeId): (String, String) =
      pensionSchemeId match {
        case PsaId(value) => ("psaId", value)
        case PspId(value) => ("pspId", value)
      }
  }

  running(_ => applicationBuilder) { implicit app =>
    lazy val connector: SchemeDetailsConnector = injected[SchemeDetailsConnector]

    ".details for psa" should {

      val psaId = psaIdGen.sample.value
      val schemeId = schemeIdGen.sample.value
      val expectedResult = schemeDetailsGen.sample.value

      "return scheme details" in {

        PsaSchemeDetailsHelper.stubGet(psaId, schemeId, ok(Json.toJson(expectedResult).toString))

        connector.details(psaId, schemeId).map { result =>
          result mustBe Some(expectedResult)
        }
      }

      "return none when 404 is sent" in {

        PsaSchemeDetailsHelper.stubGet(psaId, schemeId, notFound)

        connector.details(psaId, schemeId).map { result =>
          result mustBe None
        }
      }

      "throw error" when {

        "400 response is sent" in {

          PsaSchemeDetailsHelper.stubGet(psaId, schemeId, badRequest)

          recoverToExceptionIf[Exception] {
            connector.details(psaId, schemeId)
          }
        }

        "500 response is sent" in {

          PsaSchemeDetailsHelper.stubGet(psaId, schemeId, serverError)

          assertThrows[Exception] {
            connector.details(psaId, schemeId).futureValue
          }
        }
      }
    }

    ".details for psp" should {
      val pspId = pspIdGen.sample.value
      val srn = srnGen.sample.value
      val expectedResult = schemeDetailsGen.sample.value

      "return scheme details" in {

        PspSchemeDetailsHelper.stubGet(pspId, srn, ok(Json.toJson(expectedResult).toString()))

        connector.details(pspId, srn).map { result =>
          result mustBe Some(expectedResult)
        }
      }

      "return none when 404 is sent" in {

        PspSchemeDetailsHelper.stubGet(pspId, srn, notFound)

        connector.details(pspId, srn).map { result =>
          result mustBe None
        }
      }

      "throw error" when {

        "400 response is sent" in {

          PspSchemeDetailsHelper.stubGet(pspId, srn, badRequest)

          recoverToExceptionIf[Exception] {
            connector.details(pspId, srn)
          }
        }

        "500 response is sent" in {

          PspSchemeDetailsHelper.stubGet(pspId, srn, serverError)

          recoverToExceptionIf[Exception] {
            connector.details(pspId, srn)
          }
        }
      }
    }

    ".checkAssociation for psa" should {

      val psaId = psaIdGen.sample.value
      val srn = srnGen.sample.value

      "return true if psa is associated" in {

        CheckAssociationHelper.stubGet(psaId, srn, ok("true"))

        connector.checkAssociation(psaId, srn).map { result =>
          result mustBe true
        }
      }

      "return false if psa is not associated" in {

        CheckAssociationHelper.stubGet(psaId, srn, ok("false"))

        connector.checkAssociation(psaId, srn).map { result =>
          result mustBe false
        }
      }

      "throw error" when {

        "404 response is sent" in {

          CheckAssociationHelper.stubGet(psaId, srn, notFound)

          recoverToExceptionIf[Exception] {
            connector.checkAssociation(psaId, srn)
          }
        }

        "500 response is sent" in {

          CheckAssociationHelper.stubGet(psaId, srn, serverError)

          recoverToExceptionIf[Exception] {
            connector.checkAssociation(psaId, srn)
          }
        }
      }
    }

    ".checkAssociation for psp" should {

      val pspId = pspIdGen.sample.value
      val srn = srnGen.sample.value

      "return true if psp is associated" in {

        CheckAssociationHelper.stubGet(pspId, srn, ok("true"))

        connector.checkAssociation(pspId, srn).map { result =>
          result mustBe true
        }
      }

      "return false if psp is not associated" in {

        CheckAssociationHelper.stubGet(pspId, srn, ok("false"))

        connector.checkAssociation(pspId, srn).map { result =>
          result mustBe false
        }
      }

      "throw error" when {

        "404 response is sent" in {

          CheckAssociationHelper.stubGet(pspId, srn, notFound)

          recoverToExceptionIf[Exception] {
            connector.checkAssociation(pspId, srn)
          }
        }

        "500 response is sent" in {

          CheckAssociationHelper.stubGet(pspId, srn, serverError)

          recoverToExceptionIf[Exception] {
            connector.checkAssociation(pspId, srn)
          }
        }
      }
    }
  }
}
