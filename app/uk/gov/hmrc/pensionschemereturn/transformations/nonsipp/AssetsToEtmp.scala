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

import uk.gov.hmrc.pensionschemereturn.models.nonsipp.assets.SchemeHoldAsset.schemeHoldAssetToString
import com.google.inject.{Inject, Singleton}
import uk.gov.hmrc.pensionschemereturn.models.nonsipp.assets.Assets
import uk.gov.hmrc.pensionschemereturn.models.nonsipp.assets.HowDisposed.howDisposedToString
import uk.gov.hmrc.pensionschemereturn.models.etmp.nonsipp.assets._
import uk.gov.hmrc.pensionschemereturn.transformations.Transformer
import uk.gov.hmrc.pensionschemereturn.models.nonsipp.assets.SchemeHoldBond.schemeHoldBondToString
import uk.gov.hmrc.pensionschemereturn.models.nonsipp.assets.SchemeHoldLandProperty.schemeHoldLandPropertyToString

@Singleton()
class AssetsToEtmp @Inject() extends Transformer {

  def transform(assets: Assets): EtmpAssets =
    EtmpAssets(
      landOrProperty = assets.optLandOrProperty.map(landOrProperty =>
        EtmpLandOrProperty(
          recordVersion = landOrProperty.recordVersion,
          heldAnyLandOrProperty = landOrProperty.optLandOrPropertyHeld.map(toYesNo),
          disposeAnyLandOrProperty = landOrProperty.optDisposeAnyLandOrProperty.map(toYesNo),
          noOfTransactions = Option.when(landOrProperty.optLandOrPropertyHeld.getOrElse(false))(
            landOrProperty.landOrPropertyTransactions.size
          ),
          landOrPropertyTransactions = Option.when(
            landOrProperty.landOrPropertyTransactions.nonEmpty ||
              (landOrProperty.optLandOrPropertyHeld.nonEmpty && landOrProperty.optLandOrPropertyHeld.get)
          )(
            landOrProperty.landOrPropertyTransactions.map { landOrPropertyTransaction =>

              val propertyDetails = landOrPropertyTransaction.propertyDetails
              val heldPropertyTransaction = landOrPropertyTransaction.heldPropertyTransaction
              val optDisposedPropertyTransaction = landOrPropertyTransaction.optDisposedPropertyTransaction
              val landRegistryReferenceExist = propertyDetails.landRegistryTitleNumberKey
              val landRegistryReferenceValue = propertyDetails.landRegistryTitleNumberValue
              val addressDetails = propertyDetails.addressDetails

              EtmpLandOrPropertyTransactions(
                propertyDetails = EtmpPropertyDetails(
                  prePopulated = landOrPropertyTransaction.prePopulated,
                  landOrPropertyInUK = toYesNo(propertyDetails.landOrPropertyInUK),
                  addressDetails = EtmpAddress(
                    addressLine1 = addressDetails.addressLine1,
                    addressLine2 = addressDetails.addressLine2.getOrElse(addressDetails.town),
                    addressLine3 = addressDetails.addressLine3,
                    addressLine4 = addressDetails.addressLine2.map(_ => addressDetails.town),
                    addressLine5 = if (!propertyDetails.landOrPropertyInUK) addressDetails.postCode else None,
                    ukPostCode = if (propertyDetails.landOrPropertyInUK) addressDetails.postCode else None,
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
                  propertyAcquiredFrom = heldPropertyTransaction.optPropertyAcquiredFrom.map(propertyAcquiredFrom =>
                    toEtmpIdentityType(
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
                  residentialSchedule29A = heldPropertyTransaction.optIsLandOrPropertyResidential.map(toYesNo),
                  landOrPropertyLeased = heldPropertyTransaction.optLandOrPropertyLeased.map(toYesNo),
                  leaseDetails = heldPropertyTransaction.optLeaseDetails
                    .flatMap(leaseDetails =>
                      if (heldPropertyTransaction.optLandOrPropertyLeased.contains(true))
                        Some(
                          EtmpLeaseDetails(
                            lesseeName = leaseDetails.optLesseeName,
                            connectedPartyStatus =
                              leaseDetails.optConnectedPartyStatus.map(transformToEtmpConnectedPartyStatus),
                            leaseGrantDate = leaseDetails.optLeaseGrantDate,
                            annualLeaseAmount = leaseDetails.optAnnualLeaseAmount
                          )
                        )
                      else None
                    ),
                  totalIncomeOrReceipts = heldPropertyTransaction.optTotalIncomeOrReceipts
                ),
                disposedPropertyTransaction = optDisposedPropertyTransaction
                  .map(
                    _.map(disposedPropertyTransaction =>
                      EtmpDisposedPropertyTransaction(
                        methodOfDisposal = howDisposedToString(disposedPropertyTransaction.methodOfDisposal),
                        otherMethod = disposedPropertyTransaction.optOtherMethod,
                        dateOfSale = disposedPropertyTransaction.optDateOfSale,
                        nameOfPurchaser = disposedPropertyTransaction.optNameOfPurchaser,
                        purchaseOrgDetails =
                          disposedPropertyTransaction.optPropertyAcquiredFrom.map(propertyAcquiredFrom =>
                            toEtmpIdentityType(
                              identityType = propertyAcquiredFrom.identityType,
                              optIdNumber = propertyAcquiredFrom.idNumber,
                              optReasonNoIdNumber = propertyAcquiredFrom.reasonNoIdNumber,
                              optOtherDescription = propertyAcquiredFrom.otherDescription
                            )
                          ),
                        saleProceeds = disposedPropertyTransaction.optSaleProceeds,
                        connectedPartyStatus = disposedPropertyTransaction.optConnectedPartyStatus
                          .map(transformToEtmpConnectedPartyStatus),
                        indepValuationSupport = disposedPropertyTransaction.optIndepValuationSupport.map(toYesNo),
                        portionStillHeld = toYesNo(disposedPropertyTransaction.portionStillHeld)
                      )
                    )
                  )
                  .filter(_.nonEmpty)
              )
            }
          )
        )
      ),
      borrowing = assets.optBorrowing.map(borrowing =>
        EtmpBorrowing(
          recordVersion = borrowing.recordVersion,
          moneyWasBorrowed = toYesNo(borrowing.moneyWasBorrowed),
          noOfBorrows = Option.when(borrowing.moneyWasBorrowed)(borrowing.moneyBorrowed.size),
          moneyBorrowed = Option.when(borrowing.moneyWasBorrowed)(
            borrowing.moneyBorrowed.map(mb =>
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
          )
        )
      ),
      bonds = assets.optBonds.map(bonds =>
        EtmpBonds(
          recordVersion = bonds.recordVersion,
          bondsWereAdded = bonds.optBondsWereAdded.map(toYesNo),
          bondsWereDisposed = bonds.optBondsWereDisposed.map(toYesNo),
          noOfTransactions = Option.when(bonds.optBondsWereAdded.getOrElse(false))(bonds.bondTransactions.size),
          bondTransactions = Option.when(
            bonds.bondTransactions.nonEmpty ||
              (bonds.optBondsWereAdded.nonEmpty && bonds.optBondsWereAdded.get)
          )(
            bonds.bondTransactions.map(bondTransaction =>
              EtmpBondTransactions(
                prePopulated = bondTransaction.prePopulated,
                nameOfBonds = bondTransaction.nameOfBonds,
                methodOfHolding = schemeHoldBondToString(bondTransaction.methodOfHolding),
                dateOfAcqOrContrib = bondTransaction.optDateOfAcqOrContrib,
                costOfBonds = bondTransaction.costOfBonds,
                connectedPartyStatus = bondTransaction.optConnectedPartyStatus.map(transformToEtmpConnectedPartyStatus),
                bondsUnregulated = toYesNo(bondTransaction.bondsUnregulated),
                totalIncomeOrReceipts = bondTransaction.optTotalIncomeOrReceipts,
                bondsDisposed = bondTransaction.optBondsDisposed
                  .map(
                    _.map(bondsDisposed =>
                      EtmpBondsDisposed(
                        methodOfDisposal = howDisposedToString(bondsDisposed.methodOfDisposal),
                        otherMethod = bondsDisposed.optOtherMethod,
                        dateSold = bondsDisposed.optDateSold,
                        amountReceived = bondsDisposed.optAmountReceived,
                        bondsPurchaserName = bondsDisposed.optBondsPurchaserName,
                        connectedPartyStatus = bondsDisposed.optConnectedPartyStatus
                          .map(transformToEtmpConnectedPartyStatus),
                        totalNowHeld = bondsDisposed.totalNowHeld
                      )
                    )
                  )
                  .filter(_.nonEmpty)
              )
            )
          )
        )
      ),
      otherAssets = assets.optOtherAssets.map(otherAssets =>
        EtmpOtherAssets(
          recordVersion = otherAssets.recordVersion,
          otherAssetsWereHeld = otherAssets.optOtherAssetsWereHeld.map(toYesNo),
          otherAssetsWereDisposed = otherAssets.optOtherAssetsWereDisposed.map(toYesNo),
          noOfTransactions =
            Option.when(otherAssets.optOtherAssetsWereHeld.getOrElse(false))(otherAssets.otherAssetTransactions.size),
          otherAssetTransactions = Option.when(
            otherAssets.otherAssetTransactions.nonEmpty ||
              (otherAssets.optOtherAssetsWereHeld.nonEmpty && otherAssets.optOtherAssetsWereHeld.get)
          )(
            otherAssets.otherAssetTransactions.map(otherAssetTransaction =>
              EtmpOtherAssetTransaction(
                prePopulated = otherAssetTransaction.prePopulated,
                assetDescription = otherAssetTransaction.assetDescription,
                methodOfHolding = schemeHoldAssetToString(otherAssetTransaction.methodOfHolding),
                dateOfAcqOrContrib = otherAssetTransaction.optDateOfAcqOrContrib,
                costOfAsset = otherAssetTransaction.costOfAsset,
                acquiredFromName = otherAssetTransaction.optPropertyAcquiredFromName,
                acquiredFromType = otherAssetTransaction.optPropertyAcquiredFrom.map(propertyAcquiredFrom =>
                  toEtmpIdentityType(
                    identityType = propertyAcquiredFrom.identityType,
                    optIdNumber = propertyAcquiredFrom.idNumber,
                    optReasonNoIdNumber = propertyAcquiredFrom.reasonNoIdNumber,
                    optOtherDescription = propertyAcquiredFrom.otherDescription
                  )
                ),
                connectedStatus = otherAssetTransaction.optConnectedStatus.map(transformToEtmpConnectedPartyStatus),
                supportedByIndepValuation = otherAssetTransaction.optIndepValuationSupport.map(toYesNo),
                movableSchedule29A = otherAssetTransaction.optMovableSchedule29A.map(toYesNo),
                totalIncomeOrReceipts = otherAssetTransaction.optTotalIncomeOrReceipts,
                assetsDisposed = otherAssetTransaction.optOtherAssetDisposed
                  .map(
                    _.map(otherAssetDisposed =>
                      EtmpAssetsDisposed(
                        methodOfDisposal = howDisposedToString(otherAssetDisposed.methodOfDisposal),
                        otherMethod = otherAssetDisposed.optOtherMethod,
                        dateSold = otherAssetDisposed.optDateSold,
                        purchaserName = otherAssetDisposed.optPurchaserName,
                        purchaserType = otherAssetDisposed.optPropertyAcquiredFrom.map(propertyAcquiredFrom =>
                          toEtmpIdentityType(
                            identityType = propertyAcquiredFrom.identityType,
                            optIdNumber = propertyAcquiredFrom.idNumber,
                            optReasonNoIdNumber = propertyAcquiredFrom.reasonNoIdNumber,
                            optOtherDescription = propertyAcquiredFrom.otherDescription
                          )
                        ),
                        totalAmountReceived = otherAssetDisposed.optTotalAmountReceived,
                        connectedStatus =
                          otherAssetDisposed.optConnectedStatus.map(transformToEtmpConnectedPartyStatus),
                        supportedByIndepValuation = otherAssetDisposed.optSupportedByIndepValuation.map(toYesNo),
                        fullyDisposedOf = toYesNo(!otherAssetDisposed.anyPartAssetStillHeld)
                      )
                    )
                  )
                  .filter(_.nonEmpty)
              )
            )
          )
        )
      )
    )
}
