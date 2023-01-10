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

package uk.gov.hmrc.pensionschemereturn.connectors.cache

import com.google.inject.ImplementedBy
import play.api.Logger
import uk.gov.hmrc.http.{HeaderCarrier, HttpClient, NotFoundException}
import uk.gov.hmrc.pensionschemereturn.config.AppConfig
import uk.gov.hmrc.pensionschemereturn.models.cache.SessionData
import uk.gov.hmrc.pensionschemereturn.utils.FutureUtils._

import javax.inject.Inject
import scala.concurrent.{ExecutionContext, Future}

class SessionDataCacheConnectorImpl @Inject()(config: AppConfig, http: HttpClient) extends SessionDataCacheConnector {

  private def url(cacheId: String): String = s"${config.pensionsAdministrator}/pension-administrator/journey-cache/session-data/$cacheId"

  override def fetch(cacheId: String)(implicit hc: HeaderCarrier, ec: ExecutionContext): Future[Option[SessionData]] =
    http
      .GET[Option[SessionData]](url(cacheId))
      .recover {
        case _: NotFoundException => None
      }
      .tapError(t => Future.successful(logger.error(s"Failed to fetch $cacheId with message ${t.getMessage}")))

  override def remove(cacheId: String)(implicit hc: HeaderCarrier, ec: ExecutionContext): Future[Unit] =
    http
      .DELETE(url(cacheId))
      .as(())
      .recover {
        case _: NotFoundException => ()
      }
      .tapError(t => Future.successful(logger.error(s"Failed to delete $cacheId with message ${t.getMessage}")))
}

@ImplementedBy(classOf[SessionDataCacheConnectorImpl])
trait SessionDataCacheConnector {

  protected val logger = Logger(classOf[SessionDataCacheConnector])

  def fetch(cacheId: String)(implicit hc: HeaderCarrier, ec: ExecutionContext): Future[Option[SessionData]]

  def remove(cacheId: String)(implicit hc: HeaderCarrier, ec: ExecutionContext): Future[Unit]
}