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

package uk.gov.hmrc.pensionschemereturn.controllers

import play.api.test.FakeRequest
import uk.gov.hmrc.pensionschemereturn.services.AuditService
import uk.gov.hmrc.crypto.{ApplicationCrypto, PlainText}
import play.api.inject.bind
import uk.gov.hmrc.auth.core._
import uk.gov.hmrc.pensionschemereturn.models._
import org.mockito.ArgumentCaptor
import org.mockito.ArgumentMatchers.any
import play.api.test.Helpers._
import uk.gov.hmrc.pensionschemereturn.audit.EmailAuditEvent
import org.mockito.Mockito._
import utils.BaseSpec
import play.api.inject.guice.GuiceApplicationBuilder
import play.api.Application
import play.api.libs.json.Json

import scala.concurrent.Future

import java.time.LocalDateTime

class EmailResponseControllerSpec extends BaseSpec { // scalastyle:off magic.number

  import EmailResponseControllerSpec._

  private val mockAuditService = mock[AuditService]
  private val mockAuthConnector = mock[AuthConnector]

  private val application: Application = new GuiceApplicationBuilder()
    .configure(conf = "auditing.enabled" -> false, "metrics.enabled" -> false, "metrics.jvm" -> false)
    .overrides(
      Seq(
        bind[AuthConnector].toInstance(mockAuthConnector),
        bind[AuditService].toInstance(mockAuditService)
      )
    )
    .build()

  private val injector = application.injector
  private val controller = injector.instanceOf[EmailResponseController]
  private val encryptedPsaId =
    injector.instanceOf[ApplicationCrypto].QueryParameterCrypto.encrypt(PlainText(psaOrPspId)).value
  private val encryptedPstr = injector.instanceOf[ApplicationCrypto].QueryParameterCrypto.encrypt(PlainText(pstr)).value
  private val encryptedEmail =
    injector.instanceOf[ApplicationCrypto].QueryParameterCrypto.encrypt(PlainText(emailAddress)).value
  private val encryptedSchemeName =
    injector.instanceOf[ApplicationCrypto].QueryParameterCrypto.encrypt(PlainText(schemeName)).value
  private val encryptedUserName =
    injector.instanceOf[ApplicationCrypto].QueryParameterCrypto.encrypt(PlainText(userName)).value

  override def beforeEach(): Unit = {
    reset(mockAuditService)
    reset(mockAuthConnector)
    when(mockAuthConnector.authorise[Enrolments](any(), any())(any(), any()))
      .thenReturn(Future.successful(enrolments))
  }

  "EmailResponseController" must {

    "respond OK when given EmailEvents for PSA" in {
      val result = controller.sendAuditEvents(
        schemeAdministratorTypeAsPsa,
        requestId,
        encryptedEmail,
        encryptedPsaId,
        encryptedPstr,
        reportVersion,
        encryptedSchemeName,
        taxYear,
        encryptedUserName
      )(
        fakeRequest.withBody(Json.toJson(emailEvents))
      )

      status(result) mustBe OK
      verify(mockAuditService, times(4)).sendEvent(eventCaptor.capture())(any(), any())
      val expected = EmailAuditEvent(
        psaOrPspId,
        pstr,
        schemeAdministratorTypeAsPsa,
        emailAddress,
        Complained,
        requestId,
        reportVersion,
        schemeName,
        taxYear,
        userName
      )
      eventCaptor.getValue mustBe expected
    }

    "respond with BAD_REQUEST when not given EmailEvents" in {
      val result = controller.sendAuditEvents(
        schemeAdministratorTypeAsPsp,
        requestId,
        encryptedEmail,
        encryptedPsaId,
        encryptedPstr,
        reportVersion,
        encryptedSchemeName,
        taxYear,
        encryptedUserName
      )(
        fakeRequest.withBody(Json.obj("name" -> "invalid"))
      )

      verify(mockAuditService, never).sendEvent(any())(any(), any())
      status(result) mustBe BAD_REQUEST
    }
  }
}

object EmailResponseControllerSpec {
  private val psaOrPspId = "A7654321"
  private val schemeAdministratorTypeAsPsa = "PSA"
  private val schemeAdministratorTypeAsPsp = "PSP"
  private val emailAddress = "test@test.com"
  private val requestId = "test-request-id"
  private val reportVersion = "1"
  private val schemeName = "Test Scheme"
  private val userName = "User Name"
  private val taxYear = "Test tax year"

  private val fakeRequest = FakeRequest("", "")
  private val enrolments = Enrolments(
    Set(
      Enrolment(
        "HMRC-PODS-ORG",
        Seq(
          EnrolmentIdentifier("PSAID", "A0000000")
        ),
        "Activated",
        None
      )
    )
  )
  private val eventCaptor = ArgumentCaptor.forClass(classOf[EmailAuditEvent])
  private val emailEvents = EmailEvents(
    Seq(
      EmailEvent(Sent, LocalDateTime.now()),
      EmailEvent(Delivered, LocalDateTime.now()),
      EmailEvent(PermanentBounce, LocalDateTime.now()),
      EmailEvent(Opened, LocalDateTime.now()),
      EmailEvent(Complained, LocalDateTime.now())
    )
  )
}
