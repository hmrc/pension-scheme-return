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

package uk.gov.hmrc.pensionschemereturn.controllers

import play.api.test.FakeRequest
import uk.gov.hmrc.pensionschemereturn.services.PsrSubmissionService
import play.api.http.Status
import play.api.inject.bind
import uk.gov.hmrc.auth.core._
import uk.gov.hmrc.auth.core.retrieve.~
import uk.gov.hmrc.http._
import uk.gov.hmrc.pensionschemereturn.transformations.TransformerError
import org.scalatestplus.mockito.MockitoSugar
import org.mockito.ArgumentMatchers.any
import play.api.test.Helpers._
import org.mockito.Mockito._
import utils.{BaseSpec, TestValues}
import play.api.inject.guice.{GuiceApplicationBuilder, GuiceableModule}
import play.api.Application
import play.api.libs.json.{JsObject, JsValue, Json}
import uk.gov.hmrc.pensionschemereturn.connectors.SchemeDetailsConnector
import uk.gov.hmrc.pensionschemereturn.controllers.PsrSubmitControllerSpec.submitPsrPayload

import scala.concurrent.Future

class PsrSubmitControllerSpec extends BaseSpec with TestValues with MockitoSugar {

  implicit val hc: HeaderCarrier = HeaderCarrier()
  private val fakeRequest = FakeRequest("POST", "/")
  private val mockPsrSubmissionService = mock[PsrSubmissionService]
  private val mockAuthConnector: AuthConnector = mock[AuthConnector]
  private val mockSchemeDetailsConnector: SchemeDetailsConnector = mock[SchemeDetailsConnector]

  override def beforeEach(): Unit = {
    reset(mockAuthConnector)
    reset(mockPsrSubmissionService)
    reset(mockSchemeDetailsConnector)
  }

  val modules: Seq[GuiceableModule] =
    Seq(
      bind[PsrSubmissionService].toInstance(mockPsrSubmissionService),
      bind[AuthConnector].toInstance(mockAuthConnector),
      bind[SchemeDetailsConnector].toInstance(mockSchemeDetailsConnector)
    )

  val application: Application = new GuiceApplicationBuilder()
    .configure(conf = "auditing.enabled" -> false, "metrics.enabled" -> false, "metrics.jvm" -> false)
    .overrides(modules: _*)
    .build()

  private val controller = application.injector.instanceOf[PsrSubmitController]

  "POST standard PSR" must {

    "return 400 - Bad Request with missing parameter: srn" in {

      val thrown = intercept[BadRequestException] {
        await(controller.submitStandardPsr(fakeRequest))
      }

      thrown.message.trim mustBe "Bad Request with missing parameters: userName missing  schemeName missing  srn missing"

      verify(mockPsrSubmissionService, never).submitStandardPsr(any(), any(), any(), any())(any(), any(), any())
      verify(mockAuthConnector, never).authorise(any(), any())(any(), any())
      verify(mockSchemeDetailsConnector, never).checkAssociation(any(), any(), any())(any(), any())
    }

    "return 400 - Bad Request with Invalid scheme reference number" in {

      val result = controller.submitStandardPsr(
        fakeRequest.withHeaders("srn" -> "INVALID_SRN", "schemeName" -> schemeName, "userName" -> userName)
      )
      status(result) mustBe Status.BAD_REQUEST
      contentAsString(result) mustBe "Invalid scheme reference number"

      verify(mockPsrSubmissionService, never).submitStandardPsr(any(), any(), any(), any())(any(), any(), any())
      verify(mockAuthConnector, never).authorise(any(), any())(any(), any())
      verify(mockSchemeDetailsConnector, never).checkAssociation(any(), any(), any())(any(), any())
    }

    "return 401 - Bearer token expired" in {

      when(mockAuthConnector.authorise[Option[String] ~ Enrolments](any(), any())(any(), any()))
        .thenReturn(
          Future.failed(new BearerTokenExpired)
        )

      val thrown = intercept[AuthorisationException] {
        await(
          controller.submitStandardPsr(
            fakeRequest.withHeaders("srn" -> srn, "schemeName" -> schemeName, "userName" -> userName)
          )
        )
      }

      thrown.reason mustBe "Bearer token expired"

      verify(mockPsrSubmissionService, never).submitStandardPsr(any(), any(), any(), any())(any(), any(), any())
      verify(mockAuthConnector, times(1)).authorise(any(), any())(any(), any())
      verify(mockSchemeDetailsConnector, never).checkAssociation(any(), any(), any())(any(), any())
    }

    "return 401 - Bearer token not supplied" in {

      when(mockAuthConnector.authorise[Option[String] ~ Enrolments](any(), any())(any(), any()))
        .thenReturn(
          Future.failed(new MissingBearerToken)
        )

      val thrown = intercept[AuthorisationException] {
        await(
          controller.submitStandardPsr(
            fakeRequest.withHeaders("srn" -> srn, "schemeName" -> schemeName, "userName" -> userName)
          )
        )
      }

      thrown.reason mustBe "Bearer token not supplied"
      verify(mockPsrSubmissionService, never).submitStandardPsr(any(), any(), any(), any())(any(), any(), any())
      verify(mockAuthConnector, times(1)).authorise(any(), any())(any(), any())
      verify(mockSchemeDetailsConnector, never).checkAssociation(any(), any(), any())(any(), any())
    }

    "return 401 - Scheme is not associated with the user" in {
      when(mockAuthConnector.authorise[Option[String] ~ Enrolments](any(), any())(any(), any()))
        .thenReturn(
          Future.successful(new ~(Some(externalId), enrolments))
        )
      when(mockSchemeDetailsConnector.checkAssociation(any(), any(), any())(any(), any()))
        .thenReturn(Future.successful(false))

      val thrown = intercept[UnauthorizedException] {
        await(
          controller.submitStandardPsr(
            fakeRequest.withHeaders("srn" -> srn, "schemeName" -> schemeName, "userName" -> userName)
          )
        )
      }

      thrown.message mustBe "Not Authorised - scheme is not associated with the user"
      verify(mockPsrSubmissionService, never).submitStandardPsr(any(), any(), any(), any())(any(), any(), any())
      verify(mockAuthConnector, times(1)).authorise(any(), any())(any(), any())
      verify(mockSchemeDetailsConnector, times(1)).checkAssociation(any(), any(), any())(any(), any())
    }

    "return 204" in {
      val responseJson: JsObject = Json.obj("mock" -> "pass")

      when(mockAuthConnector.authorise[Option[String] ~ Enrolments](any(), any())(any(), any()))
        .thenReturn(
          Future.successful(new ~(Some(externalId), enrolments))
        )
      when(mockPsrSubmissionService.submitStandardPsr(any(), any(), any(), any())(any(), any(), any()))
        .thenReturn(Future.successful(HttpResponse(OK, responseJson.toString)))
      when(mockSchemeDetailsConnector.checkAssociation(any(), any(), any())(any(), any()))
        .thenReturn(Future.successful(true))

      val postRequest = fakeRequest.withJsonBody(submitPsrPayload)
      val result = controller.submitStandardPsr(
        postRequest.withHeaders(newHeaders = "srn" -> srn, "schemeName" -> schemeName, "userName" -> userName)
      )
      status(result) mustBe Status.NO_CONTENT
      verify(mockPsrSubmissionService, times(1)).submitStandardPsr(any(), any(), any(), any())(any(), any(), any())
      verify(mockAuthConnector, times(1)).authorise(any(), any())(any(), any())
      verify(mockSchemeDetailsConnector, times(1)).checkAssociation(any(), any(), any())(any(), any())
    }

    "return 400 when request body does not contain JSON" in {
      val responseJson: JsObject = Json.obj("mock" -> "pass")

      when(mockAuthConnector.authorise[Option[String] ~ Enrolments](any(), any())(any(), any()))
        .thenReturn(Future.successful(new ~(Some(externalId), enrolments)))
      when(mockPsrSubmissionService.submitStandardPsr(any(), any(), any(), any())(any(), any(), any()))
        .thenReturn(Future.successful(HttpResponse(OK, responseJson.toString)))
      when(mockSchemeDetailsConnector.checkAssociation(any(), any(), any())(any(), any()))
        .thenReturn(Future.successful(true))

      val postRequest = fakeRequest
      intercept[BadRequestException](
        await(
          controller.submitStandardPsr(
            postRequest.withHeaders(newHeaders = "srn" -> srn, "schemeName" -> schemeName, "userName" -> userName)
          )
        )
      )
      verify(mockPsrSubmissionService, never).submitStandardPsr(any(), any(), any(), any())(any(), any(), any())
      verify(mockAuthConnector, times(1)).authorise(any(), any())(any(), any())
      verify(mockSchemeDetailsConnector, times(1)).checkAssociation(any(), any(), any())(any(), any())
    }
  }

  "POST pre-populated PSR" must {

    "return 400 - Bad Request with missing parameter: srn" in {

      val thrown = intercept[BadRequestException] {
        await(controller.submitPrePopulatedPsr(fakeRequest))
      }

      thrown.message.trim mustBe "Bad Request with missing parameters: userName missing  schemeName missing  srn missing"

      verify(mockPsrSubmissionService, never).submitStandardPsr(any(), any(), any(), any())(any(), any(), any())
      verify(mockPsrSubmissionService, never).submitPrePopulatedPsr(any(), any(), any(), any())(any(), any(), any())
      verify(mockAuthConnector, never).authorise(any(), any())(any(), any())
      verify(mockSchemeDetailsConnector, never).checkAssociation(any(), any(), any())(any(), any())
    }

    "return 400 - Bad Request with Invalid scheme reference number" in {

      val result = controller.submitPrePopulatedPsr(
        fakeRequest.withHeaders("srn" -> "INVALID_SRN", "schemeName" -> schemeName, "userName" -> userName)
      )
      status(result) mustBe Status.BAD_REQUEST
      contentAsString(result) mustBe "Invalid scheme reference number"

      verify(mockPsrSubmissionService, never).submitStandardPsr(any(), any(), any(), any())(any(), any(), any())
      verify(mockPsrSubmissionService, never).submitPrePopulatedPsr(any(), any(), any(), any())(any(), any(), any())
      verify(mockAuthConnector, never).authorise(any(), any())(any(), any())
      verify(mockSchemeDetailsConnector, never).checkAssociation(any(), any(), any())(any(), any())
    }

    "return 401 - Bearer token expired" in {

      when(mockAuthConnector.authorise[Option[String] ~ Enrolments](any(), any())(any(), any()))
        .thenReturn(
          Future.failed(new BearerTokenExpired)
        )

      val thrown = intercept[AuthorisationException] {
        await(
          controller.submitPrePopulatedPsr(
            fakeRequest.withHeaders("srn" -> srn, "schemeName" -> schemeName, "userName" -> userName)
          )
        )
      }

      thrown.reason mustBe "Bearer token expired"

      verify(mockPsrSubmissionService, never).submitStandardPsr(any(), any(), any(), any())(any(), any(), any())
      verify(mockPsrSubmissionService, never).submitPrePopulatedPsr(any(), any(), any(), any())(any(), any(), any())
      verify(mockAuthConnector, times(1)).authorise(any(), any())(any(), any())
      verify(mockSchemeDetailsConnector, never).checkAssociation(any(), any(), any())(any(), any())
    }

    "return 401 - Bearer token not supplied" in {

      when(mockAuthConnector.authorise[Option[String] ~ Enrolments](any(), any())(any(), any()))
        .thenReturn(
          Future.failed(new MissingBearerToken)
        )

      val thrown = intercept[AuthorisationException] {
        await(
          controller.submitPrePopulatedPsr(
            fakeRequest.withHeaders("srn" -> srn, "schemeName" -> schemeName, "userName" -> userName)
          )
        )
      }

      thrown.reason mustBe "Bearer token not supplied"
      verify(mockPsrSubmissionService, never).submitStandardPsr(any(), any(), any(), any())(any(), any(), any())
      verify(mockPsrSubmissionService, never).submitPrePopulatedPsr(any(), any(), any(), any())(any(), any(), any())
      verify(mockAuthConnector, times(1)).authorise(any(), any())(any(), any())
      verify(mockSchemeDetailsConnector, never).checkAssociation(any(), any(), any())(any(), any())
    }

    "return 401 - Scheme is not associated with the user" in {
      when(mockAuthConnector.authorise[Option[String] ~ Enrolments](any(), any())(any(), any()))
        .thenReturn(
          Future.successful(new ~(Some(externalId), enrolments))
        )
      when(mockSchemeDetailsConnector.checkAssociation(any(), any(), any())(any(), any()))
        .thenReturn(Future.successful(false))

      val thrown = intercept[UnauthorizedException] {
        await(
          controller.submitPrePopulatedPsr(
            fakeRequest.withHeaders("srn" -> srn, "schemeName" -> schemeName, "userName" -> userName)
          )
        )
      }

      thrown.message mustBe "Not Authorised - scheme is not associated with the user"
      verify(mockPsrSubmissionService, never).submitStandardPsr(any(), any(), any(), any())(any(), any(), any())
      verify(mockAuthConnector, times(1)).authorise(any(), any())(any(), any())
      verify(mockSchemeDetailsConnector, times(1)).checkAssociation(any(), any(), any())(any(), any())
    }

    "return 204" in {
      val responseJson: JsObject = Json.obj("mock" -> "pass")

      when(mockAuthConnector.authorise[Option[String] ~ Enrolments](any(), any())(any(), any()))
        .thenReturn(
          Future.successful(new ~(Some(externalId), enrolments))
        )
      when(mockPsrSubmissionService.submitPrePopulatedPsr(any(), any(), any(), any())(any(), any(), any()))
        .thenReturn(Future.successful(HttpResponse(OK, responseJson.toString)))
      when(mockSchemeDetailsConnector.checkAssociation(any(), any(), any())(any(), any()))
        .thenReturn(Future.successful(true))

      val postRequest = fakeRequest.withJsonBody(submitPsrPayload)
      val result = controller.submitPrePopulatedPsr(
        postRequest.withHeaders(newHeaders = "srn" -> srn, "schemeName" -> schemeName, "userName" -> userName)
      )
      status(result) mustBe Status.NO_CONTENT
      verify(mockPsrSubmissionService, never).submitStandardPsr(any(), any(), any(), any())(any(), any(), any())
      verify(mockPsrSubmissionService, times(1)).submitPrePopulatedPsr(any(), any(), any(), any())(any(), any(), any())
      verify(mockAuthConnector, times(1)).authorise(any(), any())(any(), any())
      verify(mockSchemeDetailsConnector, times(1)).checkAssociation(any(), any(), any())(any(), any())
    }

    "return 400 when request body does not contain JSON" in {
      val responseJson: JsObject = Json.obj("mock" -> "pass")

      when(mockAuthConnector.authorise[Option[String] ~ Enrolments](any(), any())(any(), any()))
        .thenReturn(Future.successful(new ~(Some(externalId), enrolments)))
      when(mockPsrSubmissionService.submitStandardPsr(any(), any(), any(), any())(any(), any(), any()))
        .thenReturn(Future.successful(HttpResponse(OK, responseJson.toString)))
      when(mockSchemeDetailsConnector.checkAssociation(any(), any(), any())(any(), any()))
        .thenReturn(Future.successful(true))

      val postRequest = fakeRequest
      intercept[BadRequestException](
        await(
          controller.submitPrePopulatedPsr(
            postRequest.withHeaders(newHeaders = "srn" -> srn, "schemeName" -> schemeName, "userName" -> userName)
          )
        )
      )
      verify(mockPsrSubmissionService, never).submitStandardPsr(any(), any(), any(), any())(any(), any(), any())
      verify(mockPsrSubmissionService, never).submitPrePopulatedPsr(any(), any(), any(), any())(any(), any(), any())
      verify(mockAuthConnector, times(1)).authorise(any(), any())(any(), any())
      verify(mockSchemeDetailsConnector, times(1)).checkAssociation(any(), any(), any())(any(), any())
    }
  }

  "GET standard PSR" must {
    "return 200" in {
      when(mockAuthConnector.authorise[Option[String] ~ Enrolments](any(), any())(any(), any()))
        .thenReturn(
          Future.successful(new ~(Some(externalId), enrolments))
        )
      when(
        mockPsrSubmissionService.getStandardPsr(any(), any(), any(), any(), any(), any(), any())(any(), any(), any())
      ).thenReturn(Future.successful(Some(Right(samplePsrSubmission))))
      when(mockSchemeDetailsConnector.checkAssociation(any(), any(), any())(any(), any()))
        .thenReturn(Future.successful(true))

      val result = controller.getStandardPsr("testPstr", Some("fbNumber"), None, None)(
        fakeRequest.withHeaders(newHeaders = "srn" -> srn, "schemeName" -> schemeName, "userName" -> userName)
      )
      status(result) mustBe Status.OK
      verify(mockPsrSubmissionService, times(1)).getStandardPsr(any(), any(), any(), any(), any(), any(), any())(
        any(),
        any(),
        any()
      )
      verify(mockAuthConnector, times(1)).authorise(any(), any())(any(), any())
      verify(mockSchemeDetailsConnector, times(1)).checkAssociation(any(), any(), any())(any(), any())
    }

    "return 400 when required headers don't exist" in {
      intercept[BadRequestException] {
        await(controller.getStandardPsr("testPstr", None, Some("periodStartDate"), Some("psrVersion"))(fakeRequest))
      }

      verify(mockPsrSubmissionService, never).getStandardPsr(any(), any(), any(), any(), any(), any(), any())(
        any(),
        any(),
        any()
      )
      verify(mockAuthConnector, never).authorise(any(), any())(any(), any())
      verify(mockSchemeDetailsConnector, never).checkAssociation(any(), any(), any())(any(), any())
    }

    "return 404" in {
      when(mockAuthConnector.authorise[Option[String] ~ Enrolments](any(), any())(any(), any()))
        .thenReturn(
          Future.successful(new ~(Some(externalId), enrolments))
        )
      when(
        mockPsrSubmissionService.getStandardPsr(any(), any(), any(), any(), any(), any(), any())(any(), any(), any())
      ).thenReturn(Future.successful(None))
      when(mockSchemeDetailsConnector.checkAssociation(any(), any(), any())(any(), any()))
        .thenReturn(Future.successful(true))

      val result = controller.getStandardPsr("testPstr", None, Some("periodStartDate"), Some("psrVersion"))(
        fakeRequest.withHeaders(newHeaders = "srn" -> srn, "schemeName" -> schemeName, "userName" -> userName)
      )
      status(result) mustBe Status.NOT_FOUND
      verify(mockPsrSubmissionService, times(1)).getStandardPsr(any(), any(), any(), any(), any(), any(), any())(
        any(),
        any(),
        any()
      )
      verify(mockAuthConnector, times(1)).authorise(any(), any())(any(), any())
      verify(mockSchemeDetailsConnector, times(1)).checkAssociation(any(), any(), any())(any(), any())
    }

    "return 500" in {
      when(mockAuthConnector.authorise[Option[String] ~ Enrolments](any(), any())(any(), any()))
        .thenReturn(Future.successful(new ~(Some(externalId), enrolments)))
      when(
        mockPsrSubmissionService.getStandardPsr(any(), any(), any(), any(), any(), any(), any())(any(), any(), any())
      ).thenReturn(Future.successful(Some(Left(TransformerError.NoIdOrReason))))
      when(mockSchemeDetailsConnector.checkAssociation(any(), any(), any())(any(), any()))
        .thenReturn(Future.successful(true))

      val result = controller.getStandardPsr("testPstr", None, Some("periodStartDate"), Some("psrVersion"))(
        fakeRequest.withHeaders(newHeaders = "schemeName" -> schemeName, "userName" -> userName, "srn" -> srn)
      )
      status(result) mustBe Status.INTERNAL_SERVER_ERROR
      verify(mockPsrSubmissionService, times(1)).getStandardPsr(any(), any(), any(), any(), any(), any(), any())(
        any(),
        any(),
        any()
      )
      verify(mockAuthConnector, times(1)).authorise(any(), any())(any(), any())
      verify(mockSchemeDetailsConnector, times(1)).checkAssociation(any(), any(), any())(any(), any())
    }
  }
}

object PsrSubmitControllerSpec {
  val submitPsrPayload: JsValue = Json.parse(
    """
      |{
      |  "minimalRequiredSubmission": {
      |    "reportDetails": {
      |      "pstr": "00000042IN",
      |      "periodStart": "2023-04-06",
      |      "periodEnd": "2024-04-05"
      |    },
      |    "accountingPeriodDetails":  {
      |      "recordVersion" : "001",
      |      "accountingPeriods": [
      |        [
      |          "2023-04-06",
      |          "2024-04-05"
      |        ]
      |      ]
      |    },
      |    "schemeDesignatory": {
      |      "openBankAccount": true,
      |      "activeMembers": 23,
      |      "deferredMembers": 45,
      |      "pensionerMembers": 6,
      |      "totalPayments": 74
      |    }
      |  },
      |  "checkReturnDates": true,
      |  "loans": {
      |    "schemeHadLoans": true,
      |    "loanTransactions": [
      |      {
      |        "recipientIdentityType": {
      |          "identityType": "individual",
      |          "reasonNoIdNumber": "sdfsdf"
      |        },
      |        "loanRecipientName": "sdfsdfds",
      |        "connectedPartyStatus": true,
      |        "datePeriodLoanDetails": {
      |          "dateOfLoan": "2023-02-12",
      |          "loanTotalSchemeAssets": 3,
      |          "loanPeriodInMonths": 9
      |        },
      |        "loanAmountDetails": {
      |          "loanAmount": 9,
      |          "capRepaymentCY": 8,
      |          "amountOutstanding": 7
      |        },
      |        "equalInstallments": true,
      |        "loanInterestDetails": {
      |          "loanInterestAmount": 8,
      |          "loanInterestRate": 8,
      |          "intReceivedCY": 6
      |        },
      |        "optSecurityGivenDetails": "kjsdfvsd",
      |        "optOutstandingArrearsOnLoan": 273
      |      }
      |    ]
      |  },
      |  "assets": {
      |    "optLandOrProperty": {
      |      "optLandOrPropertyHeld": true,
      |      "disposeAnyLandOrProperty": true,
      |      "landOrPropertyTransactions": [
      |        {
      |          "propertyDetails": {
      |            "landOrPropertyInUK": false,
      |            "addressDetails": {
      |              "addressLine1": "Fenerbahce",
      |              "addressLine3": "Kadikoy",
      |              "town": "Istanbul",
      |              "countryCode": "TR"
      |            },
      |            "landRegistryTitleNumberKey": false,
      |            "landRegistryTitleNumberValue": "Foreign property"
      |          },
      |          "heldPropertyTransaction": {
      |            "methodOfHolding": "Acquisition",
      |            "dateOfAcquisitionOrContribution": "1953-03-28",
      |            "optPropertyAcquiredFromName": "Taylor Wonky Housing Estates Ltd",
      |            "optPropertyAcquiredFrom": {
      |              "identityType": "individual",
      |              "idNumber": "SX123456A"
      |            },
      |            "optConnectedPartyStatus": true,
      |            "totalCostOfLandOrProperty": 1000000,
      |            "optIndepValuationSupport": true,
      |            "optIsLandOrPropertyResidential": true,
      |            "landOrPropertyLeased": false,
      |            "totalIncomeOrReceipts": 25000
      |          },
      |          "optDisposedPropertyTransaction": [
      |            {
      |              "methodOfDisposal": "Sold",
      |              "optDateOfSale": "2022-10-19",
      |              "optNameOfPurchaser": "Victor Enterprises Inc.",
      |              "optPropertyAcquiredFrom": {
      |                "identityType": "ukCompany",
      |                "idNumber": "24896221"
      |              },
      |              "optSaleProceeds": 1500000,
      |              "optConnectedPartyStatus": true,
      |              "optIndepValuationSupport": false,
      |              "portionStillHeld": false
      |            }
      |          ]
      |        },
      |        {
      |          "propertyDetails": {
      |            "landOrPropertyInUK": true,
      |            "addressDetails": {
      |              "addressLine1": "Beyoglu",
      |              "addressLine2": "Ulker Arena",
      |              "addressLine3": "Kadikoy",
      |              "town": "Istanbul",
      |              "postCode": "GB135HG",
      |              "countryCode": "GB"
      |            },
      |            "landRegistryTitleNumberKey": true,
      |            "landRegistryTitleNumberValue": "LR10000102202202"
      |          },
      |          "heldPropertyTransaction": {
      |            "methodOfHolding": "Contribution",
      |            "dateOfAcquisitionOrContribution": "1953-03-28",
      |            "optPropertyAcquiredFromName": "Taylor Wonky Housing Estates Ltd.",
      |            "optPropertyAcquiredFrom": {
      |              "identityType": "individual",
      |              "idNumber": "SX123456A"
      |            },
      |            "optConnectedPartyStatus": true,
      |            "totalCostOfLandOrProperty": 1000000,
      |            "optIndepValuationSupport": false,
      |            "optIsLandOrPropertyResidential": true,
      |            "landOrPropertyLeased": false,
      |            "totalIncomeOrReceipts": 25000
      |          },
      |          "optDisposedPropertyTransaction": [
      |            {
      |              "methodOfDisposal": "Sold",
      |              "optDateOfSale": "2022-10-19",
      |              "optNameOfPurchaser": "Victor Enterprises Inc.",
      |              "optPropertyAcquiredFrom": {
      |                "identityType": "ukCompany",
      |                "idNumber": "24896221"
      |              },
      |              "optSaleProceeds": 1500000,
      |              "optConnectedPartyStatus": true,
      |              "optIndepValuationSupport": false,
      |              "portionStillHeld": false
      |            }
      |          ]
      |        },
      |        {
      |          "propertyDetails": {
      |            "landOrPropertyInUK": false,
      |            "addressDetails": {
      |              "addressLine1": "1 Hacienda Way",
      |              "addressLine3": "01055",
      |              "town": "Madrid",
      |              "countryCode": "ES"
      |            },
      |            "landRegistryTitleNumberKey": false,
      |            "landRegistryTitleNumberValue": "Foreign property"
      |          },
      |          "heldPropertyTransaction": {
      |            "methodOfHolding": "Acquisition",
      |            "dateOfAcquisitionOrContribution": "2022-12-30",
      |            "optPropertyAcquiredFromName": "Joe Sussex",
      |            "optPropertyAcquiredFrom": {
      |              "identityType": "individual",
      |              "idNumber": "SX654321A"
      |            },
      |            "optConnectedPartyStatus": false,
      |            "totalCostOfLandOrProperty": 14000000,
      |            "optIndepValuationSupport": false,
      |            "optIsLandOrPropertyResidential": false,
      |            "optLeaseDetails": {
      |              "lesseeName": "Leasee",
      |              "leaseGrantDate": "2023-01-17",
      |              "annualLeaseAmount": 500000,
      |              "connectedPartyStatus": false
      |            },
      |            "landOrPropertyLeased": true,
      |            "totalIncomeOrReceipts": 500000
      |          },
      |          "optDisposedPropertyTransaction": [
      |            {
      |              "methodOfDisposal": "Sold",
      |              "optDateOfSale": "2022-11-09",
      |              "optNameOfPurchaser": "Realty Purchasers Co.",
      |              "optPropertyAcquiredFrom": {
      |                "identityType": "ukCompany",
      |                "idNumber": "JE463863"
      |              },
      |              "optSaleProceeds": 1550000,
      |              "optConnectedPartyStatus": true,
      |              "optIndepValuationSupport": false,
      |              "portionStillHeld": true
      |            },
      |            {
      |              "methodOfDisposal": "Sold",
      |              "optDateOfSale": "2023-01-26",
      |              "optNameOfPurchaser": "ABC Company Inc.",
      |              "optPropertyAcquiredFrom": {
      |                "identityType": "ukCompany",
      |                "idNumber": "DA576257"
      |              },
      |              "optSaleProceeds": 10234.56,
      |              "optConnectedPartyStatus": false,
      |              "optIndepValuationSupport": false,
      |              "portionStillHeld": true
      |            }
      |          ]
      |        }
      |      ]
      |    },
      |    "optBorrowing": {
      |      "moneyWasBorrowed": true,
      |      "moneyBorrowed": [
      |        {
      |          "dateOfBorrow": "2022-10-18",
      |          "schemeAssetsValue": 0,
      |          "amountBorrowed": 2000,
      |          "interestRate": 5.55,
      |          "borrowingFromName": "Loans R Us",
      |          "connectedPartyStatus": false,
      |          "reasonForBorrow": "We needed the money."
      |        }
      |      ]
      |    },
      |    "optBonds": {
      |      "bondsWereAdded": true,
      |      "bondsWereDisposed": false,
      |      "bondTransactions": [
      |        {
      |          "nameOfBonds": "Xenex Bonds",
      |          "methodOfHolding": "01",
      |          "optDateOfAcqOrContrib": "2022-10-06",
      |          "costOfBonds": 10234.56,
      |          "optConnectedPartyStatus": false,
      |          "bondsUnregulated": false,
      |          "totalIncomeOrReceipts": 50,
      |          "optBondsDisposed": [
      |            {
      |              "methodOfDisposal": "Sold",
      |              "optDateSold": "2022-11-30",
      |              "optAmountReceived": 12333.59,
      |              "optBondsPurchaserName": "Happy Bond Buyers Inc.",
      |              "optConnectedPartyStatus": false,
      |              "totalNowHeld": 120
      |            },
      |            {
      |              "methodOfDisposal": "Transferred",
      |              "totalNowHeld": 12
      |            },
      |            {
      |              "methodOfDisposal": "Other",
      |              "optOtherMethod": "OtherMethod",
      |              "totalNowHeld": 10
      |            }
      |          ]
      |        },
      |        {
      |          "nameOfBonds": "Really Goods Bonds ABC",
      |          "methodOfHolding": "02",
      |          "optDateOfAcqOrContrib": "2022-07-30",
      |          "costOfBonds": 2000.5,
      |          "optConnectedPartyStatus": false,
      |          "bondsUnregulated": false,
      |          "totalIncomeOrReceipts": 300
      |        }
      |      ]
      |    },
      |    "optOtherAssets": {
      |      "otherAssetsWereHeld": true,
      |      "otherAssetsWereDisposed": false,
      |      "otherAssetTransactions": [
      |        {
      |          "assetDescription": "Test asset",
      |          "methodOfHolding": "01",
      |          "optDateOfAcqOrContrib": "2023-03-05",
      |          "costOfAsset": 12.34,
      |          "optPropertyAcquiredFromName": "test-name",
      |          "optPropertyAcquiredFrom": {
      |            "identityType": "other",
      |            "otherDescription": "description"
      |          },
      |          "optConnectedStatus": false,
      |          "optIndepValuationSupport": false,
      |          "movableSchedule29A": false,
      |          "totalIncomeOrReceipts": 34.56,
      |          "optOtherAssetDisposed": [
      |            {
      |              "methodOfDisposal": "Sold",
      |              "optDateSold": "2022-11-30",
      |              "optPurchaserName": "Acme Express Ltd.",
      |              "optPropertyAcquiredFrom": {
      |                "identityType": "individual",
      |                "idNumber": "SX123456A"
      |              },
      |              "optTotalAmountReceived": 12333.59,
      |              "optConnectedStatus": false,
      |              "optSupportedByIndepValuation": false,
      |              "anyPartAssetStillHeld": false
      |            },
      |            {
      |              "methodOfDisposal": "Transferred",
      |              "anyPartAssetStillHeld": false
      |            },
      |            {
      |              "methodOfDisposal": "Other",
      |              "optOtherMethod": "OtherMethod",
      |              "anyPartAssetStillHeld": false
      |            }
      |          ]
      |        }
      |      ]
      |    }
      |  },
      |  "membersPayments": {
      |    "employerContributionMade": false,
      |    "unallocatedContribsMade": false,
      |    "employerContributionMade": true,
      |    "transfersInMade": true,
      |    "transfersOutMade": true,
      |    "lumpSumReceived": true,
      |    "memberContributionMade": true,
      |    "pensionReceived": true,
      |    "surrenderMade": true,
      |    "memberDetails": [
      |      {
      |        "state": "New",
      |        "memberPSRVersion": "001",
      |        "personalDetails": {
      |          "firstName": "John",
      |          "lastName": "Doe",
      |          "dateOfBirth": "1990-10-10",
      |          "nino": "AB123456A"
      |        },
      |        "employerContributions": [
      |          {
      |            "employerName": "Acme Ltd",
      |            "employerType": {
      |              "employerType": "UKCompany",
      |              "value": "11108499"
      |            },
      |            "totalTransferValue": 12.34
      |          },
      |          {
      |            "employerName": "Slack Ltd",
      |            "employerType": {
      |              "employerType": "UKPartnership",
      |              "value": "A1230849"
      |            },
      |            "totalTransferValue": 102.88
      |          }
      |        ],
      |        "transfersIn": [
      |          {
      |            "schemeName": "Test pension scheme",
      |            "dateOfTransfer": "2023-02-12",
      |            "transferValue": 12.34,
      |            "transferIncludedAsset": true,
      |            "transferSchemeType": {
      |              "key": "registeredPS",
      |              "value": "88390774ZZ"
      |            }
      |          }
      |        ],
      |        "transfersOut": [
      |          {
      |            "schemeName": "Test pension scheme out",
      |            "dateOfTransfer": "2023-02-12",
      |            "transferSchemeType": {
      |              "key": "registeredPS",
      |              "value": "76509173AA"
      |            }
      |          }
      |        ],
      |        "benefitsSurrendered": {
      |          "totalSurrendered": 12.34,
      |          "dateOfSurrender": "2022-12-12",
      |          "surrenderReason": "some reason"
      |        },
      |        "pensionAmountReceived": 12.34
      |      },
      |      {
      |        "state": "Deleted",
      |        "personalDetails": {
      |          "firstName": "Jane",
      |          "lastName": "Dean",
      |          "dateOfBirth": "1995-06-01",
      |          "noNinoReason": "some reason"
      |        },
      |        "employerContributions": [
      |          {
      |            "employerName": "Test Ltd",
      |            "employerType": {
      |              "employerType": "UKCompany",
      |              "value": "67308411"
      |            },
      |            "totalTransferValue": 23.35
      |          },
      |          {
      |            "employerName": "Legal Ltd",
      |            "employerType": {
      |              "employerType": "Other",
      |              "value": "some description"
      |            },
      |            "totalTransferValue": 553.01
      |          }
      |        ],
      |        "transfersIn": [
      |          {
      |            "schemeName": "overseas pension scheme",
      |            "dateOfTransfer": "2020-10-04",
      |            "transferValue": 45.67,
      |            "transferIncludedAsset": false,
      |            "transferSchemeType": {
      |              "key": "qualifyingRecognisedOverseasPS",
      |              "value": "Q654321"
      |            }
      |          },
      |          {
      |            "schemeName": "Test pension scheme",
      |            "dateOfTransfer": "2021-08-21",
      |            "transferValue": 67.89,
      |            "transferIncludedAsset": true,
      |            "transferSchemeType": {
      |              "key": "other",
      |              "value": "other value"
      |            }
      |          }
      |        ],
      |        "transfersOut": [
      |          {
      |            "schemeName": "overseas pension scheme out",
      |            "dateOfTransfer": "2020-10-04",
      |            "transferSchemeType": {
      |              "key": "qualifyingRecognisedOverseasPS",
      |              "value": "Q000002"
      |            }
      |          },
      |          {
      |            "schemeName": "Test pension scheme out",
      |            "dateOfTransfer": "2021-08-21",
      |            "transferSchemeType": {
      |              "key": "other",
      |              "value": "other value"
      |            }
      |          }
      |        ],
      |        "benefitsSurrendered": {
      |          "totalSurrendered": 12.34,
      |          "dateOfSurrender": "2022-12-12",
      |          "surrenderReason": "some reason"
      |        },
      |        "pensionAmountReceived": 12.34
      |      }
      |    ]
      |  },
      |  "shares": {
      |    "optShareTransactions": [
      |      {
      |        "typeOfSharesHeld": "01",
      |        "shareIdentification": {
      |          "nameOfSharesCompany": "AppleSauce Inc.",
      |          "optReasonNoCRN": "Not able to locate Company on Companies House",
      |          "classOfShares": "Ordinary Shares"
      |        },
      |        "heldSharesTransaction": {
      |          "schemeHoldShare": "01",
      |          "optDateOfAcqOrContrib": "2022-10-29",
      |          "totalShares": 200,
      |          "optAcquiredFromName": "Fredd Bloggs",
      |          "optPropertyAcquiredFrom": {
      |            "identityType": "individual",
      |            "idNumber": "JE123176A"
      |          },
      |          "optConnectedPartyStatus": false,
      |          "costOfShares": 10000,
      |          "supportedByIndepValuation": true,
      |          "optTotalAssetValue": 2000,
      |          "totalDividendsOrReceipts": 500
      |        },
      |        "optDisposedSharesTransaction": [
      |          {
      |            "methodOfDisposal": "Sold",
      |            "optSalesQuestions": {
      |              "dateOfSale": "2023-04-06",
      |              "noOfSharesSold": 4,
      |              "amountReceived": 38.3,
      |              "nameOfPurchaser": "nameOfPurchaser",
      |              "purchaserType": {
      |                "identityType": "individual",
      |                "reasonNoIdNumber": "sdfsdf"
      |              },
      |              "connectedPartyStatus": false,
      |              "supportedByIndepValuation": true
      |            },
      |            "totalSharesNowHeld": 1
      |          },
      |          {
      |            "methodOfDisposal": "Redeemed",
      |            "optRedemptionQuestions": {
      |              "dateOfRedemption": "2023-03-07",
      |              "noOfSharesRedeemed": 27,
      |              "amountReceived": 1907
      |            },
      |            "totalSharesNowHeld": 1
      |          }
      |        ]
      |      },
      |      {
      |        "typeOfSharesHeld": "03",
      |        "shareIdentification": {
      |          "nameOfSharesCompany": "Pear Computers Inc.",
      |          "optCrnNumber": "LP289157",
      |          "classOfShares": "Preferred Shares"
      |        },
      |        "heldSharesTransaction": {
      |          "schemeHoldShare": "01",
      |          "optDateOfAcqOrContrib": "2023-02-23",
      |          "totalShares": 10000,
      |          "optAcquiredFromName": "Golden Investments Ltd.",
      |          "optPropertyAcquiredFrom": {
      |            "identityType": "ukPartnership",
      |            "idNumber": "28130262"
      |          },
      |          "optConnectedPartyStatus": false,
      |          "costOfShares": 50000,
      |          "supportedByIndepValuation": true,
      |          "optTotalAssetValue": 40000,
      |          "totalDividendsOrReceipts": 200
      |        },
      |        "optDisposedSharesTransaction": [
      |          {
      |            "methodOfDisposal": "Transferred",
      |            "totalSharesNowHeld": 48
      |          },
      |          {
      |            "methodOfDisposal": "Other",
      |            "totalSharesNowHeld": 27
      |          }
      |        ]
      |      },
      |      {
      |        "typeOfSharesHeld": "03",
      |        "shareIdentification": {
      |          "nameOfSharesCompany": "Connected Party Inc.",
      |          "optCrnNumber": "LP289157",
      |          "classOfShares": "Convertible Preference Shares"
      |        },
      |        "heldSharesTransaction": {
      |          "schemeHoldShare": "02",
      |          "optDateOfAcqOrContrib": "2023-02-23",
      |          "totalShares": 1000,
      |          "optAcquiredFromName": "Investec Inc.",
      |          "optPropertyAcquiredFrom": {
      |            "identityType": "ukCompany",
      |            "idNumber": "0000123456"
      |          },
      |          "optConnectedPartyStatus": false,
      |          "costOfShares": 120220.34,
      |          "supportedByIndepValuation": true,
      |          "optTotalAssetValue": 10000,
      |          "totalDividendsOrReceipts": 599.99
      |        }
      |      }
      |    ],
      |    "optTotalValueQuotedShares": 12.34
      |  },
      |  "psrDeclaration": {
      |    "submittedBy": "PSP",
      |    "submitterId": "21000005",
      |    "optAuthorisingPSAID": "A2100005",
      |    "declaration1": true,
      |    "declaration2": true
      |  }
      |}
      |""".stripMargin
  )
}
