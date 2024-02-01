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
import play.api.libs.json.Json
import play.api.test.Helpers.baseApplicationBuilder.injector
import uk.gov.hmrc.pensionschemereturn.validators.SchemaPaths.API_1997
import utils.{SippEtmpDummyTestValues, SippEtmpTestValues}

import java.io.{BufferedWriter, FileWriter}

class JSONSchemaValidatorSippSpec extends AnyWordSpec with Matchers with JsonFileReader with SippEtmpTestValues with SippEtmpDummyTestValues {

  private lazy val jsonPayloadSchemaValidator: JSONSchemaValidator = injector().instanceOf[JSONSchemaValidator]

  "validateJson for SIPP" must {

    "Behaviour for valid payload for API 1997" in {
      val json = Json.toJson(fullSippPsrSubmissionEtmpRequestLong)

      val filePath = "/Users/tolgahmrc/Documents/Test-RequestPayload/test24.json"

      val writer = new BufferedWriter(new FileWriter(filePath))

      val jsonString: String = Json.stringify(json)
      writer.write(jsonString)
      writer.close()

      val result = jsonPayloadSchemaValidator.validatePayload(API_1997, json)
      result.hasErrors mustBe false
    }
  }
}
