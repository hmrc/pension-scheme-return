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

import org.mockito.ArgumentMatchers.any
import org.mockito.MockitoSugar
import org.scalatest.BeforeAndAfter
import play.api.Application
import play.api.http.Status
import play.api.inject.bind
import play.api.inject.guice.{GuiceApplicationBuilder, GuiceableModule}
import play.api.libs.json.{JsObject, JsValue, Json}
import play.api.test.FakeRequest
import play.api.test.Helpers._
import uk.gov.hmrc.http.{HeaderCarrier, HttpResponse}
import uk.gov.hmrc.pensionschemereturn.base.SpecBase
import uk.gov.hmrc.pensionschemereturn.services.PsrSubmissionService

import scala.concurrent.Future

class PsrSubmitControllerSpec extends SpecBase with MockitoSugar with BeforeAndAfter {

  implicit val hc: HeaderCarrier = HeaderCarrier()
  private val fakeRequest = FakeRequest("POST", "/")
  private val mockPsrSubmissionService = mock[PsrSubmissionService]
  val modules: Seq[GuiceableModule] =
    Seq(
      bind[PsrSubmissionService].toInstance(mockPsrSubmissionService)
    )

  val application: Application = new GuiceApplicationBuilder()
    .configure(conf = "auditing.enabled" -> false, "metrics.enabled" -> false, "metrics.jvm" -> false)
    .overrides(modules: _*)
    .build()

  private val controller = application.injector.instanceOf[PsrSubmitController]

  "POST standard PSR" must {
    "return 204" in {
      val responseJson: JsObject = Json.obj("mock" -> "pass")

      when(mockPsrSubmissionService.submitStandardPsr(any())(any(), any(), any()))
        .thenReturn(Future.successful(HttpResponse(OK, responseJson.toString)))

      val requestJson: JsValue = Json.parse(
        """
          |{
          |  "minimalRequiredSubmission": {
          |    "reportDetails": {
          |      "pstr": "00000042IN",
          |      "periodStart": "2024-04-05",
          |      "periodEnd": "2023-04-06"
          |    },
          |    "accountingPeriods": [
          |      [
          |        "2023-04-06",
          |        "2024-04-05"
          |      ]
          |    ],
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
          |        "optConnectedPartyStatus": true,
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
          |  }
          |}
          |""".stripMargin
      )
      val postRequest = fakeRequest.withJsonBody(requestJson)
      val result = controller.submitStandardPsr(postRequest)
      status(result) mustBe Status.NO_CONTENT
    }
  }

  "GET standard PSR" must {
    "return 200" in {
      val responseJson: JsObject = Json.obj("mock" -> "pass")

      when(mockPsrSubmissionService.getStandardPsr(any(), any(), any(), any())(any(), any(), any()))
        .thenReturn(Future.successful(Some(responseJson)))

      val result = controller.getStandardPsr("testPstr", Some("fbNumber"), None, None)(fakeRequest)
      status(result) mustBe Status.OK
    }

    "return 404" in {

      when(mockPsrSubmissionService.getStandardPsr(any(), any(), any(), any())(any(), any(), any()))
        .thenReturn(Future.successful(None))

      val result = controller.getStandardPsr("testPstr", None, Some("periodStartDate"), Some("psrVersion"))(fakeRequest)
      status(result) mustBe Status.NOT_FOUND
    }
  }
}
