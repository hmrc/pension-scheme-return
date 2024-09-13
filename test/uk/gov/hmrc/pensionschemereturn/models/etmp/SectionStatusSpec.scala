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

package uk.gov.hmrc.pensionschemereturn.models.etmp

import uk.gov.hmrc.pensionschemereturn.models.etmp.SectionStatus.{Changed, Deleted, New}
import org.scalatest.wordspec.AnyWordSpec
import org.scalatest.matchers.should.Matchers
import play.api.libs.json._

class SectionStatusSpec extends AnyWordSpec with Matchers {

  "SectionStatus" should {

    "successfully convert from SectionStatus to Json" in {
      Json.toJson(New)(implicitly[Writes[SectionStatus]]) shouldEqual JsString("New")
      Json.toJson(Changed)(implicitly[Writes[SectionStatus]]) shouldEqual JsString("Changed")
      Json.toJson(Deleted)(implicitly[Writes[SectionStatus]]) shouldEqual JsString("Deleted")
    }

    "successfully convert from Json to SectionStatus" in {
      JsString("Deleted").validate[SectionStatus] shouldEqual JsSuccess(Deleted)
      JsString("Changed").validate[SectionStatus] shouldEqual JsSuccess(Changed)
      JsString("New").validate[SectionStatus] shouldEqual JsSuccess(New)
      JsString("INVALID").validate[SectionStatus] shouldEqual JsError("Unknown value for SectionStatus \"INVALID\"")
    }
  }
}
