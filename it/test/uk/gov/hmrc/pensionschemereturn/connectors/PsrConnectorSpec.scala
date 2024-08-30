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

package uk.gov.hmrc.pensionschemereturn.connectors

import com.github.tomakehurst.wiremock.client.WireMock
import com.github.tomakehurst.wiremock.client.WireMock._
import org.mockito.ArgumentMatchers.any
import org.mockito.Mockito.times
import org.mockito.{ArgumentMatchers, Mockito}
import org.scalatest.time.{Millis, Seconds, Span}
import org.scalatestplus.mockito.MockitoSugar.mock
import play.api.Application
import play.api.http.Status.{BAD_REQUEST, OK}
import play.api.inject.bind
import play.api.inject.guice.{GuiceApplicationBuilder, GuiceableModule}
import play.api.libs.json.{JsObject, Json}
import play.api.mvc.RequestHeader
import play.api.test.FakeRequest
import play.api.test.Helpers.{await, defaultAwaitTimeout}
import uk.gov.hmrc.http._
import uk.gov.hmrc.pensionschemereturn.BaseConnectorSpec
import uk.gov.hmrc.pensionschemereturn.audit.ApiAuditUtil
import uk.gov.hmrc.pensionschemereturn.connectors.PsrConnectorSpec._
import uk.gov.hmrc.pensionschemereturn.models.response._
import utils.TestValues

import scala.concurrent.ExecutionContext.Implicits.global

class PsrConnectorSpec extends BaseConnectorSpec with TestValues {

  private implicit lazy val hc: HeaderCarrier = HeaderCarrier()
  private implicit lazy val rh: RequestHeader = FakeRequest("", "")

  private val mockApiAuditUtil: ApiAuditUtil = mock[ApiAuditUtil]

  override def beforeEach(): Unit = {
    super.beforeEach()
    Mockito.reset(mockApiAuditUtil)
  }

  override implicit val patienceConfig: PatienceConfig =
    PatienceConfig(timeout = scaled(Span(5, Seconds)), interval = scaled(Span(50, Millis)))

  val modules: Seq[GuiceableModule] =
    Seq(
      bind[ApiAuditUtil].toInstance(mockApiAuditUtil)
    )
  val app: Application = new GuiceApplicationBuilder()
    .overrides(modules: _*)
    .configure("microservice.services.if-hod.port" -> wireMockPort)
    .build()

  private def createJsonObject(msg: String = "Sample Response"): JsObject =
    Json.obj(
      "msg" -> msg
    )

  private lazy val connector: PsrConnector = app.injector.instanceOf[PsrConnector]

  "getOverview" should {
    val getOverview = s"/pension-online/reports/overview/pods/$pstr/PSR"

    "return overview details when returns were found" in {

      stubGet(
        getOverview,
        Map(
          "fromDate" -> sampleToday.toString,
          "toDate" -> sampleToday.toString
        ),
        ok(sampleOverviewResponseAsJsonString)
      )

      whenReady(connector.getOverview(pstr, sampleToday.toString, sampleToday.toString)) {
        result: Seq[PsrOverviewEtmpResponse] =>
          WireMock.verify(
            getRequestedFor(
              urlEqualTo("/pension-online/reports/overview/pods/testPstr/PSR?fromDate=2023-10-19&toDate=2023-10-19")
            )
          )

          result shouldMatchTo sampleOverviewResponse
      }
    }

    "return empty list when pstr not found in etmp" in {

      stubGet(
        getOverview,
        Map(
          "fromDate" -> sampleToday.toString,
          "toDate" -> sampleToday.toString
        ),
        notFound().withBody(
          errorResponse(
            "NO_REPORT_FOUND",
            "The remote endpoint has indicated No Scheme report was found for the given period."
          )
        )
      )

      whenReady(connector.getOverview(pstr, sampleToday.toString, sampleToday.toString)) {
        result: Seq[PsrOverviewEtmpResponse] =>
          WireMock.verify(
            getRequestedFor(
              urlEqualTo(
                "/pension-online/reports/overview/pods/testPstr/PSR?fromDate=2023-10-19&toDate=2023-10-19"
              )
            )
          )
          result mustBe Seq.empty
      }
    }

    "return 400 BadRequest when missing parameters" in {

      stubGet(
        getOverview,
        Map(
          "fromDate" -> sampleToday.toString,
          "toDate" -> ""
        ),
        badRequest().withBody(
          errorResponse(
            "MISSING_TO_DATE",
            "Submission has not passed validation. Required query parameter toDate has not been supplied."
          )
        )
      )

      val thrown = intercept[BadRequestException] {
        await(connector.getOverview(pstr, sampleToday.toString, ""))
      }
      WireMock.verify(
        getRequestedFor(
          urlEqualTo(
            "/pension-online/reports/overview/pods/testPstr/PSR?fromDate=2023-10-19&toDate="
          )
        )
      )
      thrown.message must include(
        "'{\"failures\":[{\"code\":\"MISSING_TO_DATE\",\"reason\":\"Submission has not passed validation. Required query parameter toDate has not been supplied.\"}]}'"
      )
    }

    "return 403 Forbidden when invalid date range" in {

      stubGet(
        getOverview,
        Map(
          "fromDate" -> sampleToday.toString,
          "toDate" -> sampleToday.toString
        ),
        forbidden().withBody(
          errorResponse(
            "FROM_DATE_NOT_IN_RANGE",
            "The remote endpoint has indicated From Date cannot be in the future."
          )
        )
      )

      val thrown = intercept[UpstreamErrorResponse] {
        await(connector.getOverview(pstr, sampleToday.toString, sampleToday.toString))
      }
      WireMock.verify(
        getRequestedFor(
          urlEqualTo(
            "/pension-online/reports/overview/pods/testPstr/PSR?fromDate=2023-10-19&toDate=2023-10-19"
          )
        )
      )
      thrown.message must include(
        "'{\"failures\":[{\"code\":\"FROM_DATE_NOT_IN_RANGE\",\"reason\":\"The remote endpoint has indicated From Date cannot be in the future.\"}]}'"
      )
    }
  }

  "getVersions" should {
    val getVersionsUrl = s"/pension-online/reports/$pstr/PSR/versions"

    "return reporting version details when returns were found" in {
      stubGet(
        getVersionsUrl,
        Map(
          "startDate" -> sampleToday.toString
        ),
        ok(sampleVersionsResponseAsJsonString)
      )

      whenReady(connector.getVersions(pstr, sampleToday.toString)) { result: Seq[PsrVersionsEtmpResponse] =>
        WireMock.verify(
          getRequestedFor(
            urlEqualTo("/pension-online/reports/testPstr/PSR/versions?startDate=2023-10-19")
          )
        )

        result mustBe sampleVersionsResponse
      }
    }

    "return empty list when pstr not found in etmp" in {

      stubGet(
        getVersionsUrl,
        Map(
          "startDate" -> sampleToday.toString
        ),
        notFound().withBody(
          errorResponse(
            "NO_DATA_FOUND",
            "The remote endpoint has indicated that no scheme report was found for the given period."
          )
        )
      )

      whenReady(connector.getVersions(pstr, sampleToday.toString)) { result: Seq[PsrVersionsEtmpResponse] =>
        WireMock.verify(
          getRequestedFor(
            urlEqualTo(
              "/pension-online/reports/testPstr/PSR/versions?startDate=2023-10-19"
            )
          )
        )
        result mustBe Seq.empty
      }
    }

    "return 400 BadRequest when invalid pstr - versions" in {

      stubGet(
        getVersionsUrl,
        Map(
          "startDate" -> sampleToday.toString
        ),
        badRequest().withBody(
          errorResponse("INVALID_PSTR", "Submission has not passed validation. Invalid parameter pstr.")
        )
      )

      val thrown = intercept[BadRequestException] {
        await(connector.getVersions(pstr, sampleToday.toString))
      }
      WireMock.verify(
        getRequestedFor(
          urlEqualTo(
            "/pension-online/reports/testPstr/PSR/versions?startDate=2023-10-19"
          )
        )
      )
      thrown.message must include(
        "'{\"failures\":[{\"code\":\"INVALID_PSTR\",\"reason\":\"Submission has not passed validation. Invalid parameter pstr.\"}]}'"
      )
    }

    "return 403 Forbidden when invalid date - versions" in {

      stubGet(
        getVersionsUrl,
        Map(
          "startDate" -> sampleToday.toString
        ),
        forbidden().withBody(
          errorResponse(
            "PERIOD_START_DATE_NOT_IN_RANGE",
            "The remote endpoint has indicated that Period Start Date cannot be in the future."
          )
        )
      )

      val thrown = intercept[UpstreamErrorResponse] {
        await(connector.getVersions(pstr, sampleToday.toString))
      }
      WireMock.verify(
        getRequestedFor(
          urlEqualTo(
            "/pension-online/reports/testPstr/PSR/versions?startDate=2023-10-19"
          )
        )
      )
      thrown.message must include(
        "'{\"failures\":[{\"code\":\"PERIOD_START_DATE_NOT_IN_RANGE\",\"reason\":\"The remote endpoint has indicated that Period Start Date cannot be in the future.\"}]}'"
      )
    }
  }

  "submitStandardPsr" should {
    val submitStandardPsrDetailsUrl = s"/pension-online/scheme-return/$pstr"
    "return 200 - ok" in {
      val jsObject = createJsonObject()
      stubPost(
        submitStandardPsrDetailsUrl,
        Json.stringify(jsObject),
        ok()
      )

      whenReady(
        connector.submitStandardPsr(pstr, jsObject, schemeName, psaPspId, credentialRole, userName, cipPsrStatus)
      ) { result: HttpResponse =>
        WireMock.verify(
          postRequestedFor(urlEqualTo("/pension-online/scheme-return/testPstr"))
        )
        Mockito
          .verify(mockApiAuditUtil, times(1))
          .firePsrPostAuditEvent(
            ArgumentMatchers.eq(pstr),
            ArgumentMatchers.eq(jsObject),
            ArgumentMatchers.eq(schemeName),
            ArgumentMatchers.eq(credentialRole),
            ArgumentMatchers.eq(psaPspId),
            ArgumentMatchers.eq(userName),
            ArgumentMatchers.eq(cipPsrStatus)
          )(any(), any())
        result.status mustBe OK
      }
    }
  }

  "getStandardPsr" should {

    val getStandardPsrDetailsUrl = s"/pension-online/scheme-return/$pstr"

    "return a standard PSR value with only fbNumber" in {

      stubGet(
        getStandardPsrDetailsUrl,
        Map(
          "psrFormBundleNumber" -> psrFormBundleNumber
        ),
        ok(sampleStandardPsrResponseAsJsonString)
          .withHeader("Content-Type", "application/json")
      )

      whenReady(
        connector
          .getStandardPsr(pstr, Some(psrFormBundleNumber), None, None, schemeName, psaPspId, credentialRole, userName)
      ) { result: Option[PsrSubmissionEtmpResponse] =>
        WireMock.verify(
          getRequestedFor(urlEqualTo("/pension-online/scheme-return/testPstr?psrFormBundleNumber=1234567890"))
        )
        Mockito
          .verify(mockApiAuditUtil, times(1))
          .firePsrGetAuditEvent(
            ArgumentMatchers.eq(pstr),
            ArgumentMatchers.eq(Some(psrFormBundleNumber)),
            ArgumentMatchers.eq(None),
            ArgumentMatchers.eq(None),
            ArgumentMatchers.eq(credentialRole),
            ArgumentMatchers.eq(psaPspId),
            ArgumentMatchers.eq(userName),
            ArgumentMatchers.eq(schemeName)
          )(any(), any())
        result shouldMatchTo Some(samplePsrSubmissionEtmpResponse)
      }
    }

    "return a standard PSR value with periodStartDate and psrVersion" in {

      stubGet(
        getStandardPsrDetailsUrl,
        Map(
          "periodStartDate" -> sampleToday.toString,
          "psrVersion" -> psrVersion
        ),
        ok(sampleStandardPsrResponseAsJsonString)
          .withHeader("Content-Type", "application/json")
      )

      whenReady(
        connector.getStandardPsr(
          pstr,
          None,
          Some(sampleToday.toString),
          Some(psrVersion),
          schemeName,
          psaPspId,
          credentialRole,
          userName
        )
      ) { result: Option[PsrSubmissionEtmpResponse] =>
        WireMock.verify(
          getRequestedFor(
            urlEqualTo(
              "/pension-online/scheme-return/testPstr?periodStartDate=2023-10-19&psrVersion=001"
            )
          )
        )
        Mockito
          .verify(mockApiAuditUtil, times(1))
          .firePsrGetAuditEvent(
            ArgumentMatchers.eq(pstr),
            ArgumentMatchers.eq(None),
            ArgumentMatchers.eq(Some(sampleToday.toString)),
            ArgumentMatchers.eq(Some(psrVersion)),
            ArgumentMatchers.eq(credentialRole),
            ArgumentMatchers.eq(psaPspId),
            ArgumentMatchers.eq(userName),
            ArgumentMatchers.eq(schemeName)
          )(any(), any())
        result shouldMatchTo Some(samplePsrSubmissionEtmpResponse)
      }
    }

    "return 422 (PSR_NOT_FOUND) when pstr not found in etmp" in {

      stubGet(
        "/pension-online/scheme-return/notFoundTestPstr?periodStartDate=2023-10-19&psrVersion=001",
        badRequestEntity().withBody(
          errorResponse("PSR_NOT_FOUND", "The remote endpoint has indicated no PSR found for requested details. ")
        )
      )

      whenReady(
        connector.getStandardPsr(
          "notFoundTestPstr",
          None,
          Some(sampleToday.toString),
          Some(psrVersion),
          schemeName,
          psaPspId,
          credentialRole,
          userName
        )
      ) { result: Option[_] =>
        WireMock.verify(
          getRequestedFor(
            urlEqualTo(
              "/pension-online/scheme-return/notFoundTestPstr?periodStartDate=2023-10-19&psrVersion=001"
            )
          )
        )
        Mockito
          .verify(mockApiAuditUtil, times(1))
          .firePsrGetAuditEvent(
            ArgumentMatchers.eq("notFoundTestPstr"),
            ArgumentMatchers.eq(None),
            ArgumentMatchers.eq(Some(sampleToday.toString)),
            ArgumentMatchers.eq(Some(psrVersion)),
            ArgumentMatchers.eq(credentialRole),
            ArgumentMatchers.eq(psaPspId),
            ArgumentMatchers.eq(userName),
            ArgumentMatchers.eq(schemeName)
          )(any(), any())
        result mustBe None
      }
    }

    "return 400 BadRequest when etmp returns badRequest" in {

      stubGet(
        "/pension-online/scheme-return/invalidTestPstr?periodStartDate=2023-10-19&psrVersion=001",
        badRequest().withBody("INVALID_PAYLOAD")
      )

      val thrown = intercept[BadRequestException] {
        await(
          connector.getStandardPsr(
            "invalidTestPstr",
            None,
            Some(sampleToday.toString),
            Some(psrVersion),
            schemeName,
            psaPspId,
            credentialRole,
            userName
          )
        )
      }
      WireMock.verify(
        getRequestedFor(
          urlEqualTo(
            "/pension-online/scheme-return/invalidTestPstr?periodStartDate=2023-10-19&psrVersion=001"
          )
        )
      )
      Mockito
        .verify(mockApiAuditUtil, times(1))
        .firePsrGetAuditEvent(
          ArgumentMatchers.eq("invalidTestPstr"),
          ArgumentMatchers.eq(None),
          ArgumentMatchers.eq(Some(sampleToday.toString)),
          ArgumentMatchers.eq(Some(psrVersion)),
          ArgumentMatchers.eq(credentialRole),
          ArgumentMatchers.eq(psaPspId),
          ArgumentMatchers.eq(userName),
          ArgumentMatchers.eq(schemeName)
        )(any(), any())
      thrown.responseCode mustBe BAD_REQUEST
      thrown.message must include(s"Response body 'INVALID_PAYLOAD'")

    }

    "return 400 BadRequest when missing parameters" in {

      val thrown = intercept[BadRequestException] {
        await(connector.getStandardPsr(pstr, None, None, None, schemeName, psaPspId, credentialRole, userName))
      }

      Mockito
        .verify(mockApiAuditUtil, Mockito.never)
        .firePsrGetAuditEvent(
          any(),
          any(),
          any(),
          any(),
          any(),
          any(),
          any(),
          any()
        )(any(), any())
      thrown.responseCode mustBe BAD_REQUEST
      thrown.message mustEqual "Missing url parameters"
    }
  }
}

object PsrConnectorSpec {
  val sampleOverviewResponseAsJsonString: String =
    """
      |[
      |    {
      |        "periodStartDate": "2022-04-06",
      |        "periodEndDate": "2023-04-05",
      |        "numberOfVersions": 1,
      |        "submittedVersionAvailable": "No",
      |        "compiledVersionAvailable": "Yes",
      |        "ntfDateOfIssue": "2022-12-06",
      |        "psrDueDate": "2023-03-31",
      |        "psrReportType": "Standard"
      |    },
      |    {
      |        "periodStartDate": "2021-04-06",
      |        "periodEndDate": "2022-04-05",
      |        "numberOfVersions": 2,
      |        "submittedVersionAvailable": "Yes",
      |        "compiledVersionAvailable": "Yes",
      |        "ntfDateOfIssue": "2021-12-06",
      |        "psrDueDate": "2022-03-31",
      |        "psrReportType": "Standard"
      |    }
      |]
      |""".stripMargin

  val sampleVersionsResponseAsJsonString: String =
    """
      |[
      |    {
      |        "reportFormBundleNumber": "123456785012",
      |        "reportVersion": 1,
      |        "reportStatus": "Compiled",
      |        "compilationOrSubmissionDate": "2023-04-02T09:30:47Z",
      |        "reportSubmitterDetails": {
      |            "reportSubmittedBy": "PSP",
      |            "organisationOrPartnershipDetails": {
      |                "organisationOrPartnershipName": "ABC Limited"
      |            }
      |        },
      |        "psaDetails": {
      |            "psaOrganisationOrPartnershipDetails": {
      |                "organisationOrPartnershipName": "XYZ Limited"
      |            }
      |        }
      |    }
      |]
      |""".stripMargin

  val sampleStandardPsrResponseAsJsonString: String =
    """{
      |  "schemeDetails": {
      |    "pstr": "12345678AA",
      |    "schemeName": "My Golden Egg scheme"
      |  },
      |  "psrDetails": {
      |    "fbVersion": "001",
      |    "fbstatus": "Compiled",
      |    "periodStart": "2023-04-06",
      |    "periodEnd": "2024-04-05",
      |    "compilationOrSubmissionDate": "2023-12-17T09:30:47Z"
      |  },
      |  "accountingPeriodDetails": {
      |    "recordVersion": "002",
      |    "accountingPeriods": [
      |      {
      |        "accPeriodStart": "2022-04-06",
      |        "accPeriodEnd": "2022-12-31"
      |      },
      |      {
      |        "accPeriodStart": "2023-01-01",
      |        "accPeriodEnd": "2023-04-05"
      |      }
      |    ]
      |  },
      |  "schemeDesignatory": {
      |    "recordVersion": "002",
      |    "openBankAccount": "Yes",
      |    "noOfActiveMembers": 5,
      |    "noOfDeferredMembers": 2,
      |    "noOfPensionerMembers": 10,
      |    "totalAssetValueStart": 10000000,
      |    "totalAssetValueEnd": 11000000,
      |    "totalCashStart": 2500000,
      |    "totalCashEnd": 2800000,
      |    "totalPayments": 2000000
      |  },
      |  "membersPayments": {
      |    "recordVersion": "002",
      |    "employerContributionMade": "Yes",
      |    "unallocatedContribsMade": "No",
      |    "memberContributionMade": "Yes",
      |    "schemeReceivedTransferIn": "Yes",
      |    "schemeMadeTransferOut": "Yes",
      |    "lumpSumReceived": "Yes",
      |    "pensionReceived": "Yes",
      |    "surrenderMade": "Yes",
      |    "memberDetails": [
      |      {
      |        "memberStatus": "Changed",
      |        "memberPSRVersion": "001",
      |        "personalDetails": {
      |          "foreName": "Ferdinand",
      |          "middleName": "Felix",
      |          "lastName": "Bull",
      |          "nino": "EB103145A",
      |          "dateOfBirth": "1960-05-31"
      |        },
      |        "noOfContributions": 2,
      |        "memberEmpContribution": [
      |          {
      |            "orgName": "Acme Ltd",
      |            "organisationIdentity": {
      |              "orgType": "01",
      |              "idNumber": "AC123456"
      |            },
      |            "totalContribution": 20000
      |          },
      |          {
      |            "orgName": "UK Company Ltd",
      |            "organisationIdentity": {
      |              "orgType": "01",
      |              "idNumber": "AC123456"
      |            },
      |            "totalContribution": 10000
      |          }
      |        ],
      |        "totalContributions": 30000,
      |        "noOfTransfersIn": 2,
      |        "memberTransfersIn": [
      |          {
      |            "dateOfTransfer": "2022-08-08",
      |            "schemeName": "The Happy Retirement Scheme",
      |            "transferSchemeType": {
      |              "schemeType": "02",
      |              "refNumber": "Q123456"
      |            },
      |            "transferValue": 10000,
      |            "transferIncludedAsset": "No"
      |          },
      |          {
      |            "dateOfTransfer": "2022-11-27",
      |            "schemeName": "The Happy Retirement Scheme",
      |            "transferSchemeType": {
      |              "schemeType": "02",
      |              "refNumber": "Q123456"
      |            },
      |            "transferValue": 8000,
      |            "transferIncludedAsset": "No"
      |          }
      |        ],
      |        "noOfTransfersOut": 2,
      |        "memberTransfersOut": [
      |          {
      |            "dateOfTransfer": "2022-09-30",
      |            "schemeName": "The Golden Egg Scheme",
      |            "transferSchemeType": {
      |              "schemeType": "01",
      |              "refNumber": "76509173AA"
      |            }
      |          },
      |          {
      |            "dateOfTransfer": "2022-12-20",
      |            "schemeName": "The Golden Egg Scheme",
      |            "transferSchemeType": {
      |              "schemeType": "01",
      |              "refNumber": "76509173AB"
      |            }
      |          }
      |        ],
      |        "memberLumpSumReceived": [
      |          {
      |            "lumpSumAmount": 30000,
      |            "designatedPensionAmount": 20000
      |          }
      |        ],
      |        "pensionAmountReceived": 12000,
      |        "memberPensionSurrender": [
      |          {
      |            "totalSurrendered": 1000,
      |            "dateOfSurrender": "2022-12-19",
      |            "surrenderReason": "ABC"
      |          },
      |          {
      |            "totalSurrendered": 2000,
      |            "dateOfSurrender": "2023-02-08",
      |            "surrenderReason": "I felt like giving money away..."
      |          }
      |        ]
      |      },
      |      {
      |        "memberStatus": "Changed",
      |        "personalDetails": {
      |          "foreName": "Johnny",
      |          "middleName": "Be",
      |          "lastName": "Quicke",
      |          "reasonNoNINO": "Could not find it on record.",
      |          "dateOfBirth": "1940-10-31"
      |        },
      |        "noOfContributions": 2,
      |        "memberEmpContribution": [
      |          {
      |            "orgName": "Sofa Inc.",
      |            "organisationIdentity": {
      |              "orgType": "03",
      |              "otherDescription": "Found it down back of my sofa"
      |            },
      |            "totalContribution": 10000
      |          },
      |          {
      |            "orgName": "UK Company XYZ Ltd.",
      |            "organisationIdentity": {
      |              "orgType": "01",
      |              "idNumber": "CC123456"
      |            },
      |            "totalContribution": 10000
      |          }
      |        ],
      |        "totalContributions": 20000,
      |        "noOfTransfersIn": 2,
      |        "memberTransfersIn": [
      |          {
      |            "dateOfTransfer": "2022-12-02",
      |            "schemeName": "Golden Years Pension Scheme",
      |            "transferSchemeType": {
      |              "schemeType": "01",
      |              "refNumber": "88390774ZZ"
      |            },
      |            "transferValue": 50000,
      |            "transferIncludedAsset": "Yes"
      |          },
      |          {
      |            "dateOfTransfer": "2022-10-30",
      |            "schemeName": "Golden Goose Egg Laying Scheme",
      |            "transferSchemeType": {
      |              "schemeType": "02",
      |              "refNumber": "Q654321"
      |            },
      |            "transferValue": 2000,
      |            "transferIncludedAsset": "No"
      |          }
      |        ],
      |        "noOfTransfersOut": 2,
      |        "memberTransfersOut": [
      |          {
      |            "dateOfTransfer": "2022-05-30",
      |            "schemeName": "Dodgy Pensions Ltd",
      |            "transferSchemeType": {
      |              "schemeType": "03",
      |              "otherDescription": "Unknown identifier"
      |            }
      |          },
      |          {
      |            "dateOfTransfer": "2022-07-31",
      |            "schemeName": "My back pocket Pension Scheme",
      |            "transferSchemeType": {
      |              "schemeType": "02",
      |              "refNumber": "Q000002"
      |            }
      |          }
      |        ]
      |      }
      |    ]
      |  },
      |  "loans": {
      |    "recordVersion": "003",
      |    "schemeHadLoans": "Yes",
      |    "noOfLoans": 1,
      |    "loanTransactions": [
      |      {
      |        "dateOfLoan": "2023-03-30",
      |        "loanRecipientName": "Electric Car Co.",
      |        "recipientIdentityType": {
      |          "indivOrOrgType": "01",
      |          "otherDescription": "Identification not on record."
      |        },
      |        "recipientSponsoringEmployer": "No",
      |        "connectedPartyStatus": "01",
      |        "loanAmount": 10000,
      |        "loanInterestAmount": 2000,
      |        "loanTotalSchemeAssets": 2000,
      |        "loanPeriodInMonths": 24,
      |        "equalInstallments": "Yes",
      |        "loanInterestRate": 5.55,
      |        "securityGiven": "Yes",
      |        "securityDetails": "Japanese ming vase #344343444.",
      |        "capRepaymentCY": 5000,
      |        "intReceivedCY": 555,
      |        "arrearsPrevYears": "No",
      |        "amountOutstanding": 5000
      |      }
      |    ]
      |  },
      |  "shares": {
      |    "recordVersion": "001",
      |    "sponsorEmployerSharesWereHeld": "Yes",
      |    "noOfSponsEmplyrShareTransactions": 1,
      |    "unquotedSharesWereHeld": "Yes",
      |    "noOfUnquotedShareTransactions": 1,
      |    "connectedPartySharesWereHeld": "Yes",
      |    "noOfConnPartyTransactions": 1,
      |    "sponsorEmployerSharesWereDisposed": "No",
      |    "unquotedSharesWereDisposed": "No",
      |    "connectedPartySharesWereDisposed": "No",
      |    "shareTransactions": [
      |      {
      |        "typeOfSharesHeld": "01",
      |        "shareIdentification": {
      |          "nameOfSharesCompany": "AppleSauce Inc.",
      |          "reasonNoCRN": "Not able to locate Company on Companies House",
      |          "classOfShares": "Ordinary Shares"
      |        },
      |        "heldSharesTransaction": {
      |          "methodOfHolding": "01",
      |          "dateOfAcqOrContrib": "2023-10-19",
      |          "totalShares": 200,
      |          "acquiredFromName": "Fredd Bloggs",
      |          "acquiredFromType": {
      |            "indivOrOrgType": "01",
      |            "idNumber": "JE123176A"
      |          },
      |          "connectedPartyStatus": "02",
      |          "costOfShares": 10000,
      |          "supportedByIndepValuation": "Yes",
      |          "totalAssetValue": 2000,
      |          "totalDividendsOrReceipts": 500
      |        },
      |        "disposedSharesTransaction": [
      |          {
      |            "methodOfDisposal": "01",
      |            "salesQuestions": {
      |              "dateOfSale": "2023-02-16",
      |              "noOfSharesSold": 50,
      |              "amountReceived": 8000,
      |              "nameOfPurchaser": "Sharebuyers Inc.",
      |              "purchaserType": {
      |                "indivOrOrgType": "01",
      |                "idNumber": "0008503350"
      |              },
      |              "connectedPartyStatus": "02",
      |              "supportedByIndepValuation": "Yes"
      |            },
      |            "totalSharesNowHeld": 150
      |          },
      |          {
      |            "methodOfDisposal": "02",
      |            "redemptionQuestions": {
      |              "dateOfRedemption": "2023-03-06",
      |              "noOfSharesRedeemed": 50,
      |              "amountReceived": 7600
      |            },
      |            "totalSharesNowHeld": 100
      |          }
      |        ]
      |      },
      |      {
      |        "typeOfSharesHeld": "03",
      |        "shareIdentification": {
      |          "nameOfSharesCompany": "Pear Computers Inc.",
      |          "crnNumber": "LP289157",
      |          "classOfShares": "Preferred Shares"
      |        },
      |        "heldSharesTransaction": {
      |          "methodOfHolding": "01",
      |          "dateOfAcqOrContrib": "2023-10-19",
      |          "totalShares": 10000,
      |          "acquiredFromName": "Golden Investments Ltd.",
      |          "acquiredFromType": {
      |            "indivOrOrgType": "03",
      |            "idNumber": "28130262"
      |          },
      |          "connectedPartyStatus": "02",
      |          "costOfShares": 50000,
      |          "supportedByIndepValuation": "Yes",
      |          "totalAssetValue": 40000,
      |          "totalDividendsOrReceipts": 200
      |        },
      |        "disposedSharesTransaction": [
      |          {
      |            "methodOfDisposal": "01",
      |            "salesQuestions": {
      |              "dateOfSale": "2022-10-31",
      |              "noOfSharesSold": 1100,
      |              "amountReceived": 30000,
      |              "nameOfPurchaser": "Share Acquisitions Inc.",
      |              "purchaserType": {
      |                "indivOrOrgType": "01",
      |                "idNumber": "JJ507888A"
      |              },
      |              "connectedPartyStatus": "02",
      |              "supportedByIndepValuation": "Yes"
      |            },
      |            "totalSharesNowHeld": 8000
      |          },
      |          {
      |            "methodOfDisposal": "02",
      |            "redemptionQuestions": {
      |              "dateOfRedemption": "2022-12-20",
      |              "noOfSharesRedeemed": 900,
      |              "amountReceived": 27005.78
      |            },
      |            "totalSharesNowHeld": 8000
      |          }
      |        ]
      |      },
      |      {
      |        "typeOfSharesHeld": "03",
      |        "shareIdentification": {
      |          "nameOfSharesCompany": "Connected Party Inc.",
      |          "crnNumber": "LP289157",
      |          "classOfShares": "Convertible Preference Shares"
      |        },
      |        "heldSharesTransaction": {
      |          "methodOfHolding": "02",
      |          "dateOfAcqOrContrib": "2023-10-19",
      |          "totalShares": 1000,
      |          "acquiredFromName": "Investec Inc.",
      |          "acquiredFromType": {
      |            "indivOrOrgType": "02",
      |            "idNumber": "0000123456"
      |          },
      |          "connectedPartyStatus": "02",
      |          "costOfShares": 120220.34,
      |          "supportedByIndepValuation": "Yes",
      |          "totalAssetValue": 10000,
      |          "totalDividendsOrReceipts": 599.99
      |        },
      |        "disposedSharesTransaction": [
      |          {
      |            "methodOfDisposal": "02",
      |            "redemptionQuestions": {
      |              "dateOfRedemption": "2022-11-03",
      |              "noOfSharesRedeemed": 200,
      |              "amountReceived": 50000
      |            },
      |            "totalSharesNowHeld": 800
      |          },
      |          {
      |            "methodOfDisposal": "01",
      |            "salesQuestions": {
      |              "dateOfSale": "2022-12-31",
      |              "noOfSharesSold": 200,
      |              "amountReceived": 52000,
      |              "nameOfPurchaser": "Sam Smithsonian",
      |              "purchaserType": {
      |                "indivOrOrgType": "01",
      |                "idNumber": "JE443364A"
      |              },
      |              "connectedPartyStatus": "01",
      |              "supportedByIndepValuation": "Yes"
      |            },
      |            "totalSharesNowHeld": 400
      |          }
      |        ]
      |      }
      |    ],
      |    "totalValueQuotedShares": 0.0
      |  },
      |  "assets": {
      |    "landOrProperty": {
      |      "recordVersion": "001",
      |      "heldAnyLandOrProperty": "Yes",
      |      "disposeAnyLandOrProperty": "Yes",
      |      "noOfTransactions": 1,
      |      "landOrPropertyTransactions": [
      |        {
      |          "propertyDetails": {
      |            "landOrPropertyInUK": "Yes",
      |            "addressDetails": {
      |              "addressLine1": "testAddressLine1",
      |              "addressLine2": "testAddressLine2",
      |              "addressLine3": "testAddressLine3",
      |              "ukPostCode": "GB135HG",
      |              "countryCode": "GB"
      |            },
      |            "landRegistryDetails": {
      |              "landRegistryReferenceExists": "Yes",
      |              "landRegistryReference": "landRegistryTitleNumberValue"
      |            }
      |          },
      |          "heldPropertyTransaction": {
      |            "methodOfHolding": "01",
      |            "dateOfAcquisitionOrContribution": "2023-10-19",
      |            "propertyAcquiredFromName": "PropertyAcquiredFromName",
      |            "propertyAcquiredFrom": {
      |              "indivOrOrgType": "02",
      |              "idNumber": "idNumber"
      |            },
      |            "connectedPartyStatus": "01",
      |            "totalCostOfLandOrProperty": 1.7976931348623157E+308,
      |            "indepValuationSupport": "Yes",
      |            "residentialSchedule29A": "Yes",
      |            "landOrPropertyLeased": "Yes",
      |            "leaseDetails": {
      |              "lesseeName": "lesseeName",
      |              "connectedPartyStatus": "01",
      |              "leaseGrantDate": "2023-10-19",
      |              "annualLeaseAmount": 1.7976931348623157E+308
      |            },
      |            "totalIncomeOrReceipts": 1.7976931348623157E+308
      |          },
      |          "disposedPropertyTransaction": [
      |            {
      |              "methodOfDisposal": "01",
      |              "dateOfSale": "2023-10-19",
      |              "nameOfPurchaser": "NameOfPurchaser",
      |              "purchaseOrgDetails": {
      |                "indivOrOrgType": "01",
      |                "idNumber": "idNumber"
      |              },
      |              "saleProceeds": 1.7976931348623157E+308,
      |              "connectedPartyStatus": "01",
      |              "indepValuationSupport": "No",
      |              "portionStillHeld": "Yes"
      |            }
      |          ]
      |        }
      |      ]
      |    },
      |    "borrowing": {
      |      "recordVersion": "164",
      |      "moneyWasBorrowed": "Yes",
      |      "noOfBorrows": 1,
      |      "moneyBorrowed": [
      |        {
      |          "dateOfBorrow": "2023-10-19",
      |          "amountBorrowed": 1.7976931348623157E+308,
      |          "schemeAssetsValue": 1.7976931348623157E+308,
      |          "interestRate": 1.7976931348623157E+308,
      |          "borrowingFromName": "borrowingFromName",
      |          "connectedPartyStatus": "01",
      |          "reasonForBorrow": "reasonForBorrow"
      |        }
      |      ]
      |    },
      |    "bonds": {
      |      "recordVersion": "528",
      |      "bondsWereAdded": "Yes",
      |      "bondsWereDisposed": "Yes",
      |      "noOfTransactions": 2,
      |      "bondTransactions": [
      |        {
      |          "nameOfBonds": "Xenex Bonds",
      |          "methodOfHolding": "01",
      |          "dateOfAcqOrContrib": "2023-10-19",
      |          "costOfBonds": 10234.56,
      |          "connectedPartyStatus": "02",
      |          "bondsUnregulated": "No",
      |          "totalIncomeOrReceipts": 50,
      |          "bondsDisposed": [
      |            {
      |              "methodOfDisposal": "01",
      |              "dateSold": "2023-10-19",
      |              "amountReceived": 12333.59,
      |              "bondsPurchaserName": "Happy Bond Buyers Inc.",
      |              "connectedPartyStatus": "02",
      |              "totalNowHeld": 120
      |            }
      |          ]
      |        },
      |        {
      |          "nameOfBonds": "Really Goods Bonds ABC",
      |          "methodOfHolding": "03",
      |          "dateOfAcqOrContrib": "2023-10-19",
      |          "costOfBonds": 2000.5,
      |          "connectedPartyStatus": "02",
      |          "bondsUnregulated": "No",
      |          "totalIncomeOrReceipts": 300,
      |          "bondsDisposed": [
      |            {
      |              "methodOfDisposal": "01",
      |              "dateSold": "2023-10-19",
      |              "amountReceived": 3333.33,
      |              "bondsPurchaserName": "Bonds Buyers (PTY) Ltd",
      |              "connectedPartyStatus": "01",
      |              "totalNowHeld": 50
      |            }
      |          ]
      |        }
      |      ]
      |    },
      |    "otherAssets": {
      |      "recordVersion": "002",
      |      "otherAssetsWereHeld": "Yes",
      |      "otherAssetsWereDisposed": "No",
      |      "noOfTransactions": 1,
      |      "otherAssetTransactions": [
      |        {
      |          "assetDescription": "Box of matches",
      |          "methodOfHolding": "01",
      |          "dateOfAcqOrContrib": "2023-10-19",
      |          "costOfAsset": 1.7976931348623157E+308,
      |          "acquiredFromName": "Dodgy Den Match Co.",
      |          "acquiredFromType": {
      |            "indivOrOrgType": "01",
      |            "reasonNoIdNumber": "reasonNoId"
      |          },
      |          "connectedStatus": "01",
      |          "supportedByIndepValuation": "No",
      |          "movableSchedule29A": "No",
      |          "totalIncomeOrReceipts": 1.7976931348623157E+308,
      |          "assetsDisposed": [
      |            {
      |              "methodOfDisposal": "01",
      |              "dateSold": "2023-10-19",
      |              "purchaserName": "Acme Express Ltd.",
      |              "purchaserType": {
      |                "indivOrOrgType": "04",
      |                "otherDescription": "Foreign purchaser"
      |              },
      |              "totalAmountReceived": 150000.33,
      |              "connectedStatus": "02",
      |              "supportedByIndepValuation": "Yes",
      |              "fullyDisposedOf": "Yes"
      |            }
      |          ]
      |        }
      |      ]
      |    }
      |  },
      |  "psrDeclaration": {
      |    "submittedBy": "PSP",
      |    "submitterId": "21000005",
      |    "psaId": "A2100005",
      |    "pspDeclaration": {
      |      "pspDeclaration1": true,
      |      "pspDeclaration2": true
      |    }
      |  }
      |}""".stripMargin

  private def errorResponse(code: String, reason: String): String =
    Json.stringify(
      Json.obj(
        "failures" -> Json.arr(
          Json.obj(
            "code" -> code,
            "reason" -> reason
          )
        )
      )
    )

}
