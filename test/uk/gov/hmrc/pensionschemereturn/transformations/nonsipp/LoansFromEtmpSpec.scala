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

package uk.gov.hmrc.pensionschemereturn.transformations.nonsipp

import utils.TestValues
import com.softwaremill.diffx.scalatest.DiffShouldMatcher
import uk.gov.hmrc.pensionschemereturn.models.etmp.nonsipp.common.EtmpIdentityType
import uk.gov.hmrc.pensionschemereturn.models.nonsipp._
import uk.gov.hmrc.pensionschemereturn.models.etmp.nonsipp.{EtmpLoanTransactions, EtmpLoans}
import uk.gov.hmrc.pensionschemereturn.transformations.Transformer
import com.softwaremill.diffx.generic.auto.indicator
import org.scalatestplus.play.PlaySpec
import org.scalatestplus.mockito.MockitoSugar

class LoansFromEtmpSpec extends PlaySpec with MockitoSugar with Transformer with TestValues with DiffShouldMatcher {

  private val transformation = new LoansFromEtmp()

  "LoansFromEtmp - PSR Loans should successfully transform from etmp format" should {

    "for schemeHadLoans is false loanTransactions is empty" in {

      val etmpLoans = EtmpLoans(
        recordVersion = None,
        schemeHadLoans = No,
        noOfLoans = None,
        loanTransactions = None
      )

      val expected: Loans = Loans(
        recordVersion = None,
        schemeHadLoans = false,
        loanTransactions = List.empty
      )
      transformation.transform(etmpLoans) shouldMatchTo expected
    }

    "for Individual" in {

      val etmpLoans = EtmpLoans(
        recordVersion = Some("001"),
        schemeHadLoans = Yes,
        noOfLoans = Some(1),
        loanTransactions = Some(
          List(
            EtmpLoanTransactions(
              dateOfLoan = sampleToday,
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
              capRepaymentCY = Some(Double.MaxValue),
              intReceivedCY = Double.MaxValue,
              arrearsPrevYears = Some(Yes),
              amountOfArrears = Some(Double.MaxValue),
              amountOutstanding = Some(Double.MaxValue)
            )
          )
        )
      )

      val expected: Loans = Loans(
        recordVersion = Some("001"),
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
            datePeriodLoanDetails = LoanPeriod(sampleToday, Double.MaxValue, Int.MaxValue),
            loanAmountDetails = LoanAmountDetails(Double.MaxValue, Some(Double.MaxValue), Some(Double.MaxValue)),
            equalInstallments = true,
            loanInterestDetails = LoanInterestDetails(Double.MaxValue, Double.MaxValue, Double.MaxValue),
            optSecurityGivenDetails = None,
            optOutstandingArrearsOnLoan = Some(Double.MaxValue)
          )
        )
      )
      transformation.transform(etmpLoans) shouldMatchTo expected
    }

    "for UKCompany" in {
      val etmpLoans = EtmpLoans(
        recordVersion = Some("001"),
        schemeHadLoans = Yes,
        noOfLoans = Some(1),
        loanTransactions = Some(
          List(
            EtmpLoanTransactions(
              dateOfLoan = sampleToday,
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
              capRepaymentCY = Some(Double.MaxValue),
              intReceivedCY = Double.MaxValue,
              arrearsPrevYears = Some(Yes),
              amountOfArrears = Some(Double.MaxValue),
              amountOutstanding = Some(Double.MaxValue)
            )
          )
        )
      )

      val expected: Loans = Loans(
        recordVersion = Some("001"),
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
            datePeriodLoanDetails = LoanPeriod(sampleToday, Double.MaxValue, Int.MaxValue),
            loanAmountDetails = LoanAmountDetails(Double.MaxValue, Some(Double.MaxValue), Some(Double.MaxValue)),
            equalInstallments = true,
            loanInterestDetails = LoanInterestDetails(Double.MaxValue, Double.MaxValue, Double.MaxValue),
            optSecurityGivenDetails = None,
            optOutstandingArrearsOnLoan = Some(Double.MaxValue)
          )
        )
      )

      transformation.transform(etmpLoans) shouldMatchTo expected
    }

    "for UKPartnership" in {

      val etmpLoans = EtmpLoans(
        recordVersion = Some("001"),
        schemeHadLoans = Yes,
        noOfLoans = Some(1),
        loanTransactions = Some(
          List(
            EtmpLoanTransactions(
              dateOfLoan = sampleToday,
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
              capRepaymentCY = Some(Double.MaxValue),
              intReceivedCY = Double.MaxValue,
              arrearsPrevYears = Some(No),
              amountOfArrears = None,
              amountOutstanding = Some(Double.MaxValue)
            )
          )
        )
      )

      val expected: Loans = Loans(
        recordVersion = Some("001"),
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
            datePeriodLoanDetails = LoanPeriod(sampleToday, Double.MaxValue, Int.MaxValue),
            loanAmountDetails = LoanAmountDetails(Double.MaxValue, Some(Double.MaxValue), Some(Double.MaxValue)),
            equalInstallments = false,
            loanInterestDetails = LoanInterestDetails(Double.MaxValue, Double.MaxValue, Double.MaxValue),
            optSecurityGivenDetails = Some("SecurityGivenDetails"),
            optOutstandingArrearsOnLoan = None
          )
        )
      )

      transformation.transform(etmpLoans) shouldMatchTo expected
    }

    "for Other" in {

      val etmpLoans = EtmpLoans(
        recordVersion = Some("001"),
        schemeHadLoans = Yes,
        noOfLoans = Some(1),
        loanTransactions = Some(
          List(
            EtmpLoanTransactions(
              dateOfLoan = sampleToday,
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
              capRepaymentCY = Some(Double.MaxValue),
              intReceivedCY = Double.MaxValue,
              arrearsPrevYears = Some(No),
              amountOfArrears = None,
              amountOutstanding = Some(Double.MaxValue)
            )
          )
        )
      )

      val expected: Loans = Loans(
        recordVersion = Some("001"),
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
            datePeriodLoanDetails = LoanPeriod(sampleToday, Double.MaxValue, Int.MaxValue),
            loanAmountDetails = LoanAmountDetails(Double.MaxValue, Some(Double.MaxValue), Some(Double.MaxValue)),
            equalInstallments = false,
            loanInterestDetails = LoanInterestDetails(Double.MaxValue, Double.MaxValue, Double.MaxValue),
            optSecurityGivenDetails = Some("SecurityGivenDetails"),
            optOutstandingArrearsOnLoan = None
          )
        )
      )
      transformation.transform(etmpLoans) shouldMatchTo expected
    }
  }

}
