/* ====================================================================
   Copyright 2017 Andreas Beeker (kiwiwings@apache.org)

   Licensed under the Apache License, Version 2.0 (the "License");
   you may not use this file except in compliance with the License.
   You may obtain a copy of the License at

       http://www.apache.org/licenses/LICENSE-2.0

   Unless required by applicable law or agreed to in writing, software
   distributed under the License is distributed on an "AS IS" BASIS,
   WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
   See the License for the specific language governing permissions and
   limitations under the License.
==================================================================== */

package de.kiwiwings.poi.visualizer;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.AnnotationConfigApplicationContext;
import org.springframework.context.support.AbstractApplicationContext;
import org.springframework.stereotype.Component;

@Component
public class POIVisualizer {

	@Autowired
	private POIMainFrame frame;
	
	public void start() throws InterruptedException {
		// postConstruct is not called
		frame.init();
		frame.setVisible(true);
		synchronized (frame) {
			frame.wait();
		}
	}
	
	public static void main(String[] args) throws Exception {
    	final String pckName = POIVisualizer.class.getPackage().getName();
    	try (AbstractApplicationContext ctx = new AnnotationConfigApplicationContext(pckName)) {
    		POIVisualizer visualizer = ctx.getBean(POIVisualizer.class);
    		visualizer.start();
    	}

		System.out.println("Exiting");
    }
}
