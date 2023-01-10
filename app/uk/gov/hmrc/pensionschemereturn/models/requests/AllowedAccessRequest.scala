package uk.gov.hmrc.pensionschemereturn.models.requests

import play.api.mvc.WrappedRequest
import uk.gov.hmrc.pensionschemereturn.models.SchemeDetails

case class AllowedAccessRequest[A](request: IdentifierRequest[A], schemeDetails: SchemeDetails) extends WrappedRequest[A](request)