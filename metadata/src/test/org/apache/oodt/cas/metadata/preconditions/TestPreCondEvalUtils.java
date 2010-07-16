/*
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


package org.apache.oodt.cas.metadata.preconditions;

//JDK imports
import java.io.File;
import java.io.FileNotFoundException;
import java.util.LinkedList;

import org.springframework.context.support.FileSystemXmlApplicationContext;

//Junit imports
import junit.framework.TestCase;

/**
 * 
 * @author bfoster
 * @author mattmann
 * @version $Revision$
 * 
 * <p>
 * Test suite for the {@link PreCondEvalUtils} class
 * </p>.
 */
public class TestPreCondEvalUtils extends TestCase {

    LinkedList<String> preconditions;
    
    private PreCondEvalUtils evalUtils;
    
    private static final String MET_EXTR_TEST_FILE = "./src/main/resources/examples/met_extr_preconditions.xml";

    public TestPreCondEvalUtils() throws FileNotFoundException,
            ClassNotFoundException, InstantiationException,
            IllegalAccessException {
        this.preconditions = new LinkedList<String>();
        this.preconditions.add("CheckThatDataFileSizeIsGreaterThanZero");
        this.preconditions.add("CheckThatDataFileExists");
        this.preconditions.add("CheckDataFileMimeType");
        this.evalUtils = new PreCondEvalUtils(new FileSystemXmlApplicationContext(MET_EXTR_TEST_FILE));
    }

    public void testEval(){
        File prodFile = new File(MET_EXTR_TEST_FILE);
        try{
            assertTrue(this.evalUtils.eval(this.preconditions, prodFile));
        }
        catch(Throwable e){
            e.printStackTrace();
            fail(e.getMessage());
        }
    }

}
