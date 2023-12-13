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
import uk.gov.hmrc.pensionschemereturn.models.etmp.nonsipp._
import uk.gov.hmrc.pensionschemereturn.models.nonsipp.Assets
import uk.gov.hmrc.pensionschemereturn.models.nonsipp.HowDisposed.howDisposedToString
import uk.gov.hmrc.pensionschemereturn.models.nonsipp.SchemeHoldLandProperty.schemeHoldLandPropertyToString
import uk.gov.hmrc.pensionschemereturn.transformations.Transformer

@Singleton()
class AssetsToEtmp @Inject() extends Transformer {

  def transform(assets: Assets): EtmpAssets =
    EtmpAssets(
      landOrProperty = EtmpLandOrProperty(
        recordVersion = None,
        heldAnyLandOrProperty = toYesNo(assets.landOrProperty.landOrPropertyHeld),
        disposeAnyLandOrProperty = toYesNo(assets.landOrProperty.disposeAnyLandOrProperty),
        noOfTransactions = assets.landOrProperty.landOrPropertyTransactions.size,
        landOrPropertyTransactions = assets.landOrProperty.landOrPropertyTransactions.map(
          landOrPropertyTransaction => {

            val propertyDetails = landOrPropertyTransaction.propertyDetails
            val heldPropertyTransaction = landOrPropertyTransaction.heldPropertyTransaction
            val optDisposedPropertyTransaction = landOrPropertyTransaction.optDisposedPropertyTransaction
            val landRegistryReferenceExist = propertyDetails.landRegistryTitleNumberKey
            val landRegistryReferenceValue = propertyDetails.landRegistryTitleNumberValue
            val addressDetails = propertyDetails.addressDetails

            EtmpLandOrPropertyTransactions(
              propertyDetails = EtmpPropertyDetails(
                landOrPropertyInUK = toYesNo(propertyDetails.landOrPropertyInUK),
                addressDetails = EtmpAddress(
                  addressLine1 = addressDetails.addressLine1,
                  addressLine2 = addressDetails.addressLine2.getOrElse(addressDetails.town),
                  addressLine3 = addressDetails.addressLine3,
                  addressLine4 = addressDetails.addressLine2.map(_ => addressDetails.town),
                  addressLine5 = None,
                  ukPostCode = addressDetails.postCode,
                  countryCode = addressDetails.countryCode
                ),
                landRegistryDetails = EtmpLandRegistryDetails(
                  toYesNo(landRegistryReferenceExist),
                  Option.when(landRegistryReferenceExist)(landRegistryReferenceValue),
                  Option.when(!landRegistryReferenceExist)(landRegistryReferenceValue)
                )
              ),
              heldPropertyTransaction = EtmpHeldPropertyTransaction(
                methodOfHolding = schemeHoldLandPropertyToString(heldPropertyTransaction.methodOfHolding),
                dateOfAcquisitionOrContribution = heldPropertyTransaction.dateOfAcquisitionOrContribution,
                propertyAcquiredFromName = heldPropertyTransaction.optPropertyAcquiredFromName,
                propertyAcquiredFrom = heldPropertyTransaction.optPropertyAcquiredFrom.map(
                  propertyAcquiredFrom =>
                    transformToEtmpIdentityType(
                      identityType = propertyAcquiredFrom.identityType,
                      optIdNumber = propertyAcquiredFrom.idNumber,
                      optReasonNoIdNumber = propertyAcquiredFrom.reasonNoIdNumber,
                      optOtherDescription = propertyAcquiredFrom.otherDescription
                    )
                ),
                connectedPartyStatus =
                  heldPropertyTransaction.optConnectedPartyStatus.map(transformToEtmpConnectedPartyStatus),
                totalCostOfLandOrProperty = heldPropertyTransaction.totalCostOfLandOrProperty,
                indepValuationSupport = heldPropertyTransaction.optIndepValuationSupport.map(toYesNo),
                residentialSchedule29A = toYesNo(heldPropertyTransaction.isLandOrPropertyResidential),
                landOrPropertyLeased = toYesNo(heldPropertyTransaction.landOrPropertyLeased),
                leaseDetails = heldPropertyTransaction.optLeaseDetails.map(
                  leaseDetails =>
                    EtmpLeaseDetails(
                      lesseeName = leaseDetails.lesseeName,
                      connectedPartyStatus = transformToEtmpConnectedPartyStatus(leaseDetails.connectedPartyStatus),
                      leaseGrantDate = leaseDetails.leaseGrantDate,
                      annualLeaseAmount = leaseDetails.annualLeaseAmount
                    )
                ),
                totalIncomeOrReceipts = heldPropertyTransaction.totalIncomeOrReceipts
              ),
              disposedPropertyTransaction = optDisposedPropertyTransaction.map(
                _.map(
                  disposedPropertyTransaction =>
                    EtmpDisposedPropertyTransaction(
                      methodOfDisposal = howDisposedToString(disposedPropertyTransaction.methodOfDisposal),
                      otherMethod = disposedPropertyTransaction.optOtherMethod,
                      dateOfSale = disposedPropertyTransaction.optDateOfSale,
                      nameOfPurchaser = disposedPropertyTransaction.optNameOfPurchaser,
                      purchaseOrgDetails = disposedPropertyTransaction.optPropertyAcquiredFrom.map(
                        propertyAcquiredFrom =>
                          transformToEtmpIdentityType(
                            identityType = propertyAcquiredFrom.identityType,
                            optIdNumber = propertyAcquiredFrom.idNumber,
                            optReasonNoIdNumber = propertyAcquiredFrom.reasonNoIdNumber,
                            optOtherDescription = propertyAcquiredFrom.otherDescription
                          )
                      ),
                      saleProceeds = disposedPropertyTransaction.optSaleProceeds,
                      connectedPartyStatus =
                        disposedPropertyTransaction.optConnectedPartyStatus.map(transformToEtmpConnectedPartyStatus),
                      indepValuationSupport = disposedPropertyTransaction.optIndepValuationSupport.map(toYesNo),
                      portionStillHeld = toYesNo(disposedPropertyTransaction.portionStillHeld)
                    )
                )
              )
            )
          }
        )
      ),
      borrowing = EtmpBorrowing(
        recordVersion = None,
        moneyWasBorrowed = toYesNo(assets.borrowing.moneyWasBorrowed),
        noOfBorrows = Option.when(assets.borrowing.moneyWasBorrowed)(assets.borrowing.moneyBorrowed.size),
        moneyBorrowed = assets.borrowing.moneyBorrowed.map(
          mb =>
            EtmpMoneyBorrowed(
              dateOfBorrow = mb.dateOfBorrow,
              schemeAssetsValue = mb.schemeAssetsValue,
              amountBorrowed = mb.amountBorrowed,
              interestRate = mb.interestRate,
              borrowingFromName = mb.borrowingFromName,
              connectedPartyStatus = transformToEtmpConnectedPartyStatus(mb.connectedPartyStatus),
              reasonForBorrow = mb.reasonForBorrow
            )
        )
      ),
      bonds = EtmpBonds(
        bondsWereAdded = toYesNo(false),
        bondsWereDisposed = toYesNo(false)
      ),
      otherAssets = EtmpOtherAssets(
        otherAssetsWereHeld = toYesNo(false),
        otherAssetsWereDisposed = toYesNo(false)
      )
    )
}
