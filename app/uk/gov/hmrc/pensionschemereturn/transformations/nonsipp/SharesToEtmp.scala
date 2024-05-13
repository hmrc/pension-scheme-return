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

import com.google.inject.{Inject, Singleton}
import uk.gov.hmrc.pensionschemereturn.models.etmp.nonsipp.common.EtmpIdentityType
import uk.gov.hmrc.pensionschemereturn.models.etmp.nonsipp._
import uk.gov.hmrc.pensionschemereturn.transformations.Transformer
import uk.gov.hmrc.pensionschemereturn.models.nonsipp.shares.HowSharesDisposed.howSharesDisposedToString
import uk.gov.hmrc.pensionschemereturn.models.nonsipp.shares.{ShareTransaction, Shares, TypeOfShares}
import uk.gov.hmrc.pensionschemereturn.models.etmp.YesNo

@Singleton()
class SharesToEtmp @Inject() extends Transformer {

  def transform(shares: Shares): EtmpShares = {

    val optShareTransactions = shares.optShareTransactions

    val (sponsoringEmployerFlag, sponsoringEmployerCount) =
      transformToSharesFlagCountTuple(optShareTransactions, TypeOfShares.SponsoringEmployer)
    val (unquotedFlag, unquotedCount) =
      transformToSharesFlagCountTuple(optShareTransactions, TypeOfShares.Unquoted)
    val (connectedPartyFlag, connectedPartyCount) =
      transformToSharesFlagCountTuple(optShareTransactions, TypeOfShares.ConnectedParty)

    EtmpShares(
      recordVersion = shares.recordVersion,
      sponsorEmployerSharesWereHeld = YesNo(sponsoringEmployerFlag),
      noOfSponsEmplyrShareTransactions = Option.when(sponsoringEmployerFlag)(sponsoringEmployerCount),
      unquotedSharesWereHeld = YesNo(unquotedFlag),
      noOfUnquotedShareTransactions = Option.when(unquotedFlag)(unquotedCount),
      connectedPartySharesWereHeld = YesNo(connectedPartyFlag),
      noOfConnPartyTransactions = Option.when(connectedPartyFlag)(connectedPartyCount),
      sponsorEmployerSharesWereDisposed =
        transformToSharesDisposalFlag(optShareTransactions, TypeOfShares.SponsoringEmployer),
      unquotedSharesWereDisposed = transformToSharesDisposalFlag(optShareTransactions, TypeOfShares.Unquoted),
      connectedPartySharesWereDisposed =
        transformToSharesDisposalFlag(optShareTransactions, TypeOfShares.ConnectedParty),
      shareTransactions = optShareTransactions.map(_.map(transformShareTransactions)),
      totalValueQuotedShares = shares.optTotalValueQuotedShares.getOrElse(holderValue)
    )
  }

  private def transformToSharesFlagCountTuple(
    optShareTransactions: Option[List[ShareTransaction]],
    typeOfShares: TypeOfShares
  ): (Boolean, Int) = {
    val count = optShareTransactions.fold(0)(_.count(_.typeOfSharesHeld == typeOfShares))
    val flag = count > 0
    (flag, count)
  }

  private def transformToSharesDisposalFlag(
    optShareTransactions: Option[List[ShareTransaction]],
    typeOfShares: TypeOfShares
  ): YesNo =
    YesNo(
      optShareTransactions.fold(false)(
        shareTransactions =>
          shareTransactions
            .exists(
              shareTransaction =>
                (shareTransaction.typeOfSharesHeld == typeOfShares) && shareTransaction.optDisposedSharesTransaction.nonEmpty && shareTransaction.optDisposedSharesTransaction.get.nonEmpty
            )
      )
    )

  private def transformShareTransactions(sharesTransaction: ShareTransaction): EtmpShareTransaction = {
    val shareIdentification = sharesTransaction.shareIdentification
    val heldSharesTransaction = sharesTransaction.heldSharesTransaction
    EtmpShareTransaction(
      typeOfSharesHeld = sharesTransaction.typeOfSharesHeld.name,
      shareIdentification = EtmpShareIdentification(
        nameOfSharesCompany = shareIdentification.nameOfSharesCompany,
        crnNumber = shareIdentification.optCrnNumber,
        reasonNoCRN = shareIdentification.optReasonNoCRN,
        classOfShares = Some(shareIdentification.classOfShares)
      ),
      heldSharesTransaction = EtmpHeldSharesTransaction(
        methodOfHolding = heldSharesTransaction.schemeHoldShare.name,
        dateOfAcqOrContrib = heldSharesTransaction.optDateOfAcqOrContrib,
        totalShares = heldSharesTransaction.totalShares,
        acquiredFromName = heldSharesTransaction.optAcquiredFromName.getOrElse("Default-Acquired-From-Name"), // TODO: Waiting confirmation from Josh
        acquiredFromType = heldSharesTransaction.optPropertyAcquiredFrom
          .map(
            propertyAcquiredFrom =>
              toEtmpIdentityType(
                identityType = propertyAcquiredFrom.identityType,
                optIdNumber = propertyAcquiredFrom.idNumber,
                optReasonNoIdNumber = propertyAcquiredFrom.reasonNoIdNumber,
                optOtherDescription = propertyAcquiredFrom.otherDescription
              )
          )
          .getOrElse( // TODO: Waiting confirmation from Josh
            EtmpIdentityType(
              indivOrOrgType = "04",
              idNumber = None,
              reasonNoIdNumber = None,
              otherDescription = Some("Default-Other-Description")
            )
          ),
        connectedPartyStatus = heldSharesTransaction.optConnectedPartyStatus.map(transformToEtmpConnectedPartyStatus),
        costOfShares = heldSharesTransaction.costOfShares,
        supportedByIndepValuation = heldSharesTransaction.supportedByIndepValuation,
        totalAssetValue = heldSharesTransaction.optTotalAssetValue,
        totalDividendsOrReceipts = heldSharesTransaction.totalDividendsOrReceipts
      ),
      disposedSharesTransaction = sharesTransaction.optDisposedSharesTransaction
        .map(
          _.map(
            disposedSharesTransaction =>
              EtmpDisposedSharesTransaction(
                methodOfDisposal = howSharesDisposedToString(disposedSharesTransaction.methodOfDisposal),
                otherMethod = disposedSharesTransaction.optOtherMethod,
                salesQuestions = disposedSharesTransaction.optSalesQuestions.map(
                  salesQuestions =>
                    EtmpSalesQuestions(
                      dateOfSale = salesQuestions.dateOfSale,
                      noOfSharesSold = salesQuestions.noOfSharesSold,
                      amountReceived = salesQuestions.amountReceived,
                      nameOfPurchaser = salesQuestions.nameOfPurchaser,
                      purchaserType = toEtmpIdentityType(
                        identityType = salesQuestions.purchaserType.identityType,
                        optIdNumber = salesQuestions.purchaserType.idNumber,
                        optReasonNoIdNumber = salesQuestions.purchaserType.reasonNoIdNumber,
                        optOtherDescription = salesQuestions.purchaserType.otherDescription
                      ),
                      connectedPartyStatus = transformToEtmpConnectedPartyStatus(salesQuestions.connectedPartyStatus),
                      supportedByIndepValuation = salesQuestions.supportedByIndepValuation
                    )
                ),
                redemptionQuestions = disposedSharesTransaction.optRedemptionQuestions.map(
                  redemptionQuestions =>
                    EtmpRedemptionQuestions(
                      dateOfRedemption = redemptionQuestions.dateOfRedemption,
                      noOfSharesRedeemed = redemptionQuestions.noOfSharesRedeemed,
                      amountReceived = redemptionQuestions.amountReceived
                    )
                ),
                totalSharesNowHeld = disposedSharesTransaction.totalSharesNowHeld
              )
          ).toList
        )
        .filter(_.nonEmpty)
    )
  }
}
