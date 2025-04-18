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
import uk.gov.hmrc.pensionschemereturn.models.etmp.nonsipp._
import uk.gov.hmrc.pensionschemereturn.transformations.Transformer
import uk.gov.hmrc.pensionschemereturn.models.nonsipp.shares.HowSharesDisposed._
import com.softwaremill.diffx.generic.auto.indicator
import uk.gov.hmrc.pensionschemereturn.models.nonsipp.shares._
import uk.gov.hmrc.pensionschemereturn.models.etmp.YesNo
import org.scalatestplus.play.PlaySpec
import org.scalatestplus.mockito.MockitoSugar

import java.time.LocalDate

class SharesToEtmpSpec extends PlaySpec with MockitoSugar with Transformer with DiffShouldMatcher {

  private val transformation: SharesToEtmp = new SharesToEtmp()
  val today: LocalDate = LocalDate.now

  "SharesToEtmp - PSR Shares" should {
    val shareTransactions =
      List(
        ShareTransaction(
          prePopulated = None,
          typeOfSharesHeld = TypeOfShares.SponsoringEmployer,
          shareIdentification = ShareIdentification(
            nameOfSharesCompany = "nameOfSharesCompany",
            optCrnNumber = None,
            optReasonNoCRN = Some("NoCrnReason"),
            classOfShares = "classOfShares"
          ),
          heldSharesTransaction = HeldSharesTransaction(
            schemeHoldShare = SchemeHoldShare.Acquisition,
            optDateOfAcqOrContrib = Some(today),
            totalShares = Int.MaxValue,
            optAcquiredFromName = Some("propertyAcquiredFromName"),
            optPropertyAcquiredFrom = Some(
              PropertyAcquiredFrom(
                IdentityType.UKPartnership,
                None,
                Some("NoUtrReason"),
                None
              )
            ),
            optConnectedPartyStatus = None,
            costOfShares = Double.MaxValue,
            supportedByIndepValuation = false,
            optTotalAssetValue = Some(Double.MaxValue),
            optTotalDividendsOrReceipts = Some(Double.MaxValue)
          ),
          optDisposedSharesTransaction = Some(
            Seq(
              DisposedSharesTransaction(
                methodOfDisposal = Sold,
                optOtherMethod = None,
                optSalesQuestions = Some(
                  SalesQuestions(
                    dateOfSale = today,
                    noOfSharesSold = Int.MaxValue,
                    amountReceived = Double.MaxValue,
                    nameOfPurchaser = "nameOfPurchaser",
                    purchaserType = PropertyAcquiredFrom(
                      IdentityType.Other,
                      None,
                      None,
                      Some("Other")
                    ),
                    connectedPartyStatus = true,
                    supportedByIndepValuation = false
                  )
                ),
                optRedemptionQuestions = None,
                totalSharesNowHeld = Int.MaxValue
              ),
              DisposedSharesTransaction(
                methodOfDisposal = Transferred,
                optOtherMethod = None,
                optSalesQuestions = None,
                optRedemptionQuestions = None,
                totalSharesNowHeld = Int.MaxValue
              )
            )
          )
        ),
        ShareTransaction(
          prePopulated = None,
          typeOfSharesHeld = TypeOfShares.Unquoted,
          shareIdentification = ShareIdentification(
            nameOfSharesCompany = "nameOfSharesCompany",
            optCrnNumber = None,
            optReasonNoCRN = Some("NoCrnReason"),
            classOfShares = "classOfShares"
          ),
          heldSharesTransaction = HeldSharesTransaction(
            schemeHoldShare = SchemeHoldShare.Contribution,
            optDateOfAcqOrContrib = None,
            totalShares = Int.MaxValue,
            optAcquiredFromName = None,
            optPropertyAcquiredFrom = None,
            optConnectedPartyStatus = Some(true),
            costOfShares = Double.MaxValue,
            supportedByIndepValuation = false,
            optTotalAssetValue = None,
            optTotalDividendsOrReceipts = Some(Double.MaxValue)
          ),
          optDisposedSharesTransaction = Some(
            Seq(
              DisposedSharesTransaction(
                methodOfDisposal = Redeemed,
                optOtherMethod = None,
                optSalesQuestions = None,
                optRedemptionQuestions = Some(
                  RedemptionQuestions(
                    dateOfRedemption = today,
                    noOfSharesRedeemed = Int.MaxValue,
                    amountReceived = Double.MaxValue
                  )
                ),
                totalSharesNowHeld = Int.MaxValue
              ),
              DisposedSharesTransaction(
                methodOfDisposal = Other,
                optOtherMethod = Some("OtherMethod"),
                optSalesQuestions = None,
                optRedemptionQuestions = None,
                totalSharesNowHeld = Int.MaxValue
              )
            )
          )
        ),
        ShareTransaction(
          prePopulated = None,
          typeOfSharesHeld = TypeOfShares.ConnectedParty,
          shareIdentification = ShareIdentification(
            nameOfSharesCompany = "nameOfSharesCompany",
            optCrnNumber = Some("CrnNumber"),
            optReasonNoCRN = None,
            classOfShares = "classOfShares"
          ),
          heldSharesTransaction = HeldSharesTransaction(
            schemeHoldShare = SchemeHoldShare.Transfer,
            optDateOfAcqOrContrib = None,
            totalShares = Int.MaxValue,
            optAcquiredFromName = None,
            optPropertyAcquiredFrom = None,
            optConnectedPartyStatus = None,
            costOfShares = Double.MaxValue,
            supportedByIndepValuation = false,
            optTotalAssetValue = None,
            optTotalDividendsOrReceipts = Some(Double.MaxValue)
          ),
          optDisposedSharesTransaction = None
        )
      )
    val etmpShareTransactions = List(
      EtmpShareTransaction(
        prePopulated = None,
        typeOfSharesHeld = "01",
        shareIdentification = EtmpShareIdentification(
          nameOfSharesCompany = "nameOfSharesCompany",
          crnNumber = None,
          reasonNoCRN = Some("NoCrnReason"),
          classOfShares = Some("classOfShares")
        ),
        heldSharesTransaction = EtmpHeldSharesTransaction(
          methodOfHolding = "01",
          dateOfAcqOrContrib = Some(today),
          totalShares = Int.MaxValue,
          acquiredFromName = "propertyAcquiredFromName",
          acquiredFromType = EtmpIdentityType(
            indivOrOrgType = "03",
            idNumber = None,
            reasonNoIdNumber = Some("NoUtrReason"),
            otherDescription = None
          ),
          connectedPartyStatus = None,
          costOfShares = Double.MaxValue,
          supportedByIndepValuation = YesNo.No,
          totalAssetValue = Some(Double.MaxValue),
          totalDividendsOrReceipts = Some(Double.MaxValue)
        ),
        disposedSharesTransaction = Some(
          List(
            EtmpDisposedSharesTransaction(
              methodOfDisposal = "01",
              otherMethod = None,
              salesQuestions = Some(
                EtmpSalesQuestions(
                  dateOfSale = today,
                  noOfSharesSold = Int.MaxValue,
                  amountReceived = Double.MaxValue,
                  nameOfPurchaser = "nameOfPurchaser",
                  purchaserType = EtmpIdentityType(
                    indivOrOrgType = "04",
                    idNumber = None,
                    reasonNoIdNumber = None,
                    otherDescription = Some("Other")
                  ),
                  connectedPartyStatus = "01",
                  supportedByIndepValuation = YesNo.No
                )
              ),
              redemptionQuestions = None,
              totalSharesNowHeld = Int.MaxValue
            ),
            EtmpDisposedSharesTransaction(
              methodOfDisposal = "03",
              otherMethod = None,
              salesQuestions = None,
              redemptionQuestions = None,
              totalSharesNowHeld = Int.MaxValue
            )
          )
        )
      ),
      EtmpShareTransaction(
        prePopulated = None,
        typeOfSharesHeld = "02",
        shareIdentification = EtmpShareIdentification(
          nameOfSharesCompany = "nameOfSharesCompany",
          crnNumber = None,
          reasonNoCRN = Some("NoCrnReason"),
          classOfShares = Some("classOfShares")
        ),
        heldSharesTransaction = EtmpHeldSharesTransaction(
          methodOfHolding = "02",
          dateOfAcqOrContrib = None,
          totalShares = Int.MaxValue,
          acquiredFromName = "QUESTION NOT ASKED",
          acquiredFromType = EtmpIdentityType(
            indivOrOrgType = "04",
            idNumber = None,
            reasonNoIdNumber = None,
            otherDescription = Some("QUESTION NOT ASKED")
          ),
          connectedPartyStatus = Some("01"),
          costOfShares = Double.MaxValue,
          supportedByIndepValuation = YesNo.No,
          totalAssetValue = None,
          totalDividendsOrReceipts = Some(Double.MaxValue)
        ),
        disposedSharesTransaction = Some(
          List(
            EtmpDisposedSharesTransaction(
              methodOfDisposal = "02",
              otherMethod = None,
              salesQuestions = None,
              redemptionQuestions = Some(
                EtmpRedemptionQuestions(
                  dateOfRedemption = today,
                  noOfSharesRedeemed = Int.MaxValue,
                  amountReceived = Double.MaxValue
                )
              ),
              totalSharesNowHeld = Int.MaxValue
            ),
            EtmpDisposedSharesTransaction(
              methodOfDisposal = "04",
              otherMethod = Some("OtherMethod"),
              salesQuestions = None,
              redemptionQuestions = None,
              totalSharesNowHeld = Int.MaxValue
            )
          )
        )
      ),
      EtmpShareTransaction(
        prePopulated = None,
        typeOfSharesHeld = "03",
        shareIdentification = EtmpShareIdentification(
          nameOfSharesCompany = "nameOfSharesCompany",
          crnNumber = Some("CrnNumber"),
          reasonNoCRN = None,
          classOfShares = Some("classOfShares")
        ),
        heldSharesTransaction = EtmpHeldSharesTransaction(
          methodOfHolding = "03",
          dateOfAcqOrContrib = None,
          totalShares = Int.MaxValue,
          acquiredFromName = "QUESTION NOT ASKED",
          acquiredFromType = EtmpIdentityType(
            indivOrOrgType = "04",
            idNumber = None,
            reasonNoIdNumber = None,
            otherDescription = Some("QUESTION NOT ASKED")
          ),
          connectedPartyStatus = None,
          costOfShares = Double.MaxValue,
          supportedByIndepValuation = YesNo.No,
          totalAssetValue = None,
          totalDividendsOrReceipts = Some(Double.MaxValue)
        ),
        disposedSharesTransaction = None
      )
    )

    "successfully transform to etmp format" in {

      val shares = Shares(
        recordVersion = Some("001"),
        optDidSchemeHoldAnyShares = Some(true),
        optShareTransactions = Some(shareTransactions),
        optTotalValueQuotedShares = None
      )

      val expected = EtmpShares(
        recordVersion = Some("001"),
        sponsorEmployerSharesWereHeld = Some(YesNo.Yes),
        noOfSponsEmplyrShareTransactions = Some(1),
        unquotedSharesWereHeld = Some(YesNo.Yes),
        noOfUnquotedShareTransactions = Some(1),
        connectedPartySharesWereHeld = Some(YesNo.Yes),
        noOfConnPartyTransactions = Some(1),
        sponsorEmployerSharesWereDisposed = Some(YesNo.Yes),
        unquotedSharesWereDisposed = Some(YesNo.Yes),
        connectedPartySharesWereDisposed = Some(YesNo.No),
        shareTransactions = Some(etmpShareTransactions),
        totalValueQuotedShares = -0.01
      )

      transformation.transform(shares) shouldMatchTo expected
    }
    "successfully transform to etmp format in pre-population" in {

      val shares = Shares(
        recordVersion = None,
        optDidSchemeHoldAnyShares = None,
        optShareTransactions = Some(shareTransactions.map(_.copy(prePopulated = Some(true)))),
        optTotalValueQuotedShares = Some(Double.MaxValue)
      )

      val expected = EtmpShares(
        recordVersion = None,
        sponsorEmployerSharesWereHeld = None,
        noOfSponsEmplyrShareTransactions = None,
        unquotedSharesWereHeld = None,
        noOfUnquotedShareTransactions = None,
        connectedPartySharesWereHeld = None,
        noOfConnPartyTransactions = None,
        sponsorEmployerSharesWereDisposed = None,
        unquotedSharesWereDisposed = None,
        connectedPartySharesWereDisposed = None,
        shareTransactions = Some(etmpShareTransactions.map(_.copy(prePopulated = Some(YesNo.Yes)))),
        totalValueQuotedShares = Double.MaxValue
      )

      transformation.transform(shares) shouldMatchTo expected
    }

    "successfully transform to etmp format with quoted shares" in {

      val shares = Shares(
        recordVersion = None,
        optDidSchemeHoldAnyShares = Some(true),
        optShareTransactions = Some(shareTransactions),
        optTotalValueQuotedShares = Some(Double.MaxValue)
      )

      val expected = EtmpShares(
        recordVersion = None,
        sponsorEmployerSharesWereHeld = Some(YesNo.Yes),
        noOfSponsEmplyrShareTransactions = Some(1),
        unquotedSharesWereHeld = Some(YesNo.Yes),
        noOfUnquotedShareTransactions = Some(1),
        connectedPartySharesWereHeld = Some(YesNo.Yes),
        noOfConnPartyTransactions = Some(1),
        sponsorEmployerSharesWereDisposed = Some(YesNo.Yes),
        unquotedSharesWereDisposed = Some(YesNo.Yes),
        connectedPartySharesWereDisposed = Some(YesNo.No),
        shareTransactions = Some(etmpShareTransactions),
        totalValueQuotedShares = Double.MaxValue
      )

      transformation.transform(shares) shouldMatchTo expected
    }
  }
}
