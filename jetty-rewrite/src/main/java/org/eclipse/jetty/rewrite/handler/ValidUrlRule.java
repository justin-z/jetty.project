//
//  ========================================================================
//  Copyright (c) 1995-2012 Mort Bay Consulting Pty. Ltd.
//  ------------------------------------------------------------------------
//  All rights reserved. This program and the accompanying materials
//  are made available under the terms of the Eclipse Public License v1.0
//  and Apache License v2.0 which accompanies this distribution.
//
//      The Eclipse Public License is available at
//      http://www.eclipse.org/legal/epl-v10.html
//
//      The Apache License v2.0 is available at
//      http://www.opensource.org/licenses/apache2.0.php
//
//  You may elect to redistribute this code under either of these licenses.
//  ========================================================================
//

package org.eclipse.jetty.rewrite.handler;

import java.io.IOException;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import org.eclipse.jetty.util.URIUtil;

/**
 * This rule can be used to protect against invalid unicode characters in a url making it into applications.
 *
 * The logic is as follows.
 * 
 * - if decoded uri character is an iso control character return code/reason
 * - if no UnicodeBlock is found for character return code/reason
 * - if character is in UnicodeBlock.SPECIALS return code/reason
 */
public class ValidUrlRule extends Rule
{
    String _code = "400";
    String _reason = "Illegal Url";
    
    public ValidUrlRule()
    {
        _handling = true;
        _terminating = true;
    }

    /* ------------------------------------------------------------ */
    /**
     * Sets the response status code.
     * 
     * @param code
     *            response code
     */
    public void setCode(String code)
    {
        _code = code;
    }

    /* ------------------------------------------------------------ */
    /**
     * Sets the reason for the response status code. Reasons will only reflect if the code value is greater or equal to 400.
     * 
     * @param reason
     */
    public void setReason(String reason)
    {
        _reason = reason;
    }

    @Override
    public String matchAndApply(String target, HttpServletRequest request, HttpServletResponse response) throws IOException
    {
        // best to decide the request uri and validate that
        String uri = URIUtil.decodePath(request.getRequestURI());
        
        for (int i = 0; i < uri.length(); ++i)
        {
            if (!isValidChar(uri.charAt(i)))
            {
                int code = Integer.parseInt(_code);

                // status code 400 and up are error codes so include a reason
                if (code >= 400)
                {
                    response.sendError(code,_reason);
                }
                else
                {
                    response.setStatus(code);
                }

                // we have matched, return target and consider it is handled
                return target;
            }
        }

        // we have not matched so return null
        return null;
    }

    protected boolean isValidChar(char c)
    {
        Character.UnicodeBlock block = Character.UnicodeBlock.of(c);
        
        return (!Character.isISOControl(c)) && block != null && block != Character.UnicodeBlock.SPECIALS;
    }

    public String toString()
    {
        return super.toString() + "[" + _code + ":" + _reason + "]";
    }
}
