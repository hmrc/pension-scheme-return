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

package uk.gov.hmrc.pensionschemereturn.controllers.actions

import org.mockito.ArgumentMatchers._
import play.api.http.Status.{OK, UNAUTHORIZED}
import play.api.libs.json.Json
import play.api.mvc.Results.Ok
import play.api.mvc.{Action, AnyContent}
import play.api.test.Helpers.{contentAsJson, defaultAwaitTimeout, status}
import play.api.test.{FakeRequest, StubBodyParserFactory}
import uk.gov.hmrc.auth.core._
import uk.gov.hmrc.auth.core.retrieve.~
import uk.gov.hmrc.pensionschemereturn.config.Constants
import uk.gov.hmrc.pensionschemereturn.connectors.cache.SessionDataCacheConnector
import uk.gov.hmrc.pensionschemereturn.models.cache.PensionSchemeUser.{Administrator, Practitioner}
import uk.gov.hmrc.pensionschemereturn.models.cache.SessionData
import uk.gov.hmrc.pensionschemereturn.models.requests.IdentifierRequest.{AdministratorRequest, PractitionerRequest}
import utils.BaseSpec

import scala.concurrent.{ExecutionContext, Future}

class IdentifierActionSpec extends BaseSpec with StubBodyParserFactory {

  lazy val authAction =
    new IdentifierAction(
      mockAuthConnector,
      mockSessionDataCacheConnector,
      stubBodyParser[AnyContent]()
    )(ExecutionContext.global)

  class Handler {
    def run: Action[AnyContent] = authAction { request =>
      request match {
        case AdministratorRequest(externalId, _, id) =>
          Ok(Json.obj("externalId" -> externalId, "psaId" -> id.value))
        case PractitionerRequest(externalId, _, id) =>
          Ok(Json.obj("externalId" -> externalId, "pspId" -> id.value))
      }
    }
  }

  val handler = new Handler()

  val mockAuthConnector: AuthConnector = mock[AuthConnector]
  val mockSessionDataCacheConnector: SessionDataCacheConnector = mock[SessionDataCacheConnector]
  def authResult(externalId: Option[String], enrolments: Enrolment*) =
    new ~(externalId, Enrolments(enrolments.toSet))

  val psaEnrolment: Enrolment =
    Enrolment(Constants.psaEnrolmentKey, Seq(EnrolmentIdentifier(Constants.psaIdKey, "A000000")), "Activated")
  val pspEnrolment: Enrolment =
    Enrolment(Constants.pspEnrolmentKey, Seq(EnrolmentIdentifier(Constants.pspIdKey, "A000001")), "Activated")

  override def beforeEach: Unit = {
    reset(mockAuthConnector, mockSessionDataCacheConnector)
  }

  def setAuthValue(value: Option[String] ~ Enrolments): Unit =
    setAuthValue(Future.successful(value))

  def setAuthValue[A](value: Future[A]): Unit = {
    when(mockAuthConnector.authorise[A](any(), any())(any(), any()))
      .thenReturn(value)
  }

  def setSessionValue(value: Option[SessionData]): Unit =
    setSessionValue(Future.successful(value))

  def setSessionValue(value: Future[Option[SessionData]]): Unit = {
    when(mockSessionDataCacheConnector.fetch(any())(any(), any()))
      .thenReturn(value)
  }

  "IdentifierAction" should {
    "return an unauthorised result" when {

      "User is not signed in to GG" in {

        setAuthValue(Future.failed(new NoActiveSession("No user signed in") {}))

        val result = handler.run(FakeRequest())
        status(result) mustBe UNAUTHORIZED
      }

      "Authorise fails to match predicate" in {

        setAuthValue(Future.failed(new AuthorisationException("Authorise predicate fails") {}))

        val result = handler.run(FakeRequest())
        status(result) mustBe UNAUTHORIZED
      }

      "User does not have an external id" in {

        setAuthValue(authResult(None, psaEnrolment))

        val result = handler.run(FakeRequest())
        status(result) mustBe UNAUTHORIZED
      }

      "User does not have a psa or psp enrolment" in {

        setAuthValue(authResult(Some("externalId")))

        val result = handler.run(FakeRequest())
        status(result) mustBe UNAUTHORIZED
      }

      "User has both psa and psp enrolment but nothing is in the cache" in {

        setAuthValue(authResult(Some("externalId"), psaEnrolment, pspEnrolment))
        setSessionValue(None)

        val result = handler.run(FakeRequest())
        status(result) mustBe UNAUTHORIZED
      }
    }

    "return an IdentifierRequest" when {
      "User has a psa enrolment" in {

        setAuthValue(authResult(Some("externalId"), psaEnrolment))

        val result = handler.run(FakeRequest())

        status(result) mustBe OK
        (contentAsJson(result) \ "psaId").asOpt[String] mustBe Some("A000000")
        (contentAsJson(result) \ "pspId").asOpt[String] mustBe None
        (contentAsJson(result) \ "externalId").asOpt[String] mustBe Some("externalId")
      }

      "User has a psp enrolment" in {

        setAuthValue(authResult(Some("externalId"), pspEnrolment))

        val result = handler.run(FakeRequest())

        status(result) mustBe OK
        (contentAsJson(result) \ "psaId").asOpt[String] mustBe None
        (contentAsJson(result) \ "pspId").asOpt[String] mustBe Some("A000001")
        (contentAsJson(result) \ "externalId").asOpt[String] mustBe Some("externalId")
      }

      "User has a both psa and psp enrolment with admin stored in cache" in {

        setAuthValue(authResult(Some("externalId"), psaEnrolment, pspEnrolment))
        setSessionValue(Some(SessionData(Administrator)))

        val result = handler.run(FakeRequest())

        status(result) mustBe OK
        (contentAsJson(result) \ "psaId").asOpt[String] mustBe Some("A000000")
        (contentAsJson(result) \ "pspId").asOpt[String] mustBe None
        (contentAsJson(result) \ "externalId").asOpt[String] mustBe Some("externalId")
      }

      "User has a both psa and psp enrolment with practitioner stored in cache" in {

        setAuthValue(authResult(Some("externalId"), psaEnrolment, pspEnrolment))
        setSessionValue(Some(SessionData(Practitioner)))

        val result = handler.run(FakeRequest())

        status(result) mustBe OK
        (contentAsJson(result) \ "psaId").asOpt[String] mustBe None
        (contentAsJson(result) \ "pspId").asOpt[String] mustBe Some("A000001")
        (contentAsJson(result) \ "externalId").asOpt[String] mustBe Some("externalId")
      }
    }
  }
}