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

import com.networknt.schema.{CustomErrorMessageType, ValidationMessage}
import org.mockito.ArgumentMatchers.any
import org.mockito.MockitoSugar
import play.api.http.Status.{BAD_REQUEST, EXPECTATION_FAILED}
import play.api.libs.json.Json
import play.api.mvc.AnyContentAsEmpty
import play.api.test.FakeRequest
import play.api.test.Helpers.{await, defaultAwaitTimeout}
import uk.gov.hmrc.http.{BadRequestException, ExpectationFailedException, HeaderCarrier, HttpResponse}
import uk.gov.hmrc.pensionschemereturn.connectors.PsrConnector
import uk.gov.hmrc.pensionschemereturn.models._
import uk.gov.hmrc.pensionschemereturn.models.nonsipp.PsrSubmission
import uk.gov.hmrc.pensionschemereturn.services.PsrSubmissionServiceSpec._
import uk.gov.hmrc.pensionschemereturn.transformations.nonsipp.{PsrSubmissionToEtmp, StandardPsrFromEtmp}
import uk.gov.hmrc.pensionschemereturn.transformations.sipp.SippPsrSubmissionToEtmp
import uk.gov.hmrc.pensionschemereturn.validators.{JSONSchemaValidator, SchemaValidationResult}
import utils.BaseSpec

import java.text.MessageFormat
import scala.concurrent.ExecutionContext.Implicits.global
import scala.concurrent.Future

class PsrSubmissionServiceSpec extends BaseSpec with MockitoSugar {

  override def beforeEach(): Unit = {
    reset(mockPsrConnector)
    reset(mockJSONSchemaValidator)
    reset(mockPsrSubmissionToEtmp)
    reset(mockSippPsrSubmissionToEtmp)
  }

  private val mockPsrConnector = mock[PsrConnector]
  private val mockJSONSchemaValidator = mock[JSONSchemaValidator]
  private val mockPsrSubmissionToEtmp = mock[PsrSubmissionToEtmp]
  private val mockSippPsrSubmissionToEtmp = mock[SippPsrSubmissionToEtmp]
  private val mockStandardPsrFromEtmp = mock[StandardPsrFromEtmp]

  private val service = new PsrSubmissionService(
    mockPsrConnector,
    mockJSONSchemaValidator,
    mockPsrSubmissionToEtmp,
    mockSippPsrSubmissionToEtmp,
    mockStandardPsrFromEtmp
  )
  private implicit val hc: HeaderCarrier = HeaderCarrier()
  private implicit val rq: FakeRequest[AnyContentAsEmpty.type] = FakeRequest()

  "getStandardPsr" should {
    "return 200 without data when connector returns successfully" in {

      when(mockPsrConnector.getStandardPsr(any(), any(), any(), any())(any(), any()))
        .thenReturn(Future.successful(None))

      whenReady(service.getStandardPsr("testPstr", Some("fbNumber"), None, None)) { result: Option[PsrSubmission] =>
        result mustBe None

        verify(mockPsrConnector, times(1)).getStandardPsr(any(), any(), any(), any())(any(), any())
        verify(mockStandardPsrFromEtmp, never).transform(any())
      }
    }

    "return 200 with data when connector returns successfully" in {

      when(mockPsrConnector.getStandardPsr(any(), any(), any(), any())(any(), any()))
        .thenReturn(Future.successful(Some(samplePsrSubmissionResponse)))
      when(mockStandardPsrFromEtmp.transform(any())).thenReturn(samplePsrSubmission)

      whenReady(service.getStandardPsr("testPstr", Some("fbNumber"), None, None)) { result: Option[PsrSubmission] =>
        result mustBe Some(samplePsrSubmission)

        verify(mockPsrConnector, times(1)).getStandardPsr(any(), any(), any(), any())(any(), any())
        verify(mockStandardPsrFromEtmp, times(1)).transform(any())
      }
    }
  }

  "submitStandardPsr" should {
    "successfully submit only minimal required submission details" in {
      val expectedResponse = HttpResponse(201, Json.obj(), Map.empty)

      when(mockPsrSubmissionToEtmp.transform(any())).thenReturn(samplePsrSubmissionRequest)
      when(mockJSONSchemaValidator.validatePayload(any(), any()))
        .thenReturn(SchemaValidationResult(Set.empty))
      when(mockPsrConnector.submitStandardPsr(any())(any(), any(), any()))
        .thenReturn(Future.successful(expectedResponse))

      whenReady(service.submitStandardPsr(samplePsrSubmission)) { result: HttpResponse =>
        result mustEqual expectedResponse

        verify(mockPsrSubmissionToEtmp, times(1)).transform(any())
        verify(mockJSONSchemaValidator, times(1)).validatePayload(any(), any())
        verify(mockPsrConnector, times(1)).submitStandardPsr(any())(any(), any(), any())
      }
    }

    "throw exception when validation fails for submitStandardPsr" in {
      when(mockPsrSubmissionToEtmp.transform(any())).thenReturn(samplePsrSubmissionRequest)
      when(mockJSONSchemaValidator.validatePayload(any(), any()))
        .thenReturn(SchemaValidationResult(Set(validationMessage)))

      val thrown = intercept[PensionSchemeReturnValidationFailureException] {
        await(service.submitStandardPsr(samplePsrSubmission))
      }
      thrown.responseCode mustBe BAD_REQUEST
      thrown.message must include("Invalid payload when submitStandardPsr :-\ncustomMessage")

      verify(mockPsrSubmissionToEtmp, times(1)).transform(any())
      verify(mockJSONSchemaValidator, times(1)).validatePayload(any(), any())
      verify(mockPsrConnector, never).submitStandardPsr(any())(any(), any(), any())
    }

    "throw exception when connector call not successful for submitStandardPsr" in {
      when(mockPsrSubmissionToEtmp.transform(any())).thenReturn(samplePsrSubmissionRequest)
      when(mockJSONSchemaValidator.validatePayload(any(), any()))
        .thenReturn(SchemaValidationResult(Set.empty))
      when(mockPsrConnector.submitStandardPsr(any())(any(), any(), any()))
        .thenReturn(Future.failed(new BadRequestException("invalid-request")))

      val thrown = intercept[ExpectationFailedException] {
        await(service.submitStandardPsr(samplePsrSubmission))
      }
      thrown.responseCode mustBe EXPECTATION_FAILED
      thrown.message must include("Nothing to submit")

      verify(mockPsrSubmissionToEtmp, times(1)).transform(any())
      verify(mockJSONSchemaValidator, times(1)).validatePayload(any(), any())
      verify(mockPsrConnector, times(1)).submitStandardPsr(any())(any(), any(), any())
    }
  }

  "submitSippPsr" should {
    "successfully submit only minimal required SIPP submission details" in {
      val expectedResponse = HttpResponse(201, Json.obj(), Map.empty)

      when(mockSippPsrSubmissionToEtmp.transform(any())).thenReturn(sampleSippPsrSubmissionRequest)
      when(mockJSONSchemaValidator.validatePayload(any(), any()))
        .thenReturn(SchemaValidationResult(Set.empty))
      when(mockPsrConnector.submitSippPsr(any())(any(), any(), any()))
        .thenReturn(Future.successful(expectedResponse))

      whenReady(service.submitSippPsr(sampleSippPsrSubmission)) { result: HttpResponse =>
        result mustEqual expectedResponse

        verify(mockSippPsrSubmissionToEtmp, times(1)).transform(any())
        verify(mockJSONSchemaValidator, times(1)).validatePayload(any(), any())
        verify(mockPsrConnector, times(1)).submitSippPsr(any())(any(), any(), any())
      }
    }

    "throw exception when validation fails for submitSippPsr" in {
      when(mockSippPsrSubmissionToEtmp.transform(any())).thenReturn(sampleSippPsrSubmissionRequest)
      when(mockJSONSchemaValidator.validatePayload(any(), any()))
        .thenReturn(SchemaValidationResult(Set(validationMessage)))

      val thrown = intercept[PensionSchemeReturnValidationFailureException] {
        await(service.submitSippPsr(sampleSippPsrSubmission))
      }
      thrown.responseCode mustBe BAD_REQUEST
      thrown.message must include("Invalid payload when submitSippPsr :-\ncustomMessage")

      verify(mockSippPsrSubmissionToEtmp, times(1)).transform(any())
      verify(mockJSONSchemaValidator, times(1)).validatePayload(any(), any())
      verify(mockPsrConnector, never).submitSippPsr(any())(any(), any(), any())
    }

    "throw exception when connector call not successful for submitSippPsr" in {
      when(mockSippPsrSubmissionToEtmp.transform(any())).thenReturn(sampleSippPsrSubmissionRequest)
      when(mockJSONSchemaValidator.validatePayload(any(), any()))
        .thenReturn(SchemaValidationResult(Set.empty))
      when(mockPsrConnector.submitSippPsr(any())(any(), any(), any()))
        .thenReturn(Future.failed(new BadRequestException("invalid-request")))

      val thrown = intercept[ExpectationFailedException] {
        await(service.submitSippPsr(sampleSippPsrSubmission))
      }
      thrown.responseCode mustBe EXPECTATION_FAILED
      thrown.message must include("Nothing to submit")

      verify(mockSippPsrSubmissionToEtmp, times(1)).transform(any())
      verify(mockJSONSchemaValidator, times(1)).validatePayload(any(), any())
      verify(mockPsrConnector, times(1)).submitSippPsr(any())(any(), any(), any())
    }
  }

}

object PsrSubmissionServiceSpec {

  val validationMessage: ValidationMessage = ValidationMessage.ofWithCustom(
    "type",
    CustomErrorMessageType.of("CustomErrorMessageType"),
    new MessageFormat("MessageFormat"),
    "customMessage",
    "at",
    "schemaPath"
  )
}
