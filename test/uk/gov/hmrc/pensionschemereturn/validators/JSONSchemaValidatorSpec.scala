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

package uk.gov.hmrc.pensionschemereturn.validators

import uk.gov.hmrc.pensionschemereturn.validators.SchemaPaths.{API_1999, API_1999_optional}
import play.api.inject.guice.{GuiceApplicationBuilder, GuiceableModule}
import org.scalatest.matchers.must.Matchers
import play.api.inject.bind
import uk.gov.hmrc.auth.core.AuthConnector
import play.api.Application
import org.scalatest.wordspec.AnyWordSpec
import org.scalatestplus.mockito.MockitoSugar.mock

class JSONSchemaValidatorSpec extends AnyWordSpec with Matchers with JsonFileReader {

  private val mockAuthConnector = mock[AuthConnector]

  val modules: Seq[GuiceableModule] =
    Seq(
      bind[AuthConnector].toInstance(mockAuthConnector)
    )
  val app: Application = new GuiceApplicationBuilder()
    .overrides(modules*)
    .build()

  private lazy val jsonPayloadSchemaValidator: JSONSchemaValidator = app.injector.instanceOf[JSONSchemaValidator]

  "validate json payload against API 1999 schema" must {
    "Behaviour for valid payload for " in {
      val json = readJsonFromFile("/api-1999-valid-example.json")
      val result = jsonPayloadSchemaValidator.validatePayload(API_1999, json)
      result.hasErrors mustBe false
    }

    "Behaviour for invalid payload with 3 invalid inputs" in {
      val json = readJsonFromFile("/api-1999-invalid-example.json")
      val result = jsonPayloadSchemaValidator.validatePayload(API_1999, json)
      result.hasErrors mustBe true

      val actualErrors = result.errors.map(_.getMessage)

      val expectedErrors = Set(
        "$.reportDetails.psrStatus: does not have a value in the enumeration [\"Compiled\", \"Submitted\"]",
        "$.reportDetails.periodStart: does not match the regex pattern ^(((19|20)([2468][048]|[13579][26]|0[48])|2000)[-]02[-]29|((19|20)[0-9]{2}[-](0[469]|11)[-](0[1-9]|1[0-9]|2[0-9]|30)|(19|20)[0-9]{2}[-](0[13578]|1[02])[-](0[1-9]|[12][0-9]|3[01])|(19|20)[0-9]{2}[-]02[-](0[1-9]|1[0-9]|2[0-8])))$",
        "$.reportDetails.periodEnd: does not match the regex pattern ^(((19|20)([2468][048]|[13579][26]|0[48])|2000)[-]02[-]29|((19|20)[0-9]{2}[-](0[469]|11)[-](0[1-9]|1[0-9]|2[0-9]|30)|(19|20)[0-9]{2}[-](0[13578]|1[02])[-](0[1-9]|[12][0-9]|3[01])|(19|20)[0-9]{2}[-]02[-](0[1-9]|1[0-9]|2[0-8])))$"
      )

      actualErrors.equals(expectedErrors) mustBe true
    }

    "Behaviour for valid payload loans only" in {
      val json = readJsonFromFile("/api-1999-loans-only-valid-example.json")
      val result = jsonPayloadSchemaValidator.validatePayload(API_1999, json)
      result.hasErrors mustBe false
    }

    "Behaviour for valid payload land-or-property only" in {
      val json = readJsonFromFile("/api-1999-land-or-property-only-valid-example.json")
      val result = jsonPayloadSchemaValidator.validatePayload(API_1999, json)
      result.hasErrors mustBe false
    }

    "Behaviour for valid payload borrowing only" in {
      val json = readJsonFromFile("/api-1999-borrowing-only-valid-example.json")
      val result = jsonPayloadSchemaValidator.validatePayload(API_1999, json)
      result.hasErrors mustBe false
    }

    "Behaviour for valid payload bonds only" in {
      val json = readJsonFromFile("/api-1999-bonds-only-valid-example.json")
      val result = jsonPayloadSchemaValidator.validatePayload(API_1999, json)
      result.hasErrors mustBe false
    }

    "Behaviour for valid payload other assets only" in {
      val json = readJsonFromFile("/api-1999-other-assets-only-valid-example.json")
      val result = jsonPayloadSchemaValidator.validatePayload(API_1999, json)
      result.hasErrors mustBe false
    }

    "Behaviour for valid payload membersPayments only" in {
      val json = readJsonFromFile("/api-1999-membersPayments-only-valid-example.json")
      val result = jsonPayloadSchemaValidator.validatePayload(API_1999, json)
      result.hasErrors mustBe false
    }

    "Behaviour for valid payload shares only" in {
      val json = readJsonFromFile("/api-1999-shares-only-valid-example.json")
      val result = jsonPayloadSchemaValidator.validatePayload(API_1999, json)
      result.hasErrors mustBe false
    }

    "Behaviour for valid payload declaration only" in {
      val json = readJsonFromFile("/api-1999-declaration-only-valid-example.json")
      val result = jsonPayloadSchemaValidator.validatePayload(API_1999, json)
      result.hasErrors mustBe false
    }
  }
  "validate json payload against API 1999 schema with optional fields" must {

    "Behaviour for valid payload" in {
      val json = readJsonFromFile("/api-1999-valid-example-optional-fields.json")
      val result = jsonPayloadSchemaValidator.validatePayload(API_1999_optional, json)
      result.hasErrors mustBe false
    }

    "Behaviour for invalid payload with 3 invalid inputs" in {
      val json = readJsonFromFile("/api-1999-invalid-example-optional-fields.json")
      val result = jsonPayloadSchemaValidator.validatePayload(API_1999_optional, json)
      result.hasErrors mustBe true

      val actualErrors = result.errors.map(_.getMessage)

      val expectedErrors = Set(
        "$.reportDetails.psrStatus: does not have a value in the enumeration [\"Compiled\", \"Submitted\"]",
        "$.reportDetails.periodStart: does not match the date pattern must be a valid RFC 3339 full-date",
        "$.reportDetails.periodEnd: does not match the date pattern must be a valid RFC 3339 full-date"
      )
      actualErrors.equals(expectedErrors) mustBe true
    }

    "Behaviour for valid payload shares only" in {
      val json = readJsonFromFile("/api-1999-shares-only-valid-example-optional-fields.json")
      val result = jsonPayloadSchemaValidator.validatePayload(API_1999_optional, json)
      result.hasErrors mustBe false
    }

    "Behaviour for valid payload land-or-property only" in {
      val json = readJsonFromFile("/api-1999-land-or-property-only-valid-example-optional-fields.json")
      val result = jsonPayloadSchemaValidator.validatePayload(API_1999_optional, json)
      result.hasErrors mustBe false
    }

    "Behaviour for valid payload loans only" in {
      val json = readJsonFromFile("/api-1999-loans-only-valid-example-optional-fields.json")
      val result = jsonPayloadSchemaValidator.validatePayload(API_1999_optional, json)
      result.hasErrors mustBe false
    }

    "Behaviour for valid payload bonds only" in {
      val json = readJsonFromFile("/api-1999-bonds-only-valid-example-optional-fields.json")
      val result = jsonPayloadSchemaValidator.validatePayload(API_1999_optional, json)
      result.hasErrors mustBe false
    }

    "Behaviour for valid payload other assets only" in {
      val json = readJsonFromFile("/api-1999-other-assets-only-valid-example-optional-fields.json")
      val result = jsonPayloadSchemaValidator.validatePayload(API_1999_optional, json)
      result.hasErrors mustBe false
    }

  }
}
