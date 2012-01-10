package com.khotyn.varamyr;

import org.w3c.dom.*;
import org.xml.sax.SAXException;

import javax.xml.parsers.DocumentBuilder;
import javax.xml.parsers.DocumentBuilderFactory;
import javax.xml.parsers.ParserConfigurationException;
import javax.xml.transform.*;
import javax.xml.transform.dom.DOMSource;
import javax.xml.transform.stream.StreamResult;
import java.io.*;
import java.util.ArrayList;
import java.util.Enumeration;
import java.util.List;
import java.util.zip.ZipEntry;
import java.util.zip.ZipFile;
import java.util.zip.ZipOutputStream;

/**
 * User: khotyn
 * Date: 12-1-10
 * Time: PM10:19
 */
public class Varamyr {
    public static final String font = "\n@page {\n" +
            "    margin-bottom: 5pt;\n" +
            "    margin-top: 5pt\n" +
            "    }\n" +
            "@font-face {\n" +
            "    font-family: \"DroidFont\", serif, sans-serif;\n" +
            "    font-weight: normal;\n" +
            "    font-style: normal;\n" +
            "    src: url(res:///system/fonts/DroidSansFallback.ttf)\n" +
            "    }\n" +
            "@font-face {\n" +
            "    font-family: \"DroidFont\", serif, sans-serif;\n" +
            "    font-weight: bold;\n" +
            "    font-style: normal;\n" +
            "    src: url(res:///system/fonts/DroidSansFallback.ttf)\n" +
            "    }\n" +
            "@font-face {\n" +
            "    font-family: \"DroidFont\", serif, sans-serif;\n" +
            "    font-weight: normal;\n" +
            "    font-style: italic;\n" +
            "    src: url(res:///system/fonts/DroidSansFallback.ttf)\n" +
            "    }\n" +
            "@font-face {\n" +
            "    font-family: \"DroidFont\", serif, sans-serif;\n" +
            "    font-weight: bold;\n" +
            "    font-style: italic;\n" +
            "    src: url(res:///system/fonts/DroidSansFallback.ttf)\n" +
            "    }\n";

    public static void main(String args[]) throws IOException, ParserConfigurationException, SAXException, TransformerException {
        new Varamyr().parse();
    }

    public void parse() throws IOException, ParserConfigurationException, SAXException, TransformerException {
        ZipFile book = new ZipFile("/Users/apple/Downloads/1fe051f3b309c4405965903fd9ca053d.epub");
        ZipOutputStream out = new ZipOutputStream(new FileOutputStream("/Users/apple/Downloads/test.epub"));
        Enumeration entries = book.entries();
        List<String> htmls = new ArrayList<String>();
        DocumentBuilderFactory dbf = DocumentBuilderFactory.newInstance();
        DocumentBuilder db = dbf.newDocumentBuilder();
        byte[] buf = new byte[1024];

        while (entries.hasMoreElements()) {
            ZipEntry zipEntry = (ZipEntry) entries.nextElement();

            // Get the OPF file
            if (zipEntry.getName().endsWith(".opf")) {
                Document document = db.parse(book.getInputStream(zipEntry));
                Element element = document.getDocumentElement();

                // Change the book title
                NodeList nodeList = element.getElementsByTagName("item");

                if (nodeList != null && nodeList.getLength() > 0) {
                    for (int i = 0; i < nodeList.getLength(); i++) {
                        Element el = (Element) nodeList.item(i);

                        if (el.getAttribute("media-type").equals("application/xhtml+xml")) {
                            htmls.add(el.getAttribute("href"));
                        }
                    }
                }
            }
        }

        entries = book.entries();

        while (entries.hasMoreElements()) {
            ZipEntry zipEntry = (ZipEntry) entries.nextElement();
            String path = zipEntry.getName().substring(zipEntry.getName().lastIndexOf('/') + 1);

            if (htmls.contains(path)) {
                try {
                    Document document = db.parse(book.getInputStream(zipEntry));

                    Element element = document.getDocumentElement();

                    NodeList nodeList = element.getElementsByTagName("head");

                    if (nodeList != null && nodeList.getLength() == 1) {
                        Element head = (Element) nodeList.item(0);

                        Node css = document.createElement("style");
                        NamedNodeMap attributes = css.getAttributes();
                        Attr type = document.createAttribute("type");
                        type.setValue("text/css");
                        attributes.setNamedItem(type);
                        css.setTextContent(font);
                        head.appendChild(css);
                    }

                    Transformer transformer = TransformerFactory.newInstance().newTransformer();
                    transformer.setOutputProperty(OutputKeys.INDENT, "yes");

                    StreamResult result = new StreamResult(new StringWriter());
                    DOMSource source = new DOMSource(document);
                    transformer.transform(source, result);

                    out.putNextEntry(new ZipEntry(zipEntry.getName()));
                    out.write(result.toString().getBytes());
                    out.closeEntry();
                } catch (SAXException ex) {
                    System.out.println(zipEntry.getName());
                }
            } else {
                InputStream in = book.getInputStream(zipEntry);
                out.putNextEntry(new ZipEntry(zipEntry.getName()));
                int len = 0;

                while ((len = in.read(buf)) > 0) {
                    out.write(buf, 0, len);
                }

                out.closeEntry();
                in.close();
            }
        }
    }
}
