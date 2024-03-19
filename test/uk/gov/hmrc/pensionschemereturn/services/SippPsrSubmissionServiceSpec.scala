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
import uk.gov.hmrc.pensionschemereturn.transformations.sipp.{SippPsrFromEtmp, SippPsrSubmissionToEtmp}
import play.api.mvc.AnyContentAsEmpty
import uk.gov.hmrc.pensionschemereturn.models._
import uk.gov.hmrc.pensionschemereturn.validators.{JSONSchemaValidator, SchemaValidationResult}
import uk.gov.hmrc.pensionschemereturn.connectors.PsrConnector
import org.mockito.MockitoSugar
import org.mockito.ArgumentMatchers.any
import utils.{BaseSpec, TestValues}
import play.api.test.Helpers.{await, defaultAwaitTimeout}
import uk.gov.hmrc.pensionschemereturn.models.sipp.SippPsrSubmission
import play.api.libs.json.Json
import play.api.http.Status.{BAD_REQUEST, EXPECTATION_FAILED}
import uk.gov.hmrc.http._

import scala.concurrent.Future
import scala.concurrent.ExecutionContext.Implicits.global

class SippPsrSubmissionServiceSpec extends BaseSpec with MockitoSugar with TestValues {

  override def beforeEach(): Unit = {
    reset(mockPsrConnector)
    reset(mockJSONSchemaValidator)
    reset(mockSippPsrSubmissionToEtmp)
    reset(mockSippPsrFromEtmp)
  }

  private val mockPsrConnector = mock[PsrConnector]
  private val mockJSONSchemaValidator = mock[JSONSchemaValidator]
  private val mockSippPsrSubmissionToEtmp = mock[SippPsrSubmissionToEtmp]
  private val mockSippPsrFromEtmp = mock[SippPsrFromEtmp]

  private val service: SippPsrSubmissionService = new SippPsrSubmissionService(
    mockPsrConnector,
    mockJSONSchemaValidator,
    mockSippPsrSubmissionToEtmp,
    mockSippPsrFromEtmp
  )

  private implicit val hc: HeaderCarrier = HeaderCarrier()
  private implicit val rq: FakeRequest[AnyContentAsEmpty.type] = FakeRequest()

  "getSippPsr" should {
    "return 200 without data when connector returns successfully" in {

      when(mockPsrConnector.getSippPsr(any(), any(), any(), any())(any(), any()))
        .thenReturn(Future.successful(None))

      whenReady(service.getSippPsr("testPstr", Some("fbNumber"), None, None)) { result: Option[SippPsrSubmission] =>
        result mustBe None

        verify(mockPsrConnector, times(1)).getSippPsr(any(), any(), any(), any())(any(), any())
        verify(mockSippPsrFromEtmp, never).transform(any())
      }
    }

    "return 200 with data when connector returns successfully" in {

      when(mockPsrConnector.getSippPsr(any(), any(), any(), any())(any(), any()))
        .thenReturn(Future.successful(Some(sampleSippPsrSubmissionEtmpResponse)))
      when(mockSippPsrFromEtmp.transform(any())).thenReturn(sampleSippPsrSubmission)

      whenReady(service.getSippPsr("testPstr", Some("fbNumber"), None, None)) { result: Option[SippPsrSubmission] =>
        result mustBe Some(sampleSippPsrSubmission)

        verify(mockPsrConnector, times(1)).getSippPsr(any(), any(), any(), any())(any(), any())
        verify(mockSippPsrFromEtmp, times(1)).transform(any())
      }
    }
  }

  "submitSippPsr" should {
    "successfully submit only minimal required SIPP submission details" in {
      val expectedResponse = HttpResponse(200, Json.obj(), Map.empty)

      when(mockSippPsrSubmissionToEtmp.transform(any())).thenReturn(sampleSippPsrSubmissionEtmpRequest)
      when(mockJSONSchemaValidator.validatePayload(any(), any()))
        .thenReturn(SchemaValidationResult(Set.empty))
      when(mockPsrConnector.submitSippPsr(any(), any())(any(), any(), any()))
        .thenReturn(Future.successful(expectedResponse))

      whenReady(service.submitSippPsr(sampleSippPsrSubmission)) { result: HttpResponse =>
        result mustEqual expectedResponse

        verify(mockSippPsrSubmissionToEtmp, times(1)).transform(any())
        verify(mockJSONSchemaValidator, times(1)).validatePayload(any(), any())
        verify(mockPsrConnector, times(1)).submitSippPsr(any(), any())(any(), any(), any())
      }
    }

    "throw exception when validation fails for submitSippPsr" in {
      when(mockSippPsrSubmissionToEtmp.transform(any())).thenReturn(sampleSippPsrSubmissionEtmpRequest)
      when(mockJSONSchemaValidator.validatePayload(any(), any()))
        .thenReturn(SchemaValidationResult(Set(validationMessage)))

      val thrown = intercept[PensionSchemeReturnValidationFailureException] {
        await(service.submitSippPsr(sampleSippPsrSubmission))
      }
      thrown.responseCode mustBe BAD_REQUEST
      thrown.message must include("Invalid payload when submitSippPsr :-\ncustomMessage")

      verify(mockSippPsrSubmissionToEtmp, times(1)).transform(any())
      verify(mockJSONSchemaValidator, times(1)).validatePayload(any(), any())
      verify(mockPsrConnector, never).submitSippPsr(any(), any())(any(), any(), any())
    }

    "throw exception when connector call not successful for submitSippPsr" in {
      when(mockSippPsrSubmissionToEtmp.transform(any())).thenReturn(sampleSippPsrSubmissionEtmpRequest)
      when(mockJSONSchemaValidator.validatePayload(any(), any()))
        .thenReturn(SchemaValidationResult(Set.empty))
      when(mockPsrConnector.submitSippPsr(any(), any())(any(), any(), any()))
        .thenReturn(Future.failed(new BadRequestException("invalid-request")))

      val thrown = intercept[ExpectationFailedException] {
        await(service.submitSippPsr(sampleSippPsrSubmission))
      }
      thrown.responseCode mustBe EXPECTATION_FAILED
      thrown.message must include("Nothing to submit")

      verify(mockSippPsrSubmissionToEtmp, times(1)).transform(any())
      verify(mockJSONSchemaValidator, times(1)).validatePayload(any(), any())
      verify(mockPsrConnector, times(1)).submitSippPsr(any(), any())(any(), any(), any())
    }
  }

}
