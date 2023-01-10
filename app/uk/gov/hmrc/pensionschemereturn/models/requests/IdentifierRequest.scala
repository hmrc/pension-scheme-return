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

package uk.gov.hmrc.pensionschemereturn.models.requests

import play.api.mvc.{Request, WrappedRequest}
import uk.gov.hmrc.pensionschemereturn.models.PensionSchemeId.{PsaId, PspId}
import uk.gov.hmrc.pensionschemereturn.models.requests.IdentifierRequest.{AdministratorRequest, PractitionerRequest}

sealed abstract class IdentifierRequest[A](request: Request[A]) extends WrappedRequest[A](request) { self =>

  def fold[B](admin: AdministratorRequest[A] => B)(practitioner: PractitionerRequest[A] => B): B =
    self match {
      case a: AdministratorRequest[A] => admin(a)
      case p: PractitionerRequest[A]  => practitioner(p)
    }
}

object IdentifierRequest {

  case class AdministratorRequest[A](
    externalId: String,
    request: Request[A],
    psaId: PsaId
  ) extends IdentifierRequest[A](request)

  object AdministratorRequest {
    def apply[A](externalId: String, request: Request[A], psaId: String): IdentifierRequest[A] =
      AdministratorRequest(externalId, request, PsaId(psaId))
  }

  case class PractitionerRequest[A](
    externalId: String,
    request: Request[A],
    pspId: PspId
  ) extends IdentifierRequest[A](request)

  object PractitionerRequest {

    def apply[A](externalId: String, request: Request[A], pspId: String): IdentifierRequest[A] =
      PractitionerRequest(externalId, request, PspId(pspId))
  }

}