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

package utils

import com.networknt.schema.{CustomErrorMessageType, ValidationMessage}
import uk.gov.hmrc.pensionschemereturn.models.etmp.Compiled
import uk.gov.hmrc.pensionschemereturn.models.etmp.nonsipp._
import uk.gov.hmrc.pensionschemereturn.models.etmp.sipp.EtmpSippReportDetails
import uk.gov.hmrc.pensionschemereturn.models.nonsipp._
import uk.gov.hmrc.pensionschemereturn.models.requests.etmp.{PsrSubmissionEtmpRequest, SippPsrSubmissionEtmpRequest}
import uk.gov.hmrc.pensionschemereturn.models.response.{
  EtmpPsrDetails,
  EtmpSchemeDetails,
  PsrSubmissionEtmpResponse,
  SippPsrSubmissionEtmpResponse
}
import uk.gov.hmrc.pensionschemereturn.models.sipp.{SippPsrSubmission, SippReportDetailsSubmission}

import java.text.MessageFormat
import java.time.format.DateTimeFormatter
import java.time.{LocalDate, LocalDateTime}

trait TestValues {

  val pstr = "testPstr"
  val today: LocalDate = LocalDate.now

  val sampleMinimalRequiredSubmission: MinimalRequiredSubmission = MinimalRequiredSubmission(
    reportDetails = ReportDetails(
      pstr = pstr,
      periodStart = today,
      periodEnd = today
    ),
    accountingPeriods = List(today -> today),
    schemeDesignatory = SchemeDesignatory(
      reasonForNoBankAccount = None,
      openBankAccount = true,
      activeMembers = 1,
      deferredMembers = 2,
      pensionerMembers = 3,
      totalAssetValueStart = None,
      totalAssetValueEnd = None,
      totalCashStart = None,
      totalCashEnd = None,
      totalPayments = None
    )
  )

  val samplePsrSubmission: PsrSubmission = PsrSubmission(
    minimalRequiredSubmission = sampleMinimalRequiredSubmission,
    checkReturnDates = true,
    loans = None,
    assets = None
  )

  private val sampleEtmpAccountingPeriodDetails: EtmpAccountingPeriodDetails = EtmpAccountingPeriodDetails(
    recordVersion = "002",
    accountingPeriods = List(
      EtmpAccountingPeriod(
        accPeriodStart = LocalDate.parse("2022-04-06"),
        accPeriodEnd = LocalDate.parse("2022-12-31")
      ),
      EtmpAccountingPeriod(
        accPeriodStart = LocalDate.parse("2023-01-01"),
        accPeriodEnd = LocalDate.parse("2023-04-05")
      )
    )
  )
  val samplePsrSubmissionEtmpResponse: PsrSubmissionEtmpResponse = PsrSubmissionEtmpResponse(
    EtmpSchemeDetails(pstr = "12345678AA", schemeName = "My Golden Egg scheme"),
    EtmpPsrDetails(
      fbVersion = "001",
      fbstatus = Compiled,
      periodStart = LocalDate.parse("2023-04-06"),
      periodEnd = LocalDate.parse("2024-04-05"),
      compilationOrSubmissionDate = LocalDateTime.parse("2023-12-17T09:30:47Z", DateTimeFormatter.ISO_DATE_TIME)
    ),
    sampleEtmpAccountingPeriodDetails,
    EtmpSchemeDesignatory(
      recordVersion = "002",
      openBankAccount = "Yes",
      reasonNoOpenAccount = None,
      noOfActiveMembers = 5,
      noOfDeferredMembers = 2,
      noOfPensionerMembers = 10,
      totalAssetValueStart = Some(10000000),
      totalAssetValueEnd = Some(11000000),
      totalCashStart = Some(2500000),
      totalCashEnd = Some(2800000),
      totalPayments = Some(2000000)
    ),
    Some(
      EtmpLoans(
        recordVersion = "003",
        schemeHadLoans = "Yes",
        noOfLoans = 1,
        loanTransactions = Seq(
          EtmpLoanTransactions(
            dateOfLoan = LocalDate.parse("2023-03-30"),
            loanRecipientName = "Electric Car Co.",
            recipientIdentityType = EtmpIdentityType(
              indivOrOrgType = "01",
              idNumber = None,
              reasonNoIdNumber = None,
              otherDescription = Some("Identification not on record.")
            ),
            recipientSponsoringEmployer = "No",
            connectedPartyStatus = "01",
            loanAmount = 10000,
            loanInterestAmount = 2000,
            loanTotalSchemeAssets = 2000,
            loanPeriodInMonths = 24,
            equalInstallments = "Yes",
            loanInterestRate = 5.55,
            securityGiven = "Yes",
            securityDetails = Some("Japanese ming vase #344343444."),
            capRepaymentCY = 5000,
            intReceivedCY = 555,
            arrearsPrevYears = "No",
            amountOfArrears = None,
            amountOutstanding = 5000
          )
        )
      )
    )
  )

  val sampleLoans: Loans = Loans(
    schemeHadLoans = true,
    loanTransactions = List(
      LoanTransactions(
        recipientIdentityType = RecipientIdentityType(
          IdentityType.Individual,
          None,
          Some("NoNinoReason"),
          None
        ),
        loanRecipientName = "IndividualName",
        connectedPartyStatus = true,
        optRecipientSponsoringEmployer = None,
        datePeriodLoanDetails = LoanPeriod(today, Double.MaxValue, Int.MaxValue),
        loanAmountDetails = LoanAmountDetails(Double.MaxValue, Double.MaxValue, Double.MaxValue),
        equalInstallments = true,
        loanInterestDetails = LoanInterestDetails(Double.MaxValue, Double.MaxValue, Double.MaxValue),
        optSecurityGivenDetails = None,
        optOutstandingArrearsOnLoan = Some(Double.MaxValue)
      )
    )
  )

  val samplePsrSubmissionEtmpRequest: PsrSubmissionEtmpRequest = PsrSubmissionEtmpRequest(
    EtmpReportDetails(pstr, Compiled, today, today),
    EtmpAccountingPeriodDetails("001", List(EtmpAccountingPeriod(today, today))),
    EtmpSchemeDesignatory(
      "001",
      "openBankAccount",
      None,
      1,
      2,
      3,
      None,
      None,
      None,
      None,
      None
    ),
    None,
    None
  )

  val sampleSippReportDetailsSubmission: SippReportDetailsSubmission = SippReportDetailsSubmission(
    "17836742CF",
    periodStart = LocalDate.of(2020, 12, 12),
    periodEnd = LocalDate.of(2021, 12, 12),
    memberTransactions = "Yes"
  )

  val sampleSippPsrSubmission: SippPsrSubmission = SippPsrSubmission(
    sampleSippReportDetailsSubmission
  )

  val sampleSippPsrSubmissionEtmpRequest: SippPsrSubmissionEtmpRequest = SippPsrSubmissionEtmpRequest(
    EtmpSippReportDetails(pstr, Compiled, today, today, "Yes", None, None)
  )

  val sampleSippPsrSubmissionEtmpResponse: SippPsrSubmissionEtmpResponse = SippPsrSubmissionEtmpResponse(
    reportDetails = EtmpSippReportDetails(
      pstr = "12345678AA",
      status = Compiled,
      periodStart = LocalDate.parse("2022-04-06"),
      periodEnd = LocalDate.parse("2023-04-05"),
      memberTransactions = "Yes",
      schemeName = Some("PSR Scheme"),
      psrVersion = Some("001")
    ),
    accountingPeriodDetails = sampleEtmpAccountingPeriodDetails
  )

  val validationMessage: ValidationMessage = ValidationMessage.ofWithCustom(
    "type",
    CustomErrorMessageType.of("CustomErrorMessageType"),
    new MessageFormat("MessageFormat"),
    "customMessage",
    "at",
    "schemaPath"
  )
}
