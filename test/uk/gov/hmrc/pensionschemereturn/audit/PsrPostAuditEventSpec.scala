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

package uk.gov.hmrc.pensionschemereturn.audit

import utils.BaseSpec
import play.api.libs.json.Json

class PsrPostAuditEventSpec extends BaseSpec {

  "PsrPostAuditEvent" should {

    val event = PsrPostAuditEvent(
      pstr = "test-pstr",
      credentialRole = "test-credential-role",
      psaPspId = "test-psaId",
      userName = "test-username",
      schemeName = "test-scheme-name",
      payload = Json.obj(
        "test-payload" -> "test-payload-value"
      ),
      status = Some(1),
      response = Some(
        Json.obj(
          "test-response" -> "test-response-value"
        )
      ),
      errorMessage = Some("test-error-message"),
      psrStatus = Some("test-psr-status")
    )

    "have the correct audit type" in {
      event.auditType mustEqual "PensionSchemeReturnPost"
    }

    "return the correct Json object when all fields are populated" in {
      val expected = Json.obj(
        "pensionSchemePractitionerId" -> "test-psaId",
        "schemePractitionerName" -> "test-username",
        "pensionSchemeTaxReference" -> "test-pstr",
        "schemeName" -> "test-scheme-name",
        "payload" -> Json.obj(
          "test-payload" -> "test-payload-value"
        ),
        "httpStatus" -> 1,
        "response" -> Json.obj(
          "test-response" -> "test-response-value"
        ),
        "errorMessage" -> "test-error-message",
        "psrStatus" -> "test-psr-status"
      )

      event.details mustEqual expected
    }

    "return the correct Json object when all optional fields are empty" in {
      val partialEvent = PsrPostAuditEvent(
        pstr = "test-pstr",
        credentialRole = "test-credential-role",
        psaPspId = "test-psaId",
        userName = "test-username",
        schemeName = "test-scheme-name",
        payload = Json.obj(
          "test-payload" -> "test-payload-value"
        ),
        status = None,
        response = None,
        errorMessage = None,
        psrStatus = None
      )

      val expected = Json.obj(
        "pensionSchemePractitionerId" -> "test-psaId",
        "schemePractitionerName" -> "test-username",
        "pensionSchemeTaxReference" -> "test-pstr",
        "schemeName" -> "test-scheme-name",
        "payload" -> Json.obj(
          "test-payload" -> "test-payload-value"
        )
      )

      partialEvent.details mustEqual expected
    }
  }
}
