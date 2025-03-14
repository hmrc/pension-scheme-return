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

package uk.gov.hmrc.pensionschemereturn.services

import play.api.test.FakeRequest
import uk.gov.hmrc.pensionschemereturn.auth.PsrAuthContext
import uk.gov.hmrc.pensionschemereturn.models.etmp.{Compiled, EtmpPsrStatus, Submitted}
import uk.gov.hmrc.pensionschemereturn.models.nonsipp.{MinimalRequiredSubmission, PsrSubmission, ReportDetails}
import play.api.libs.json.Json
import uk.gov.hmrc.pensionschemereturn.connectors.PsrConnector
import com.softwaremill.diffx.generic.auto.indicator
import org.scalatestplus.mockito.MockitoSugar
import org.mockito.ArgumentMatchers.any
import utils.{BaseSpec, TestValues}
import play.api.test.Helpers.{await, defaultAwaitTimeout}
import uk.gov.hmrc.pensionschemereturn.config.AppConfig
import org.mockito.Mockito._
import play.api.mvc.AnyContentAsEmpty
import com.softwaremill.diffx.scalatest.DiffShouldMatcher
import uk.gov.hmrc.pensionschemereturn.transformations.nonsipp.{PsrSubmissionToEtmp, StandardPsrFromEtmp}
import uk.gov.hmrc.pensionschemereturn.config.Constants.PSA
import uk.gov.hmrc.pensionschemereturn.models.enumeration.CipPsrStatus
import uk.gov.hmrc.pensionschemereturn.models._
import play.api.http.Status.{BAD_REQUEST, EXPECTATION_FAILED}
import uk.gov.hmrc.http._
import uk.gov.hmrc.pensionschemereturn.validators.{JSONSchemaValidator, SchemaValidationResult}
import uk.gov.hmrc.pensionschemereturn.transformations.TransformerError

import scala.concurrent.Future
import scala.concurrent.ExecutionContext.Implicits.global

import java.time.LocalDateTime

class PsrSubmissionServiceSpec extends BaseSpec with MockitoSugar with TestValues with DiffShouldMatcher {

  override def beforeEach(): Unit = {
    reset(mockPsrConnector)
    reset(mockJSONSchemaValidator)
    reset(mockPsrSubmissionToEtmp)
  }

  private val mockPsrConnector = mock[PsrConnector]
  private val mockJSONSchemaValidator = mock[JSONSchemaValidator]
  private val mockPsrSubmissionToEtmp = mock[PsrSubmissionToEtmp]
  private val mockStandardPsrFromEtmp = mock[StandardPsrFromEtmp]
  private val mockConfig = mock[AppConfig]

  private val service: PsrSubmissionService = new PsrSubmissionService(
    mockPsrConnector,
    mockJSONSchemaValidator,
    mockPsrSubmissionToEtmp,
    mockStandardPsrFromEtmp,
    mockConfig
  )

  private implicit val hc: HeaderCarrier = HeaderCarrier()
  private implicit val rq: FakeRequest[AnyContentAsEmpty.type] = FakeRequest()

  "getStandardPsr" should {
    "return 200 without data when connector returns successfully" in {

      when(mockPsrConnector.getStandardPsr(any(), any(), any(), any(), any(), any(), any(), any())(any(), any(), any()))
        .thenReturn(Future.successful(None))

      whenReady(
        service.getStandardPsr(
          "testPstr",
          Some("fbNumber"),
          None,
          None,
          PsrAuthContext(
            externalId = "externalId",
            psaPspId = "psaPspId",
            credentialRole = PSA,
            request = rq
          ),
          "userName",
          "schemeName"
        )
      ) { result =>
        result mustBe None

        verify(mockPsrConnector, times(1)).getStandardPsr(any(), any(), any(), any(), any(), any(), any(), any())(
          any(),
          any(),
          any()
        )
        verify(mockStandardPsrFromEtmp, never).transform(any())
      }
    }

    "return 200 with data when connector returns successfully" in {

      when(mockPsrConnector.getStandardPsr(any(), any(), any(), any(), any(), any(), any(), any())(any(), any(), any()))
        .thenReturn(Future.successful(Some(samplePsrSubmissionEtmpResponse)))
      when(mockStandardPsrFromEtmp.transform(any())).thenReturn(Right(samplePsrSubmission))

      whenReady(
        service.getStandardPsr(
          "testPstr",
          Some("fbNumber"),
          None,
          None,
          PsrAuthContext(
            externalId = "externalId",
            psaPspId = "psaPspId",
            credentialRole = PSA,
            request = rq
          ),
          "userName",
          "schemeName"
        )
      ) { (result: Option[Either[TransformerError, PsrSubmission]]) =>

        val actual: Option[Right[TransformerError, PsrSubmission]] = Some(Right(samplePsrSubmission))
        result shouldMatchTo actual

        verify(mockPsrConnector, times(1)).getStandardPsr(any(), any(), any(), any(), any(), any(), any(), any())(
          any(),
          any(),
          any()
        )
        verify(mockStandardPsrFromEtmp, times(1)).transform(any())
      }
    }
  }

  "submitStandardPsr" should {
    "successfully submit only minimal required submission details" in {
      val expectedResponse = HttpResponse(200, Json.obj(), Map.empty)

      when(mockPsrSubmissionToEtmp.transform(any())).thenReturn(samplePsrSubmissionEtmpRequest)
      when(mockJSONSchemaValidator.validatePayload(any(), any()))
        .thenReturn(SchemaValidationResult(Set.empty))
      when(mockPsrConnector.submitStandardPsr(any(), any(), any(), any(), any(), any(), any())(any(), any(), any()))
        .thenReturn(Future.successful(expectedResponse))

      whenReady(
        service.submitStandardPsr(
          samplePsrSubmission,
          PsrAuthContext(
            externalId = "externalId",
            psaPspId = "psaPspId",
            credentialRole = PSA,
            request = rq
          ),
          "userName",
          "schemeName"
        )
      ) { (result: HttpResponse) =>
        result mustEqual expectedResponse

        verify(mockPsrSubmissionToEtmp, times(1)).transform(any())
        verify(mockJSONSchemaValidator, times(1)).validatePayload(any(), any())
        verify(mockPsrConnector, times(1)).submitStandardPsr(any(), any(), any(), any(), any(), any(), any())(
          any(),
          any(),
          any()
        )
      }
    }

    "throw exception when validation fails for submitStandardPsr" in {
      when(mockPsrSubmissionToEtmp.transform(any())).thenReturn(samplePsrSubmissionEtmpRequest)
      when(mockJSONSchemaValidator.validatePayload(any(), any()))
        .thenReturn(SchemaValidationResult(Set(validationMessage)))

      val thrown = intercept[PensionSchemeReturnValidationFailureException] {
        await(
          service.submitStandardPsr(
            samplePsrSubmission,
            PsrAuthContext(
              externalId = "externalId",
              psaPspId = "psaPspId",
              credentialRole = PSA,
              request = rq
            ),
            "userName",
            "schemeName"
          )
        )
      }
      thrown.responseCode mustBe BAD_REQUEST
      thrown.message must include("Invalid payload when submitStandardPsr :-\ncustomMessage")

      verify(mockPsrSubmissionToEtmp, times(1)).transform(any())
      verify(mockJSONSchemaValidator, times(1)).validatePayload(any(), any())
      verify(mockPsrConnector, never).submitStandardPsr(any(), any(), any(), any(), any(), any(), any())(
        any(),
        any(),
        any()
      )
    }

    "throw exception when connector call not successful for submitStandardPsr" in {
      when(mockPsrSubmissionToEtmp.transform(any())).thenReturn(samplePsrSubmissionEtmpRequest)
      when(mockJSONSchemaValidator.validatePayload(any(), any()))
        .thenReturn(SchemaValidationResult(Set.empty))
      when(mockPsrConnector.submitStandardPsr(any(), any(), any(), any(), any(), any(), any())(any(), any(), any()))
        .thenReturn(Future.failed(new BadRequestException("invalid-request")))

      val thrown = intercept[ExpectationFailedException] {
        await(
          service.submitStandardPsr(
            samplePsrSubmission,
            PsrAuthContext(
              externalId = "externalId",
              psaPspId = "psaPspId",
              credentialRole = PSA,
              request = rq
            ),
            "userName",
            "schemeName"
          )
        )
      }
      thrown.responseCode mustBe EXPECTATION_FAILED
      thrown.message must include("invalid-request")

      verify(mockPsrSubmissionToEtmp, times(1)).transform(any())
      verify(mockJSONSchemaValidator, times(1)).validatePayload(any(), any())
      verify(mockPsrConnector, times(1)).submitStandardPsr(any(), any(), any(), any(), any(), any(), any())(
        any(),
        any(),
        any()
      )
    }
    "getCipPsrStatus" should {
      List(
        (Some("000"), Some(Compiled), None),
        (Some("000"), Some(Submitted), None),
        (Some("001"), Some(Compiled), None),
        (Some("001"), Some(Submitted), None),
        (Some("002"), Some(Compiled), Some(CipPsrStatus.CHANGED_COMPILED.toString)),
        (Some("002"), Some(Submitted), Some(CipPsrStatus.CHANGED_SUBMITTED.toString)),
        (Some("003"), Some(Compiled), Some(CipPsrStatus.CHANGED_COMPILED.toString)),
        (Some("003"), Some(Submitted), Some(CipPsrStatus.CHANGED_SUBMITTED.toString)),
        (None, Some(Compiled), Some(CipPsrStatus.CHANGED_COMPILED.toString)),
        (None, Some(Submitted), Some(CipPsrStatus.CHANGED_SUBMITTED.toString)),
        (Some("000"), None, None),
        (None, None, None)
      ).foreach { case (version: Option[String], status: Option[EtmpPsrStatus], expected: Option[String]) =>
        s"$version and $status should return $expected" in {
          val minimalRequiredSubmission: MinimalRequiredSubmission = MinimalRequiredSubmission(
            reportDetails = ReportDetails(
              fbVersion = version,
              fbstatus = status,
              pstr = pstr,
              periodStart = sampleToday,
              periodEnd = sampleToday,
              compilationOrSubmissionDate = Some(LocalDateTime.parse("2023-04-02T09:30:47"))
            ),
            accountingPeriodDetails = sampleAccountingPeriodDetails,
            schemeDesignatory = sampleSchemeDesignatory
          )

          val actual: Option[String] =
            service.getCipPsrStatus(samplePsrSubmission.copy(minimalRequiredSubmission = minimalRequiredSubmission))
          expected shouldMatchTo actual
        }
      }
    }
  }

  "submitPrePopulatedPsr" should {
    "successfully submit only minimal required submission details" in {
      val expectedResponse = HttpResponse(200, Json.obj(), Map.empty)

      when(mockPsrSubmissionToEtmp.transform(any())).thenReturn(samplePsrSubmissionEtmpRequest)
      when(mockJSONSchemaValidator.validatePayload(any(), any()))
        .thenReturn(SchemaValidationResult(Set.empty))
      when(mockPsrConnector.submitStandardPsr(any(), any(), any(), any(), any(), any(), any())(any(), any(), any()))
        .thenReturn(Future.successful(expectedResponse))

      whenReady(
        service.submitPrePopulatedPsr(
          samplePsrSubmission,
          PsrAuthContext(
            externalId = "externalId",
            psaPspId = "psaPspId",
            credentialRole = PSA,
            request = rq
          ),
          "userName",
          "schemeName"
        )
      ) { (result: HttpResponse) =>
        result mustEqual expectedResponse

        verify(mockPsrSubmissionToEtmp, times(1)).transform(any())
        verify(mockJSONSchemaValidator, times(1)).validatePayload(any(), any())
        verify(mockPsrConnector, times(1)).submitStandardPsr(any(), any(), any(), any(), any(), any(), any())(
          any(),
          any(),
          any()
        )
      }
    }

    "throw exception when validation fails" in {
      when(mockPsrSubmissionToEtmp.transform(any())).thenReturn(samplePsrSubmissionEtmpRequest)
      when(mockJSONSchemaValidator.validatePayload(any(), any()))
        .thenReturn(SchemaValidationResult(Set(validationMessage)))

      val thrown = intercept[PensionSchemeReturnValidationFailureException] {
        await(
          service.submitPrePopulatedPsr(
            samplePsrSubmission,
            PsrAuthContext(
              externalId = "externalId",
              psaPspId = "psaPspId",
              credentialRole = PSA,
              request = rq
            ),
            "userName",
            "schemeName"
          )
        )
      }
      thrown.responseCode mustBe BAD_REQUEST
      thrown.message must include("Invalid payload when submitPrePopulatedPsr :-\ncustomMessage")

      verify(mockPsrSubmissionToEtmp, times(1)).transform(any())
      verify(mockJSONSchemaValidator, times(1)).validatePayload(any(), any())
      verify(mockPsrConnector, never).submitStandardPsr(any(), any(), any(), any(), any(), any(), any())(
        any(),
        any(),
        any()
      )
    }

    "throw exception when connector call not successful" in {
      when(mockPsrSubmissionToEtmp.transform(any())).thenReturn(samplePsrSubmissionEtmpRequest)
      when(mockJSONSchemaValidator.validatePayload(any(), any()))
        .thenReturn(SchemaValidationResult(Set.empty))
      when(mockPsrConnector.submitStandardPsr(any(), any(), any(), any(), any(), any(), any())(any(), any(), any()))
        .thenReturn(Future.failed(new BadRequestException("invalid-request")))

      val thrown = intercept[ExpectationFailedException] {
        await(
          service.submitPrePopulatedPsr(
            samplePsrSubmission,
            PsrAuthContext(
              externalId = "externalId",
              psaPspId = "psaPspId",
              credentialRole = PSA,
              request = rq
            ),
            "userName",
            "schemeName"
          )
        )
      }
      thrown.responseCode mustBe EXPECTATION_FAILED
      thrown.message must include("invalid-request")

      verify(mockPsrSubmissionToEtmp, times(1)).transform(any())
      verify(mockJSONSchemaValidator, times(1)).validatePayload(any(), any())
      verify(mockPsrConnector, times(1)).submitStandardPsr(any(), any(), any(), any(), any(), any(), any())(
        any(),
        any(),
        any()
      )
    }
  }
}
