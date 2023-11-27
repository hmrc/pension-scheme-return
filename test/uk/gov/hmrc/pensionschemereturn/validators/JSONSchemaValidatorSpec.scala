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

package uk.gov.hmrc.pensionschemereturn.validators

import org.scalatest.matchers.must.Matchers
import org.scalatest.wordspec.AnyWordSpec
import play.api.test.Helpers.baseApplicationBuilder.injector
import uk.gov.hmrc.pensionschemereturn.validators.SchemaPaths.API_1999

class JSONSchemaValidatorSpec extends AnyWordSpec with Matchers with JsonFileReader {

  private lazy val jsonPayloadSchemaValidator: JSONSchemaValidator = injector().instanceOf[JSONSchemaValidator]

  "validateJson" must {

    "Behaviour for valid payload for API 1999" in {
      val json = readJsonFromFile("/api-1999-valid-example.json")
      val result = jsonPayloadSchemaValidator.validatePayload(API_1999, json)
      result.hasErrors mustBe false
    }

    "Behaviour for invalid payload with 3 invalid inputs for API 1999" in {
      val json = readJsonFromFile("/api-1999-invalid-example.json")
      val result = jsonPayloadSchemaValidator.validatePayload(API_1999, json)
      result.hasErrors mustBe true
      val errorsAsString = result.errors.map(_.toString)

      errorsAsString.equals(
        Set(
          "$.reportDetails.psrStatus: does not have a value in the enumeration [Compiled, Submitted]",
          "$.reportDetails.periodStart: does not match the regex pattern ^(((19|20)([2468][048]|[13579][26]|0[48])|2000)[-]02[-]29|((19|20)[0-9]{2}[-](0[469]|11)[-](0[1-9]|1[0-9]|2[0-9]|30)|(19|20)[0-9]{2}[-](0[13578]|1[02])[-](0[1-9]|[12][0-9]|3[01])|(19|20)[0-9]{2}[-]02[-](0[1-9]|1[0-9]|2[0-8])))$",
          "$.reportDetails.periodEnd: does not match the regex pattern ^(((19|20)([2468][048]|[13579][26]|0[48])|2000)[-]02[-]29|((19|20)[0-9]{2}[-](0[469]|11)[-](0[1-9]|1[0-9]|2[0-9]|30)|(19|20)[0-9]{2}[-](0[13578]|1[02])[-](0[1-9]|[12][0-9]|3[01])|(19|20)[0-9]{2}[-]02[-](0[1-9]|1[0-9]|2[0-8])))$"
        )
      ) mustBe true
    }

    "Behaviour for valid payload for API 1999 loans only" in {
      val json = readJsonFromFile("/api-1999-loans-only-valid-example.json")
      val result = jsonPayloadSchemaValidator.validatePayload(API_1999, json)
      result.hasErrors mustBe false
    }

    "Behaviour for valid payload for API 1999 assets only" in {
      val json = readJsonFromFile("/api-1999-assets-only-valid-example.json")
      val result = jsonPayloadSchemaValidator.validatePayload(API_1999, json)
      result.hasErrors mustBe false
    }
  }
}
