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

import com.softwaremill.diffx.generic.auto.diffForCaseClass
import com.softwaremill.diffx.scalatest.DiffShouldMatcher
import org.mockito.MockitoSugar
import org.scalatestplus.play.PlaySpec
import uk.gov.hmrc.pensionschemereturn.models.etmp.YesNo
import uk.gov.hmrc.pensionschemereturn.models.etmp.nonsipp._
import uk.gov.hmrc.pensionschemereturn.models.nonsipp.HowSharesDisposed.{Other, Redeemed, Sold, Transferred}
import uk.gov.hmrc.pensionschemereturn.models.nonsipp._
import uk.gov.hmrc.pensionschemereturn.transformations.Transformer

import java.time.LocalDate

class SharesFromEtmpSpec extends PlaySpec with MockitoSugar with Transformer with DiffShouldMatcher {

  private val transformation: SharesFromEtmp = new SharesFromEtmp()
  val today: LocalDate = LocalDate.now

  "SharesFromEtmp - PSR Shares should successfully transform from etmp format " should {
    "Shares with an empty disposedSharesTransaction sequence" in {

      val shares = EtmpShares(
        recordVersion = None,
        sponsorEmployerSharesWereHeld = YesNo.Yes,
        noOfSponsEmplyrShareTransactions = Some(1),
        unquotedSharesWereHeld = YesNo.Yes,
        noOfUnquotedShareTransactions = Some(1),
        connectedPartySharesWereHeld = YesNo.Yes,
        noOfConnPartyTransactions = Some(1),
        sponsorEmployerSharesWereDisposed = YesNo.Yes,
        unquotedSharesWereDisposed = YesNo.Yes,
        connectedPartySharesWereDisposed = YesNo.No,
        shareTransactions = Some(
          List(
            EtmpShareTransaction(
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
                totalDividendsOrReceipts = Double.MaxValue
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
                acquiredFromName = "Default-Acquired-From-Name",
                acquiredFromType = EtmpIdentityType(
                  indivOrOrgType = "04",
                  idNumber = None,
                  reasonNoIdNumber = None,
                  otherDescription = Some("Default-Other-Description")
                ),
                connectedPartyStatus = Some("02"),
                costOfShares = Double.MaxValue,
                supportedByIndepValuation = YesNo.No,
                totalAssetValue = None,
                totalDividendsOrReceipts = Double.MaxValue
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
                acquiredFromName = "Default-Acquired-From-Name",
                acquiredFromType = EtmpIdentityType(
                  indivOrOrgType = "04",
                  idNumber = None,
                  reasonNoIdNumber = None,
                  otherDescription = Some("Default-Other-Description")
                ),
                connectedPartyStatus = None,
                costOfShares = Double.MaxValue,
                supportedByIndepValuation = YesNo.No,
                totalAssetValue = None,
                totalDividendsOrReceipts = Double.MaxValue
              ),
              disposedSharesTransaction = None
            )
          )
        ),
        totalValueQuotedShares = 0.00
      )
      val expected = Shares(
        optShareTransactions = Some(
          List(
            ShareTransaction(
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
                totalDividendsOrReceipts = Double.MaxValue
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
                optAcquiredFromName = Some("Default-Acquired-From-Name"),
                optPropertyAcquiredFrom = Some(
                  PropertyAcquiredFrom(
                    IdentityType.Other,
                    None,
                    None,
                    Some("Default-Other-Description")
                  )
                ),
                optConnectedPartyStatus = Some(false),
                costOfShares = Double.MaxValue,
                supportedByIndepValuation = false,
                optTotalAssetValue = None,
                totalDividendsOrReceipts = Double.MaxValue
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
                optAcquiredFromName = Some("Default-Acquired-From-Name"),
                optPropertyAcquiredFrom = Some(
                  PropertyAcquiredFrom(
                    IdentityType.Other,
                    None,
                    None,
                    Some("Default-Other-Description")
                  )
                ),
                optConnectedPartyStatus = None,
                costOfShares = Double.MaxValue,
                supportedByIndepValuation = false,
                optTotalAssetValue = None,
                totalDividendsOrReceipts = Double.MaxValue
              ),
              optDisposedSharesTransaction = None
            )
          )
        )
      )

      transformation.transform(shares) shouldMatchTo expected
    }
  }
}
