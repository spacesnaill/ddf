/**
 * Copyright (c) Codice Foundation
 *
 * <p>This is free software: you can redistribute it and/or modify it under the terms of the GNU
 * Lesser General Public License as published by the Free Software Foundation, either version 3 of
 * the License, or any later version.
 *
 * <p>This program is distributed in the hope that it will be useful, but WITHOUT ANY WARRANTY;
 * without even the implied warranty of MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE. See the
 * GNU Lesser General Public License for more details. A copy of the GNU Lesser General Public
 * License is distributed along with this program and can be found at
 * <http://www.gnu.org/licenses/lgpl.html>.
 */
package org.codice.ddf.security;

import java.security.cert.X509Certificate;

/**
 * <b> This code is experimental. While this interface is functional and tested, it may change or be
 * removed in a future version of the library. </b>
 */
public interface OcspService {

  /**
   * Checks the whether the given {@param certs} are revoked or not against the OCSP server.
   *
   * @param certs - an array of certificates to verify
   * @return true if the certificates are not revoked or if they could not be properly checked
   *     against the OCSP server. Returns false if any of them are revoked.
   */
  boolean passesOcspCheck(X509Certificate[] certs);
}
