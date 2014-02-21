/*
 * CDDL HEADER START
 *
 * The contents of this file are subject to the terms of the
 * Common Development and Distribution License, Version 1.0 only
 * (the "License").  You may not use this file except in compliance
 * with the License.
 *
 * You can obtain a copy of the license at legal-notices/CDDLv1_0.txt
 * or http://forgerock.org/license/CDDLv1.0.html.
 * See the License for the specific language governing permissions
 * and limitations under the License.
 *
 * When distributing Covered Code, include this CDDL HEADER in each
 * file and include the License file at legal-notices/CDDLv1_0.txt.
 * If applicable, add the following below this CDDL HEADER, with the
 * fields enclosed by brackets "[]" replaced with your own identifying
 * information:
 *      Portions Copyright [yyyy] [name of copyright owner]
 *
 * CDDL HEADER END
 *
 *
 *      Copyright 2009-2010 Sun Microsystems, Inc.
 *      Portions Copyright 2011-2014 ForgeRock AS
 */
package org.opends.server.extensions;

import java.util.Collections;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

import org.forgerock.i18n.LocalizableMessage;
import org.opends.server.admin.std.server.
        CollectiveAttributeSubentriesVirtualAttributeCfg;
import org.opends.server.api.VirtualAttributeProvider;
import org.opends.server.core.DirectoryServer;
import org.opends.server.core.SearchOperation;
import org.opends.server.types.*;
import org.forgerock.opendj.ldap.ResultCode;
import static org.opends.messages.ExtensionMessages.*;

/**
 * This class implements a virtual attribute provider to serve the
 * collectiveAttributeSubentries operational attribute as described
 * in RFC 3671.
 */
public class CollectiveAttributeSubentriesVirtualAttributeProvider
        extends VirtualAttributeProvider<
        CollectiveAttributeSubentriesVirtualAttributeCfg>
{
  /**
   * Creates a new instance of this collectiveAttributeSubentries
   * virtual attribute provider.
   */
  public CollectiveAttributeSubentriesVirtualAttributeProvider()
  {
    super();

    // All initialization should be performed in the
    // initializeVirtualAttributeProvider method.
  }

  /** {@inheritDoc} */
  @Override()
  public boolean isMultiValued()
  {
    return true;
  }

  /** {@inheritDoc} */
  @Override()
  public Set<AttributeValue> getValues(Entry entry,
                                       VirtualAttributeRule rule)
  {
    Set<AttributeValue> values = null;

    if (!entry.isSubentry() && !entry.isLDAPSubentry())
    {
      List<SubEntry> subentries = DirectoryServer.getSubentryManager()
          .getCollectiveSubentries(entry);

      AttributeType dnAttrType =
              DirectoryServer.getAttributeType("2.5.4.49");
      for (SubEntry subentry : subentries)
      {
        if (subentry.isCollective() ||
            subentry.isInheritedCollective())
        {
          DN subentryDN = subentry.getDN();
          AttributeValue value = AttributeValues.create(
                  dnAttrType, subentryDN.toString());

          if (values == null)
          {
            values = Collections.singleton(value);
          }
          else if (values.size() == 1)
          {
            Set<AttributeValue> tmp = new HashSet<AttributeValue>(2);
            tmp.addAll(values);
            tmp.add(value);
            values = tmp;
          }
          else
          {
            values.add(value);
          }
        }
      }
    }

    if (values != null)
    {
      return Collections.unmodifiableSet(values);
    }
    return Collections.emptySet();
  }

  /** {@inheritDoc} */
  @Override()
  public boolean isSearchable(VirtualAttributeRule rule,
                              SearchOperation searchOperation,
                              boolean isPreIndexed)
  {
    return false;
  }

  /** {@inheritDoc} */
  @Override()
  public void processSearch(VirtualAttributeRule rule,
                            SearchOperation searchOperation)
  {
    searchOperation.setResultCode(ResultCode.UNWILLING_TO_PERFORM);

    LocalizableMessage message =
            ERR_COLLECTIVEATTRIBUTESUBENTRIES_VATTR_NOT_SEARCHABLE.get(
            rule.getAttributeType().getNameOrOID());
    searchOperation.appendErrorMessage(message);
  }
}