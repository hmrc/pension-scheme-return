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

package uk.gov.hmrc.pensionschemereturn.controllers.actions

import play.api.Logger
import play.api.mvc.{ActionBuilder, AnyContent, BodyParser, Request, Result}
import uk.gov.hmrc.auth.core.{AuthConnector, AuthorisedFunctions}
import uk.gov.hmrc.pensionschemereturn.models.requests.IdentifierRequest

import javax.inject.Inject
import scala.concurrent.{ExecutionContext, Future}

class IdentifierAction @Inject()(
  override val authConnector: AuthConnector,
  override val parser: BodyParser[AnyContent]
)(implicit override val executionContext: ExecutionContext) extends ActionBuilder[IdentifierRequest, AnyContent] with AuthorisedFunctions {

  private val logger = Logger(classOf[IdentifierAction])

  override def invokeBlock[A](request: Request[A], block: IdentifierRequest[A] => Future[Result]): Future[Result] = ???
}