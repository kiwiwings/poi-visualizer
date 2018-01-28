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

package de.kiwiwings.poi.visualizer.xmleditor;

import java.io.IOException;
import java.io.StringReader;
import java.io.StringWriter;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import javax.swing.JScrollPane;
import javax.swing.SwingUtilities;
import javax.swing.text.Document;
import javax.xml.parsers.DocumentBuilderFactory;
import javax.xml.parsers.ParserConfigurationException;

import org.exbin.utils.binary_data.ByteArrayEditableData;
import org.springframework.beans.factory.InitializingBean;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;
import org.w3c.dom.DOMConfiguration;
import org.w3c.dom.DOMImplementation;
import org.w3c.dom.ls.DOMImplementationLS;
import org.w3c.dom.ls.LSOutput;
import org.w3c.dom.ls.LSSerializer;
import org.xml.sax.InputSource;
import org.xml.sax.SAXException;

import de.kiwiwings.poi.visualizer.treemodel.TreeModelLoadException;
import de.kiwiwings.poi.visualizer.treemodel.TreeObservable;
import de.kiwiwings.poi.visualizer.treemodel.TreeObservable.SourceType;

@SuppressWarnings("serial")
@Component(value="xmlEditor")
public class XMLEditor extends JScrollPane implements InitializingBean {

	@Autowired
	private TreeObservable treeObservable;

	@Autowired
	private XmlTextPane xmlPane;

	@Override
	public void afterPropertiesSet() {
		xmlPane.setEditable(false);
		xmlPane.setText("");
		setViewportView(xmlPane);

		treeObservable.addObserver((o,arg) -> SwingUtilities.invokeLater(() -> contentChanged()));
	}

	public void prettyPrint() {
		final String xml = xmlPane.getText();
		final InputSource src = new InputSource(new StringReader(xml));
        org.w3c.dom.Document document;
		try {
			document = DocumentBuilderFactory.newInstance().newDocumentBuilder().parse(src);
		} catch (SAXException | IOException | ParserConfigurationException e) {
			return;
		}
		
		final Pattern p = Pattern.compile("^<\\?xml.*\\?>");
		final Matcher m = p.matcher(xml);
		final String xmlDecl = (m.find()) ? m.group() : "";
		
		// Pretty-prints a DOM document to XML using DOM Load and Save's LSSerializer.
		// Note that the "format-pretty-print" DOM configuration parameter can only be set in JDK 1.6+.
        final DOMImplementation domImplementation = document.getImplementation();
		if (!domImplementation.hasFeature("LS", "3.0") || !domImplementation.hasFeature("Core", "2.0")) {
			return;
		}
		final DOMImplementationLS domImplementationLS = (DOMImplementationLS) domImplementation.getFeature("LS", "3.0");
		final LSSerializer lsSerializer = domImplementationLS.createLSSerializer();
		final DOMConfiguration domConfiguration = lsSerializer.getDomConfig();
		if (domConfiguration.canSetParameter("format-pretty-print", true)) {
			domConfiguration.setParameter("format-pretty-print", true);
		}
		if (domConfiguration.canSetParameter("xml-declaration", false)) {
			domConfiguration.setParameter("xml-declaration", false);
		}
		
		final LSOutput lsOutput = domImplementationLS.createLSOutput();
		lsOutput.setEncoding("UTF-8");
		final StringWriter stringWriter = new StringWriter();
		if (!xmlDecl.isEmpty()) {
			stringWriter.write(xmlDecl);
			stringWriter.write("\n");
		}
		lsOutput.setCharacterStream(stringWriter);
		lsSerializer.write(document, lsOutput);
		final String xmlOut = stringWriter.toString();
		xmlPane.setText(xmlOut);
	}

	public void saveXml() {
		treeObservable.setBinarySource(() -> new ByteArrayEditableData(xmlPane.getText().getBytes("UTF-8")));
	}
	
	private void contentChanged() {
		final Document doc = xmlPane.getDocument();

		if (treeObservable.getSourceType() != SourceType.text_xml) {
			xmlPane.setEditable(false);
			xmlPane.setText("");
		} else {
			xmlPane.setEditable(true);
			xmlPane.setText(convertBytes());
		}
	}
	
	private String convertBytes() {
		// TODO: handle BOM for UTF-8/16?
		try {
			ByteArrayEditableData binData = treeObservable.getBinarySource().getBinaryData();
			return new String(binData.getData(), "UTF-8");
		} catch (TreeModelLoadException|IOException e) {
			return "Error in loading content";
		}
	}
}
