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

package uk.gov.hmrc.pensionschemereturn.models.nonsipp

import org.scalatest.matchers.should.Matchers
import uk.gov.hmrc.pensionschemereturn.models.nonsipp.shares.HowSharesDisposed._
import org.scalatest.wordspec.AnyWordSpec

class HowSharesDisposedSpec extends AnyWordSpec with Matchers {

  "HowSharesDisposed" should {

    "successfully convert from string to HowSharesDisposed" in {
      howSharesDisposedToString(Sold) shouldEqual "01"
      howSharesDisposedToString(Redeemed) shouldEqual "02"
      howSharesDisposedToString(Transferred) shouldEqual "03"
      howSharesDisposedToString(Other) shouldEqual "04"
    }

    "successfully convert from HowSharesDisposed to String" in {
      stringToHowSharesDisposed("01") shouldEqual Sold
      stringToHowSharesDisposed("02") shouldEqual Redeemed
      stringToHowSharesDisposed("03") shouldEqual Transferred
      stringToHowSharesDisposed("04") shouldEqual Other
    }
  }
}
