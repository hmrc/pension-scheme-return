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

package uk.gov.hmrc.pensionschemereturn.models

import uk.gov.hmrc.pensionschemereturn.models.enumeration.{Enumerable, WithName}
import play.api.libs.json._
import play.api.libs.functional.syntax._

import scala.util.{Success, Try}

import java.time.LocalDateTime
import java.util.Locale
import java.time.format.DateTimeFormatter

sealed trait Event

object Event extends Enumerable.Implicits {

  override def toString: String = super.toString.toLowerCase

  implicit val enumerable: Enumerable[Event] = Enumerable(
    Seq(Sent, Delivered, PermanentBounce, Opened, Complained).map(v => v.toString -> v)*
  )
}

case object Sent extends WithName("Sent") with Event

case object Delivered extends WithName("Delivered") with Event

case object PermanentBounce extends WithName("PermanentBounce") with Event

case object Opened extends WithName("Opened") with Event

case object Complained extends WithName("Complained") with Event

case class EmailEvent(event: Event, detected: LocalDateTime)

object EmailEvent {
  private val isoZonedDateFormatter: DateTimeFormatter =
    DateTimeFormatter.ofPattern("yyyy-MM-dd'T'HH:mm:ss.SSS'Z'", Locale.UK)

  // List of formatters to handle both with and without milliseconds
  private val formatters = List(
    DateTimeFormatter.ofPattern("yyyy-MM-dd'T'HH:mm:ss'Z'", Locale.UK),
    DateTimeFormatter.ofPattern("yyyy-MM-dd'T'HH:mm:ss.SSS'Z'", Locale.UK)
  )

  protected def parseDateTime(dateStr: String): LocalDateTime =
    formatters.view
      .map { fmt =>
        Try(LocalDateTime.parse(dateStr, fmt))
      }
      .collectFirst { case Success(dateTime) =>
        dateTime
      }
      .getOrElse(throw new IllegalArgumentException(s"Unparseable date: $dateStr"))

  private val dateTimeWritesWithMilliseconds = Writes[LocalDateTime] { localDateTime =>
    JsString(isoZonedDateFormatter.format(localDateTime))
  }

  implicit val read: Reads[EmailEvent] =
    (JsPath \ "event")
      .read[Event]
      .and((JsPath \ "detected").read[String].map(detected => parseDateTime(detected)))(
        EmailEvent.apply
      )
  implicit val write: Writes[EmailEvent] =
    (JsPath \ "event")
      .write[Event]
      .and((JsPath \ "detected").write[LocalDateTime](dateTimeWritesWithMilliseconds))(emailEvent =>
        (emailEvent.event, emailEvent.detected)
      )
}

case class EmailEvents(events: Seq[EmailEvent])

object EmailEvents {
  implicit val format: OFormat[EmailEvents] = Json.format[EmailEvents]
}
