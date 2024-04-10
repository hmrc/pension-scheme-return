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

import play.api.libs.json._

sealed trait ReportStatus {
  val name: String
}

case object ReportStatusCompiled extends ReportStatus {
  val name = "Compiled"
}

case object SubmittedAndInProgress extends ReportStatus {
  val name = "SubmittedAndInProgress"
}

case object SubmittedAndSuccessfullyProcessed extends ReportStatus {
  val name = "SubmittedAndSuccessfullyProcessed"
}

object ReportStatus {

  private val values: List[ReportStatus] =
    List(ReportStatusCompiled, SubmittedAndInProgress, SubmittedAndSuccessfullyProcessed)

  implicit val formats: Format[ReportStatus] = new Format[ReportStatus] {
    override def writes(o: ReportStatus): JsValue = JsString(o.name)

    override def reads(json: JsValue): JsResult[ReportStatus] = {
      val jsonAsString = json.as[String]
      values.find(_.name == jsonAsString) match {
        case Some(status) => JsSuccess(status)
        case None => JsError(s"Unknown report status: $jsonAsString")
      }
    }
  }
}
