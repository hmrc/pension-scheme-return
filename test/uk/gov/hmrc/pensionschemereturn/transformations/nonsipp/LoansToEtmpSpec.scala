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

package uk.gov.hmrc.pensionschemereturn.transformations.nonsipp

import org.mockito.MockitoSugar
import org.scalatestplus.play.PlaySpec
import uk.gov.hmrc.pensionschemereturn.models.etmp.nonsipp.{EtmpIdentityType, EtmpLoanTransactions, EtmpLoans}
import uk.gov.hmrc.pensionschemereturn.models.nonsipp._
import uk.gov.hmrc.pensionschemereturn.transformations.Transformer

import java.time.LocalDate

class LoansToEtmpSpec extends PlaySpec with MockitoSugar with Transformer {
  private val transformation: LoansToEtmp = new LoansToEtmp()
  val today: LocalDate = LocalDate.now

  "LoansToEtmp - PSR Loans should successfully transform to etmp format " should {
    "for Individual" in {
      val loans: Loans = Loans(
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

      val expected = EtmpLoans(
        recordVersion = "001",
        schemeHadLoans = Yes,
        noOfLoans = 1,
        loanTransactions = List(
          EtmpLoanTransactions(
            dateOfLoan = today,
            loanRecipientName = "IndividualName",
            recipientIdentityType = EtmpIdentityType(
              indivOrOrgType = "01",
              idNumber = None,
              reasonNoIdNumber = Some("NoNinoReason"),
              otherDescription = None
            ),
            recipientSponsoringEmployer = No,
            connectedPartyStatus = "01",
            loanAmount = Double.MaxValue,
            loanInterestAmount = Double.MaxValue,
            loanTotalSchemeAssets = Double.MaxValue,
            loanPeriodInMonths = Int.MaxValue,
            equalInstallments = Yes,
            loanInterestRate = Double.MaxValue,
            securityGiven = No,
            securityDetails = None,
            capRepaymentCY = Double.MaxValue,
            intReceivedCY = Double.MaxValue,
            arrearsPrevYears = Yes,
            amountOfArrears = Some(Double.MaxValue),
            amountOutstanding = Double.MaxValue
          )
        )
      )

      transformation.transform(loans) mustEqual expected
    }
    "for UKCompany" in {
      val loans: Loans = Loans(
        schemeHadLoans = true,
        loanTransactions = List(
          LoanTransactions(
            recipientIdentityType = RecipientIdentityType(
              IdentityType.UKCompany,
              None,
              Some("NoCrnReason"),
              None
            ),
            loanRecipientName = "UKCompanyName",
            connectedPartyStatus = true,
            optRecipientSponsoringEmployer = Some("connectedParty"),
            datePeriodLoanDetails = LoanPeriod(today, Double.MaxValue, Int.MaxValue),
            loanAmountDetails = LoanAmountDetails(Double.MaxValue, Double.MaxValue, Double.MaxValue),
            equalInstallments = true,
            loanInterestDetails = LoanInterestDetails(Double.MaxValue, Double.MaxValue, Double.MaxValue),
            optSecurityGivenDetails = None,
            optOutstandingArrearsOnLoan = Some(Double.MaxValue)
          )
        )
      )

      val expected = EtmpLoans(
        recordVersion = "001",
        schemeHadLoans = Yes,
        noOfLoans = 1,
        loanTransactions = List(
          EtmpLoanTransactions(
            dateOfLoan = today,
            loanRecipientName = "UKCompanyName",
            recipientIdentityType = EtmpIdentityType(
              indivOrOrgType = "02",
              idNumber = None,
              reasonNoIdNumber = Some("NoCrnReason"),
              otherDescription = None
            ),
            recipientSponsoringEmployer = No,
            connectedPartyStatus = "01",
            loanAmount = Double.MaxValue,
            loanInterestAmount = Double.MaxValue,
            loanTotalSchemeAssets = Double.MaxValue,
            loanPeriodInMonths = Int.MaxValue,
            equalInstallments = Yes,
            loanInterestRate = Double.MaxValue,
            securityGiven = No,
            securityDetails = None,
            capRepaymentCY = Double.MaxValue,
            intReceivedCY = Double.MaxValue,
            arrearsPrevYears = Yes,
            amountOfArrears = Some(Double.MaxValue),
            amountOutstanding = Double.MaxValue
          )
        )
      )

      transformation.transform(loans) mustEqual expected
    }
    "for UKPartnership" in {
      val loans: Loans = Loans(
        schemeHadLoans = true,
        loanTransactions = List(
          LoanTransactions(
            recipientIdentityType = RecipientIdentityType(
              IdentityType.UKPartnership,
              Some("1234567890"),
              None,
              None
            ),
            loanRecipientName = "UKPartnershipName",
            connectedPartyStatus = false,
            optRecipientSponsoringEmployer = Some("sponsoring"),
            datePeriodLoanDetails = LoanPeriod(today, Double.MaxValue, Int.MaxValue),
            loanAmountDetails = LoanAmountDetails(Double.MaxValue, Double.MaxValue, Double.MaxValue),
            equalInstallments = false,
            loanInterestDetails = LoanInterestDetails(Double.MaxValue, Double.MaxValue, Double.MaxValue),
            optSecurityGivenDetails = Some("SecurityGivenDetails"),
            optOutstandingArrearsOnLoan = None
          )
        )
      )

      val expected = EtmpLoans(
        recordVersion = "001",
        schemeHadLoans = Yes,
        noOfLoans = 1,
        loanTransactions = List(
          EtmpLoanTransactions(
            dateOfLoan = today,
            loanRecipientName = "UKPartnershipName",
            recipientIdentityType = EtmpIdentityType(
              indivOrOrgType = "03",
              idNumber = Some("1234567890"),
              reasonNoIdNumber = None,
              otherDescription = None
            ),
            recipientSponsoringEmployer = Yes,
            connectedPartyStatus = "02",
            loanAmount = Double.MaxValue,
            loanInterestAmount = Double.MaxValue,
            loanTotalSchemeAssets = Double.MaxValue,
            loanPeriodInMonths = Int.MaxValue,
            equalInstallments = No,
            loanInterestRate = Double.MaxValue,
            securityGiven = Yes,
            securityDetails = Some("SecurityGivenDetails"),
            capRepaymentCY = Double.MaxValue,
            intReceivedCY = Double.MaxValue,
            arrearsPrevYears = No,
            amountOfArrears = None,
            amountOutstanding = Double.MaxValue
          )
        )
      )

      transformation.transform(loans) mustEqual expected
    }
    "for Other" in {
      val loans: Loans = Loans(
        schemeHadLoans = true,
        loanTransactions = List(
          LoanTransactions(
            recipientIdentityType = RecipientIdentityType(
              IdentityType.Other,
              None,
              None,
              Some("otherDescription")
            ),
            loanRecipientName = "OtherName",
            connectedPartyStatus = false,
            optRecipientSponsoringEmployer = Some("neither"),
            datePeriodLoanDetails = LoanPeriod(today, Double.MaxValue, Int.MaxValue),
            loanAmountDetails = LoanAmountDetails(Double.MaxValue, Double.MaxValue, Double.MaxValue),
            equalInstallments = false,
            loanInterestDetails = LoanInterestDetails(Double.MaxValue, Double.MaxValue, Double.MaxValue),
            optSecurityGivenDetails = Some("SecurityGivenDetails"),
            optOutstandingArrearsOnLoan = None
          )
        )
      )

      val expected = EtmpLoans(
        recordVersion = "001",
        schemeHadLoans = Yes,
        noOfLoans = 1,
        loanTransactions = List(
          EtmpLoanTransactions(
            dateOfLoan = today,
            loanRecipientName = "OtherName",
            recipientIdentityType = EtmpIdentityType(
              indivOrOrgType = "04",
              idNumber = None,
              reasonNoIdNumber = None,
              otherDescription = Some("otherDescription")
            ),
            recipientSponsoringEmployer = No,
            connectedPartyStatus = "02",
            loanAmount = Double.MaxValue,
            loanInterestAmount = Double.MaxValue,
            loanTotalSchemeAssets = Double.MaxValue,
            loanPeriodInMonths = Int.MaxValue,
            equalInstallments = No,
            loanInterestRate = Double.MaxValue,
            securityGiven = Yes,
            securityDetails = Some("SecurityGivenDetails"),
            capRepaymentCY = Double.MaxValue,
            intReceivedCY = Double.MaxValue,
            arrearsPrevYears = No,
            amountOfArrears = None,
            amountOutstanding = Double.MaxValue
          )
        )
      )

      transformation.transform(loans) mustEqual expected
    }
  }
}
