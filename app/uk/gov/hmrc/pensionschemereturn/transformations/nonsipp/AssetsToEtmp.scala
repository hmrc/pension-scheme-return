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
import uk.gov.hmrc.pensionschemereturn.models.nonsipp.IdentityType.identityTypeToString
import uk.gov.hmrc.pensionschemereturn.models.nonsipp.SchemeHoldLandProperty.schemeHoldLandPropertyToString
import uk.gov.hmrc.pensionschemereturn.models.nonsipp.{Assets, PropertyAcquiredFrom}
import uk.gov.hmrc.pensionschemereturn.transformations.Transformer

@Singleton()
class AssetsToEtmp @Inject()() extends Transformer {

  def transform(assets: Assets): EtmpAssets =
    EtmpAssets(
      landOrProperty = EtmpLandOrProperty(
        recordVersion = None,
        heldAnyLandOrProperty = toYesNo(assets.landOrProperty.landOrPropertyHeld),
        disposeAnyLandOrProperty = toYesNo(false), //TODO
        noOfTransactions = assets.landOrProperty.landOrPropertyTransactions.size,
        landOrPropertyTransactions = assets.landOrProperty.landOrPropertyTransactions.map(
          landOrPropertyTransaction => {

            val propertyDetails = landOrPropertyTransaction.propertyDetails
            val heldPropertyTransaction = landOrPropertyTransaction.heldPropertyTransaction
            val landRegistryReferenceExist = propertyDetails.landRegistryTitleNumberKey
            val landRegistryReferenceValue = propertyDetails.landRegistryTitleNumberValue
            val addressDetails = propertyDetails.addressDetails

            EtmpLandOrPropertyTransactions(
              EtmpPropertyDetails(
                landOrPropertyInUK = toYesNo(propertyDetails.landOrPropertyInUK),
                addressDetails = EtmpAddress(
                  addressLine1 = addressDetails.addressLine1,
                  addressLine2 = addressDetails.addressLine2,
                  addressLine3 = addressDetails.addressLine3,
                  addressLine4 = None,
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
              EtmpHeldPropertyTransaction(
                methodOfHolding = schemeHoldLandPropertyToString(heldPropertyTransaction.methodOfHolding),
                dateOfAcquisitionOrContribution = heldPropertyTransaction.dateOfAcquisitionOrContribution,
                propertyAcquiredFromName = heldPropertyTransaction.optPropertyAcquiredFromName,
                propertyAcquiredFrom = heldPropertyTransaction.optPropertyAcquiredFrom.map(buildEtmpIdentityType),
                connectedPartyStatus = heldPropertyTransaction.optConnectedPartyStatus
                  .map(connectedPartyStatus => if (connectedPartyStatus) "01" else "02"),
                totalCostOfLandOrProperty = heldPropertyTransaction.totalCostOfLandOrProperty,
                indepValuationSupport = heldPropertyTransaction.optIndepValuationSupport.map(toYesNo),
                residentialSchedule29A = toYesNo(heldPropertyTransaction.isLandOrPropertyResidential),
                landOrPropertyLeased = toYesNo(heldPropertyTransaction.landOrPropertyLeased),
                leaseDetails = heldPropertyTransaction.optLeaseDetails.map(
                  leaseDetails =>
                    EtmpLeaseDetails(
                      lesseeName = leaseDetails.lesseeName,
                      connectedPartyStatus = if (leaseDetails.connectedPartyStatus) "01" else "02",
                      leaseGrantDate = leaseDetails.leaseGrantDate,
                      annualLeaseAmount = leaseDetails.annualLeaseAmount
                    )
                ),
                totalIncomeOrReceipts = heldPropertyTransaction.totalIncomeOrReceipts
              )
            )
          }
        )
      ),
      borrowing = EtmpBorrowing(
        moneyWasBorrowed = toYesNo(false)
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

  private def buildEtmpIdentityType(
    propertyAcquiredFrom: PropertyAcquiredFrom
  ): EtmpIdentityType =
    EtmpIdentityType(
      indivOrOrgType = identityTypeToString(propertyAcquiredFrom.identityType),
      idNumber = propertyAcquiredFrom.idNumber,
      reasonNoIdNumber = propertyAcquiredFrom.reasonNoIdNumber,
      otherDescription = propertyAcquiredFrom.otherDescription
    )
}
