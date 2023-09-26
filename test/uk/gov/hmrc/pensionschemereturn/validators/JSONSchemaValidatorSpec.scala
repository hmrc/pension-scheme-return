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
import uk.gov.hmrc.pensionschemereturn.validators.SchemaPaths.EPID_1444

class JSONSchemaValidatorSpec extends AnyWordSpec with Matchers with JsonFileReader {

  private lazy val jsonPayloadSchemaValidator: JSONSchemaValidator = injector().instanceOf[JSONSchemaValidator]

  "validateJson" must {

    "Behaviour for valid payload for EPID 1444" in {
      val json = readJsonFromFile("/epid-1444-valid-example.json")
      val result = jsonPayloadSchemaValidator.validatePayload(EPID_1444, json)
      result.hasErrors mustBe false
    }

    "Behaviour for invalid payload with 3 invalid inputs for API 1828" in {
      val json = readJsonFromFile("/epid-1444-invalid-example.json")
      val result = jsonPayloadSchemaValidator.validatePayload(EPID_1444, json)
      result.hasErrors mustBe true
      val errorsAsString = result.errors.map(_.toString)

      errorsAsString.equals(
        Set(
          "$.reportDetails.psrStatus: does not have a value in the enumeration [Compiled, Submitted]",
          "$.reportDetails.periodStart: does not match the date pattern must be a valid RFC 3339 full-date",
          "$.reportDetails.periodEnd: does not match the date pattern must be a valid RFC 3339 full-date"
        )
      )
    }

    "Behaviour for valid payload for EPID 1444 loans only" in {
      val json = readJsonFromFile("/epid-1444-loans-only-valid-example.json")
      val result = jsonPayloadSchemaValidator.validatePayload(EPID_1444, json)
      result.hasErrors mustBe false
    }
  }
}
