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
import play.api.http.Status
import uk.gov.hmrc.pensionschemereturn.config.Constants._
import uk.gov.hmrc.auth.core._
import uk.gov.hmrc.auth.core.retrieve.~
import uk.gov.hmrc.http.{BadRequestException, HeaderCarrier, UnauthorizedException}
import uk.gov.hmrc.pensionschemereturn.connectors.SchemeDetailsConnector
import uk.gov.hmrc.play.http.HeaderCarrierConverter
import play.api.mvc.Results.Ok
import org.mockito.ArgumentMatchers.any
import utils.BaseSpec
import play.api.test.Helpers.{contentAsString, defaultAwaitTimeout, status}
import org.mockito.Mockito._

import scala.concurrent.{Await, Future}
import scala.concurrent.ExecutionContext.Implicits.global
import scala.concurrent.duration.Duration

class PsrAuthSpec extends BaseSpec {

  private val mockAuthConnector = mock[AuthConnector]
  private val mockSchemeDetailsConnector = mock[SchemeDetailsConnector]

  override protected def beforeEach(): Unit = {
    reset(mockAuthConnector)
    reset(mockSchemeDetailsConnector)
  }

  private val psaEnrolment = Enrolments(
    Set(
      Enrolment(
        psaEnrolmentKey,
        Seq(
          EnrolmentIdentifier("PSAID", "A0000000")
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
          EnrolmentIdentifier("PSPID", "A0000000")
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

  val auth: PsrAuth = new PsrAuth {
    override val authConnector: AuthConnector = mockAuthConnector
    override protected val schemeDetailsConnector: SchemeDetailsConnector = mockSchemeDetailsConnector
  }

  "PsrAuth" should {

    "fail when srn is not in valid format" in {
      val result = auth.authorisedAsPsrUser("INVALID_SRN")(body)
      status(result) mustBe Status.BAD_REQUEST
      contentAsString(result) mustEqual "Invalid scheme reference number"
      verify(mockAuthConnector, never).authorise[Option[String] ~ Enrolments](any(), any())(any(), any())
      verify(mockSchemeDetailsConnector, never).checkAssociation(any(), any(), any())(any(), any())
    }

    "fail when it's not possible to authorise as there is empty enrolments and None as externalId" in {
      when(mockAuthConnector.authorise[Option[String] ~ Enrolments](any(), any())(any(), any()))
        .thenReturn(Future.successful(new ~(None, Enrolments(Set.empty))))

      intercept[UnauthorizedException](Await.result(auth.authorisedAsPsrUser(srn)(body), Duration.Inf))
      verify(mockAuthConnector, times(1)).authorise[Option[String] ~ Enrolments](any(), any())(any(), any())
      verify(mockSchemeDetailsConnector, never).checkAssociation(any(), any(), any())(any(), any())
    }

    "fail when it's not possible to authorise as there is no psp or psa enrolment" in {
      when(mockAuthConnector.authorise[Option[String] ~ Enrolments](any(), any())(any(), any()))
        .thenReturn(Future.successful(new ~(Some(externalId), unknownEnrolment)))

      intercept[BadRequestException](Await.result(auth.authorisedAsPsrUser(srn)(body), Duration.Inf))
      verify(mockAuthConnector, times(1)).authorise[Option[String] ~ Enrolments](any(), any())(any(), any())
      verify(mockSchemeDetailsConnector, never).checkAssociation(any(), any(), any())(any(), any())
    }

    "fail when it's not possible to authorise as the scheme is not associated with the user" in {
      when(mockAuthConnector.authorise[Option[String] ~ Enrolments](any(), any())(any(), any()))
        .thenReturn(Future.successful(new ~(Some(externalId), psaEnrolment)))
      when(mockSchemeDetailsConnector.checkAssociation(any(), any(), any())(any(), any()))
        .thenReturn(Future.successful(false))

      intercept[UnauthorizedException](Await.result(auth.authorisedAsPsrUser(srn)(body), Duration.Inf))
      verify(mockAuthConnector, times(1)).authorise[Option[String] ~ Enrolments](any(), any())(any(), any())
      verify(mockSchemeDetailsConnector, times(1)).checkAssociation(any(), any(), any())(any(), any())
    }

    "authorise a PSA" in {
      when(mockAuthConnector.authorise[Option[String] ~ Enrolments](any(), any())(any(), any()))
        .thenReturn(Future.successful(new ~(Some(externalId), psaEnrolment)))
      when(mockSchemeDetailsConnector.checkAssociation(any(), any(), any())(any(), any()))
        .thenReturn(Future.successful(true))

      val result = auth.authorisedAsPsrUser(srn)(body)
      status(result) mustBe Status.OK
      verify(mockAuthConnector, times(1)).authorise[Option[String] ~ Enrolments](any(), any())(any(), any())
      verify(mockSchemeDetailsConnector, times(1)).checkAssociation(any(), any(), any())(any(), any())
    }

    "authorise a PSP" in {
      when(mockAuthConnector.authorise[Option[String] ~ Enrolments](any(), any())(any(), any()))
        .thenReturn(Future.successful(new ~(Some(externalId), pspEnrolment)))
      when(mockSchemeDetailsConnector.checkAssociation(any(), any(), any())(any(), any()))
        .thenReturn(Future.successful(true))

      val result = auth.authorisedAsPsrUser(srn)(body)
      status(result) mustBe Status.OK
      verify(mockAuthConnector, times(1)).authorise[Option[String] ~ Enrolments](any(), any())(any(), any())
      verify(mockSchemeDetailsConnector, times(1)).checkAssociation(any(), any(), any())(any(), any())
    }
  }
}
