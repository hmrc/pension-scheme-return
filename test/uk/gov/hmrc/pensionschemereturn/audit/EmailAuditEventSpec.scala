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

package uk.gov.hmrc.pensionschemereturn.audit

import org.scalatest.flatspec.AnyFlatSpec
import uk.gov.hmrc.pensionschemereturn.models.Sent
import org.scalatest.matchers.should.Matchers
import play.api.libs.json.{JsObject, Json}

class EmailAuditEventSpec extends AnyFlatSpec with Matchers {

  "EmailAuditEvent" should "output the correct map of data" in {

    val event = EmailAuditEvent(
      psaPspId = "A2500001",
      pstr = "pstr-test",
      submittedBy = "PSA",
      emailAddress = "test@test.com",
      event = Sent,
      requestId = "test-request-id",
      reportVersion = "001",
      schemeName = "Test Scheme",
      taxYear = "test tax year",
      userName = "Test User"
    )

    val expected: JsObject = Json.obj(
      "emailInitiationRequestId" -> "test-request-id",
      "pensionSchemeAdministratorId" -> "A2500001",
      "schemeAdministratorName" -> "Test User",
      "emailAddress" -> "test@test.com",
      "event" -> Sent.toString,
      "submittedBy" -> "PSA",
      "reportVersion" -> "001",
      "pensionSchemeTaxReference" -> "pstr-test",
      "schemeName" -> "Test Scheme",
      "taxYear" -> "test tax year"
    )

    event.auditType shouldBe "PensionSchemeReturnEmailEvent"
    event.details shouldBe expected
  }
}
