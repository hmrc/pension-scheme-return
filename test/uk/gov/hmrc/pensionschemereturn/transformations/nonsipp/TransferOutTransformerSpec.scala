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

import uk.gov.hmrc.pensionschemereturn.base.EtmpTransformerSpec
import uk.gov.hmrc.pensionschemereturn.models.etmp.nonsipp.{EtmpTransfersOut, TransferSchemeType}
import uk.gov.hmrc.pensionschemereturn.models.nonsipp.{PensionSchemeType, TransfersOut}

import java.time.LocalDate

class TransferOutTransformerSpec extends EtmpTransformerSpec {

  private val transfersOut = TransfersOut(
    schemeName = "test scheme name",
    dateOfTransfer = LocalDate.of(2010, 10, 10),
    transferSchemeType = PensionSchemeType.RegisteredPS("some pension scheme")
  )

  private val etmpTransfersOut = EtmpTransfersOut(
    schemeName = "test scheme name",
    dateOfTransfer = LocalDate.of(2010, 10, 10),
    transferSchemeType = TransferSchemeType.registeredScheme("some pension scheme")
  )

  "TransferInTransformer" should {
    "successfully transform to ETMP format" when {
      "transferSchemeType is a registered pension scheme" in {
        val result = transferOutTransformer.toEtmp(transfersOut)

        result shouldMatchTo etmpTransfersOut
      }

      "transferSchemeType is a qrops" in {
        val result = transferOutTransformer.toEtmp(
          transfersOut.copy(
            transferSchemeType = PensionSchemeType.QualifyingRecognisedOverseasPS("some overseas scheme")
          )
        )

        result shouldMatchTo etmpTransfersOut.copy(
          transferSchemeType = TransferSchemeType.qrops("some overseas scheme")
        )
      }

      "transferSchemeType is other" in {
        val result = transferOutTransformer.toEtmp(
          transfersOut.copy(
            transferSchemeType = PensionSchemeType.Other("other")
          )
        )

        result shouldMatchTo etmpTransfersOut.copy(transferSchemeType = TransferSchemeType.other("other"))
      }
    }

    "successfully transform from ETMP format" when {
      "transferSchemeType is a registered pension scheme" in {
        val result = transferOutTransformer.fromEtmp(etmpTransfersOut)

        result shouldMatchTo Right(transfersOut)
      }

      "transferSchemeType is a qrops" in {
        val result = transferOutTransformer.fromEtmp(
          etmpTransfersOut.copy(transferSchemeType = TransferSchemeType.qrops("some overseas scheme"))
        )

        result shouldMatchTo Right(
          transfersOut.copy(
            transferSchemeType = PensionSchemeType.QualifyingRecognisedOverseasPS("some overseas scheme")
          )
        )
      }

      "transferSchemeType is other" in {
        val result = transferOutTransformer.fromEtmp(
          etmpTransfersOut.copy(transferSchemeType = TransferSchemeType.other("other"))
        )

        result shouldMatchTo Right(
          transfersOut.copy(
            transferSchemeType = PensionSchemeType.Other("other")
          )
        )
      }
    }
  }
}
