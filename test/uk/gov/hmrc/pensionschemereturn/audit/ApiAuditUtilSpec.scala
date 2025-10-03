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

import play.api.test.FakeRequest
import uk.gov.hmrc.pensionschemereturn.services.AuditService
import play.api.mvc.RequestHeader
import play.api.http.Status
import uk.gov.hmrc.pensionschemereturn.config.Constants.PSA
import uk.gov.hmrc.http.{HttpException, HttpResponse, UpstreamErrorResponse}
import org.mockito.ArgumentMatchers
import org.mockito.ArgumentMatchers.any
import utils.BaseSpec
import org.mockito.Mockito._
import org.scalatest.BeforeAndAfterEach
import play.api.libs.json.Json

import scala.concurrent.ExecutionContext.Implicits.global
import scala.util.{Failure, Success}

class ApiAuditUtilSpec extends BaseSpec with BeforeAndAfterEach {

  private implicit lazy val rh: RequestHeader = FakeRequest("", "")

  private val mockAuditService = mock[AuditService]

  private val payload = Json.obj("test" -> "test")
  private val responseData = Json.obj("responsetest" -> "test")

  override def beforeEach(): Unit =
    reset(mockAuditService)

  val service = new ApiAuditUtil(mockAuditService)

  "firePsrPostAuditEvent" must {

    doNothing().when(mockAuditService).sendEvent(any())(any(), any())
    val psrPostEventPf = service.firePsrPostAuditEvent(pstr, payload, schemeName, PSA, psaPspId, userName, cipPsrStatus)

    "send the correct audit event for a successful response" in {

      psrPostEventPf(Success(HttpResponse.apply(Status.OK, responseData, Map.empty)))
      val expectedAuditEvent = PsrPostAuditEvent(
        pstr = pstr,
        credentialRole = PSA,
        psaPspId = psaPspId,
        userName = userName,
        schemeName = schemeName,
        payload = payload,
        status = Some(Status.OK),
        response = Some(responseData),
        errorMessage = None,
        psrStatus = None
      )
      verify(mockAuditService, times(1)).sendEvent(ArgumentMatchers.eq(expectedAuditEvent))(any(), any())
    }

    "send the audit event with the status code when an upstream error occurs" in {

      val reportAs = 202
      val message = "The request was not found"
      val status = Status.NOT_FOUND
      psrPostEventPf(Failure(UpstreamErrorResponse.apply(message, status, reportAs, Map.empty)))
      val expectedAuditEvent = PsrPostAuditEvent(
        pstr = pstr,
        credentialRole = PSA,
        psaPspId = psaPspId,
        userName = userName,
        schemeName = schemeName,
        status = Some(status),
        payload = payload,
        response = None,
        errorMessage = Some(message),
        psrStatus = None
      )
      verify(mockAuditService, times(1)).sendEvent(ArgumentMatchers.eq(expectedAuditEvent))(any(), any())
    }

    "send the audit event with the status code when an HttpException error occurs" in {

      val message = "The request had a network error"
      val status = Status.SERVICE_UNAVAILABLE
      psrPostEventPf(Failure(new HttpException(message, status)))
      val expectedAuditEvent = PsrPostAuditEvent(
        pstr = pstr,
        credentialRole = PSA,
        psaPspId = psaPspId,
        userName = userName,
        schemeName = schemeName,
        status = Some(status),
        payload = payload,
        response = None,
        errorMessage = Some(message),
        psrStatus = None
      )
      verify(mockAuditService, times(1)).sendEvent(ArgumentMatchers.eq(expectedAuditEvent))(any(), any())
    }

    "send the audit event when a throwable is thrown" in {

      val message = "The request had a network error"
      psrPostEventPf(Failure(new RuntimeException(message)))
      val expectedAuditEvent = PsrPostAuditEvent(
        pstr = pstr,
        credentialRole = PSA,
        psaPspId = psaPspId,
        userName = userName,
        schemeName = schemeName,
        status = None,
        payload = payload,
        response = None,
        errorMessage = Some(message),
        psrStatus = None
      )
      verify(mockAuditService, times(1)).sendEvent(ArgumentMatchers.eq(expectedAuditEvent))(any(), any())
    }
  }
}
