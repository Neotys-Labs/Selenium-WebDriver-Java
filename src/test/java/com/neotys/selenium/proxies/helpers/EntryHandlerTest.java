/*
 * Copyright (c) 2016, Neotys
 * All rights reserved.
 *
 * Redistribution and use in source and binary forms, with or without
 * modification, are permitted provided that the following conditions are met:
 *
 *     * Redistributions of source code must retain the above copyright
 *       notice, this list of conditions and the following disclaimer.
 *     * Redistributions in binary form must reproduce the above copyright
 *       notice, this list of conditions and the following disclaimer in the
 *       documentation and/or other materials provided with the distribution.
 *     * Neither the name of Neotys nor the names of its contributors may be
 *       used to endorse or promote products derived from this software
 *       without specific prior written permission.
 *
 * THIS SOFTWARE IS PROVIDED BY THE COPYRIGHT HOLDERS AND CONTRIBUTORS "AS IS" AND
 * ANY EXPRESS OR IMPLIED WARRANTIES, INCLUDING, BUT NOT LIMITED TO, THE IMPLIED
 * WARRANTIES OF MERCHANTABILITY AND FITNESS FOR A PARTICULAR PURPOSE ARE
 * DISCLAIMED. IN NO EVENT SHALL NEOTYS BE LIABLE FOR ANY
 * DIRECT, INDIRECT, INCIDENTAL, SPECIAL, EXEMPLARY, OR CONSEQUENTIAL DAMAGES
 * (INCLUDING, BUT NOT LIMITED TO, PROCUREMENT OF SUBSTITUTE GOODS OR SERVICES;
 * LOSS OF USE, DATA, OR PROFITS; OR BUSINESS INTERRUPTION) HOWEVER CAUSED AND
 * ON ANY THEORY OF LIABILITY, WHETHER IN CONTRACT, STRICT LIABILITY, OR TORT
 * (INCLUDING NEGLIGENCE OR OTHERWISE) ARISING IN ANY WAY OUT OF THE USE OF THIS
 * SOFTWARE, EVEN IF ADVISED OF THE POSSIBILITY OF SUCH DAMAGE.
 */
package com.neotys.selenium.proxies.helpers;

import static org.junit.Assert.assertEquals;

import java.util.List;

import org.junit.Test;

/**
 * @author aaron
 *
 */
public class EntryHandlerTest {

    /** Make sure the path is split by slashes (/).
     * Test method for {@link com.neotys.selenium.proxies.helpers.EntryHandler#createPath(java.lang.String, java.lang.String)}.
     */
    @Test
    public void testCreatePathURLSplit() {
        SeleniumProxyConfig proxyConfig = new SeleniumProxyConfig("Internal JUnit Test Driver");
        final EntryHandler entryHandler = EntryHandler.start(proxyConfig);
        
        final List<String> path = entryHandler.createPath("current/url", "pageTitle");
        
        assertEquals("Path doesn't have enough parts.", 4, path.size());
        assertEquals("End of the path is wrong.", "url", path.get(path.size() - 1));
    }

    /** Make sure the path is split by slashes (/).
     * Test method for {@link com.neotys.selenium.proxies.helpers.EntryHandler#createPath(java.lang.String, java.lang.String)}.
     */
    @Test
    public void testCreatePathCustomNameSplit() {
        SeleniumProxyConfig proxyConfig = new SeleniumProxyConfig("Internal JUnit Test Driver");
        final EntryHandler entryHandler = EntryHandler.start(proxyConfig);
        
        proxyConfig.setCustomName("bob/cobb/custom/name");
        
        final List<String> path = entryHandler.createPath("current/url", "pageTitle/something");
        
        assertEquals("Path doesn't have enough parts.", 6, path.size());
        assertEquals("End of the path is wrong.", "name", path.get(path.size() - 1));
    }

    /** Make sure the path is split by slashes (/).
     * Test method for {@link com.neotys.selenium.proxies.helpers.EntryHandler#createPath(java.lang.String, java.lang.String)}.
     */
    @Test
    public void testCreatePathTitleSplit() {
        try {
            System.setProperty(SeleniumProxyConfig.OPT_PATH_NAMING_POLICY, "Title");
            
            SeleniumProxyConfig proxyConfig = new SeleniumProxyConfig("Internal JUnit Test Driver");
            final EntryHandler entryHandler = EntryHandler.start(proxyConfig);
            
            final List<String> path = entryHandler.createPath("current/url", "bob/pageTitle/something");
            
            assertEquals("Path doesn't have enough parts.", 5, path.size());
            assertEquals("End of the path is wrong.", "something", path.get(path.size() - 1));
        } finally {
            System.clearProperty(SeleniumProxyConfig.OPT_PATH_NAMING_POLICY);
        }
    }
}
