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

package uk.gov.hmrc.pensionschemereturn.auth

import play.api.test.FakeRequest
import play.api.mvc.{AnyContentAsEmpty, Result}
import uk.gov.hmrc.pensionschemereturn.config.Constants._
import uk.gov.hmrc.auth.core._
import uk.gov.hmrc.auth.core.retrieve.~
import uk.gov.hmrc.http.{BadRequestException, HeaderCarrier}
import uk.gov.hmrc.play.http.HeaderCarrierConverter
import play.api.mvc.Results.Ok
import org.mockito.ArgumentMatchers.any
import utils.BaseSpec
import org.mockito.Mockito.{reset, when}

import scala.concurrent.{Await, Future}
import scala.concurrent.ExecutionContext.Implicits.global
import scala.concurrent.duration.Duration

class PsrAuthSpec extends BaseSpec {

  private val mockAuthConnector = mock[AuthConnector]

  override protected def beforeEach(): Unit =
    reset(mockAuthConnector)

  private val psaEnrolment = Enrolments(
    Set(
      Enrolment(
        psaEnrolmentKey,
        Seq(
          EnrolmentIdentifier(psaIdKey, "A0000000")
        ),
        "Activated",
        None
      )
    )
  )

  private val pspEnrolment = Enrolments(
    Set(
      Enrolment(
        pspEnrolmentKey,
        Seq(
          EnrolmentIdentifier(pspIdKey, "A0000000")
        ),
        "Activated",
        None
      )
    )
  )

  private val unknownEnrolment = Enrolments(
    Set(
      Enrolment(
        "unknownEnrolmentId",
        Seq(
          EnrolmentIdentifier("unknownId", "A0000000")
        ),
        "Activated",
        None
      )
    )
  )

  private implicit val req: FakeRequest[AnyContentAsEmpty.type] = FakeRequest()
  private implicit val hc: HeaderCarrier = HeaderCarrierConverter.fromRequest(req)

  private val body: PsrAuthContext[Any] => Future[Result] = _ => Future.successful(Ok)

  "PsrAuth" should {
    "authorise a PSA" in {
      val auth = new PsrAuth {
        override def authConnector: AuthConnector = mockAuthConnector
      }

      when(mockAuthConnector.authorise[Option[String] ~ Enrolments](any(), any())(any(), any()))
        .thenReturn(Future.successful(new ~(Some(externalId), psaEnrolment)))

      val result = Await.result(auth.authorisedAsPsrUser(body), Duration.Inf)
      result.header.status mustEqual 200
    }

    "authorise a PSP" in {
      val auth = new PsrAuth {
        override def authConnector: AuthConnector = mockAuthConnector
      }

      when(mockAuthConnector.authorise[Option[String] ~ Enrolments](any(), any())(any(), any()))
        .thenReturn(Future.successful(new ~(Some(externalId), pspEnrolment)))

      val result = Await.result(auth.authorisedAsPsrUser(body), Duration.Inf)
      result.header.status mustEqual 200
    }

    "fail when it's not possible to authorise as there is no psp or psa enrolment" in {
      val auth = new PsrAuth {
        override def authConnector: AuthConnector = mockAuthConnector
      }

      when(mockAuthConnector.authorise[Option[String] ~ Enrolments](any(), any())(any(), any()))
        .thenReturn(Future.successful(new ~(Some(externalId), unknownEnrolment)))

      intercept[BadRequestException](Await.result(auth.authorisedAsPsrUser(body), Duration.Inf))
    }
  }
}
