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

package uk.gov.hmrc.pensionschemereturn.models.nonsipp

import org.scalatest.matchers.should.Matchers
import org.scalatest.wordspec.AnyWordSpec
import uk.gov.hmrc.pensionschemereturn.models.nonsipp.assets.SchemeHoldBond.{
  schemeHoldBondToString,
  stringToSchemeHoldBond,
  Acquisition,
  Contribution,
  Transfer
}

class SchemeHoldBondSpec extends AnyWordSpec with Matchers {

  "SchemeHoldBond" should {

    "successfully convert from string to SchemeHoldBond" in {
      schemeHoldBondToString(Acquisition) shouldEqual "01"
      schemeHoldBondToString(Contribution) shouldEqual "02"
      schemeHoldBondToString(Transfer) shouldEqual "03"
    }

    "successfully convert from SchemeHoldBond to String" in {
      stringToSchemeHoldBond("01") shouldEqual Acquisition
      stringToSchemeHoldBond("02") shouldEqual Contribution
      stringToSchemeHoldBond("03") shouldEqual Transfer
    }
  }
}
