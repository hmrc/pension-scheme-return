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
import org.mockito.MockitoSugar
import org.scalatest.concurrent.ScalaFutures
import org.scalatest.matchers.must.Matchers
import org.scalatest.wordspec.AnyWordSpec
import play.api.libs.json.{Json, Writes}
import play.api.mvc.{Action, AnyContent}
import play.api.mvc.Results.Ok
import play.api.http.Status.UNAUTHORIZED
import play.api.test.Helpers.{defaultAwaitTimeout, status}
import play.api.test.{FakeRequest, StubBodyParserFactory}
import play.mvc.Controller
import uk.gov.hmrc.auth.core.retrieve.~
import uk.gov.hmrc.auth.core.{AuthConnector, Enrolment, EnrolmentIdentifier, Enrolments, NoActiveSession}
import uk.gov.hmrc.pensionschemereturn.models.requests.IdentifierRequest.{AdministratorRequest, PractitionerRequest}

import scala.concurrent.{ExecutionContext, Future}

class IdentifierActionSpec extends AnyWordSpec with Matchers with MockitoSugar with StubBodyParserFactory with ScalaFutures {

  // User not signed in to GG - 401 //
  // User does not have an external id - 401
  // User does not have a PSA or PSP enrolment - 401
  // User has both PSA and PSP enrolment but noting in cache - 401
  // User has both PSA and PSP enrolment with PSA enrolment in cache - 200
  // User has both PSA and PSP enrolment with PSP enrolment in cache - 200
  // User has only PSA enrolment - 200
  // User has only PSP enrolment - 200

  lazy val authAction = new IdentifierAction(mockAuthConnector, stubBodyParser[AnyContent]())(ExecutionContext.global)

  lazy val handler =
    new Controller {
      def run: Action[AnyContent] = authAction { request =>
          request match {
            case AdministratorRequest(externalId, _, id) =>
              Ok(Json.obj("externalId" -> externalId, "psaId" -> id))
            case PractitionerRequest(externalId, _, id) =>
              Ok(Json.obj("externalId" -> externalId, "pspId" -> id))
          }
        }
      }

  val mockAuthConnector = mock[AuthConnector]

  def authResult(externalId: Option[String], enrolments: Enrolment*) = new ~(externalId, Enrolments(enrolments.toSet))

  val psaEnrolment = Enrolment("HMRC-PODS-ORG", Seq(EnrolmentIdentifier("PSAID", "A000000")), "Activated")
  val pspEnrolment = Enrolment("HMRC-PODSPP-ORG", Seq(EnrolmentIdentifier("PSPID", "A000001")), "Activated")

  "IdentifierAction" should {
    "return an unauthorised result" when {

      "User is not signed in to GG" in {

        when(mockAuthConnector.authorise(any(), any())(any(), any()))
          .thenThrow(new NoActiveSession("Not signed in") {})

        val result = handler.run(FakeRequest())

        status(result) mustBe UNAUTHORIZED
      }

      "User does not have an external id" in {

        when(mockAuthConnector.authorise[Option[String] ~ Enrolments](any(), any())(any(), any()))
          .thenReturn(Future.successful(authResult(None, psaEnrolment)))

        val result = handler.run(FakeRequest())

        status(result) mustBe UNAUTHORIZED
      }

      "User does not have a psa or psp enrolment" in {

        when(mockAuthConnector.authorise[Option[String] ~ Enrolments](any(), any())(any(), any()))
          .thenReturn(Future.successful(authResult(Some("externalId"))))

        val result = handler.run(FakeRequest())

        status(result) mustBe UNAUTHORIZED
      }
    }

    "return an IdentifierRequest" when {
      sealed trait A
      case object B extends A
      case object C extends A
      case object D extends A


      implicit val wA: Writes[A] = Writes[A] {
        implicit val wB = Json.writes[B.type]
        implicit val wC = Json.writes[C.type]
        implicit val wD = Json.writes[D.type]

        Json.writes[A]
      }

      println(Json.toJson(B: A).toString())
      println(Json.toJson(C: A).toString())
      println(Json.toJson(D: A).toString())

    }
  }







  // User has been deregistered - 401
  // User has deceased flag - 401
  // User has rls flag - 401
  // User is not associated with scheme - 401
  // Scheme does not have a valid status - 401
}