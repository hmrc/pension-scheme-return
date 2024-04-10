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

import uk.gov.hmrc.pensionschemereturn.models.nonsipp.shares.TypeOfShares.stringToTypeOfShares
import uk.gov.hmrc.pensionschemereturn.models.nonsipp.shares.SchemeHoldShare.stringToSchemeHoldShare
import uk.gov.hmrc.pensionschemereturn.models.nonsipp.IdentityType.stringToIdentityType
import com.google.inject.{Inject, Singleton}
import uk.gov.hmrc.pensionschemereturn.models.nonsipp._
import uk.gov.hmrc.pensionschemereturn.models.etmp.nonsipp.{EtmpShareTransaction, EtmpShares}
import uk.gov.hmrc.pensionschemereturn.transformations.Transformer
import uk.gov.hmrc.pensionschemereturn.models.nonsipp.shares.HowSharesDisposed.stringToHowSharesDisposed
import uk.gov.hmrc.pensionschemereturn.models.nonsipp.shares._
import uk.gov.hmrc.pensionschemereturn.models.etmp.YesNo

@Singleton()
class SharesFromEtmp @Inject() extends Transformer {

  def transform(shares: EtmpShares): Shares =
    Shares(
      optShareTransactions = shares.shareTransactions.map(_.map(transformShareTransactions)),
      optTotalValueQuotedShares =
        Option.when(shares.totalValueQuotedShares != holderValue)(shares.totalValueQuotedShares)
    )

  private def transformShareTransactions(shareTransactions: EtmpShareTransaction): ShareTransaction = {

    val shareIdentification = shareTransactions.shareIdentification
    val heldSharesTransaction = shareTransactions.heldSharesTransaction
    val acquiredFromType = heldSharesTransaction.acquiredFromType

    ShareTransaction(
      typeOfSharesHeld = stringToTypeOfShares(shareTransactions.typeOfSharesHeld),
      shareIdentification = ShareIdentification(
        nameOfSharesCompany = shareIdentification.nameOfSharesCompany,
        optCrnNumber = shareIdentification.crnNumber,
        optReasonNoCRN = shareIdentification.reasonNoCRN,
        classOfShares = shareIdentification.classOfShares.get
      ),
      heldSharesTransaction = HeldSharesTransaction(
        schemeHoldShare = stringToSchemeHoldShare(heldSharesTransaction.methodOfHolding),
        optDateOfAcqOrContrib = heldSharesTransaction.dateOfAcqOrContrib,
        totalShares = heldSharesTransaction.totalShares,
        optAcquiredFromName = Some(heldSharesTransaction.acquiredFromName),
        optPropertyAcquiredFrom = Some(
          PropertyAcquiredFrom(
            identityType = stringToIdentityType(acquiredFromType.indivOrOrgType),
            idNumber = acquiredFromType.idNumber,
            reasonNoIdNumber = acquiredFromType.reasonNoIdNumber,
            otherDescription = acquiredFromType.otherDescription
          )
        ),
        optConnectedPartyStatus = heldSharesTransaction.connectedPartyStatus.map(_ == Connected),
        costOfShares = heldSharesTransaction.costOfShares,
        supportedByIndepValuation = YesNo.unapply(heldSharesTransaction.supportedByIndepValuation),
        optTotalAssetValue = heldSharesTransaction.totalAssetValue,
        totalDividendsOrReceipts = heldSharesTransaction.totalDividendsOrReceipts
      ),
      optDisposedSharesTransaction = shareTransactions.disposedSharesTransaction.map(
        _.map(
          dst =>
            DisposedSharesTransaction(
              methodOfDisposal = stringToHowSharesDisposed(dst.methodOfDisposal),
              optOtherMethod = dst.otherMethod,
              optSalesQuestions = dst.salesQuestions.map(
                sq => {
                  val purchaserType = sq.purchaserType
                  SalesQuestions(
                    dateOfSale = sq.dateOfSale,
                    noOfSharesSold = sq.noOfSharesSold,
                    amountReceived = sq.amountReceived,
                    nameOfPurchaser = sq.nameOfPurchaser,
                    purchaserType = PropertyAcquiredFrom(
                      identityType = stringToIdentityType(purchaserType.indivOrOrgType),
                      idNumber = purchaserType.idNumber,
                      reasonNoIdNumber = purchaserType.reasonNoIdNumber,
                      otherDescription = purchaserType.otherDescription
                    ),
                    connectedPartyStatus = sq.connectedPartyStatus == Connected,
                    supportedByIndepValuation = YesNo.unapply(sq.supportedByIndepValuation)
                  )
                }
              ),
              optRedemptionQuestions = dst.redemptionQuestions
                .map(
                  rq =>
                    RedemptionQuestions(
                      dateOfRedemption = rq.dateOfRedemption,
                      noOfSharesRedeemed = rq.noOfSharesRedeemed,
                      amountReceived = rq.amountReceived
                    )
                ),
              totalSharesNowHeld = dst.totalSharesNowHeld
            )
        )
      )
    )
  }
}
