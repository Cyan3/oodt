/**
 * Licensed to the Apache Software Foundation (ASF) under one or more
 * contributor license agreements.  See the NOTICE file distributed with
 * this work for additional information regarding copyright ownership.
 * The ASF licenses this file to You under the Apache License, Version 2.0
 * (the "License"); you may not use this file except in compliance with
 * the License.  You may obtain a copy of the License at
 *
 *     http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package org.apache.oodt.cas.filemgr.webapp.browser;

//JDK imports
import java.util.logging.Logger;

//Wicket imports
import org.apache.oodt.cas.webcomponents.filemgr.FMBrowserSession;
import org.apache.wicket.Request;
import org.apache.wicket.Response;
import org.apache.wicket.Session;
import org.apache.wicket.protocol.http.WebApplication;
import org.apache.wicket.request.target.coding.MixedParamUrlCodingStrategy;

/**
 * Application object for your web application. If you want to run this
 * application without deploying, run the Start class.
 * 
 * @see org.apache.oodt.cas.filemgr.webapp.browser.Start#main(String[])
 */
public class FMBrowserApp extends WebApplication {

  private static final Logger LOG = Logger.getLogger(FMBrowserApp.class
      .getName());

  /**
   * 
   * @param fmUrlStr
   *          The String URL of the file manager to connect to.
   */
  public FMBrowserApp() {
    MixedParamUrlCodingStrategy types = new MixedParamUrlCodingStrategy(
        "types", TypesPage.class, new String[] {});
    mount(types);

    MixedParamUrlCodingStrategy typeBrowser = new MixedParamUrlCodingStrategy(
        "type", TypeBrowserPage.class, new String[] { "name", "pageNum" });
    mount(typeBrowser);

    MixedParamUrlCodingStrategy prodBrowser = new MixedParamUrlCodingStrategy(
        "product", ProductBrowserPage.class, new String[] { "id" });
    mount(prodBrowser);

  }

  /**
   * @see org.apache.wicket.Application#getHomePage()
   */
  public Class<Home> getHomePage() {
    return Home.class;
  }

 
  /*
   * (non-Javadoc)
   * 
   * @see
   * org.apache.wicket.protocol.http.WebApplication#newSession(org.apache.wicket
   * .Request, org.apache.wicket.Response)
   */
  @Override
  public Session newSession(Request request, Response response) {
    return new FMBrowserSession(request);
  }
  
  public String getFmUrlStr() {
    return getServletContext().getInitParameter("filemgr.url");
  }


}