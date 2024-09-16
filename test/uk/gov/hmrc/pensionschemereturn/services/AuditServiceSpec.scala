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

package uk.gov.hmrc.pensionschemereturn.services

import play.api.test.FakeRequest
import org.scalatest.matchers.must.Matchers
import play.api.mvc.AnyContentAsEmpty
import uk.gov.hmrc.play.audit.http.connector.AuditResult.Success
import uk.gov.hmrc.play.audit.model.ExtendedDataEvent
import play.api.inject.bind
import uk.gov.hmrc.pensionschemereturn.models.Sent
import uk.gov.hmrc.play.audit.http.connector.AuditConnector
import org.scalatest.wordspec.AnyWordSpec
import org.mockito.ArgumentMatchers.any
import play.api.inject.guice.GuiceApplicationBuilder
import uk.gov.hmrc.pensionschemereturn.audit.EmailAuditEvent
import org.mockito.Mockito.{times, verify, when}
import org.scalatest.Inside
import play.api.libs.json.Json
import org.mockito.ArgumentCaptor
import org.scalatestplus.mockito.MockitoSugar

import scala.concurrent.Future
import scala.concurrent.ExecutionContext.Implicits.global

class AuditServiceSpec extends AnyWordSpec with Matchers with Inside {

  import AuditServiceSpec._

  "AuditServiceImpl" must {
    "construct and send the correct event" in {

      implicit val request: FakeRequest[AnyContentAsEmpty.type] = fakeRequest()

      val event = EmailAuditEvent(
        psaPspId = "A2500001",
        pstr = "test-pstr",
        submittedBy = "PSA",
        emailAddress = "test@test.com",
        event = Sent,
        requestId = "test-request-id",
        reportVersion = "001",
        schemeName = "Test Scheme",
        taxYear = "test tax year",
        userName = "test user"
      )

      val templateCaptor = ArgumentCaptor.forClass(classOf[ExtendedDataEvent])

      when(mockAuditConnector.sendExtendedEvent(any())(any(), any()))
        .thenReturn(Future.successful(Success))
      auditService().sendEvent(event)
      verify(mockAuditConnector, times(1)).sendExtendedEvent(templateCaptor.capture())(any(), any())
      inside(templateCaptor.getValue) { case ExtendedDataEvent(auditSource, auditType, _, _, detail, _, _, _) =>
        auditSource mustBe appName
        auditType mustBe event.auditType

        detail mustBe Json.obj(
          "emailInitiationRequestId" -> "test-request-id",
          "pensionSchemeTaxReference" -> "test-pstr",
          "schemeAdministratorName" -> "test user",
          "emailAddress" -> "test@test.com",
          "event" -> "Sent",
          "submittedBy" -> "PSA",
          "reportVersion" -> "001",
          "pensionSchemeAdministratorId" -> "A2500001",
          "schemeName" -> "Test Scheme",
          "taxYear" -> "test tax year"
        )
      }
    }
  }
}

object AuditServiceSpec extends MockitoSugar {

  private val mockAuditConnector: AuditConnector = mock[AuditConnector]

  private val app = new GuiceApplicationBuilder()
    .overrides(bind[AuditConnector].toInstance(mockAuditConnector))
    .build()

  def fakeRequest(): FakeRequest[AnyContentAsEmpty.type] = FakeRequest("", "")

  def auditService(): AuditService = app.injector.instanceOf[AuditService]

  def appName: String = app.configuration.underlying.getString("appName")

}
