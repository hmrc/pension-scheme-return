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

import com.google.inject.{Inject, Singleton}
import uk.gov.hmrc.pensionschemereturn.models.etmp.YesNo
import uk.gov.hmrc.pensionschemereturn.models.etmp.nonsipp._
import uk.gov.hmrc.pensionschemereturn.models.nonsipp.{ShareTransaction, Shares, TypeOfShares}
import uk.gov.hmrc.pensionschemereturn.transformations.Transformer

@Singleton()
class SharesToEtmp @Inject() extends Transformer {

  def transform(shares: Shares): EtmpShares = {

    val sponsoringEmployerCount =
      shares.optShareTransactions.fold(0)(_.count(_.typeOfSharesHeld == TypeOfShares.SponsoringEmployer))
    val unquotedCount = shares.optShareTransactions.fold(0)(_.count(_.typeOfSharesHeld == TypeOfShares.Unquoted))
    val connectedPartyCount =
      shares.optShareTransactions.fold(0)(_.count(_.typeOfSharesHeld == TypeOfShares.ConnectedParty))

    val sponsorEmployerSharesWereHeld = sponsoringEmployerCount > 0
    val unquotedSharesWereHeld = unquotedCount > 0
    val connectedPartySharesWereHeld = connectedPartyCount > 0

    EtmpShares(
      recordVersion = None,
      sponsorEmployerSharesWereHeld = YesNo(sponsorEmployerSharesWereHeld),
      noOfSponsEmplyrShareTransactions = Option.when(sponsorEmployerSharesWereHeld)(sponsoringEmployerCount),
      unquotedSharesWereHeld = YesNo(unquotedSharesWereHeld),
      noOfUnquotedShareTransactions = Option.when(unquotedSharesWereHeld)(unquotedCount),
      connectedPartySharesWereHeld = YesNo(connectedPartySharesWereHeld),
      noOfConnPartyTransactions = Option.when(connectedPartySharesWereHeld)(connectedPartyCount),
      sponsorEmployerSharesWereDisposed = YesNo.No,
      unquotedSharesWereDisposed = YesNo.No,
      connectedPartySharesWereDisposed = YesNo.No,
      shareTransactions = shares.optShareTransactions.map(_.map(transformShareTransactions)),
      totalValueQuotedShares = 0.00 // TODO: Waiting confirmation from Josh
    )
  }

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
      disposedSharesTransaction = None
    )
  }
}
