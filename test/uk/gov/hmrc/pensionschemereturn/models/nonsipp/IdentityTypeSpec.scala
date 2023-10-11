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
import uk.gov.hmrc.pensionschemereturn.models.nonsipp.IdentityType.{Individual, Other, UKCompany, UKPartnership}

class IdentityTypeSpec extends AnyWordSpec with Matchers {

  "IdentityType" should {

    "successfully convert from string to IdentityType" in {
      IdentityType.stringToIdentityType("01") shouldEqual Individual
      IdentityType.stringToIdentityType("02") shouldEqual UKCompany
      IdentityType.stringToIdentityType("03") shouldEqual UKPartnership
      IdentityType.stringToIdentityType("04") shouldEqual Other
    }
  }

  "successfully convert from IdentityType to string" in {
    IdentityType.identityTypeToString(Individual) shouldEqual "01"
    IdentityType.identityTypeToString(UKCompany) shouldEqual "02"
    IdentityType.identityTypeToString(UKPartnership) shouldEqual "03"
    IdentityType.identityTypeToString(Other) shouldEqual "04"
  }

}
