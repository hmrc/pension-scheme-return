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
import uk.gov.hmrc.pensionschemereturn.models.etmp.nonsipp.{
  EtmpAddress,
  EtmpAssets,
  EtmpBonds,
  EtmpBorrowing,
  EtmpLandOrProperty,
  EtmpLandOrPropertyTransactions,
  EtmpLandRegistryDetails,
  EtmpOtherAssets,
  EtmpRecipientIdentityType
}
import uk.gov.hmrc.pensionschemereturn.models.nonsipp.IdentityType.identityTypeToString
import uk.gov.hmrc.pensionschemereturn.models.nonsipp.{Assets, RecipientIdentityType}
import uk.gov.hmrc.pensionschemereturn.transformations.Transformer

@Singleton()
class AssetsToEtmp @Inject()() extends Transformer {

  def transform(assets: Assets): EtmpAssets =
    EtmpAssets(
      landOrProperty = EtmpLandOrProperty(
        recordVersion = None,
        heldAnyLandOrProperty = toYesNo(assets.landOrProperty.landOrPropertyHeld),
        disposeAnyLandOrProperty = toYesNo(false), //TODO
        noOfTransactions = Some(assets.landOrProperty.landOrPropertyTransactions.size),
        landOrPropertyTransactions = Some(
          assets.landOrProperty.landOrPropertyTransactions.map(
            landOrPropertyTransactions =>
              EtmpLandOrPropertyTransactions(
                landOrPropertyInUK = toYesNo(landOrPropertyTransactions.propertyDetails.landOrPropertyInUK),
                addressDetails = EtmpAddress(
                  addressLine1 = landOrPropertyTransactions.propertyDetails.addressDetails.addressLine1,
                  addressLine2 = landOrPropertyTransactions.propertyDetails.addressDetails.addressLine2,
                  addressLine3 = landOrPropertyTransactions.propertyDetails.addressDetails.addressLine3,
                  addressLine4 = None,
                  addressLine5 = None,
                  ukPostCode = landOrPropertyTransactions.propertyDetails.addressDetails.postCode,
                  countryCode = landOrPropertyTransactions.propertyDetails.addressDetails.countryCode
                ),
                landRegistryDetails = EtmpLandRegistryDetails(
                  //TODO - 2nd and 3rd in PSR should be option
                  toYesNo(false),
                  None,
                  Some(landOrPropertyTransactions.propertyDetails.landRegistryTitleNumberValue)
                )
              )
          )
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

  private def buildRecipientIdentityTypeRequest(
    recipientIdentityType: RecipientIdentityType
  ): EtmpRecipientIdentityType =
    EtmpRecipientIdentityType(
      indivOrOrgType = identityTypeToString(recipientIdentityType.identityType),
      idNumber = recipientIdentityType.idNumber,
      reasonNoIdNumber = recipientIdentityType.reasonNoIdNumber,
      otherDescription = recipientIdentityType.otherDescription
    )
}
