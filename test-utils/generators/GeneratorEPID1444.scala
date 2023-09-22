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

package generators

import org.scalacheck.Gen
import play.api.libs.json.{JsValue, Json}

// scalastyle:off
trait GeneratorEPID1444 extends ModelGenerators {

  def generateUserAnswersAndPOSTBody: Gen[(JsValue, JsValue)] = {
    val fullUA = Json.toJson(
      """
        |{
        |  "reportDetails": {
        |    "pstr": "17836742CF",
        |    "psrStatus": "Compiled",
        |    "from": "2022-04-06",
        |    "to": "2023-04-05"
        |  },
        |  "checkReturnDates": false,
        |  "accountingPeriods": [
        |    {
        |      "from": "2022-04-06",
        |      "to": "2022-12-31"
        |    },
        |    {
        |      "from": "2023-01-01",
        |      "to": "2023-04-05"
        |    }
        |  ],
        |  "schemeDesignatory": {
        |    "openBankAccount": true,
        |    "howManyMembersPage": {
        |      "noOfActiveMembers": 5,
        |      "noOfDeferredMembers": 2,
        |      "noOfPensionerMembers": 10
        |    },
        |    "totalAssetValue": {
        |      "moneyAtStart": 10000000,
        |      "moneyAtEnd": 11000000
        |    },
        |    "totalCash": {
        |      "moneyAtStart": 2500000,
        |      "moneyAtEnd": 2800000
        |    },
        |    "totalPayments": 2000000
        |  },
        |  "loans": {
        |    "loanTransactions": {
        |      "recipientIdentityType": {
        |        "identityTypes": {
        |          "0": "individual"
        |        },
        |        "nino": {
        |          "0": {
        |            "yes": "SX123456A"
        |          }
        |        }
        |      },
        |      "loanRecipientName": {
        |        "individual": {
        |          "0": "Electric Car Co."
        |        }
        |      },
        |      "individualConnectedPartyStatus": {
        |        "0": true
        |      },
        |      "datePeriodLoanPage": {
        |        "0": [
        |          "2023-03-30",
        |          {
        |            "value": 2000,
        |            "displayAs": "2,000.00"
        |          },
        |          24
        |        ]
        |      },
        |      "loanAmountPage": {
        |        "0": [
        |          {
        |            "value": 10000,
        |            "displayAs": "10,000.00"
        |          },
        |          {
        |            "value": 5000,
        |            "displayAs": "5,000.00"
        |          },
        |          {
        |            "value": 5000,
        |            "displayAs": "5,000.00"
        |          }
        |        ]
        |      },
        |      "equalInstallments": {
        |        "0": true
        |      },
        |      "loanInterestPage": {
        |        "0": [
        |          {
        |            "value": 2000,
        |            "displayAs": "2,000.00"
        |          },
        |          {
        |            "value": 5.55,
        |            "displayAs": "5.55"
        |          },
        |          {
        |            "value": 555,
        |            "displayAs": "555.00"
        |          }
        |        ]
        |      },
        |      "securityGivenPage": {
        |        "0": {
        |          "yes": "Japanese ming vase #344343444."
        |        }
        |      },
        |      "outstandingArrearsOnLoan": {
        |        "0": {
        |          "yes": {
        |            "value": 1234,
        |            "displayAs": "1,234.00"
        |          }
        |        }
        |      }
        |    },
        |    "schemeHadLoans": true
        |  }
        |}
        |""".stripMargin
    )
    val fullBody = Json.toJson(
      """
        |{
        |      "reportDetails" : {
        |            "pstr" : "17836742CF",
        |            "psrStatus" : "Compiled",
        |            "periodStart" : "2022-04-06",
        |            "periodEnd" : "2023-04-05"
        |      },
        |      "accountingPeriodDetails" : {
        |            "recordVersion" : "002",
        |            "accountingPeriods" : [
        |                  {
        |                        "accPeriodStart" : "2022-04-06",
        |                        "accPeriodEnd" : "2022-12-31"
        |                  },
        |                  {
        |                        "accPeriodStart" : "2023-01-01",
        |                        "accPeriodEnd" : "2023-04-05"
        |                  }
        |            ]
        |      },
        |      "schemeDesignatory" : {
        |            "recordVersion" : "002",
        |            "openBankAccount" : "Yes",
        |            "noOfActiveMembers" : 5,
        |            "noOfDeferredMembers" : 2,
        |            "noOfPensionerMembers" : 10,
        |            "totalAssetValueStart" : 10000000,
        |            "totalAssetValueEnd" : 11000000,
        |            "totalCashStart" : 2500000,
        |            "totalCashEnd" : 2800000,
        |            "totalPayments" : 2000000
        |      },
        |      "loans" : {
        |            "recordVersion" : "003",
        |            "schemeHadLoans" : "Yes",
        |            "noOfLoans" : 1,
        |            "loanTransactions" : [
        |                  {
        |                        "dateOfLoan" : "2023-03-30",
        |                        "loanRecipientName" : "Electric Car Co.",
        |                        "recipientIdentityType" : {
        |                              "indivOrOrgType" : "01",
        |                              "otherDescription" : "Identification not on record."
        |                        },
        |                        "recipientSponsoringEmployer" : "No",
        |                        "connectedPartyStatus" : "01",
        |                        "loanAmount" : 10000,
        |                        "loanInterestAmount" : 2000,
        |                        "loanTotalSchemeAssets" : 2000,
        |                        "loanPeriodInMonths" : 24,
        |                        "equalInstallments" : "Yes",
        |                        "loanInterestRate" : 5.55,
        |                        "securityGiven" : "Yes",
        |                        "securityDetails" : "Japanese ming vase #344343444.",
        |                        "capRepaymentCY" : 5000,
        |                        "intReceivedCY" : 555,
        |                        "arrearsPrevYears" : "No",
        |                        "amountOutstanding" : 5000
        |                  }
        |            ]
        |      }
        |}
        |""".stripMargin
    )
    Tuple2(fullUA, fullBody)
  }


  /*def generateUserAnswersAndPOSTBody: Gen[(JsObject, JsObject)] =
    for {
      startDate <- dateGenerator
      pstr <- pstrGen
      psaOrPsp <- psaOrPspGen
      psaId <- psaIdGen
      pspId <- pspIdGen
    } yield {

      val endDate = startDate.plusDays(1)
      val selected = "Selected"

      def psaOrPspId(psaOrPsp: String): String =
        psaOrPsp match {
          case "PSA" => psaId.value
          case "PSP" => pspId.value
        }
      def psaOrPspDeclaration(psaOrPsp: String): (String, Json.JsValueWrapper) =
        psaOrPsp match {
          case "PSA" =>
            "psaDeclaration" -> Json.obj(
              "psaDeclaration1" -> selected,
              "psaDeclaration2" -> selected
            )
          case "PSP" =>
            "pspDeclaration" -> Json.obj(
              "authorisedPSAID" -> psaId.value,
              "pspDeclaration1" -> selected,
              "pspDeclaration2" -> selected
            )
        }

      val psaOrPspJson = psaOrPspDeclaration(psaOrPsp)

      def fullUA(psaOrPsp: String) = psaOrPsp match {
        case "PSA" =>
          Json.obj(
            "pstr" -> pstr.value,
            "reportStartDate" -> startDate,
            "reportEndDate" -> startDate.plusDays(1),
            "submittedBy" -> psaOrPsp,
            "submittedID" -> psaOrPspId(psaOrPsp),
            "psaDeclaration1" -> selected,
            "psaDeclaration2" -> selected
          )
        case "PSP" =>
          Json.obj(
            "pstr" -> pstr.value,
            "reportStartDate" -> startDate,
            "reportEndDate" -> startDate.plusDays(1),
            "submittedBy" -> psaOrPsp,
            "submittedID" -> psaOrPspId(psaOrPsp),
            "authorisedPSAID" -> psaId.value,
            "pspDeclaration1" -> selected,
            "pspDeclaration2" -> selected
          )
      }

      val fullExpectedResult = Json.obj(
        "declarationDetails" -> Json.obj(
          "erDetails" -> Json.obj(
            "pSTR" -> pstr.value,
            "reportStartDate" -> startDate,
            "reportEndDate" -> endDate
          ),
          "erDeclarationDetails" -> Json.obj(
            "submittedBy" -> psaOrPsp,
            "submittedID" -> psaOrPspId(psaOrPsp)
          ),
          psaOrPspJson
        )
      )
      Tuple2(fullUA(psaOrPsp), fullExpectedResult)
    }*/

  /*def generateUserAnswersAndPOSTBody: Gen[Tuple2[JsObject, JsObject]] = {
    val whoReceivedUnauthorisedPaymentMember = "member"
    val whoReceivedUnauthorisedPaymentEmployer = "employer"
    val whoReceivedUnauthorisedPaymentMap: Map[String, String] = Map(
      whoReceivedUnauthorisedPaymentMember -> "Individual",
      whoReceivedUnauthorisedPaymentEmployer -> "Employer",
    )

    val result = for {
      taxYear <- taxYearGenerator
      whoReceivedUnauthorisedPayment <- Gen.oneOf(whoReceivedUnauthorisedPaymentMember, whoReceivedUnauthorisedPaymentEmployer)
    } yield {
      (whoReceivedUnauthorisedPayment match {
        case `whoReceivedUnauthorisedPaymentMember` => generateMember
        case _ => generateEmployer
      }).map { case (generatedUA, generatedExpectedResult) =>
        val fullUA = Json.obj(
          "event1" ->
            Json.obj(
              "membersOrEmployers" ->
                Json.arr(
                  generatedUA ++ Json.obj("whoReceivedUnauthPayment" -> whoReceivedUnauthorisedPayment)
                )
            ),
          "taxYear" -> taxYear
        )
        val endTaxYear = (taxYear.toInt + 1).toString
        val fullExpectedResult = Json.obj(
          "eventReportDetails" -> Json.obj(
            "reportStartDate" -> s"$taxYear-04-06",
            "reportEndDate" -> s"$endTaxYear-04-05"
          ),
          "event1Details" -> Json.obj(
            "event1Details" -> Json.arr(
              generatedExpectedResult ++ Json.obj(
                "memberType" -> whoReceivedUnauthorisedPaymentMap(whoReceivedUnauthorisedPayment)
              )
            )
          )
        )
        Tuple2(fullUA, fullExpectedResult)
      }

    }

    result.flatMap(identity)

  }*/
}
