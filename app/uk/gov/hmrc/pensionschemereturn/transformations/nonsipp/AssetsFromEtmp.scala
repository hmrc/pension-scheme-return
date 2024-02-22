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
import uk.gov.hmrc.pensionschemereturn.models.etmp.nonsipp.EtmpAssets
import uk.gov.hmrc.pensionschemereturn.models.nonsipp.HowDisposed.stringToHowDisposed
import uk.gov.hmrc.pensionschemereturn.models.nonsipp.IdentityType.stringToIdentityType
import uk.gov.hmrc.pensionschemereturn.models.nonsipp.SchemeHoldLandProperty.stringToSchemeHoldLandProperty
import uk.gov.hmrc.pensionschemereturn.models.nonsipp._
import uk.gov.hmrc.pensionschemereturn.transformations.Transformer

@Singleton()
class AssetsFromEtmp @Inject() extends Transformer {

  def transform(assets: EtmpAssets): Assets =
    Assets(
      optLandOrProperty = assets.landOrProperty.map(
        landOrProperty =>
          LandOrProperty(
            landOrPropertyHeld = fromYesNo(landOrProperty.heldAnyLandOrProperty),
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
                        postCode = addressDetails.ukPostCode,
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
                      isLandOrPropertyResidential = fromYesNo(heldPropertyTransaction.residentialSchedule29A),
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
      )
    )
}
