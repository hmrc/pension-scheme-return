/*
 * Copyright 2025 HM Revenue & Customs
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

package uk.gov.hmrc.pensionschemereturn.models

import org.scalatest.wordspec.AnyWordSpec
import org.scalatest.matchers.should.Matchers
import play.api.libs.json.{JsSuccess, Json}

import java.time.LocalDateTime

class EmailEventsSpec extends AnyWordSpec with Matchers {

  private val jsonWithoutMilliseconds: String =
    """
      |{
      |  "event": "Sent",
      |  "detected": "2025-04-29T09:09:20Z"
      |}
      |""".stripMargin

  private val jsonWithMilliseconds: String =
    """
      |{
      |  "event": "Sent",
      |  "detected": "2025-04-29T09:09:20.123Z"
      |}
      |""".stripMargin

  private val expectedWithoutMilliseconds = EmailEvent(
    Sent,
    LocalDateTime.of(2025, 4, 29, 9, 9, 20)
  )

  private val expectedWithMilliseconds = EmailEvent(
    Sent,
    LocalDateTime.of(2025, 4, 29, 9, 9, 20, 123000000) // 123 ms = 123,000,000 ns
  )

  "Json of EmailEvent" should {

    "be parsed ok when input is without milliseconds" in {
      Json.parse(jsonWithoutMilliseconds).validate[EmailEvent] shouldEqual JsSuccess(expectedWithoutMilliseconds)
    }

    "be parsed ok when input is with milliseconds" in {
      Json.parse(jsonWithMilliseconds).validate[EmailEvent] shouldEqual JsSuccess(expectedWithMilliseconds)
    }
  }
}
