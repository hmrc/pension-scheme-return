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

import com.softwaremill.diffx.scalatest.DiffShouldMatcher
import uk.gov.hmrc.pensionschemereturn.models.etmp.nonsipp.common.EtmpIdentityType
import uk.gov.hmrc.pensionschemereturn.models.nonsipp._
import uk.gov.hmrc.pensionschemereturn.models.etmp.nonsipp.{EtmpLoanTransactions, EtmpLoans}
import uk.gov.hmrc.pensionschemereturn.transformations.Transformer
import com.softwaremill.diffx.generic.auto.indicator
import org.scalatestplus.play.PlaySpec
import org.scalatestplus.mockito.MockitoSugar

import java.time.LocalDate

class LoansToEtmpSpec extends PlaySpec with MockitoSugar with Transformer with DiffShouldMatcher {
  private val transformation: LoansToEtmp = new LoansToEtmp()
  val today: LocalDate = LocalDate.now

  "LoansToEtmp - PSR Loans should successfully transform to etmp format " should {
    List(
      (None, No),
      (Some(ConnectedParty), No),
      (Some(Sponsoring), Yes),
      (Some(Neither), No)
    ).foreach { case (inputRecipientSponsoringEmployer, resultRecipientSponsoringEmployer) =>
      s"as an Individual for recipientSponsoringEmployer : $inputRecipientSponsoringEmployer" in {
        val loans: Loans = Loans(
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
              optRecipientSponsoringEmployer = inputRecipientSponsoringEmployer,
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
          recordVersion = Some("001"),
          schemeHadLoans = Yes,
          noOfLoans = Some(1),
          loanTransactions = Some(
            List(
              EtmpLoanTransactions(
                dateOfLoan = today,
                loanRecipientName = "IndividualName",
                recipientIdentityType = EtmpIdentityType(
                  indivOrOrgType = "01",
                  idNumber = None,
                  reasonNoIdNumber = Some("NoNinoReason"),
                  otherDescription = None
                ),
                recipientSponsoringEmployer = resultRecipientSponsoringEmployer,
                connectedPartyStatus = Connected,
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
        )

        transformation.transform(loans) shouldMatchTo expected
      }
    }

    s"when schemeHadLoans is false and loanTransactions is empty" in {
      val loans: Loans = Loans(
        recordVersion = None,
        schemeHadLoans = false,
        loanTransactions = List.empty
      )

      val expected = EtmpLoans(
        recordVersion = None,
        schemeHadLoans = No,
        noOfLoans = None,
        loanTransactions = None
      )

      transformation.transform(loans) shouldMatchTo expected
    }
  }
}
