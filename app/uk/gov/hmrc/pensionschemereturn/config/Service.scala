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

package uk.gov.hmrc.pensionschemereturn.config

import com.typesafe.config.Config
import play.api.ConfigLoader

case class Service(protocol: String, host: String, port: String) {
  val url = s"$protocol://$host:$port"

  override def toString: String = url
}

object Service {

  implicit val configLoader: ConfigLoader[Service] = (config: Config, path: String) => {
    val protocol = config.getString(s"$path.protocol")
    val host = config.getString(s"$path.host")
    val port = config.getString(s"$path.port")
    Service(if(protocol.nonEmpty) protocol else "https", host, port)
  }
}