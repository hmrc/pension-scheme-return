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

import uk.gov.hmrc.pensionschemereturn.models.etmp.YesNo
import uk.gov.hmrc.pensionschemereturn.models.nonsipp.memberpayments.{PensionSchemeType, TransfersIn}
import uk.gov.hmrc.pensionschemereturn.base.EtmpTransformerSpec
import uk.gov.hmrc.pensionschemereturn.models.etmp.nonsipp.memberpayments.{EtmpTransfersIn, TransferSchemeType}
import com.softwaremill.diffx.generic.auto.indicator

import java.time.LocalDate

class TransferInTransformerSpec extends EtmpTransformerSpec {

  private val transfersIn = TransfersIn(
    schemeName = "test scheme name",
    dateOfTransfer = LocalDate.of(2010, 10, 10),
    transferSchemeType = PensionSchemeType.RegisteredPS("some pension scheme"),
    transferValue = 12.34,
    transferIncludedAsset = true
  )

  private val etmpTransfersIn = EtmpTransfersIn(
    schemeName = "test scheme name",
    dateOfTransfer = LocalDate.of(2010, 10, 10),
    transferSchemeType = TransferSchemeType.registeredScheme("some pension scheme"),
    transferValue = 12.34,
    transferIncludedAsset = YesNo.Yes
  )

  "TransferInTransformer" should {
    "successfully transform to ETMP format" when {
      "transferSchemeType is a registered pension scheme" in {
        val result = transferInTransformer.toEtmp(transfersIn)

        result shouldMatchTo etmpTransfersIn
      }

      "transferSchemeType is a qrops" in {
        val result = transferInTransformer.toEtmp(
          transfersIn.copy(
            transferSchemeType = PensionSchemeType.QualifyingRecognisedOverseasPS("some overseas scheme")
          )
        )

        result shouldMatchTo etmpTransfersIn.copy(transferSchemeType = TransferSchemeType.qrops("some overseas scheme"))
      }

      "transferSchemeType is other" in {
        val result = transferInTransformer.toEtmp(
          transfersIn.copy(
            transferSchemeType = PensionSchemeType.Other("other")
          )
        )

        result shouldMatchTo etmpTransfersIn.copy(transferSchemeType = TransferSchemeType.other("other"))
      }
    }

    "successfully transform from ETMP format" when {
      "transferSchemeType is a registered pension scheme" in {
        val result = transferInTransformer.fromEtmp(etmpTransfersIn)

        result shouldMatchTo Right(transfersIn)
      }

      "transferSchemeType is a qrops" in {
        val result = transferInTransformer.fromEtmp(
          etmpTransfersIn.copy(transferSchemeType = TransferSchemeType.qrops("some overseas scheme"))
        )

        result shouldMatchTo Right(
          transfersIn.copy(
            transferSchemeType = PensionSchemeType.QualifyingRecognisedOverseasPS("some overseas scheme")
          )
        )
      }

      "transferSchemeType is other" in {
        val result = transferInTransformer.fromEtmp(
          etmpTransfersIn.copy(transferSchemeType = TransferSchemeType.other("other"))
        )

        result shouldMatchTo Right(
          transfersIn.copy(
            transferSchemeType = PensionSchemeType.Other("other")
          )
        )
      }
    }
  }
}
