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

import uk.gov.hmrc.pensionschemereturn.models.nonsipp.assets.SchemeHoldAsset.stringToSchemeHoldAsset
import uk.gov.hmrc.pensionschemereturn.models.nonsipp.IdentityType.stringToIdentityType
import com.google.inject.{Inject, Singleton}
import uk.gov.hmrc.pensionschemereturn.models.nonsipp.assets._
import uk.gov.hmrc.pensionschemereturn.models.nonsipp.assets.HowDisposed.stringToHowDisposed
import uk.gov.hmrc.pensionschemereturn.models.nonsipp._
import uk.gov.hmrc.pensionschemereturn.models.etmp.nonsipp.assets.EtmpAssets
import uk.gov.hmrc.pensionschemereturn.transformations.Transformer
import uk.gov.hmrc.pensionschemereturn.models.nonsipp.assets.SchemeHoldBond.stringToSchemeHoldBond
import uk.gov.hmrc.pensionschemereturn.models.nonsipp.assets.SchemeHoldLandProperty.stringToSchemeHoldLandProperty

@Singleton()
class AssetsFromEtmp @Inject() extends Transformer {

  def transform(assets: EtmpAssets): Assets =
    Assets(
      optLandOrProperty = assets.landOrProperty.map(
        landOrProperty =>
          LandOrProperty(
            recordVersion = landOrProperty.recordVersion,
            optLandOrPropertyHeld = landOrProperty.heldAnyLandOrProperty.map(fromYesNo),
            disposeAnyLandOrProperty = fromYesNo(landOrProperty.disposeAnyLandOrProperty),
            landOrPropertyTransactions = landOrProperty.landOrPropertyTransactions
              .getOrElse(Seq.empty)
              .map(
                landOrPropertyTransaction => {

                  val propertyDetails = landOrPropertyTransaction.propertyDetails
                  val heldPropertyTransaction = landOrPropertyTransaction.heldPropertyTransaction
                  val disposedPropertyTransaction = landOrPropertyTransaction.disposedPropertyTransaction
                  val addressDetails = propertyDetails.addressDetails

                  LandOrPropertyTransactions(
                    propertyDetails = PropertyDetails(
                      landOrPropertyInUK = fromYesNo(propertyDetails.landOrPropertyInUK),
                      addressDetails = Address(
                        addressLine1 = addressDetails.addressLine1,
                        addressLine2 = addressDetails.addressLine4.map(_ => addressDetails.addressLine2),
                        addressLine3 = addressDetails.addressLine3,
                        town = addressDetails.addressLine4.getOrElse(addressDetails.addressLine2),
                        postCode =
                          if (fromYesNo(propertyDetails.landOrPropertyInUK)) addressDetails.ukPostCode
                          else addressDetails.addressLine5,
                        countryCode = addressDetails.countryCode
                      ),
                      landRegistryTitleNumberKey =
                        fromYesNo(propertyDetails.landRegistryDetails.landRegistryReferenceExists),
                      landRegistryTitleNumberValue = Seq(
                        propertyDetails.landRegistryDetails.landRegistryReference,
                        propertyDetails.landRegistryDetails.reasonNoReference
                      ).flatten.headOption.get
                    ),
                    heldPropertyTransaction = HeldPropertyTransaction(
                      methodOfHolding = stringToSchemeHoldLandProperty(heldPropertyTransaction.methodOfHolding),
                      dateOfAcquisitionOrContribution = heldPropertyTransaction.dateOfAcquisitionOrContribution,
                      optPropertyAcquiredFromName = heldPropertyTransaction.propertyAcquiredFromName,
                      optPropertyAcquiredFrom = heldPropertyTransaction.propertyAcquiredFrom.map { etmpIdentityType =>
                        val identityType = stringToIdentityType(etmpIdentityType.indivOrOrgType)
                        PropertyAcquiredFrom(
                          identityType = identityType,
                          idNumber = etmpIdentityType.idNumber,
                          reasonNoIdNumber = etmpIdentityType.reasonNoIdNumber,
                          otherDescription = etmpIdentityType.otherDescription
                        )
                      },
                      optConnectedPartyStatus = heldPropertyTransaction.connectedPartyStatus.map(_ == Connected),
                      totalCostOfLandOrProperty = heldPropertyTransaction.totalCostOfLandOrProperty,
                      optIndepValuationSupport = heldPropertyTransaction.indepValuationSupport.map(fromYesNo),
                      optIsLandOrPropertyResidential = heldPropertyTransaction.residentialSchedule29A.map(fromYesNo),
                      optLeaseDetails = heldPropertyTransaction.leaseDetails.map(
                        leaseDetail =>
                          LeaseDetails(
                            lesseeName = leaseDetail.lesseeName,
                            leaseGrantDate = leaseDetail.leaseGrantDate,
                            annualLeaseAmount = leaseDetail.annualLeaseAmount,
                            connectedPartyStatus = leaseDetail.connectedPartyStatus == Connected
                          )
                      ),
                      landOrPropertyLeased = fromYesNo(heldPropertyTransaction.landOrPropertyLeased),
                      totalIncomeOrReceipts = heldPropertyTransaction.totalIncomeOrReceipts
                    ),
                    optDisposedPropertyTransaction = disposedPropertyTransaction.map(
                      _.map(
                        dpt =>
                          DisposedPropertyTransaction(
                            methodOfDisposal = stringToHowDisposed(dpt.methodOfDisposal),
                            optOtherMethod = dpt.otherMethod,
                            optDateOfSale = dpt.dateOfSale,
                            optNameOfPurchaser = dpt.nameOfPurchaser,
                            optPropertyAcquiredFrom = dpt.purchaseOrgDetails.map { etmpIdentityType =>
                              val identityType = stringToIdentityType(etmpIdentityType.indivOrOrgType)
                              PropertyAcquiredFrom(
                                identityType = identityType,
                                idNumber = etmpIdentityType.idNumber,
                                reasonNoIdNumber = etmpIdentityType.reasonNoIdNumber,
                                otherDescription = etmpIdentityType.otherDescription
                              )
                            },
                            optSaleProceeds = dpt.saleProceeds,
                            optConnectedPartyStatus = dpt.connectedPartyStatus.map(_ == Connected),
                            optIndepValuationSupport = dpt.indepValuationSupport.map(fromYesNo),
                            portionStillHeld = fromYesNo(dpt.portionStillHeld)
                          )
                      )
                    )
                  )
                }
              )
          )
      ),
      optBorrowing = assets.borrowing.map(
        borrowing =>
          Borrowing(
            recordVersion = borrowing.recordVersion,
            moneyWasBorrowed = fromYesNo(borrowing.moneyWasBorrowed),
            moneyBorrowed = borrowing.moneyBorrowed
              .getOrElse(Seq.empty)
              .map(
                mb =>
                  MoneyBorrowed(
                    dateOfBorrow = mb.dateOfBorrow,
                    schemeAssetsValue = mb.schemeAssetsValue,
                    amountBorrowed = mb.amountBorrowed,
                    interestRate = mb.interestRate,
                    borrowingFromName = mb.borrowingFromName,
                    connectedPartyStatus = mb.connectedPartyStatus == Connected,
                    reasonForBorrow = mb.reasonForBorrow
                  )
              )
          )
      ),
      optBonds = assets.bonds.map(
        bonds =>
          Bonds(
            recordVersion = bonds.recordVersion,
            bondsWereAdded = fromYesNo(bonds.bondsWereAdded),
            bondsWereDisposed = fromYesNo(bonds.bondsWereDisposed),
            bondTransactions = bonds.bondTransactions
              .getOrElse(Seq.empty)
              .map(
                bondTransaction =>
                  BondTransactions(
                    nameOfBonds = bondTransaction.nameOfBonds,
                    methodOfHolding = stringToSchemeHoldBond(bondTransaction.methodOfHolding),
                    optDateOfAcqOrContrib = bondTransaction.dateOfAcqOrContrib,
                    costOfBonds = bondTransaction.costOfBonds,
                    optConnectedPartyStatus = bondTransaction.connectedPartyStatus.map(_ == Connected),
                    bondsUnregulated = fromYesNo(bondTransaction.bondsUnregulated),
                    totalIncomeOrReceipts = bondTransaction.totalIncomeOrReceipts,
                    optBondsDisposed = bondTransaction.bondsDisposed.map(
                      _.map(
                        bd =>
                          BondDisposed(
                            methodOfDisposal = stringToHowDisposed(bd.methodOfDisposal),
                            optOtherMethod = bd.otherMethod,
                            optDateSold = bd.dateSold,
                            optAmountReceived = bd.amountReceived,
                            optBondsPurchaserName = bd.bondsPurchaserName,
                            optConnectedPartyStatus = bd.connectedPartyStatus.map(_ == Connected),
                            totalNowHeld = bd.totalNowHeld
                          )
                      )
                    )
                  )
              )
          )
      ),
      optOtherAssets = assets.otherAssets.map(
        otherAssets =>
          OtherAssets(
            recordVersion = otherAssets.recordVersion,
            otherAssetsWereHeld = fromYesNo(otherAssets.otherAssetsWereHeld),
            otherAssetsWereDisposed = fromYesNo(otherAssets.otherAssetsWereDisposed),
            otherAssetTransactions = otherAssets.otherAssetTransactions
              .getOrElse(Seq.empty)
              .map(
                oa =>
                  OtherAssetTransaction(
                    assetDescription = oa.assetDescription,
                    methodOfHolding = stringToSchemeHoldAsset(oa.methodOfHolding),
                    optDateOfAcqOrContrib = oa.dateOfAcqOrContrib,
                    costOfAsset = oa.costOfAsset,
                    optPropertyAcquiredFromName = oa.acquiredFromName,
                    optPropertyAcquiredFrom = oa.acquiredFromType.map { etmpIdentityType =>
                      val identityType = stringToIdentityType(etmpIdentityType.indivOrOrgType)
                      PropertyAcquiredFrom(
                        identityType = identityType,
                        idNumber = etmpIdentityType.idNumber,
                        reasonNoIdNumber = etmpIdentityType.reasonNoIdNumber,
                        otherDescription = etmpIdentityType.otherDescription
                      )
                    },
                    optConnectedStatus = oa.connectedStatus.map(_ == Connected),
                    optIndepValuationSupport = oa.supportedByIndepValuation.map(fromYesNo),
                    movableSchedule29A = fromYesNo(oa.movableSchedule29A),
                    totalIncomeOrReceipts = oa.totalIncomeOrReceipts,
                    optOtherAssetDisposed = oa.assetsDisposed.map(
                      _.map(
                        oad =>
                          OtherAssetDisposed(
                            methodOfDisposal = stringToHowDisposed(oad.methodOfDisposal),
                            optOtherMethod = oad.otherMethod,
                            optDateSold = oad.dateSold,
                            optPurchaserName = oad.purchaserName,
                            optPropertyAcquiredFrom = oad.purchaserType.map { etmpIdentityType =>
                              val identityType = stringToIdentityType(etmpIdentityType.indivOrOrgType)
                              PropertyAcquiredFrom(
                                identityType = identityType,
                                idNumber = etmpIdentityType.idNumber,
                                reasonNoIdNumber = etmpIdentityType.reasonNoIdNumber,
                                otherDescription = etmpIdentityType.otherDescription
                              )
                            },
                            optTotalAmountReceived = oad.totalAmountReceived,
                            optConnectedStatus = oad.connectedStatus.map(_ == Connected),
                            optSupportedByIndepValuation = oad.supportedByIndepValuation.map(fromYesNo),
                            anyPartAssetStillHeld = !fromYesNo(oad.fullyDisposedOf)
                          )
                      )
                    )
                  )
              )
          )
      )
    )
}
