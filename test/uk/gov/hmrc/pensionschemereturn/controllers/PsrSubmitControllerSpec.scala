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
import utils.TestValues

import scala.concurrent.Future

class PsrSubmitControllerSpec extends SpecBase with MockitoSugar with BeforeAndAfter with TestValues {

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
          |    "minimalRequiredSubmission": {
          |        "reportDetails": {
          |            "pstr": "00000042IN",
          |            "periodStart": "2023-04-06",
          |            "periodEnd": "2024-04-05"
          |        },
          |        "accountingPeriods": [
          |            [
          |                "2023-04-06",
          |                "2024-04-05"
          |            ]
          |        ],
          |        "schemeDesignatory": {
          |            "openBankAccount": true,
          |            "activeMembers": 23,
          |            "deferredMembers": 45,
          |            "pensionerMembers": 6,
          |            "totalPayments": 74
          |        }
          |    },
          |    "checkReturnDates": true,
          |    "loans": {
          |        "schemeHadLoans": true,
          |        "loanTransactions": [
          |            {
          |                "recipientIdentityType": {
          |                    "identityType": "individual",
          |                    "reasonNoIdNumber": "sdfsdf"
          |                },
          |                "loanRecipientName": "sdfsdfds",
          |                "connectedPartyStatus": true,
          |                "datePeriodLoanDetails": {
          |                    "dateOfLoan": "2023-02-12",
          |                    "loanTotalSchemeAssets": 3,
          |                    "loanPeriodInMonths": 9
          |                },
          |                "loanAmountDetails": {
          |                    "loanAmount": 9,
          |                    "capRepaymentCY": 8,
          |                    "amountOutstanding": 7
          |                },
          |                "equalInstallments": true,
          |                "loanInterestDetails": {
          |                    "loanInterestAmount": 8,
          |                    "loanInterestRate": 8,
          |                    "intReceivedCY": 6
          |                },
          |                "optSecurityGivenDetails": "kjsdfvsd",
          |                "optOutstandingArrearsOnLoan": 273
          |            }
          |        ]
          |    },
          |    "assets": {
          |        "landOrProperty": {
          |            "landOrPropertyHeld": true,
          |            "disposeAnyLandOrProperty": true,
          |            "landOrPropertyTransactions": [
          |                {
          |                    "propertyDetails": {
          |                        "landOrPropertyInUK": true,
          |                        "addressDetails": {
          |                            "addressLine1": "Flat 2",
          |                            "addressLine2": "7 Other Place",
          |                            "addressLine3": "Some District",
          |                            "town": "Anytown",
          |                            "postCode": "ZZ1 1ZZ",
          |                            "country": "United Kingdom",
          |                            "countryCode": "GB"
          |                        },
          |                        "landRegistryTitleNumberKey": true,
          |                        "landRegistryTitleNumberValue": "MS123456"
          |                    },
          |                    "heldPropertyTransaction": {
          |                        "methodOfHolding": "Acquisition",
          |                        "dateOfAcquisitionOrContribution": "2009-03-01",
          |                        "optPropertyAcquiredFromName": "Seller-Test",
          |                        "optPropertyAcquiredFrom": {
          |                            "identityType": "individual",
          |                            "idNumber": "SX123456D"
          |                        },
          |                        "optConnectedPartyStatus": true,
          |                        "totalCostOfLandOrProperty": 87,
          |                        "optIndepValuationSupport": true,
          |                        "isLandOrPropertyResidential": true,
          |                        "optLeaseDetails": {
          |                            "lesseeName": "Lessee-Test",
          |                            "leaseGrantDate": "2008-07-06",
          |                            "annualLeaseAmount": 98,
          |                            "connectedPartyStatus": false
          |                        },
          |                        "landOrPropertyLeased": true,
          |                        "totalIncomeOrReceipts": 90
          |                    },
          |                    "optDisposedPropertyTransaction": [
          |                        {
          |                            "methodOfDisposal": "Sold",
          |                            "optDateOfSale": "2009-03-01",
          |                            "optNameOfPurchaser": "Purchaser-Test",
          |                            "optPropertyAcquiredFrom": {
          |                                "identityType": "individual",
          |                                "idNumber": "SX123456D"
          |                            },
          |                            "optSaleProceeds": 1907,
          |                            "optConnectedPartyStatus": true,
          |                            "optIndepValuationSupport": false,
          |                            "portionStillHeld": true
          |                        }
          |                    ]
          |                }
          |            ]
          |        },
          |        "borrowing": {
          |            "moneyWasBorrowed": true,
          |            "moneyBorrowed": [
          |                {
          |                    "dateOfBorrow": "2023-10-19",
          |                    "amountBorrowed": 2,
          |                    "schemeAssetsValue": 3,
          |                    "interestRate": 4,
          |                    "borrowingFromName": "borrowingFromName",
          |                    "connectedPartyStatus": true,
          |                    "reasonForBorrow": "reasonForBorrow"
          |                }
          |            ]
          |        }
          |    },
          |    "membersPayments": {
          |        "employerContributionsDetails": {
          |          "made": true,
          |          "completed": true
          |        },
          |        "unallocatedContribsMade": false,
          |        "employerContributionsCompleted": true,
          |        "transfersInCompleted": true,
          |        "transfersOutCompleted": true,
          |        "lumpSumReceived": true,
          |        "memberContributionMade": true,
          |        "pensionReceived": true,
          |        "benefitsSurrenderedDetails": {
          |            "made": true,
          |            "completed": true
          |        },
          |        "memberDetails": [
          |            {
          |                "personalDetails": {
          |                    "firstName": "John",
          |                    "lastName": "Doe",
          |                    "dateOfBirth": "1990-10-10",
          |                    "nino": "AB123456A"
          |                },
          |                "employerContributions": [
          |                    {
          |                        "employerName": "Acme Ltd",
          |                        "employerType": {
          |                            "employerType": "UKCompany",
          |                            "value": "11108499"
          |                        },
          |                        "totalTransferValue": 12.34
          |                    },
          |                    {
          |                        "employerName": "Slack Ltd",
          |                        "employerType": {
          |                            "employerType": "UKPartnership",
          |                            "value": "A1230849"
          |                        },
          |                        "totalTransferValue": 102.88
          |                    }
          |                ],
          |                "transfersIn": [
          |                    {
          |                        "schemeName": "Test pension scheme",
          |                        "dateOfTransfer": "2023-02-12",
          |                        "transferValue": 12.34,
          |                        "transferIncludedAsset": true,
          |                        "transferSchemeType": {
          |                            "key": "registeredPS",
          |                            "value": "88390774ZZ"
          |                        }
          |                    }
          |                ],
          |                "transfersOut": [
          |                    {
          |                        "schemeName": "Test pension scheme out",
          |                        "dateOfTransfer": "2023-02-12",
          |                        "transferSchemeType": {
          |                            "key": "registeredPS",
          |                            "value": "76509173AA"
          |                        }
          |                    }
          |                ],
          |                "benefitsSurrendered": {
          |                    "totalSurrendered": 12.34,
          |                    "dateOfSurrender": "2022-12-12",
          |                    "surrenderReason": "some reason"
          |                },
          |                "pensionAmountReceived": 12.34
          |            },
          |            {
          |                "personalDetails": {
          |                    "firstName": "Jane",
          |                    "lastName": "Dean",
          |                    "dateOfBirth": "1995-06-01",
          |                    "noNinoReason": "some reason"
          |                },
          |                "employerContributions": [
          |                    {
          |                        "employerName": "Test Ltd",
          |                        "employerType": {
          |                            "employerType": "UKCompany",
          |                            "value": "67308411"
          |                        },
          |                        "totalTransferValue": 23.35
          |                    },
          |                    {
          |                        "employerName": "Legal Ltd",
          |                        "employerType": {
          |                            "employerType": "Other",
          |                            "value": "some description"
          |                        },
          |                        "totalTransferValue": 553.01
          |                    }
          |                ],
          |                "transfersIn": [
          |                    {
          |                        "schemeName": "overseas pension scheme",
          |                        "dateOfTransfer": "2020-10-04",
          |                        "transferValue": 45.67,
          |                        "transferIncludedAsset": false,
          |                        "transferSchemeType": {
          |                            "key": "qualifyingRecognisedOverseasPS",
          |                            "value": "Q654321"
          |                        }
          |                    },
          |                    {
          |                        "schemeName": "Test pension scheme",
          |                        "dateOfTransfer": "2021-08-21",
          |                        "transferValue": 67.89,
          |                        "transferIncludedAsset": true,
          |                        "transferSchemeType": {
          |                            "key": "other",
          |                            "value": "other value"
          |                        }
          |                    }
          |                ],
          |                "transfersOut": [
          |                    {
          |                        "schemeName": "overseas pension scheme out",
          |                        "dateOfTransfer": "2020-10-04",
          |                        "transferSchemeType": {
          |                            "key": "qualifyingRecognisedOverseasPS",
          |                            "value": "Q000002"
          |                        }
          |                    },
          |                    {
          |                        "schemeName": "Test pension scheme out",
          |                        "dateOfTransfer": "2021-08-21",
          |                        "transferSchemeType": {
          |                            "key": "other",
          |                            "value": "other value"
          |                        }
          |                    }
          |                ],
          |                "benefitsSurrendered": {
          |                    "totalSurrendered": 12.34,
          |                    "dateOfSurrender": "2022-12-12",
          |                    "surrenderReason": "some reason"
          |                },
          |                "pensionAmountReceived": 12.34
          |            }
          |        ]
          |    },
          |    "shares": {
          |        "optShareTransactions": [
          |            {
          |                "typeOfSharesHeld": "01",
          |                "shareIdentification": {
          |                    "nameOfSharesCompany": "AppleSauce Inc.",
          |                    "optReasonNoCRN": "Not able to locate Company on Companies House",
          |                    "classOfShares": "Ordinary Shares"
          |                },
          |                "heldSharesTransaction": {
          |                    "schemeHoldShare": "01",
          |                    "optDateOfAcqOrContrib": "2022-10-29",
          |                    "totalShares": 200,
          |                    "optAcquiredFromName": "Fredd Bloggs",
          |                    "optPropertyAcquiredFrom": {
          |                        "identityType": "individual",
          |                        "idNumber": "JE123176A"
          |                    },
          |                    "optConnectedPartyStatus": false,
          |                    "costOfShares": 10000,
          |                    "supportedByIndepValuation": true,
          |                    "optTotalAssetValue": 2000,
          |                    "totalDividendsOrReceipts": 500
          |                }
          |            },
          |            {
          |                "typeOfSharesHeld": "03",
          |                "shareIdentification": {
          |                    "nameOfSharesCompany": "Pear Computers Inc.",
          |                    "optCrnNumber": "LP289157",
          |                    "classOfShares": "Preferred Shares"
          |                },
          |                "heldSharesTransaction": {
          |                    "schemeHoldShare": "01",
          |                    "optDateOfAcqOrContrib": "2023-02-23",
          |                    "totalShares": 10000,
          |                    "optAcquiredFromName": "Golden Investments Ltd.",
          |                    "optPropertyAcquiredFrom": {
          |                        "identityType": "ukPartnership",
          |                        "idNumber": "28130262"
          |                    },
          |                    "optConnectedPartyStatus": false,
          |                    "costOfShares": 50000,
          |                    "supportedByIndepValuation": true,
          |                    "optTotalAssetValue": 40000,
          |                    "totalDividendsOrReceipts": 200
          |                }
          |            },
          |            {
          |                "typeOfSharesHeld": "03",
          |                "shareIdentification": {
          |                    "nameOfSharesCompany": "Connected Party Inc.",
          |                    "optCrnNumber": "LP289157",
          |                    "classOfShares": "Convertible Preference Shares"
          |                },
          |                "heldSharesTransaction": {
          |                    "schemeHoldShare": "02",
          |                    "optDateOfAcqOrContrib": "2023-02-23",
          |                    "totalShares": 1000,
          |                    "optAcquiredFromName": "Investec Inc.",
          |                    "optPropertyAcquiredFrom": {
          |                        "identityType": "ukCompany",
          |                        "idNumber": "0000123456"
          |                    },
          |                    "optConnectedPartyStatus": false,
          |                    "costOfShares": 120220.34,
          |                    "supportedByIndepValuation": true,
          |                    "optTotalAssetValue": 10000,
          |                    "totalDividendsOrReceipts": 599.99
          |                }
          |            }
          |        ]
          |    }
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

      when(mockPsrSubmissionService.getStandardPsr(any(), any(), any(), any())(any(), any()))
        .thenReturn(Future.successful(Some(Right(samplePsrSubmission))))

      val result = controller.getStandardPsr("testPstr", Some("fbNumber"), None, None)(fakeRequest)
      status(result) mustBe Status.OK
    }

    "return 404" in {

      when(mockPsrSubmissionService.getStandardPsr(any(), any(), any(), any())(any(), any()))
        .thenReturn(Future.successful(None))

      val result = controller.getStandardPsr("testPstr", None, Some("periodStartDate"), Some("psrVersion"))(fakeRequest)
      status(result) mustBe Status.NOT_FOUND
    }
  }
}
